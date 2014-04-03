    public void visitAssignop(JCAssignOp tree) {
		DEBUG.P(this,"visitAssignop(1)");
		OperatorSymbol operator = (OperatorSymbol) tree.operator;
		DEBUG.P("operator.opcode="+code.mnem(operator.opcode));
		DEBUG.P("tree="+tree);
		DEBUG.P("tree.tag="+tree.myTreeTag());
		
		Item l;
		if (operator.opcode == string_add) {
			// Generate code to make a string buffer
			makeStringBuffer(tree.pos());

			// Generate code for first string, possibly save one
			// copy under buffer
			l = genExpr(tree.lhs, tree.lhs.type);
			DEBUG.P("Item l="+l);
			DEBUG.P("l.width()="+l.width());
			if (l.width() > 0) {
				code.emitop0(dup_x1 + 3 * (l.width() - 1));
			}

			// Load first string and append to buffer.
			l.load();
			appendString(tree.lhs);

			// Append all other strings to buffer.
			appendStrings(tree.rhs);

			// Convert buffer to string.
			bufferToString(tree.pos());
		} else {
			// Generate code for first expression
			l = genExpr(tree.lhs, tree.lhs.type);

			// If we have an increment of -32768 to +32767 of a local
			// int variable we can use an incr instruction instead of
			// proceeding further.
			if ((tree.tag == JCTree.PLUS_ASG || tree.tag == JCTree.MINUS_ASG) &&
			l instanceof LocalItem &&
			tree.lhs.type.tag <= INT &&
			tree.rhs.type.tag <= INT &&
			tree.rhs.type.constValue() != null) {
				int ival = ((Number) tree.rhs.type.constValue()).intValue();
				if (tree.tag == JCTree.MINUS_ASG) ival = -ival;
				((LocalItem)l).incr(ival);
				result = l;
				return;
			}
			// Otherwise, duplicate expression, load one copy
			// and complete binary operation.
			l.duplicate();
			l.coerce(operator.type.getParameterTypes().head).load();
			completeBinop(tree.lhs, tree.rhs, operator).coerce(tree.lhs.type);
		}
		result = items.makeAssignItem(l);
		DEBUG.P(0,this,"visitAssignop(1)");
    }