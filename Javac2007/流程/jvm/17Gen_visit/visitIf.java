    public void visitIf(JCIf tree) {
		DEBUG.P(this,"visitIf(1)");
		int limit = code.nextreg;
		Chain thenExit = null;

		DEBUG.P("limit="+limit);
		//在genCond也调用了TreeInfo.skipParens，这里重复了
		DEBUG.P("tree.cond="+tree.cond);
		CondItem c = genCond(TreeInfo.skipParens(tree.cond),
					 CRT_FLOW_CONTROLLER);
		
		DEBUG.P("c="+c);
		Chain elseChain = c.jumpFalse();

		DEBUG.P("elseChain="+elseChain);
		DEBUG.P("c.isFalse()="+c.isFalse());
		if (!c.isFalse()) {
			code.resolve(c.trueJumps);
			genStat(tree.thenpart, env, CRT_STATEMENT | CRT_FLOW_TARGET);
			thenExit = code.branch(goto_);
		}
		if (elseChain != null) {
			code.resolve(elseChain);
			if (tree.elsepart != null)
				genStat(tree.elsepart, env,CRT_STATEMENT | CRT_FLOW_TARGET);
		}
		code.resolve(thenExit);
		code.endScopes(limit);
		DEBUG.P(0,this,"visitIf(1)");
    }