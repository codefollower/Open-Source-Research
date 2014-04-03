    public static class ForAll extends DelegatedType
            implements Cloneable, ExecutableType {
        public List<Type> tvars;//一般是TypeParameters
        //qtype一般是MethodType
        public ForAll(List<Type> tvars, Type qtype) {
            super(FORALL, qtype);
            this.tvars = tvars;
        }

        @Override
        public <R,S> R accept(Type.Visitor<R,S> v, S s) {
            return v.visitForAll(this, s);
        }

        public String toString() {
            return "<" + tvars + ">" + qtype;
        }

        //为了区分getTypeArguments()与getParameterTypes()哪个是类型变量哪个是
        //方法参数，只要看字符串“Type”是在前还是在后，在前就是类型变量，在
        //后就是方法参数
        public List<Type> getTypeArguments()   { return tvars; }

        public void setThrown(List<Type> t) {
            qtype.setThrown(t);
        }

        public Object clone() {
            ForAll result = (ForAll)super.clone();
            result.qtype = (Type)result.qtype.clone();
            return result;
        }

        public boolean isErroneous()  {
            return qtype.isErroneous();
        }

        public Type map(Mapping f) {
            return f.apply(qtype);
        }

        public boolean contains(Type elem) {
            return qtype.contains(elem);
        }

        public MethodType asMethodType() {
            return qtype.asMethodType();
        }

        public void complete() {
            for (List<Type> l = tvars; l.nonEmpty(); l = l.tail) {
                ((TypeVar)l.head).bound.complete();
            }
            qtype.complete();
        }

        public List<TypeVar> getTypeVariables() {
            return List.convert(TypeVar.class, getTypeArguments());
        }

        public TypeKind getKind() {
            return TypeKind.EXECUTABLE;
        }

        public <R, P> R accept(TypeVisitor<R, P> v, P p) {
            return v.visitExecutable(this, p);
        }
    }