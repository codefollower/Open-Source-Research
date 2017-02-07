/*
 * Copyright 2011 The Apache Software Foundation
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.codefollower.douyu.ajp;

import java.nio.charset.Charset;


import com.codefollower.douyu.http.DouyuHttpRequest;
import com.codefollower.douyu.http.HttpHeaders;
import com.codefollower.douyu.http.HttpMethod;
import com.codefollower.douyu.netty.buffer.ChannelBuffer;
import com.codefollower.douyu.netty.buffer.ChannelBuffers;
import com.codefollower.douyu.netty.channel.Channel;
import com.codefollower.douyu.netty.channel.ChannelHandlerContext;
import com.codefollower.douyu.netty.handler.codec.frame.FrameDecoder;

public class AjpRequestDecoder extends FrameDecoder {

	/**
	 * The Request attribute key for the cipher suite.
	 */
	public static final String CIPHER_SUITE_KEY = "javax.servlet.request.cipher_suite";

	/**
	 * The Request attribute key for the key size.
	 */
	public static final String KEY_SIZE_KEY = "javax.servlet.request.key_size";

	/**
	 * The Request attribute key for the client certificate chain.
	 */
	public static final String CERTIFICATE_KEY = "javax.servlet.request.X509Certificate";

	/**
	 * The Request attribute key for the session id. This one is a Tomcat
	 * extension to the Servlet spec.
	 */
	public static final String SESSION_ID_KEY = "javax.servlet.request.ssl_session";

	/**
	 * The request attribute key for the session manager. This one is a Tomcat
	 * extension to the Servlet spec.
	 */
	public static final String SESSION_MGR = "javax.servlet.request.ssl_session_mgr";

	/**
	 * End message array.
	 */
	protected static final byte[] endMessageArray;
	protected static final byte[] endAndCloseMessageArray;

	/**
	 * Flush message array.
	 */
	protected static final byte[] flushMessageArray;

	/**
	 * Pong message array.
	 */
	protected static final byte[] pongMessageArray;

	/**
	 * GetBody message array. Not static like the other message arrays since the
	 * message varies with packetSize and that can vary per connector.
	 */
	protected static final byte[] getBodyMessageArray;
	/**
	 * AJP packet size.
	 */
	public static int packetSize = 8192; // 8k
	public static Charset UTF8 = Charset.forName("UTF-8");
	static {
		// Allocate the end message array
		AjpMessage endMessage = new AjpMessage(16);
		endMessage.reset();
		endMessage.appendByte(Constants.JK_AJP13_END_RESPONSE);
		endMessage.appendByte(1);
		endMessage.end();
		endMessageArray = new byte[endMessage.getLen()];
		System.arraycopy(endMessage.getBuffer(), 0, endMessageArray, 0, endMessage.getLen());

		// Allocate the end and close message array
		AjpMessage endAndCloseMessage = new AjpMessage(16);
		endAndCloseMessage.reset();
		endAndCloseMessage.appendByte(Constants.JK_AJP13_END_RESPONSE);
		endAndCloseMessage.appendByte(0);
		endAndCloseMessage.end();
		endAndCloseMessageArray = new byte[endAndCloseMessage.getLen()];
		System.arraycopy(endAndCloseMessage.getBuffer(), 0, endAndCloseMessageArray, 0, endAndCloseMessage.getLen());

		// Allocate the flush message array
		AjpMessage flushMessage = new AjpMessage(16);
		flushMessage.reset();
		flushMessage.appendByte(Constants.JK_AJP13_SEND_BODY_CHUNK);
		flushMessage.appendInt(0);
		flushMessage.appendByte(0);
		flushMessage.end();
		flushMessageArray = new byte[flushMessage.getLen()];
		System.arraycopy(flushMessage.getBuffer(), 0, flushMessageArray, 0, flushMessage.getLen());

		// Allocate the pong message array
		AjpMessage pongMessage = new AjpMessage(16);
		pongMessage.reset();
		pongMessage.appendByte(Constants.JK_AJP13_CPONG_REPLY);
		pongMessage.end();
		pongMessageArray = new byte[pongMessage.getLen()];
		System.arraycopy(pongMessage.getBuffer(), 0, pongMessageArray, 0, pongMessage.getLen());

		// Set the getBody message buffer
		AjpMessage getBodyMessage = new AjpMessage(16);
		getBodyMessage.reset();
		getBodyMessage.appendByte(Constants.JK_AJP13_GET_BODY_CHUNK);
		// Adjust read size if packetSize != default (Constants.MAX_PACKET_SIZE)
		getBodyMessage.appendInt(Constants.MAX_READ_SIZE + packetSize - Constants.MAX_PACKET_SIZE);
		getBodyMessage.end();
		getBodyMessageArray = new byte[getBodyMessage.getLen()];
		System.arraycopy(getBodyMessage.getBuffer(), 0, getBodyMessageArray, 0, getBodyMessage.getLen());
	}

	protected static enum State {
		READ_REQUEST_HEADER_MESSAGE, //
		READ_FIRST_BODY_MESSAGE, //
		READ_MORE_BODY_MESSAGE, //
		READ_END, //
		READ_END_AND_CLOSE,
	}

	private boolean useFrontEndServerAuthentication = false;
	private String requiredSecret;
	protected ChannelBuffer certificates;

	private int contentLength = -1;
	private DouyuHttpRequest request;
	private State state = State.READ_REQUEST_HEADER_MESSAGE;

	public AjpRequestDecoder() {
	}

	public String readCString(ChannelBuffer buffer) {
		int length = readShort(buffer);
		if ((length == 0xFFFF) || (length == -1)) {
			return "";
		}

		String str = buffer.readBytes(length).toString(UTF8);
		buffer.readByte(); // Skip the terminating \0
		return str;
	}

	public int readShort(ChannelBuffer buffer) {
		int b1 = buffer.readByte() & 0xFF;
		int b2 = buffer.readByte() & 0xFF;
		return (b1 << 8) + b2;

		// return buffer.readShort();
	}

	private Object reset() {
		DouyuHttpRequest request = this.request;
		this.request = null;
		this.state = State.READ_REQUEST_HEADER_MESSAGE;
		certificates = null;
		contentLength = -1;
		return request;
	}

	protected void prepareRequest(ChannelBuffer buffer) {
		request = new DouyuHttpRequest();
		// Translate the HTTP method code to a String.
		byte methodCode = buffer.readByte();
		if (methodCode != Constants.SC_M_JK_STORED) {
			String methodName = Constants.getMethodForCode(methodCode - 1);

			request.setMethod(HttpMethod.valueOf(methodName));
		}

		request.setProtocol(readCString(buffer));
		request.setRequestURI(readCString(buffer));
		request.setRemoteAddr(readCString(buffer));
		request.setRemoteHost(readCString(buffer));
		request.setLocalName(readCString(buffer));
		request.setLocalPort(readShort(buffer));
		request.setSSL(buffer.readByte() != 0);

		int hCount = readShort(buffer);
		for (int i = 0; i < hCount; i++) {
			String hName = null;

			// Header names are encoded as either an integer code starting
			// with 0xA0, or as a normal string (in which case the first
			// two bytes are the length).
			int isc = buffer.getShort(buffer.readerIndex());
			int hId = isc & 0xFF;

			String hValue = null;
			isc &= 0xFF00;
			if (0xA000 == isc) {
				readShort(buffer); // To advance the read position
				hName = Constants.getHeaderForCode(hId - 1);
			} else {
				// reset hId -- if the header currently being read
				// happens to be 7 or 8 bytes long, the code below
				// will think it's the content-type header or the
				// content-length header - SC_REQ_CONTENT_TYPE=7,
				// SC_REQ_CONTENT_LENGTH=8 - leading to unexpected
				// behaviour. see bug 5861 for more information.
				hId = -1;
				hName = readCString(buffer);
			}

			hValue = readCString(buffer);

			if (hId == Constants.SC_REQ_CONTENT_LENGTH || (hId == -1 && hName.equalsIgnoreCase("Content-Length"))) {
				// just read the content-length header, so set it
				try {
					long cl = Long.parseLong(hValue);
					if (cl < Integer.MAX_VALUE) {
						contentLength = (int) cl;
						request.setContentLength(contentLength);
					}
				} catch (NumberFormatException e) {
					// TODO
				}
			} else if (hId == Constants.SC_REQ_CONTENT_TYPE || (hId == -1 && hName.equalsIgnoreCase("Content-Type"))) {
				// just read the content-type header, so set it
				request.setContentType(hValue);
			}
			request.addHeader(hName, hValue);
			System.out.println(hName + " : " + hValue);
		}

		parseAttributes(buffer);

	}

	protected void parseCertificates(ChannelBuffer buffer) {
		int length = readShort(buffer);
		if ((length == 0xFFFF) || (length == -1)) {
			return;
		}

		certificates = buffer.copy(buffer.readerIndex(), length);
		buffer.readByte(); // Skip the terminating \0
	}

	protected void parseAttributes(ChannelBuffer buffer) {
		// Decode extra attributes
		boolean secret = false;
		byte attributeCode;
		while ((attributeCode = buffer.readByte()) != Constants.SC_A_ARE_DONE) {

			switch (attributeCode) {

			case Constants.SC_A_REQ_ATTRIBUTE:
				String n = readCString(buffer);
				String v = readCString(buffer);
				/*
				 * AJP13 misses to forward the remotePort. Allow the AJP
				 * connector to add this info via a private request attribute.
				 * We will accept the forwarded data as the remote port, and
				 * remove it from the public list of request attributes.
				 */
				if (n.equals(Constants.SC_A_REQ_REMOTE_PORT)) {
					try {
						request.setRemotePort(Integer.parseInt(v));
					} catch (NumberFormatException nfe) {
						// Ignore invalid value
					}
				} else {
					request.setAttribute(n, v);
				}
				break;

			case Constants.SC_A_CONTEXT:
				readCString(buffer);
				// nothing
				break;

			case Constants.SC_A_SERVLET_PATH:
				readCString(buffer);
				// nothing
				break;

			case Constants.SC_A_REMOTE_USER:
				if (useFrontEndServerAuthentication) {
					request.setRemoteUser(readCString(buffer));
				} else {
					// ignore server
					readCString(buffer);
				}
				break;

			case Constants.SC_A_AUTH_TYPE:
				if (useFrontEndServerAuthentication) {
					request.setAuthType(readCString(buffer));
				} else {
					// ignore server
					readCString(buffer);
				}
				break;

			case Constants.SC_A_QUERY_STRING:
				request.setQueryString(readCString(buffer));
				break;

			case Constants.SC_A_JVM_ROUTE:
				request.setInstanceId(readCString(buffer));
				break;

			case Constants.SC_A_SSL_CERT:
				request.setScheme("https");
				// SSL certificate extraction is lazy
				parseCertificates(buffer);
				break;

			case Constants.SC_A_SSL_CIPHER:
				request.setScheme("https");
				request.setAttribute(CIPHER_SUITE_KEY, readCString(buffer));
				break;

			case Constants.SC_A_SSL_SESSION:
				request.setScheme("https");
				request.setAttribute(SESSION_ID_KEY, readCString(buffer));
				break;

			case Constants.SC_A_SSL_KEY_SIZE:
				request.setAttribute(KEY_SIZE_KEY, readShort(buffer));
				break;

			case Constants.SC_A_SECRET:
				String secretStr = readCString(buffer);
				if (requiredSecret != null) {
					secret = true;
					if (!secretStr.equals(requiredSecret)) {
						//TODO 响应403并记日志
					}
				}
				break;

			case Constants.SC_A_STORED_METHOD:
				request.setMethod(HttpMethod.valueOf(readCString(buffer)));
				break;

			default:
				// Ignore unknown attribute for backward compatibility
				break;

			}
		}

		// Check if secret was submitted if required
		if ((requiredSecret != null) && !secret) {
			//TODO 响应403并记日
		}
	}

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws Exception {
		if (buffer.readableBytes() < 4) {
			return null;
		}

		buffer.markReaderIndex();
		int mark = readShort(buffer);
		//if ((mark != 0x1234) && (mark != 0x4142)) {
		if (mark != 0x1234) {
			throw new IllegalStateException("Unexpected mark = " + mark);
		}

		int length = readShort(buffer);
		if (buffer.readableBytes() < length) {
			buffer.resetReaderIndex();
			return null;
		}

		switch (state) {
		case READ_REQUEST_HEADER_MESSAGE: {
			int type = buffer.readByte();
			if (type == Constants.JK_AJP13_CPING_REQUEST) {
				channel.write(pongMessageArray);
				return null;
			} else if (type != Constants.JK_AJP13_FORWARD_REQUEST) {
				return null;
			}

			prepareRequest(buffer);

			if (contentLength == 0) {
				return reset();
			} else if (contentLength > 0) {
				state = State.READ_FIRST_BODY_MESSAGE;
			} else {
				channel.write(ChannelBuffers.wrappedBuffer(getBodyMessageArray));
				state = State.READ_MORE_BODY_MESSAGE;
			}

			request.removeHeader(HttpHeaders.Names.TRANSFER_ENCODING);

			return null;
		}

		case READ_FIRST_BODY_MESSAGE:
			length = readShort(buffer);
			request.setContent(buffer.readBytes(length));
			contentLength -= length;

			//buffer.readByte(); //请求体不会在末尾加\0

			if (contentLength == 0) {
				return reset();
			} else if (contentLength > 0) {
				channel.write(ChannelBuffers.wrappedBuffer(getBodyMessageArray));
				state = State.READ_MORE_BODY_MESSAGE;
				return null;
			} else {
				throw new IllegalArgumentException("body message length greater than max context length" + "(" + (-contentLength)
						+ ")");
			}
		case READ_MORE_BODY_MESSAGE:
			if (length == 0) {
				return reset();
			}

			length = readShort(buffer);

			if (request.getContent() == null)
				request.setContent(buffer.readBytes(length));
			else
				request.setContent(ChannelBuffers.wrappedBuffer(request.getContent(), buffer.readBytes(length)));

			//buffer.readByte(); //请求体不会在末尾加\0

			//chunked一般没有Content-Length请求头
			if (contentLength < 0) {
				channel.write(ChannelBuffers.wrappedBuffer(getBodyMessageArray));
				state = State.READ_MORE_BODY_MESSAGE;
				return null;
			}

			contentLength -= length;

			if (contentLength == 0) {
				return reset();
			} else if (contentLength > 0) {
				channel.write(ChannelBuffers.wrappedBuffer(getBodyMessageArray));
				state = State.READ_MORE_BODY_MESSAGE;
				return null;
			} else {
				throw new IllegalArgumentException("body message length greater than max context length" + "(" + (-contentLength)
						+ ")");
			}
		}

		return null;
	}
}
