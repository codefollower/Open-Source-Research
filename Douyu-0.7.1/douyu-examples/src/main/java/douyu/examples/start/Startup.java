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
package douyu.examples.start;

import java.io.File;

//import com.codefollower.douyu.startup.AjpConnector;
import com.codefollower.douyu.startup.Connector;
import com.codefollower.douyu.startup.HttpConnector;
import com.codefollower.douyu.startup.Server;

public class Startup {

	public static void main(String[] args) throws Exception {
		Server server = new Server();
		// Connector ajp = new AjpConnector();
		Connector http = new HttpConnector();
		// server.addConnectors(ajp, http);

		// server.addConnector(ajp);
		server.addConnector(http);

		String baseDir = new File(".").getCanonicalPath();
		String srcDir = new File(baseDir, "src/main/java").getCanonicalPath();
		System.out.println("src dir: " + srcDir);
		String resourcesDir = new File(baseDir, "src/main/resources").getCanonicalPath();
		server.getConfig().addClassPath(resourcesDir);
		System.out.println("resources dir: " + resourcesDir);
		String classesDir = new File(baseDir, "douyu-examples-classes").getCanonicalPath();
		System.out.println("classes dir: " + classesDir);

		System.out.println();

		server.init("douyu-examples", "UTF-8", srcDir, classesDir, true, null);
		server.start();

		System.out.println("Started douyu http server at port " + http.getPort());
	}
}
