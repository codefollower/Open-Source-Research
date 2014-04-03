    // where
        private boolean checkDirectory(String optName) {
			try {//我加上的
			DEBUG.P(this,"checkDirectory(1)");
			DEBUG.P("optName="+optName);

            String value = options.get(optName);
			DEBUG.P("value="+value);
            if (value == null)
                return true;

            File file = new File(value);

			DEBUG.P("file.exists()="+file.exists());
            if (!file.exists()) {
				//javac -d bin\directory_not_found_test
				//如果指定的目录不存在，提示以下错误:
				//javac: directory not found: bin\directory_not_found_test
				//用法: javac <options> <source files>
				//-help 用于列出可能的选项
				//注:com\sun\tools\javac\resources\javac_zh_CN.properties文件
				//没有定义"err.dir.not.found"，所以出现的提示是英文的，
				//这是从com\sun\tools\javac\resources\javac.properties文件提取的信息
                error("err.dir.not.found", value);
                return false;
            }

			DEBUG.P("file.isDirectory()="+file.isDirectory());
            if (!file.isDirectory()) {
				//javac -d args.txt
				//如果指定的是一个存在的文件，提示以下错误:
				//javac: 不是目录: args.txt
				//用法: javac <options> <source files>
				//-help 用于列出可能的选项
                error("err.file.not.directory", value);
                return false;
            }
            return true;

			}finally{//我加上的
			DEBUG.P(0,this,"checkDirectory(1)");
			}
        }