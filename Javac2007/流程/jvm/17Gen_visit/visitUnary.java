    public void visitUnary(JCUnary tree) {
		DEBUG.P(this,"visitUnary(1)");
		OperatorSymbol operator = (OperatorSymbol)tree.operator;
		DEBUG.P("tree.tag="+tree.myTreeTag());
		if (tree.tag == JCTree.NOT) {
			CondItem od = genCond(tree.arg, false);
			result = od.negate();
		} else {
			Item od = genExpr(tree.arg, operator.type.getParameterTypes().head);
			DEBUG.P("od="+od);
			DEBUG.P("tree.tag="+tree.myTreeTag());
			DEBUG.P("operator.opcode="+code.mnem(operator.opcode));
			
			switch (tree.tag) {
				case JCTree.POS:
					result = od.load();
					break;
				case JCTree.NEG:
					result = od.load();
					code.emitop0(operator.opcode);
					break;
				case JCTree.COMPL:
					result = od.load();
					emitMinusOne(od.typecode);
					code.emitop0(operator.opcode);
					break;
				case JCTree.PREINC: case JCTree.PREDEC:
					od.duplicate();
					if (od instanceof LocalItem &&
						(operator.opcode == iadd || operator.opcode == isub)) {
						((LocalItem)od).incr(tree.tag == JCTree.PREINC ? 1 : -1);
						result = od;
					} else {
						od.load();
						code.emitop0(one(od.typecode));
						code.emitop0(operator.opcode);
						// Perform narrowing primitive conversion if byte,
						// char, or short.  Fix for 4304655.
						if (od.typecode != INTcode &&
							Code.truncate(od.typecode) == INTcode)
							code.emitop0(int2byte + od.typecode - BYTEcode);
							result = items.makeAssignItem(od);
					}
					break;
				case JCTree.POSTINC: case JCTree.POSTDEC:
					od.duplicate();
					if (od instanceof LocalItem && 
								(operator.opcode == iadd || operator.opcode == isub)) {
						Item res = od.load();
						((LocalItem)od).incr(tree.tag == JCTree.POSTINC ? 1 : -1);
						result = res;
					} else {
						Item res = od.load();
						od.stash(od.typecode);
						code.emitop0(one(od.typecode));
						code.emitop0(operator.opcode);
						// Perform narrowing primitive conversion if byte,
						// char, or short.  Fix for 4304655.
						if (od.typecode != INTcode &&
						Code.truncate(od.typecode) == INTcode)
							code.emitop0(int2byte + od.typecode - BYTEcode);
						od.store();
						result = res;
					}
					break;
				case JCTree.NULLCHK:
					result = od.load();
					code.emitop0(dup);
					genNullCheck(tree.pos());
					break;
				default:
					assert false;
			}
		}
		DEBUG.P(0,this,"visitUnary(1)");
    }
