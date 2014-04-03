    public static abstract class DelegatedType extends Type {
        public Type qtype;
        public DelegatedType(int tag, Type qtype) {
            super(tag, qtype.tsym);
            this.qtype = qtype;
        }
        public String toString() { return qtype.toString(); }
        public List<Type> getTypeArguments() { return qtype.getTypeArguments(); }
        public Type getEnclosingType() { return qtype.getEnclosingType(); }
        public List<Type> getParameterTypes() { return qtype.getParameterTypes(); }
        public Type getReturnType() { return qtype.getReturnType(); }
        public List<Type> getThrownTypes() { return qtype.getThrownTypes(); }
        public List<Type> allparams() { return qtype.allparams(); }
        public Type getUpperBound() { return qtype.getUpperBound(); }
        public Object clone() { DelegatedType t = (DelegatedType)super.clone(); t.qtype = (Type)qtype.clone(); return t; }
        public boolean isErroneous() { return qtype.isErroneous(); }
    }