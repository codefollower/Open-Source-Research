    /** Return the first method in t2 that conflicts with a method from t1. */
    private Symbol firstDirectIncompatibility(Type t1, Type t2, Type site) {
	for (Scope.Entry e1 = t1.tsym.members().elems; e1 != null; e1 = e1.sibling) {
	    Symbol s1 = e1.sym;
	    Type st1 = null;
	    if (s1.kind != MTH || !s1.isInheritedIn(site.tsym, types)) continue;
            Symbol impl = ((MethodSymbol)s1).implementation(site.tsym, types, false);
            if (impl != null && (impl.flags() & ABSTRACT) == 0) continue;
	    for (Scope.Entry e2 = t2.tsym.members().lookup(s1.name); e2.scope != null; e2 = e2.next()) {
		Symbol s2 = e2.sym;
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
    }

	    /** Is this symbol inherited into a given class?
     *  PRE: If symbol's owner is a interface,
     *       it is already assumed that the interface is a superinterface
     *       of given class.
     *  @param clazz  The class for which we want to establish membership.
     *                This must be a subclass of the member's owner.
     */
    public boolean isInheritedIn(Symbol clazz, Types types) {
        switch ((int)(flags_field & Flags.AccessFlags)) {
        default: // error recovery
        case PUBLIC:
            return true;
        case PRIVATE:
            return this.owner == clazz;
        case PROTECTED:
            // we model interfaces as extending Object
            return (clazz.flags() & INTERFACE) == 0;
        case 0:
            PackageSymbol thisPackage = this.packge();
            for (Symbol sup = clazz;
                 sup != null && sup != this.owner;
                 sup = types.supertype(sup.type).tsym) {
                if (sup.type.isErroneous())
                    return true; // error recovery
                if ((sup.flags() & COMPOUND) != 0)
                    continue;
                if (sup.packge() != thisPackage)
                    return false;
            }
            return (clazz.flags() & INTERFACE) == 0;
        }
    }