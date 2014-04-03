    //where
        /** Determine symbol referenced by a Select expression,
         *
         *  @param tree   The select tree.
         *  @param site   The type of the selected expression,
         *  @param env    The current environment.
         *  @param pt     The current prototype.
         *  @param pkind  The expected kind(s) of the Select expression.
         */
        private Symbol selectSym(JCFieldAccess tree,
                                 Type site,
                                 Env<AttrContext> env,
                                 Type pt,
                                 int pkind) {
            try {//我加上的
			DEBUG.P(this,"selectSym(5)");
			DEBUG.P("tree="+tree);
			DEBUG.P("site="+site); 
			DEBUG.P("site.tag="+TypeTags.toString(site.tag));   
			DEBUG.P("env="+env);
			DEBUG.P("pt="+pt); 
			DEBUG.P("pt.tag="+TypeTags.toString(pt.tag));
			DEBUG.P("pkind="+Kinds.toString(pkind));
    	
			                 	
            DiagnosticPosition pos = tree.pos();
            Name name = tree.name;

            switch (site.tag) {
            case PACKAGE:
                return rs.access(
                    rs.findIdentInPackage(env, site.tsym, name, pkind),
                    pos, site, name, true);
            case ARRAY:
            case CLASS:
                if (pt.tag == METHOD || pt.tag == FORALL) {
                    return rs.resolveQualifiedMethod(
                        pos, env, site, name, pt.getParameterTypes(), pt.getTypeArguments());
                } else if (name == names._this || name == names._super) {
                    return rs.resolveSelf(pos, env, site.tsym, name);
                } else if (name == names._class) {
                    // In this case, we have already made sure in
                    // visitSelect that qualifier expression is a type.
                    Type t = syms.classType;
                    List<Type> typeargs = allowGenerics
                        ? List.of(types.erasure(site))
                        : List.<Type>nil();
                    t = new ClassType(t.getEnclosingType(), typeargs, t.tsym);
                    return new VarSymbol(
                        STATIC | PUBLIC | FINAL, names._class, t, site.tsym);
                } else {
                    // We are seeing a plain identifier as selector.
                    Symbol sym = rs.findIdentInType(env, site, name, pkind);
                    if ((pkind & ERRONEOUS) == 0)
                        sym = rs.access(sym, pos, site, name, true);
                    return sym;
                }
            case WILDCARD:
                throw new AssertionError(tree);
            case TYPEVAR:
                // Normally, site.getUpperBound() shouldn't be null.
                // It should only happen during memberEnter/attribBase
                // when determining the super type which *must* be
                // done before attributing the type variables.  In
                // other words, we are seeing this illegal program:
                // class B<T> extends A<T.foo> {}
                Symbol sym = (site.getUpperBound() != null)
                    ? selectSym(tree, capture(site.getUpperBound()), env, pt, pkind)
                    : null;
                if (sym == null || isType(sym)) {
                    log.error(pos, "type.var.cant.be.deref");
                    return syms.errSymbol;
                } else {
                    return sym;
                }
            case ERROR:
                // preserve identifier names through errors
                return new ErrorType(name, site.tsym).tsym;
            default:
                // The qualifier expression is of a primitive type -- only
                // .class is allowed for these.
                if (name == names._class) {
                    // In this case, we have already made sure in Select that
                    // qualifier expression is a type.
                    Type t = syms.classType;
                    Type arg = types.boxedClass(site).type;
                    t = new ClassType(t.getEnclosingType(), List.of(arg), t.tsym);
                    return new VarSymbol(
                        STATIC | PUBLIC | FINAL, names._class, t, site.tsym);
                } else {
                    log.error(pos, "cant.deref", site);
                    return syms.errSymbol;
                }
            }
            
            }finally{//我加上的
			DEBUG.P(0,this,"selectSym(5)");
			}
        }

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
    //假定Symbol sym是Type site的成员(member),判断
    //在env这样一个环境中是否有权限访问Symbol sym
    public boolean isAccessible(Env<AttrContext> env, Type site, Symbol sym) {
    	try {//我加上的
		DEBUG.P(this,"isAccessible(3)");
		DEBUG.P("sym.name="+sym.name);
		DEBUG.P("sym.flags_field="+Flags.toString(sym.flags_field));
		DEBUG.P("sym.flags_field & AccessFlags="+Flags.toString(sym.flags_field & AccessFlags));
		
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