    //用户级别的类路径搜索顺序如下(前一级不存在才往下搜索)：
    //javac -classpath==>OS环境变量CLASSPATH==>application.home(这个不知道在哪设?)==>
    //java -classpath ==>当前目录(.)
    //另外类路径里的jar或zip文件需要展开
    //此方法一定不会返回null
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