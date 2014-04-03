    /** Check that type is a class or interface type.
     *  @param pos           Position to be used for error reporting.
     *  @param t             The type to be checked.
     */
    Type checkClassType(DiagnosticPosition pos, Type t) {
		try {//我加上的
		DEBUG.P(this,"checkClassType(2)");
		DEBUG.P("t="+t+"  t.tag="+TypeTags.toString(t.tag));
	
        /*src/my/test/EnterTest.java:23: 意外的类型
        找到： 类型参数 T 
        需要： 类
        public class EnterTest<T,S> extends T implements EnterTestInterfaceA,EnterTestInterfaceB {                                         ^
        */
		if (t.tag != CLASS && t.tag != ERROR)
            return typeTagError(pos,
                                JCDiagnostic.fragment("type.req.class"),
                                (t.tag == TYPEVAR)
                                ? JCDiagnostic.fragment("type.parameter", t)
                                : t); 
		else
			return t;
	    
		}finally{//我加上的
		DEBUG.P(0,this,"checkClassType(2)");
		}
    }

    /** Check that type is a class or interface type.
     *  @param pos           Position to be used for error reporting.
     *  @param t             The type to be checked.
     *  @param noBounds    True if type bounds are illegal here.
     */
    Type checkClassType(DiagnosticPosition pos, Type t, boolean noBounds) {
    try {//我加上的
	DEBUG.P(this,"checkClassType(3)");
	DEBUG.P("t="+t);
	DEBUG.P("t.tag="+TypeTags.toString(t.tag));
	DEBUG.P("t.isParameterized()="+t.isParameterized());
	DEBUG.P("noBounds="+noBounds);
	
	t = checkClassType(pos, t);
	DEBUG.P("t="+t);
	DEBUG.P("t.tag="+TypeTags.toString(t.tag));
	DEBUG.P("t.isParameterized()="+t.isParameterized());
	DEBUG.P("noBounds="+noBounds);
	//noBounds为true时表示t的类型参数不能是WILDCARD(即: <?>、<? extends ...>、<? super ...>)
	if (noBounds && t.isParameterized()) {
	    List<Type> args = t.getTypeArguments();
	    while (args.nonEmpty()) {
	    DEBUG.P("args.head.tag="+TypeTags.toString(args.head.tag));
	    /*报错如下:
	    bin\mysrc\my\test\Test.java:85: unexpected type
		found   : ?
		required: class or interface without bounds
		public class Test<S,T> extends TestOhter<?,String> implements MyInterfaceA,MyInterfaceB {                                       ^
		1 error
		*/
		if (args.head.tag == WILDCARD)
		    return typeTagError(pos,
					log.getLocalizedString("type.req.exact"),
					args.head);
		args = args.tail;
	    }
	}
	return t;
	
	}finally{//我加上的
	DEBUG.P(0,this,"checkClassType(3)");
	}
	
    }