    /** A customized "override" warning message.
     *  @param m      The overriding method.
     *  @param other  The overridden method.
     *  @return       An internationalized string.
     */
    static Object uncheckedOverrides(MethodSymbol m, MethodSymbol other) {
	String key;
	if ((other.owner.flags() & INTERFACE) == 0) 
	    key = "unchecked.override";
	else if ((m.owner.flags() & INTERFACE) == 0) 
	    key = "unchecked.implement";
	else 
	    key = "unchecked.clash.with";
	return JCDiagnostic.fragment(key, m, m.location(), other, other.location());
    }