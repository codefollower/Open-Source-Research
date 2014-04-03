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
		DEBUG.P(this,"resolveConstructor(5)");

        Symbol sym = resolveConstructor(pos, env, site, argtypes, typeargtypes, false, env.info.varArgs=false);
        if (varargsEnabled && sym.kind >= WRONG_MTHS) {
            sym = resolveConstructor(pos, env, site, argtypes, typeargtypes, true, false);
            if (sym.kind >= WRONG_MTHS)
                sym = resolveConstructor(pos, env, site, argtypes, typeargtypes, true, env.info.varArgs=true);
        }
        if (sym.kind >= AMBIGUOUS) {
            sym = access(sym, pos, site, names.init, true, argtypes, typeargtypes);
        }

		DEBUG.P("sym="+sym);
		DEBUG.P(0,this,"resolveConstructor(5)");
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
		DEBUG.P(this,"resolveConstructor(7)");
    	DEBUG.P("site="+site);
		DEBUG.P("argtypes="+argtypes);
		DEBUG.P("typeargtypes="+typeargtypes);
		DEBUG.P("allowBoxing="+allowBoxing);
		DEBUG.P("useVarargs="+useVarargs);
        Symbol sym = findMethod(env, site,
                                names.init, argtypes,
                                typeargtypes, allowBoxing,
                                useVarargs, false);

		DEBUG.P("sym.flags()="+Flags.toString(sym.flags()));
		DEBUG.P("env.info.scope.owner="+env.info.scope.owner);
		DEBUG.P("env.info.scope.owner.flags()="+Flags.toString(env.info.scope.owner.flags()));
        DEBUG.P("env.info.scope.owner.outermostClass()="+env.info.scope.owner.outermostClass());
		DEBUG.P("sym.outermostClass()="+sym.outermostClass());

		/*
			package test.attr;
			class A{
				@Deprecated
				A(){}

				class B extends A{
					//sym.flags()=0x20000 deprecated 
					//env.info.scope.owner=B()
					//env.info.scope.owner.flags()=0x0 
					//env.info.scope.owner.outermostClass()=test.attr.A
					//sym.outermostClass()=test.attr.A
					B(){
						super(); //不会警告，因为B是A的成员
					}
				}
			}
			class B extends A{
				//sym.flags()=0x40020000 deprecated acyclic 
				//env.info.scope.owner=B()
				//env.info.scope.owner.flags()=0x0 
				//env.info.scope.owner.outermostClass()=test.attr.B
				//sym.outermostClass()=test.attr.A
				B(){
					super(); //警告：[deprecation] test.attr.A 中的 A() 已过时
				}
			}
			class C extends A{
				//sym.flags()=0x40020000 deprecated acyclic 
				//env.info.scope.owner=C()
				//env.info.scope.owner.flags()=0x20000 deprecated 
				//env.info.scope.owner.outermostClass()=test.attr.C
				//sym.outermostClass()=test.attr.A
				@Deprecated
				C(){
					super(); //不会警告，因为C()已有@Deprecated
				}
			}
		*/
		if ((sym.flags() & DEPRECATED) != 0 &&
            (env.info.scope.owner.flags() & DEPRECATED) == 0 &&
            env.info.scope.owner.outermostClass() != sym.outermostClass())
            chk.warnDeprecated(pos, sym);

		DEBUG.P("sym="+sym);
		DEBUG.P(0,this,"resolveConstructor(7)");
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