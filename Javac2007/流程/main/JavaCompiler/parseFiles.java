    /**
     * Parses a list of files.
     */
   public List<JCCompilationUnit> parseFiles(List<JavaFileObject> fileObjects) throws IOException {
       try {//我加上的
       DEBUG.P(this,"parseFiles(1) (语法分析......)");
       
       if (errorCount() > 0)
       	   return List.nil();

        //parse all files
        ListBuffer<JCCompilationUnit> trees = lb();
        //lb()生成一个元素类型为JCCompilationUnit的空ListBuffer
        //在com.sun.tools.javac.util.ListBuffer类中定义;
        for (JavaFileObject fileObject : fileObjects)
            trees.append(parse(fileObject));
        return trees.toList();
        
        }finally{//我加上的
        DEBUG.P(2,this,"parseFiles(1)");
    	}
    }