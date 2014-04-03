    public void visitTypeParameter(JCTypeParameter tree) {
        DEBUG.P(this,"visitTypeParameter(1)");
        DEBUG.P("tree="+tree);
	    validate(tree.bounds);
	    checkClassBounds(tree.pos(), tree.type);
	    DEBUG.P(0,this,"visitTypeParameter(1)");
	}

    /** Visitor method: Validate a list of type expressions.
     */
    void validate(List<? extends JCTree> trees) {
    DEBUG.P(this,"validate(List<? extends JCTree> trees)");
    DEBUG.P("trees.size="+trees.size());
    DEBUG.P("trees="+trees);
	for (List<? extends JCTree> l = trees; l.nonEmpty(); l = l.tail)
	    validate(l.head);
	DEBUG.P(1,this,"validate(List<? extends JCTree> trees)");
    }

	void validate(JCTree tree) {
    DEBUG.P(this,"validate(JCTree tree)");
    if (tree != null) {
    	//DEBUG.P("tree.type="+tree.type);
    	DEBUG.P("tree.tag="+tree.myTreeTag());
	}else DEBUG.P("tree=null");
    
	try {
	    if (tree != null) tree.accept(validator);
	} catch (CompletionFailure ex) {
	    completionError(tree.pos(), ex);
	}
	DEBUG.P(1,this,"validate(JCTree tree)");
    }