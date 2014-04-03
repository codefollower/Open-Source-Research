    /** Select the best method for a call site among two choices.
     *  @param env              The current environment.
     *  @param site             The original type from where the
     *                          selection takes place.
     *  @param argtypes         The invocation's value arguments,
     *  @param typeargtypes     The invocation's type arguments,
     *  @param sym              Proposed new best match.
     *  @param bestSoFar        Previously found best match.
     *  @param allowBoxing Allow boxing conversions of arguments.
     *  @param useVarargs Box trailing arguments into an array for varargs.
     */
    Symbol selectBest(Env<AttrContext> env,
                      Type site,
                      List<Type> argtypes,
                      List<Type> typeargtypes,
                      Symbol sym,
                      Symbol bestSoFar,
                      boolean allowBoxing,
                      boolean useVarargs,
                      boolean operator) {
		try {//我加上的
		DEBUG.P(this,"selectBest(9)");
		DEBUG.P("env="+env);
		DEBUG.P("site="+site);
		DEBUG.P("argtypes="+argtypes);
		DEBUG.P("typeargtypes="+typeargtypes);
		DEBUG.P("sym="+sym);
		DEBUG.P("bestSoFar="+bestSoFar);
		DEBUG.P("allowBoxing="+allowBoxing);
		DEBUG.P("useVarargs="+useVarargs);
		DEBUG.P("operator="+operator);

		DEBUG.P("sym.kind="+Kinds.toString(sym.kind));

        if (sym.kind == ERR) return bestSoFar;
        if (!sym.isInheritedIn(site.tsym, types)) return bestSoFar;

		DEBUG.P("sym.kind="+Kinds.toString(sym.kind));
        assert sym.kind < AMBIGUOUS;
        try {
            if (rawInstantiate(env, site, sym, argtypes, typeargtypes,
                               allowBoxing, useVarargs, Warner.noWarnings) == null) {
                // inapplicable
				DEBUG.P("bestSoFar.kind="+Kinds.toString(bestSoFar.kind));
                switch (bestSoFar.kind) {
                case ABSENT_MTH: return wrongMethod.setWrongSym(sym);
                case WRONG_MTH: return wrongMethods;
                default: return bestSoFar;
                }
            }
        } catch (Infer.NoInstanceException ex) {
            switch (bestSoFar.kind) {
            case ABSENT_MTH:
                return wrongMethod.setWrongSym(sym, ex.getDiagnostic());
            case WRONG_MTH:
                return wrongMethods;
            default:
                return bestSoFar;
            }
        }
        if (!isAccessible(env, site, sym)) {
            return (bestSoFar.kind == ABSENT_MTH)
                ? new AccessError(env, site, sym)
                : bestSoFar;
        }

		DEBUG.P("bestSoFar.kind="+Kinds.toString(bestSoFar.kind));
        return (bestSoFar.kind > AMBIGUOUS)
            ? sym
            : mostSpecific(sym, bestSoFar, env, site,
                           allowBoxing && operator, useVarargs);

		}finally{//我加上的
		DEBUG.P(0,this,"selectBest(9)");
		}
    }