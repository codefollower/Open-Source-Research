//hashCode
    // <editor-fold defaultstate="collapsed" desc="hashCode">
    /**
     * Compute a hash code on a type.
     */
    public static int hashCode(Type t) {
        return hashCode.visit(t);
    }
    // where
        private static final UnaryVisitor<Integer> hashCode = new UnaryVisitor<Integer>() {

            public Integer visitType(Type t, Void ignored) {
                return t.tag;
            }

            @Override
            public Integer visitClassType(ClassType t, Void ignored) {
                int result = visit(t.getEnclosingType());
                result *= 127;
                result += t.tsym.flatName().hashCode();
                for (Type s : t.getTypeArguments()) {
                    result *= 127;
                    result += visit(s);
                }
                return result;
            }

            @Override
            public Integer visitWildcardType(WildcardType t, Void ignored) {
                int result = t.kind.hashCode();
                if (t.type != null) {
                    result *= 127;
                    result += visit(t.type);
                }
                return result;
            }

            @Override
            public Integer visitArrayType(ArrayType t, Void ignored) {
                return visit(t.elemtype) + 12;
            }

            @Override
            public Integer visitTypeVar(TypeVar t, Void ignored) {
                return System.identityHashCode(t.tsym);
            }

            @Override
            public Integer visitUndetVar(UndetVar t, Void ignored) {
                return System.identityHashCode(t);
            }

            @Override
            public Integer visitErrorType(ErrorType t, Void ignored) {
                return 0;
            }
        };
    // </editor-fold>
//