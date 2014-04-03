    public void visitTopLevel(JCCompilationUnit tree) {
    	try {
            // <editor-fold defaultstate="collapsed">
    	DEBUG.P(this,"visitTopLevel(1)");
    	DEBUG.P("tree.namedImportScope="+tree.namedImportScope);
    	DEBUG.P("tree.starImportScope="+tree.starImportScope);
    	DEBUG.P("env.info.scope="+env.info.scope);
    	DEBUG.P("tree.packge.name="+tree.packge.name);
    	DEBUG.P("tree.packge.fullname="+tree.packge.fullname);
    	DEBUG.P("tree.packge.owner.name="+tree.packge.owner.name);
    	DEBUG.P("tree.packge.owner.fullname="+tree.packge.owner.getQualifiedName());
    	
		DEBUG.P(2);
    	DEBUG.P("tree.starImportScope.elems="+tree.starImportScope.elems);
    	//当tree.starImportScope.nelems=0时tree.starImportScope.elems==null
        if (tree.starImportScope.elems != null) {
        	/*
        	当在同一文件内定义了多个类时就会出现这种情况
        	如下代码所示:
        	
        	package my.test;
			public class Test {}
        	class MyTheSamePackageClass {}
        	
        	*/
        	DEBUG.P("starImportScope 已处理");
        	
            // we must have already processed this toplevel
            return;
        }

		DEBUG.P("checkClash="+checkClash);
		DEBUG.P("tree.pid="+tree.pid);

        // check that no class exists with same fully qualified name as
        // toplevel package
        if (checkClash && tree.pid != null) {
            Symbol p = tree.packge;
			while (p.owner != syms.rootPackage) {
                p.owner.complete(); // enter all class members of p            
                /*
                比如:如果包名是my.test,然后在my目录下有个test.java文件
                那么就会出现错误提示:
                package my.test clashes with class of same name
                package my.test;
                ^
                原因:
                如果类路径是: F:\javac\bin\mybin；
                test.java文件位置: F:\javac\bin\mybin\my\test.java；
                p是: my.test
                p.owner就是: my
                那么加载的包名是: my
                test.java文件内容不用管，什么都可以；

                当调用到com.sun.tools.javac.util.JavacFileManager===>inferBinaryName(2)时
                它按包名my截断F:\javac\bin\mybin\my\test.java得到my\test.java
                将目录分隔符替换成".",去掉扩展名，得到一个完全类名"my.test"，
                如果这里的包名也是"my.test"就会产生冲突

				但是，如果在F:\javac\bin\mybin目录下有个类文件my.java是不会冲突的，
				因为当p变为“my"时，p.owner变成了syms.rootPackage，while循环结束了。
				如果把循环条件改成(p.owner != null)，就可以检测出my.java与包名my冲突
                */
                if (syms.classes.get(p.getQualifiedName()) != null) {
                    log.error(tree.pos,
                              "pkg.clashes.with.class.of.same.name",
                              p);
                }
                p = p.owner;
                
                DEBUG.P("p.name="+p.name);
                DEBUG.P("p.fullname="+p.getQualifiedName());
                DEBUG.P("p.owner.name="+p.owner.name);
                DEBUG.P("p.owner.fullname="+p.owner.getQualifiedName());
            }
        }
        // </editor-fold>

        // process package annotations
		//编译package-info.java时能测试tree.packageAnnotations!=null
        annotateLater(tree.packageAnnotations, env, tree.packge);
        
        // Import-on-demand java.lang.
        importAll(tree.pos, reader.enterPackage(names.java_lang), env);

		DEBUG.P("tree.namedImportScope="+tree.namedImportScope);
    	DEBUG.P("tree.starImportScope="+tree.starImportScope);
    	DEBUG.P("env.info.scope="+env.info.scope);
		DEBUG.P("env="+env);

        // Process all import clauses.
        memberEnter(tree.defs, env);

		//DEBUG.P("stop",true);
        
    	}finally{
    	DEBUG.P(0,this,"visitTopLevel(1)");
    	}
    }