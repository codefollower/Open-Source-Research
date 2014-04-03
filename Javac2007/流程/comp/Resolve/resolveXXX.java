/* ***************************************************************************
 *  Name resolution
 *  Naming conventions are as for symbol lookup
 *  Unlike the find... methods these methods will report access errors
 ****************************************************************************/

    /** Resolve an unqualified (non-method) identifier.
     *  @param pos       The position to use for error reporting.
     *  @param env       The environment current at the identifier use.
     *  @param name      The identifier's name.
     *  @param kind      The set of admissible symbol kinds for the identifier.
     */
    Symbol resolveIdent(DiagnosticPosition pos, Env<AttrContext> env,
                        Name name, int kind) {
        try {
        DEBUG.P(this,"resolveIdent(4)");   
        DEBUG.P("env="+env);
        DEBUG.P("name="+name);
        DEBUG.P("kind="+Kinds.toString(kind));
               	
        return access(
            findIdent(env, name, kind),
            pos, env.enclClass.sym.type, name, false);
            
        
        }finally{
        DEBUG.P(0,this,"resolveIdent(4)");  
        }
    }

    /** Resolve an unqualified method identifier.
     *  @param pos       The position to use for error reporting.
     *  @param env       The environment current at the method invocation.
     *  @param name      The identifier's name.
     *  @param argtypes  The types of the invocation's value arguments.
     *  @param typeargtypes  The types of the invocation's type arguments.
     */
    Symbol resolveMethod(DiagnosticPosition pos,
                         Env<AttrContext> env,
                         Name name,
                         List<Type> argtypes,
                         List<Type> typeargtypes) {
        try {
        DEBUG.P(this,"resolveMethod(5)");   
        DEBUG.P("env="+env);
        DEBUG.P("name="+name);
        DEBUG.P("argtypes="+argtypes);
        DEBUG.P("typeargtypes="+typeargtypes);
        
        Symbol sym = findFun(env, name, argtypes, typeargtypes, false, env.info.varArgs=false);
        if (varargsEnabled && sym.kind >= WRONG_MTHS) {
            sym = findFun(env, name, argtypes, typeargtypes, true, false);
            if (sym.kind >= WRONG_MTHS)
                sym = findFun(env, name, argtypes, typeargtypes, true, env.info.varArgs=true);
        }
        if (sym.kind >= AMBIGUOUS) {
            sym = access(
                sym, pos, env.enclClass.sym.type, name, false, argtypes, typeargtypes);
        }
        return sym;
        
        }finally{
        DEBUG.P(0,this,"resolveMethod(5)");   
        }
    }

    /** Resolve a qualified method identifier
     *  @param pos       The position to use for error reporting.
     *  @param env       The environment current at the method invocation.
     *  @param site      The type of the qualifying expression, in which
     *                   identifier is searched.
     *  @param name      The identifier's name.
     *  @param argtypes  The types of the invocation's value arguments.
     *  @param typeargtypes  The types of the invocation's type arguments.
     */
    Symbol resolveQualifiedMethod(DiagnosticPosition pos, Env<AttrContext> env,
                                  Type site, Name name, List<Type> argtypes,
                                  List<Type> typeargtypes) {
        Symbol sym = findMethod(env, site, name, argtypes, typeargtypes, false,
                                env.info.varArgs=false, false);
        if (varargsEnabled && sym.kind >= WRONG_MTHS) {
            sym = findMethod(env, site, name, argtypes, typeargtypes, true,
                             false, false);
            if (sym.kind >= WRONG_MTHS)
                sym = findMethod(env, site, name, argtypes, typeargtypes, true,
                                 env.info.varArgs=true, false);
        }
        if (sym.kind >= AMBIGUOUS) {
            sym = access(sym, pos, site, name, true, argtypes, typeargtypes);
        }
        return sym;
    }

    /** Resolve a qualified method identifier, throw a fatal error if not
     *  found.
     *  @param pos       The position to use for error reporting.
     *  @param env       The environment current at the method invocation.
     *  @param site      The type of the qualifying expression, in which
     *                   identifier is searched.
     *  @param name      The identifier's name.
     *  @param argtypes  The types of the invocation's value arguments.
     *  @param typeargtypes  The types of the invocation's type arguments.
     */
    public MethodSymbol resolveInternalMethod(DiagnosticPosition pos, Env<AttrContext> env,
                                        Type site, Name name,
                                        List<Type> argtypes,
                                        List<Type> typeargtypes) {
        Symbol sym = resolveQualifiedMethod(
            pos, env, site, name, argtypes, typeargtypes);
        if (sym.kind == MTH) return (MethodSymbol)sym;
        else throw new FatalError(
                 JCDiagnostic.fragment("fatal.err.cant.locate.meth",
                                name));
    }

    /** Resolve constructor.
     *  @param pos       The position to use for error reporting.
     *  @param env       The environment current at the constructor invocation.
     *  @param site      The type of class for which a constructor is searched.
     *  @param argtypes  The types of the constructor invocation's value
     *                   arguments.
     *  @param typeargtypes  The types of the constructor invocation's type
     *                   arguments.
     */
    Symbol resolveConstructor(DiagnosticPosition pos,
                              Env<AttrContext> env,
                              Type site,
                              List<Type> argtypes,
                              List<Type> typeargtypes) {
        Symbol sym = resolveConstructor(pos, env, site, argtypes, typeargtypes, false, env.info.varArgs=false);
        if (varargsEnabled && sym.kind >= WRONG_MTHS) {
            sym = resolveConstructor(pos, env, site, argtypes, typeargtypes, true, false);
            if (sym.kind >= WRONG_MTHS)
                sym = resolveConstructor(pos, env, site, argtypes, typeargtypes, true, env.info.varArgs=true);
        }
        if (sym.kind >= AMBIGUOUS) {
            sym = access(sym, pos, site, names.init, true, argtypes, typeargtypes);
        }
        return sym;
    }

    /** Resolve constructor.
     *  @param pos       The position to use for error reporting.
     *  @param env       The environment current at the constructor invocation.
     *  @param site      The type of class for which a constructor is searched.
     *  @param argtypes  The types of the constructor invocation's value
     *                   arguments.
     *  @param typeargtypes  The types of the constructor invocation's type
     *                   arguments.
     *  @param allowBoxing Allow boxing and varargs conversions.
     *  @param useVarargs Box trailing arguments into an array for varargs.
     */
    Symbol resolveConstructor(DiagnosticPosition pos, Env<AttrContext> env,
                              Type site, List<Type> argtypes,
                              List<Type> typeargtypes,
                              boolean allowBoxing,
                              boolean useVarargs) {
        Symbol sym = findMethod(env, site,
                                names.init, argtypes,
                                typeargtypes, allowBoxing,
                                useVarargs, false);
        if ((sym.flags() & DEPRECATED) != 0 &&
            (env.info.scope.owner.flags() & DEPRECATED) == 0 &&
            env.info.scope.owner.outermostClass() != sym.outermostClass())
            chk.warnDeprecated(pos, sym);
        return sym;
    }

    /** Resolve a constructor, throw a fatal error if not found.
     *  @param pos       The position to use for error reporting.
     *  @param env       The environment current at the method invocation.
     *  @param site      The type to be constructed.
     *  @param argtypes  The types of the invocation's value arguments.
     *  @param typeargtypes  The types of the invocation's type arguments.
     */
    public MethodSymbol resolveInternalConstructor(DiagnosticPosition pos, Env<AttrContext> env,
                                        Type site,
                                        List<Type> argtypes,
                                        List<Type> typeargtypes) {
        Symbol sym = resolveConstructor(
            pos, env, site, argtypes, typeargtypes);
        if (sym.kind == MTH) return (MethodSymbol)sym;
        else throw new FatalError(
                 JCDiagnostic.fragment("fatal.err.cant.locate.ctor", site));
    }

    /** Resolve operator.
     *  @param pos       The position to use for error reporting.
     *  @param optag     The tag of the operation tree.
     *  @param env       The environment current at the operation.
     *  @param argtypes  The types of the operands.
     */
    Symbol resolveOperator(DiagnosticPosition pos, int optag,
                           Env<AttrContext> env, List<Type> argtypes) {
		try {
    	DEBUG.P(this,"resolveOperator(4)");
    	DEBUG.P("argtypes="+argtypes);

        Name name = treeinfo.operatorName(optag);

		DEBUG.P("name="+name);

        Symbol sym = findMethod(env, syms.predefClass.type, name, argtypes,
                                null, false, false, true);
        if (boxingEnabled && sym.kind >= WRONG_MTHS)
            sym = findMethod(env, syms.predefClass.type, name, argtypes,
                             null, true, false, true);
        return access(sym, pos, env.enclClass.sym.type, name,
                      false, argtypes, null);

		}finally{
    	DEBUG.P(0,this,"resolveOperator(4)");
    	}
    }

    /** Resolve operator.
     *  @param pos       The position to use for error reporting.
     *  @param optag     The tag of the operation tree.
     *  @param env       The environment current at the operation.
     *  @param arg       The type of the operand.
     */
    Symbol resolveUnaryOperator(DiagnosticPosition pos, int optag, Env<AttrContext> env, Type arg) {
        return resolveOperator(pos, optag, env, List.of(arg));
    }

    /** Resolve binary operator.
     *  @param pos       The position to use for error reporting.
     *  @param optag     The tag of the operation tree.
     *  @param env       The environment current at the operation.
     *  @param left      The types of the left operand.
     *  @param right     The types of the right operand.
     */
    Symbol resolveBinaryOperator(DiagnosticPosition pos,
                                 int optag,
                                 Env<AttrContext> env,
                                 Type left,
                                 Type right) {
        return resolveOperator(pos, optag, env, List.of(left, right));
    }

    /**
     * Resolve `c.name' where name == this or name == super.
     * @param pos           The position to use for error reporting.
     * @param env           The environment current at the expression.
     * @param c             The qualifier.
     * @param name          The identifier's name.
     */
    Symbol resolveSelf(DiagnosticPosition pos,
                       Env<AttrContext> env,
                       TypeSymbol c,
                       Name name) {
		try {//我加上的
		DEBUG.P(this,"resolveSelf(4)");
		DEBUG.P("c="+c);
		DEBUG.P("name="+name);

        Env<AttrContext> env1 = env;
        boolean staticOnly = false;
        while (env1.outer != null) {
            if (isStatic(env1)) staticOnly = true;
			DEBUG.P("staticOnly="+staticOnly);
			DEBUG.P("env1.enclClass.sym="+env1.enclClass.sym);
            if (env1.enclClass.sym == c) {
                Symbol sym = env1.info.scope.lookup(name).sym;
                if (sym != null) {
                    if (staticOnly) sym = new StaticError(sym);
                    return access(sym, pos, env.enclClass.sym.type,
                                  name, true);
                }
            }
            if ((env1.enclClass.sym.flags() & STATIC) != 0) staticOnly = true;
            env1 = env1.outer;
        }
        log.error(pos, "not.encl.class", c);
        return syms.errSymbol;

		}finally{//我加上的
		DEBUG.P(0,this,"resolveSelf(4)");
		}
    }

    /**
     * Resolve `c.this' for an enclosing class c that contains the
     * named member.
     * @param pos           The position to use for error reporting.
     * @param env           The environment current at the expression.
     * @param member        The member that must be contained in the result.
     */
    Symbol resolveSelfContaining(DiagnosticPosition pos,
                                 Env<AttrContext> env,
                                 Symbol member) {
		try {//我加上的
		DEBUG.P(this,"resolveSelfContaining(3)");
		DEBUG.P("member="+member);

        Name name = names._this;
        Env<AttrContext> env1 = env;
        boolean staticOnly = false;
        while (env1.outer != null) {
            if (isStatic(env1)) staticOnly = true;
            if (env1.enclClass.sym.isSubClass(member.owner, types) &&
                isAccessible(env, env1.enclClass.sym.type, member)) {
                Symbol sym = env1.info.scope.lookup(name).sym;
                if (sym != null) {
                    if (staticOnly) sym = new StaticError(sym);
                    return access(sym, pos, env.enclClass.sym.type,
                                  name, true);
                }
            }
            if ((env1.enclClass.sym.flags() & STATIC) != 0)
                staticOnly = true;
            env1 = env1.outer;
        }
        log.error(pos, "encl.class.required", member);
        return syms.errSymbol;

		}finally{//我加上的
		DEBUG.P(0,this,"resolveSelfContaining(3)");
		}
    }

    /**
     * Resolve an appropriate implicit this instance for t's container.
     * JLS2 8.8.5.1 and 15.9.2
     */
    Type resolveImplicitThis(DiagnosticPosition pos, Env<AttrContext> env, Type t) {
		DEBUG.P(this,"resolveImplicitThis(3)");
		DEBUG.P("env="+env);
		DEBUG.P("t="+t);
		DEBUG.P("t.tsym.owner="+t.tsym.owner);
		DEBUG.P("t.tsym.owner.kind="+Kinds.toString(t.tsym.owner.kind));

        Type thisType = (((t.tsym.owner.kind & (MTH|VAR)) != 0)
                         ? resolveSelf(pos, env, t.getEnclosingType().tsym, names._this)
                         : resolveSelfContaining(pos, env, t.tsym)).type;
        if (env.info.isSelfCall && thisType.tsym == env.enclClass.sym)
            log.error(pos, "cant.ref.before.ctor.called", "this");
        
		DEBUG.P("thisType="+thisType);
		DEBUG.P(0,this,"resolveImplicitThis(3)");
		return thisType;
    }