	/** Default visitor method: do nothing.
	 */
	public void visitTree(JCTree tree) {
		DEBUG.P(this,"visitTree(1)");
		DEBUG.P("tree="+tree);
		DEBUG.P("do nothing");
		DEBUG.P(0,this,"visitTree(1)");
	}
    }


	public void visitTypeArray(JCArrayTypeTree tree) {
        DEBUG.P(this,"visitTypeArray(1)");
        DEBUG.P("tree="+tree);
	    validate(tree.elemtype);
	    DEBUG.P(0,this,"visitTypeArray(1)");
	}