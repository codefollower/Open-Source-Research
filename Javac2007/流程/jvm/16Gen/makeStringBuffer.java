//where
        /** Make a new string buffer.
	 */
        void makeStringBuffer(DiagnosticPosition pos) {
		DEBUG.P(this,"makeStringBuffer(1)");

	    code.emitop2(new_, makeRef(pos, stringBufferType));
	    code.emitop0(dup);
	    callMethod(
		pos, stringBufferType, names.init, List.<Type>nil(), false);

		DEBUG.P(0,this,"makeStringBuffer(1)");
	}

        /** Append value (on tos) to string buffer (on tos - 1).
	 */
        void appendString(JCTree tree) {
		DEBUG.P(this,"appendString(1)");
		DEBUG.P("tree="+tree);

	    Type t = tree.type.baseType();
	    if (t.tag > lastBaseTag && t.tsym != syms.stringType.tsym) {
		t = syms.objectType;
	    }
	    items.makeMemberItem(getStringBufferAppend(tree, t), false).invoke();

		DEBUG.P(0,this,"appendString(1)");
	}
        Symbol getStringBufferAppend(JCTree tree, Type t) {
		DEBUG.P(this,"getStringBufferAppend(2)");
		DEBUG.P("tree="+tree);
		DEBUG.P("t="+t);

	    assert t.constValue() == null;
	    Symbol method = stringBufferAppend.get(t);

		DEBUG.P("method="+method);

	    if (method == null) {
		method = rs.resolveInternalMethod(tree.pos(),
						  attrEnv,
						  stringBufferType,
						  names.append,
						  List.of(t),
						  null);
		stringBufferAppend.put(t, method);
	    }
		
		DEBUG.P("method="+method);
		DEBUG.P(0,this,"getStringBufferAppend(2)");
	    return method;
	}

        /** Add all strings in tree to string buffer.
	 */
        void appendStrings(JCTree tree) {
		try {//我加上的
		DEBUG.P(this,"appendStrings(1)");

	    tree = TreeInfo.skipParens(tree);

		DEBUG.P("tree="+tree);
		DEBUG.P("tree.tag="+tree.myTreeTag());

	    if (tree.tag == JCTree.PLUS && tree.type.constValue() == null) {
		JCBinary op = (JCBinary) tree;
		if (op.operator.kind == MTH &&
		    ((OperatorSymbol) op.operator).opcode == string_add) {
		    appendStrings(op.lhs);
		    appendStrings(op.rhs);
		    return;
		}
	    }
	    genExpr(tree, tree.type).load();
	    appendString(tree);

		}finally{//我加上的
		DEBUG.P(0,this,"appendStrings(2)");
		}
	}

        /** Convert string buffer on tos to string.
	 */
        void bufferToString(DiagnosticPosition pos) {
		DEBUG.P(this,"bufferToString(1)");
	    callMethod(
		pos,
		stringBufferType,
		names.toString,
		List.<Type>nil(),
		false);
		DEBUG.P(0,this,"bufferToString(1)");
	}