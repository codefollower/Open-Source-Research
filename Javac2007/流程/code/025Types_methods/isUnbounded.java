//isUnbounded
    // <editor-fold defaultstate="collapsed" desc="isUnbounded">
    /**
     * Checks that all the arguments to a class are unbounded
     * wildcards or something else that doesn't make any restrictions
     * on the arguments. If a class isUnbounded, a raw super- or
     * subclass can be cast to it without a warning.
     * @param t a type
     * @return true iff the given type is unbounded or raw
     */
    public boolean isUnbounded(Type t) {
        return isUnbounded.visit(t);
    }
    // where
        private final UnaryVisitor<Boolean> isUnbounded = new UnaryVisitor<Boolean>() {

            public Boolean visitType(Type t, Void ignored) {
                return true;
            }

            @Override
            public Boolean visitClassType(ClassType t, Void ignored) {
                List<Type> parms = t.tsym.type.allparams();//类型变量
                List<Type> args = t.allparams();
                
                DEBUG.P("");DEBUG.P(this,"isUnbounded.visitClassType(2)");
                DEBUG.P("parms="+parms);
                DEBUG.P("args="+args);
                DEBUG.P("");
                
                while (parms.nonEmpty()) {
                    WildcardType unb = new WildcardType(syms.objectType,
                                                        BoundKind.UNBOUND,
                                                        syms.boundClass,
                                                        (TypeVar)parms.head);
                    if (!containsType(args.head, unb))
                        return false;
                    parms = parms.tail;
                    args = args.tail;
                }
                return true;
            }
        };
    // </editor-fold>
//