/** A customized "cannot override" error message.
     *  @param m      The overriding method.
     *  @param other  The overridden method.
     *  @return       An internationalized string.
     */
    static Object cannotOverride(MethodSymbol m, MethodSymbol other) {
	String key;
	if ((other.owner.flags() & INTERFACE) == 0) 
	    key = "cant.override";
	else if ((m.owner.flags() & INTERFACE) == 0) 
	    key = "cant.implement";
	else
	    key = "clashes.with";
	return JCDiagnostic.fragment(key, m, m.location(), other, other.location());
    }