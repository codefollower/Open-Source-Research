    /** Derived visitor method: check whether CharacterRangeTable
     *  should be emitted, if so, put a new entry into CRTable
     *  and call method to generate bytecode.
     *  If not, just call method to generate bytecode.
     *  @see    #genCond(Tree,boolean)
     *
     *  @param  tree     The tree to be visited.
     *  @param  crtFlags The CharacterRangeTable flags
     *                   indicating type of the entry.
     */
    public CondItem genCond(JCTree tree, int crtFlags) {
		try {//我加上的
		DEBUG.P(this,"genCond(2)");
		DEBUG.P("genCrt="+genCrt);
		if(code.crt!=null) DEBUG.P("crtFlags="+code.crt.getTypes(crtFlags));
		
		if (!genCrt) return genCond(tree, false);
		int startpc = code.curPc();
		CondItem item = genCond(tree, (crtFlags & CRT_FLOW_CONTROLLER) != 0);
		code.crt.put(tree, crtFlags, startpc, code.curPc());
		return item;
		
		}finally{//我加上的
		DEBUG.P(0,this,"genCond(2)");
		}
    }

    /** Derived visitor method: generate code for a boolean
     *  expression in a control-flow context.
     *  @param _tree         The expression to be visited.
     *  @param markBranches The flag to indicate that the condition is
     *                      a flow controller so produced conditions
     *                      should contain a proper tree to generate
     *                      CharacterRangeTable branches for them.
     */
    public CondItem genCond(JCTree _tree, boolean markBranches) {
		try {//我加上的
		DEBUG.P(this,"genCond(JCTree _tree, boolean markBranches)");
		DEBUG.P("markBranches="+markBranches);
		DEBUG.P("_tree="+_tree);
		
		JCTree inner_tree = TreeInfo.skipParens(_tree);
		DEBUG.P("inner_tree="+_tree);
		DEBUG.P("inner_tree.tag="+inner_tree.myTreeTag());

		if (inner_tree.tag == JCTree.CONDEXPR) {
			JCConditional tree = (JCConditional)inner_tree;
			CondItem cond = genCond(tree.cond, CRT_FLOW_CONTROLLER);
			
			DEBUG.P("cond="+cond);
			DEBUG.P("cond.isTrue() ="+cond.isTrue());
			DEBUG.P("cond.isFalse()="+cond.isFalse());
			if (cond.isTrue()) {
				code.resolve(cond.trueJumps);
				CondItem result = genCond(tree.truepart, CRT_FLOW_TARGET);
				if (markBranches) result.tree = tree.truepart;
				return result;
			}
			if (cond.isFalse()) {
				code.resolve(cond.falseJumps);
				CondItem result = genCond(tree.falsepart, CRT_FLOW_TARGET);
				if (markBranches) result.tree = tree.falsepart;
				return result;
			}

			Chain secondJumps = cond.jumpFalse();
			DEBUG.P("secondJumps="+secondJumps);

			code.resolve(cond.trueJumps);
			CondItem first = genCond(tree.truepart, CRT_FLOW_TARGET);
			if (markBranches) first.tree = tree.truepart;
			DEBUG.P("first="+first);

			Chain falseJumps = first.jumpFalse();
			DEBUG.P("falseJumps="+falseJumps);

			code.resolve(first.trueJumps);
			Chain trueJumps = code.branch(goto_);
			DEBUG.P("trueJumps="+trueJumps);

			code.resolve(secondJumps);
			CondItem second = genCond(tree.falsepart, CRT_FLOW_TARGET);
			DEBUG.P("second="+second);
			CondItem result = items.makeCondItem(second.opcode,
						  code.mergeChains(trueJumps, second.trueJumps),
						  code.mergeChains(falseJumps, second.falseJumps));
			if (markBranches) result.tree = tree.falsepart;
			return result;
		} else {
			CondItem result = genExpr(_tree, syms.booleanType).mkCond();
			if (markBranches) result.tree = _tree;
			
			DEBUG.P("result="+result);
			return result;
		}
		
		}finally{//我加上的
		DEBUG.P(0,this,"genCond(JCTree _tree, boolean markBranches)");
		}
    }