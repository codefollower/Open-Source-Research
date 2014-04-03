/*
 * @(#)Main.java	1.115 07/03/21
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

package com.sun.tools.javac.main;

import com.sun.tools.javac.util.Options;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.MissingResourceException;

import com.sun.tools.javac.code.Source;
import com.sun.tools.javac.jvm.Target;
import com.sun.tools.javac.main.JavacOption.Option;
import com.sun.tools.javac.main.RecognizedOptions.OptionHelper;
import com.sun.tools.javac.util.*;
import com.sun.tools.javac.processing.AnnotationProcessingError;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.annotation.processing.Processor;

/** This class provides a commandline interface to the GJC compiler.
 *
 *  <p><b>This is NOT part of any API supported by Sun Microsystems.  If
 *  you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 */
@Version("@(#)Main.java	1.115 07/03/21")
public class Main {
    private static my.Debug DEBUG=new my.Debug(my.Debug.Main);//我加上的

    /** The name of the compiler, for use in diagnostics.
     */
    String ownName;

    /** The writer to use for diagnostic output.
     */
    PrintWriter out;

    /**
     * If true, any command line arg errors will cause an exception.
     */
    boolean fatalErrors;

    /** Result codes.
     */
    static final int
        EXIT_OK = 0,        // Compilation completed with no errors.
        EXIT_ERROR = 1,     // Completed but reported errors.
        EXIT_CMDERR = 2,    // Bad command-line arguments
        EXIT_SYSERR = 3,    // System error or resource exhaustion.
        EXIT_ABNORMAL = 4;  // Compiler terminated abnormally
        
    //我加上的
    public static String resultCode(int rc) {
    	switch (rc) {
    		case 0: return "EXIT_OK";
    		case 1: return "EXIT_ERROR";
    		case 2: return "EXIT_CMDERR";
    		case 3: return "EXIT_SYSERR";
    		case 4: return "EXIT_ABNORMAL";
    		default: return rc+"";
    	}
    }
        
    /*
    实例字段初始化细节问题:
    编译器在编译期间，会把实例字段的初始代码(initializer code)放入所
    有第一条语句不是“this(可选参数)”调用的构造方法(constructor)中,
    当JVM调用某一个构造方法生成此类的新实例时，实例字段的初始代码就被执行了。
    
    举例:对于如下的源代码片断
    private Option[] recognizedOptions = initializer code......
    
    public Main(String name, PrintWriter out) {
        this.ownName = name;
        this.out = out;
    }
    编译器在编译期间会重新调整成类似下面这样(只为了方便理解，实际并不完全相同):
    public Main(String name, PrintWriter out) {
    	recognizedOptions = initializer code......//总是在其他语句之前
        this.ownName = name;
        this.out = out;
    }
    更多细节参考:com.sun.tools.javac.jvm.Gen类的normalizeDefs()方法的内部实现
    */
    
    /*
    recognizedOptions字段在生成Main类的一个新实例时，
    默认被初始化为有51个元素类型为:
    com.sun.tools.javac.main.JavacOption.Option的数组，
    对应RecognizedOptions中的“static Set<OptionName> javacOptions”
    */
    private Option[] recognizedOptions = RecognizedOptions.getJavaCompilerOptions(new OptionHelper() {//OptionHelper是在RecognizedOptions内部定义的接口

        public void setOut(PrintWriter out) {
            Main.this.out = out;
        }

        public void error(String key, Object... args) {
            Main.this.error(key, args);
        }

        public void printVersion() {
        	//因为com\sun\tools\javac\resources\version.properties文件不存在
        	//所以无法取得version信息
        	//DEBUG.P("JavaCompiler.version()="+JavaCompiler.version());
        	/*
        	javac 1.7对于“-version”选项的输出:
        	javac compiler message file broken: key=compiler.misc.version.resource.missing arguments=1.6.0-beta2, {1}, {2}, {3}, {4}, {5}, {6}, {7}
            
            javac 1.6对于“-version”选项的输出: javac 1.6.0-beta2
            */
            Log.printLines(out, getLocalizedString("version", ownName,  JavaCompiler.version()));
        }

        public void printFullVersion() {
        	//因为com\sun\tools\javac\resources\version.properties文件不存在
        	//所以无法取得fullVersion信息
        	//DEBUG.P("JavaCompiler.fullVersion()="+JavaCompiler.fullVersion());
        	/*
        	javac 1.7对于“-fullversion”选项的输出:
        	javac 完整版本 "compiler message file broken: key=compiler.misc.version.resource.missing arguments=1.6.0-beta2, {1}, {2}, {3}, {4}, {5}, {6}, {7}"
            
            javac 1.6对于“-fullversion”选项的输出: javac 完整版本 "1.6.0-beta2-b86"
            */
            Log.printLines(out, getLocalizedString("fullVersion", ownName,  JavaCompiler.fullVersion()));
        }

        public void printHelp() {
            help();
        }

        public void printXhelp() {
            xhelp();
        }

        public void addFile(File f) {
            if (!filenames.contains(f))
                filenames.append(f);
        }

        public void addClassName(String s) {
            classnames.append(s);
        }

    });

    /**
     * Construct a compiler instance.
     */
    public Main(String name) {
        this(name, new PrintWriter(System.err, true));
    }

    /**
     * Construct a compiler instance.
     */
    public Main(String name, PrintWriter out) {
    	/*
    	//recognizedOptions在这里已经非null了，原因请看上面的注释
    	//DEBUG.P("recognizedOptions="+recognizedOptions);
    	从下面的输出顺序也可以看出来:
		class com.sun.tools.javac.main.RecognizedOptions===>getJavaCompilerOptions(1)
		-------------------------------------------------------------------------
		class com.sun.tools.javac.main.RecognizedOptions===>getOptions(2)
		-------------------------------------------------------------------------
		class com.sun.tools.javac.main.RecognizedOptions===>getOptions(2)  END
		-------------------------------------------------------------------------
		class com.sun.tools.javac.main.RecognizedOptions===>getJavaCompilerOptions(1)  END
		-------------------------------------------------------------------------
		com.sun.tools.javac.main.Main===>Main(2)
		-------------------------------------------------------------------------
		this.ownName=javac
		com.sun.tools.javac.main.Main===>Main(2)  END
		-------------------------------------------------------------------------
    	*/
    	
    	DEBUG.P(this,"Main(2)");

        this.ownName = name;
        this.out = out;
        
        DEBUG.P("this.ownName="+this.ownName);
        DEBUG.P(0,this,"Main(2)");
    }
    /** A table of all options that's passed to the JavaCompiler constructor.  */
    private Options options = null;

    /** The list of source files to process
     */
    public ListBuffer<File> filenames = null; // XXX sb protected

    /** List of class files names passed on the command line
     */
    public ListBuffer<String> classnames = null; // XXX sb protected

    /** Print a string that explains usage.
     */
    void help() {
    	/*例子:
    	在com\sun\tools\javac\resources\javac.properties文件中有如下内容:
    	-----------------------------------------------------
    	javac.msg.usage.header=\
		Usage: {0} <options> <source files>\n\
		where possible options include:
		-----------------------------------------------------
		在getLocalizedString()方法内部先在第一个参数"msg.usage.header"前
		加上“javac.”行成一个Key=“javac.msg.usage.header"，然后根据Key
		查找对应的内容，然后再用参数"ownName"的值替换上面的“{0}”,
		Log.printLines()方法按"\n"截取一行并打印输出,最后结果如下:
		-----------------------------------------------------
		Usage: javac <options> <source files>
		where possible options include:
		-----------------------------------------------------
		*/
        Log.printLines(out, getLocalizedString("msg.usage.header", ownName));
        //按照与上面类似的方法打印每个一选项的格式信息
        for (int i=0; i<recognizedOptions.length; i++) {
            recognizedOptions[i].help(out);
        }
        out.println();
    }

    /** Print a string that explains usage for X options.
     */
    void xhelp() {
        for (int i=0; i<recognizedOptions.length; i++) {
            recognizedOptions[i].xhelp(out);
        }
        out.println();
        Log.printLines(out, getLocalizedString("msg.usage.nonstandard.footer"));
    }

    /** Report a usage error.
     */
    void error(String key, Object... args) {
        if (fatalErrors) {
            String msg = getLocalizedString(key, args);
            //类全限定名称:com.sun.tools.javac.util.PropagatedException
            throw new PropagatedException(new IllegalStateException(msg));
        }
        warning(key, args);
        Log.printLines(out, getLocalizedString("msg.usage", ownName));
    }

    /** Report a warning.
     */
    void warning(String key, Object... args) {
        Log.printLines(out, ownName + ": "
                       + getLocalizedString(key, args));
    }

    public Option getOption(String flag) {
        for (Option option : recognizedOptions) {
            if (option.matches(flag))
                return option;
        }
        return null;
    }

    public void setOptions(Options options) {
        if (options == null)
            throw new NullPointerException();
        this.options = options;
    }
    
    //我加上的，调试用途
    public Options getOptions() {
        return options;
    }
    
    //在com.sun.tools.javac.api.JavacTaskImpl类中使用到这个方法
    public void setFatalErrors(boolean fatalErrors) {
        this.fatalErrors = fatalErrors;
    }

    /** Process command line arguments: store all command line options
     *  in `options' table and return all source filenames.
     *  @param flags    The array of command line arguments.
     */
    public List<File> processArgs(String[] flags) { // XXX sb protected
    //String[] flags的值已由CommandLine.parse(args)处理过,args是命令行参数
    try {//我加上的
    	DEBUG.P(this,"processArgs(1)");
        DEBUG.P("recognizedOptions.length="+recognizedOptions.length);
        DEBUG.P("recognizedOptions["+(recognizedOptions.length-1)+"]="+recognizedOptions[recognizedOptions.length-1]);
    	DEBUG.P("options前="+options);
		//DEBUG.P("Options options.size()="+options.size());
        //DEBUG.P("Options options.keySet()="+options.keySet());

        int ac = 0;
        while (ac < flags.length) {
        	DEBUG.P("flags["+ac+"]="+flags[ac]);
        	
            String flag = flags[ac];
            ac++;

            int j;
            // quick hack to speed up file processing: 
            // if the option does not begin with '-', there is no need to check
            // most of the compiler options.
            /*
            下面的程序代码技巧性很强，
            因为javac命令行的选项名称都是以'-'字符开头的,recognizedOptions数组中存放的
            选项除了最后一个是HiddenOption(SOURCEFILE)不以'-'字符开头外，其它所有选项
            名称都是以'-'字符开头的。如果在javac命令行中出现不是以'-'字符开头的选项，则
            查找位置firstOptionToCheck从recognizedOptions数组最末尾开始,
            (也就是直接与recognizedOptions数组的最后一个选项比较)
            它要么是要编译的源文件，要么是错误的选项。
            
            当出现在javac命令行中的选项是以'-'字符开头时，
            查找位置firstOptionToCheck从recognizedOptions数组第一个元素开始，直到
            搜索完整个recognizedOptions数组(j == recognizedOptions.length)时，才能
            确定是错误的选项。
            */
            
            //如果flag.length()的长度为0时会出现异常
			//见com.sun.tools.javac.main.CommandLine类中的注释
            int firstOptionToCheck = flag.charAt(0) == '-' ? 0 : recognizedOptions.length-1;
            for (j=firstOptionToCheck; j<recognizedOptions.length; j++)
                if (recognizedOptions[j].matches(flag)) break;

            if (j == recognizedOptions.length) {
                error("err.invalid.flag", flag);
                return null;
            }
            

            Option option = recognizedOptions[j];
            
            DEBUG.P("option.hasArg()="+option.hasArg());
            //参看JavacOption.hasArg()中的注释
            //另外，一个选项最多只带一个参数
            if (option.hasArg()) {
                if (ac == flags.length) {
                	/*错误例子:
                	F:\Javac>javac -d
					javac: -d 需要参数
					用法: javac <options> <source files>
					-help 用于列出可能的选项
					*/
                    error("err.req.arg", flag);
                    return null;
                }
                String operand = flags[ac];
                ac++;
                
                //大多数process()内部都是把flag与operand构成一<K,V>对，
                //存入options中,options可以看成是一个Map<K,V>
                //细节请看com.sun.tools.javac.main.RecognizedOptions类的getAll()方法
                if (option.process(options, flag, operand))
                    return null;
            } else {
            	//大多数process()内部都是把flag与flag构成一<K,V>对，
                //存入options中,options可以看成是一个Map<K,V>
                //细节请看com.sun.tools.javac.main.RecognizedOptions类的getAll()方法
                if (option.process(options, flag))
                    return null;
            }
        }
        
        //当在javac命令行中指定了“-d <目录>”选项时，
        //检查<目录>是否存在，不存在或不是目录则提示错误并返回
        if (!checkDirectory("-d"))
            return null;
        //当在javac命令行中指定了“-s <目录>”选项时，
        //检查<目录>是否存在，不存在或不是目录则提示错误并返回
        if (!checkDirectory("-s"))
            return null;
            
        //如果命令行中没带-source与-target选项，则采用默认值
        String sourceString = options.get("-source");
        Source source = (sourceString != null)
        //在这里lookup()一定不会返回null,因为在上面
        //的option.process(options, flag, operand)时如果有错已经检测出来
            ? Source.lookup(sourceString)
            : Source.DEFAULT;
        String targetString = options.get("-target");
        //在这里lookup()一定不会返回null,因为在上面
        //的option.process(options, flag, operand)时如果有错已经检测出来
        Target target = (targetString != null)
            ? Target.lookup(targetString)
            : Target.DEFAULT;
        // We don't check source/target consistency for CLDC, as J2ME
        // profiles are not aligned with J2SE targets; moreover, a
        // single CLDC target may have many profiles.  In addition,
        // this is needed for the continued functioning of the JSR14
        // prototype.
        
        DEBUG.P("sourceString="+sourceString);
		DEBUG.P("source="+source);
		DEBUG.P("source.requiredTarget()="+source.requiredTarget());
		DEBUG.P("targetString="+targetString);
		DEBUG.P("target="+target);
        //如果是"-target jsr14"，则不用执行下面的代码
		//target的版本号总是围绕source的版本号而变动的
        if (Character.isDigit(target.name.charAt(0))) {
        	//当target的版本号<source的版本号
            if (target.compareTo(source.requiredTarget()) < 0) {
                if (targetString != null) {
                    if (sourceString == null) {//指定-target，没指定-source的情况
                    	/*错误例子:
                    	F:\Javac>javac -target 1.4
						javac: 目标版本 1.4 与默认的源版本 1.5 冲突
						*/
                        warning("warn.target.default.source.conflict",
                                targetString,
                                source.requiredTarget().name);
                    } else {//指定-target，同时指定-source的情况
                    	/*错误例子:
                    	F:\Javac>javac -target 1.4 -source 1.5
						javac: 源版本 1.5 需要目标版本 1.5
						*/
                        warning("warn.source.target.conflict",
                                sourceString,
                                source.requiredTarget().name);
                    }
                    return null;
                } else {
                	//没有指定-target时，target取默认版本号(javac1.7默认是1.6)
                	//如果默认版本号还比source低，则target版本号由source决定
                    options.put("-target", source.requiredTarget().name);
                }
            } else {
            	//当target的版本号>=source的版本号且用户没在
            	//javac命令行中指定“-target”选项，且不允许使用
            	//泛型时，target版本默认为1.4
                if (targetString == null && !source.allowGenerics()) {
                    options.put("-target", Target.JDK1_4.name);
                }
            }
        }
        return filenames.toList();
        
    }finally{//我加上的
    DEBUG.P("");
	DEBUG.P("source="+options.get("-source"));
	DEBUG.P("target="+options.get("-target"));

	DEBUG.P("");
    DEBUG.P("ListBuffer<File> filenames.size()="+filenames.size());
    DEBUG.P("ListBuffer<String> classnames.size()="+classnames.size());
    //DEBUG.P("Options options.size()="+options.size());
    //DEBUG.P("Options options.keySet()="+options.keySet());
    
    DEBUG.P("options后="+options);
	DEBUG.P(0,this,"processArgs(1)");
	}
	
    }
    // where
        private boolean checkDirectory(String optName) {
			try {//我加上的
			DEBUG.P(this,"checkDirectory(1)");
			DEBUG.P("optName="+optName);

            String value = options.get(optName);
			DEBUG.P("value="+value);
            if (value == null)
                return true;

            File file = new File(value);

			DEBUG.P("file.exists()="+file.exists());
            if (!file.exists()) {
				//javac -d bin\directory_not_found_test
				//如果指定的目录不存在，提示以下错误:
				//javac: directory not found: bin\directory_not_found_test
				//用法: javac <options> <source files>
				//-help 用于列出可能的选项
				//注:com\sun\tools\javac\resources\javac_zh_CN.properties文件
				//没有定义"err.dir.not.found"，所以出现的提示是英文的，
				//这是从com\sun\tools\javac\resources\javac.properties文件提取的信息
                error("err.dir.not.found", value);
                return false;
            }

			DEBUG.P("file.isDirectory()="+file.isDirectory());
            if (!file.isDirectory()) {
				//javac -d args.txt
				//如果指定的是一个存在的文件，提示以下错误:
				//javac: 不是目录: args.txt
				//用法: javac <options> <source files>
				//-help 用于列出可能的选项
                error("err.file.not.directory", value);
                return false;
            }
            return true;

			}finally{//我加上的
			DEBUG.P(0,this,"checkDirectory(1)");
			}
        }

    /** Programmatic interface for main function.
     * @param args    The command line parameters.
     */
    public int compile(String[] args) {
    	DEBUG.P(this,"compile(1)");
    	
        Context context = new Context();
        JavacFileManager.preRegister(context); // can't create it until Log has been set up
        int result = compile(args, context);
        if (fileManager instanceof JavacFileManager) {
            // A fresh context was created above, so jfm must be a JavacFileManager
            ((JavacFileManager)fileManager).close();
        }
        
        DEBUG.P(0,this,"compile(1)");
        return result;
    }

    public int compile(String[] args, Context context) {
    	try {//我加上的
		DEBUG.P(this,"compile(2)");
		
		//类全限定名称:com.sun.tools.javac.util.List
		//类全限定名称:javax.tools.JavaFileObject
    	//List.<JavaFileObject>nil()表示分配一个其元素为JavaFileObject类
    	//型的空List(不是null，而是指size=0)
        return compile(args, context, List.<JavaFileObject>nil(), null);
        
        }finally{//我加上的
		DEBUG.P(0,this,"compile(2)");
		}
    }

    /** Programmatic interface for main function.
     * @param args    The command line parameters.
     */
    public int compile(String[] args,
                       Context context,
                       List<JavaFileObject> fileObjects,
                       Iterable<? extends Processor> processors)
    {
    	try {//我加上的
    	DEBUG.P(this,"compile(4)");
    	DEBUG.P("options="+options);
	DEBUG.P("context="+context);
    	
        if (options == null)
            options = Options.instance(context); // creates a new one
            
        //这两个实例字段的值在调用processArgs()方法时，
        //都是通过RecognizedOptions.HiddenOption(SOURCEFILE)的process()得到的.
        filenames = new ListBuffer<File>();//这个是实例安段
        classnames = new ListBuffer<String>();
        
        //类全限定名称:com.sun.tools.javac.main.JavaCompiler
        JavaCompiler comp = null;
        /*
         * TODO: Logic below about what is an acceptable command line
         * should be updated to take annotation processing semantics
         * into account.
         */
        try {
        	//在javac命令后没有任何选项参数时显示帮助信息
            if (args.length == 0 && fileObjects.isEmpty()) {
                help();
                return EXIT_CMDERR;
            }
            
            //这个是本地变量，注意上面还有个同名的实例字段
            //在调用processArgs方法时如果没有选项错误或选项参数错误时，
            //要编译的所有源文件名会被加到实例字段ListBuffer<File> filenames中
            //这个本地变量的值实际上是等于ListBuffer<File> filenames.toList()
            List<File> filenames;
            try {
                filenames = processArgs(CommandLine.parse(args));
                //有选项错误或选项参数错误时processArgs()的返回值都为null
                if (filenames == null) {
                    // null signals an error in options, abort
                    return EXIT_CMDERR;
                } else if (filenames.isEmpty() && fileObjects.isEmpty() && classnames.isEmpty()) {
                    // it is allowed to compile nothing if just asking for help or version info
                    if (options.get("-help") != null
                        || options.get("-X") != null
                        || options.get("-version") != null
                        || options.get("-fullversion") != null)
                        return EXIT_OK;
                    error("err.no.source.files");
                    return EXIT_CMDERR;
                }
            } catch (java.io.FileNotFoundException e) {
            	DEBUG.P("java.io.FileNotFoundException");
            	//这里的异常不知从哪里抛出,
            	//在RecognizedOptions的new HiddenOption(SOURCEFILE)
            	//的process()中有helper.error("err.file.not.found", f);
            	//如果源文件(.java)不存在的话，在那里都有错误提示了
            	//但即使文件不存在，也不抛出FileNotFoundException异常
            	
            	//2007-06-01中午已解决这个问题:
	            //javac命令行中可以处理windows平台上的批处理
	            //文件里的参数，如：javac @myfile.bat
	            //如果myfile.bat文件找不到会提示错误信息如:
	            //“javac: 找不到文件： myfile.bat (系统找不到指定的文件。)”
	            //同时还会抛出FileNotFoundException异常，
	            //之后退出CommandLine.parse方法，processArgs方法也不再执行
	            //异常在这里被捕获
                Log.printLines(out, ownName + ": " +
                               getLocalizedString("err.file.not.found",
                                                  e.getMessage()));
                return EXIT_SYSERR;
            }
            
            //不知道"-Xstdout"与这里的"stdout"有什么区别
            //而且命令行中并不能使用"stdout"
            //(可能在程序内部加入options的,但搜索了所有源代码，也没找到在哪里加入)
            //2007-05-31晚上已解决这个问题:
            //可以通过“-XDstdout=...(根据实际情况填写)”选项设置
            //见RecognizedOptions类的“new HiddenOption(XD)”那一段代码
            boolean forceStdOut = options.get("stdout") != null;
            DEBUG.P("forceStdOut="+forceStdOut);
            
            //当javac命令行中带有任何以“-XDstdout”开头的选项时，
            //再结合下面的“context.put(Log.outKey, out)”语句，
            //就可以把任何错误、警告信息重定向到标准输出
            if (forceStdOut) {
                out.flush();
                out = new PrintWriter(System.out, true);
            }
            
            DEBUG.P("生成一个JavacFileManager类的新实例...开始");
            
            //下面两条语句不能调换先后次序，否则出错,
            //详情参考JavacFileManager.preRegister()中的注释
            context.put(Log.outKey, out);
            fileManager = context.get(JavaFileManager.class);
            
            DEBUG.P("生成一个JavacFileManager类的新实例...结束");
            
            
            DEBUG.P(3);
            DEBUG.P("生成一个JavaCompiler类的新实例...开始");
            //在得到JavaCompiler的实例的过程里，进行了很多初始化工作
            comp = JavaCompiler.instance(context);
            DEBUG.P("生成一个JavaCompiler类的新实例...结束");
            DEBUG.P(3);
            if (comp == null) return EXIT_SYSERR;
            
            //“filenames”指的是本地局部变量“List<File> filenames”
            if (!filenames.isEmpty()) {
                // add filenames to fileObjects
                comp = JavaCompiler.instance(context);
                List<JavaFileObject> otherFiles = List.nil();
                JavacFileManager dfm = (JavacFileManager)fileManager;
                //在JavacFileManager.getJavaFileObjectsFromFiles()方法里，把
                //每一个要编译的源文件都“包装”成一个RegularFileObject实例。
                //RegularFileObject类是JavacFileManager的内部类，同时实现了
                //JavaFileObject接口，通过调用getCharContent()方法返回一个
                //java.nio.CharBuffer实例的引用就可以对源文件内容进行解析了。
                //在com.sun.tools.javac.main.JavaCompiler类的readSource()方
                //法中有这样的应用
                
                
                //this.filenames的类型是ListBuffer，可以像下面这样改变长度
                //this.filenames.prepend(null);
                //this.filenames.prepend(new File("args.txt"));
                
                //注意这种方式并不能改变filenames的长度
                //(因为本地局部变量 filenames 是List类型的)
                //--------------------------------
                //filenames.append(null);
                //filenames.prepend(new File("args.txt"));
                //--------------------------------
                
                //必需像这样，把filenames指向新的链头
                //--------------------------------
                //filenames=filenames.append(null);
                //filenames=filenames.prepend(new File("args.txt"));
                //filenames=filenames.prepend(new File("src"));
                //DEBUG.P("filenames.size()="+this.filenames.size());
                //filenames=this.filenames.toList();
                //DEBUG.P("filenames.size()="+filenames.size());


                for (JavaFileObject fo : dfm.getJavaFileObjectsFromFiles(filenames))
                //getJavaFileObjectsFromFiles方法的参数是“Iterable<? extends File>”类型的，
                //filenames是com.sun.tools.javac.util.List<File>类型，
                //com.sun.tools.javac.util.List<T>类实现了java.lang.Iterable<T>接口
                    otherFiles = otherFiles.prepend(fo);
                for (JavaFileObject fo : otherFiles)
                    fileObjects = fileObjects.prepend(fo);
            }
            comp.compile(fileObjects,
                         classnames.toList(),
                         processors);

            if (comp.errorCount() != 0 ||
                options.get("-Werror") != null && comp.warningCount() != 0)
                return EXIT_ERROR;
        } catch (IOException ex) {
            ioMessage(ex);
            return EXIT_SYSERR;
        } catch (OutOfMemoryError ex) {
            resourceMessage(ex);
            return EXIT_SYSERR;
        } catch (StackOverflowError ex) {
            resourceMessage(ex);
            return EXIT_SYSERR;
        } catch (FatalError ex) {
            feMessage(ex);
            return EXIT_SYSERR;
        } catch(AnnotationProcessingError ex) {
            apMessage(ex);
            return EXIT_SYSERR;
        } catch (ClientCodeException ex) {
            // as specified by javax.tools.JavaCompiler#getTask
            // and javax.tools.JavaCompiler.CompilationTask#call
            throw new RuntimeException(ex.getCause());
        } catch (PropagatedException ex) {
            throw ex.getCause();
        } catch (Throwable ex) {
            // Nasty.  If we've already reported an error, compensate
            // for buggy compiler error recovery by swallowing thrown
            // exceptions.
            if (comp == null || comp.errorCount() == 0 ||
                options == null || options.get("dev") != null)
                bugMessage(ex);
            return EXIT_ABNORMAL;
        } finally {
            if (comp != null) comp.close();
            filenames = null;
            options = null;
        }
        return EXIT_OK;
        
        }finally{//我加上的
		DEBUG.P(0,this,"compile(4)");
		}
    }

    /** Print a message reporting an internal error.
     */
    void bugMessage(Throwable ex) {
        Log.printLines(out, getLocalizedString("msg.bug",
                                               JavaCompiler.version()));
        ex.printStackTrace(out);
    }

    /** Print a message reporting an fatal error.
     */
    void feMessage(Throwable ex) {
        Log.printLines(out, ex.getMessage());
    }

    /** Print a message reporting an input/output error.
     */
    void ioMessage(Throwable ex) {
        Log.printLines(out, getLocalizedString("msg.io"));
        ex.printStackTrace(out);
    }

    /** Print a message reporting an out-of-resources error.
     */
    void resourceMessage(Throwable ex) {
        Log.printLines(out, getLocalizedString("msg.resource"));
//      System.out.println("(name buffer len = " + Name.names.length + " " + Name.nc);//DEBUG
        ex.printStackTrace(out);
    }

    /** Print a message reporting an uncaught exception from an
     * annotation processor.
     */
    void apMessage(AnnotationProcessingError ex) {
        Log.printLines(out,
                       getLocalizedString("msg.proc.annotation.uncaught.exception"));
        ex.getCause().printStackTrace();
    }
    
    //类全限定名称:javax.tools.JavaFileManager
    private JavaFileManager fileManager;

    /* ************************************************************************
     * Internationalization
     *************************************************************************/

    /** Find a localized string in the resource bundle.
     *  @param key     The key for the localized string.
     */
    public static String getLocalizedString(String key, Object... args) { // FIXME sb private
        try {
            if (messages == null)
                messages = new Messages(javacBundleName);
            return messages.getLocalizedString("javac." + key, args);
        }
        catch (MissingResourceException e) {
            throw new Error("Fatal Error: Resource for javac is missing", e);
        }
    }
    
    //这个方法没有被使用
    public static void useRawMessages(boolean enable) {
        if (enable) {
            messages = new Messages(javacBundleName) {
                    public String getLocalizedString(String key, Object... args) {
                        return key;
                    }
                };
        } else {
            messages = new Messages(javacBundleName);
        }
    }
    
    //资源绑定名称的字符串通常精确到文件名，而且文件名之前
    //的限定名称(如下面的"com.sun.tools.javac.resources")还
    //必须紧跟在类路径的某一目录下
    private static final String javacBundleName =
        "com.sun.tools.javac.resources.javac";
        
    //类全限定名称:com.sun.tools.javac.util.Messages
    private static Messages messages;
}
