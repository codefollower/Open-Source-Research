    /** Generate code for a catch clause.
	 *  @param tree     The catch clause.
	 *  @param env      The environment current in the enclosing try.
	 *  @param startpc  Start pc of try-block.
	 *  @param endpc    End pc of try-block.
	 */
    void genCatch(JCCatch tree,
		      Env<GenContext> env,
		      int startpc, int endpc,
		      List<Integer> gaps) {
		DEBUG.P(this,"genCatch(4)");
		DEBUG.P("startpc="+startpc);
		DEBUG.P("endpc="+endpc);
		DEBUG.P("gaps="+gaps);

	    if (startpc != endpc) {
			int catchType = makeRef(tree.pos(), tree.param.type);
			while (gaps.nonEmpty()) {
				int end = gaps.head.intValue();
				registerCatch(tree.pos(),
					  startpc,  end, code.curPc(),
					  catchType);
				gaps = gaps.tail;
				startpc = gaps.head.intValue();
				gaps = gaps.tail;
			}
			DEBUG.P("startpc="+startpc);
			DEBUG.P("endpc="+endpc);
			if (startpc < endpc)
				registerCatch(tree.pos(),
					  startpc, endpc, code.curPc(),
					  catchType);
			VarSymbol exparam = tree.param.sym;
			DEBUG.P("exparam="+exparam);
			code.statBegin(tree.pos);
			code.markStatBegin();
			int limit = code.nextreg;
			int exlocal = code.newLocal(exparam);
			items.makeLocalItem(exparam).store();
			code.statBegin(TreeInfo.firstStatPos(tree.body));
			genStat(tree.body, env, CRT_BLOCK);
			code.endScopes(limit);
			code.statBegin(TreeInfo.endPos(tree.body));
	    }
	    DEBUG.P(0,this,"genCatch(4)");
	}