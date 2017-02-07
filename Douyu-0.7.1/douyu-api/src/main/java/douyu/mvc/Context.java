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
 * 执行控制器的Action时，会为这个控制器生成一个Context，这个Context用来管理一次请求过程中用到的相关上下文信息。
 * 
 * 此类不是线程安全的，存活期与HttpRequest相同。
 * 
 * @author ZHH
 * @since 0.6.1
 * 
 */
public interface Context extends ModelManager, ViewManager, ControllerManager {
}