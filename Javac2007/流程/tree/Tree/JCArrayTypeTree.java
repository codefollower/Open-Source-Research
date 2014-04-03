    /**
     * An array type, A[]
     */
    public static class JCArrayTypeTree extends JCExpression implements ArrayTypeTree {
        public JCExpression elemtype;
        protected JCArrayTypeTree(JCExpression elemtype) {
            super(TYPEARRAY);
            this.elemtype = elemtype;
        }
        @Override
        public void accept(Visitor v) { v.visitTypeArray(this); }

        public Kind getKind() { return Kind.ARRAY_TYPE; }
        public JCTree getType() { return elemtype; }
        @Override
        public <R,D> R accept(TreeVisitor<R,D> v, D d) {
            return v.visitArrayType(this, d);
        }
    }