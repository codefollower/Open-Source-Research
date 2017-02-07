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
package com.codefollower.douyu.startup;

import static com.codefollower.douyu.netty.channel.Channels.pipeline;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.X509KeyManager;


import com.codefollower.douyu.http.DouyuHttpRequestDecoder;
import com.codefollower.douyu.http.HttpResponseEncoder;
import com.codefollower.douyu.http.ssl.SSLConfig;
import com.codefollower.douyu.http.ssl.SSLImplementation;
import com.codefollower.douyu.http.ssl.SSLUtil;
import com.codefollower.douyu.logging.InternalLogLevel;
import com.codefollower.douyu.netty.channel.ChannelHandlerContext;
import com.codefollower.douyu.netty.channel.ChannelPipeline;
import com.codefollower.douyu.netty.channel.ChannelPipelineFactory;
import com.codefollower.douyu.netty.handler.logging.LoggingHandler;
import com.codefollower.douyu.netty.handler.ssl.SslHandler;
import com.codefollower.douyu.netty.handler.timeout.IdleState;
import com.codefollower.douyu.netty.handler.timeout.IdleStateAwareChannelHandler;
import com.codefollower.douyu.netty.handler.timeout.IdleStateEvent;
import com.codefollower.douyu.netty.handler.timeout.IdleStateHandler;
import com.codefollower.douyu.netty.util.HashedWheelTimer;
import com.codefollower.douyu.netty.util.Timer;

public class HttpConnector extends Connector {
	public HttpConnector() {
		this(8080);
	}

	public HttpConnector(String host, int port) {
		super(host, port);
	}

	public HttpConnector(int port) {
		super(port);
	}

	protected SSLConfig sslConfig;

	protected int maxHttpInitialLineLength = 4096;
	protected int maxHttpHeaderSize = 8192;
	protected int maxHttpChunkSize = 8192;

	protected int maxKeepAliveRequests = -1; // TODO

	protected boolean compression = false;
	protected int compressionMinSize = 2048;

	protected int connectionTimeout = 20000; // 20毫秒

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public boolean isCompression() {
		return compression;
	}

	public void setCompression(boolean compression) {
		this.compression = compression;
	}

	public int getCompressionMinSize() {
		return compressionMinSize;
	}

	public void setCompressionMinSize(int compressionMinSize) {
		this.compressionMinSize = compressionMinSize;
	}

	public int getMaxHttpInitialLineLength() {
		return maxHttpInitialLineLength;
	}

	public void setMaxHttpInitialLineLength(int maxHttpInitialLineLength) {
		this.maxHttpInitialLineLength = maxHttpInitialLineLength;
	}

	public int getMaxHttpHeaderSize() {
		return maxHttpHeaderSize;
	}

	public void setMaxHttpHeaderSize(int maxHttpHeaderSize) {
		this.maxHttpHeaderSize = maxHttpHeaderSize;
	}

	public int getMaxHttpChunkSize() {
		return maxHttpChunkSize;
	}

	public void setMaxHttpChunkSize(int maxHttpChunkSize) {
		this.maxHttpChunkSize = maxHttpChunkSize;
	}

	public ChannelPipelineFactory getChannelPipelineFactory() {
		return new HttpServerPipelineFactory();
	}

	private class HttpServerPipelineFactory implements ChannelPipelineFactory {
		private final Timer timer = new HashedWheelTimer();

		@Override
		public ChannelPipeline getPipeline() throws Exception {
			// Create a default pipeline implementation.
			ChannelPipeline pipeline = pipeline();
			pipeline.addLast("log", new LoggingHandler(InternalLogLevel.INFO));

			// Uncomment the following line if you want HTTPS

			if (sslConfig != null) {
				SSLUtil sslUtil = SSLImplementation.getInstance().getSSLUtil(sslConfig);

				SSLContext sslContext = sslUtil.createSSLContext();
				sslContext.init(wrap(sslUtil.getKeyManagers()), sslUtil.getTrustManagers(), null);

				SSLSessionContext sessionContext = sslContext.getServerSessionContext();
				if (sessionContext != null) {
					sslUtil.configureSessionContext(sessionContext);
				}
				SSLEngine engine = createSSLEngine(sslContext);
				engine.setUseClientMode(false);
				pipeline.addLast("ssl", new SslHandler(engine));
			}

			// pipeline.addLast("idleStateHandler", new IdleStateHandler(timer,
			// 60, 30, 0));
			pipeline.addLast("idleStateHandler", new IdleStateHandler(timer, connectionTimeout, connectionTimeout, 0));
			pipeline.addLast("idleAwareHandler", new IdleAwareHandler());

			// pipeline.addLast("decoder", new HttpRequestDecoder());
			pipeline.addLast("decoder", new DouyuHttpRequestDecoder(maxHttpInitialLineLength, maxHttpHeaderSize,
					maxHttpChunkSize));

			// Uncomment the following line if you don't want to handle
			// HttpChunks.
			// pipeline.addLast("aggregator", new HttpChunkAggregator(1048576));
			pipeline.addLast("encoder", new HttpResponseEncoder());
			// Remove the following line if you don't want automatic content
			// compression.
			// pipeline.addLast("deflater", new HttpContentCompressor());
			pipeline.addLast("handler", new HttpRequestHandler(config));
			return pipeline;
		}

		public class IdleAwareHandler extends IdleStateAwareChannelHandler {

			@Override
			public void channelIdle(ChannelHandlerContext ctx, IdleStateEvent e) {
				if (e.getState() == IdleState.READER_IDLE) {
					e.getChannel().close();
				} else if (e.getState() == IdleState.WRITER_IDLE) {
					// TODO
					// e.getChannel().write(....);
				}
			}
		}

		protected SSLEngine createSSLEngine(SSLContext sslContext) {
			SSLEngine engine = sslContext.createSSLEngine();
			String ca = sslConfig.getClientAuth();
			if ("false".equals(ca)) {
				engine.setNeedClientAuth(false);
				engine.setWantClientAuth(false);
			} else if ("true".equals(ca) || "yes".equals(ca)) {
				engine.setNeedClientAuth(true);
			} else if ("want".equals(ca)) {
				engine.setWantClientAuth(true);
			}
			engine.setUseClientMode(false);
			if (sslConfig.getCiphersArray().length > 0)
				engine.setEnabledCipherSuites(sslConfig.getCiphersArray());
			if (sslConfig.getSslEnabledProtocolsArray().length > 0)
				engine.setEnabledProtocols(sslConfig.getSslEnabledProtocolsArray());

			return engine;
		}

		public KeyManager[] wrap(KeyManager[] managers) {
			if (managers == null)
				return null;
			KeyManager[] result = new KeyManager[managers.length];
			for (int i = 0; i < result.length; i++) {
				if (managers[i] instanceof X509KeyManager && sslConfig.getKeyAlias() != null) {
					result[i] = new com.codefollower.douyu.http.ssl.jsse.NioX509KeyManager((X509KeyManager) managers[i], sslConfig
							.getKeyAlias());
				} else {
					result[i] = managers[i];
				}
			}
			return result;
		}
	}
}
