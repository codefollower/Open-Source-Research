     /** Complete generating code for operation, with left operand
	 *  already on stack.
	 *  @param lhs       The tree representing the left operand.
	 *  @param rhs       The tree representing the right operand.
	 *  @param operator  The operator symbol.
	 */
	Item completeBinop(JCTree lhs, JCTree rhs, OperatorSymbol operator) {
		try {//我加上的
		DEBUG.P(this,"completeBinop(3)");
		DEBUG.P("lhs="+lhs);
		DEBUG.P("rhs="+rhs);
		DEBUG.P("operator="+operator);
		DEBUG.P("operator.opcode="+code.mnem(operator.opcode));

	    MethodType optype = (MethodType)operator.type;
	    int opcode = operator.opcode;

	    if (opcode >= if_icmpeq && opcode <= if_icmple &&
		rhs.type.constValue() instanceof Number &&
		((Number) rhs.type.constValue()).intValue() == 0) {
			//如果关系运算符右边的操作数是0，把if_icmpeq到if_icmple这6条指令
			//转换成ifeq到ifle这6条指令，这样就不用将右边的操作数0压入堆栈了
			opcode = opcode + (ifeq - if_icmpeq);
	    } else if (opcode >= if_acmpeq && opcode <= if_acmpne &&
				   TreeInfo.isNull(rhs)) {
			//如果关系运算符右边的操作数是null，把if_acmpeq转换成if_acmp_null，
			//把if_acmpne转换成if_acmp_nonnull。
			opcode = opcode + (if_acmp_null - if_acmpeq);
	    } else {
			// The expected type of the right operand is
			// the second parameter type of the operator, except for
			// shifts with long shiftcount, where we convert the opcode
			// to a short shift and the expected type to int.
			Type rtype = operator.erasure(types).getParameterTypes().tail.head;

			DEBUG.P("");
			DEBUG.P("operator.type="+operator.type);
			DEBUG.P("operator.erasure(types).getParameterTypes()="+operator.erasure(types).getParameterTypes());
			DEBUG.P("rtype="+rtype);
			if (opcode >= ishll && opcode <= lushrl) {
				//把ishll到lushrl这6条非标准指令转换成ishl到lushr这6条指令，
				opcode = opcode + (ishl - ishll);
				rtype = syms.intType;
			}

			DEBUG.P("opcode="+code.mnem(opcode));
			// Generate code for right operand and load.
			genExpr(rhs, rtype).load();
			// If there are two consecutive opcode instructions,
			// emit the first now.
			if (opcode >= (1 << preShift)) { //参考Symtab类的enterBinop方法
				code.emitop0(opcode >> preShift);
				opcode = opcode & 0xFF;
			}
	    }
	    
	    DEBUG.P("opcode="+code.mnem(opcode));
	    if (opcode >= ifeq && opcode <= if_acmpne ||
			opcode == if_acmp_null || opcode == if_acmp_nonnull) {
			return items.makeCondItem(opcode);
	    } else {
			code.emitop0(opcode);
			return items.makeStackItem(optype.restype);
	    }
	    
	    }finally{//我加上的
		DEBUG.P(0,this,"completeBinop(3)");
		}
	}