    public void visitNewClass(JCNewClass tree) {
    	DEBUG.P(this,"visitNewClass(1)");
		DEBUG.P("tree="+tree);
		
        Type owntype = syms.errType;

        // The local environment of a class creation is
        // a new environment nested in the current one.
        Env<AttrContext> localEnv = env.dup(tree, env.info.dup());
        
        DEBUG.P("localEnv="+localEnv);
        
        // The anonymous inner class definition of the new expression,
        // if one is defined by it.
        JCClassDecl cdef = tree.def;

        // If enclosing class is given, attribute it, and
        // complete class name to be fully qualified
        JCExpression clazz = tree.clazz; // Class field following new
        DEBUG.P("clazz="+clazz);
        DEBUG.P("clazz.tag="+clazz.myTreeTag());
        JCExpression clazzid =          // Identifier in class field
            (clazz.tag == JCTree.TYPEAPPLY)
            ? ((JCTypeApply) clazz).clazz
            : clazz;

        JCExpression clazzid1 = clazzid; // The same in fully qualified form
        
        DEBUG.P("clazzid="+clazzid);
        DEBUG.P("tree.encl="+tree.encl);
        
        if (tree.encl != null) {
            // We are seeing a qualified new, of the form
            //    <expr>.new C <...> (...) ...
            // In this case, we let clazz stand for the name of the
            // allocated class C prefixed with the type of the qualifier
            // expression, so that we can
            // resolve it with standard techniques later. I.e., if
            // <expr> has type T, then <expr>.new C <...> (...)
            // yields a clazz T.C.
            Type encltype = chk.checkRefType(tree.encl.pos(),
                                             attribExpr(tree.encl, env));
            clazzid1 = make.at(clazz.pos).Select(make.Type(encltype),
                                                 ((JCIdent) clazzid).name);
            if (clazz.tag == JCTree.TYPEAPPLY)
                clazz = make.at(tree.pos).
                    TypeApply(clazzid1,
                              ((JCTypeApply) clazz).arguments);
            else
                clazz = clazzid1;
//          System.out.println(clazz + " generated.");//DEBUG
        }

        // Attribute clazz expression and store
        // symbol + type back into the attributed tree.
        Type clazztype = chk.checkClassType(
            tree.clazz.pos(), attribType(clazz, env), true);
        chk.validate(clazz);
        if (tree.encl != null) {
            // We have to work in this case to store
            // symbol + type back into the attributed tree.
            tree.clazz.type = clazztype;
            TreeInfo.setSymbol(clazzid, TreeInfo.symbol(clazzid1));
            clazzid.type = ((JCIdent) clazzid).sym.type;
            if (!clazztype.isErroneous()) {
                if (cdef != null && clazztype.tsym.isInterface()) {
                    log.error(tree.encl.pos(), "anon.class.impl.intf.no.qual.for.new");
                } else if (clazztype.tsym.isStatic()) {
                    log.error(tree.encl.pos(), "qualified.new.of.static.class", clazztype.tsym);
                }
            }
        } else if (!clazztype.tsym.isInterface() &&
                   clazztype.getEnclosingType().tag == CLASS) {
            // Check for the existence of an apropos outer instance
            rs.resolveImplicitThis(tree.pos(), env, clazztype);
        }

        // Attribute constructor arguments.
        List<Type> argtypes = attribArgs(tree.args, localEnv);
        List<Type> typeargtypes = attribTypes(tree.typeargs, localEnv);

        // If we have made no mistakes in the class type...
        if (clazztype.tag == CLASS) {
            // Enums may not be instantiated except implicitly
            if (allowEnums &&
                (clazztype.tsym.flags_field&Flags.ENUM) != 0 &&
                (env.tree.tag != JCTree.VARDEF ||
                 (((JCVariableDecl) env.tree).mods.flags&Flags.ENUM) == 0 ||
                 ((JCVariableDecl) env.tree).init != tree))
                log.error(tree.pos(), "enum.cant.be.instantiated");
            // Check that class is not abstract
            if (cdef == null &&
                (clazztype.tsym.flags() & (ABSTRACT | INTERFACE)) != 0) {
                log.error(tree.pos(), "abstract.cant.be.instantiated",
                          clazztype.tsym);
            } else if (cdef != null && clazztype.tsym.isInterface()) {
                // Check that no constructor arguments are given to
                // anonymous classes implementing an interface
                if (!argtypes.isEmpty())
                    log.error(tree.args.head.pos(), "anon.class.impl.intf.no.args");

                if (!typeargtypes.isEmpty())
                    log.error(tree.typeargs.head.pos(), "anon.class.impl.intf.no.typeargs");

                // Error recovery: pretend no arguments were supplied.
                argtypes = List.nil();
                typeargtypes = List.nil();
            }

            // Resolve the called constructor under the assumption
            // that we are referring to a superclass instance of the
            // current instance (JLS ???).
            else {
                localEnv.info.selectSuper = cdef != null;
                localEnv.info.varArgs = false;
                tree.constructor = rs.resolveConstructor(
                    tree.pos(), localEnv, clazztype, argtypes, typeargtypes);
                Type ctorType = checkMethod(clazztype,
                                            tree.constructor,
                                            localEnv,
                                            tree.args,
                                            argtypes,
                                            typeargtypes,
                                            localEnv.info.varArgs);
                if (localEnv.info.varArgs)
                    assert ctorType.isErroneous() || tree.varargsElement != null;
            }

            if (cdef != null) {
                // We are seeing an anonymous class instance creation.
                // In this case, the class instance creation
                // expression
                //
                //    E.new <typeargs1>C<typargs2>(args) { ... }
                //
                // is represented internally as
                //
                //    E . new <typeargs1>C<typargs2>(args) ( class <empty-name> { ... } )  .
                //
                // This expression is then *transformed* as follows:
                //
                // (1) add a STATIC flag to the class definition
                //     if the current environment is static
                // (2) add an extends or implements clause
                // (3) add a constructor.
                //
                // For instance, if C is a class, and ET is the type of E,
                // the expression
                //
                //    E.new <typeargs1>C<typargs2>(args) { ... }
                //
                // is translated to (where X is a fresh name and typarams is the
                // parameter list of the super constructor):
                //
                //   new <typeargs1>X(<*nullchk*>E, args) where
                //     X extends C<typargs2> {
                //       <typarams> X(ET e, args) {
                //         e.<typeargs1>super(args)
                //       }
                //       ...
                //     }
                if (Resolve.isStatic(env)) cdef.mods.flags |= STATIC;

                if (clazztype.tsym.isInterface()) {
                    cdef.implementing = List.of(clazz);
                } else {
                    cdef.extending = clazz;
                }

                attribStat(cdef, localEnv);

                // If an outer instance is given,
                // prefix it to the constructor arguments
                // and delete it from the new expression
                if (tree.encl != null && !clazztype.tsym.isInterface()) {
                    tree.args = tree.args.prepend(makeNullCheck(tree.encl));
                    argtypes = argtypes.prepend(tree.encl.type);
                    tree.encl = null;
                }

                // Reassign clazztype and recompute constructor.
                clazztype = cdef.sym.type;
                Symbol sym = rs.resolveConstructor(
                    tree.pos(), localEnv, clazztype, argtypes,
                    typeargtypes, true, tree.varargsElement != null);
                assert sym.kind < AMBIGUOUS || tree.constructor.type.isErroneous();
                tree.constructor = sym;
            }

            if (tree.constructor != null && tree.constructor.kind == MTH)
                owntype = clazztype;
        }
        result = check(tree, owntype, VAL, pkind, pt);
        chk.validate(tree.typeargs);
        
        DEBUG.P(1,this,"visitNewClass(1)");
    }