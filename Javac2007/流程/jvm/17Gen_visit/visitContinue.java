    public void visitContinue(JCContinue tree) {
		DEBUG.P(this,"visitContinue(1)");
		DEBUG.P("tree.label="+tree.label);
		DEBUG.P("tree.target="+tree.target);

        Env<GenContext> targetEnv = unwind(tree.target, env);
		assert code.state.stacksize == 0;
		targetEnv.info.addCont(code.branch(goto_));
		endFinalizerGaps(env, targetEnv);

		DEBUG.P(0,this,"visitContinue(1)");
    }