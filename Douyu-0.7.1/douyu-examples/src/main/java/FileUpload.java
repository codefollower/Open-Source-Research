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
import java.io.File;
import java.io.PrintWriter;

import douyu.mvc.Context;
import douyu.mvc.Controller;

import douyu.http.HttpRequest;
import douyu.http.UploadedFile;

@Controller
public class FileUpload {
	public void index(Context c) {
		c.out("FileUpload.html");
	}

	public void upload(HttpRequest request, UploadedFile[] uploadedFiles, UploadedFile file1, String description, PrintWriter out) {

		out.println("说明: " + description);
		out.println("file1: " + request.getUploadedFile("file1"));
		out.println("uploadedFiles.length: " + uploadedFiles.length);
		out.println();

		if (uploadedFiles != null) {
			for (UploadedFile uf : uploadedFiles) {
				//注意这里，file1与uploadedFiles中的某一个元素指向同一个对象
				if (file1 == uf) {
					out.println("这是文件1:");
					out.println();
				}

				out.println("大小  : " + uf.getSize() + " 字节");
				out.println("类型  : " + uf.getContentType());
				out.println("文件名: " + uf.getSimpleName());
				out.println("全名  : " + uf.getFullName());
				out.println("路径名: " + uf.getPathName());

				out.println();
				out.println("文件内容:");
				out.println("--------------------------------------");
				out.println(uf.getContent());
				out.println("--------------------------------------");

				out.println();

				File file = new File("E:/Douyu/douyu-examples-classes/uploadedFiles", uf.getSimpleName());

				try {
					uf.saveTo(file);
					out.println("已保存到: " + file);
				} catch (Exception e) {
					out.println("出错了: " + e);
				}
				out.println();
				out.println();
			}
		}
	}
}
