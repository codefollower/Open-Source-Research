    public void visitUnary(JCUnary tree) {
		DEBUG.P(this,"visitUnary(1)");
		DEBUG.P("tree.tag="+tree.myTreeTag());
		switch (tree.tag) {
		case JCTree.NOT:
			scanCond(tree.arg);
			Bits t = initsWhenFalse;
			initsWhenFalse = initsWhenTrue;
			initsWhenTrue = t;
			t = uninitsWhenFalse;
			uninitsWhenFalse = uninitsWhenTrue;
			uninitsWhenTrue = t;
			break;
		case JCTree.PREINC: case JCTree.POSTINC:
		case JCTree.PREDEC: case JCTree.POSTDEC:
			scanExpr(tree.arg);
			letInit(tree.arg);
			break;
		default:
			scanExpr(tree.arg);
		}
		DEBUG.P("initsWhenFalse  ="+initsWhenFalse);
		DEBUG.P("uninitsWhenFalse="+uninitsWhenFalse);
		DEBUG.P("initsWhenTrue   ="+initsWhenTrue);
		DEBUG.P("uninitsWhenTrue ="+uninitsWhenTrue);
		DEBUG.P(0,this,"visitUnary(1)");
    }