    public void visitConditional(JCConditional tree) {
		DEBUG.P(this,"visitConditional(1)");	
		Chain thenExit = null;
		CondItem c = genCond(tree.cond, CRT_FLOW_CONTROLLER);
		Chain elseChain = c.jumpFalse();
		if (!c.isFalse()) {
			code.resolve(c.trueJumps);
			int startpc = genCrt ? code.curPc() : 0;
			genExpr(tree.truepart, pt).load();
			code.state.forceStackTop(tree.type);
			if (genCrt) code.crt.put(tree.truepart, CRT_FLOW_TARGET,
						 startpc, code.curPc());
			thenExit = code.branch(goto_);
		}
		if (elseChain != null) {
			code.resolve(elseChain);
			int startpc = genCrt ? code.curPc() : 0;
			genExpr(tree.falsepart, pt).load();
			code.state.forceStackTop(tree.type);
			if (genCrt) code.crt.put(tree.falsepart, CRT_FLOW_TARGET,
						 startpc, code.curPc());
		}
		code.resolve(thenExit);
		result = items.makeStackItem(pt);
		DEBUG.P(0,this,"visitConditional(1)");
    }