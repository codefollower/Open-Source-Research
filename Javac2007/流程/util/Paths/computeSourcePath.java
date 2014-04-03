    //此方法有可能返回null
    private Path computeSourcePath() {
		//-sourcepath <路径>           指定查找输入源文件的位置
		DEBUG.P(SOURCEPATH+"="+options.get(SOURCEPATH));
		
		String sourcePathArg = options.get(SOURCEPATH);
		if (sourcePathArg == null)
			return null;

		return new Path().addFiles(sourcePathArg);
    }