    //首先获得给定的file的全名(如:F:\javac\bin\classes\my\test\Test4.class)
    //然后按location搜索指定的类路径(假设类路径以F:\javac\bin\classes开头),
    //最后将file的全名一一与类路径中的目录比较,只要file的全名中开始部分与
    //类路径中的某一目录相同,则结束比较,并截取file的全名的剩余部分，将目录分隔
    //符替换成".",去掉扩展名，得到一个完全类名
    //如F:\javac\bin\classes\my\test\Test4.class最后将返回my.test.Test4
    public String inferBinaryName(Location location, JavaFileObject file) {
    	try {
    	DEBUG.P(this,"inferBinaryName(2)");
    	
        file.getClass(); // null check
        location.getClass(); // null check
        // Need to match the path semantics of list(location, ...)
        Iterable<? extends File> path = getLocation(location);
        if (path == null) {
            //System.err.println("Path for " + location + " is null");
            return null;
        }
        //System.err.println("Path for " + location + " is " + path);

        if (file instanceof RegularFileObject) {
            RegularFileObject r = (RegularFileObject) file;
            String rPath = r.getPath();
            //DEBUG.P("RegularFileObject " + file + " " +r.getPath());
            //System.err.println("RegularFileObject " + file + " " +r.getPath());
            for (File dir: path) {
                //System.err.println("dir: " + dir);
                String dPath = dir.getPath();
                //DEBUG.P("dir=" + dir);
                //DEBUG.P("dPath=" + dPath);
                if (!dPath.endsWith(File.separator))
                    dPath += File.separator;
                //DEBUG.P("dPath2=" + dPath);
                if (rPath.regionMatches(true, 0, dPath, 0, dPath.length())
                    && new File(rPath.substring(0, dPath.length())).equals(new File(dPath))) {
                    String relativeName = rPath.substring(dPath.length());
                    return removeExtension(relativeName).replace(File.separatorChar, '.');
                }
            }
        } else if (file instanceof ZipFileObject) {
            ZipFileObject z = (ZipFileObject) file;
            String entryName = z.getZipEntryName();
            
            //DEBUG.P("ZipFileObject " + file);
            //DEBUG.P("entryName=" + entryName);
            
            if (entryName.startsWith(symbolFilePrefix))
                entryName = entryName.substring(symbolFilePrefix.length());
            return removeExtension(entryName).replace('/', '.');
        } else
            throw new IllegalArgumentException(file.getClass().getName());
        // System.err.println("inferBinaryName failed for " + file);
        return null;
        
        
    	} finally {
    		DEBUG.P(0,this,"inferBinaryName(2)");
    	}
    }
    // where
        private static String removeExtension(String fileName) {
            int lastDot = fileName.lastIndexOf(".");
            return (lastDot == -1 ? fileName : fileName.substring(0, lastDot));
        }