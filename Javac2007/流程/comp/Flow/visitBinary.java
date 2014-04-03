    public void visitBinary(JCBinary tree) {
		DEBUG.P(this,"visitBinary(1)");
		DEBUG.P("tree.lhs="+tree.lhs);
		DEBUG.P("tree.rhs="+tree.rhs);
		//DEBUG.P("tree.tag="+tree.myTreeTag());
		switch (tree.tag) {
		case JCTree.AND:
			scanCond(tree.lhs);
			Bits initsWhenFalseLeft = initsWhenFalse;
			Bits uninitsWhenFalseLeft = uninitsWhenFalse;
			inits = initsWhenTrue;
			uninits = uninitsWhenTrue;
			scanCond(tree.rhs);
			initsWhenFalse.andSet(initsWhenFalseLeft);
			uninitsWhenFalse.andSet(uninitsWhenFalseLeft);
			break;
		case JCTree.OR:
			scanCond(tree.lhs);
			Bits initsWhenTrueLeft = initsWhenTrue;
			Bits uninitsWhenTrueLeft = uninitsWhenTrue;
			inits = initsWhenFalse;
			uninits = uninitsWhenFalse;
			scanCond(tree.rhs);
			initsWhenTrue.andSet(initsWhenTrueLeft);
			uninitsWhenTrue.andSet(uninitsWhenTrueLeft);
			break;
		default:
			scanExpr(tree.lhs);
			scanExpr(tree.rhs);
		}
		DEBUG.P(0,this,"visitBinary(1)");
    }