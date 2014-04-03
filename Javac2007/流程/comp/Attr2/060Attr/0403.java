    void attribBounds(List<JCTypeParameter> typarams) {
    	DEBUG.P(this,"attribBounds(1)");
    	DEBUG.P("typarams="+typarams);
        for (JCTypeParameter typaram : typarams) {
            Type bound = typaram.type.getUpperBound();
			DEBUG.P("");
            DEBUG.P("typaram="+typaram);
			DEBUG.P("bound="+bound);
			if (bound != null) DEBUG.P("bound.tsym.className="+bound.tsym.getClass().getName());

            if (bound != null && bound.tsym instanceof ClassSymbol) {
                ClassSymbol c = (ClassSymbol)bound.tsym;
                DEBUG.P("bound.tsym.flags_field="+Flags.toString(c.flags_field));
                if ((c.flags_field & COMPOUND) != 0) {
                    assert (c.flags_field & UNATTRIBUTED) != 0 : c;
                    attribClass(typaram.pos(), c);
                }
            }
        }
        DEBUG.P(1,this,"attribBounds(1)");
    }

	/** Visitor method: Validate a list of type parameters.
     */
    void validateTypeParams(List<JCTypeParameter> trees) {
   	DEBUG.P(this,"validateTypeParams(1)");
   	DEBUG.P("trees="+trees);
   	
	for (List<JCTypeParameter> l = trees; l.nonEmpty(); l = l.tail)
	    validate(l.head);
	DEBUG.P(1,this,"validateTypeParams(1)");
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
