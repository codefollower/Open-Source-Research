     /** Visitor method for method invocations.
     *  NOTE: The method part of an application will have in its type field
     *        the return type of the method, not the method's type itself!
     */
    public void visitApply(JCMethodInvocation tree) {
    	DEBUG.P(this,"visitApply(1)");
        // The local environment of a method application is
        // a new environment nested in the current one.
        Env<AttrContext> localEnv = env.dup(tree, env.info.dup());

        // The types of the actual method arguments.
        List<Type> argtypes;

        // The types of the actual method type arguments.
        List<Type> typeargtypes = null;

        Name methName = TreeInfo.name(tree.meth);

        boolean isConstructorCall =
            methName == names._this || methName == names._super;

        if (isConstructorCall) {
            // We are seeing a ...this(...) or ...super(...) call.
            // Check that this is the first statement in a constructor.
            if (checkFirstConstructorStat(tree, env)) {

                // Record the fact
                // that this is a constructor call (using isSelfCall).
                localEnv.info.isSelfCall = true;

                // Attribute arguments, yielding list of argument types.
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