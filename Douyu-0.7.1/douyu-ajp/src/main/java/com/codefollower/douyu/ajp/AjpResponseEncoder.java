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

import java.util.Map;


import com.codefollower.douyu.http.HttpChunk;
import com.codefollower.douyu.http.HttpHeaders;
import com.codefollower.douyu.http.HttpResponse;
import com.codefollower.douyu.http.util.HttpMessages;
import com.codefollower.douyu.netty.buffer.ChannelBuffer;
import com.codefollower.douyu.netty.buffer.ChannelBuffers;
import com.codefollower.douyu.netty.channel.ChannelDownstreamHandler;
import com.codefollower.douyu.netty.channel.ChannelEvent;
import com.codefollower.douyu.netty.channel.ChannelHandlerContext;
import com.codefollower.douyu.netty.channel.Channels;
import com.codefollower.douyu.netty.channel.MessageEvent;

public class AjpResponseEncoder implements ChannelDownstreamHandler {
	/**
	 * If true, custom HTTP status messages will be used in headers.
	 */
	public static final boolean USE_CUSTOM_STATUS_MSG_IN_HEADER = Boolean.valueOf(
			System.getProperty("com.codefollower.douyu.ajp.USE_CUSTOM_STATUS_MSG_IN_HEADER", "false")).booleanValue();

	private AjpMessage responsMessage = null;
	private int packetSize = 8192;

	public AjpResponseEncoder() {
		responsMessage = new AjpMessage(packetSize);
	}

	@Override
	public void handleDownstream(ChannelHandlerContext ctx, ChannelEvent evt) throws Exception {
		if (!(evt instanceof MessageEvent)) {
			ctx.sendDownstream(evt);
			return;
		}

		MessageEvent e = (MessageEvent) evt;
		Object msg = e.getMessage();

		if (msg instanceof HttpResponse) {
			HttpResponse response = (HttpResponse) msg;
			writeHeaders(ctx, e, response);
			writeContent(ctx, e, response.getContent(), true);
		} else if (msg instanceof HttpChunk) {
			HttpChunk chunk = (HttpChunk) msg;
			writeContent(ctx, e, chunk.getContent(), chunk.isLast());
		} else {
			ctx.sendDownstream(evt);
		}
	}

	private void writeHeaders(ChannelHandlerContext ctx, MessageEvent e, HttpResponse response) {
		responsMessage.reset();
		responsMessage.appendByte(Constants.JK_AJP13_SEND_HEADERS);

		// HTTP header contents
		responsMessage.appendInt(response.getStatus().getCode());
		String message = null;
		if (USE_CUSTOM_STATUS_MSG_IN_HEADER && HttpMessages.isSafeInHttpHeader(response.getStatus().getReasonPhrase())) {
			message = response.getStatus().getReasonPhrase();
		}
		if (message == null) {
			message = HttpMessages.getMessage(response.getStatus().getCode());
		}
		if (message == null) {
			// mod_jk + httpd 2.x fails with a null status message - bug 45026
			message = Integer.toString(response.getStatus().getCode());
		}
		responsMessage.appendString(message);
		
		// Special headers
		if(!response.isChunked() && !response.containsHeader(HttpHeaders.Names.CONTENT_LENGTH)) {
			response.addHeader(HttpHeaders.Names.CONTENT_LENGTH, response.getContent().readableBytes());
		}

		// Other headers
		int numHeaders = response.getHeaders().size();
		responsMessage.appendInt(numHeaders);

		for (Map.Entry<String, String> entry : response.getHeaders()) {
			String hN = entry.getKey();
			int hC = Constants.getResponseAjpIndex(hN.toString());
			if (hC > 0) {
				responsMessage.appendInt(hC);
			} else {
				responsMessage.appendString(hN);
			}
			responsMessage.appendString(entry.getValue());
		}
		responsMessage.end();
		write(ctx, e, ChannelBuffers.wrappedBuffer(responsMessage.getBuffer(), 0, responsMessage.getLen()));
	}

	private void writeContent(ChannelHandlerContext ctx, MessageEvent e, ChannelBuffer content, boolean endMessage) {
		if (content != null) {
			int len = content.readableBytes();
			// 4 - hardcoded, byte[] marshaling overhead
			// Adjust allowed size if packetSize != default (Constants.MAX_PACKET_SIZE)
			int chunkSize = Constants.MAX_SEND_SIZE + packetSize - Constants.MAX_PACKET_SIZE;
			int off = 0;
			while (len > 0) {
				int thisTime = len;
				if (thisTime > chunkSize) {
					thisTime = chunkSize;
				}
				len -= thisTime;
				responsMessage.reset();
				responsMessage.appendByte(Constants.JK_AJP13_SEND_BODY_CHUNK);
				byte[] bytes = new byte[thisTime];
				content.getBytes(content.readerIndex(), bytes);
				responsMessage.appendBytes(bytes, 0, thisTime);
				responsMessage.end();
				write(ctx, e, ChannelBuffers.wrappedBuffer(responsMessage.getBuffer(), 0, responsMessage.getLen()));

				off += thisTime;
			}

			if (endMessage)
				write(ctx, e, ChannelBuffers.wrappedBuffer(AjpRequestDecoder.endMessageArray, 0,
						AjpRequestDecoder.endMessageArray.length));
		}
	}

	private void write(ChannelHandlerContext ctx, MessageEvent e, Object msg) {
		Channels.write(ctx, e.getFuture(), msg, e.getRemoteAddress());
	}
}
