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
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;

import java.net.URL;
import java.net.URLClassLoader;

import java.util.ArrayList;
import java.util.Map;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * @author ZHH
 *
 */
public class ResourceLoader extends URLClassLoader {

	private static Map<String, Holder> holders = new ConcurrentHashMap<String, Holder>();

	public static class Holder {
		private ResourceLoader loader;

		public void set(ResourceLoader loader) {
			this.loader = loader;
		}

		public ResourceLoader get() {
			return loader;
		}

		public void free() {
			loader.free();
			loader = null;
		}
	}

	public static Holder newHolder(Config config, ClassLoader parent) {
		ResourceLoader rl = new ResourceLoader(config, parent);
		Holder h = new Holder();
		h.set(rl);

		holders.put(config.appName, h);
		return h;
	}

	public static byte[] loadBytesFromStream(InputStream in, long len) throws IOException {
		int length = (int) len;
		byte[] buf = new byte[length];
		int nRead, count = 0;

		while ((length > 0) && ((nRead = in.read(buf, count, length)) != -1)) {
			count += nRead;
			length -= nRead;
		}
		return buf;
	}

	private Map<String, ClassResource> classResourceCache = new ConcurrentHashMap<String, ClassResource>();

	//查找java源文件和class文件的路径，要么是目录，要么是jar文件
	private ArrayList<File> findPath = new ArrayList<File>();
	private Javac javac = new Javac();
	private Config config;
	private ClassLoader parent;

	private ResourceLoader(Config config, ClassLoader parent) {
		super(config.appClassPath, parent);

		this.config = config;
		this.parent = parent;

		javac.setSrcDir(config.srcDir);
		javac.setClassesDir(config.classesDir);
		javac.setEncoding(config.javacEncoding);

		if (config.srcDir != null)
			findPath.add(new File(config.srcDir));
		if (config.classesDir != null)
			findPath.add(new File(config.classesDir));

		URL[] urls = null;
		ClassLoader cl = parent;
		while (cl != null) {
			if (cl instanceof URLClassLoader) {
				urls = ((URLClassLoader) cl).getURLs();
				for (URL url : urls) {
					//javac.addClassPath(url.toExternalForm()); //javac不能识别这种格式
					try {
						javac.addClassPath(new File(url.toURI()).getCanonicalPath());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			cl = cl.getParent();
		}
	}

	private ResourceLoader(URL[] urls, ClassLoader parent) {
		super(urls, parent);
		this.parent = parent;
	}

	private ResourceLoader copy() {
		ResourceLoader rl = new ResourceLoader(config.appClassPath, parent);
		rl.findPath = findPath;
		rl.javac = javac;
		rl.config = config;
		Holder h = holders.get(config.appName);
		h.set(rl);

		this.free();
		return rl;
	}

	public void free() {
		for (ClassResource cr : classResourceCache.values())
			cr.free();
		classResourceCache.clear();
		classResourceCache = null;

		findPath = null;
		config = null;
		javac = null;
		parent = null;
	}

	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		boolean findJavaSourceFile = false;
		if (config.isDevMode)
			findJavaSourceFile = true;

		Object c = findClassOrClassResource(name, resolve, findJavaSourceFile);

		//if(null instanceof Type)总是返回false，所以这个if语句是没必要的
		//if (c == null)
		//	throw new ClassNotFoundException(name);

		if (c instanceof Class<?>)
			return (Class<?>) c;
		else if (c instanceof ClassResource)
			return ((ClassResource) c).loadedClass;
		else
			throw new ClassNotFoundException(name);
	}

	//因为此方法是最核心的也是频繁使用的，
	//不但要加载ArrayList<File> findPath中的本地类，还要加载服务器本身及java平台的相关类，
	//如果每找到一个Class都生成一个ClassResource实例把它包装后再返回，那么开销太大。
	//所以只对findPath中的本地类才生成一个ClassResource实例

	//另外还有个更重要的功能:如果uri是服务器本身或java平台的相关类
	//如在地址栏中输入/java.io.File，那么即使从上一级classLoader中找得到，
	//但是返回的是一个Class而不是ClassResource，这样就可以直接响应404错误

	//另外，如果是一个ClassResource，那么还会查找对应的Java源文件
	private Object findClassOrClassResource(String name, boolean resolve, boolean findJavaSourceFile) {
		if (name == null)
			return null;

		name = name.trim();
		if (name.length() < 1)
			return null;

		Class<?> c = null;
		ClassResource classResource = classResourceCache.get(name);
		//(1)先查看缓存(只缓存用户应用程序类)，如果上次已加载过则直接返回
		if (classResource != null) {
			c = classResource.loadedClass;
			if (resolve)
				resolveClass(c);

			return classResource;
		}

		//*******************************************************************************
		//用eclipse直接运行时，应用的类、容器类都在同一个ClassLoader，所以都能从parent查到，
		//这样会导致错误的。
		//*******************************************************************************

		//      (2)委托上一级装载器加载
		//		if (parent != null) {
		//			//if (debug) log("[from  parent] " + name);
		//
		//			try {
		//				c = parent.loadClass(name);
		//				if (c != null) {
		//					if (resolve)
		//						resolveClass(c);
		//					return c;
		//				}
		//			} catch (Throwable t) {
		//				//忽略
		//			}
		//		}
		//
		//		//(3)尝试从系统装载器加载
		//		//if (debug) log("[from  system] " + name);
		//		try {
		//			c = findSystemClass(name);
		//			if (c != null) {
		//				if (resolve)
		//					resolveClass(c);
		//				return c;
		//			}
		//		} catch (Throwable t) {
		//			//忽略
		//		}

		//(4)如果仍未找到，
		//尝试从findPath加载
		//在找到指定的类后，同时要把它缓存起来
		//if (debug) log("[from local classPath] " + name);

		File classFile = null;
		byte[] classData = null;

		String fileName = name.replace('.', File.separatorChar);
		String classFileName = fileName + ".class";

		String zipEntryName = classFileName;
		if (File.separatorChar != '/') {
			zipEntryName = name.replace('.', '/') + ".class";
		}

		for (File base : findPath) {
			//从目录中查找class文件并读取字节数据
			if (base.isDirectory()) {
				classFile = new File(base, classFileName);

				if (classFile.exists()) {
					InputStream in = null;
					try {
						in = new FileInputStream(classFile);
						classData = loadBytesFromStream(in, classFile.length());
					} catch (Throwable t) {
						throw new ResourceLoaderException("failed to load file: " + classFile, t);
					} finally {
						if (in != null) {
							try {
								in.close();
							} catch (Throwable t) {
							}
						}
					}
				}
			} else { //从zip(jar)文件中查找class文件并读取字节数据
				classFile = base; //注意: class文件是从zip(jar)文件中读取的

				ZipFile zipfile = null;
				try {
					zipfile = new ZipFile(base);
					ZipEntry entry = zipfile.getEntry(zipEntryName);
					if (entry != null)
						classData = loadBytesFromStream(zipfile.getInputStream(entry), entry.getSize());
				} catch (Throwable t) {
					throw new ResourceLoaderException("failed to load zip entry: " + zipEntryName, t);
				} finally {
					if (zipfile != null) {
						try {
							zipfile.close();
						} catch (Throwable t) {
						}
					}
				}
			}

			if (classData != null) {
				try {
					c = defineClass(name, classData, 0, classData.length);
					if (resolve)
						resolveClass(c);
				} catch (Throwable t) {
					//defineClass可能发生异常(如ClassFormatError)?
					throw new ResourceLoaderException("failed to define class: " + name, t);
				}

				classResource = new ClassResource();
				classResource.loadedClass = c;
				classResource.classFile = classFile;
				classResource.classFileLastModified = classFile.lastModified();
				classResourceCache.put(name, classResource);
				break; //classFile已经找到了，退出for循环
			}
		}
		//继续寻找对应的java源文件
		//(注意:退出for循环后，如果classResource还为null说明classFile没找到)
		if (findJavaSourceFile) {
			String javaSourceFileName = fileName + ".java";
			File javaSourceFile = null;

			//尝试寻找Java源文件
			for (File base : findPath) {
				//TODO 只从目录中查找java文件，未来是否考虑从zip(jar)中读取？
				if (base.isDirectory()) {
					File f = new File(base, javaSourceFileName);

					if (f.exists()) {
						javaSourceFile = f;
						break;
					}
				}
			}

			if (javaSourceFile != null) {
				if (classResource == null) {
					classResource = new ClassResource();
				}

				classResource.sourceFile = javaSourceFile;
				classResource.sourceFileLastModified = javaSourceFile.lastModified();
			}
			//如果classFile没找到，即使sourceFile找到了也不用缓存
			//因为只缓存class文件的Class实例
			//classResourceCache.put(name, classResource);
		}

		if (classResource != null) {
			return classResource;
		}

		//(2)委托上一级装载器加载
		if (parent != null) {
			try {
				c = parent.loadClass(name);
				if (c != null) {
					if (resolve)
						resolveClass(c);
					return c;
				}
			} catch (Throwable t) {
				//忽略
			}
		}

		//(3)尝试从系统装载器加载
		try {
			c = findSystemClass(name);
			if (c != null) {
				if (resolve)
					resolveClass(c);
				return c;
			}
		} catch (Throwable t) {
			//忽略
		}

		return null;
	}

	private static String SUFFIX = "$DOUYU";

	public ClassResource loadContextClassResource(String controllerClassName, PrintWriter out) throws JavacException {
		String contextClassName = controllerClassName + SUFFIX;
		ClassResource resource = classResourceCache.get(contextClassName);
		if (resource == null) {
			resource = loadContextClassResource(controllerClassName, contextClassName, out);

			if (resource != null) {
				classResourceCache.put(contextClassName, resource);
			}
		}

		if (resource != null && config.isDevMode) {
			if (classResourceModified(out)) {
				return copy().loadContextClassResource(controllerClassName, out);
			}
		}

		return resource;
	}

	private ClassResource loadContextClassResource(String controllerClassName, String contextClassName, PrintWriter out) {

		//带有SUFFIX后缀的类(以下简称:context类)，无需加载java源文件
		ClassResource context = loadClassResource(contextClassName, false);

		if (!config.isDevMode)
			return context;

		//带有@Controller标注的类(以下简称:controller类)
		//注意:事先并不知道controllerClassName是否是一个controller类，
		//所以先假定它是controller类，
		//当编译这个假想的controller类后，如果得不到对应的context类，
		//那么就返回错误(比如返回404 或 返回400(Bad request)
		ClassResource controller = loadClassResource(controllerClassName, true);

		//都未找到
		if (context == null && controller == null) {
			return null;
		} else { //找到controller类或context类其中之一，或两者都找到了

			//controller类找不到(对应的java源文件和class文件都找不到)
			//这可能是由于误删除引起的，所以不管context类是否存在都无意义了，
			//因为context类总是要引用controller类的.
			if (controller == null) {
				return null;
			}
			//通常是第一次请求controller类，此时服务器需要尝试编译它，并生成context类
			else if (controller != null && context == null) {
				//未找到controller类的java源文件
				if (controller.sourceFile == null) {
					return null;
				} else {
					javac.compile(out, controller.sourceFile);

					//如果不是有效的controller类时，可能为null
					return loadClassResource(contextClassName, false);
				}
			} else { //两者都找到了,直接返回context
				return context;
			}
		}
	}

	private ClassResource loadClassResource(String name, boolean findJavaSourceFile) {
		ClassResource cr = null;
		Object c = findClassOrClassResource(name, true, findJavaSourceFile);
		if (c instanceof ClassResource)
			cr = (ClassResource) c;

		return cr;
	}

	private boolean classResourceModified(PrintWriter out) {
		boolean modified = false;
		ArrayList<File> files = new ArrayList<File>();

		for (ClassResource cr : classResourceCache.values()) {
			int command = check(cr);
			if (command == LOAD || command == COMPLIE_AND_LOAD) {
				modified = true;

				if (command == COMPLIE_AND_LOAD) {
					files.add(cr.sourceFile);
				}
			}
		}

		if (files.size() > 0) {
			//TODO 
			//正确的做法应该是把classResourceCache中的所有源文件都重新编译一次，因为有可能对A修改了一个方法，A被重新编译了，
			//但是因为B调用了A的这个方法，而B的源文件没有修改，所以B还是调用A的是旧版本.
			//另外，如果所有源文件都要重新编译，性能问题是不是可以接受?
			files.clear();
			for (ClassResource cr : classResourceCache.values()) {
				if (cr.sourceFile != null)
					files.add(cr.sourceFile);
			}

			javac.compile(out, files);
		}
		return modified;
	}

	private static final int NONE = 0;
	private static final int LOAD = 1;
	private static final int COMPLIE_AND_LOAD = 2;

	//ClassResource的sourceFile和classFile保证不同时为null
	private int check(ClassResource cr) {
		//(1)只有java源文件时，不管isDevMode的值是什么，
		//先编译源文件，然后加载class文件
		if (cr.sourceFile != null && cr.classFile == null)
			return COMPLIE_AND_LOAD;

		//(2)同时有java源文件和class文件，
		//或者只有class文件的情况
		//(出现这种情况的原因是:在第一次由服务器自动编译(或用户手工编译)完java源文件后，
		//用户可以把java源文件删除，或者class文件是从其他地方编译而来的)
		//这两种情况都要根据isDevMode的值来决定，
		//如果isDevMode是false，就表示仍然用缓存中的类
		//如果isDevMode是true， 就必须按下面的规则重新加载

		if (config.isDevMode) {
			//(2.1)同时有java源文件和class文件且java源文件比class文件新，
			//那么先编译java源文件，然后加载class文件
			if (cr.sourceFile != null && cr.classFile != null && cr.sourceFile.lastModified() > cr.classFile.lastModified()) {
				return COMPLIE_AND_LOAD;
			}

			//(2.2)只有class文件的情况,如果class文件比缓存中的类新，
			//那么重新加载class文件(缓存中的类的时间用cr.classFileLastModified表示)
			if (cr.sourceFile == null && cr.classFile != null && cr.classFile.lastModified() > cr.classFileLastModified) {
				return LOAD;
			}
		}
		return NONE;
	}

}