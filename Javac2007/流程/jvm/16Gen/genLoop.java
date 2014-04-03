    //where
        /** Generate code for a loop.
		 *  @param loop       The tree representing the loop.
		 *  @param body       The loop's body.
		 *  @param cond       The loop's controling condition.
		 *  @param step       "Step" statements to be inserted at end of
		 *                    each iteration.
		 *  @param testFirst  True if the loop test belongs before the body.
		 */
        private void genLoop(JCStatement loop,
			     JCStatement body,
			     JCExpression cond,
			     List<JCExpressionStatement> step,
			     boolean testFirst) {
			DEBUG.P(this,"genLoop(1)");	 
			DEBUG.P("cond="+cond);
			DEBUG.P("testFirst="+testFirst);
					
			Env<GenContext> loopEnv = env.dup(loop, new GenContext());
			int startpc = code.entryPoint();
			if (testFirst) {
				CondItem c;
				if (cond != null) {
					code.statBegin(cond.pos);
					c = genCond(TreeInfo.skipParens(cond), CRT_FLOW_CONTROLLER);
				} else {
					c = items.makeCondItem(goto_);
				}
				Chain loopDone = c.jumpFalse();
				code.resolve(c.trueJumps);
				genStat(body, loopEnv, CRT_STATEMENT | CRT_FLOW_TARGET);
				code.resolve(loopEnv.info.cont);
				genStats(step, loopEnv);
				code.resolve(code.branch(goto_), startpc);
				code.resolve(loopDone);
			} else {
				genStat(body, loopEnv, CRT_STATEMENT | CRT_FLOW_TARGET);
				code.resolve(loopEnv.info.cont);
				genStats(step, loopEnv);
				CondItem c;
				if (cond != null) {
					code.statBegin(cond.pos);
					c = genCond(TreeInfo.skipParens(cond), CRT_FLOW_CONTROLLER);
				} else {
					c = items.makeCondItem(goto_);
				}
				//do-while语句生成的字节码比while语句生成的字节码高效，因为少了goto指令
				code.resolve(c.jumpTrue(), startpc);
				code.resolve(c.falseJumps);
			}
			code.resolve(loopEnv.info.exit);
			DEBUG.P(0,this,"genLoop(1)");	
		}