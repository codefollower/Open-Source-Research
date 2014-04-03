	//在Attr阶段前JCIdent.sym是null的，在调用visitIdent()就有适当的值了
    public void visitIdent(JCIdent tree) {
    	DEBUG.P(this,"visitIdent(1)");
        Symbol sym;
        boolean varArgs = false;
        
        DEBUG.P("pt.tag="+TypeTags.toString(pt.tag));
        DEBUG.P("tree.sym="+tree.sym);

        // Find symbol
        if (pt.tag == METHOD || pt.tag == FORALL) {
            // If we are looking for a method, the prototype `pt' will be a
            // method type with the type of the call's arguments as parameters.
            env.info.varArgs = false;
            sym = rs.resolveMethod(tree.pos(), env, tree.name, pt.getParameterTypes(), pt.getTypeArguments());
            varArgs = env.info.varArgs;
        } else if (tree.sym != null && tree.sym.kind != VAR) {
            sym = tree.sym;
        } else {
            sym = rs.resolveIdent(tree.pos(), env, tree.name, pkind);
        }
        tree.sym = sym;
        DEBUG.P("tree.sym="+tree.sym);
        DEBUG.P("tree.sym.kind="+Kinds.toString(tree.sym.kind));

        // (1) Also find the environment current for the class where
        //     sym is defined (`symEnv').
        // Only for pre-tiger versions (1.4 and earlier):
        // (2) Also determine whether we access symbol out of an anonymous
        //     class in a this or super call.  This is illegal for instance
        //     members since such classes don't carry a this$n link.
        //     (`noOuterThisPath').
        Env<AttrContext> symEnv = env;
        DEBUG.P("symEnv="+symEnv);
        DEBUG.P("env.enclClass.sym.owner.kind="+Kinds.toString(env.enclClass.sym.owner.kind));
        if(sym.owner!=null) DEBUG.P("sym.owner.kind="+Kinds.toString(sym.owner.kind));
        
        boolean noOuterThisPath = false;
        if (env.enclClass.sym.owner.kind != PCK && // we are in an inner class
            (sym.kind & (VAR | MTH | TYP)) != 0 &&
            sym.owner.kind == TYP &&
            tree.name != names._this && tree.name != names._super) {

            // Find environment in which identifier is defined.
            while (symEnv.outer != null &&
                   !sym.isMemberOf(symEnv.enclClass.sym, types)) {
                if ((symEnv.enclClass.sym.flags() & NOOUTERTHIS) != 0)
                    noOuterThisPath = !allowAnonOuterThis;
                symEnv = symEnv.outer;
            }
        }

        // If symbol is a variable, ...
        if (sym.kind == VAR) {
            VarSymbol v = (VarSymbol)sym;

            // ..., evaluate its initializer, if it has one, and check for
            // illegal forward reference.
            checkInit(tree, env, v, false);

            // If symbol is a local variable accessed from an embedded
            // inner class check that it is final.
            if (v.owner.kind == MTH &&
                v.owner != env.info.scope.owner &&
                (v.flags_field & FINAL) == 0) {
                log.error(tree.pos(),
                          "local.var.accessed.from.icls.needs.final",
                          v);
            }

            // If we are expecting a variable (as opposed to a value), check
            // that the variable is assignable in the current environment.
            if (pkind == VAR)
                checkAssignable(tree.pos(), v, null, env);
        }
        
        DEBUG.P("symEnv.info.isSelfCall="+symEnv.info.isSelfCall);
        DEBUG.P("noOuterThisPath="+noOuterThisPath);
        // In a constructor body,
        // if symbol is a field or instance method, check that it is
        // not accessed before the supertype constructor is called.
        if ((symEnv.info.isSelfCall || noOuterThisPath) &&
            (sym.kind & (VAR | MTH)) != 0 &&
            sym.owner.kind == TYP &&
            (sym.flags() & STATIC) == 0) {
            chk.earlyRefError(tree.pos(), sym.kind == VAR ? sym : thisSym(tree.pos(), env));
        }
		Env<AttrContext> env1 = env;
		DEBUG.P("env1="+env1);
		DEBUG.P("sym.owner="+sym.owner);
		DEBUG.P("env1.enclClass.sym="+env1.enclClass.sym);
		if (sym.kind != ERR && sym.owner != null && sym.owner != env1.enclClass.sym) {
		    // If the found symbol is inaccessible, then it is
		    // accessed through an enclosing instance.  Locate this
		    // enclosing instance:
		    DEBUG.P("env1.outer="+env1.outer);
		    while (env1.outer != null && !rs.isAccessible(env, env1.enclClass.sym.type, sym))
			env1 = env1.outer;
		}
		DEBUG.P("env1="+env1);
		DEBUG.P("env1.enclClass.sym="+env1.enclClass.sym);
        result = checkId(tree, env1.enclClass.sym.type, sym, env, pkind, pt, varArgs);
        
        DEBUG.P(0,this,"visitIdent(1)");
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