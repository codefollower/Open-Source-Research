    public void visitTypeArray(JCArrayTypeTree tree) {
    	DEBUG.P(this,"visitTypeArray(JCArrayTypeTree tree)");
		DEBUG.P("tree="+tree);
		
        Type etype = attribType(tree.elemtype, env);
        Type type = new ArrayType(etype, syms.arrayClass);
        result = check(tree, type, TYP, pkind, pt);
        
        DEBUG.P(0,this,"visitTypeArray(JCArrayTypeTree tree)");
    }