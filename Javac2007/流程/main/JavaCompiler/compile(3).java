    /**
     * Main method: compile a list of files, return all compiled classes
     *
     * @param sourceFileObjects file objects to be compiled
     * @param classnames class names to process for annotations
     * @param processors user provided annotation processors to bypass
     * discovery, {@code null} means that no processors were provided
     */
    public void compile(List<JavaFileObject> sourceFileObjects,
                        List<String> classnames,
			Iterable<? extends Processor> processors)
        throws IOException // TODO: temp, from JavacProcessingEnvironment
    {
    	try {//我加上的
    	DEBUG.P(3);DEBUG.P(this,"compile(3) 一系列编译任务的起点......");
    	DEBUG.P("sourceFileObjects="+sourceFileObjects);
    	DEBUG.P("classnames="+classnames);
    	DEBUG.P("processors="+processors);
    	
    	//通过com.sun.tools.javac.api.JavacTaskImpl类的call()方法
    	//调用com.sun.tools.javac.main.Main类compile(4)方法间接
    	//调用到这里时，processors不为null；
    	//如果通过com.sun.tools.javac.main.Main类的compile(2)方法
    	//调用com.sun.tools.javac.main.Main类compile(4)方法间接
    	//调用到这里时，processors为null；
    	
        if (processors != null && processors.iterator().hasNext())
            explicitAnnotationProcessingRequested = true;
        // as a JavaCompiler can only be used once, throw an exception if
        // it has been used before.
        if (hasBeenUsed)
	    throw new AssertionError("attempt to reuse JavaCompiler");
        hasBeenUsed = true;

        start_msec = now();//记录开始编译时间
        try {
            initProcessAnnotations(processors);

            // These method calls must be chained to avoid memory leaks
            delegateCompiler = processAnnotations(enterTrees(stopIfError(parseFiles(sourceFileObjects))),
                                                  classnames);
            /*运行完上面后，已完成的编译任务有:
            1.词法分析(Scanner)
            2.语法分析(Parser)
            3.Enter与MemberEnter
            4.注释处理(JavacProcessingEnvironment)
            */

            delegateCompiler.compile2();
            /*运行完compile2()后，已完成的编译任务有:
            1.属性分析(Attr)
            2.数据流分析(Flow)
            3.Desugar
            4.生成字节码(Gen,ClassWriter)
            */
            
            /*
            上面所述内容只是对编译任务的一个粗略划分,具体细节还得
            分析到每一阶段时才能明了，另外对于错误处理是无处不在的，
            每一阶段都有特定的错误要查找。
            
            核心的内部数据结构在下面几个类中定义:
            com.sun.tools.javac.util.Name
            com.sun.tools.javac.tree.JCTree
            com.sun.tools.javac.code.Symbol
            com.sun.tools.javac.code.Type
            com.sun.tools.javac.code.Scope
            com.sun.tools.javac.jvm.Items
            com.sun.tools.javac.jvm.Code
            */
	    delegateCompiler.close();
	    elapsed_msec = delegateCompiler.elapsed_msec;
        } catch (Abort ex) { //类全限定名称:com.sun.tools.javac.util.Abort
            if (devVerbose)
                ex.printStackTrace();
        } 
        
        }finally{//我加上的
        DEBUG.P(0,this,"compile(3)");
    	}
    }