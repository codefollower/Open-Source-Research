    /** Find a global type in given scope and load corresponding class.
     *  @param env       The current environment.
     *  @param scope     The scope in which to look for the type.
     *  @param name      The type's name.
     */
    Symbol findGlobalType(Env<AttrContext> env, Scope scope, Name name) {
		DEBUG.P(this,"findGlobalType(3)");
		DEBUG.P("env="+env);
    	DEBUG.P("scope="+scope);
		DEBUG.P("name="+name);

        Symbol bestSoFar = typeNotFound;
        for (Scope.Entry e = scope.lookup(name); e.scope != null; e = e.next()) {
            Symbol sym = loadClass(env, e.sym.flatName());
            
            DEBUG.P("bestSoFar.kind="+Kinds.toString(bestSoFar.kind));
            DEBUG.P("sym.kind="+Kinds.toString(sym.kind));
            
            if (bestSoFar.kind == TYP && sym.kind == TYP &&
                bestSoFar != sym)
                return new AmbiguityError(bestSoFar, sym);
            else if (sym.kind < bestSoFar.kind)
                bestSoFar = sym;
        }

		DEBUG.P("bestSoFar="+bestSoFar);
		DEBUG.P(0,this,"findGlobalType(3)");
        return bestSoFar;
    }
