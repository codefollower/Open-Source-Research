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
            		
					/*错误例子:
					bin\mysrc\my\test\Test.java:28: 无法从静态上下文中引用非静态 类型变量的限制范围T
							public static <M extends T,S> int[] myMethod(final M m,S[] s[],int i,Str
					ing s2,int... ii)[] throws Exception,Error{
													 ^
					这里的错误提示位置有点怪，虽然错误是在static方法myMethod中引用非静态 类型变量T，
					但错误提示位置是在Exception，而不是在<M extends T>下
					*/
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


	    /** Find qualified member type.
     *  @param env       The current environment.
     *  @param site      The original type from where the selection takes
     *                   place.
     *  @param name      The type's name.
     *  @param c         The class to search for the member type. This is
     *                   always a superclass or implemented interface of
     *                   site's class.
     */
    Symbol findMemberType(Env<AttrContext> env,
                          Type site,
                          Name name,
                          TypeSymbol c) {
		DEBUG.P(this,"findMemberType(4)");
		DEBUG.P("env="+env);
		DEBUG.P("Type site="+site);
		DEBUG.P("site.tag="+TypeTags.toString(site.tag));
		DEBUG.P("Name name="+name);
		DEBUG.P("TypeSymbol c="+c);
		DEBUG.P("c.completer="+c.completer);

		/*注释里的内容是我加的
		if(c!=null) {
			DEBUG.P("c.getClass()="+c.getClass());
			if(c instanceof PackageSymbol)
				DEBUG.P("c.members_field="+((PackageSymbol)c).members_field);
			else if(c instanceof ClassSymbol) {
			 	DEBUG.P("c instanceof ClassSymbol");
			 	ClassSymbol myClassSymbol=(ClassSymbol)c;
			 	//DEBUG.P("myClassSymbol.members_field="+myClassSymbol.members_field);
			 	Scope myScope=myClassSymbol.members_field;
			 	DEBUG.P("myScope="+myScope);
			}
		}
		//下面这行很怪异,会调用complete()
		//DEBUG.P("c.members()="+c.members());
		//而下面这行却不会调用complete()
		//Scope.Entry e = c.members().lookup(name);
		*/
		
        Symbol bestSoFar = typeNotFound;
        Symbol sym;
        
        Scope.Entry e = c.members().lookup(name);
		DEBUG.P("e.scope="+e.scope);
        while (e.scope != null) {
            if (e.sym.kind == TYP) {
                return isAccessible(env, site, e.sym)
                    ? e.sym
                    : new AccessError(env, site, e.sym);
            }
            e = e.next();
        }
        Type st = types.supertype(c.type);

		DEBUG.P("st="+st);
		DEBUG.P("st.tag="+TypeTags.toString(st.tag));

        if (st != null && st.tag == CLASS) {
            sym = findMemberType(env, site, name, st.tsym);
            if (sym.kind < bestSoFar.kind) bestSoFar = sym;
        }
        
        DEBUG.P("bestSoFar.kind != AMBIGUOUS="+(bestSoFar.kind != AMBIGUOUS));
        
        for (List<Type> l = types.interfaces(c.type);
             bestSoFar.kind != AMBIGUOUS && l.nonEmpty();
             l = l.tail) {
            sym = findMemberType(env, site, name, l.head.tsym);
            if (bestSoFar.kind < AMBIGUOUS && sym.kind < AMBIGUOUS &&
                sym.owner != bestSoFar.owner)
                bestSoFar = new AmbiguityError(bestSoFar, sym);
            else if (sym.kind < bestSoFar.kind)
                bestSoFar = sym;
        }
		DEBUG.P("bestSoFar="+bestSoFar);
		DEBUG.P(0,this,"findMemberType(4)");
        return bestSoFar;
    }


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