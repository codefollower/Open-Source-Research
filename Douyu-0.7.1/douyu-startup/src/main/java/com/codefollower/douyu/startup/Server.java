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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import com.codefollower.douyu.core.Config;
import com.codefollower.douyu.core.ResourceLoader;

public class Server {
	private ArrayList<Connector> connectors = new ArrayList<Connector>(1);

	public void addConnectors(Connector... connectors) {
		if (connectors == null)
			throw new NullPointerException("connectors");

		this.connectors.addAll(Arrays.asList(connectors));
	}

	public void addConnector(Connector connector) {
		if (connector == null)
			throw new NullPointerException("connector");

		connectors.add(connector);
	}

	public void start() {
		if (connectors.isEmpty())
			connectors.add(new HttpConnector());

		for (Connector c : connectors)
			c.start(config);
	}

	private Config config = new Config();

	public ResourceLoader getResourceLoader() {
		return config.getResourceLoader();
	}

	public Config getConfig() {
		return config;
	}

	private static final String viewManagerProviderConfig = "com.codefollower.douyu.plugins.velocity.VelocityViewManagerProvider=vm;"
			+ "com.codefollower.douyu.plugins.freemarker.FreeMarkerViewManagerProvider=ftl;";

	public void init(String appName, String javacEncoding, String srcDir, String classesDir, boolean isDevMode,
			String vmpConfig) throws Exception {
		config.appName = appName;
		config.javacEncoding = javacEncoding;
		config.srcDir = srcDir;
		config.classesDir = classesDir;

		if (config.srcDir == null)
			config.srcDir = "src";

		if (config.classesDir == null)
			config.classesDir = "classes";

		// if (!new File(config.srcDir).isAbsolute())
		// config.srcDir = "";

		File f = new File(config.srcDir);
		if (!f.exists())
			f.mkdirs();

		// if (!new File(config.classesDir).isAbsolute())
		// config.classesDir = "";

		f = new File(config.classesDir);
		if (!f.exists())
			f.mkdirs();

		config.isDevMode = isDevMode;
		if (vmpConfig == null)
			vmpConfig = viewManagerProviderConfig;
		config.setViewManagerProviderConfig(vmpConfig);

		config.addClassPath(config.srcDir);
		config.addClassPath(config.classesDir);

		config.resourceLoaderHolder = ResourceLoader.newHolder(config, getClass().getClassLoader());
	}

	public void destroy() {
		config.free();
		config = null;
	}
}
