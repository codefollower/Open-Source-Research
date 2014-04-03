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