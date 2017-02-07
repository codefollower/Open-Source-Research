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
import java.io.PrintWriter;
import java.io.Writer;

import douyu.examples.models.MyModel;
import douyu.http.HttpRequest;
import douyu.http.HttpResponse;
import douyu.mvc.Context;
import douyu.mvc.Controller;
import douyu.mvc.ControllerManager;
import douyu.mvc.ModelManager;
import douyu.mvc.ViewManager;

@Controller
public class DevTest {
	//static {}
	//{ }
	public DevTest() {
		//throw new Error();
	}

	//http://127.0.0.1:8080/douyu/DevTest
	public void index(PrintWriter out) {
		out.println("invoke defaultAction: 'index' at " + new java.util.Date());
	}

	//http://127.0.0.1:8080/douyu/DevTest.haha?name=haha&age=1000
	public void haha(Context c, String name, int age) {

		//c.out("/jsp/ViewTest.jsp");
		c.out(); //DevTest.haha.jsp

		c.out("/ViewTest.vm");
		c.out("/ViewTest.ftl");
	}

	//////////////////以下是所有可注入的方法参数类型/////////////////
	public void method0() {
	}

	public void method1(Context context, ModelManager m, ViewManager v, ControllerManager c) {
	}

	public void method2(HttpRequest p1, HttpResponse p2) {
	}

	public void method3(PrintWriter p1, Writer p2) {
	}

	public void method4(int i, long l, float f, double d, boolean bool, byte b, short s, char c) {

	}

	public void method5(Integer i, Long l, Float f, Double d, Boolean bool, Byte b, Short s, Character c) {

	}

	public void method6(String[] strs) {

	}

	//servlet3.0才支持Part
	//如果编译出错，注销掉这个方法
	/*
	public void method7(javax.servlet.http.Part part, javax.servlet.http.Part[] parts, PrintWriter out) {
		out.println("part=" + part);

		if (parts != null) {
			out.println("parts.length=" + parts.length);
		} else {
			out.println("parts=null");
		}
	}
	*/

	//http://127.0.0.1:8080/douyu/DevTest.method8?model.f1=1&model.f2=2&model.subModel.f1=3&model.subModel.f2=4
	public void method8(Object obj, MyModel model, PrintWriter out) {
		out.println("invoke 'method8' at " + new java.util.Date());

		out.println();
		out.println("obj=" + obj); //总是为null，java.lang.Object不是可注入的类型

		out.println();
		out.println("model=" + model);
	}

	//////////////////package、private、protected以及所有static方法都是不能通过URI直接访问的/////////////////
	void package_method() {
	}

	@SuppressWarnings("unused")
	private void private_method() {
	}

	protected void protected_method() {
	}

	public static void static_method() {
	}
}
