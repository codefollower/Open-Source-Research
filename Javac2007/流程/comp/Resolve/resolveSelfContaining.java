    /**
     * Resolve `c.this' for an enclosing class c that contains the
     * named member.
     * @param pos           The position to use for error reporting.
     * @param env           The environment current at the expression.
     * @param member        The member that must be contained in the result.
     */
    Symbol resolveSelfContaining(DiagnosticPosition pos,
                                 Env<AttrContext> env,
                                 Symbol member) {
		try {//我加上的
		DEBUG.P(this,"resolveSelfContaining(3)");
		DEBUG.P("member="+member);

        Name name = names._this;
        Env<AttrContext> env1 = env;
        boolean staticOnly = false;
        while (env1.outer != null) {
            if (isStatic(env1)) staticOnly = true;
            if (env1.enclClass.sym.isSubClass(member.owner, types) &&
                isAccessible(env, env1.enclClass.sym.type, member)) {
                Symbol sym = env1.info.scope.lookup(name).sym;
                if (sym != null) {
                    if (staticOnly) sym = new StaticError(sym);
                    return access(sym, pos, env.enclClass.sym.type,
                                  name, true);
                }
            }
            if ((env1.enclClass.sym.flags() & STATIC) != 0)
                staticOnly = true;
            env1 = env1.outer;
        }
        log.error(pos, "encl.class.required", member);
        return syms.errSymbol;

		}finally{//我加上的
		DEBUG.P(0,this,"resolveSelfContaining(3)");
		}
    }