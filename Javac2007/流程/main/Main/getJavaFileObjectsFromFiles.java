    public Iterable<? extends JavaFileObject> getJavaFileObjectsFromFiles(
        Iterable<? extends File> files)
    {
    	DEBUG.P(this,"getJavaFileObjectsFromFiles(1)");
    	
        ArrayList<RegularFileObject> result;
        if (files instanceof Collection)
            result = new ArrayList<RegularFileObject>(((Collection)files).size());
        else
            result = new ArrayList<RegularFileObject>();
        for (File f: files)
            result.add(new RegularFileObject(nullCheck(f)));
        
        for (File f: files) DEBUG.P("fileName="+f);
        DEBUG.P(0,this,"getJavaFileObjectsFromFiles(1)");
        return result;
    }