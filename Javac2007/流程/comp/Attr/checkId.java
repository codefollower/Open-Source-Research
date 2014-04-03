        /** Determine type of identifier or select expression and check that
         *  (1) the referenced symbol is not deprecated
         *  (2) the symbol's type is safe (@see checkSafe)
         *  (3) if symbol is a variable, check that its type and kind are
         *      compatible with the prototype and protokind.
         *  (4) if symbol is an instance field of a raw type,
         *      which is being assigned to, issue an unchecked warning if its
         *      type changes under erasure.
         *  (5) if symbol is an instance method of a raw type, issue an
         *      unchecked warning if its argument types change under erasure.
         *  If checks succeed:
         *    If symbol is a constant, return its constant type
         *    else if symbol is a method, return its result type
         *    otherwise return its type.
         *  Otherwise return errType.
         *
         *  @param tree       The syntax tree representing the identifier
         *  @param site       If this is a select, the type of the selected
         *                    expression, otherwise the type of the current class.
         *  @param sym        The symbol representing the identifier.
         *  @param env        The current environment.
         *  @param pkind      The set of expected kinds.
         *  @param pt         The expected type.
         */
        Type checkId(JCTree tree,
                     Type site,
                     Symbol sym,
                     Env<AttrContext> env,
                     int pkind,
                     Type pt,
                     boolean useVarargs) {
            try {//我加上的
            DEBUG.P(this,"checkId(7)");
            DEBUG.P("env="+env);
            DEBUG.P("sym="+sym);
            DEBUG.P("sym.kind="+Kinds.toString(sym.kind));
			
			
            if (pt.isErroneous()) return syms.errType;
            Type owntype; // The computed type of this identifier occurrence.
            switch (sym.kind) {
            case TYP:
				/*
					//import test.attr.PointTree.Visitor;//导入需要 test.attr.Tree.Visitor 的规范名称
					import test.attr.Tree.Visitor;//

					class ClassA{}
					class Tree<A> { class Visitor {  } }
					class PointTree extends Tree<ClassA> {}

					class VisitSelectTest {
						PointTree.Visitor pv;

						Tree<ClassA>.Visitor pv2;
						Visitor pv3;
					}
				*/
                // <editor-fold defaultstate="collapsed">
                // For types, the computed type equals the symbol's type,
                // except for two situations:
                owntype = sym.type;
                DEBUG.P("owntype.tag="+TypeTags.toString(owntype.tag));
                if (owntype.tag == CLASS) {
                    Type ownOuter = owntype.getEnclosingType();
                    DEBUG.P("ownOuter="+ownOuter);
                    DEBUG.P("ownOuter.tag="+TypeTags.toString(ownOuter.tag));
                    DEBUG.P("site != ownOuter="+(site != ownOuter));
                    DEBUG.P("owntype.tsym.type.getTypeArguments()="+owntype.tsym.type.getTypeArguments());

                    // (a) If the symbol's type is parameterized, erase it
                    // because no type parameters were given.
                    // We recover generic outer type later in visitTypeApply.
                    if (owntype.tsym.type.getTypeArguments().nonEmpty()) {
                        owntype = types.erasure(owntype);
                        DEBUG.P("owntype="+owntype);
                        DEBUG.P("owntype.tag="+TypeTags.toString(owntype.tag));
                    }

                    // (b) If the symbol's type is an inner class, then
                    // we have to interpret its outer type as a superclass
                    // of the site type. Example:
                    //
                    // class Tree<A> { class Visitor { ... } }
                    // class PointTree extends Tree<Point> { ... }
                    // ...PointTree.Visitor...
                    //
                    // Then the type of the last expression above is
                    // Tree<Point>.Visitor.
                    else if (ownOuter.tag == CLASS && site != ownOuter) {
                        Type normOuter = site;
						DEBUG.P("site="+site); 
						DEBUG.P("ownOuter="+ownOuter);
						DEBUG.P("ownOuter.tsym="+ownOuter.tsym);
                        DEBUG.P("normOuter.tag="+TypeTags.toString(normOuter.tag));
                        if (normOuter.tag == CLASS)
                            normOuter = types.asEnclosingSuper(site, ownOuter.tsym);
                       
                        DEBUG.P("normOuter="+normOuter);    
                        if (normOuter == null) // perhaps from an import
                            normOuter = types.erasure(ownOuter);
                        
						DEBUG.P("normOuter="+normOuter);
						DEBUG.P("ownOuter ="+ownOuter);
                        DEBUG.P("normOuter != ownOuter="+(normOuter != ownOuter));
                        if (normOuter != ownOuter)
                            owntype = new ClassType(
                                normOuter, List.<Type>nil(), owntype.tsym);
                        DEBUG.P("owntype="+owntype);
                        DEBUG.P("owntype.tag="+TypeTags.toString(owntype.tag));
                    }
                }
                break;
                // </editor-fold>
            case VAR:
                // <editor-fold defaultstate="collapsed">
                VarSymbol v = (VarSymbol)sym;
                // Test (4): if symbol is an instance field of a raw type,
                // which is being assigned to, issue an unchecked warning if
                // its type changes under erasure.
                if (allowGenerics &&
                    pkind == VAR &&
                    v.owner.kind == TYP &&
                    (v.flags() & STATIC) == 0 &&
                    (site.tag == CLASS || site.tag == TYPEVAR)) {
                    Type s = types.asOuterSuper(site, v.owner);
                    if (s != null &&
                        s.isRaw() &&
                        !types.isSameType(v.type, v.erasure(types))) {
                        chk.warnUnchecked(tree.pos(),
                                          "unchecked.assign.to.var",
                                          v, s);
                    }
                }
                // The computed type of a variable is the type of the
                // variable symbol, taken as a member of the site type.
                owntype = (sym.owner.kind == TYP &&
                           sym.name != names._this && sym.name != names._super)
                    ? types.memberType(site, sym)
                    : sym.type;

                if (env.info.tvars.nonEmpty()) {
                    Type owntype1 = new ForAll(env.info.tvars, owntype);
                    for (List<Type> l = env.info.tvars; l.nonEmpty(); l = l.tail)
                        if (!owntype.contains(l.head)) {
                            log.error(tree.pos(), "undetermined.type", owntype1);
                            owntype1 = syms.errType;
                        }
                    owntype = owntype1;
                }

                // If the variable is a constant, record constant value in
                // computed type.
                if (v.getConstValue() != null && isStaticReference(tree))
                    owntype = owntype.constType(v.getConstValue());

                if (pkind == VAL) {
                    owntype = capture(owntype); // capture "names as expressions"
                }
                break;
                // </editor-fold>
            case MTH: {
                JCMethodInvocation app = (JCMethodInvocation)env.tree;
                owntype = checkMethod(site, sym, env, app.args,
                                      pt.getParameterTypes(), pt.getTypeArguments(),
                                      env.info.varArgs);
                break;
            }
            case PCK: case ERR:
                owntype = sym.type;
                DEBUG.P("owntype.tag="+TypeTags.toString(owntype.tag));
                break;
            default:
                throw new AssertionError("unexpected kind: " + sym.kind +
                                         " in tree " + tree);
            }
            DEBUG.P("sym.flags_field="+Flags.toString(sym.flags_field));

            // Test (1): emit a `deprecation' warning if symbol is deprecated.
            // (for constructors, the error was given when the constructor was
            // resolved)
            if (sym.name != names.init &&
                (sym.flags() & DEPRECATED) != 0 &&
                (env.info.scope.owner.flags() & DEPRECATED) == 0 &&
                sym.outermostClass() != env.info.scope.owner.outermostClass())
                chk.warnDeprecated(tree.pos(), sym);

            if ((sym.flags() & PROPRIETARY) != 0)
                log.strictWarning(tree.pos(), "sun.proprietary", sym);
                
            // Test (3): if symbol is a variable, check that its type and
            // kind are compatible with the prototype and protokind.
            return check(tree, owntype, sym.kind, pkind, pt);
            
            
            }finally{//我加上的
            DEBUG.P(0,this,"checkId(7)");
            }
        }