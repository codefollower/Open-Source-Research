    //实现了Iterable<String>接口的类可用在有foreach语句的地方(JDK>=1.5才能用)
    private static class PathIterator implements Iterable<String> {
		private int pos = 0;
		private final String path;
		private final String emptyPathDefault;
	
		//按分号";"(windows)或冒号":"(unix/linux)将多个路径分开 
		public PathIterator(String path, String emptyPathDefault) {
			DEBUG.P(this,"PathIterator(2)");
			DEBUG.P("path="+path);
			DEBUG.P("emptyPathDefault="+emptyPathDefault);
				
			this.path = path;
			this.emptyPathDefault = emptyPathDefault;
				
			DEBUG.P(0,this,"PathIterator(2)");
		}

		public PathIterator(String path) { this(path, null); }
		public Iterator<String> iterator() {
			return new Iterator<String>() {//这里的匿名类实现了Iterator<E>接口
				public boolean hasNext() {
					return pos <= path.length();
				}
				public String next() {
					try {//我加上的
					DEBUG.P(this,"next()");
						
					int beg = pos;
					//File.pathSeparator路径分隔符,windows是分号";",unix/linux是冒号":"
					int end = path.indexOf(File.pathSeparator, beg);
						
					DEBUG.P("beg="+beg+" end="+end);
						
					if (end == -1)
						end = path.length();
					pos = end + 1;
						
					DEBUG.P("beg="+beg+" end="+end);
						
					//(beg == end)路径分隔符在最前面或最后面或连续出现的情况(如“:dir1::dir2:”)
					//如果没有emptyPathDefault==null，
					//那么path.substring(beg, end)返回一个空串("")，在用空串生成File的实例时
					//这个File的实例代表的是当前目录，所以把emptyPathDefault设成“.”是多余的
					//见computeUserClassPath()最后一条语句
					if (beg == end && emptyPathDefault != null)
						return emptyPathDefault;
					else
						return path.substring(beg, end);
						
					} finally {
					DEBUG.P(0,this,"next()");
					}
				}
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		}
    }