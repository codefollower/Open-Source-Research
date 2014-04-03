    /** Visitor method for if statements.
     */
    public void visitIf(JCIf tree) {
    DEBUG.P(this,"visitIf(1)");
	DEBUG.P("tree="+tree);
	JCTree cond = tree.cond = translate(tree.cond, syms.booleanType);
	
	DEBUG.P("cond.type="+cond.type);
	DEBUG.P("cond.type.isTrue()="+cond.type.isTrue());
	DEBUG.P("cond.type.isFalse()="+cond.type.isFalse());
	if (cond.type.isTrue()) {
	    result = translate(tree.thenpart);
	} else if (cond.type.isFalse()) {
	    if (tree.elsepart != null) {
		result = translate(tree.elsepart);
	    } else {
		result = make.Skip();
	    }
	} else {
	    // Condition is not a compile-time constant.
	    tree.thenpart = translate(tree.thenpart);
	    tree.elsepart = translate(tree.elsepart);
	    result = tree;
	}
	
	DEBUG.P(1,this,"visitIf(1)");
    }