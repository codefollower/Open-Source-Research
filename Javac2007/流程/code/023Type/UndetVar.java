    /** A class for instantiatable variables, for use during type
     *  inference.
     */
    public static class UndetVar extends DelegatedType {
        public List<Type> lobounds = List.nil();
        public List<Type> hibounds = List.nil();
        public Type inst = null;

        @Override
        public <R,S> R accept(Type.Visitor<R,S> v, S s) {
            return v.visitUndetVar(this, s);
        }
        
        //只在Infer.java Types.java文件中各调用new UndetVar()
        public UndetVar(Type origin) {
            super(UNDETVAR, origin);
        }

        public String toString() {
            if (inst != null) return inst.toString();
            else return qtype + "?";
        }

        public Type baseType() {
            if (inst != null) return inst.baseType();
            else return this;
        }
    }