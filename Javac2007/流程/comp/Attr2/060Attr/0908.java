    // where
	private boolean isDeprecatedOverrideIgnorable(MethodSymbol m, ClassSymbol origin) {
	    try {//我加上的
			DEBUG.P(this,"isDeprecatedOverrideIgnorable(2)");
			DEBUG.P("m="+m);
			DEBUG.P("origin="+origin);


		// If the method, m, is defined in an interface, then ignore the issue if the method
	    // is only inherited via a supertype and also implemented in the supertype,
	    // because in that case, we will rediscover the issue when examining the method
	    // in the supertype.
	    // If the method, m, is not defined in an interface, then the only time we need to
	    // address the issue is when the method is the supertype implemementation: any other
	    // case, we will have dealt with when examining the supertype classes
	    ClassSymbol mc = m.enclClass();
	    Type st = types.supertype(origin.type);
		DEBUG.P("st="+st+"  st.tag="+TypeTags.toString(st.tag));
	    if (st.tag != CLASS)
		return true;
	    MethodSymbol stimpl = m.implementation((ClassSymbol)st.tsym, types, false);

	    if (mc != null && ((mc.flags() & INTERFACE) != 0)) {
		List<Type> intfs = types.interfaces(origin.type);
		return (intfs.contains(mc.type) ? false : (stimpl != null));
	    }
	    else
		return (stimpl != m);

		}finally{//我加上的
		DEBUG.P(0,this,"isDeprecatedOverrideIgnorable(2)");
		}
	}