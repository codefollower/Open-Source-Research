    public void visitAssign(JCAssign tree) {
		DEBUG.P(this,"visitAssign(1)");
		Item l = genExpr(tree.lhs, tree.lhs.type);
		genExpr(tree.rhs, tree.lhs.type).load();
		result = items.makeAssignItem(l);
		DEBUG.P(0,this,"visitAssign(1)");
    }