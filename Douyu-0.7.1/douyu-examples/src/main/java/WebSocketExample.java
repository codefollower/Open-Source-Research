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

import douyu.http.WebSocket;
import douyu.mvc.Context;
import douyu.mvc.Controller;

@Controller
public class WebSocketExample {
	public void index(Context c) {
		c.out("WebSocketExample.html");
	}

	public void join(Context c) {
		c.setWebSocket(new MyWebSocket());
	}

	public static class MyWebSocket implements WebSocket {
		private final static Set<MyWebSocket> members = new CopyOnWriteArraySet<MyWebSocket>();

		private Outbound outbound;

		@Override
		public void onConnect(Outbound outbound) {
			this.outbound = outbound;
			members.add(this);
		}

		@Override
		public void onDisconnect() {
			members.remove(this);
		}

		@Override
		public void onMessage(int type, String data) {
			if (data.indexOf("disconnect") >= 0)
				outbound.close();
			else {
				for (MyWebSocket member : members) {
					try {
						member.outbound.send(type, data);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

		@Override
		public void onMessage(int type, byte[] data) {
		}
	}
}
