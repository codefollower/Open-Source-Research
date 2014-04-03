    public static class JCErroneous extends JCExpression
            implements com.sun.source.tree.ErroneousTree {
        public List<? extends JCTree> errs;
        protected JCErroneous(List<? extends JCTree> errs) {
            super(ERRONEOUS);
            this.errs = errs;
        }
        @Override
        public void accept(Visitor v) { v.visitErroneous(this); }

        public Kind getKind() { return Kind.ERRONEOUS; }

        public List<? extends JCTree> getErrorTrees() {
            return errs;
        }

        @Override
        public <R,D> R accept(TreeVisitor<R,D> v, D d) {
            return v.visitErroneous(this, d);
        }
    }