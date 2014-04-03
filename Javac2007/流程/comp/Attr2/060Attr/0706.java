    /** Return the first method in t2 that conflicts with a method from t1. */
    private Symbol firstDirectIncompatibility(Type t1, Type t2, Type site) {
	try {//我加上的
	DEBUG.P(this,"firstDirectIncompatibility(3)");
	DEBUG.P("t1="+t1);
	DEBUG.P("t2="+t2);
	DEBUG.P("site="+site);
	DEBUG.P("t1.tsym.members()="+t1.tsym.members());
	DEBUG.P("t2.tsym.members()="+t2.tsym.members());

	for (Scope.Entry e1 = t1.tsym.members().elems; e1 != null; e1 = e1.sibling) {
	    Symbol s1 = e1.sym;
	    Type st1 = null;
		DEBUG.P("s1.name="+s1.name);
		DEBUG.P("s1.kind="+Kinds.toString(s1.kind));
	    if (s1.kind != MTH || !s1.isInheritedIn(site.tsym, types)) continue;
            Symbol impl = ((MethodSymbol)s1).implementation(site.tsym, types, false);
            //if (impl != null && (impl.flags() & ABSTRACT) == 0) continue;
            if (impl != null && (impl.flags() & ABSTRACT) == 0) {
            	DEBUG.P("");
            	DEBUG.P("***********************");
            	DEBUG.P("site="+site);
            	DEBUG.P("与下面的type中的方法( "+s1.name+" )兼容");
            	DEBUG.P("t1="+t1);
            	DEBUG.P("所以不再与下面的type比较");
				DEBUG.P("t2="+t2);
				DEBUG.P("***********************");
				DEBUG.P("");
            	continue;
            }
		
		DEBUG.P("");DEBUG.P("for.......................");
	    for (Scope.Entry e2 = t2.tsym.members().lookup(s1.name); e2.scope != null; e2 = e2.next()) {
		Symbol s2 = e2.sym;
		DEBUG.P("s2.name="+s2.name);
		DEBUG.P("s2.kind="+Kinds.toString(s2.kind));

		if (s1 == s2) continue;
		if (s2.kind != MTH || !s2.isInheritedIn(site.tsym, types)) continue;
		if (st1 == null) st1 = types.memberType(t1, s1);
		Type st2 = types.memberType(t2, s2);
		if (types.overrideEquivalent(st1, st2)) {
		    List<Type> tvars1 = st1.getTypeArguments();
		    List<Type> tvars2 = st2.getTypeArguments();
		    Type rt1 = st1.getReturnType();
		    Type rt2 = types.subst(st2.getReturnType(), tvars2, tvars1);
		    boolean compat =
			types.isSameType(rt1, rt2) ||
                        rt1.tag >= CLASS && rt2.tag >= CLASS &&
                        (types.covariantReturnType(rt1, rt2, Warner.noWarnings) ||
                         types.covariantReturnType(rt2, rt1, Warner.noWarnings));
		    if (!compat) return s2;
		}
	    }
	}
	return null;
    
	}finally{//我加上的
	DEBUG.P(0,this,"firstDirectIncompatibility(3)");
	}
    }