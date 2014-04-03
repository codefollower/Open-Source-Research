    /** Make a package, given its fully qualified name.
     */

	/*
	当packageName=java.lang,首次调用enterPackage()时的输出:
	com.sun.tools.javac.jvm.ClassReader===>enterPackage(1)
	-------------------------------------------------------------------------
	fullname=java.lang
	Convert.shortName(fullname)=lang
	Convert.packagePart(fullname)=java
	com.sun.tools.javac.jvm.ClassReader===>enterPackage(1)
	-------------------------------------------------------------------------
	fullname=java
	Convert.shortName(fullname)=java
	Convert.packagePart(fullname)=
	com.sun.tools.javac.jvm.ClassReader===>enterPackage(1)
	-------------------------------------------------------------------------
	fullname=
	com.sun.tools.javac.jvm.ClassReader===>enterPackage(1)  END
	-------------------------------------------------------------------------
	com.sun.tools.javac.jvm.ClassReader===>enterPackage(1)  END
	-------------------------------------------------------------------------
	com.sun.tools.javac.jvm.ClassReader===>enterPackage(1)  END
	-------------------------------------------------------------------------
	*/
    public PackageSymbol enterPackage(Name fullname) {
    	DEBUG.P(this,"enterPackage(1)");
		DEBUG.P("fullname="+fullname);
		
		//packages是一个Map
        PackageSymbol p = packages.get(fullname);
        if (p == null) {
        	//断言:当assert后面的条件为真时执行assert语句后的其他语句，否则报错退出。
        	//p == null且fullname也是一个空串(fullname=names.empty)这两个条件不会同时发生，
        	//因为空串(fullname=names.empty)在初始化Systab类时已跟PackageSymbol rootPackage对应
        	//且PackageSymbol rootPackage已放入packages
            assert !fullname.isEmpty() : "rootPackage missing!";
            
            DEBUG.P("Convert.shortName(fullname)="+Convert.shortName(fullname));
            DEBUG.P("Convert.packagePart(fullname)="+Convert.packagePart(fullname));
            
            /*
			如果fullname从没出现过，一般会递归调用到当fullname是names.empty(Table.empty)时结束,
			rootPackage的fullname就是names.empty,在init()时已加进packages.
			另外,PackageSymbol类是按包名的逆序递归嵌套的,内部字段Symbol owner就是下面代码中
			的enterPackage(Convert.packagePart(fullname))
			
			举例:包名my.test的嵌套格式如下:
			PackageSymbol {
				Name name="test";
				Symbol owner=new PackageSymbol {
					Name name="my";
					Symbol owner=rootPackage = new PackageSymbol(names.empty, null);
				}
			}
			*/
            p = new PackageSymbol(
                Convert.shortName(fullname),
                enterPackage(Convert.packagePart(fullname)));
            //这一步是为了以后调用Symbol.complete()来间接调用ClassReader的complete(Symbol sym)
            p.completer = this;
            packages.put(fullname, p);
        }
        DEBUG.P(0,this,"enterPackage(1)");
        return p;
    }

    /** Make a package, given its unqualified name and enclosing package.
     */
    public PackageSymbol enterPackage(Name name, PackageSymbol owner) {
        return enterPackage(TypeSymbol.formFullName(name, owner));
    }