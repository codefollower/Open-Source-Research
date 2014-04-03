	@Override
        public void visitWildcard(JCWildcard tree) {
        DEBUG.P(this,"visitWildcard(1)");
        DEBUG.P("tree="+tree);
	    if (tree.inner != null)
		validate(tree.inner);
		DEBUG.P(0,this,"visitWildcard(1)");
	}