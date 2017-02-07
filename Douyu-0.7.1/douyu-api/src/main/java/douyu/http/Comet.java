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
package douyu.http;

import java.io.IOException;

import douyu.mvc.Context;

public interface Comet {
	void onConnect(Context context);

	//void onRead(Context context, String data);
	
	void onMessage(String data);

	//void onMessage(Context context, byte[] data);

	void onError();

	void onDisconnect();
	
	
	public interface Outbound {
		void send(String data) throws IOException;

		void send(byte[] data) throws IOException;

		void send(byte[] data, int offset, int length) throws IOException;

		void close();

		boolean isOpen();
	}
}
