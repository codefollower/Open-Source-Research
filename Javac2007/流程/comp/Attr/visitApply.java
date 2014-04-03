    /** Visitor method for method invocations.
     *  NOTE: The method part of an application will have in its type field
     *        the return type of the method, not the method's type itself!
     */
    public void visitApply(JCMethodInvocation tree) {
    	DEBUG.P(this,"visitApply(1)");
		DEBUG.P("tree="+tree);
		DEBUG.P("tree.meth="+tree.meth);
		DEBUG.P("tree.typeargs="+tree.typeargs);
		DEBUG.P("tree.args="+tree.args);
		DEBUG.P("tree.varargsElement="+tree.varargsElement);

        // The local environment of a method application is
        // a new environment nested in the current one.
        Env<AttrContext> localEnv = env.dup(tree, env.info.dup());

		DEBUG.P("localEnv="+localEnv);

        // The types of the actual method arguments.
        List<Type> argtypes;

        // The types of the actual method type arguments.
        List<Type> typeargtypes = null;

        Name methName = TreeInfo.name(tree.meth);

		DEBUG.P("methName="+methName);

        boolean isConstructorCall =
            methName == names._this || methName == names._super;

		DEBUG.P("isConstructorCall="+isConstructorCall);

        if (isConstructorCall) {
            // We are seeing a ...this(...) or ...super(...) call.
            // Check that this is the first statement in a constructor.
            if (checkFirstConstructorStat(tree, env)) {
				//注意:是传入env，而不是localEnv

                // Record the fact
                // that this is a constructor call (using isSelfCall).
                localEnv.info.isSelfCall = true;

                // Attribute arguments, yielding list of argument types.
				DEBUG.P("tree.args="+tree.args);
				DEBUG.P("tree.typeargs="+tree.typeargs);
                argtypes = attribArgs(tree.args, localEnv);
                typeargtypes = attribTypes(tree.typeargs, localEnv);

                // Variable `site' points to the class in which the called
                // constructor is defined.
                Type site = env.enclClass.sym.type;
                DEBUG.P("site="+site);
                DEBUG.P("methName="+methName);
                if (methName == names._super) {
                    if (site == syms.objectType) {
                        log.error(tree.meth.pos(), "no.superclass", site);
                        site = syms.errType;
                    } else {
                        site = types.supertype(site);
                    }
                }
                
                DEBUG.P("site="+site);
                DEBUG.P("site.tag="+TypeTags.toString(site.tag));

                if (site.tag == CLASS) {
                	DEBUG.P("site.getEnclosingType().tag="+TypeTags.toString(site.getEnclosingType().tag));
                    if (site.getEnclosingType().tag == CLASS) {
                        // we are calling a nested class

                        if (tree.meth.tag == JCTree.SELECT) {
                            JCTree qualifier = ((JCFieldAccess) tree.meth).selected;

                            // We are seeing a prefixed call, of the form
                            //     <expr>.super(...).
                            // Check that the prefix expression conforms
                            // to the outer instance type of the class.
                            chk.checkRefType(qualifier.pos(),
                                             attribExpr(qualifier, localEnv,
                                                        site.getEnclosingType()));
                        } else if (methName == names._super) {
                            // qualifier omitted; check for existence
                            // of an appropriate implicit qualifier.
                            rs.resolveImplicitThis(tree.meth.pos(),
                                                   localEnv, site);
                        }
                    } else if (tree.meth.tag == JCTree.SELECT) {
						//例:class ClassA { ClassA() { ClassA.super(); } } 
                        log.error(tree.meth.pos(), "illegal.qual.not.icls",
                                  site.tsym);
                    }

                    // if we're calling a java.lang.Enum constructor,
                    // prefix the implicit String and int parameters
                    if (site.tsym == syms.enumSym && allowEnums)
                        argtypes = argtypes.prepend(syms.intType).prepend(syms.stringType);

                    // Resolve the called constructor under the assumption
                    // that we are referring to a superclass instance of the
                    // current instance (JLS ???).
                    boolean selectSuperPrev = localEnv.info.selectSuper;
                    localEnv.info.selectSuper = true;
                    localEnv.info.varArgs = false;
                    Symbol sym = rs.resolveConstructor(
                        tree.meth.pos(), localEnv, site, argtypes, typeargtypes);
                    localEnv.info.selectSuper = selectSuperPrev;

                    // Set method symbol to resolved constructor...
                    TreeInfo.setSymbol(tree.meth, sym);

                    // ...and check that it is legal in the current context.
                    // (this will also set the tree's type)
                    Type mpt = newMethTemplate(argtypes, typeargtypes);
                    checkId(tree.meth, site, sym, localEnv, MTH,
                            mpt, tree.varargsElement != null);
                }
                // Otherwise, `site' is an error type and we do nothing
            }
            result = tree.type = syms.voidType;
        } else {
            // Otherwise, we are seeing a regular method call.
            // Attribute the arguments, yielding list of argument types, ...
            argtypes = attribArgs(tree.args, localEnv);
            typeargtypes = attribTypes(tree.typeargs, localEnv);

            // ... and attribute the method using as a prototype a methodtype
            // whose formal argument types is exactly the list of actual
            // arguments (this will also set the method symbol).
            Type mpt = newMethTemplate(argtypes, typeargtypes);
            localEnv.info.varArgs = false;
            Type mtype = attribExpr(tree.meth, localEnv, mpt);
            if (localEnv.info.varArgs)
                assert mtype.isErroneous() || tree.varargsElement != null;

            // Compute the result type.
            Type restype = mtype.getReturnType();
            assert restype.tag != WILDCARD : mtype;

            // as a special case, array.clone() has a result that is
            // the same as static type of the array being cloned
            if (tree.meth.tag == JCTree.SELECT &&
                allowCovariantReturns &&
                methName == names.clone &&
                types.isArray(((JCFieldAccess) tree.meth).selected.type))
                restype = ((JCFieldAccess) tree.meth).selected.type;

            // as a special case, x.getClass() has type Class<? extends |X|>
            if (allowGenerics &&
                methName == names.getClass && tree.args.isEmpty()) {
                Type qualifier = (tree.meth.tag == JCTree.SELECT)
                    ? ((JCFieldAccess) tree.meth).selected.type
                    : env.enclClass.sym.type;
                restype = new
                    ClassType(restype.getEnclosingType(),
                              List.<Type>of(new WildcardType(types.erasure(qualifier),
                                                               BoundKind.EXTENDS,
                                                               syms.boundClass)),
                              restype.tsym);
            }

            // Check that value of resulting type is admissible in the
            // current context.  Also, capture the return type
            result = check(tree, capture(restype), VAL, pkind, pt);
        }
        chk.validate(tree.typeargs);
        DEBUG.P(0,this,"visitApply(1)");
    }
    //where
        /** Check that given application node appears as first statement
         *  in a constructor call.
         *  @param tree   The application node
         *  @param env    The environment current at the application.
         */
		//调用这个方法的前提是存在this(...)或super(...)调用，
		//因为可能在方法或构造函数中任何位置调用this(...)或super(...)，
		//所以必须检查只有在构造函数中第一条语句才能调用this(...)或super(...)
		//下面的enclMethod表示调用this(...)或super(...)的方法或构造函数
		//JCMethodInvocation tree表示this(...)或super(...)
        boolean checkFirstConstructorStat(JCMethodInvocation tree, Env<AttrContext> env) {
            try {//我加上的
			DEBUG.P(this,"checkFirstConstructorStat(2)");
			DEBUG.P("tree="+tree);
			DEBUG.P("env="+env);
			
            JCMethodDecl enclMethod = env.enclMethod;

			if(enclMethod != null) DEBUG.P("enclMethod.name="+enclMethod.name);
            else DEBUG.P("enclMethod=null");

			//如果在实例初始化语句块或静态语句块(如{this();} static {this();})
			//此时enclMethod为null，所以下面加了enclMethod != null条件
			if (enclMethod != null && enclMethod.name == names.init) {
                JCBlock body = enclMethod.body;
				//第一条语句是JCMethodInvocation tree(即:this(...)或super(...))
                if (body.stats.head.tag == JCTree.EXEC &&
                    ((JCExpressionStatement) body.stats.head).expr == tree)
                    return true;
            }
            log.error(tree.pos(),"call.must.be.first.stmt.in.ctor",
                      TreeInfo.name(tree.meth));
            return false;
            
            }finally{//我加上的
			DEBUG.P(0,this,"checkFirstConstructorStat(2)");
			}
        }

        /** Obtain a method type with given argument types.
         */
        Type newMethTemplate(List<Type> argtypes, List<Type> typeargtypes) {
			DEBUG.P(this,"newMethTemplate(2)");
			DEBUG.P("argtypes="+argtypes);
			DEBUG.P("typeargtypes="+typeargtypes);

            MethodType mt = new MethodType(argtypes, null, null, syms.methodClass);
            
			
			//typeargtypes不会为null,因为attribTypes(2)不会返回null
			//return (typeargtypes == null) ? mt : (Type)new ForAll(typeargtypes, mt);
			Type newMeth = (typeargtypes == null) ? mt : (Type)new ForAll(typeargtypes, mt);
			DEBUG.P("newMeth="+newMeth);
			DEBUG.P(0,this,"newMethTemplate(2)");
			return newMeth;
        }
