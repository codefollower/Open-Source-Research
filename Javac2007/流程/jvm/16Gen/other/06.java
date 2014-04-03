    public void visitExec(JCExpressionStatement tree) {
    DEBUG.P(this,"visitExec(1)");
	// Optimize x++ to ++x and x-- to --x.
	if (tree.expr.tag == JCTree.POSTINC) tree.expr.tag = JCTree.PREINC;
	else if (tree.expr.tag == JCTree.POSTDEC) tree.expr.tag = JCTree.PREDEC;
	genExpr(tree.expr, tree.expr.type).drop();
	DEBUG.P(0,this,"visitExec(1)");
    }