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