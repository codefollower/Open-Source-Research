    public static class PackageType extends Type implements NoType {

        PackageType(TypeSymbol tsym) {
            super(PACKAGE, tsym);
        }

        @Override
        public <R,S> R accept(Type.Visitor<R,S> v, S s) {
            return v.visitPackageType(this, s);
        }

        public String toString() {
            return tsym.getQualifiedName().toString();
        }

        public TypeKind getKind() {
            return TypeKind.PACKAGE;
        }

        public <R, P> R accept(TypeVisitor<R, P> v, P p) {
            return v.visitNoType(this, p);
        }
    }