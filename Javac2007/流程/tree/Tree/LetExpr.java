    /** (let int x = 3; in x+2) */
    public static class LetExpr extends JCExpression {
        public List<JCVariableDecl> defs;
        public JCTree expr;
        protected LetExpr(List<JCVariableDecl> defs, JCTree expr) {
            super(LETEXPR);
            this.defs = defs;
            this.expr = expr;
        }
        @Override
        public void accept(Visitor v) { v.visitLetExpr(this); }

        public Kind getKind() {
            throw new AssertionError("TypeBoundKind is not part of a public API");
        }
        @Override
        public <R,D> R accept(TreeVisitor<R,D> v, D d) {
            throw new AssertionError("LetExpr is not part of a public API");
        }
    }