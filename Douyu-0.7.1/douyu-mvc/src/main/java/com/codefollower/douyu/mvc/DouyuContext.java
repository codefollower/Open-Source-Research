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
package com.codefollower.douyu.mvc;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import douyu.http.Comet;
import douyu.http.WebSocket;
import douyu.http.WebSocket.Outbound;

import douyu.mvc.Context;
import douyu.mvc.ControllerException;
import douyu.mvc.ViewException;
import douyu.mvc.ViewManager;
import douyu.mvc.ViewManagerProvider;


import com.codefollower.douyu.core.Config;
import com.codefollower.douyu.http.Attribute;
import com.codefollower.douyu.http.DouyuHttpRequest;
import com.codefollower.douyu.http.DouyuHttpResponse;
import com.codefollower.douyu.http.DouyuUploadedFile;
import com.codefollower.douyu.http.FileUpload;
import com.codefollower.douyu.http.HttpPostRequestDecoder;
import com.codefollower.douyu.http.InterfaceHttpData;
import com.codefollower.douyu.http.InterfaceHttpData.HttpDataType;

/**
 * 
 * 执行控制器的Action时，会为这个控制器生成一个Context，这个Context用来管理一次请求过程中用到的相关上下文信息。
 * 
 * 此类不是线程安全的，存活期与HttpRequest相同。
 * 
 * @author ZHH
 * 
 */
public abstract class DouyuContext implements Context, Runnable {
	private Config config;
	private String controllerClassName;

	// 编译器在编译带有@Controller的类时，会为它自动生成一个DouyuContext的子类，
	// 这3个字段会在自动生成的子类中直接访问，所以用protected.
	// 如果修改了这3个字段的名称要记得修改com.sun.tools.javac.processing.ControllerProcessor类
	protected DouyuHttpRequest request;
	protected DouyuHttpResponse response;
	protected String actionName;

	private Outbound outbound;

	public void setOutbound(Outbound outbound) {
		this.outbound = outbound;
	}

	private Map<String, Object> viewArgs = new HashMap<String, Object>();

	public void init(Config config, String controllerClassName, DouyuHttpRequest request, DouyuHttpResponse response) {
		this.config = config;
		this.controllerClassName = controllerClassName;
		this.request = request;
		this.response = response;
	}

	public void free() {
		//TODO
		if (viewArgs == null)
			return;
		config = null;
		controllerClassName = null;
		request = null;
		response = null;
		actionName = null;
		viewArgs.clear();
		viewArgs = null;
	}

	protected void checkHttpMethods(String... methods) {
		String method = request.getHttpMethod().name();
		for (String m : methods) {
			if (m.equals(method)) {
				return;
			}
		}

		throw new ControllerException("501 Not Implemented method: " + method);
	}

	// ========================================================
	// 实现douyu.mvc.ControllerManager
	// ========================================================

	@Override
	public DouyuHttpRequest getHttpRequest() {
		return request;
	}

	@Override
	public DouyuHttpResponse getHttpResponse() {
		return response;
	}

	@Override
	public String getControllerClassName() {
		return controllerClassName;
	}

	@Override
	public String getActionName() {
		return actionName;
	}

	@Override
	public String getApplicationBase() {
		if (config.srcDir == null)
			return config.classesDir;

		if (config.classesDir == null)
			return config.srcDir;

		if (config.isDevMode)
			return config.srcDir;
		else
			return config.classesDir;
	}

	@Override
	public void executeAction(String actionName) throws ControllerException {
		this.actionName = actionName;
		try {
			executeAction();
		} catch (Exception e) {
			throw new ControllerException("failed to execute action: " + actionName, e);
		}
	}

	private WebSocket ws;

	@Override
	public void setWebSocket(WebSocket ws) {
		this.ws = ws;
		if (ws != null && outbound != null)
			ws.onConnect(outbound);
	}

	@Override
	public WebSocket getWebSocket() {
		return ws;
	}

	//private HttpPostRequestDecoder httpPostRequestDecoder = null;

	public void setHttpPostRequestDecoder(HttpPostRequestDecoder httpPostRequestDecoder) throws Exception {
		//this.httpPostRequestDecoder = httpPostRequestDecoder;

		for (InterfaceHttpData data : httpPostRequestDecoder.getBodyHttpDatas()) {
			writeHttpData(data);
		}
	}

	private void writeHttpData(InterfaceHttpData data) throws Exception {
		if (data.getHttpDataType() == HttpDataType.Attribute) {
			Attribute attribute = (Attribute) data;
			request.addParameter(attribute.getName(), attribute.getValue());
		} else if (data.getHttpDataType() == HttpDataType.FileUpload) {
			FileUpload fileUpload = (FileUpload) data;
			if (fileUpload.isCompleted()) {
				request.addUploadedFile(fileUpload.getName(), new DouyuUploadedFile(fileUpload));
			}
		}
	}

	//自动生成的DouyuContext子类会覆盖此方法
	protected void executeAction() throws Exception {
	}

	//自动生成的DouyuContext子类如果有异步Action会覆盖此方法
	public boolean isAsyncAction(String actionName) {
		return false;
	}

	// ========================================================
	// 实现java.lang.Runnable
	// ========================================================

	@Override
	public void run() {
		try {
			executeAction();
			asyncCallback.writeResponse(this);
		} catch (Exception e) {
			asyncCallback.writeResponse(this, new ControllerException("failed to execute action: " + actionName, e));
		}
	}

	private static class ExecutorHolder {
		// 惰性初始化，因为异步Action并不常用
		private static Executor executor = Executors.newCachedThreadPool();
	}

	private static Executor getExecutor() {
		return ExecutorHolder.executor;
	}

	public void executeAsyncAction(String actionName) throws ControllerException {
		this.actionName = actionName;
		getExecutor().execute(this);
	}

	private AsyncCallback asyncCallback;

	public void setAsyncCallback(AsyncCallback asyncCallback) {
		this.asyncCallback = asyncCallback;
	}

	public static interface AsyncCallback {
		void writeResponse(DouyuContext douyuContext);

		void writeResponse(DouyuContext douyuContext, Exception e);
	}

	// ========================================================
	// 实现douyu.mvc.ViewManager
	// ========================================================

	@Override
	public void put(String key, Object value) {
		viewArgs.put(key, value);
	}

	@Override
	public void out() {
		ViewManagerProvider def = config.getDefaultViewManagerProvider();
		if (def != null) {
			try {
				outView(def, null);
				return;
			} catch (Exception e) {
			}
		}
		for (ViewManagerProvider vmp : config.getViewManagerProviders()) {
			if (vmp != def) {
				try {
					outView(vmp, null);
					return;
				} catch (Exception e) {
				}
			}
		}

		throw new ViewException("No ViewManager for 'ViewManager.out()', controller='" + controllerClassName + "', attion='"
				+ actionName + "'.");
	}

	private void outView(ViewManagerProvider vmp, String viewFileName) {
		ViewManager vm = vmp.getViewManager(this);
		if (vm == null)
			throw new ViewException("No ViewManager for view file: " + viewFileName);

		for (Map.Entry<String, Object> e : viewArgs.entrySet()) {
			vm.put(e.getKey(), e.getValue());
		}

		if (viewFileName == null)
			vm.out();
		else
			vm.out(viewFileName);
	}

	@Override
	public void out(String viewFileName) {
		String extension = null;
		int dotPos = viewFileName.lastIndexOf('.');
		if (dotPos >= 0) {
			extension = viewFileName.substring(dotPos + 1).trim();
		}

		ViewManagerProvider vmp;
		if (extension == null) {
			vmp = config.getDefaultViewManagerProvider();
		} else {
			vmp = config.getViewManagerProvider(extension);
		}

		if (vmp == null) {
			throw new ViewException("No ViewManagerProvider for view file: " + viewFileName);
		}

		outView(vmp, viewFileName);
	}

	public void setComet(Comet comet) {
	}

	public Comet getComet() {
		return null;
	}
}