    /* Return the most specific of the two methods for a call,
     *  given that both are accessible and applicable.
     *  @param m1               A new candidate for most specific.
     *  @param m2               The previous most specific candidate.
     *  @param env              The current environment.
     *  @param site             The original type from where the selection
     *                          takes place.
     *  @param allowBoxing Allow boxing conversions of arguments.
     *  @param useVarargs Box trailing arguments into an array for varargs.
     */
    Symbol mostSpecific(Symbol m1,
                        Symbol m2,
                        Env<AttrContext> env,
                        Type site,
                        boolean allowBoxing,
                        boolean useVarargs) {
		try {//我加上的
		DEBUG.P(this,"mostSpecific(6)");
		DEBUG.P("m1="+m1);
		DEBUG.P("m2="+m2);
		DEBUG.P("site="+site);
		DEBUG.P("allowBoxing="+allowBoxing);
		DEBUG.P("useVarargs="+useVarargs);
		DEBUG.P("m2.kind="+Kinds.toString(m2.kind));

        switch (m2.kind) {
        case MTH:
            if (m1 == m2) return m1;
            Type mt1 = types.memberType(site, m1);
            noteWarner.unchecked = false;
            boolean m1SignatureMoreSpecific =
                (instantiate(env, site, m2, types.lowerBoundArgtypes(mt1), null,
                             allowBoxing, false, noteWarner) != null ||
                 useVarargs && instantiate(env, site, m2, types.lowerBoundArgtypes(mt1), null,
                                           allowBoxing, true, noteWarner) != null) &&
                !noteWarner.unchecked;
            Type mt2 = types.memberType(site, m2);
            noteWarner.unchecked = false;
            boolean m2SignatureMoreSpecific =
                (instantiate(env, site, m1, types.lowerBoundArgtypes(mt2), null,
                             allowBoxing, false, noteWarner) != null ||
                 useVarargs && instantiate(env, site, m1, types.lowerBoundArgtypes(mt2), null,
                                           allowBoxing, true, noteWarner) != null) &&
                !noteWarner.unchecked;
            if (m1SignatureMoreSpecific && m2SignatureMoreSpecific) {
                if (!types.overrideEquivalent(mt1, mt2))
                    return new AmbiguityError(m1, m2);
                // same signature; select (a) the non-bridge method, or
                // (b) the one that overrides the other, or (c) the concrete
                // one, or (d) merge both abstract signatures
                if ((m1.flags() & BRIDGE) != (m2.flags() & BRIDGE)) {
                    return ((m1.flags() & BRIDGE) != 0) ? m2 : m1;
                }
                // if one overrides or hides the other, use it
                TypeSymbol m1Owner = (TypeSymbol)m1.owner;
                TypeSymbol m2Owner = (TypeSymbol)m2.owner;
                if (types.asSuper(m1Owner.type, m2Owner) != null &&
                    ((m1.owner.flags_field & INTERFACE) == 0 ||
                     (m2.owner.flags_field & INTERFACE) != 0) &&
                    m1.overrides(m2, m1Owner, types, false))
                    return m1;
                if (types.asSuper(m2Owner.type, m1Owner) != null &&
                    ((m2.owner.flags_field & INTERFACE) == 0 ||
                     (m1.owner.flags_field & INTERFACE) != 0) &&
                    m2.overrides(m1, m2Owner, types, false))
                    return m2;
                boolean m1Abstract = (m1.flags() & ABSTRACT) != 0;
                boolean m2Abstract = (m2.flags() & ABSTRACT) != 0;
                if (m1Abstract && !m2Abstract) return m2;
                if (m2Abstract && !m1Abstract) return m1;
                // both abstract or both concrete
                if (!m1Abstract && !m2Abstract)
                    return new AmbiguityError(m1, m2);
                // check for same erasure
                if (!types.isSameType(m1.erasure(types), m2.erasure(types)))
                    return new AmbiguityError(m1, m2);
                // both abstract, neither overridden; merge throws clause and result type
                Symbol result;
                Type result2 = mt2.getReturnType();;
                if (mt2.tag == FORALL)
                    result2 = types.subst(result2, ((ForAll)mt2).tvars, ((ForAll)mt1).tvars);
                if (types.isSubtype(mt1.getReturnType(), result2)) {
                    result = m1;
                } else if (types.isSubtype(result2, mt1.getReturnType())) {
                    result = m2;
                } else {
                    // Theoretically, this can't happen, but it is possible
                    // due to error recovery or mixing incompatible class files
                    return new AmbiguityError(m1, m2);
                }
                result = result.clone(result.owner);
                result.type = (Type)result.type.clone();
                result.type.setThrown(chk.intersect(mt1.getThrownTypes(),
                                                    mt2.getThrownTypes()));
                return result;
            }
            if (m1SignatureMoreSpecific) return m1;
            if (m2SignatureMoreSpecific) return m2;
            return new AmbiguityError(m1, m2);
        case AMBIGUOUS:
            AmbiguityError e = (AmbiguityError)m2;
            Symbol err1 = mostSpecific(m1, e.sym1, env, site, allowBoxing, useVarargs);
            Symbol err2 = mostSpecific(m1, e.sym2, env, site, allowBoxing, useVarargs);
            if (err1 == err2) return err1;
            if (err1 == e.sym1 && err2 == e.sym2) return m2;
            if (err1 instanceof AmbiguityError &&
                err2 instanceof AmbiguityError &&
                ((AmbiguityError)err1).sym1 == ((AmbiguityError)err2).sym1)
                return new AmbiguityError(m1, m2);
            else
                return new AmbiguityError(err1, err2);
        default:
            throw new AssertionError();
        }

		}finally{//我加上的
		DEBUG.P(0,this,"mostSpecific(6)");
		}
    }