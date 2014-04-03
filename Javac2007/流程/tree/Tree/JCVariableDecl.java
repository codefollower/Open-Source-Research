    /**
     * A variable definition.
     * @param modifiers variable modifiers
     * @param name variable name
     * @param vartype type of the variable
     * @param init variables initial value
     * @param sym symbol
     */
    public static class JCVariableDecl extends JCStatement implements VariableTree {
        public JCModifiers mods;
        public Name name;
        public JCExpression vartype;
        public JCExpression init;
        public VarSymbol sym;
        protected JCVariableDecl(JCModifiers mods,
			 Name name,
			 JCExpression vartype,
			 JCExpression init,
			 VarSymbol sym) {
            super(VARDEF);
            this.mods = mods;
            this.name = name;
            this.vartype = vartype;
            this.init = init;
            this.sym = sym;
        }
        @Override
        public void accept(Visitor v) { v.visitVarDef(this); }

        public Kind getKind() { return Kind.VARIABLE; }
        public JCModifiers getModifiers() { return mods; }
        public Name getName() { return name; }
        public JCTree getType() { return vartype; }
        public JCExpression getInitializer() {
            return init;
        }
        @Override
        public <R,D> R accept(TreeVisitor<R,D> v, D d) {
            return v.visitVariable(this, d);
        }
    }