    /** Check that a class does not inherit two concrete methods
     *  with the same signature.
     *  @param pos          Position to be used for error reporting.
     *  @param site         The class type to be checked.
     */
    public void checkCompatibleConcretes(DiagnosticPosition pos, Type site) {
    try {//我加上的
	DEBUG.P(this,"checkCompatibleConcretes(2)");
	DEBUG.P("site="+site);
	
	Type sup = types.supertype(site);
	DEBUG.P("sup="+sup);
	DEBUG.P("sup.tag="+TypeTags.toString(sup.tag));
	if (sup.tag != CLASS) return;

	for (Type t1 = sup;
	     t1.tsym.type.isParameterized();
	     t1 = types.supertype(t1)) {
	    for (Scope.Entry e1 = t1.tsym.members().elems;
		 e1 != null;
		 e1 = e1.sibling) {
		Symbol s1 = e1.sym;
		if (s1.kind != MTH ||
		    (s1.flags() & (STATIC|SYNTHETIC|BRIDGE)) != 0 ||
		    !s1.isInheritedIn(site.tsym, types) ||
		    ((MethodSymbol)s1).implementation(site.tsym,
						      types,
						      true) != s1)
		    continue;
		Type st1 = types.memberType(t1, s1);
		int s1ArgsLength = st1.getParameterTypes().length();
		if (st1 == s1.type) continue;

		for (Type t2 = sup;
		     t2.tag == CLASS;
		     t2 = types.supertype(t2)) {
		    for (Scope.Entry e2 = t1.tsym.members().lookup(s1.name);
			 e2.scope != null;
			 e2 = e2.next()) {
			Symbol s2 = e2.sym;
			if (s2 == s1 ||
			    s2.kind != MTH ||
			    (s2.flags() & (STATIC|SYNTHETIC|BRIDGE)) != 0 ||
			    s2.type.getParameterTypes().length() != s1ArgsLength ||
			    !s2.isInheritedIn(site.tsym, types) ||
			    ((MethodSymbol)s2).implementation(site.tsym,
							      types,
							      true) != s2)
			    continue;
			Type st2 = types.memberType(t2, s2);
			if (types.overrideEquivalent(st1, st2))
			    log.error(pos, "concrete.inheritance.conflict",
				      s1, t1, s2, t2, sup);
		    }
		}
	    }
	}
	
	}finally{//我加上的
	DEBUG.P(0,this,"checkCompatibleConcretes(2)");
	}
	
    }