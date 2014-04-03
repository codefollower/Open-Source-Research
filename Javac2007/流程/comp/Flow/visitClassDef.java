    public void visitClassDef(JCClassDecl tree) {
		DEBUG.P(this,"visitClassDef(JCClassDecl tree)");
		if (tree.sym == null) return;
		DEBUG.P("tree.name="+tree.name);
		DEBUG.P("tree.sym="+tree.sym);

		JCClassDecl classDefPrev = classDef;
		List<Type> thrownPrev = thrown;
		List<Type> caughtPrev = caught;
		boolean alivePrev = alive;
		int firstadrPrev = firstadr;
		int nextadrPrev = nextadr;
		ListBuffer<PendingExit> pendingExitsPrev = pendingExits;
		Lint lintPrev = lint;

		pendingExits = new ListBuffer<PendingExit>();

		//不是匿名类
		if (tree.name != names.empty) {
			caught = List.nil();
			firstadr = nextadr;
		}
		classDef = tree;
		thrown = List.nil();
		lint = lint.augment(tree.sym.attributes_field);

		try {
			// define all the static fields
			DEBUG.P("");DEBUG.P("define all the static fields......");
			//DEBUG.P("tree="+tree);
			//如果类中没有定义任何构造函数，
			//那么由编译器生成的默认构造函数 "类名(){super();}" 将放在tree.defs的最前面.
			//参见MemberEnter中的DefaultConstructor
			//for (List<JCTree> l = tree.defs; l.nonEmpty(); l = l.tail) {
			//	DEBUG.P("l.head.tag="+l.head.myTreeTag()); DEBUG.P("");
			//}
			for (List<JCTree> l = tree.defs; l.nonEmpty(); l = l.tail) {
				DEBUG.P("l.head.tag="+l.head.myTreeTag());
				if (l.head.tag == JCTree.VARDEF) {
					JCVariableDecl def = (JCVariableDecl)l.head;
					DEBUG.P("def.mods.flags="+Flags.toString(def.mods.flags));
					DEBUG.P("l.head="+l.head);
					//找出所有标记为static final但没有初始化的字段,并用uninits记录下来
					if ((def.mods.flags & STATIC) != 0) {
						VarSymbol sym = def.sym;
						if (trackable(sym))
							newVar(sym);
					}
				}
				DEBUG.P("");
			}
			
			DEBUG.P(2);
			DEBUG.P("可能尚未初始化的static final变量有:");
			DEBUG.P("----------------------------------");
			for(int i=0;i<vars.length;i++)
				if (vars[i]!=null) DEBUG.P("vars["+i+"]="+vars[i]);
			DEBUG.P(2);

			// process all the static initializers
			DEBUG.P("");DEBUG.P("process all the static initializers......");
			/*
			for (List<JCTree> l = tree.defs; l.nonEmpty(); l = l.tail) {
				if (l.head.tag != JCTree.METHODDEF &&
					(TreeInfo.flags(l.head) & STATIC) != 0) {
					//DEBUG.P("l.head.tag="+l.head.getKind());
					DEBUG.P("l.head.tag="+l.head.myTreeTag());
					DEBUG.P("l.head="+l.head);
					DEBUG.P("");
				}
			}
			*/

			for (List<JCTree> l = tree.defs; l.nonEmpty(); l = l.tail) {
				//满足if条件的有:静态变量、静态block、静态成员类
				if (l.head.tag != JCTree.METHODDEF &&
					(TreeInfo.flags(l.head) & STATIC) != 0) {
					//DEBUG.P("l.head.tag="+l.head.getKind());
					DEBUG.P("l.head.tag="+l.head.myTreeTag());
					scanDef(l.head);
					
					/*
					//在静态块中有可能调用抛出异常的静态方法，但是没有捕获
					static {
						final int i4=myStaticMethod();
					}
					
					public static int myStaticMethod() throws Exception{
						return 10;
					}
					
					错误提示:
					bin\mysrc\my\test\Test.java:44: 未报告的异常 java.lang.Exception；必须对其进行捕捉或声明以便抛出
						final int i4=myStaticMethod();
												   ^
					*/
					errorUncaught();
					
					DEBUG.P("");
				}
			}
			
			//注意:执行完上面的代码后，即使static final变量没有初始化还是不能发现错误
			
			DEBUG.P("tree.name="+tree.name);
			// add intersection of all thrown clauses of initial constructors
			// to set of caught exceptions, unless class is anonymous.
			if (tree.name != names.empty) {
				/*
					在所有构造方法中找出第一条语句不是this()调用的所有构造方法
					将这些构造方法抛出的异常构成一个交集
					
					例子:
					Test() {
						this(2);
					}
					Test(int myInt) throws Error, Exception {
						this.myInt=myInt;
					}
					Test(float f) throws Exception {
					}
					
					第一条语句不是this()调用的所有构造方法有:Test(int myInt)与Test(float f)
					抛出的异常构成一个交集:Exception
				*/
				DEBUG.P("caught="+caught);
				boolean firstConstructor = true;
				for (List<JCTree> l = tree.defs; l.nonEmpty(); l = l.tail) {
					boolean isInitialConstructor=TreeInfo.isInitialConstructor(l.head);
					//DEBUG.P("l.head.tag="+l.head.getKind());
					DEBUG.P("l.head.tag="+l.head.myTreeTag());
					DEBUG.P("l.head="+l.head);
					DEBUG.P("isInitialConstructor="+isInitialConstructor);
					
					
					//if (TreeInfo.isInitialConstructor(l.head)) {
					if (isInitialConstructor) {
						List<Type> mthrown =
							((JCMethodDecl) l.head).sym.type.getThrownTypes();
						DEBUG.P("mthrown="+mthrown);
						if (firstConstructor) {
							caught = mthrown;
							firstConstructor = false;
						} else {
							caught = chk.intersect(mthrown, caught);
						}
					}
					DEBUG.P("");
				}
			}
			DEBUG.P("caught="+caught);

			//只有未初始化的final实例字段才trackable
			DEBUG.P("");DEBUG.P("define all the instance fields......");
			// define all the instance fields
			for (List<JCTree> l = tree.defs; l.nonEmpty(); l = l.tail) {
				if (l.head.tag == JCTree.VARDEF) {
					JCVariableDecl def = (JCVariableDecl)l.head;
					if ((def.mods.flags & STATIC) == 0) {
						VarSymbol sym = def.sym;
						if (trackable(sym))
							newVar(sym);
					}
				}
			}
			
			DEBUG.P("");DEBUG.P("process all the instance initializers......");
			/*//所有没有siatic的JCTree
			for (List<JCTree> l = tree.defs; l.nonEmpty(); l = l.tail) {
				if (l.head.tag != JCTree.METHODDEF &&
				(TreeInfo.flags(l.head) & STATIC) == 0) {
					DEBUG.P("l.head.tag="+l.head.getKind());
					DEBUG.P("l.head.flags="+Flags.toString(TreeInfo.flags(l.head)));
					DEBUG.P("l.head="+l.head);
					DEBUG.P("");
				}
			}
			*/
			
			// process all the instance initializers
			for (List<JCTree> l = tree.defs; l.nonEmpty(); l = l.tail) {
				if (l.head.tag != JCTree.METHODDEF &&
					(TreeInfo.flags(l.head) & STATIC) == 0) {
					scanDef(l.head);
					errorUncaught();
				}
			}

			// in an anonymous class, add the set of thrown exceptions to
			// the throws clause of the synthetic constructor and propagate
			// outwards.
			if (tree.name == names.empty) {
				for (List<JCTree> l = tree.defs; l.nonEmpty(); l = l.tail) {
					if (TreeInfo.isInitialConstructor(l.head)) {
						JCMethodDecl mdef = (JCMethodDecl)l.head;
						mdef.thrown = make.Types(thrown);
						mdef.sym.type.setThrown(thrown);
					}
				}
				thrownPrev = chk.union(thrown, thrownPrev);
			}
			
			DEBUG.P("");DEBUG.P("process all the methods......");
			// process all the methods
			for (List<JCTree> l = tree.defs; l.nonEmpty(); l = l.tail) {
				if (l.head.tag == JCTree.METHODDEF) {
					scan(l.head);
					errorUncaught();
					
					DEBUG.P("处理结束 方法名:"+((JCMethodDecl)l.head).name);
					DEBUG.P(2);
				}
			}

			thrown = thrownPrev;
		} finally {
			pendingExits = pendingExitsPrev;
			alive = alivePrev;
			nextadr = nextadrPrev;
			firstadr = firstadrPrev;
			caught = caughtPrev;
			classDef = classDefPrev;
			lint = lintPrev;
			DEBUG.P(0,this,"visitClassDef(JCClassDecl tree)");
		}
    }