    public void visitTypeIdent(JCPrimitiveTypeTree tree) {
    	DEBUG.P(this,"visitTypeIdent(JCPrimitiveTypeTree tree)");
		DEBUG.P("tree="+tree);
		
        result = check(tree, syms.typeOfTag[tree.typetag], TYP, pkind, pt);
        
        DEBUG.P(0,this,"visitTypeIdent(JCPrimitiveTypeTree tree)");
    }