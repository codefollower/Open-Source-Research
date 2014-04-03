    /** Parse contents of file.
     *  @param filename     The name of the file to be parsed.
     */
    public JCTree.JCCompilationUnit parse(JavaFileObject filename) {
    	DEBUG.P(this,"parse(1)");
    	
    	//将log内部的引用文件切换到当前待处理的文件filename
        JavaFileObject prev = log.useSource(filename);
        try {
            JCTree.JCCompilationUnit t = parse(filename, readSource(filename));
            if (t.endPositions != null)
                log.setEndPosTable(filename, t.endPositions);
            return t;
        } finally {
            log.useSource(prev);//将log内部的引用文件切换到原来的文件
            DEBUG.P(0,this,"parse(1)");
        }
    }

    /** Try to open input stream with given name.
     *  Report an error if this fails.
     *  @param filename   The file name of the input stream to be opened.
     */
    //类全限定名称:java.lang.CharSequence
    public CharSequence readSource(JavaFileObject filename) {
    	try {//我加上的
    	DEBUG.P(this,"readSource(1)");
    	
        try {
            inputFiles.add(filename);
            //在这里实际已开始读取源文件的内容了
            //参考com.sun.tools.javac.main.Main类compile()方法中的注释
            return filename.getCharContent(false);
        } catch (IOException e) {
            log.error("error.reading.file", filename, e.getLocalizedMessage());
            return null;
        }
        
        }finally{//我加上的
		DEBUG.P(0,this,"readSource(1)");
		}
    }