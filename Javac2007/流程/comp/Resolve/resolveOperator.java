    /** Resolve operator.
     *  @param pos       The position to use for error reporting.
     *  @param optag     The tag of the operation tree.
     *  @param env       The environment current at the operation.
     *  @param argtypes  The types of the operands.
     */
    Symbol resolveOperator(DiagnosticPosition pos, int optag,
                           Env<AttrContext> env, List<Type> argtypes) {
		try {
    	DEBUG.P(this,"resolveOperator(4)");
    	DEBUG.P("argtypes="+argtypes);

        Name name = treeinfo.operatorName(optag);

		DEBUG.P("name="+name);

        Symbol sym = findMethod(env, syms.predefClass.type, name, argtypes,
                                null, false, false, true);
        if (boxingEnabled && sym.kind >= WRONG_MTHS)
            sym = findMethod(env, syms.predefClass.type, name, argtypes,
                             null, true, false, true);
        return access(sym, pos, env.enclClass.sym.type, name,
                      false, argtypes, null);

		}finally{
    	DEBUG.P(0,this,"resolveOperator(4)");
    	}
    }

    /** Resolve operator.
     *  @param pos       The position to use for error reporting.
     *  @param optag     The tag of the operation tree.
     *  @param env       The environment current at the operation.
     *  @param arg       The type of the operand.
     */
    Symbol resolveUnaryOperator(DiagnosticPosition pos, int optag, Env<AttrContext> env, Type arg) {
        return resolveOperator(pos, optag, env, List.of(arg));
    }

    /** Resolve binary operator.
     *  @param pos       The position to use for error reporting.
     *  @param optag     The tag of the operation tree.
     *  @param env       The environment current at the operation.
     *  @param left      The types of the left operand.
     *  @param right     The types of the right operand.
     */
    Symbol resolveBinaryOperator(DiagnosticPosition pos,
                                 int optag,
                                 Env<AttrContext> env,
                                 Type left,
                                 Type right) {
        return resolveOperator(pos, optag, env, List.of(left, right));
    }