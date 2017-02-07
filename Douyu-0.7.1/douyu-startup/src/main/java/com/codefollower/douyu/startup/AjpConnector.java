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


import com.codefollower.douyu.ajp.AjpRequestDecoder;
import com.codefollower.douyu.ajp.AjpResponseEncoder;
import com.codefollower.douyu.logging.InternalLogLevel;
import com.codefollower.douyu.netty.channel.ChannelPipeline;
import com.codefollower.douyu.netty.channel.ChannelPipelineFactory;
import com.codefollower.douyu.netty.channel.Channels;
import com.codefollower.douyu.netty.handler.logging.LoggingHandler;

public class AjpConnector extends Connector {
	public AjpConnector() {
		this(8009);
	}

	public AjpConnector(String host, int port) {
		super(host, port);
	}

	public AjpConnector(int port) {
		super(port);
	}

	public ChannelPipelineFactory getChannelPipelineFactory() {
		return new AjpServerPipelineFactory();
	}

	private class AjpServerPipelineFactory implements ChannelPipelineFactory {
		@Override
		public ChannelPipeline getPipeline() throws Exception {
			ChannelPipeline pipeline = Channels.pipeline();
			pipeline.addLast("log", new LoggingHandler(InternalLogLevel.INFO));
			pipeline.addLast("decoder", new AjpRequestDecoder());
			pipeline.addLast("encoder", new AjpResponseEncoder());
			pipeline.addLast("handler", new HttpRequestHandler(config));
			return pipeline;
		}

	}
}
