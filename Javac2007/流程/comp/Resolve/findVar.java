    /** Find unqualified variable or field with given name.
     *  Synthetic fields always skipped.
     *  @param env     The current environment.
     *  @param name    The name of the variable or field.
     */
    Symbol findVar(Env<AttrContext> env, Name name) {
		try {
    	DEBUG.P(this,"findVar(2)");
		DEBUG.P("env="+env);
    	DEBUG.P("name="+name);

        Symbol bestSoFar = varNotFound;
        Symbol sym;
        Env<AttrContext> env1 = env;
        boolean staticOnly = false;
        while (env1.outer != null) {
            if (isStatic(env1)) staticOnly = true;
            Scope.Entry e = env1.info.scope.lookup(name);
			DEBUG.P("staticOnly="+staticOnly);
			DEBUG.P("e.scope="+e.scope);
            while (e.scope != null &&
                   (e.sym.kind != VAR ||
                    (e.sym.flags_field & SYNTHETIC) != 0))
                e = e.next();
            sym = (e.scope != null)
                ? e.sym
                : findField(
                    env1, env1.enclClass.sym.type, name, env1.enclClass.sym);
            DEBUG.P("sym.exists()="+sym.exists());
			DEBUG.P("sym.flags() ="+Flags.toString(sym.flags()));
			if (sym.exists()) {
                if (staticOnly &&
                    sym.kind == VAR &&
                    sym.owner.kind == TYP &&
                    (sym.flags() & STATIC) == 0)
                    return new StaticError(sym);
                else
                    return sym;
            } else if (sym.kind < bestSoFar.kind) {
                bestSoFar = sym;
            }
			/*
				class VisitSelectTest {
					int a1;
					//env1.enclClass.sym.flags() & STATIC) != 0)==TRUE
					static class C1_1 {
						int a2=a1; //无法从静态上下文中引用非静态 变量 a1
						class C1_1_1 {
							//static int a2=a1; //内部类不能有静态声明
							int a2=a1; //无法从静态上下文中引用非静态 变量 a1
						}
					}
				}
			*/
            if ((env1.enclClass.sym.flags() & STATIC) != 0) staticOnly = true;
            env1 = env1.outer;
			DEBUG.P("staticOnly="+staticOnly);
        }

        sym = findField(env, syms.predefClass.type, name, syms.predefClass);
        DEBUG.P("sym.exists()="+sym.exists());
		if (sym.exists())
            return sym;
		DEBUG.P("bestSoFar.exists()="+bestSoFar.exists());
        if (bestSoFar.exists())
            return bestSoFar;

        Scope.Entry e = env.toplevel.namedImportScope.lookup(name);
		DEBUG.P("e.scope="+e.scope);
        for (; e.scope != null; e = e.next()) {
            sym = e.sym;
            Type origin = e.getOrigin().owner.type;
            if (sym.kind == VAR) {
                if (e.sym.owner.type != origin)
                    sym = sym.clone(e.getOrigin().owner);
                return isAccessible(env, origin, sym)
                    ? sym : new AccessError(env, origin, sym);
            }
        }

        Symbol origin = null;
        e = env.toplevel.starImportScope.lookup(name);
		DEBUG.P("e.scope="+e.scope);
        for (; e.scope != null; e = e.next()) {
            sym = e.sym;
            if (sym.kind != VAR)
                continue;
            // invariant: sym.kind == VAR
            if (bestSoFar.kind < AMBIGUOUS && sym.owner != bestSoFar.owner)
                return new AmbiguityError(bestSoFar, sym);
            else if (bestSoFar.kind >= VAR) {
                origin = e.getOrigin().owner;
                bestSoFar = isAccessible(env, origin.type, sym)
                    ? sym : new AccessError(env, origin.type, sym);
            }
        }
        if (bestSoFar.kind == VAR && bestSoFar.owner.type != origin.type)
            return bestSoFar.clone(origin);
        else
            return bestSoFar;

		}finally{
    	DEBUG.P(0,this,"findVar(2)");
    	}
    }