    /** Visitor method for parameterized types.
     *  Bound checking is left until later, since types are attributed
     *  before supertype structure is completely known
     */
    public void visitTypeApply(JCTypeApply tree) {
		DEBUG.P(this,"visitTypeApply(JCTypeApply tree)");
		DEBUG.P("tree="+tree);
		
        Type owntype = syms.errType;

        // Attribute functor part of application and make sure it's a class.
        Type clazztype = chk.checkClassType(tree.clazz.pos(), attribType(tree.clazz, env));
        
        DEBUG.P("clazztype="+clazztype);
        DEBUG.P("clazztype.tag="+TypeTags.toString(clazztype.tag));

        // Attribute type parameters
        List<Type> actuals = attribTypes(tree.arguments, env);

        if (clazztype.tag == CLASS) {
            List<Type> formals = clazztype.tsym.type.getTypeArguments();

            if (actuals.length() == formals.length()) {
                List<Type> a = actuals;
                List<Type> f = formals;
                while (a.nonEmpty()) {
                    a.head = a.head.withTypeVar(f.head);
                    a = a.tail;
                    f = f.tail;
                }
                // Compute the proper generic outer
                Type clazzOuter = clazztype.getEnclosingType();
                if (clazzOuter.tag == CLASS) {
                    Type site;
                    if (tree.clazz.tag == JCTree.IDENT) {
                        site = env.enclClass.sym.type;
                    } else if (tree.clazz.tag == JCTree.SELECT) {
                        site = ((JCFieldAccess) tree.clazz).selected.type;
                    } else throw new AssertionError(""+tree);
                    if (clazzOuter.tag == CLASS && site != clazzOuter) {
                        if (site.tag == CLASS)
                            site = types.asOuterSuper(site, clazzOuter.tsym);
                        if (site == null)
                            site = types.erasure(clazzOuter);
                        clazzOuter = site;
                    }
                }
                owntype = new ClassType(clazzOuter, actuals, clazztype.tsym);
            } else {
                if (formals.length() != 0) {
                    log.error(tree.pos(), "wrong.number.type.args",
                              Integer.toString(formals.length()));
                } else {
                    log.error(tree.pos(), "type.doesnt.take.params", clazztype.tsym);
                }
                owntype = syms.errType;
            }
        }
        result = check(tree, owntype, TYP, pkind, pt);
		DEBUG.P(0,this,"visitTypeApply(JCTypeApply tree)");
    }