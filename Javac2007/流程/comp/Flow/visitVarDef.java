    public void visitVarDef(JCVariableDecl tree) {
		DEBUG.P(this,"visitVarDef(1)");
		boolean track = trackable(tree.sym);
		DEBUG.P("track="+track);
		//注意:在JCBlock中定义的变量,tree.sym.owner.kind都为MTH
		DEBUG.P("tree.sym.owner.kind="+Kinds.toString(tree.sym.owner.kind));
		if (track && tree.sym.owner.kind == MTH) newVar(tree.sym);
		DEBUG.P("tree.init="+tree.init);
		
		Bits initsPrev = inits.dup();//我加上的
		Bits uninitsPrev = uninits.dup();//我加上的
		
		if (tree.init != null) {
			Lint lintPrev = lint;
			lint = lint.augment(tree.sym.attributes_field);
			try{
				scanExpr(tree.init);
				if (track) letInit(tree.pos(), tree.sym);
			} finally {
				lint = lintPrev;
			}
		}
		DEBUG.P("inits  前="+initsPrev);
		DEBUG.P("inits  后="+inits);
		//注意下面两个的输出，跟调用letInit的情况有关
		DEBUG.P("uninits前="+uninitsPrev);
		DEBUG.P("uninits后="+uninits);
		DEBUG.P(0,this,"visitVarDef(1)");
    }