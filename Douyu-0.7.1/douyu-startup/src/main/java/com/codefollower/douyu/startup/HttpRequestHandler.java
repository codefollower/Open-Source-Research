/*
 * Copyright 2009 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.codefollower.douyu.startup;

import static com.codefollower.douyu.http.HttpHeaders.*;
import static com.codefollower.douyu.http.HttpHeaders.Names.*;
import static com.codefollower.douyu.http.HttpHeaders.Values.WEBSOCKET;
import static com.codefollower.douyu.http.HttpResponseStatus.*;
import static com.codefollower.douyu.http.HttpVersion.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;


import com.codefollower.douyu.core.ClassResource;
import com.codefollower.douyu.core.Config;
import com.codefollower.douyu.core.JavacException;
import com.codefollower.douyu.http.Cookie;
import com.codefollower.douyu.http.CookieDecoder;
import com.codefollower.douyu.http.CookieEncoder;
import com.codefollower.douyu.http.DefaultHttpDataFactory;
import com.codefollower.douyu.http.DefaultHttpResponse;
import com.codefollower.douyu.http.DiskAttribute;
import com.codefollower.douyu.http.DiskFileUpload;
import com.codefollower.douyu.http.DouyuHttpRequest;
import com.codefollower.douyu.http.DouyuHttpResponse;
import com.codefollower.douyu.http.DouyuOutbound;
import com.codefollower.douyu.http.HttpChunk;
import com.codefollower.douyu.http.HttpChunkTrailer;
import com.codefollower.douyu.http.HttpDataFactory;
import com.codefollower.douyu.http.HttpHeaders;
import com.codefollower.douyu.http.HttpMessage;
import com.codefollower.douyu.http.HttpMethod;
import com.codefollower.douyu.http.HttpPostRequestDecoder;
import com.codefollower.douyu.http.HttpResponse;
import com.codefollower.douyu.http.HttpResponseStatus;
import com.codefollower.douyu.http.HttpVersion;
import com.codefollower.douyu.http.HttpHeaders.Names;
import com.codefollower.douyu.http.HttpHeaders.Values;
import com.codefollower.douyu.http.HttpPostRequestDecoder.ErrorDataDecoderException;
import com.codefollower.douyu.http.util.FastHttpDateFormat;
import com.codefollower.douyu.http.websocket.WebSocketFrame;
import com.codefollower.douyu.http.websocket.WebSocketFrameDecoder;
import com.codefollower.douyu.http.websocket.WebSocketFrameEncoder;
import com.codefollower.douyu.mvc.DouyuContext;
import com.codefollower.douyu.mvc.DouyuContext.AsyncCallback;
import com.codefollower.douyu.netty.buffer.ChannelBuffer;
import com.codefollower.douyu.netty.buffer.ChannelBuffers;
import com.codefollower.douyu.netty.channel.Channel;
import com.codefollower.douyu.netty.channel.ChannelFuture;
import com.codefollower.douyu.netty.channel.ChannelFutureListener;
import com.codefollower.douyu.netty.channel.ChannelHandlerContext;
import com.codefollower.douyu.netty.channel.ChannelPipeline;
import com.codefollower.douyu.netty.channel.ChannelStateEvent;
import com.codefollower.douyu.netty.channel.Channels;
import com.codefollower.douyu.netty.channel.ExceptionEvent;
import com.codefollower.douyu.netty.channel.MessageEvent;
import com.codefollower.douyu.netty.channel.SimpleChannelUpstreamHandler;

/**
 * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
 * @author Andy Taylor (andy.taylor@jboss.org)
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 * @author ZHH
 * 
 */
public class HttpRequestHandler extends SimpleChannelUpstreamHandler implements AsyncCallback {
	// Disk if size exceed MINSIZE
	private static final HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);

	static {
		DiskFileUpload.deleteOnExitTemporaryFile = true; // should delete file
		// on exit (in normal exit)
		DiskFileUpload.baseDirectory = null; // system temp directory
		DiskAttribute.deleteOnExitTemporaryFile = true; // should delete file on
		// exit (in normal exit)
		DiskAttribute.baseDirectory = null; // system temp directory
	}

	private HttpPostRequestDecoder decoder = null;

	private boolean isWebSocket = false;

	private volatile Channel channel;

	private DouyuContext firstDouyuContext;
	private List<DouyuContext> otherDouyuContexts;

	private DouyuHttpRequest currentRequest;

	private Config config;

	public HttpRequestHandler(Config config) {
		this.config = config;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		channel = e.getChannel();
		Object msg = e.getMessage();
		if (msg instanceof DouyuHttpRequest) {
			currentRequest = (DouyuHttpRequest) msg;
			// Serve the WebSocket handshake request.
			if (Values.UPGRADE.equalsIgnoreCase(currentRequest.getHeader(CONNECTION))
					&& WEBSOCKET.equalsIgnoreCase(currentRequest.getHeader(Names.UPGRADE))) {
				isWebSocket = true;

				handleWebSocketHandshake(ctx);
			}

			handleHttpRequest(ctx);
		} else if (msg instanceof HttpChunk) {
			handleHttpChunk(ctx, (HttpChunk) msg);
		} else if (msg instanceof WebSocketFrame) {
			handleWebSocketFrame(ctx, (WebSocketFrame) msg);
		}
	}

	private void handleWebSocketHandshake(ChannelHandlerContext ctx) throws Exception {
		// Create the WebSocket handshake response.
		HttpResponse res = new DefaultHttpResponse(HTTP_1_1, new HttpResponseStatus(101,
				"Web Socket Protocol Handshake"));
		res.addHeader(Names.UPGRADE, WEBSOCKET);
		res.addHeader(CONNECTION, Values.UPGRADE);

		// Fill in the headers and contents depending on handshake method.
		if (currentRequest.containsHeader(SEC_WEBSOCKET_KEY1) && currentRequest.containsHeader(SEC_WEBSOCKET_KEY2)) {
			// New handshake method with a challenge:
			res.addHeader(SEC_WEBSOCKET_ORIGIN, currentRequest.getHeader(ORIGIN));
			res.addHeader(SEC_WEBSOCKET_LOCATION, "ws://" + currentRequest.getHeader(HttpHeaders.Names.HOST)
					+ currentRequest.getUri());
			String protocol = currentRequest.getHeader(SEC_WEBSOCKET_PROTOCOL);
			if (protocol != null) {
				res.addHeader(SEC_WEBSOCKET_PROTOCOL, protocol);
			}

			// Calculate the answer of the challenge.
			String key1 = currentRequest.getHeader(SEC_WEBSOCKET_KEY1);
			String key2 = currentRequest.getHeader(SEC_WEBSOCKET_KEY2);
			int a = (int) (Long.parseLong(key1.replaceAll("[^0-9]", "")) / key1.replaceAll("[^ ]", "").length());
			int b = (int) (Long.parseLong(key2.replaceAll("[^0-9]", "")) / key2.replaceAll("[^ ]", "").length());
			long c = currentRequest.getContent().readLong();
			ChannelBuffer input = ChannelBuffers.buffer(16);
			input.writeInt(a);
			input.writeInt(b);
			input.writeLong(c);
			ChannelBuffer output = ChannelBuffers.wrappedBuffer(MessageDigest.getInstance("MD5").digest(input.array()));
			res.setContent(output);
		} else {
			// Old handshake method with no challenge:
			res.addHeader(WEBSOCKET_ORIGIN, currentRequest.getHeader(ORIGIN));
			res.addHeader(WEBSOCKET_LOCATION, "ws://" + currentRequest.getHeader(HttpHeaders.Names.HOST)
					+ currentRequest.getUri());
			String protocol = currentRequest.getHeader(WEBSOCKET_PROTOCOL);
			if (protocol != null) {
				res.addHeader(WEBSOCKET_PROTOCOL, protocol);
			}
		}

		// Upgrade the connection and send the handshake response.
		ChannelPipeline p = ctx.getChannel().getPipeline();
		// p.remove("aggregator");
		p.replace("decoder", "wsdecoder", new WebSocketFrameDecoder());

		ctx.getChannel().write(res);

		p.replace("encoder", "wsencoder", new WebSocketFrameEncoder());
	}

	private void handleHttpRequest(ChannelHandlerContext ctx) throws Exception {
		if (is100ContinueExpected(currentRequest)) {
			send100Continue();
		}

		HttpMethod method = currentRequest.getMethod();
		if (method.equals(HttpMethod.POST) || method.equals(HttpMethod.PUT)) {
			// clean previous FileUpload if Any
			if (decoder != null) {
				decoder.cleanFiles();
				decoder = null;
			}

			try {
				decoder = new HttpPostRequestDecoder(factory, currentRequest);
			} catch (ErrorDataDecoderException e) {
				writeResponse(null, e);
				Channels.close(channel);
				return;
			}
		}

		if (!currentRequest.isChunked()) {
			DouyuHttpRequest request = currentRequest;
			currentRequest = null;

			executeAction(request, new DouyuHttpResponse());
		}
	}

	private void handleHttpChunk(ChannelHandlerContext ctx, HttpChunk chunk) throws Exception {
		// Sanity check
		if (currentRequest == null) {
			throw new IllegalStateException("received " + HttpChunk.class.getSimpleName() + " without "
					+ HttpMessage.class.getSimpleName());
		}
		if (decoder != null) {
			try {
				decoder.offer(chunk);
			} catch (ErrorDataDecoderException e1) {
				writeResponse(null, e1);
				Channels.close(channel);
				return;
			}
		} else {
			ChannelBuffer content = currentRequest.getContent();
			content.writeBytes(chunk.getContent());
		}

		if (chunk.isLast()) {
			// Merge trailing headers into the message.
			if (chunk instanceof HttpChunkTrailer) {
				HttpChunkTrailer trailer = (HttpChunkTrailer) chunk;
				for (Entry<String, String> header : trailer.getHeaders()) {
					currentRequest.setHeader(header.getKey(), header.getValue());
				}
			}

			DouyuHttpRequest request = currentRequest;
			currentRequest = null;

			executeAction(request, new DouyuHttpResponse());
		}
	}

	private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
		// Send the uppercased string back.
		// ctx.getChannel().write(new
		// DefaultWebSocketFrame(frame.getTextData().toUpperCase()));
		if (firstDouyuContext != null && firstDouyuContext.getWebSocket() != null) {
			if (frame.isBinary()) {
				byte[] data = null;
				try {
					data = frame.getTextData().getBytes("UTF-8");
				} catch (UnsupportedEncodingException e) {
				}
				firstDouyuContext.getWebSocket().onMessage((byte) frame.getType(), data);
			} else {
				firstDouyuContext.getWebSocket().onMessage((byte) frame.getType(), frame.getTextData());
			}
		}

	}

	private void executeAction(DouyuHttpRequest request, DouyuHttpResponse response) throws Exception {
		if (!prepareRequest(request, response)) {
			writeResponse(request, response);
			return;
		}

		String path = request.getRequestURI();

		// 以'/'结尾说明是一个目录
		if (path.endsWith("/")) {
			return; // TODO 处理静态资源
		}

		// path格式: /packageName/controllerClassName.actionName
		if (path.startsWith("/"))
			path = path.substring(1);

		String controllerClassName = path;
		String actionName = null;
		int dotPos = path.indexOf('.');
		if (dotPos >= 0) {
			actionName = path.substring(dotPos + 1).trim();
			controllerClassName = path.substring(0, dotPos);
		}

		controllerClassName = controllerClassName.replace('/', '.');

		StringWriter sw = new StringWriter();
		PrintWriter javacOut = new PrintWriter(sw);

		ClassResource cr = null;
		DouyuContext dc = null;
		boolean isAsync = false;
		// boolean error = false;
		try {
			cr = config.getResourceLoader().loadContextClassResource(controllerClassName, javacOut);
			if (cr == null) {
				return; // TODO 处理静态资源
			}
			dc = (DouyuContext) cr.loadedClass.newInstance();
			if (isWebSocket)
				dc.setOutbound(new DouyuOutbound(channel));
			dc.init(config, controllerClassName, request, response);
			dc.setAsyncCallback(this);
			if (decoder != null)
				dc.setHttpPostRequestDecoder(decoder);
			addDouyuContext(dc);

			if (dc.isAsyncAction(actionName)) {
				isAsync = true;
				// ac.setAsyncCallback(this);
				dc.executeAsyncAction(actionName);
			} else {
				dc.executeAction(actionName);
				if (!isWebSocket)
					writeResponse(dc);
			}
		} catch (JavacException e) {
			printJavacMessage(sw.toString(), response, e);
			// error = true;
			writeResponse(dc, e);
		} finally {
			// 即不是异步，也不是websocket，则马上释放AbstractContext
			if (dc != null && !isAsync && !isWebSocket)
				dc.free();

			isWebSocket = false;
		}
	}

	boolean isSSLEnabled = false; // TODO

	protected boolean prepareRequest(DouyuHttpRequest request, DouyuHttpResponse response) {
		String uri = request.getRequestURI();
		String host = null;
		String queryString = null;
		int questionPos = uri.indexOf('?', 0);
		if (questionPos != -1) {
			queryString = uri.substring(questionPos + 1);
			uri = uri.substring(0, questionPos);
		}
		if (uri.toLowerCase().startsWith("http", 0)) {
			int pos = uri.indexOf("://");
			if (pos != -1) {
				int slashPos = uri.indexOf('/', pos + 3);
				if (slashPos == -1) {
					slashPos = uri.length();
					// Set URI as "/"
					request.setRequestURI("/");
				} else {
					request.setRequestURI(uri.substring(slashPos, uri.length() - slashPos));
				}
				host = uri.substring(pos + 3, slashPos);
				request.setHeader(Names.HOST, host);
			}
		} else {
			host = request.getHeader(Names.HOST);
		}

		if (host == null && request.getProtocolVersion().equals(HttpVersion.HTTP_1_1)) {
			response.setStatus(BAD_REQUEST);
			return false;
		}

		int serverPort = -1;
		String serverName = null;

		if (host == null || host.isEmpty()) {
			serverPort = ((InetSocketAddress) channel.getLocalAddress()).getPort();

			// TODO 优化一下，只用IP地址表示
			serverName = ((InetSocketAddress) channel.getLocalAddress()).getHostName();
		} else {
			boolean ipv6 = (host.charAt(0) == '[');
			boolean bracketClosed = false;
			int colonPos = -1;

			for (int i = 0; i < host.length(); i++) {
				char b = host.charAt(i);
				if (b == ']') {
					bracketClosed = true;
				} else if (b == ':') {
					if (!ipv6 || bracketClosed) {
						colonPos = i;
						break;
					}
				}
			}

			if (colonPos < 0) {
				if (!isSSLEnabled) {
					// 80 - Default HTTP port
					serverPort = 80;
				} else {
					// 443 - Default HTTPS port
					serverPort = 443;
				}
				serverName = host;
			} else {
				serverName = host.substring(0, colonPos);

				int port = 0;
				int mult = 1;
				for (int i = host.length() - 1; i > colonPos; i--) {
					int charValue = HexUtils.getDec(host.charAt(i));
					if (charValue == -1 || charValue > 9) {
						// Invalid character
						// 400 - Bad request
						response.setStatus(BAD_REQUEST);
						return false;
					}
					port = port + (charValue * mult);
					mult = 10 * mult;
				}
				serverPort = port;
			}
		}

		// TODO character encoding外部可配置
		String requestURI = request.getRequestURI();
		try {
			requestURI = URLDecoder.decode(requestURI, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			try {
				requestURI = URLDecoder.decode(requestURI, "ISO-8859-1");
			} catch (UnsupportedEncodingException e1) {
				// TODO log
			}
		}
		request.setRequestURI(requestURI);
		// TODO
		// if (normalize(request)) {
		// response.setStatus(BAD_REQUEST);
		// return false;
		// }

		request.setQueryString(queryString);
		request.setServerPort(serverPort);
		request.setServerName(serverName);

		return true;
	}

	protected static final boolean ALLOW_BACKSLASH = Boolean.valueOf(
			System.getProperty("com.codefollower.douyu.http.ALLOW_BACKSLASH", "false")).booleanValue();

	public static boolean normalize(DouyuHttpRequest request) {
		final char[] b = request.getRequestURI().toCharArray();
		final int start = 0;
		int end = b.length;

		// An empty URL is not acceptable
		if (start == end)
			return false;

		// URL * is acceptable
		if ((end - start == 1) && b[start] == (byte) '*')
			return true;

		int pos = 0;
		int index = 0;

		// Replace '\' with '/'
		// Check for null byte
		for (pos = start; pos < end; pos++) {
			if (b[pos] == (byte) '\\') {
				if (ALLOW_BACKSLASH) {
					b[pos] = (byte) '/';
				} else {
					return false;
				}
			}
			if (b[pos] == (byte) 0) {
				return false;
			}
		}

		// The URL must start with '/'
		if (b[start] != (byte) '/') {
			return false;
		}

		// Replace "//" with "/"
		for (pos = start; pos < (end - 1); pos++) {
			if (b[pos] == (byte) '/') {
				while ((pos + 1 < end) && (b[pos + 1] == (byte) '/')) {
					copyChars(b, pos, pos + 1, end - pos - 1);
					end--;
				}
			}
		}

		// If the URI ends with "/." or "/..", then we append an extra "/"
		// Note: It is possible to extend the URI by 1 without any side effect
		// as the next character is a non-significant WS.
		if (((end - start) >= 2) && (b[end - 1] == (byte) '.')) {
			if ((b[end - 2] == (byte) '/') || ((b[end - 2] == (byte) '.') && (b[end - 3] == (byte) '/'))) {
				b[end] = (byte) '/';
				end++;
			}
		}

		index = 0;

		String uri = new String(b, 0, end);

		// Resolve occurrences of "/./" in the normalized path
		while (true) {
			index = uri.indexOf("/./", index);
			if (index < 0)
				break;
			copyChars(b, start + index, start + index + 2, end - start - index - 2);
			end = end - 2;
		}

		index = 0;

		uri = new String(b, 0, end);

		// Resolve occurrences of "/../" in the normalized path
		while (true) {
			index = uri.indexOf("/../", index);
			if (index < 0)
				break;
			// Prevent from going outside our context
			if (index == 0)
				return false;
			int index2 = -1;
			for (pos = start + index - 1; (pos >= 0) && (index2 < 0); pos--) {
				if (b[pos] == (byte) '/') {
					index2 = pos;
				}
			}
			copyChars(b, start + index2, start + index + 3, end - start - index - 3);
			end = end + index2 - index - 3;
			index = index2;
		}

		request.setRequestURI(new String(b, 0, end));

		return checkNormalize(request);

	}

	private static void copyChars(char[] b, int dest, int src, int len) {
		for (int pos = 0; pos < len; pos++) {
			b[pos + dest] = b[pos + src];
		}
	}

	public static boolean checkNormalize(DouyuHttpRequest request) {

		char[] c = request.getRequestURI().toCharArray();
		int start = 0;
		int end = c.length;

		int pos = 0;

		// Check for '\' and 0
		for (pos = start; pos < end; pos++) {
			if (c[pos] == '\\') {
				return false;
			}
			if (c[pos] == 0) {
				return false;
			}
		}

		// Check for "//"
		for (pos = start; pos < (end - 1); pos++) {
			if (c[pos] == '/') {
				if (c[pos + 1] == '/') {
					return false;
				}
			}
		}

		// Check for ending with "/." or "/.."
		if (((end - start) >= 2) && (c[end - 1] == '.')) {
			if ((c[end - 2] == '/') || ((c[end - 2] == '.') && (c[end - 3] == '/'))) {
				return false;
			}
		}

		// Check for "/./"
		if (request.getRequestURI().indexOf("/./", 0) >= 0) {
			return false;
		}

		// Check for "/../"
		if (request.getRequestURI().indexOf("/../", 0) >= 0) {
			return false;
		}

		return true;

	}

	protected void parseURI(DouyuHttpRequest request) {
		String uri = request.getUri();
		String host = null;
		if (uri.toLowerCase().startsWith("http", 0)) {

			try {
				host = new URI(request.getUri()).getHost();
			} catch (URISyntaxException e) {
				// TODO
				e.printStackTrace();
			}
		} else {
			host = request.getHeader("host");
		}

		if (host == null && request.getProtocolVersion().equals(HttpVersion.HTTP_1_1)) {
			// TODO
		}

	}

	protected void prepareResponse(DouyuHttpRequest request, DouyuHttpResponse response) {
		boolean keepAlive = true;
		boolean entityBody = true;
		int statusCode = response.getStatus().getCode();

		if ((statusCode == 204) || (statusCode == 205) || (statusCode == 304)
				|| (request.getHttpMethod() == douyu.http.HttpMethod.HEAD)) {
			entityBody = false;
		}

		ChannelBuffer content = response.getContent();
		int contentLength = -1;
		if (content != null)
			contentLength = content.readableBytes();

		if (entityBody) {
			if (contentLength >= 0) {
				response.setHeader("Content-Length", String.valueOf(contentLength));
			} else if (request.getProtocolVersion().equals(HttpVersion.HTTP_1_1)) {
				response.setChunked(true);
				response.setHeader(HttpHeaders.Names.TRANSFER_ENCODING, HttpHeaders.Values.CHUNKED);
			} else {
				keepAlive = false;
			}
		}

		keepAlive = keepAlive && !statusDropsConnection(statusCode);
		if (keepAlive
				&& HttpHeaders.Values.KEEP_ALIVE.equalsIgnoreCase(request.getHeader(HttpHeaders.Names.CONNECTION))) {
			response.setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
		} else {
			response.setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
		}
		response.setHeader("Date", FastHttpDateFormat.getCurrentDate());
		String server = response.getHeader("Server");
		if (server == null)
			response.setHeader("Server", "Douyu/1.1");

	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		if (firstDouyuContext != null && firstDouyuContext.getWebSocket() != null) {
			firstDouyuContext.getWebSocket().onDisconnect();
		}
		firstDouyuContext = null;
		// ctx.sendUpstream(e);
		e.getChannel().close();
	}

	private void send100Continue() {
		HttpResponse response = new DefaultHttpResponse(HTTP_1_1, CONTINUE);
		channel.write(response);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		Throwable t = e.getCause();
		Channel channel = e.getChannel();
		t.printStackTrace();

		if (channel.isOpen()) {
			if (t instanceof IOException) {
				channel.close();
			} else {
				DouyuHttpResponse response = new DouyuHttpResponse();
				response.setStatus(BAD_REQUEST);
				channel.write(response);
				channel.close();
			}
		}
	}

	private static void printJavacMessage(String javacMessage, douyu.http.HttpResponse response, JavacException e)
			throws Exception {
		PrintWriter out = response.getWriter();
		if (javacMessage.length() > 0) {
			out.println();
			out.println("javac message:");
			out.println("-----------------------------------");
			out.println(javacMessage);
		}

		if (e != null) {
			out.println();
			out.println("javac error:");
			out.println("-----------------------------------");
			e.printStackTrace(out);
		}
	}

	private void addDouyuContext(DouyuContext douyuContext) {
		if (douyuContext == null) {
			throw new NullPointerException("douyuContext");
		}
		synchronized (this) {
			if (firstDouyuContext == null) {
				firstDouyuContext = douyuContext;
			} else {
				if (otherDouyuContexts == null) {
					otherDouyuContexts = new ArrayList<DouyuContext>(1);
				}
				otherDouyuContexts.add(douyuContext);
			}
		}
	}

	private void removeDouyuContext(DouyuContext douyuContext) {
		if (douyuContext == null) {
			throw new NullPointerException("douyuContext");
		}

		synchronized (this) {
			if (douyuContext == firstDouyuContext) {
				if (otherDouyuContexts != null && !otherDouyuContexts.isEmpty()) {
					firstDouyuContext = otherDouyuContexts.remove(0);
				} else {
					firstDouyuContext = null;
				}
			} else if (otherDouyuContexts != null) {
				otherDouyuContexts.remove(douyuContext);
			}
		}

		douyuContext.free();
	}

	public void writeResponse(DouyuContext douyuContext) {
		try {
			DouyuHttpRequest request = douyuContext.getHttpRequest();
			DouyuHttpResponse response = douyuContext.getHttpResponse();

			writeResponse(request, response);
		} finally {
			if (douyuContext != null)
				removeDouyuContext(douyuContext);
		}
	}

	public void writeResponse(DouyuHttpRequest request, DouyuHttpResponse response) {
		prepareResponse(request, response);

		// Decide whether to close the connection or not.
		boolean keepAlive = isKeepAlive(request);

		// Build the response object.
		// HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
		// response.setContent(ChannelBuffers.copiedBuffer(buf.toString(),
		// CharsetUtil.UTF_8));
		// response.setContent(response.getContent());
		if (response.getContentType() != null)
			response.setHeader(CONTENT_TYPE, response.getContentType());
		else
			response.setHeader(CONTENT_TYPE, "text/plain; charset=UTF-8");

		if (keepAlive) {
			// Add 'Content-Length' header only for a keep-alive connection.
			response.setHeader(CONTENT_LENGTH, response.getContent().readableBytes());
		}

		// Encode the cookie.
		String cookieString = request.getHeader(COOKIE);
		if (cookieString != null) {
			CookieDecoder cookieDecoder = new CookieDecoder();
			Set<Cookie> cookies = cookieDecoder.decode(cookieString);
			if (!cookies.isEmpty()) {
				// Reset the cookies if necessary.
				CookieEncoder cookieEncoder = new CookieEncoder(true);
				for (Cookie cookie : cookies) {
					cookieEncoder.addCookie(cookie);
				}
				response.addHeader(SET_COOKIE, cookieEncoder.encode());
			}
		}

		// Write the response.
		ChannelFuture future = this.channel.write(response);

		// Close the non-keep-alive connection after the write operation is
		// done.
		if (!keepAlive) {
			future.addListener(ChannelFutureListener.CLOSE);
		}
	}

	// TODO
	public void writeResponse(DouyuContext douyuContext, Exception e) {
		try {
			e.printStackTrace();
			DouyuHttpRequest request = douyuContext.getHttpRequest();
			DouyuHttpResponse response = douyuContext.getHttpResponse();

			prepareResponse(request, response);

			// Build the response object.
			HttpResponse response2 = new DefaultHttpResponse(HTTP_1_1, OK);
			// response.setContent(this.response.getContent());
			if (response.getContentType() != null)
				response2.setHeader(CONTENT_TYPE, response.getContentType());
			else
				response2.setHeader(CONTENT_TYPE, "text/plain; charset=UTF-8");

			// Encode the cookie.
			String cookieString = request.getHeader(COOKIE);
			if (cookieString != null) {
				CookieDecoder cookieDecoder = new CookieDecoder();
				Set<Cookie> cookies = cookieDecoder.decode(cookieString);
				if (!cookies.isEmpty()) {
					// Reset the cookies if necessary.
					CookieEncoder cookieEncoder = new CookieEncoder(true);
					for (Cookie cookie : cookies) {
						cookieEncoder.addCookie(cookie);
					}
					response2.addHeader(SET_COOKIE, cookieEncoder.encode());
				}
			}

			// Write the response.
			ChannelFuture future = this.channel.write(response2);

			future.addListener(ChannelFutureListener.CLOSE);

		} finally {
			if (douyuContext != null)
				removeDouyuContext(douyuContext);
		}

	}

	/**
	 * Determine if we must drop the connection because of the HTTP status code.
	 * Use the same list of codes as Apache/httpd.
	 */
	protected boolean statusDropsConnection(int status) {
		return status == 400 /* SC_BAD_REQUEST */|| //
				status == 408 /* SC_REQUEST_TIMEOUT */|| //
				status == 411 /* SC_LENGTH_REQUIRED */|| //
				status == 413 /* SC_REQUEST_ENTITY_TOO_LARGE */|| //
				status == 414 /* SC_REQUEST_URI_TOO_LONG */|| //
				status == 500 /* SC_INTERNAL_SERVER_ERROR */|| //
				status == 503 /* SC_SERVICE_UNAVAILABLE */|| //
				status == 501 /* SC_NOT_IMPLEMENTED */;//
	}
}
