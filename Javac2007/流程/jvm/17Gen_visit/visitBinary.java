    public void visitBinary(JCBinary tree) {
		DEBUG.P(this,"visitBinary(1)");
        OperatorSymbol operator = (OperatorSymbol)tree.operator;

		DEBUG.P("tree.tag="+tree.myTreeTag());
		DEBUG.P("operator.opcode="+code.mnem(operator.opcode));
		if (operator.opcode == string_add) {
			// Create a string buffer.
			makeStringBuffer(tree.pos());
			// Append all strings to buffer.
			appendStrings(tree);
			// Convert buffer to string.
			bufferToString(tree.pos());
			result = items.makeStackItem(syms.stringType);
		} else if (tree.tag == JCTree.AND) {
			CondItem lcond = genCond(tree.lhs, CRT_FLOW_CONTROLLER);
			if (!lcond.isFalse()) {
				Chain falseJumps = lcond.jumpFalse();
				code.resolve(lcond.trueJumps);
				CondItem rcond = genCond(tree.rhs, CRT_FLOW_TARGET);
				result = items.
					makeCondItem(rcond.opcode,
						 rcond.trueJumps,
						 code.mergeChains(falseJumps,
								  rcond.falseJumps));
			} else {
				result = lcond;
			}
		} else if (tree.tag == JCTree.OR) {
			CondItem lcond = genCond(tree.lhs, CRT_FLOW_CONTROLLER);
			if (!lcond.isTrue()) {
				Chain trueJumps = lcond.jumpTrue();
				code.resolve(lcond.falseJumps);
				CondItem rcond = genCond(tree.rhs, CRT_FLOW_TARGET);
				result = items.
					makeCondItem(rcond.opcode,
						 code.mergeChains(trueJumps, rcond.trueJumps),
						 rcond.falseJumps);
			} else {
				result = lcond;
			}
		} else {
			Item od = genExpr(tree.lhs, operator.type.getParameterTypes().head);
			od.load();
			result = completeBinop(tree.lhs, tree.rhs, operator);
		}
		DEBUG.P(0,this,"visitBinary(1)");
    }