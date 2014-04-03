    public Iterable<? extends JavaFileObject> getJavaFileObjectsFromFiles(
        Iterable<? extends File> files)
    {
    	DEBUG.P(this,"getJavaFileObjectsFromFiles(1)");
    	DEBUG.P("files.getClass().getName()="+files.getClass().getName());
    	DEBUG.P("(files instanceof Collection)="+(files instanceof Collection));
    	if (files instanceof Collection)
    		DEBUG.P("(((Collection)files).size())="+(((Collection)files).size()));
    		
        ArrayList<RegularFileObject> result;
        
        //在com.sun.tools.javac.main.Main===>compile(4)方法中
        //把List<File> filenames传给files，
        //com.sun.tools.javac.util.List<T>类
        //继承了java.util.AbstractCollection<E>类，
        //而java.util.AbstractCollection<E>类又
        //实现了java.util.Collection<E>接口
        if (files instanceof Collection)
        	//构造一个ArrayList，这个ArrayList的初始大小能容纳size()个元素
        	//这里作者也考虑到了效率问题，如果files的size()个数己知的话，
        	//就事先预分配好size()指定大小的空间，这样在以后往ArrayList中添加
        	//新元素时就不用每次都分配新空间了。
            result = new ArrayList<RegularFileObject>(((Collection)files).size());
        else
        	//初始大小能容纳10个元素
        	//(见java.util.ArrayList类ArrayList()方法的原码)
            result = new ArrayList<RegularFileObject>();
        //注意ArrayList类的size()返回的是实际已加入的元素个数
        //不是指初始大小容量大小
        //也就是说假设初始大小容量大小是20，当调用ArrayList类的add方法
        //增加了5个元素时，size()返回的是5而不是20
        DEBUG.P("result.size()="+result.size());
        
        for (File f: files)
            result.add(new RegularFileObject(nullCheck(f)));
        
        for (File f: files) DEBUG.P("fileName="+f);
        DEBUG.P("result.size()="+result.size());
        DEBUG.P(0,this,"getJavaFileObjectsFromFiles(1)");
        return result;
    }