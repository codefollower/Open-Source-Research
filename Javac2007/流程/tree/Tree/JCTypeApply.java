    /**
     * A parameterized type, T<...>
     */
    public static class JCTypeApply extends JCExpression implements ParameterizedTypeTree {
        /*如果泛型类是Test<T,V>，
        那么Test的parameterized type可以是：Test<String,int>，
        对应JCTypeApply的字段如下:
        clazz=Test；
        arguments[1]=String,
        arguments[2]=int
        */
		public JCExpression clazz;
        public List<JCExpression> arguments;
        protected JCTypeApply(JCExpression clazz, List<JCExpression> arguments) {
            super(TYPEAPPLY);
            this.clazz = clazz;
            this.arguments = arguments;
        }
        @Override
        public void accept(Visitor v) { v.visitTypeApply(this); }

        public Kind getKind() { return Kind.PARAMETERIZED_TYPE; }
        public JCTree getType() { return clazz; }
        public List<JCExpression> getTypeArguments() {
            return arguments;
        }
        @Override
        public <R,D> R accept(TreeVisitor<R,D> v, D d) {
            return v.visitParameterizedType(this, d);
        }
    }