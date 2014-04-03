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
        return containsType.visit(t, s);
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