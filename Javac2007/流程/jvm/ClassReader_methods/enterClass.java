    /** Create a new toplevel or member class symbol with given name
     *  and owner and enter in `classes' unless already there.
     */
    public ClassSymbol enterClass(Name name, TypeSymbol owner) {
    	DEBUG.P(this,"enterClass(Name name, TypeSymbol owner)");
    	DEBUG.P("name="+name+" owner="+owner);
    	
        Name flatname = TypeSymbol.formFlatName(name, owner);
        ClassSymbol c = classes.get(flatname);
        
        DEBUG.P("flatname="+flatname+" ClassSymbol c="+c);
        
        if (c == null) {
            c = defineClass(name, owner);
            classes.put(flatname, c);
        } else if ((c.name != name || c.owner != owner) && owner.kind == TYP && c.owner.kind == PCK) {
        	/*
        	这种情况主要是在一个类中又定义了一个类(或接口)(也就是成员类的情况)
        	在执行Enter.visitTopLevel()方法时需要为JCCompilationUnit.packge.members_field
        	加载包名目录下的所有类文件并“包装”成ClassSymbol加入members_field中，但在执行
        	到Enter.visitClassDef()时成员类得重新移到它的owner的Scope中
        	
        	举例:如下代码片断:
        	package my.test;
        	public class Test {
				public static interface MyInterface {
				}
			}
			打印结果:
			com.sun.tools.javac.jvm.ClassReader===>enterClass(Name name, TypeSymbol owner)
			-------------------------------------------------------------------------
			name=MyInterface owner=my.test.Test
			flatname=my.test.Test$MyInterface ClassSymbol c=my.test.Test$MyInterface
			c.name=Test$MyInterface c.owner=my.test
			c.fullname(注意分析)=my.test.Test.MyInterface
			com.sun.tools.javac.jvm.ClassReader===>enterClass(Name name, TypeSymbol owner)  END
			-------------------------------------------------------------------------
        	*/
        	
        	
            // reassign fields of classes that might have been loaded with
            // their flat names.
            DEBUG.P("c.name="+c.name+" c.owner="+c.owner);
            c.owner.members().remove(c);
            DEBUG.P("("+name+")是一个成员类，已从("+c.owner+")包的Scope中删除");
            c.name = name;
            c.owner = owner;
            c.fullname = ClassSymbol.formFullName(name, owner);
            DEBUG.P("c.fullname(注意分析)="+c.fullname);
            
        }
        //DEBUG.P("c.owner="+c.owner);
        DEBUG.P(0,this,"enterClass(Name name, TypeSymbol owner)");
        return c;
    }

    /**
     * Creates a new toplevel class symbol with given flat name and
     * given class (or source) file.
     *
     * @param flatName a fully qualified binary class name
     * @param classFile the class file or compilation unit defining
     * the class (may be {@code null})
     * @return a newly created class symbol
     * @throws AssertionError if the class symbol already exists
     */
    public ClassSymbol enterClass(Name flatName, JavaFileObject classFile) {
    	DEBUG.P(this,"enterClass(2)");
    	DEBUG.P("flatName="+flatName+" classFile="+classFile);
        ClassSymbol cs = classes.get(flatName);
        if (cs != null) {
            String msg = Log.format("%s: completer = %s; class file = %s; source file = %s",
                                    cs.fullname,
                                    cs.completer,
                                    cs.classfile,
                                    cs.sourcefile);
            throw new AssertionError(msg);
        }
        Name packageName = Convert.packagePart(flatName);
        DEBUG.P("packageName="+packageName);
        /*
        syms未检测是否为null,会出现小问题(参见Symtab类中的注释)
        syms是在protected ClassReader(Context context, boolean definitive)中通过
        "syms = Symtab.instance(context);"进行初始化的，但在执行Symtab.instance(context)的过
        程中又会在Symtab(Context context)中间接执行到这里，但此时并没有完成
        Symtab(Context context)，也就是syms没有初始化，当执行syms.unnamedPackage时就会引起
        java.lang.NullPointerException
        */
        PackageSymbol owner = packageName.isEmpty()
				? syms.unnamedPackage
				: enterPackage(packageName);
        cs = defineClass(Convert.shortName(flatName), owner);
        cs.classfile = classFile;
        classes.put(flatName, cs);

        DEBUG.P(0,this,"enterClass(2)");
        return cs;
    }

    /** Create a new member or toplevel class symbol with given flat name
     *  and enter in `classes' unless already there.
     */
    public ClassSymbol enterClass(Name flatname) {
		try {//我加上的
		DEBUG.P(this,"enterClass(1)");
		
        ClassSymbol c = classes.get(flatname);
        if(c!=null) DEBUG.P("ClassSymbol("+flatname+")已存在");
        //DEBUG.P("ClassSymbol c="+(JavaFileObject)null);//呵呵，第一次见这种语法(JavaFileObject)null
        /*2008-11-15更正:
		因为上面有两个方法:
		1.public ClassSymbol enterClass(Name name, TypeSymbol owner)
		2.public ClassSymbol enterClass(Name flatName, JavaFileObject classFile)
		如果用这种方式调用:enterClass(flatname, null)
		将产生编译错误:对enterClass的引用不明确
		因为null既可以赋给TypeSymbol owner也可赋给JavaFileObject classFile
		所以必须用类型转换:(JavaFileObject)null，告诉编译器它调用的是方法2
		*/
		if (c == null)
            return enterClass(flatname, (JavaFileObject)null);
        else
            return c;
            
        }finally{//我加上的
		DEBUG.P(1,this,"enterClass(1)");
		}
    }
