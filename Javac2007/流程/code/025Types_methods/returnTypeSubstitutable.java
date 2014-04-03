//Return-Type-Substitutable
    // <editor-fold defaultstate="collapsed" desc="Return-Type-Substitutable">
    /**
     * Does t have a result that is a subtype of the result type of s,
     * suitable for covariant returns?  It is assumed that both types
     * are (possibly polymorphic) method types.  Monomorphic method
     * types are handled in the obvious way.  Polymorphic method types
     * require renaming all type variables of one to corresponding
     * type variables in the other, where correspondence is by
     * position in the type parameter list. */
    public boolean resultSubtype(Type t, Type s, Warner warner) {
		try {//我加上的
		DEBUG.P(this,"resultSubtype(3)");
		DEBUG.P("t="+t+"  t.tag="+TypeTags.toString(t.tag));
		DEBUG.P("s="+s+"  s.tag="+TypeTags.toString(s.tag));

        List<Type> tvars = t.getTypeArguments();
        List<Type> svars = s.getTypeArguments();
		DEBUG.P("tvars="+tvars);
		DEBUG.P("svars="+svars);
        Type tres = t.getReturnType();
        Type sres = subst(s.getReturnType(), svars, tvars);
        return covariantReturnType(tres, sres, warner);

		}finally{//我加上的
		DEBUG.P(0,this,"resultSubtype(3)");
		}
    }

    /**
     * Return-Type-Substitutable.
     * @see <a href="http://java.sun.com/docs/books/jls/">The Java
     * Language Specification, Third Ed. (8.4.5)</a>
     */
    public boolean returnTypeSubstitutable(Type r1, Type r2) {
		DEBUG.P(this,"returnTypeSubstitutable(2)");
		DEBUG.P("r1="+r1+"  r1.tag="+TypeTags.toString(r1.tag));
		DEBUG.P("r2="+r2+"  r2.tag="+TypeTags.toString(r2.tag));
		
		boolean returnResult;

		if (hasSameArgs(r1, r2))
            returnResult = resultSubtype(r1, r2, Warner.noWarnings);
        else
            returnResult = covariantReturnType(r1.getReturnType(),
                                       erasure(r2.getReturnType()),
                                       Warner.noWarnings);
        DEBUG.P("");    
		DEBUG.P("returnResult="+returnResult);
		DEBUG.P(1,this,"returnTypeSubstitutable(2)");
		return returnResult;
		/*
        if (hasSameArgs(r1, r2))
            return resultSubtype(r1, r2, Warner.noWarnings);
        else
            return covariantReturnType(r1.getReturnType(),
                                       erasure(r2.getReturnType()),
                                       Warner.noWarnings);
		*/
    }

    public boolean returnTypeSubstitutable(Type r1,
                                           Type r2, Type r2res,
                                           Warner warner) {
		try {//我加上的
		DEBUG.P(this,"returnTypeSubstitutable(4)");
		DEBUG.P("r1="+r1+"  r1.tag="+TypeTags.toString(r1.tag));
		DEBUG.P("r2="+r2+"  r2.tag="+TypeTags.toString(r2.tag));
		DEBUG.P("r2res="+r2res+"  r2res.tag="+TypeTags.toString(r2res.tag));

        if (isSameType(r1.getReturnType(), r2res))
            return true;
        if (r1.getReturnType().isPrimitive() || r2res.isPrimitive())
            return false;

        if (hasSameArgs(r1, r2))
            return covariantReturnType(r1.getReturnType(), r2res, warner);
        if (!source.allowCovariantReturns())
            return false;
        if (isSubtypeUnchecked(r1.getReturnType(), r2res, warner))
            return true;
        if (!isSubtype(r1.getReturnType(), erasure(r2res)))
            return false;
        warner.warnUnchecked();
        return true;

		}finally{//我加上的
		DEBUG.P(1,this,"returnTypeSubstitutable(4)");
		}
    }

    /**
     * Is t an appropriate return type in an overrider for a
     * method that returns s?
     */
    public boolean covariantReturnType(Type t, Type s, Warner warner) {
		try {//我加上的
		DEBUG.P(this,"covariantReturnType(3)");
		DEBUG.P("t="+t+"  t.tag="+TypeTags.toString(t.tag));
		DEBUG.P("s="+s+"  s.tag="+TypeTags.toString(s.tag));

        return
            isSameType(t, s) ||
            source.allowCovariantReturns() &&
            !t.isPrimitive() &&
            !s.isPrimitive() &&
            isAssignable(t, s, warner);

		}finally{//我加上的
		DEBUG.P(0,this,"covariantReturnType(3)");
		}
    }
    // </editor-fold>
//