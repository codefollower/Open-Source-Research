    /** Include class corresponding to given class file in package,
     *  unless (1) we already have one the same kind (.class or .java), or
     *         (2) we have one of the other kind, and the given class file
     *             is older.
     */
    protected void includeClassFile(PackageSymbol p, JavaFileObject file) {
    	DEBUG.P("");
    	DEBUG.P(this,"includeClassFile(2)");
    	DEBUG.P("PackageSymbol p.flags_field="+p.flags_field+" ("+Flags.toString(p.flags_field)+")");
    	DEBUG.P("p.members_field="+p.members_field);
    	
    	//检查PackageSymbol是否已有成员(以前有没有ClassSymbol加进了members_field)
    	//另外只要子包已有成员，那么就认为子包的所有owner都已有成员
    	//另请参考Flags类的EXISTS字段说明
        if ((p.flags_field & EXISTS) == 0)
            for (Symbol q = p; q != null && q.kind == PCK; q = q.owner)
                q.flags_field |= EXISTS;
        JavaFileObject.Kind kind = file.getKind();
        int seen;
        if (kind == JavaFileObject.Kind.CLASS)
            seen = CLASS_SEEN;//CLASS_SEEN在Flags类中定义
        else
            seen = SOURCE_SEEN;
        
        //binaryName在先前的fillIn(3)中已找过一次了,这里又找了一次,
        //可以适当改进一下,因为调用inferBinaryName方法还是耗时间的
        String binaryName = fileManager.inferBinaryName(currentLoc, file);
        DEBUG.P("binaryName="+binaryName);
        int lastDot = binaryName.lastIndexOf(".");
        Name classname = names.fromString(binaryName.substring(lastDot + 1));
        DEBUG.P("classname="+classname);
        boolean isPkgInfo = classname == names.package_info;
        ClassSymbol c = isPkgInfo
            ? p.package_info
            : (ClassSymbol) p.members_field.lookup(classname).sym;
        DEBUG.P("ClassSymbol c="+c);
        if (c != null) DEBUG.P("在包("+p+")的Scope中已有这个ClassSymbol");
        if (c == null) {
            c = enterClass(classname, p);
            if (c.classfile == null) // only update the file if's it's newly created
                c.classfile = file;
            if (isPkgInfo) {
                p.package_info = c;
            } else {
            	DEBUG.P("c="+c+" c.owner="+c.owner+" p="+p);
            	if(c.owner != p) 
            		DEBUG.P("(内部类没有Enter到包Scope)");
            	else 
            		DEBUG.P("(已Enter到包Scope)");
            	/*
            	也就是说PackageSymbol的members_field不会含有内部类
            	这是因为在enterClass(classname, p)的内部可以改变
            	c的owner,而不一定是传进去的参数PackageSymbol p.
            	
            	但是还是奇怪,如下代码:
            	package my.test;
            	public class Test{
					public class MyInnerClass {
					}
				}
				打印结果还是:
				c=my.test.Test$MyInnerClass c.owner=my.test p=my.test
				*/
                if (c.owner == p)  // it might be an inner class
                    p.members_field.enter(c);
            }
        //在类路径中找到包名与类名相同的多个文件时，
        //1.如果文件扩展名相同，则选先找到的那一个
        //2.如果文件扩展名不同且在javac中加上“-Xprefer:source”选项时，则选源文件(.java)
        //3.如果文件扩展名不同且在javac中没有加“-Xprefer:source”选项，则选最近修改过的那一个
        
        //(c.flags_field & seen) == 0)表示原先的ClassSymbol所代表的文件
        //的扩展名与现在的file所代表的文件的扩展名不同
        } else if (c.classfile != null && (c.flags_field & seen) == 0) {
        	DEBUG.P("ClassSymbol c.classfile(旧)="+c.classfile);
            // if c.classfile == null, we are currently compiling this class
            // and no further action is necessary.
            // if (c.flags_field & seen) != 0, we have already encountered
            // a file of the same kind; again no further action is necessary.
            if ((c.flags_field & (CLASS_SEEN | SOURCE_SEEN)) != 0)
                c.classfile = preferredFileObject(file, c.classfile);
        }
        c.flags_field |= seen;
        DEBUG.P("ClassSymbol c.classfile="+c.classfile);
        DEBUG.P("ClassSymbol c.flags_field="+c.flags_field+" ("+Flags.toString(c.flags_field)+")");
        DEBUG.P(1,this,"includeClassFile(2)");
    }