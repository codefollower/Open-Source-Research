    /**
     * A new(...) operation.
     */
    //如EnumeratorDeclaration也有用JCNewClass表示的地方，参考Parser的enumeratorDeclaration(Name enumName)
    public static class JCNewClass extends JCExpression implements NewClassTree {
        public JCExpression encl;//对应NewClassTree中的enclosingExpression
        public List<JCExpression> typeargs;//对应NewClassTree中的typeArguments
        public JCExpression clazz;//对应NewClassTree中的identifier
        public List<JCExpression> args;//对应NewClassTree中的arguments
        public JCClassDecl def;//对应NewClassTree中的classBody
        public Symbol constructor;
        public Type varargsElement;
        protected JCNewClass(JCExpression encl,
			   List<JCExpression> typeargs,
			   JCExpression clazz,
			   List<JCExpression> args,
			   JCClassDecl def)
	{
            super(NEWCLASS);
            this.encl = encl;
	    this.typeargs = (typeargs == null) ? List.<JCExpression>nil()
		                               : typeargs;
            this.clazz = clazz;
            this.args = args;
            this.def = def;
        }
        @Override
        public void accept(Visitor v) { v.visitNewClass(this); }

        public Kind getKind() { return Kind.NEW_CLASS; }
        public JCExpression getEnclosingExpression() { // expr.new C< ... > ( ... )
            return encl;
        }
        public List<JCExpression> getTypeArguments() {
            return typeargs;
        }
        public JCExpression getIdentifier() { return clazz; }
        public List<JCExpression> getArguments() {
            return args;
        }
        public JCClassDecl getClassBody() { return def; }
        @Override
        public <R,D> R accept(TreeVisitor<R,D> v, D d) {
            return v.visitNewClass(this, d);
        }
    }