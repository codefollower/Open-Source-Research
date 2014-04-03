    /** Check that all abstract methods implemented by a class are
     *  mutually compatible.
     *  @param pos          Position to be used for error reporting.
     *  @param c            The class whose interfaces are checked.
     */
    void checkCompatibleSupertypes(DiagnosticPosition pos, Type c) {
    try {//我加上的
	DEBUG.P(this,"checkCompatibleSupertypes(2)");
	DEBUG.P("c="+c);
	
	
	List<Type> supertypes = types.interfaces(c);
	Type supertype = types.supertype(c);
	if (supertype.tag == CLASS &&
	    (supertype.tsym.flags() & ABSTRACT) != 0)
	    supertypes = supertypes.prepend(supertype);
	DEBUG.P("supertypes="+supertypes);
	DEBUG.P("");
	for (List<Type> l = supertypes; l.nonEmpty(); l = l.tail) {
		DEBUG.P("allowGenerics="+allowGenerics);
		DEBUG.P("l.head.getTypeArguments()="+l.head.getTypeArguments());
		
	    if (allowGenerics && !l.head.getTypeArguments().isEmpty() &&
		!checkCompatibleAbstracts(pos, l.head, l.head, c))
		return;
	    for (List<Type> m = supertypes; m != l; m = m.tail)
		if (!checkCompatibleAbstracts(pos, l.head, m.head, c))
		    return;
	}
	checkCompatibleConcretes(pos, c);
	
	}finally{//我加上的
	DEBUG.P(2,this,"checkCompatibleSupertypes(2)");
	}
	
    }