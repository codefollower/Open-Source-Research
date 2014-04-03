    /** Analyze an expression. Make sure to set (un)inits rather than
     *	(un)initsWhenTrue(WhenFalse) on exit.
     */
    void scanExpr(JCTree tree) {
		DEBUG.P(this,"scanExpr(1)");

		if (tree == null) DEBUG.P("tree is null");
		else DEBUG.P("tree.tag="+tree.myTreeTag());

		if (tree != null) {
			scan(tree);
			DEBUG.P("inits="+inits);
			if (inits == null) merge();
		}
		DEBUG.P(0,this,"scanExpr(1)");
    }

    /** Analyze a list of expressions.
     */
    void scanExprs(List<? extends JCExpression> trees) {
		DEBUG.P(this,"scanExprs(1)");
		if (trees == null) DEBUG.P("trees is null");
		else DEBUG.P("trees.size="+trees.size());
		
		if (trees != null)
			for (List<? extends JCExpression> l = trees; l.nonEmpty(); l = l.tail)
				scanExpr(l.head);
		
		DEBUG.P(0,this,"scanExprs(1)");	
    }