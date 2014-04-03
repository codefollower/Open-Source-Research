/*
 * @(#)Paths.java	1.25 07/03/21
 * 
 * Copyright (c) 2007 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *  
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *  
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *  
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *  
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package com.sun.tools.javac.util;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.jar.Attributes;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import com.sun.tools.javac.code.Lint;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.Options;
import com.sun.tools.javac.util.Position;
import javax.tools.JavaFileManager.Location;

import static com.sun.tools.javac.main.OptionName.*;
import static javax.tools.StandardLocation.*;

/** This class converts command line arguments, environment variables
 *  and system properties (in File.pathSeparator-separated String form)
 *  into a boot class path, user class path, and source path (in
 *  Collection<String> form).
 *
 *  <p><b>This is NOT part of any API supported by Sun Microsystems.  If
 *  you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 */
@Version("@(#)Paths.java	1.25 07/03/21")
public class Paths {
	
    private static my.Debug DEBUG=new my.Debug(my.Debug.Paths);//我加上的
	
    /** The context key for the todo list */
    protected static final Context.Key<Paths> pathsKey =
	new Context.Key<Paths>();

    /** Get the Paths instance for this context. */
    public static Paths instance(Context context) {
		Paths instance = context.get(pathsKey);
		if (instance == null)
			instance = new Paths(context);
		return instance;
    }

    /** The log to use for warning output */
    private Log log;

    /** Collection of command-line options */
    private Options options;

    /** Handler for -Xlint options */
    private Lint lint;

    protected Paths(Context context) {
        DEBUG.P(this,"Paths(1)");
		context.put(pathsKey, this);
		pathsForLocation = new HashMap<Location,Path>(16);
        setContext(context);
        DEBUG.P(0,this,"Paths(1)");
    }

    void setContext(Context context) {
        log = Log.instance(context);
        options = Options.instance(context);
        lint = Lint.instance(context);
    }

    /** Whether to warn about non-existent path elements */
    private boolean warn;

    private Map<Location, Path> pathsForLocation;

    private boolean inited = false; // TODO? caching bad?

    /**
     * rt.jar as found on the default bootclass path.  If the user specified a
     * bootclasspath, null is used.
     */
    private File bootClassPathRtJar = null;

    Path getPathForLocation(Location location) {
    	DEBUG.P(this,"getPathForLocation(1)");
        Path path = pathsForLocation.get(location);
        DEBUG.P("path="+path);
        if (path == null)
            setPathForLocation(location, null);
            
        DEBUG.P(0,this,"getPathForLocation(1)");
		return pathsForLocation.get(location);
    }
    
    void setPathForLocation(Location location, Iterable<? extends File> path) {
        DEBUG.P(this,"setPathForLocation(2)");
		// TODO? if (inited) throw new IllegalStateException
		// TODO: otherwise reset sourceSearchPath, classSearchPath as needed
		Path p;
		if (path == null) {
			if (location == CLASS_PATH)
				p = computeUserClassPath();
			else if (location == PLATFORM_CLASS_PATH)
				p = computeBootClassPath();
			else if (location == ANNOTATION_PROCESSOR_PATH)
				p = computeAnnotationProcessorPath();
			else if (location == SOURCE_PATH)
				p = computeSourcePath();
			else 
				// no defaults for other paths
				p = null;
		} else {
			p = new Path();
			for (File f: path)
				p.addFile(f, warn); // TODO: is use of warn appropriate? 
		}
		pathsForLocation.put(location, p);
		
		DEBUG.P(1,this,"setPathForLocation(2)");
    }

    protected void lazy() {
		DEBUG.P(this,"lazy()");
		DEBUG.P("inited="+inited);
		
		//在初始化时执行(也就是在parser之前)
		if (!inited) {
			//是否加了Xlint:中的path选项,一般为没加
			//如果加了-Xlint:path时，如果路径名有错时，会发出警告
			warn = lint.isEnabled(Lint.LintCategory.PATH);
			
			pathsForLocation.put(PLATFORM_CLASS_PATH, computeBootClassPath());
			
			DEBUG.P(this,"computeUserClassPath()");
			pathsForLocation.put(CLASS_PATH, computeUserClassPath());
			DEBUG.P(2,this,"computeUserClassPath()");
			
			DEBUG.P(this,"computeSourcePath()");
			pathsForLocation.put(SOURCE_PATH, computeSourcePath());
			DEBUG.P(2,this,"computeSourcePath()");

			inited = true;

			DEBUG.P("BootClassPath="+pathsForLocation.get(PLATFORM_CLASS_PATH));
			DEBUG.P("UserClassPath="+pathsForLocation.get(CLASS_PATH));
			DEBUG.P("SourcePath   ="+pathsForLocation.get(SOURCE_PATH));
		}
		
		DEBUG.P(0,this,"lazy()");
    }

    public Collection<File> bootClassPath() {
        lazy();
        return Collections.unmodifiableCollection(getPathForLocation(PLATFORM_CLASS_PATH));
    }
    public Collection<File> userClassPath() {
        lazy();
        return Collections.unmodifiableCollection(getPathForLocation(CLASS_PATH));
    }
    public Collection<File> sourcePath() {
        lazy();
		Path p = getPathForLocation(SOURCE_PATH);
        return p == null || p.size() == 0
            ? null
            : Collections.unmodifiableCollection(p);
    }

    boolean isBootClassPathRtJar(File file) {
        return file.equals(bootClassPathRtJar);
    }
    
    /*
    这是我加上的,用于调试,用在com.sun.tools.javac.util.JavacFileManager===>openArchive(1)
    */
    public File getBootClassPathRtJar() {
    	return bootClassPathRtJar;
    }
    
    //实现了Iterable<String>接口的类可用在有foreach语句的地方(JDK>=1.5才能用)
    private static class PathIterator implements Iterable<String> {
		private int pos = 0;
		private final String path;
		private final String emptyPathDefault;
	
		//按分号";"(windows)或冒号":"(unix/linux)将多个路径分开 
		public PathIterator(String path, String emptyPathDefault) {
			DEBUG.P(this,"PathIterator(2)");
			DEBUG.P("path="+path);
			DEBUG.P("emptyPathDefault="+emptyPathDefault);
				
			this.path = path;
			this.emptyPathDefault = emptyPathDefault;
				
			DEBUG.P(0,this,"PathIterator(2)");
		}

		public PathIterator(String path) { this(path, null); }
		public Iterator<String> iterator() {
			return new Iterator<String>() {//这里的匿名类实现了Iterator<E>接口
				public boolean hasNext() {
					return pos <= path.length();
				}
				public String next() {
					try {//我加上的
					DEBUG.P(this,"next()");
						
					int beg = pos;
					//File.pathSeparator路径分隔符,windows是分号";",unix/linux是冒号":"
					int end = path.indexOf(File.pathSeparator, beg);
						
					DEBUG.P("beg="+beg+" end="+end);
						
					if (end == -1)
						end = path.length();
					pos = end + 1;
						
					DEBUG.P("beg="+beg+" end="+end);
						
					//(beg == end)路径分隔符在最前面或最后面或连续出现的情况(如“:dir1::dir2:”)
					//如果没有emptyPathDefault==null，
					//那么path.substring(beg, end)返回一个空串("")，在用空串生成File的实例时
					//这个File的实例代表的是当前目录，所以把emptyPathDefault设成“.”是多余的
					//见computeUserClassPath()最后一条语句
					if (beg == end && emptyPathDefault != null)
						return emptyPathDefault;
					else
						return path.substring(beg, end);
						
					} finally {
					DEBUG.P(0,this,"next()");
					}
				}
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		}
    }

    private class Path extends LinkedHashSet<File> {
		private static final long serialVersionUID = 0;

		private boolean expandJarClassPaths = false;
        private Set<File> canonicalValues = new HashSet<File>();

		public Path expandJarClassPaths(boolean x) {
			expandJarClassPaths = x;
			return this;
		}

		/** What to use when path element is the empty string */
		private String emptyPathDefault = null;

		public Path emptyPathDefault(String x) {
			emptyPathDefault = x;
			return this;
		}

		public Path() { super(); }

		public Path addDirectories(String dirs, boolean warn) {
			DEBUG.P(this,"addDirectories(2)");
			DEBUG.P("warn="+warn+" dirs="+dirs);
			
			if (dirs != null)
				for (String dir : new PathIterator(dirs))
					addDirectory(dir, warn);
			
			DEBUG.P(1,this,"addDirectories(2)");
			return this;
		}

		public Path addDirectories(String dirs) {
			return addDirectories(dirs, warn);
		}
	
		//从给定目录下查找文件时，只找扩展名为jar与zip的文件
		private void addDirectory(String dir, boolean warn) {
            try {//我加上的
            DEBUG.P(this,"addDirectory(2)");
            DEBUG.P("warn="+warn+" dir="+dir);
            DEBUG.P("isDirectory()="+new File(dir).isDirectory());
		
			if (! new File(dir).isDirectory()) {
				//如果是像System.getProperty("java.endorsed.dirs")这种编译器假设的目录
				//要是不存在的话，因为在调用addDirectory时把warn设成false了，所以不会警告。
				if (warn)
					log.warning("dir.path.element.not.found", dir);
				return;
			}

            File[] files = new File(dir).listFiles();//列出dir目录下的文件和目录(没有递归子目录)
            
            if (files == null) DEBUG.P("files=null");
            else {
                DEBUG.P("files.length="+files.length);
                //DEBUG.P("files="+files);
                for (File direntry : files) {
                    DEBUG.P("[isArchive="+isArchive(direntry)+"]direntry="+direntry);
                }
            }
            
            if (files == null)
                return;
            
			for (File direntry : files) {
                if (isArchive(direntry)) {
                    DEBUG.P("direntry="+direntry);
                    addFile(direntry, warn);
                }
			}
	    
			} finally {
            DEBUG.P(0,this,"addDirectory(2)");
			}
		}

		public Path addFiles(String files, boolean warn) {
            DEBUG.P(this,"addFiles(2)");
            DEBUG.P("warn="+warn+" files="+files);
            
			if (files != null)
			for (String file : new PathIterator(files, emptyPathDefault)) {
				//DEBUG.P("fileName="+file);
				addFile(file, warn);
			}
                
            DEBUG.P(1,this,"addFiles(2)");
			return this;
		}

		public Path addFiles(String files) {
			return addFiles(files, warn);
		}
		
		public Path addFile(String file, boolean warn) {
			addFile(new File(file), warn);
			return this;
		}
        
        //参数file可以代表一个文件也可代表一个目录
		public void addFile(File file, boolean warn) {
            try {//我加上的
            DEBUG.P(this,"addFile(2)");
            DEBUG.P("warn="+warn+" file="+file);
		
		
            File canonFile;
            try {
                //规范化的文件(一般是包涵绝对路径的文件)
                canonFile = file.getCanonicalFile();
            } catch (IOException e) {
                canonFile = file;
            }
            DEBUG.P("canonFile="+canonFile);
        
        
            //contains(file)在哪??? 在LinkedHashSet<File>(Path继承了LinkedHashSet<File>)
			if (contains(file) || canonicalValues.contains(canonFile)) {
                /* Discard duplicates and avoid infinite recursion */

                DEBUG.P("文件已存在,返回");
                return;
			}
	    
			DEBUG.P("file.exists()="+file.exists());
			DEBUG.P("file.isFile()="+file.isFile());
			DEBUG.P("file.isArchive()="+isArchive(file));
			DEBUG.P("expandJarClassPaths="+expandJarClassPaths);
	    
            /*
            假设有：javac -Xlint:path -Xbootclasspath/p:srcs:JarTest:args.txt:classes
             * 其中srcs是一个不存在的目录，JarTest是由“JarTest.jar”删除扩展名“.jar”后得到的
             * 实际存在的jar文件，args.txt也是一个存在的文本文件，则对应如下警告:
			警告：[path] 错误的路径元素 "srcs"：无此文件或目录
            警告：[path] 以下归档文件存在意外的扩展名: JarTest
            警告：[path] 以下路径中存在意外的文件: args.txt
            */

            if (! file.exists()) {
                /* No such file or directory exists */
                if (warn)
                    log.warning("path.element.not.found", file);	
			} else if (file.isFile()) {
                /* File is an ordinary file. */ 
                if (!isArchive(file)) {
                    /* Not a recognized extension; open it to see if
                     it looks like a valid zip file. */
                    try {
                        ZipFile z = new ZipFile(file);
                        z.close();
                        if (warn)
                            log.warning("unexpected.archive.file", file);
                    } catch (IOException e) {
                        // FIXME: include e.getLocalizedMessage in warning
                        if (warn)
                            log.warning("invalid.archive.file", file);
                        return;
                    }
                }
			}
        
			/* Now what we have left is either a directory or a file name
			   confirming to archive naming convention */
			   
			//当文件或目录不存在时，作者还是同样把它加到HashSet<File>
			super.add(file);//从类 java.util.HashSet 继承的方法
				canonicalValues.add(canonFile);

				//是否展开压缩文件(如jar文件)
			if (expandJarClassPaths && file.exists() && file.isFile())
                addJarClassPath(file, warn);

            } finally {
                DEBUG.P(0,this,"addFile(2)");
            }
		}

		// Adds referenced classpath elements from a jar's Class-Path
		// Manifest entry.  In some future release, we may want to
		// update this code to recognize URLs rather than simple
		// filenames, but if we do, we should redo all path-related code.
		private void addJarClassPath(File jarFile, boolean warn) {
            try {
            DEBUG.P(this,"addJarClassPath(2)");
            DEBUG.P("warn="+warn+" jarFile="+jarFile);
            
			try {
				String jarParent = jarFile.getParent();
				
				DEBUG.P("jarParent="+jarParent);
				
				JarFile jar = new JarFile(jarFile);

				try {
					Manifest man = jar.getManifest();
							DEBUG.P("man="+man);
					if (man == null) return;

					Attributes attr = man.getMainAttributes();
							DEBUG.P("attr="+attr);
					if (attr == null) return;
					
					//是指：java.util.jar.Attributes.Name
					String path = attr.getValue(Attributes.Name.CLASS_PATH);
					DEBUG.P("Attributes.Name.CLASS_PATH="+path);
					//在System.getProperty("sun.boot.class.path")里包含的jar文件没有一个有CLASS_PATH
					if (path == null) return;

					for (StringTokenizer st = new StringTokenizer(path);
					 st.hasMoreTokens();) {
					String elt = st.nextToken();
					File f = (jarParent == null ? new File(elt) : new File(jarParent, elt));
					addFile(f, warn);
					}
				} finally {
					jar.close();
				}
			} catch (IOException e) {
				log.error("error.reading.file", jarFile, e.getLocalizedMessage());
			}
            
            
            } finally {
			DEBUG.P(0,this,"addJarClassPath(2)");
            }
		}
    }
    
    /**
     * 如指定“-Xbootclasspath/p:<路径>”时，分开“<路径>”中的目录或文件加进Path；
     * 提取“-endorseddirs <目录>”指定的目录(不包括子目录)中的所有jar、zip文件加进Path
     * 如指定“-bootclasspath <路径>”时，分开“<路径>”中的目录或文件加进Path；
     * 如指定“-Xbootclasspath/a:<路径>”时，分开“<路径>”中的目录或文件加进Path；
     * 提取“-extdirs <目录> ”指定的目录(不包括子目录)中的所有jar、zip文件加进Path

	 * 凡是“<路径>”都调用addFiles，凡是“<目录>”都调用addDirectories。
     */
    //此方法一定不会返回null
    private Path computeBootClassPath() {
        DEBUG.P(this,"computeBootClassPath()");

        bootClassPathRtJar = null;
		String optionValue;
		Path path = new Path();
		
		DEBUG.P(XBOOTCLASSPATH_PREPEND+"="+options.get(XBOOTCLASSPATH_PREPEND));
		path.addFiles(options.get(XBOOTCLASSPATH_PREPEND));
		
		
		DEBUG.P(ENDORSEDDIRS+"="+options.get(ENDORSEDDIRS));
		
		//-endorseddirs <目录> 覆盖签名的标准路径的位置
		if ((optionValue = options.get(ENDORSEDDIRS)) != null)
			path.addDirectories(optionValue);
		else {
            DEBUG.P("java.endorsed.dirs="+System.getProperty("java.endorsed.dirs"));
            //输出:D:\Java\jre1.6.0\lib\endorsed(此目录一般不存在)
			path.addDirectories(System.getProperty("java.endorsed.dirs"), false);
		}
	    
		//-bootclasspath <路径>        覆盖引导类文件的位置
		DEBUG.P(BOOTCLASSPATH+"="+options.get(BOOTCLASSPATH));
        if ((optionValue = options.get(BOOTCLASSPATH)) != null) {
            path.addFiles(optionValue);
        } else {
            DEBUG.P("sun.boot.class.path="+System.getProperty("sun.boot.class.path"));
            //输出:sun.boot.class.path=D:\Java\jre1.6.0\lib\resources.jar;D:\Java\jre1.6.0\lib\rt.jar;D:\Java\jre1.6.0\lib\sunrsasign.jar;D:\Java\jre1.6.0\lib\jsse.jar;D:\Java\jre1.6.0\lib\jce.jar;D:\Java\jre1.6.0\lib\charsets.jar;D:\Java\jre1.6.0\classes
            
            //在Ubuntu下运行如下命令时
            //java -Xbootclasspath/p:src:classes -Xbootclasspath/a:src:classes -classpath src:classes com.sun.tools.javac.Main
            //输出:sun.boot.class.path=src:classes:/home/zhh/java/jdk1.6.0_04/jre/lib/resources.jar:/home/zhh/java/jdk1.6.0_04/jre/lib/rt.jar:/home/zhh/java/jdk1.6.0_04/jre/lib/sunrsasign.jar:/home/zhh/java/jdk1.6.0_04/jre/lib/jsse.jar:/home/zhh/java/jdk1.6.0_04/jre/lib/jce.jar:/home/zhh/java/jdk1.6.0_04/jre/lib/charsets.jar:/home/zhh/java/jdk1.6.0_04/jre/classes:src:classes
            
            // Standard system classes for this compiler's release.
            String files = System.getProperty("sun.boot.class.path");
            path.addFiles(files, false);
            File rt_jar = new File("rt.jar");
            
            DEBUG.P("rt_jar="+rt_jar);
            DEBUG.P("bootClassPathRtJar="+bootClassPathRtJar);
            
            for (String file : new PathIterator(files, null)) {
                File f = new File(file);
                if (new File(f.getName()).equals(rt_jar))
                    bootClassPathRtJar = f;
            }
            DEBUG.P("bootClassPathRtJar="+bootClassPathRtJar);
        }

        DEBUG.P(XBOOTCLASSPATH_APPEND+"="+options.get(XBOOTCLASSPATH_APPEND));
		path.addFiles(options.get(XBOOTCLASSPATH_APPEND));
	
	
		DEBUG.P(EXTDIRS+"="+options.get(EXTDIRS));

		// Strictly speaking, standard extensions are not bootstrap
		// classes, but we treat them identically, so we'll pretend
		// that they are.
		if ((optionValue = options.get(EXTDIRS)) != null)
			path.addDirectories(optionValue);
		else {
            DEBUG.P("java.ext.dirs="+System.getProperty("java.ext.dirs"));
			path.addDirectories(System.getProperty("java.ext.dirs"), false);
		}
	
		DEBUG.P(2,this,"computeBootClassPath()");
		return path;
    }
    
    
    
    
    
    //用户级别的类路径搜索顺序如下(前一级不存在才往下搜索)：
    //javac -classpath==>OS环境变量CLASSPATH==>application.home(这个不知道在哪设?)==>
    //java -classpath ==>当前目录(.)
    //另外类路径里的jar或zip文件需要展开
    //此方法一定不会返回null

	//2009-3-14更新: 可以通过  java -Dapplication.home="这里用测试字符串替换"
	//使得System.getProperty("application.home")不为null
    private Path computeUserClassPath() {
		DEBUG.P(CLASSPATH+"="+options.get(CLASSPATH));
		DEBUG.P("env.class.path="+System.getProperty("env.class.path"));
		DEBUG.P("application.home="+System.getProperty("application.home"));
		DEBUG.P("java.class.path="+System.getProperty("java.class.path"));
		
		String cp = options.get(CLASSPATH);
		// CLASSPATH environment variable when run from `javac'.
		if (cp == null) cp = System.getProperty("env.class.path");

		// If invoked via a java VM (not the javac launcher), use the
		// platform class path
		//如java -classpath src;classes com.sun.tools.javac.Main ....
		//则System.getProperty("java.class.path")="src;classes"，
		//也就是java命令的-classpath选项所指定的参数，
		//如果java命令不待-classpath选项，
		//则System.getProperty("java.class.path")="."(也就是当前目录)
		if (cp == null && System.getProperty("application.home") == null)
			cp = System.getProperty("java.class.path");

		// Default to current working directory.
		if (cp == null) cp = ".";

		//在-classpath中指定的jar文件要展开
        return new Path()
	    .expandJarClassPaths(true) // Only search user jars for Class-Paths
	    .emptyPathDefault(".")     // Empty path elt ==> current directory
	    .addFiles(cp);
    }




    //此方法有可能返回null
    private Path computeSourcePath() {
		//-sourcepath <路径>           指定查找输入源文件的位置
		DEBUG.P(SOURCEPATH+"="+options.get(SOURCEPATH));
		
		String sourcePathArg = options.get(SOURCEPATH);
		if (sourcePathArg == null)
			return null;

		return new Path().addFiles(sourcePathArg);
    }

    //此方法有可能返回null
    private Path computeAnnotationProcessorPath() {
        try {
        //-processorpath <路径>        指定查找注释处理程序的位置
        DEBUG.P(this,"computeAnnotationProcessorPath()");
        DEBUG.P(PROCESSORPATH+"="+options.get(PROCESSORPATH));
    
		String processorPathArg = options.get(PROCESSORPATH);
		if (processorPathArg == null)
			return null;

		return new Path().addFiles(processorPathArg);
		
		}finally{
		DEBUG.P(0,this,"computeAnnotationProcessorPath()");
		}
    }

    /** The actual effective locations searched for sources */
    private Path sourceSearchPath;
    
    //当sourcePath为null时取userClassPath的值
    public Collection<File> sourceSearchPath() {
		if (sourceSearchPath == null) {
			lazy();
			Path sourcePath = getPathForLocation(SOURCE_PATH);
			Path userClassPath = getPathForLocation(CLASS_PATH);
			sourceSearchPath = sourcePath != null ? sourcePath : userClassPath;
		}
		return Collections.unmodifiableCollection(sourceSearchPath);
    }

    /** The actual effective locations searched for classes */
    private Path classSearchPath;

    public Collection<File> classSearchPath() {
		if (classSearchPath == null) {
			lazy();
			Path bootClassPath = getPathForLocation(PLATFORM_CLASS_PATH);
			Path userClassPath = getPathForLocation(CLASS_PATH);
			classSearchPath = new Path();
			classSearchPath.addAll(bootClassPath);
			classSearchPath.addAll(userClassPath);
		}
		return Collections.unmodifiableCollection(classSearchPath);
    }
    
    /** The actual effective locations for non-source, non-class files */
    private Path otherSearchPath;
    
    Collection<File> otherSearchPath() {
		if (otherSearchPath == null) {
			lazy();
			Path userClassPath = getPathForLocation(CLASS_PATH);
			Path sourcePath = getPathForLocation(SOURCE_PATH);
			if (sourcePath == null)
				otherSearchPath = userClassPath;
			else {
				otherSearchPath = new Path();
				otherSearchPath.addAll(userClassPath);
				otherSearchPath.addAll(sourcePath);
			}
		}
		return Collections.unmodifiableCollection(otherSearchPath);
    }

    /** Is this the name of an archive file? */
    private static boolean isArchive(File file) {
		String n = file.getName().toLowerCase();
		return file.isFile()
			&& (n.endsWith(".jar") || n.endsWith(".zip"));
    }
}
