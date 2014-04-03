    /** Find an identifier among the members of a given type `site'.
     *  @param env       The current environment.
     *  @param site      The type containing the symbol to be found.
     *  @param name      The identifier's name.
     *  @param kind      Indicates the possible symbol kinds
     *                   (a subset of VAL, TYP).
     */
    Symbol findIdentInType(Env<AttrContext> env, Type site,
                           Name name, int kind) {
        try {//我加上的
		DEBUG.P(this,"findIdentInType(4)");
		DEBUG.P("site="+site);
		DEBUG.P("name="+name);
        DEBUG.P("kind="+Kinds.toString(kind));
                  
        Symbol bestSoFar = typeNotFound;
        Symbol sym;
        if ((kind & VAR) != 0) {
            sym = findField(env, site, name, site.tsym);
            if (sym.exists()) return sym;
            else if (sym.kind < bestSoFar.kind) bestSoFar = sym;
        }

        if ((kind & TYP) != 0) {
            sym = findMemberType(env, site, name, site.tsym);
            if (sym.exists()) return sym;
            else if (sym.kind < bestSoFar.kind) bestSoFar = sym;
        }
        return bestSoFar;
        
        }finally{//我加上的
		DEBUG.P(0,this,"findIdentInType(4)");
		}
    }