    private static final boolean fileSystemIsCaseSensitive =
        File.separatorChar == '/';

    /** Hack to make Windows case sensitive. Test whether given path
     *  ends in a string of characters with the same case as given name.
     *  Ignore file separators in both path and name.
     */
    private boolean caseMapCheck(File f, String name) {
		try {
            DEBUG.P(this,"caseMapCheck(2)");
            DEBUG.P("File f="+f);
			DEBUG.P("f.exists()="+f.exists());
			DEBUG.P("name="+name);
			DEBUG.P("fileSystemIsCaseSensitive="+fileSystemIsCaseSensitive);

        if (fileSystemIsCaseSensitive) return true;
        // Note that getCanonicalPath() returns the case-sensitive
        // spelled file name.
        String path;
        try {
			//当f不存在时，getCanonicalPath()并不会产生IOException
            path = f.getCanonicalPath();
			DEBUG.P("path="+path);
        } catch (IOException ex) {
			DEBUG.P("IOException ex="+ex);
            return false;
        }
        char[] pcs = path.toCharArray();
        char[] ncs = name.toCharArray();
        int i = pcs.length - 1;
        int j = ncs.length - 1; //当包名是unnamed package时,j=-1，返回ture
        //判断File f所在目录是否以name结尾(windows系统不区分目录大小写)
		//包名对应的目录名必须完全一样，虽然windows平台的目录不区分大小写
		//但是当包名是my.test时，如果windows平台的对应目录是my\Test，
		//则编译器是不会从my\Test中寻找文件的，通常会报“找不到符号”之类的错误
        while (i >= 0 && j >= 0) {
            while (i >= 0 && pcs[i] == File.separatorChar) i--;
            while (j >= 0 && ncs[j] == File.separatorChar) j--;
            if (i >= 0 && j >= 0) {
                if (pcs[i] != ncs[j]) return false;
                i--;
                j--;
            }
        }
        return j < 0;

		} finally {
			DEBUG.P(0,this,"caseMapCheck(2)");
        }
    }