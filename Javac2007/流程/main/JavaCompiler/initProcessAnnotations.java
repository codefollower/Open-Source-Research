    /**
     * Set to true to enable skeleton annotation processing code.
     * Currently, we assume this variable will be replaced more
     * advanced logic to figure out if annotation processing is
     * needed.
     */
    boolean processAnnotations = false;

    /**
     * Object to handle annotation processing.
     */
    JavacProcessingEnvironment procEnvImpl = null;


	/**
     * Check if we should process annotations.
     * If so, and if no scanner is yet registered, then set up the DocCommentScanner
     * to catch doc comments, and set keepComments so the parser records them in
     * the compilation unit.
     *
     * @param processors user provided annotation processors to bypass
     * discovery, {@code null} means that no processors were provided
     */
    public void initProcessAnnotations(Iterable<? extends Processor> processors) {
    	DEBUG.P(this,"initProcessAnnotations(1)");
        // Process annotations if processing is not disabled and there
        // is at least one Processor available.
        Options options = Options.instance(context);
        DEBUG.P("options.get(\"-proc:none\")="+options.get("-proc:none"));
        DEBUG.P("JavacProcessingEnvironment procEnvImpl="+procEnvImpl);
        if (options.get("-proc:none") != null) {
            processAnnotations = false;
        } else if (procEnvImpl == null) {
        	/*
        	当在javac命令行中加了"-proc:none"选项时，
        	就表示不执行注释处理和/或编译，processAnnotations为false。
        	
        	
        	不加"-proc:none"选项时，会生成一个JavacProcessingEnvironment类
        	的实例，在生成实例的过程中，查看是否在命令行中指定了如下选项：
        	
        	-processor <class1>[,<class2>,<class3>...]要运行的注释处理程序的名称；绕过默认的搜索进程
        	-processorpath <路径>        指定查找注释处理程序的位置
        	
        	如果选项“-processor”没指定，就采用默认的注释处理程序
        	(注:默认的注释处理程序由sun.misc.Service类提供，
        	    sun.misc.Service类并不包含在javac1.7的源代码中，
        	    而是在rt.jar文件中，并没有开源)
        	    
        	如果选项“-processorpath”没指定，就以-classpath为准。
        	然后在上面指定的路径中搜索注释处理程序的名称，找到至少
        	一个注释处理程序的话就设processAnnotations为ture，否则
        	设processAnnotations为false。以后当调用processAnnotations()方法
        	时会根据processAnnotations的取值决定是否对源代码中的所有注释
        	进行处理和/或编译。
        	
        	最后还得注意一个细节:
        	在生成一个JavacProcessingEnvironment类的实例时，已间接的调
        	用了com.sun.tools.javac.util.Paths类的lazy()方法，在此方法
        	中会把PLATFORM_CLASS_PATH,CLASS_PATH,SOURCE_PATH这三种路径
        	的值计算出来。
        	*/
            procEnvImpl = new JavacProcessingEnvironment(context, processors);
            processAnnotations = procEnvImpl.atLeastOneProcessor();
            
            DEBUG.P("processAnnotations="+processAnnotations);
            if (processAnnotations) {
                if (context.get(Scanner.Factory.scannerFactoryKey) == null)
                    DocCommentScanner.Factory.preRegister(context);
                    
                options.put("save-parameter-names", "save-parameter-names");
                reader.saveParameterNames = true;
				keepComments = true;
				
				if (taskListener != null)
				    taskListener.started(new TaskEvent(TaskEvent.Kind.ANNOTATION_PROCESSING));
            } else { // free resources
                procEnvImpl.close();
	    	}
        }
        DEBUG.P(0,this,"initProcessAnnotations(1)");
    }