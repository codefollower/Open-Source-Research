    /** A customized "override" warning message.
     *  @param m      The overriding method.
     *  @param other  The overridden method.
     *  @return       An internationalized string.
     */
    static Object varargsOverrides(MethodSymbol m, MethodSymbol other) {
	String key;
	if ((other.owner.flags() & INTERFACE) == 0) 
	    key = "varargs.override";
	else  if ((m.owner.flags() & INTERFACE) == 0) 
	    key = "varargs.implement";
	else
	    key = "varargs.clash.with";
	return JCDiagnostic.fragment(key, m, m.location(), other, other.location());
    }