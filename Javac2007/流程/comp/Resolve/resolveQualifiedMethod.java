    /** Resolve a qualified method identifier
     *  @param pos       The position to use for error reporting.
     *  @param env       The environment current at the method invocation.
     *  @param site      The type of the qualifying expression, in which
     *                   identifier is searched.
     *  @param name      The identifier's name.
     *  @param argtypes  The types of the invocation's value arguments.
     *  @param typeargtypes  The types of the invocation's type arguments.
     */
    Symbol resolveQualifiedMethod(DiagnosticPosition pos, Env<AttrContext> env,
                                  Type site, Name name, List<Type> argtypes,
                                  List<Type> typeargtypes) {
		DEBUG.P(this,"resolveQualifiedMethod(6)");   
        DEBUG.P("env="+env);
        DEBUG.P("site="+site);
		DEBUG.P("name="+name);
        DEBUG.P("argtypes="+argtypes);
        DEBUG.P("typeargtypes="+typeargtypes);

        Symbol sym = findMethod(env, site, name, argtypes, typeargtypes, false,
                                env.info.varArgs=false, false);
        if (varargsEnabled && sym.kind >= WRONG_MTHS) {
            sym = findMethod(env, site, name, argtypes, typeargtypes, true,
                             false, false);
            if (sym.kind >= WRONG_MTHS)
                sym = findMethod(env, site, name, argtypes, typeargtypes, true,
                                 env.info.varArgs=true, false);
        }
        if (sym.kind >= AMBIGUOUS) {
            sym = access(sym, pos, site, name, true, argtypes, typeargtypes);
        }

		DEBUG.P("sym="+sym+" sym.kind="+Kinds.toString(sym.kind));
		DEBUG.P(0,this,"resolveQualifiedMethod(6)");
        return sym;
    }