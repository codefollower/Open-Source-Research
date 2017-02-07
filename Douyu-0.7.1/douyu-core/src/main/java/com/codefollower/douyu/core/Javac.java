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
package com.codefollower.douyu.core;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.lang.model.SourceVersion;

import com.sun.tools.javac.Main;
import com.sun.tools.javac.code.Source;
import com.sun.tools.javac.processing.ControllerProcessor;

/**
 * 
 * @author ZHH
 *
 */
public class Javac {
	private StringBuilder classPath = new StringBuilder();
	private String classesDir; //存放编译后的类文件或自动生成的java源文件
	private String encoding;

	public Javac() {
	}

	public void addClassPath(String cp) {
		classPath.append(cp).append(File.pathSeparatorChar);
	}

	public void setSrcDir(String srcDir) {
		addClassPath(srcDir);
	}

	public void setClassesDir(String classesDir) {
		this.classesDir = classesDir;
		addClassPath(classesDir);
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public synchronized void compile(PrintWriter out, File... files) throws JavacException {
		compile(out, Arrays.asList(files));
	}

	public synchronized void compile(PrintWriter out, List<File> files) throws JavacException {
		ArrayList<String> args = new ArrayList<String>();
		//args.add("-Xlint:unchecked");

		//否则取不到方法参数名(因为有class文件时会优先加载class文件)
		//ClassReader类并不解析方法参数，导致MethodSymbol.params为null
		args.add("-Xprefer:source");
		
		//为了清除javac出现的这个warning
		//warning: Implicitly compiled files were not subject to annotation processing.
		//args.add("-implicit:none");
		args.add("-implicit:class");

		if (encoding != null) {
			args.add("-encoding");
			args.add(encoding);
		}

		if (classesDir != null) {
			args.add("-s");
			args.add(classesDir);
			args.add("-d");
			args.add(classesDir);
		}

		args.add("-processor");
		args.add(ControllerProcessor.class.getName());

		if (SourceVersion.latest() == SourceVersion.RELEASE_6) {
			args.add("-target");
			args.add("1.6");
			args.add("-source");
			args.add("1.6");
		}

		//没有"-Xlint:-options"，
		//在com.sun.tools.javac.main.JavaCompiler.JavaCompiler(Context context)会出warning
		if (Source.DEFAULT.ordinal() > Source.JDK1_6.ordinal())
			args.add("-Xlint:-options");

		if (classPath.length() > 0) {
			args.add("-cp");
			args.add(classPath.toString());
		}

		for (File f : files) {
			try {
				args.add(f.getCanonicalPath());
			} catch (IOException e) {
				throw new JavacException("failed to compile file: " + f);
			}
		}

		try {
			//0以外的值都表示编译失败
			if (Main.compile(args.toArray(new String[0]), out) != 0) {
				throw new JavacException("failed to compile files: " + files);
			}
		} catch (Throwable e) {
			throw new JavacException("failed to compile files: " + files, e);
		}
	}

}