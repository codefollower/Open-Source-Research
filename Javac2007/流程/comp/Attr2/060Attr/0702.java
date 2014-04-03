    /** Return the first method which is defined with same args
     *  but different return types in two given interfaces, or null if none
     *  exists.
     *  @param t1     The first type.
     *  @param t2     The second type.
     *  @param site   The most derived type.
     *  @returns symbol from t2 that conflicts with one in t1.
     */
    private Symbol firstIncompatibility(Type t1, Type t2, Type site) {
	try {//我加上的
	DEBUG.P(this,"firstIncompatibility(3)");
	DEBUG.P("t1="+t1);
	DEBUG.P("t2="+t2);
	DEBUG.P("site="+site);

	Map<TypeSymbol,Type> interfaces1 = new HashMap<TypeSymbol,Type>();
	closure(t1, interfaces1);
	Map<TypeSymbol,Type> interfaces2;
	if (t1 == t2)
	    interfaces2 = interfaces1;
	else
	    closure(t2, interfaces1, interfaces2 = new HashMap<TypeSymbol,Type>());
	
	DEBUG.P("");
	DEBUG.P("site="+site);
	DEBUG.P("interfaces1="+interfaces1);
	DEBUG.P("interfaces2="+interfaces2);
	for (Type t3 : interfaces1.values()) {
	    for (Type t4 : interfaces2.values()) {
		Symbol s = firstDirectIncompatibility(t3, t4, site);
		if (s != null) return s;
	    }
	}
	return null;


    }finally{//我加上的
	DEBUG.P(0,this,"firstIncompatibility(3)");
	}
    }