    public void visitLabelled(JCLabeledStatement tree) {
    DEBUG.P(this,"visitLabelled(1)");	
	Env<GenContext> localEnv = env.dup(tree, new GenContext());
	genStat(tree.body, localEnv, CRT_STATEMENT);
	code.resolve(localEnv.info.exit);
	DEBUG.P(0,this,"visitLabelled(1)");	
    }