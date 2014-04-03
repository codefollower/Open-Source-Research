//classBound
    // <editor-fold defaultstate="collapsed" desc="classBound">
    /**
     * If the given type is a (possibly selected) type variable,
     * return the bounding class of this type, otherwise return the
     * type itself.
     */
    public Type classBound(Type t) {
        //return classBound.visit(t);
        
        DEBUG.P(this,"classBound(Type t)");
        DEBUG.P("t="+t+" t.tag="+TypeTags.toString(t.tag));
        
        Type returnType=classBound.visit(t);
        
        DEBUG.P("returnType="+returnType);
        DEBUG.P(1,this,"classBound(Type t)");
        return returnType;
    }
    // where
        private UnaryVisitor<Type> classBound = new UnaryVisitor<Type>() {

            public Type visitType(Type t, Void ignored) {
                return t;
            }

            @Override
            public Type visitClassType(ClassType t, Void ignored) {
                Type outer1 = classBound(t.getEnclosingType());
                if (outer1 != t.getEnclosingType())
                    return new ClassType(outer1, t.getTypeArguments(), t.tsym);
                else
                    return t;
            }

            @Override
            public Type visitTypeVar(TypeVar t, Void ignored) {
                return classBound(supertype(t));
            }

            @Override
            public Type visitErrorType(ErrorType t, Void ignored) {
                return t;
            }
        };
    // </editor-fold>
//