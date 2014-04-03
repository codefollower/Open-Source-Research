    /** Parse contents of input stream.
     *  @param filename     The name of the file from which input stream comes.
     *  @param input        The input stream to be parsed.
     */
    protected JCCompilationUnit parse(JavaFileObject filename, CharSequence content) {
        DEBUG.P(this,"parse(2)");
        
        long msec = now();
        
        //生成一棵空JCCompilationUnit树，
        //JCCompilationUnit是最顶层的抽象语法树(abstract syntax tree)
        //参考com.sun.tools.javac.tree.JCTree类与com.sun.tools.javac.tree.TreeMaker类
        JCCompilationUnit tree = make.TopLevel(List.<JCTree.JCAnnotation>nil(),
                                      null, List.<JCTree>nil());
        if (content != null) {
            if (verbose) {
                printVerbose("parsing.started", filename);
            }
            
            //taskListener在这为空,因为这个版本的Javac还没有任
            //何类实现com.sun.source.util.TaskListener接口
        	DEBUG.P("taskListener="+taskListener);
            if (taskListener != null) {
                TaskEvent e = new TaskEvent(TaskEvent.Kind.PARSE, filename);
                taskListener.started(e);
            }
            
	    	int initialErrorCount = log.nerrors;
	    	
	    	//建立一个词法分析类Scanner的实例,并指向第一个字符
            Scanner scanner = getScannerFactory().newScanner(content);
            
            //建立一个语法分析类Parser的实例,并指向第一个token
            Parser parser = parserFactory.newParser(scanner, keepComments(), genEndPos);
            
            //java语言的语法符合LL(1)文法,所以采用的是递归下降分析算法,
            //对于二元运算表达式采用运算符优先级算法
            //Parser通过nextToken()来驱动Scanner
            tree = parser.compilationUnit();
            
	    	parseErrors |= (log.nerrors > initialErrorCount);
            if (lineDebugInfo) {
                tree.lineMap = scanner.getLineMap();
            }
            if (verbose) {
                printVerbose("parsing.done", Long.toString(elapsed(msec)));
            }
        }

        tree.sourcefile = filename;

        if (content != null && taskListener != null) {
            TaskEvent e = new TaskEvent(TaskEvent.Kind.PARSE, tree);
            taskListener.finished(e);
        }
        
        DEBUG.P(0,this,"parse(2)");
        return tree;
    }