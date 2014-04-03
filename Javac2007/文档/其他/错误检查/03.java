    /**
     * Is t the same type as s?
     */
    public boolean isSameType(Type t, Type s) {
        return isSameType.visit(t, s);
    }
    // where
        private TypeRelation isSameType = new TypeRelation() {

            public Boolean visitType(Type t, Type s) {
                if (t == s)
                    return true;

                if (s.tag >= firstPartialTag)
                    return visit(s, t);

                switch (t.tag) {
                case BYTE: case CHAR: case SHORT: case INT: case LONG: case FLOAT:
                case DOUBLE: case BOOLEAN: case VOID: case BOT: case NONE:
                    return t.tag == s.tag;
                case TYPEVAR:
                    return s.isSuperBound()
                        && !s.isExtendsBound()
                        && visit(t, upperBound(s));
                default:
                    throw new AssertionError("isSameType " + t.tag);
                }
            }

            @Override
            public Boolean visitWildcardType(WildcardType t, Type s) {
                if (s.tag >= firstPartialTag)
                    return visit(s, t);
                else
                    return false;
            }

            @Override
            public Boolean visitClassType(ClassType t, Type s) {
                if (t == s)
                    return true;

                if (s.tag >= firstPartialTag)
                    return visit(s, t);

                if (s.isSuperBound() && !s.isExtendsBound())
                    return visit(t, upperBound(s)) && visit(t, lowerBound(s));

                if (t.isCompound() && s.isCompound()) {
                    if (!visit(supertype(t), supertype(s)))
                        return false;

                    HashSet<SingletonType> set = new HashSet<SingletonType>();
                    for (Type x : interfaces(t))
                        set.add(new SingletonType(x));
                    for (Type x : interfaces(s)) {
                        if (!set.remove(new SingletonType(x)))
                            return false;
                    }
                    return (set.size() == 0);
                }
                return t.tsym == s.tsym
                    && visit(t.getEnclosingType(), s.getEnclosingType())
                    && containsTypeEquivalent(t.getTypeArguments(), s.getTypeArguments());
            }

            @Override
            public Boolean visitArrayType(ArrayType t, Type s) {
                if (t == s)
                    return true;

                if (s.tag >= firstPartialTag)
                    return visit(s, t);

                return s.tag == ARRAY
                    && containsTypeEquivalent(t.elemtype, elemtype(s));
            }

            @Override
            public Boolean visitMethodType(MethodType t, Type s) {
                // isSameType for methods does not take thrown
                // exceptions into account!
                return hasSameArgs(t, s) && visit(t.getReturnType(), s.getReturnType());
            }

            @Override
            public Boolean visitPackageType(PackageType t, Type s) {
                return t == s;
            }

            @Override
            public Boolean visitForAll(ForAll t, Type s) {
                if (s.tag != FORALL)
                    return false;

                ForAll forAll = (ForAll)s;
                return hasSameBounds(t, forAll)
                    && visit(t.qtype, subst(forAll.qtype, forAll.tvars, t.tvars));
            }

            @Override
            public Boolean visitUndetVar(UndetVar t, Type s) {
                if (s.tag == WILDCARD)
                    // FIXME, this might be leftovers from before capture conversion
                    return false;

                if (t == s || t.qtype == s || s.tag == ERROR || s.tag == UNKNOWN)
                    return true;

                if (t.inst != null)
                    return visit(t.inst, s);

                t.inst = fromUnknownFun.apply(s);
                for (List<Type> l = t.lobounds; l.nonEmpty(); l = l.tail) {
                    if (!isSubtype(l.head, t.inst))
                        return false;
                }
                for (List<Type> l = t.hibounds; l.nonEmpty(); l = l.tail) {
                    if (!isSubtype(t.inst, l.head))
                        return false;
                }
                return true;
            }

            @Override
            public Boolean visitErrorType(ErrorType t, Type s) {
                return true;
            }
        };