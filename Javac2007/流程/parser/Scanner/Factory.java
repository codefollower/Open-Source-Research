    /** A factory for creating scanners. */
    public static class Factory {
	/** The context key for the scanner factory. */
	public static final Context.Key<Scanner.Factory> scannerFactoryKey =
	    new Context.Key<Scanner.Factory>();

	/** Get the Factory instance for this context. */
	public static Factory instance(Context context) {
	    Factory instance = context.get(scannerFactoryKey);
	    if (instance == null)
		instance = new Factory(context);
	    return instance;
	}

	final Log log;
	final Name.Table names;
	final Source source;
	final Keywords keywords;

	/** Create a new scanner factory. */
	protected Factory(Context context) {
		DEBUG.P(this,"Factory(1)");
	    context.put(scannerFactoryKey, this);
	    this.log = Log.instance(context);
	    this.names = Name.Table.instance(context);
	    this.source = Source.instance(context);
	    this.keywords = Keywords.instance(context);
	    DEBUG.P(0,this,"Factory(1)");
	}

        public Scanner newScanner(CharSequence input) {
        	try {//我加上的
        	DEBUG.P(this,"newScanner(1)");
        	//DEBUG.P("input instanceof CharBuffer="+(input instanceof CharBuffer));
        	/*
        	为什么要(input instanceof CharBuffer)呢？
        	因为每个要编译的源文件都被“包装”成一
        	个JavacFileManager.RegularFileObject类的实例 ,
        	RegularFileObject类实现了JavaFileObject接口,JavaFileObject接口的
        	超级接口是FileObject，在FileObject接口中有一个方法(用于读取文件内容):
        	java.lang.CharSequence getCharContent(boolean ignoreEncodingErrors)
                                      throws java.io.IOException
                                      
            而JavacFileManager.RegularFileObject类对应的实现方法为:
            public java.nio.CharBuffer getCharContent(boolean ignoreEncodingErrors)
                                   throws java.io.IOException
                                   
            比较两个方法的返回值，初看可能觉得有点怪，其实这是合法的，
            因为java.nio.CharBuffer类实现了java.lang.CharSequence接口                   
        	*/
            if (input instanceof CharBuffer) {
                return new Scanner(this, (CharBuffer)input);
            } else {
                char[] array = input.toString().toCharArray();
                return newScanner(array, array.length);
            }
            
            }finally{//我加上的
			DEBUG.P(0,this,"newScanner(1)");
			}
        }

        public Scanner newScanner(char[] input, int inputLength) {
            return new Scanner(this, input, inputLength);
        }
    }