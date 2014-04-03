    /** Compute all the supertypes of t, indexed by type symbol. */
    private void closure(Type t, Map<TypeSymbol,Type> typeMap) {
	try {//我加上的
	DEBUG.P(this,"closure(2)");
	DEBUG.P("t="+t+"  t.tag="+TypeTags.toString(t.tag));
	DEBUG.P("typeMap="+typeMap);

	if (t.tag != CLASS) return;
	if (typeMap.put(t.tsym, t) == null) {
	    closure(types.supertype(t), typeMap);
	    for (Type i : types.interfaces(t))
		closure(i, typeMap);
	}

    }finally{//我加上的
	DEBUG.P(0,this,"closure(2)");
	}
    }

    /** Compute all the supertypes of t, indexed by type symbol (except thise in typesSkip). */
    private void closure(Type t, Map<TypeSymbol,Type> typesSkip, Map<TypeSymbol,Type> typeMap) {
	try {//我加上的
	DEBUG.P(this,"closure(3)");
	DEBUG.P("t="+t+"  t.tag="+TypeTags.toString(t.tag));
	DEBUG.P("typesSkip="+typesSkip);
	DEBUG.P("typeMap="+typeMap);

	if (t.tag != CLASS) return;
	if (typesSkip.get(t.tsym) != null) return;
	if (typeMap.put(t.tsym, t) == null) {
	    closure(types.supertype(t), typesSkip, typeMap);
	    for (Type i : types.interfaces(t))
		closure(i, typesSkip, typeMap);
	}

    }finally{//我加上的
	DEBUG.P(0,this,"closure(3)");
	}
    }