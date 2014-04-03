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

            List<File> filenames;//这个是本地变量，注意上面还有个同名的实例安段
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
                for (JavaFileObject fo : dfm.getJavaFileObjectsFromFiles(filenames))
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