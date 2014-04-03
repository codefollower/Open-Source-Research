    public void visitTopLevel(JCCompilationUnit tree) {
		JavaFileObject prev = log.useSource(tree.sourcefile);
		DEBUG.P(this,"visitTopLevel(1)");
		//在没有进行到Enter阶段的时候JCCompilationUnit的PackageSymbol packge
		//是null，这也说明了:Parser的后续阶段的任务就是往各类JCTree中“塞入数据”
		DEBUG.P("JCCompilationUnit tree.sourcefile="+tree.sourcefile);
		DEBUG.P("JCCompilationUnit tree.packge="+tree.packge);
        DEBUG.P("JCCompilationUnit tree.pid="+tree.pid);

		boolean addEnv = false;
		
		//DEBUG.P("JCCompilationUnit tree.sourcefile.className="+tree.sourcefile.getClass().getName());
		//输出一般是:com.sun.tools.javac.util.JavacFileManager$RegularFileObject
		//JavacFileManager.RegularFileObject, JavacFileManager.ZipFileObject都实现了
		//JavaFileObject接口
		
		//检查JCCompilationUnit tree.sourcefile的文件名是否是package-info.java
		boolean isPkgInfo = tree.sourcefile.isNameCompatible("package-info",
									 JavaFileObject.Kind.SOURCE);
		DEBUG.P("isPkgInfo="+isPkgInfo);

		//tree.pid是源文件所在包的全名					     
		if (tree.pid != null) {
				//在执行了TreeInfo.fullName(tree.pid)后，将产生一个完整的包名，并且
				//存放在Name.Table中
				//(注:如果包名是:my.test,在Name.Table中会有三个name:(my),(test)与(my.test)
				//作者一心只想提高javac的执行速度
				//DEBUG.P(names.myNames());
			tree.packge = reader.enterPackage(TreeInfo.fullName(tree.pid));
			//DEBUG.P(names.myNames());
			DEBUG.P("tree.packageAnnotations="+tree.packageAnnotations);
			if (tree.packageAnnotations.nonEmpty()) {
					if (isPkgInfo) {
						addEnv = true;
					} else {
						//只有package-info.java才能有包注释
						//参考:Parser.compilationUnit()
						log.error(tree.packageAnnotations.head.pos(),
								  "pkg.annotations.sb.in.package-info.java");
					}
			}
		} else {
				//源文件未定义所属package的情况
			tree.packge = syms.unnamedPackage;
		}
		DEBUG.P("JCCompilationUnit tree.packge="+tree.packge);
		DEBUG.P("JCCompilationUnit tree.packge.members_field="+tree.packge.members_field);
		DEBUG.P("syms.classes.size="+syms.classes.size()+" keySet="+syms.classes.keySet());
        DEBUG.P("syms.packages.size="+syms.packages.size()+" keySet="+syms.packages.keySet());
		
		/*
		complete()在com.sun.tools.javac.code.Symbol定义
		tree.packge是com.sun.tools.javac.code.Symbol.PackageSymbol的实例引用
		com.sun.tools.javac.jvm.ClassReader实现了com.sun.tools.javac.code.Symbol.Completer接口
		调用Symbol.complete()会通过Symbol.Completer completer(在ClassReader的enterPackage方法中赋值)
		间接调用ClassReader的complete(Symbol sym)方法
		
		调用过程:com.sun.tools.javac.code.Symbol::complete()==>
				 com.sun.tools.javac.jvm.ClassReader::complete(1)

		在没执行complete()前，在执行完上面的enterPackage后，得到了一个
		PackageSymbol，但这个PackageSymbol的Scope members_field是null的，
		执行complete()的目的就是为了找出PackageSymbol所表示的包名中的
		所有类文件，并将这些类文件“包装”成一个ClassSymbol放入members_field
		*/

		//虽然complete()方法抛出CompletionFailure，
		//但因为CompletionFailure是RuntimeException的子类，
		//所以在visitTopLevel此方法中可以不捕获
		tree.packge.complete(); // Find all classes in package.

		//成员也有可能是未编译的.java文件
		//如果文件是Package-Info1.java，
		//则因为"-"不满足ClassReader的方法fillIn(3)中的SourceVersion.isIdentifier(simpleName)而被过滤掉，
		//另外文件Package-Info1.java在ClassReader的方法includeClassFile(2)中被加入tree.packge.package_info，而不是加入tree.packge.members_field
		DEBUG.P(3);
		DEBUG.P(tree.packge+"包中的所有成员装载完成(Enter)");
		DEBUG.P("JCCompilationUnit tree.packge.members_field="+tree.packge.members_field);
        DEBUG.P("syms.classes.size="+syms.classes.size()+" keySet="+syms.classes.keySet());
        DEBUG.P("syms.packages.size="+syms.packages.size()+" keySet="+syms.packages.keySet());
		DEBUG.P(3);

        Env<AttrContext> env = topLevelEnv(tree);

		// Save environment of package-info.java file.
		if (isPkgInfo) {
			Env<AttrContext> env0 = typeEnvs.get(tree.packge);
			if (env0 == null) {
				typeEnvs.put(tree.packge, env);
			} else {
				JCCompilationUnit tree0 = env0.toplevel;
				if (!fileManager.isSameFile(tree.sourcefile, tree0.sourcefile)) {
					/* 当同时编译两个在不同目录的同名package-info.java文件时，
					如果这两个package-info.java的内容都是相同的包如，:package test.enter;
					则会发出"警告：[package-info] 已找到软件包 test.enter 的 package-info.java 文件"
					//test\enter\package-info.java
					package test.enter;
					//test\enter\package-info.java
					package test.enter;
					*/
					log.warning(tree.pid != null ? tree.pid.pos()
								: null,
								"pkg-info.already.seen",
								tree.packge);
					if (addEnv || (tree0.packageAnnotations.isEmpty() &&
						   tree.docComments != null &&
						   tree.docComments.get(tree) != null)) {
						typeEnvs.put(tree.packge, env);
					}
				}
			}
		}

		classEnter(tree.defs, env);
        if (addEnv) {//包注释待处理
            todo.append(env);
        }
		log.useSource(prev);
		result = null;
	
	/*******************以下都是打印信息的语句(调试用途)********************/
        DEBUG.P(2);
        DEBUG.P("***第一阶段Enter完成***");
        DEBUG.P("-----------------------------------------------");
        DEBUG.P("包名: "+tree.packge);
        DEBUG.P("--------------------------");
        DEBUG.P("tree.packge.members_field: "+tree.packge.members_field);
        DEBUG.P("tree.namedImportScope    : "+tree.namedImportScope);
        DEBUG.P("tree.starImportScope     : "+tree.starImportScope);
        DEBUG.P("");
        
        //ListBuffer<ClassSymbol> uncompleted
        DEBUG.P("等待编译的类的总数: "+uncompleted.size());
        DEBUG.P("--------------------------");
        for(ClassSymbol myClassSymbol:uncompleted) {
        	DEBUG.P("类名             : "+myClassSymbol);
        	DEBUG.P("members_field    : "+myClassSymbol.members_field);
        	DEBUG.P("flags            : "+Flags.toString(myClassSymbol.flags_field));
        	DEBUG.P("sourcefile       : "+myClassSymbol.sourcefile);
        	DEBUG.P("classfile        : "+myClassSymbol.classfile);
        	DEBUG.P("completer        : "+myClassSymbol.completer);
        	ClassType myClassType=(ClassType)myClassSymbol.type;
        	DEBUG.P("type             : "+myClassType);
        	DEBUG.P("outer_field      : "+myClassType.getEnclosingType());
        	DEBUG.P("supertype_field  : "+myClassType.supertype_field);
        	DEBUG.P("interfaces_field : "+myClassType.interfaces_field);
        	DEBUG.P("typarams_field   : "+myClassType.typarams_field);
        	DEBUG.P("allparams_field  : "+myClassType.allparams_field);
        	DEBUG.P("");
        }
        DEBUG.P("");
        DEBUG.P("Env总数: "+typeEnvs.size());
        DEBUG.P("--------------------------");
        for(Map.Entry<TypeSymbol,Env<AttrContext>> myMapEntry:typeEnvs.entrySet())
        	DEBUG.P(""+myMapEntry);
        DEBUG.P(2);
        
        DEBUG.P("Todo总数: "+todo.size());
        DEBUG.P("--------------------------");
        for(List<Env<AttrContext>> l=todo.toList();l.nonEmpty();l=l.tail)
        	DEBUG.P(""+l.head);
        DEBUG.P(2);
        
    	DEBUG.P("syms.classes.size="+syms.classes.size()+" keySet="+syms.classes.keySet());
        DEBUG.P("syms.packages.size="+syms.packages.size()+" keySet="+syms.packages.keySet());
        DEBUG.P(2);
		DEBUG.P(2,this,"visitTopLevel(1)");
	//
    }