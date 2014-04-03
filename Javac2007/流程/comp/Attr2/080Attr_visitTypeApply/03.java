    /** Attribute a type argument list, returning a list of types.
     */
    List<Type> attribTypes(List<JCExpression> trees, Env<AttrContext> env) {
    	DEBUG.P(this,"attribTypes(2)");
    	DEBUG.P("trees="+trees);
		DEBUG.P("env="+env);
        ListBuffer<Type> argtypes = new ListBuffer<Type>();
        for (List<JCExpression> l = trees; l.nonEmpty(); l = l.tail)
            argtypes.append(chk.checkRefType(l.head.pos(), attribType(l.head, env)));
        
        DEBUG.P(0,this,"attribTypes(2)");
        return argtypes.toList();
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