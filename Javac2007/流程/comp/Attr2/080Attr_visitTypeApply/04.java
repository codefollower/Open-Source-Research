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

    /** Check that type is a reference type, i.e. a class, interface or array type
     *  or a type variable.
     *  @param pos           Position to be used for error reporting.
     *  @param t             The type to be checked.
     */
    Type checkRefType(DiagnosticPosition pos, Type t) {
    try {//我加上的
	DEBUG.P(this,"checkRefType(2)");
	DEBUG.P("t="+t+"  t.tag="+TypeTags.toString(t.tag));
	
	switch (t.tag) {
	case CLASS:
	case ARRAY:
	case TYPEVAR:
	case WILDCARD:
	case ERROR:
	    return t;
	default:
	/*例子:
	bin\mysrc\my\test\Test.java:8: 意外的类型
	找到： int
	需要： 引用
			MyTestInnerClass<Z extends ExtendsTest<int,? super ExtendsTest>>
												   ^
	*/
	    return typeTagError(pos,
				JCDiagnostic.fragment("type.req.ref"),
				t);
	}
	
    }finally{//我加上的
	DEBUG.P(0,this,"checkRefType(2)");
	}

    }