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

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;


import com.codefollower.douyu.core.Config;
import com.codefollower.douyu.netty.bootstrap.ServerBootstrap;
import com.codefollower.douyu.netty.channel.ChannelPipelineFactory;
import com.codefollower.douyu.netty.channel.nio.NioServerSocketChannelFactory;

public abstract class Connector {
	protected String host = "localhost";
	protected int port = 8080;

	protected Connector() {
	}

	protected Connector(String host, int port) {
		this.host = host;
		this.port = port;
	}

	protected Connector(int port) {
		this.port = port;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	Config config;

	public void start(Config config) {
		this.config = config;
		// Configure the server.
		ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors
				.newCachedThreadPool(), Executors.newCachedThreadPool()));

		// Set up the event pipeline factory.
		bootstrap.setPipelineFactory(getChannelPipelineFactory());
		// Bind and start to accept incoming connections.
		bootstrap.bind(new InetSocketAddress(host, port));
	}

	protected abstract ChannelPipelineFactory getChannelPipelineFactory();
}
