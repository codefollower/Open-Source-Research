    /** The current `this' symbol.
     *  @param env    The current environment.
     */
    Symbol thisSym(DiagnosticPosition pos, Env<AttrContext> env) {
		try {//我加上的
            DEBUG.P(this,"thisSym(2)");
        return rs.resolveSelf(pos, env, env.enclClass.sym, names._this);

		}finally{//我加上的
            DEBUG.P(0,this,"thisSym(2)");
        }
    }