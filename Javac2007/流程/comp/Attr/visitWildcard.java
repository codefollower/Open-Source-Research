    public void visitWildcard(JCWildcard tree) {
    	DEBUG.P(this,"visitWildcard(1)");
    	DEBUG.P("tree="+tree);
    	DEBUG.P("tree.kind="+tree.kind);
    	DEBUG.P("tree.inner="+tree.inner);
    	
        //- System.err.println("visitWildcard("+tree+");");//DEBUG
        Type type = (tree.kind.kind == BoundKind.UNBOUND)
            ? syms.objectType
            : attribType(tree.inner, env);
        result = check(tree, new WildcardType(chk.checkRefType(tree.pos(), type),
                                              tree.kind.kind,
                                              syms.boundClass),
                       TYP, pkind, pt);
                       
       DEBUG.P(0,this,"visitWildcard(1)");                
    }