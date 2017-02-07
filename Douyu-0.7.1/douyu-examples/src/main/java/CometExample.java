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
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import douyu.http.Comet;
import douyu.mvc.Context;
import douyu.mvc.Controller;

@Controller
public class CometExample {
	public void index(Context c) {
		c.out("CometExample.html");
	}

	public void join(Context c) {
		c.setComet(new MyComet());
	}

	public static class MyComet implements Comet {
		private final static Set<MyComet> members = new CopyOnWriteArraySet<MyComet>();
		Context context;

		@Override
		public void onConnect(Context context) {
			members.add(this);
			this.context = context;
		}

		@Override
		public void onDisconnect() {
			members.remove(this);
		}

		@Override
		public void onError() {
		}

		@Override
		public void onMessage(String data) {
			if (data.indexOf("disconnect") >= 0)
				context.setComet(null);
			else {
				for (MyComet member : members) {
					try {
						member.context.getHttpResponse().getWriter().print(data);
					} catch (IOException e) {
						e.printStackTrace();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

}
