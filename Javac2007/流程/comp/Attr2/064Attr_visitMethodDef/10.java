    /** Return the first method which is defined with same args
     *  but different return types in two given interfaces, or null if none
     *  exists.
     *  @param t1     The first type.
     *  @param t2     The second type.
     *  @param site   The most derived type.
     *  @returns symbol from t2 that conflicts with one in t1.
     */
    private Symbol firstIncompatibility(Type t1, Type t2, Type site) {
	Map<TypeSymbol,Type> interfaces1 = new HashMap<TypeSymbol,Type>();
	closure(t1, interfaces1);
	Map<TypeSymbol,Type> interfaces2;
	if (t1 == t2)
	    interfaces2 = interfaces1;
	else
	    closure(t2, interfaces1, interfaces2 = new HashMap<TypeSymbol,Type>());

	for (Type t3 : interfaces1.values()) {
	    for (Type t4 : interfaces2.values()) {
		Symbol s = firstDirectIncompatibility(t3, t4, site);
		if (s != null) return s;
	    }
	}
	return null;
    }

    /** Compute all the supertypes of t, indexed by type symbol. */
    private void closure(Type t, Map<TypeSymbol,Type> typeMap) {
	if (t.tag != CLASS) return;
	if (typeMap.put(t.tsym, t) == null) {
	    closure(types.supertype(t), typeMap);
	    for (Type i : types.interfaces(t))
		closure(i, typeMap);
	}
    }

    /** Compute all the supertypes of t, indexed by type symbol (except thise in typesSkip). */
    private void closure(Type t, Map<TypeSymbol,Type> typesSkip, Map<TypeSymbol,Type> typeMap) {
	if (t.tag != CLASS) return;
	if (typesSkip.get(t.tsym) != null) return;
	if (typeMap.put(t.tsym, t) == null) {
	    closure(types.supertype(t), typesSkip, typeMap);
	    for (Type i : types.interfaces(t))
		closure(i, typesSkip, typeMap);
	}
    }
