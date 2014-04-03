    public void visitDoLoop(JCDoWhileLoop tree) {
		DEBUG.P(this,"visitDoLoop(1)");
		genLoop(tree, tree.body, tree.cond, List.<JCExpressionStatement>nil(), false);
		DEBUG.P(0,this,"visitDoLoop(1)");
    }

    public void visitWhileLoop(JCWhileLoop tree) {
		DEBUG.P(this,"visitWhileLoop(1)");
		genLoop(tree, tree.body, tree.cond, List.<JCExpressionStatement>nil(), true);
		DEBUG.P(0,this,"visitWhileLoop(1)");
    }

    public void visitForLoop(JCForLoop tree) {
		DEBUG.P(this,"visitForLoop(1)");
		int limit = code.nextreg;
		genStats(tree.init, env);
		genLoop(tree, tree.body, tree.cond, tree.step, true);
		code.endScopes(limit);
		DEBUG.P(0,this,"visitForLoop(1)");
    }