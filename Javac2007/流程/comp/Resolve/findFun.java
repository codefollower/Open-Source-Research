    /** Find unqualified method matching given name, type and value arguments.
     *  @param env       The current environment.
     *  @param name      The method's name.
     *  @param argtypes  The method's value arguments.
     *  @param typeargtypes  The method's type arguments.
     *  @param allowBoxing Allow boxing conversions of arguments.
     *  @param useVarargs Box trailing arguments into an array for varargs.
     */
    Symbol findFun(Env<AttrContext> env, Name name,
                   List<Type> argtypes, List<Type> typeargtypes,
                   boolean allowBoxing, boolean useVarargs) {
        try {
        DEBUG.P(this,"findFun(6)");   
        DEBUG.P("env="+env);
        DEBUG.P("name="+name);
        //DEBUG.P("argtypes="+argtypes);
        //DEBUG.P("typeargtypes="+typeargtypes);
        DEBUG.P("allowBoxing="+allowBoxing);
        DEBUG.P("useVarargs="+useVarargs);

        
        
        Symbol bestSoFar = methodNotFound;
        Symbol sym;
        Env<AttrContext> env1 = env;
        boolean staticOnly = false;
        while (env1.outer != null) {
            if (isStatic(env1)) staticOnly = true;
            sym = findMethod(
                env1, env1.enclClass.sym.type, name, argtypes, typeargtypes,
                allowBoxing, useVarargs, false);
            if (sym.exists()) {
                if (staticOnly &&
                    sym.kind == MTH &&
                    sym.owner.kind == TYP &&
                    (sym.flags() & STATIC) == 0) return new StaticError(sym);
                else return sym;
            } else if (sym.kind < bestSoFar.kind) {
                bestSoFar = sym;
            }
            if ((env1.enclClass.sym.flags() & STATIC) != 0) staticOnly = true;
            env1 = env1.outer;
        }

        sym = findMethod(env, syms.predefClass.type, name, argtypes,
                         typeargtypes, allowBoxing, useVarargs, false);
        if (sym.exists())
            return sym;

        Scope.Entry e = env.toplevel.namedImportScope.lookup(name);
        for (; e.scope != null; e = e.next()) {
            sym = e.sym;
            Type origin = e.getOrigin().owner.type;
            if (sym.kind == MTH) {
                if (e.sym.owner.type != origin)
                    sym = sym.clone(e.getOrigin().owner);
                if (!isAccessible(env, origin, sym))
                    sym = new AccessError(env, origin, sym);
                bestSoFar = selectBest(env, origin,
                                       argtypes, typeargtypes,
                                       sym, bestSoFar,
                                       allowBoxing, useVarargs, false);
            }
        }
        if (bestSoFar.exists())
            return bestSoFar;

        e = env.toplevel.starImportScope.lookup(name);
        for (; e.scope != null; e = e.next()) {
            sym = e.sym;
            Type origin = e.getOrigin().owner.type;
            if (sym.kind == MTH) {
                if (e.sym.owner.type != origin)
                    sym = sym.clone(e.getOrigin().owner);
                if (!isAccessible(env, origin, sym))
                    sym = new AccessError(env, origin, sym);
                bestSoFar = selectBest(env, origin,
                                       argtypes, typeargtypes,
                                       sym, bestSoFar,
                                       allowBoxing, useVarargs, false);
            }
        }
        return bestSoFar;
        
        }finally{
        DEBUG.P(0,this,"findFun(6)");   
        }
    }