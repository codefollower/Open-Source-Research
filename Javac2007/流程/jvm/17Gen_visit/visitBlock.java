    public void visitBlock(JCBlock tree) {
		DEBUG.P(this,"visitBlock(JCBlock tree)");
		int limit = code.nextreg;
		DEBUG.P("limit="+limit);
		
		Env<GenContext> localEnv = env.dup(tree, new GenContext());
		genStats(tree.stats, localEnv);
		// End the scope of all block-local variables in variable info.

		DEBUG.P("");
		DEBUG.P("env.tree.tag="+env.tree.myTreeTag());
		if (env.tree.tag != JCTree.METHODDEF) {
            code.statBegin(tree.endpos);
            code.endScopes(limit);
            code.pendingStatPos = Position.NOPOS;
        }
		DEBUG.P(0,this,"visitBlock(JCBlock tree)");   
    }