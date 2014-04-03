    /**
     * A method invocation
     */
     
    //注意看看MethodInvocationTree里的注释才好理解
    public static class JCMethodInvocation extends JCExpression implements MethodInvocationTree {
        public List<JCExpression> typeargs;
        public JCExpression meth;
        public List<JCExpression> args;
        public Type varargsElement;
        protected JCMethodInvocation(List<JCExpression> typeargs,
			JCExpression meth,
			List<JCExpression> args)
	{
	    super(APPLY);
	    this.typeargs = (typeargs == null) ? List.<JCExpression>nil()
		                               : typeargs;
            this.meth = meth;
            this.args = args;
        }
        @Override
        public void accept(Visitor v) { v.visitApply(this); }

        public Kind getKind() { return Kind.METHOD_INVOCATION; }
        public List<JCExpression> getTypeArguments() {
            return typeargs;
        }
        public JCExpression getMethodSelect() { return meth; }
        public List<JCExpression> getArguments() {
            return args;
        }
        @Override
        public <R,D> R accept(TreeVisitor<R,D> v, D d) {
            return v.visitMethodInvocation(this, d);
        }
        @Override
        public JCMethodInvocation setType(Type type) {
            super.setType(type);
            return this;
        }
    }