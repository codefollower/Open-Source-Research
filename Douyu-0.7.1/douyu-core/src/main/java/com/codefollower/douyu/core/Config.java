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
package com.codefollower.douyu.core;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import douyu.mvc.ViewException;
import douyu.mvc.ViewManagerProvider;

/**
 * 
 * @author ZHH
 * 
 */
public class Config {
	/**
	 * 应用名
	 */
	public String appName;

	/**
	 * 对应javac的-encoding参数
	 */
	public String javacEncoding;

	// 警告: srcDir目录和classesDir目录不能都放class文件，如果是由两个编译器编译的，时间不同，会导致一直重复编译

	/**
	 * java源文件目录
	 */
	public String srcDir;

	/**
	 * 存放编译后的类文件或自动生成的java源文件
	 */
	public String classesDir;

	/**
	 * vm、ftl文件目录，如果没有设置，则默认是srcDir
	 */
	// public String viewsDir;
	/**
	 * 在开发模式下， java源文件修改时会重新编译并重新加载(通常是替换ClassLoader)
	 */
	public boolean isDevMode = true;

	// public ResourceLoader loader;

	public ResourceLoader.Holder resourceLoaderHolder;

	public ResourceLoader getResourceLoader() {
		return resourceLoaderHolder.get();
	}

	public void free() {
		resourceLoaderHolder.free();
		resourceLoaderHolder = null;
	}

	/**
	 * 应用类路径
	 */
	public URL[] appClassPath = new URL[0];

	public void addClassPath(String path) throws MalformedURLException {
		if (path != null) {
			URL url = new File(path).toURI().toURL();
			URL[] urls = new URL[appClassPath.length + 1];
			System.arraycopy(appClassPath, 0, urls, 0, appClassPath.length);
			urls[appClassPath.length] = url;
			appClassPath = urls;
		}
	}

	private ViewManagerProvider defaultViewManagerProvider;
	private String defaultViewManagerProviderClassName;
	// key: ViewManagerProvider的类名
	private Map<String, ViewManagerProvider> viewManagerProviders = new HashMap<String, ViewManagerProvider>();

	// key: 视图文件扩展名
	// value: ViewManagerProvider的类名
	private Map<String, String> extensionMap = new LinkedHashMap<String, String>();

	public void setDefaultViewManagerProvider(ViewManagerProvider defaultViewManagerProvider) {
		this.defaultViewManagerProvider = defaultViewManagerProvider;
	}

	public ViewManagerProvider getDefaultViewManagerProvider() {
		if (defaultViewManagerProvider == null && defaultViewManagerProviderClassName != null) {
			try {
				this.defaultViewManagerProvider = (ViewManagerProvider) getResourceLoader().loadClass(
						defaultViewManagerProviderClassName).newInstance();
			} catch (Exception e) {
				throw new ViewException("failed to getDefaultViewManagerProvider: " + e);
			}
		}
		return defaultViewManagerProvider;
	}

	public List<ViewManagerProvider> getViewManagerProviders() {
		List<ViewManagerProvider> list = new ArrayList<ViewManagerProvider>(extensionMap.size());
		for (String extension : extensionMap.keySet()) {
			list.add(getViewManagerProvider(extension));
		}
		return list;
	}

	/**
	 * 
	 * @param extension
	 * @return 
	 *         返回与extension相关的ViewManagerProvider，如果没有找到，返回默认的ViewManagerProvider
	 */
	public ViewManagerProvider getViewManagerProvider(String extension) {
		String className = extensionMap.get(extension);
		if (className == null)
			return getDefaultViewManagerProvider();

		ViewManagerProvider vmp = viewManagerProviders.get(className);
		if (vmp == null) {
			try {
				vmp = (ViewManagerProvider) getResourceLoader().loadClass(className).newInstance();
				viewManagerProviders.put(className, vmp);
			} catch (Exception e) {
				throw new ViewException("failed to getViewManagerProvider for extension: " + extension, e);
			}
		}
		return vmp;
	}

	// 如:"com.codefollower.douyu.plugins.velocity.VelocityViewManagerProvider=vm; com.codefollower.douyu.plugins.freemarker.FreeMarkerViewManagerProvider=ftl";
	public void setViewManagerProviderConfig(String viewManagerProviderConfig) {
		if (viewManagerProviderConfig != null) {
			for (String m : viewManagerProviderConfig.split(";")) {
				String[] n = m.trim().split("=");
				if (n.length != 2) {
					throw new IllegalArgumentException(viewManagerProviderConfig
							+ "(usage: classNameA=extensionA|extensionB;classNameB=extensionC|extensionD)");
				}
				String className = n[0].trim();

				// 默认是第一个
				if (defaultViewManagerProviderClassName == null)
					defaultViewManagerProviderClassName = className;

				for (String extension : n[1].split("\\|")) {
					extensionMap.put(extension.trim(), className);
				}
			}
		}
	}
}
