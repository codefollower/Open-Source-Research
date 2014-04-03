    /**
     * An identifier
     * @param idname the name
     * @param sym the symbol
     */
    public static class JCIdent extends JCExpression implements IdentifierTree {
        public Name name;
        public Symbol sym;
        protected JCIdent(Name name, Symbol sym) {
            super(IDENT);
            this.name = name;
            this.sym = sym;
        }
        @Override
        public void accept(Visitor v) { v.visitIdent(this); }

        public Kind getKind() { return Kind.IDENTIFIER; }
        public Name getName() { return name; }
        @Override
        public <R,D> R accept(TreeVisitor<R,D> v, D d) {
            return v.visitIdentifier(this, d);
        }
    }