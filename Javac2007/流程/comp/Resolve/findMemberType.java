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

		问题已解决:因为在调用到Scope.toString()方法时，Scope.Entry.sym有
		可能有MethodSymbol，而调用MethodSymbol.toString()会触
		发对MethodSymbol的complete()，相反调用其它种类的Symbol(如ClassSymbol)
		的toString()方法不会触发它的complete()
		*/
		DEBUG.P("c.members()="+c.members());
		
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
			DEBUG.P("sym.kind="+Kinds.toString(sym.kind));
			DEBUG.P("bestSoFar.kind="+Kinds.toString(bestSoFar.kind));
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