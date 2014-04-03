    /**
     * A type cast.
     */
    public static class JCTypeCast extends JCExpression implements TypeCastTree {
        public JCTree clazz;
        public JCExpression expr;
        protected JCTypeCast(JCTree clazz, JCExpression expr) {
            super(TYPECAST);
            this.clazz = clazz;
            this.expr = expr;
        }
        @Override
        public void accept(Visitor v) { v.visitTypeCast(this); }

        public Kind getKind() { return Kind.TYPE_CAST; }
        public JCTree getType() { return clazz; }
        public JCExpression getExpression() { return expr; }
        @Override
        public <R,D> R accept(TreeVisitor<R,D> v, D d) {
            return v.visitTypeCast(this, d);
        }
    }