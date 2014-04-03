    /** Add the implicit members for an enum type
     *  to the symbol table.
     */
    private void addEnumMembers(JCClassDecl tree, Env<AttrContext> env) {
    	try {//我加上的
    	DEBUG.P(this,"addEnumMembers(2)");

        JCExpression valuesType = make.Type(new ArrayType(tree.sym.type, syms.arrayClass));

        // public static T[] values() { return ???; }
        JCMethodDecl values = make.
            MethodDef(make.Modifiers(Flags.PUBLIC|Flags.STATIC),
                      names.values,
                      valuesType,
                      List.<JCTypeParameter>nil(),
                      List.<JCVariableDecl>nil(),
                      List.<JCExpression>nil(), // thrown
                      null, //make.Block(0, Tree.emptyList.prepend(make.Return(make.Ident(names._null)))),
                      null);
        memberEnter(values, env);

        // public static T valueOf(String name) { return ???; }
        JCMethodDecl valueOf = make.
            MethodDef(make.Modifiers(Flags.PUBLIC|Flags.STATIC),
                      names.valueOf,
                      make.Type(tree.sym.type),
                      List.<JCTypeParameter>nil(),
                      List.of(make.VarDef(make.Modifiers(Flags.PARAMETER),
                                            names.fromString("name"),
                                            make.Type(syms.stringType), null)),
                      List.<JCExpression>nil(), // thrown
                      null, //make.Block(0, Tree.emptyList.prepend(make.Return(make.Ident(names._null)))),
                      null);
        memberEnter(valueOf, env);

        // the remaining members are for bootstrapping only
        if (!target.compilerBootstrap(tree.sym)) return;

        // public final int ordinal() { return ???; }
        JCMethodDecl ordinal = make.at(tree.pos).
            MethodDef(make.Modifiers(Flags.PUBLIC|Flags.FINAL),
                      names.ordinal,
                      make.Type(syms.intType),
                      List.<JCTypeParameter>nil(),
                      List.<JCVariableDecl>nil(),
                      List.<JCExpression>nil(),
                      null,
                      null);
        memberEnter(ordinal, env);

        // public final String name() { return ???; }
        JCMethodDecl name = make.
            MethodDef(make.Modifiers(Flags.PUBLIC|Flags.FINAL),
                      names._name,
                      make.Type(syms.stringType),
                      List.<JCTypeParameter>nil(),
                      List.<JCVariableDecl>nil(),
                      List.<JCExpression>nil(),
                      null,
                      null);
        memberEnter(name, env);

        // public int compareTo(E other) { return ???; }
        MethodSymbol compareTo = new
            MethodSymbol(Flags.PUBLIC,
                         names.compareTo,
                         new MethodType(List.of(tree.sym.type),
                                        syms.intType,
                                        List.<Type>nil(),
                                        syms.methodClass),
                         tree.sym);
        memberEnter(make.MethodDef(compareTo, null), env);
        
        }finally{//我加上的
    	DEBUG.P("tree="+tree);
		DEBUG.P(0,this,"addEnumMembers(2)");
		}
    }