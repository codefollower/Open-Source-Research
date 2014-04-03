    /** Construct method type from method signature.
     *  @param typarams    The method's type parameters.
     *  @param params      The method's value parameters.
     *  @param res             The method's result type,
     *                 null if it is a constructor.
     *  @param thrown      The method's thrown exceptions.
     *  @param env             The method's (local) environment.
     */
    Type signature(List<JCTypeParameter> typarams,
                   List<JCVariableDecl> params,
                   JCTree res,
                   List<JCExpression> thrown,
                   Env<AttrContext> env) {
        try {//我加上的
        DEBUG.P(this,"signature(5)");
        DEBUG.P("typarams="+typarams);
		DEBUG.P("params="+params); 
		DEBUG.P("res="+res); 
		DEBUG.P("thrown="+thrown); 
		DEBUG.P("env="+env); 
		        	
        // Enter and attribute type parameters.
        List<Type> tvars = enter.classEnter(typarams, env);
        attr.attribTypeVariables(typarams, env);

        // Enter and attribute value parameters.
        ListBuffer<Type> argbuf = new ListBuffer<Type>();
        for (List<JCVariableDecl> l = params; l.nonEmpty(); l = l.tail) {
            memberEnter(l.head, env);
            argbuf.append(l.head.vartype.type);
        }

        // Attribute result type, if one is given.
        Type restype = res == null ? syms.voidType : attr.attribType(res, env);

        // Attribute thrown exceptions.
        ListBuffer<Type> thrownbuf = new ListBuffer<Type>();
        for (List<JCExpression> l = thrown; l.nonEmpty(); l = l.tail) {
            Type exc = attr.attribType(l.head, env);
			DEBUG.P("exc="+exc);
			DEBUG.P("exc.tag="+TypeTags.toString(exc.tag));
            if (exc.tag != TYPEVAR)
                exc = chk.checkClassType(l.head.pos(), exc);//也就是说throws语句后面必须是类名
            thrownbuf.append(exc);
        }
        
        //注意MethodType并不包含Type Parameter
        Type mtype = new MethodType(argbuf.toList(),
                                    restype,
                                    thrownbuf.toList(),
                                    syms.methodClass);
        return tvars.isEmpty() ? mtype : new ForAll(tvars, mtype);
        
        }finally{//我加上的
        DEBUG.P(0,this,"signature(5)");
        }
    }