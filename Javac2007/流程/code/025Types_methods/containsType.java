//Contains Type
    // <editor-fold defaultstate="collapsed" desc="Contains Type">
    public boolean containedBy(Type t, Type s) {
        switch (t.tag) {
        case UNDETVAR:
            if (s.tag == WILDCARD) {
                UndetVar undetvar = (UndetVar)t;

                // Because of wildcard capture, s must be on the left
                // hand side of an assignment.  Furthermore, t is an
                // underconstrained type variable, for example, one
                // that is only used in the return type of a method.
                // If the type variable is truly underconstrained, it
                // cannot have any low bounds:
                assert undetvar.lobounds.isEmpty() : undetvar;

                undetvar.inst = glb(upperBound(s), undetvar.inst);
                return true;
            } else {
                return isSameType(t, s);
            }
        case ERROR:
            return true;
        default:
            return containsType(s, t);
        }
    }

    boolean containsType(List<Type> ts, List<Type> ss) {
		DEBUG.P(this,"containsType(2)");
		DEBUG.P("ts="+ts);
		DEBUG.P("ss="+ss);
		
        while (ts.nonEmpty() && ss.nonEmpty()
               && containsType(ts.head, ss.head)) {
            ts = ts.tail;
            ss = ss.tail;
        }
        //return ts.isEmpty() && ss.isEmpty();
        boolean returnResult = ts.isEmpty() && ss.isEmpty();
        DEBUG.P("returnResult="+returnResult);
        DEBUG.P(1,this,"containsType(2)");
        return returnResult;
    }

    /**
     * Check if t contains s.
     *
     * <p>T contains S if:
     *
     * <p>{@code L(T) <: L(S) && U(S) <: U(T)}
     *
     * <p>This relation is only used by ClassType.isSubtype(), that
     * is,
     *
     * <p>{@code C<S> <: C<T> if T contains S.}
     *
     * <p>Because of F-bounds, this relation can lead to infinite
     * recursion.  Thus we must somehow break that recursion.  Notice
     * that containsType() is only called from ClassType.isSubtype().
     * Since the arguments have already been checked against their
     * bounds, we know:
     *
     * <p>{@code U(S) <: U(T) if T is "super" bound (U(T) *is* the bound)}
     *
     * <p>{@code L(T) <: L(S) if T is "extends" bound (L(T) is bottom)}
     *
     * @param t a type
     * @param s a type
     */
    public boolean containsType(Type t, Type s) {
        //return containsType.visit(t, s);
        
        DEBUG.P(this,"containsType(Type t, Type s)");
		DEBUG.P("t="+t+"  t.tag="+TypeTags.toString(t.tag));
		DEBUG.P("s="+s+"  s.tag="+TypeTags.toString(s.tag));
		
		boolean returnResult= containsType.visit(t, s);
            
		DEBUG.P("returnResult="+returnResult);
		DEBUG.P(0,this,"containsType(Type t, Type s)");
		return returnResult;
    }
    // where
        private TypeRelation containsType = new TypeRelation() {

            private Type U(Type t) {
                while (t.tag == WILDCARD) {
                    WildcardType w = (WildcardType)t;
                    if (w.isSuperBound())
                        return w.bound == null ? syms.objectType : w.bound.bound;
                    else
                        t = w.type;
                }
                return t;
            }

            private Type L(Type t) {
                while (t.tag == WILDCARD) {
                    WildcardType w = (WildcardType)t;
                    if (w.isExtendsBound())
                        return syms.botType;
                    else
                        t = w.type;
                }
                return t;
            }

            public Boolean visitType(Type t, Type s) {
                if (s.tag >= firstPartialTag)
                    return containedBy(s, t);
                else
                    return isSameType(t, s);
            }

            void debugContainsType(WildcardType t, Type s) {
                System.err.println();
                System.err.format(" does %s contain %s?%n", t, s);
                System.err.format(" %s U(%s) <: U(%s) %s = %s%n",
                                  upperBound(s), s, t, U(t),
                                  t.isSuperBound()
                                  || isSubtypeNoCapture(upperBound(s), U(t)));
                System.err.format(" %s L(%s) <: L(%s) %s = %s%n",
                                  L(t), t, s, lowerBound(s),
                                  t.isExtendsBound()
                                  || isSubtypeNoCapture(L(t), lowerBound(s)));
                System.err.println();
            }

            @Override
            public Boolean visitWildcardType(WildcardType t, Type s) {
                if (s.tag >= firstPartialTag)
                    return containedBy(s, t);
                else {
                    // debugContainsType(t, s);
                    return isSameWildcard(t, s)
                        || isCaptureOf(s, t)
                        || ((t.isExtendsBound() || isSubtypeNoCapture(L(t), lowerBound(s))) &&
                            (t.isSuperBound() || isSubtypeNoCapture(upperBound(s), U(t))));
                }
            }

            @Override
            public Boolean visitUndetVar(UndetVar t, Type s) {
                if (s.tag != WILDCARD)
                    return isSameType(t, s);
                else
                    return false;
            }

            @Override
            public Boolean visitErrorType(ErrorType t, Type s) {
                return true;
            }
        };

    public boolean isCaptureOf(Type s, WildcardType t) {
        if (s.tag != TYPEVAR || !(s instanceof CapturedType))
            return false;
        return isSameWildcard(t, ((CapturedType)s).wildcard);
    }

    public boolean isSameWildcard(WildcardType t, Type s) {
        if (s.tag != WILDCARD)
            return false;
        WildcardType w = (WildcardType)s;
        return w.kind == t.kind && w.type == t.type;
    }

    public boolean containsTypeEquivalent(List<Type> ts, List<Type> ss) {
		DEBUG.P(this,"containsTypeEquivalent(2)");
		DEBUG.P("ts="+ts);
		DEBUG.P("ss="+ss);
		
        while (ts.nonEmpty() && ss.nonEmpty()
               && containsTypeEquivalent(ts.head, ss.head)) {
            ts = ts.tail;
            ss = ss.tail;
        }
        //return ts.isEmpty() && ss.isEmpty();
		
		boolean returnResult=ts.isEmpty() && ss.isEmpty();
		DEBUG.P("returnResult="+returnResult);
		DEBUG.P(0,this,"containsTypeEquivalent(2)");
		return returnResult;
    }
    // </editor-fold>
//
	private boolean containsTypeEquivalent(Type t, Type s) {
    	DEBUG.P(this,"containsTypeEquivalent(Type t, Type s)");
		DEBUG.P("t="+t);
		DEBUG.P("s="+s);
		
        //return
        //    isSameType(t, s) || // shortcut
        //    containsType(t, s) && containsType(s, t);
        
        boolean returnResult=isSameType(t, s) || // shortcut
            containsType(t, s) && containsType(s, t);
        DEBUG.P("returnResult="+returnResult);      
        DEBUG.P(0,this,"containsTypeEquivalent(Type t, Type s)");
        return returnResult;
    }