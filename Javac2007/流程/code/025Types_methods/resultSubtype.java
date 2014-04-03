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