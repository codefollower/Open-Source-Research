    //此方法有可能返回null
    private Path computeAnnotationProcessorPath() {
        try {
        //-processorpath <路径>        指定查找注释处理程序的位置
        DEBUG.P(this,"computeAnnotationProcessorPath()");
        DEBUG.P(PROCESSORPATH+"="+options.get(PROCESSORPATH));
    
		String processorPathArg = options.get(PROCESSORPATH);
		if (processorPathArg == null)
			return null;

		return new Path().addFiles(processorPathArg);
		
		}finally{
		DEBUG.P(0,this,"computeAnnotationProcessorPath()");
		}
    }