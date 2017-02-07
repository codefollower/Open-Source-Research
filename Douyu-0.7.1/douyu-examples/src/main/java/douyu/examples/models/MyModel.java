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
package douyu.examples.models;

import java.io.PrintWriter;

import douyu.mvc.Model;

@Model
public class MyModel {
	private int f1;
	private String f2;
	private MySubModel subModel;

	public void set(int f1, String f2, MySubModel subModel, PrintWriter out) {
		this.f1 = f1;
		this.f2 = f2;
		this.subModel = subModel;

		out.println("invoke MyModel.set(): subModel.getParentModel()==this : " + (subModel.getParentModel() == this));
		out.println();
	}

	public String toString() {
		return "MyModel[f1=" + f1 + ", f2=" + f2 + ", subModel=" + subModel + "]";
	}
}
