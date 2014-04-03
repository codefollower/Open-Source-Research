    /** An environment is "static" if its static level is greater than
     *  the one of its outer environment
     */
    static boolean isStatic(Env<AttrContext> env) {
        return env.info.staticLevel > env.outer.info.staticLevel;
    }

	/** Find an unqualified type symbol.
     *  @param env       The current environment.
     *  @param name      The type's name.
     */
    Symbol findType(Env<AttrContext> env, Name name) {
    	Symbol bestSoFar = typeNotFound;
        Symbol sym;
        
    	try{
    	DEBUG.P(this,"findType(2)");
    	DEBUG.P("name="+name);
    	DEBUG.P("env="+env);
    	DEBUG.P("env.outer="+env.outer);
    	
    	
        
        boolean staticOnly = false;
        for (Env<AttrContext> env1 = env; env1.outer != null; env1 = env1.outer) {
        	DEBUG.P("env1.info.staticLevel="+env1.info.staticLevel);
        	DEBUG.P("env1.outer.info.staticLevel="+env1.outer.info.staticLevel);
            if (isStatic(env1)) staticOnly = true;
            DEBUG.P("staticOnly="+staticOnly);
            DEBUG.P("env1.info.scope="+env1.info.scope);
            for (Scope.Entry e = env1.info.scope.lookup(name);
                 e.scope != null;
                 e = e.next()) {
                DEBUG.P("e.sym="+e.sym);
            	DEBUG.P("e.sym.kind="+Kinds.toString(e.sym.kind));
                if (e.sym.kind == TYP) {
                	DEBUG.P("e.sym.type.tag="+TypeTags.toString(e.sym.type.tag));
                	DEBUG.P("e.sym.owner="+e.sym.owner);
            		DEBUG.P("e.sym.owner.kind="+Kinds.toString(e.sym.owner.kind));
            		
            		//错误提示:无法从静态上下文中引用非静态 类型变量的限制范围
                    if (staticOnly &&
                        e.sym.type.tag == TYPEVAR &&
                        e.sym.owner.kind == TYP) return new StaticError(e.sym);
                    return e.sym;
                }
            }
			

            sym = findMemberType(env1, env1.enclClass.sym.type, name,
                                 env1.enclClass.sym);
            if (staticOnly && sym.kind == TYP &&
                sym.type.tag == CLASS &&
                sym.type.getEnclosingType().tag == CLASS &&
                env1.enclClass.sym.type.isParameterized() &&
                sym.type.getEnclosingType().isParameterized())
                return new StaticError(sym);
            else if (sym.exists()) return sym;
            else if (sym.kind < bestSoFar.kind) bestSoFar = sym;

            JCClassDecl encl = env1.baseClause ? (JCClassDecl)env1.tree : env1.enclClass;
            if ((encl.sym.flags() & STATIC) != 0)
                staticOnly = true;
        }
        
        DEBUG.P("env.tree.tag="+env.tree.myTreeTag());
        if (env.tree.tag != JCTree.IMPORT) {
            sym = findGlobalType(env, env.toplevel.namedImportScope, name);
            if (sym.exists()) return sym;
            else if (sym.kind < bestSoFar.kind) bestSoFar = sym;

            sym = findGlobalType(env, env.toplevel.packge.members(), name);
            DEBUG.P("sym="+sym);
            DEBUG.P("sym.exists()="+sym.exists());
            if (sym.exists()) return sym;
            else if (sym.kind < bestSoFar.kind) bestSoFar = sym;

            sym = findGlobalType(env, env.toplevel.starImportScope, name);
            if (sym.exists()) return sym;
            else if (sym.kind < bestSoFar.kind) bestSoFar = sym;
        }
        
        return bestSoFar;
        
        
    	}finally{
    	//DEBUG.P("env.toplevel.namedImportScope="+env.toplevel.namedImportScope);
    	//DEBUG.P("env.toplevel.packge.members()="+env.toplevel.packge.members());
    	//DEBUG.P("env.toplevel.starImportScope="+env.toplevel.starImportScope);
    	//DEBUG.P("Symbol bestSoFar="+bestSoFar);
    	DEBUG.P("bestSoFar.kind="+Kinds.toString(bestSoFar.kind));
    	DEBUG.P(0,this,"findType(2)");
    	}
    }