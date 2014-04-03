    /**
     * Selects through packages and classes
     * @param selected selected Tree hierarchie
     * @param selector name of field to select thru
     * @param sym symbol of the selected class
     */
   
    //参见Parser类的public JCExpression qualident()中的注释
    public static class JCFieldAccess extends JCExpression implements MemberSelectTree {
        public JCExpression selected;
        public Name name;
        public Symbol sym;
        protected JCFieldAccess(JCExpression selected, Name name, Symbol sym) {
            super(SELECT);
            this.selected = selected;
            this.name = name;
            this.sym = sym;
        }
        @Override
        public void accept(Visitor v) { v.visitSelect(this); }

        public Kind getKind() { return Kind.MEMBER_SELECT; }
        public JCExpression getExpression() { return selected; }
        @Override
        public <R,D> R accept(TreeVisitor<R,D> v, D d) {
            return v.visitMemberSelect(this, d);
        }
        public Name getIdentifier() { return name; }
    }