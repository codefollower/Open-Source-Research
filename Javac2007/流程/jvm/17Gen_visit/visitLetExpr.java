    public void visitLetExpr(LetExpr tree) {
		DEBUG.P(this,"visitLetExpr(1)");
		int limit = code.nextreg;
		genStats(tree.defs, env);
		result = genExpr(tree.expr, tree.expr.type).load();
		code.endScopes(limit);
		DEBUG.P(0,this,"visitLetExpr(1)");
    }