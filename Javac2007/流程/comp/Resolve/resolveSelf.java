    /**
     * Resolve `c.name' where name == this or name == super.
     * @param pos           The position to use for error reporting.
     * @param env           The environment current at the expression.
     * @param c             The qualifier.
     * @param name          The identifier's name.
     */
    Symbol resolveSelf(DiagnosticPosition pos,
                       Env<AttrContext> env,
                       TypeSymbol c,
                       Name name) {
		try {//我加上的
		DEBUG.P(this,"resolveSelf(4)");
		DEBUG.P("c="+c);
		DEBUG.P("name="+name);

        Env<AttrContext> env1 = env;
        boolean staticOnly = false;
        while (env1.outer != null) {
            if (isStatic(env1)) staticOnly = true;
			DEBUG.P("staticOnly="+staticOnly);
			DEBUG.P("env1.enclClass.sym="+env1.enclClass.sym);
            if (env1.enclClass.sym == c) {
                Symbol sym = env1.info.scope.lookup(name).sym;
				DEBUG.P("sym="+sym);
				DEBUG.P("env1.info.scope="+env1.info.scope);
                if (sym != null) {
                    if (staticOnly) sym = new StaticError(sym);
                    return access(sym, pos, env.enclClass.sym.type,
                                  name, true);
                }
            }
            if ((env1.enclClass.sym.flags() & STATIC) != 0) staticOnly = true;
            env1 = env1.outer;
        }
		DEBUG.P("在"+c+"中找不到"+name);
        log.error(pos, "not.encl.class", c);
        return syms.errSymbol;

		}finally{//我加上的
		DEBUG.P(0,this,"resolveSelf(4)");
		}
    }