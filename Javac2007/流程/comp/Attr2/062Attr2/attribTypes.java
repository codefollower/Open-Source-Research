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