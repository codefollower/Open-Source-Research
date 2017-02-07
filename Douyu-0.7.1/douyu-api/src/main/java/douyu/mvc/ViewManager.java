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

/**
 * 
 * 输出视图模板文件，存放模板文件需要用到的参数。
 * 
 * @author ZHH
 * @since 0.6.1
 *
 */
public interface ViewManager {
	/**
	 * 输出默认视图，默认视图的位置取决于ViewManagerProvider，通常会根据控制器类名和Action名来决定
	 */
	public void out();

	public void out(String viewFileName);

	public void put(String key, Object value);
}