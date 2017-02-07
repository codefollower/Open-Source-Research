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
package douyu.mvc;

import douyu.http.Comet;
import douyu.http.HttpRequest;
import douyu.http.HttpResponse;
import douyu.http.WebSocket;

/**
 * 
 * 执行控制器的Action时，会为这个控制器生成一个ControllerManager，
 * 这个ControllerManager用来管理一次请求过程中用到的相关上下文信息。
 * 
 * 此类不是线程安全的，存活期与HttpRequest相同。
 * 
 * @author ZHH
 * @since 0.6.1
 * 
 */
public interface ControllerManager {

	public HttpRequest getHttpRequest();

	public HttpResponse getHttpResponse();

	public String getControllerClassName();

	public String getActionName();

	public String getApplicationBase();

	public void executeAction(String actionName) throws ControllerException;

	public void setWebSocket(WebSocket ws);

	public WebSocket getWebSocket();
	
	public void setComet(Comet comet);

	public Comet getComet();
}