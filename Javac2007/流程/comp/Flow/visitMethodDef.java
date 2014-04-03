    public void visitMethodDef(JCMethodDecl tree) {
		try {//我加上的
		DEBUG.P(this,"visitMethodDef(JCMethodDecl tree)");
		DEBUG.P("tree="+tree);

		if (tree.body == null) return;

		List<Type> caughtPrev = caught;
		List<Type> mthrown = tree.sym.type.getThrownTypes();
		Bits initsPrev = inits.dup();
		Bits uninitsPrev = uninits.dup();
		int nextadrPrev = nextadr;
		int firstadrPrev = firstadr;
		Lint lintPrev = lint;

		lint = lint.augment(tree.sym.attributes_field);

		assert pendingExits.isEmpty();

		try {
			boolean isInitialConstructor =
			TreeInfo.isInitialConstructor(tree);

			DEBUG.P("isInitialConstructor="+isInitialConstructor);
			DEBUG.P("firstadr="+firstadr);
			DEBUG.P("nextadr="+nextadr);

			if (!isInitialConstructor)
				firstadr = nextadr;

			DEBUG.P("");DEBUG.P("for tree.params......");
			for (List<JCVariableDecl> l = tree.params; l.nonEmpty(); l = l.tail) {
				JCVariableDecl def = l.head;
				DEBUG.P("def="+def);
				scan(def);
				//从下面两条语句看出，
				//只是为了在newVar(VarSymbol sym)给def.sym.adr赋值，并修改nextadr
				inits.incl(def.sym.adr);
				uninits.excl(def.sym.adr);
				
				DEBUG.P("inits  ="+inits);
				DEBUG.P("uninits="+uninits);DEBUG.P("");
			}

			DEBUG.P(2);DEBUG.P("for tree.params......结束");
			DEBUG.P("caught1="+caught);
			
			DEBUG.P("方法:"+tree.name+" isInitialConstructor="+isInitialConstructor);
			DEBUG.P("方法:"+tree.name+" mthrown="+mthrown);
			//DEBUG.P("mthrown="+mthrown);

			if (isInitialConstructor) //第一条语句不是this(...)调用的构造函数
				caught = chk.union(caught, mthrown);
			//方法或静态初始化块的情形?方法会有BLOCK标记吗？
			else if ((tree.sym.flags() & (BLOCK | STATIC)) != BLOCK)
				caught = mthrown;
			// else we are in an instance initializer block;
			// leave caught unchanged.

			DEBUG.P("caught2="+caught);

			alive = true;
			scanStat(tree.body);
			DEBUG.P("方法体scan结束");
			DEBUG.P("alive="+alive);
			DEBUG.P("ree.sym.type.getReturnType()="+tree.sym.type.getReturnType());
			if (alive && tree.sym.type.getReturnType().tag != VOID)
				log.error(TreeInfo.diagEndPos(tree.body), "missing.ret.stmt");

			/*
			当数据流分析到任意一个第一条语句不是this()调用的构造方法时,
			在分析完此构造方法的方法体时，如果发现final实例字段还有初始
			化，就可以直接报错了，而不管其他构造方法内部是否对它初始化过
			*/
			if (isInitialConstructor) {
				DEBUG.P("firstadr="+firstadr);
				DEBUG.P("nextadr="+nextadr);
				for (int i = firstadr; i < nextadr; i++)
					if (vars[i].owner == classDef.sym)
						checkInit(TreeInfo.diagEndPos(tree.body), vars[i]);
			}


			List<PendingExit> exits = pendingExits.toList();
			pendingExits = new ListBuffer<PendingExit>();
			while (exits.nonEmpty()) {
				PendingExit exit = exits.head;
				exits = exits.tail;
				if (exit.thrown == null) {
					assert exit.tree.tag == JCTree.RETURN;
					if (isInitialConstructor) {
						inits = exit.inits;
						for (int i = firstadr; i < nextadr; i++)
							checkInit(exit.tree.pos(), vars[i]);
					}
				} else {
					// uncaught throws will be reported later
					pendingExits.append(exit);
				}
			}
		} finally {
			inits = initsPrev;
			uninits = uninitsPrev;
			nextadr = nextadrPrev;
			firstadr = firstadrPrev;
			caught = caughtPrev;
			lint = lintPrev;
		}

		}finally{//我加上的
		DEBUG.P(1,this,"visitMethodDef(JCMethodDecl tree)");
		}
    }