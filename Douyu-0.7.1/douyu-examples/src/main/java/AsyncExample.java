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
import java.util.Date;

import douyu.mvc.Async;
import douyu.mvc.Context;
import douyu.mvc.Controller;

@Controller
public class AsyncExample {
	@Async
	public void asyncAction(Context c, PrintWriter out) {
		out.println("before invokeLongtimeService..." + Thread.currentThread());
		invokeLongtimeService(c, out);
		out.println("after invokeLongtimeService...");
	}

	private void invokeLongtimeService(Context c, PrintWriter out) {
		out.println("invokeLongtimeService...");
		out.println("at " + new Date());

		int seconds = 2;
		out.println("sleep " + seconds + " seconds...");
		try {
			Thread.sleep(seconds * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		out.println("at " + new Date());
	}

	@Async
	public void asyncAction2(PrintWriter out) {
		out.println("invoke asyncAction2..." + Thread.currentThread());
	}

	public void action3(PrintWriter out) {
		out.println("invoke action3..." + Thread.currentThread());
	}
}
