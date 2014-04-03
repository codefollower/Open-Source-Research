    public static class ArrayType extends Type
            implements javax.lang.model.type.ArrayType {

        public Type elemtype;

        public ArrayType(Type elemtype, TypeSymbol arrayClass) {
            super(ARRAY, arrayClass);
            this.elemtype = elemtype;
        }

        @Override
        public <R,S> R accept(Type.Visitor<R,S> v, S s) {
            return v.visitArrayType(this, s);
        }

        public String toString() {
            return elemtype + "[]";
        }

        public boolean equals(Object obj) {
            return
                this == obj ||
                (obj instanceof ArrayType &&
                 this.elemtype.equals(((ArrayType)obj).elemtype));
        }

        public int hashCode() {
            return (ARRAY << 5) + elemtype.hashCode();
        }

        public List<Type> allparams() { return elemtype.allparams(); }

        public boolean isErroneous() {
            return elemtype.isErroneous();
        }

        public boolean isParameterized() {
            return elemtype.isParameterized();
        }

        public boolean isRaw() {
            return elemtype.isRaw();
        }

        public Type map(Mapping f) {
            Type elemtype1 = f.apply(elemtype);
            if (elemtype1 == elemtype) return this;
            else return new ArrayType(elemtype1, tsym);
        }

        public boolean contains(Type elem) {
            return elem == this || elemtype.contains(elem);
        }

        public void complete() {
            elemtype.complete();
        }

        public Type getComponentType() {
            return elemtype;
        }

        public TypeKind getKind() {
            return TypeKind.ARRAY;
        }

        public <R, P> R accept(TypeVisitor<R, P> v, P p) {
            return v.visitArray(this, p);
        }
    }