    /** Analyze a condition. Make sure to set (un)initsWhenTrue(WhenFalse)
     *	rather than (un)inits on exit.
     */
    void scanCond(JCTree tree) {
		DEBUG.P(this,"scanCond(1)");
		DEBUG.P("tree.type="+tree.type);
		DEBUG.P("tree.type.isFalse()="+tree.type.isFalse());
		DEBUG.P("tree.type.isTrue()="+tree.type.isTrue());
		DEBUG.P("firstadr="+firstadr+"  nextadr="+nextadr);

		DEBUG.P("");
		DEBUG.P("inits   ="+inits);
		DEBUG.P("uninits ="+uninits);

		//Bits initsPrev = inits.dup();//我加上的
		//Bits uninitsPrev = uninits.dup();//我加上的

		if (tree.type.isFalse()) {//如if(false)，条件表达式的值在编译阶段已知的情况
			if (inits == null) merge();
			initsWhenTrue = inits.dup();
			//因为如果是if(false)，那么then语句部份就不会执行，
			//所以就把initsWhenTrue中从firstadr到nextadr(不包含)的位都置1,
			//这样then语句中涉及的变量都假定它们都己初始化过了
			initsWhenTrue.inclRange(firstadr, nextadr);
			uninitsWhenTrue = uninits.dup();
			//同上
			uninitsWhenTrue.inclRange(firstadr, nextadr);
			initsWhenFalse = inits;
			uninitsWhenFalse = uninits;
		} else if (tree.type.isTrue()) {//如if(true)，条件表达式的值在编译阶段已知的情况
			if (inits == null) merge();
			initsWhenFalse = inits.dup();
			//因为如果是if(true)，那么else语句部份就不会执行，
			//所以就把initsWhenFalse中从firstadr到nextadr(不包含)的位都置1,
			//这样else语句中涉及的变量都假定它们都己初始化过了
			initsWhenFalse.inclRange(firstadr, nextadr);
			uninitsWhenFalse = uninits.dup();
			//同上
			uninitsWhenFalse.inclRange(firstadr, nextadr);
			initsWhenTrue = inits;
			uninitsWhenTrue = uninits;
		} else {//如if(i>0)，条件表达式包含变量且真假值在编译阶段未知的情况
			scan(tree);
			if (inits != null) split();//都要检查
		}
		inits = uninits = null;

		DEBUG.P("");
		//DEBUG.P("inits前         ="+initsPrev+"     inits后="+inits);
		//DEBUG.P("initsWhenFalse  ="+initsWhenFalse);
		//DEBUG.P("initsWhenTrue   ="+initsWhenTrue);
		//DEBUG.P("");
		//DEBUG.P("uninits前       ="+uninitsPrev+"     uninits后="+uninits);

		DEBUG.P("initsWhenFalse   ="+initsWhenFalse);
		DEBUG.P("uninitsWhenFalse ="+uninitsWhenFalse);
		DEBUG.P("");
		DEBUG.P("initsWhenTrue    ="+initsWhenTrue);
		DEBUG.P("uninitsWhenTrue  ="+uninitsWhenTrue);

		//myUninitVars(initsPrev,uninitsPrev);

		myUninitVars(initsWhenFalse.andSet(initsWhenTrue),
			uninitsWhenFalse.andSet(uninitsWhenTrue));
		DEBUG.P(0,this,"scanCond(1)");
    }