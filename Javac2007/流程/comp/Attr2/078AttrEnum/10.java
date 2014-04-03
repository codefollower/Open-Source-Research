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


	    /** Is symbol accessible as a member of given type in given evironment?
     *  @param env    The current environment.
     *  @param site   The type of which the tested symbol is regarded
     *                as a member.
     *  @param sym    The symbol.
     */
    public boolean isAccessible(Env<AttrContext> env, Type site, Symbol sym) {
    	try {//我加上的
		DEBUG.P(this,"isAccessible(3)");
		DEBUG.P("sym.name="+sym.name);
		
        if (sym.name == names.init && sym.owner != site.tsym) return false;
        ClassSymbol sub;
        switch ((short)(sym.flags() & AccessFlags)) {
        case PRIVATE:
            return
                (env.enclClass.sym == sym.owner // fast special case
                 ||
                 env.enclClass.sym.outermostClass() ==
                 sym.owner.outermostClass())
                &&
                sym.isInheritedIn(site.tsym, types);
        case 0:
            return
                (env.toplevel.packge == sym.owner.owner // fast special case
                 ||
                 env.toplevel.packge == sym.packge())
                &&
                isAccessible(env, site)
                &&
                sym.isInheritedIn(site.tsym, types);
        case PROTECTED:
            return
                (env.toplevel.packge == sym.owner.owner // fast special case
                 ||
                 env.toplevel.packge == sym.packge()
                 ||
                 isProtectedAccessible(sym, env.enclClass.sym, site)
                 ||
                 // OK to select instance method or field from 'super' or type name
                 // (but type names should be disallowed elsewhere!)
                 env.info.selectSuper && (sym.flags() & STATIC) == 0 && sym.kind != TYP)
                &&
                isAccessible(env, site)
                &&
                // `sym' is accessible only if not overridden by
                // another symbol which is a member of `site'
                // (because, if it is overridden, `sym' is not strictly
                // speaking a member of `site'.)
                (sym.kind != MTH || sym.isConstructor() ||
                 ((MethodSymbol)sym).implementation(site.tsym, types, true) == sym);
        default: // this case includes erroneous combinations as well
            return isAccessible(env, site);
        }
        
        
        }finally{//我加上的
		DEBUG.P(0,this,"isAccessible(3)");
		}
    }