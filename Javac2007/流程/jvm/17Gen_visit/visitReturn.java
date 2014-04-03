    public void visitReturn(JCReturn tree) {
		DEBUG.P(this,"visitReturn(1)");
		DEBUG.P("tree.expr="+tree.expr);

		int limit = code.nextreg;
		final Env<GenContext> targetEnv;
		if (tree.expr != null) {
			Item r = genExpr(tree.expr, pt).load();
			if (hasFinally(env.enclMethod, env)) {
				r = makeTemp(pt);
				r.store();
			}
			targetEnv = unwind(env.enclMethod, env);
			r.load();
			code.emitop0(ireturn + Code.truncate(Code.typecode(pt)));
		} else {
			targetEnv = unwind(env.enclMethod, env);
			code.emitop0(return_);
		}
		endFinalizerGaps(env, targetEnv);
		code.endScopes(limit);

		DEBUG.P(0,this,"visitReturn(1)");
    }