    public void visitMethodDef(JCMethodDecl tree) {
    	DEBUG.P(this,"visitMethodDef(JCMethodDecl tree)");
    	DEBUG.P("tree.sym="+tree.sym);
        MethodSymbol m = tree.sym;

        Lint lint = env.info.lint.augment(m.attributes_field, m.flags());
        Lint prevLint = chk.setLint(lint);
        try {
            chk.checkDeprecatedAnnotation(tree.pos(), m);
            
            //COMPOUND类型会对应一个ClassSymbol
            //在attribBounds必须对这个ClassSymbol进行attribClass
            attribBounds(tree.typarams);

            // If we override any other methods, check that we do so properly.
            // JLS ???
            chk.checkOverride(tree, m);

            // Create a new environment with local scope
            // for attributing the method.
            Env<AttrContext> localEnv = memberEnter.methodEnv(tree, env);

            localEnv.info.lint = lint;

            // Enter all type parameters into the local method scope.
            for (List<JCTypeParameter> l = tree.typarams; l.nonEmpty(); l = l.tail)
                localEnv.info.scope.enterIfAbsent(l.head.type.tsym);

            ClassSymbol owner = env.enclClass.sym;
            if ((owner.flags() & ANNOTATION) != 0 &&
                tree.params.nonEmpty())
                log.error(tree.params.head.pos(),
                          "intf.annotation.members.cant.have.params");

            // Attribute all value parameters.
            for (List<JCVariableDecl> l = tree.params; l.nonEmpty(); l = l.tail) {
                attribStat(l.head, localEnv);
            }

            // Check that type parameters are well-formed.
            chk.validateTypeParams(tree.typarams);
            if ((owner.flags() & ANNOTATION) != 0 &&
                tree.typarams.nonEmpty())
                log.error(tree.typarams.head.pos(),
                          "intf.annotation.members.cant.have.type.params");

            // Check that result type is well-formed.
            chk.validate(tree.restype);
            if ((owner.flags() & ANNOTATION) != 0)
                chk.validateAnnotationType(tree.restype);

            if ((owner.flags() & ANNOTATION) != 0)
                chk.validateAnnotationMethod(tree.pos(), m);

            // Check that all exceptions mentioned in the throws clause extend
            // java.lang.Throwable.
            if ((owner.flags() & ANNOTATION) != 0 && tree.thrown.nonEmpty())
                log.error(tree.thrown.head.pos(),
                          "throws.not.allowed.in.intf.annotation");
            for (List<JCExpression> l = tree.thrown; l.nonEmpty(); l = l.tail)
                chk.checkType(l.head.pos(), l.head.type, syms.throwableType);

            if (tree.body == null) {
                // Empty bodies are only allowed for
                // abstract, native, or interface methods, or for methods
                // in a retrofit signature class.
                if ((owner.flags() & INTERFACE) == 0 &&
                    (tree.mods.flags & (ABSTRACT | NATIVE)) == 0 &&
                    !relax)
                    log.error(tree.pos(), "missing.meth.body.or.decl.abstract");
                if (tree.defaultValue != null) {
                    if ((owner.flags() & ANNOTATION) == 0)
                        log.error(tree.pos(),
                                  "default.allowed.in.intf.annotation.member");
                }
            } else if ((owner.flags() & INTERFACE) != 0) {
                log.error(tree.body.pos(), "intf.meth.cant.have.body");
            } else if ((tree.mods.flags & ABSTRACT) != 0) {
                log.error(tree.pos(), "abstract.meth.cant.have.body");
            } else if ((tree.mods.flags & NATIVE) != 0) {
                log.error(tree.pos(), "native.meth.cant.have.body");
            } else {
                // Add an implicit super() call unless an explicit call to
                // super(...) or this(...) is given
                // or we are compiling class java.lang.Object.
                if (tree.name == names.init && owner.type != syms.objectType) {
                    JCBlock body = tree.body;
                    if (body.stats.isEmpty() ||
                        !TreeInfo.isSelfCall(body.stats.head)) {
                        body.stats = body.stats.
                            prepend(memberEnter.SuperCall(make.at(body.pos),
                                                          List.<Type>nil(),
                                                          List.<JCVariableDecl>nil(),
                                                          false));
                    } else if ((env.enclClass.sym.flags() & ENUM) != 0 &&
                               (tree.mods.flags & GENERATEDCONSTR) == 0 &&
                               TreeInfo.isSuperCall(body.stats.head)) {
                        // enum constructors are not allowed to call super
                        // directly, so make sure there aren't any super calls
                        // in enum constructors, except in the compiler
                        // generated one.
                        log.error(tree.body.stats.head.pos(),
                                  "call.to.super.not.allowed.in.enum.ctor",
                                  env.enclClass.sym);
                    }
                }

                // Attribute method body.
                attribStat(tree.body, localEnv);
            }
            localEnv.info.scope.leave();
            result = tree.type = m.type;
            chk.validateAnnotations(tree.mods.annotations, m);

        }
        finally {
            chk.setLint(prevLint);
            DEBUG.P(1,this,"visitMethodDef(JCMethodDecl tree)");
        }
    }