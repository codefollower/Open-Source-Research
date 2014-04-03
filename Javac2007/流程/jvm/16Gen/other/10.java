    public void visitMethodDef(JCMethodDecl tree) {
    DEBUG.P(this,"visitMethodDef(1)");
	DEBUG.P("tree.name="+tree.name);
	DEBUG.P("currentClass="+currentClass);
	DEBUG.P("currentClass.flags_field="+Flags.toString(currentClass.flags_field));

	if (tree.name == names.init && (currentClass.flags_field&ENUM) != 0) {
	    // Add "String $enum$name, int $enum$ordinal" to the beginning of the
	    // argument list for each constructor of an enum.
	    JCVariableDecl nameParam = make_at(tree.pos()).
		Param(names.fromString(target.syntheticNameChar() +
				       "enum" + target.syntheticNameChar() + "name"),
		      syms.stringType, tree.sym);
	    nameParam.mods.flags |= SYNTHETIC; nameParam.sym.flags_field |= SYNTHETIC;

	    JCVariableDecl ordParam = make.
		Param(names.fromString(target.syntheticNameChar() +
				       "enum" + target.syntheticNameChar() +
				       "ordinal"),
		      syms.intType, tree.sym);
	    ordParam.mods.flags |= SYNTHETIC; ordParam.sym.flags_field |= SYNTHETIC;

	    tree.params = tree.params.prepend(ordParam).prepend(nameParam);

	    MethodSymbol m = tree.sym;
	    Type olderasure = m.erasure(types);
	    m.erasure_field = new MethodType(
		olderasure.getParameterTypes().prepend(syms.intType).prepend(syms.stringType),
		olderasure.getReturnType(),
		olderasure.getThrownTypes(),
		syms.methodClass);

            if (target.compilerBootstrap(m.owner)) {
                // Initialize synthetic name field
                Symbol nameVarSym = lookupSynthetic(names.fromString("$name"),
                                                    tree.sym.owner.members());
                JCIdent nameIdent = make.Ident(nameParam.sym);
                JCIdent id1 = make.Ident(nameVarSym);
                JCAssign newAssign = make.Assign(id1, nameIdent);
                newAssign.type = id1.type;
                JCExpressionStatement nameAssign = make.Exec(newAssign);
                nameAssign.type = id1.type;
                tree.body.stats = tree.body.stats.prepend(nameAssign);

                // Initialize synthetic ordinal field
                Symbol ordinalVarSym = lookupSynthetic(names.fromString("$ordinal"),
                                                       tree.sym.owner.members());
                JCIdent ordIdent = make.Ident(ordParam.sym);
                id1 = make.Ident(ordinalVarSym);
                newAssign = make.Assign(id1, ordIdent);
                newAssign.type = id1.type;
                JCExpressionStatement ordinalAssign = make.Exec(newAssign);
                ordinalAssign.type = id1.type;
                tree.body.stats = tree.body.stats.prepend(ordinalAssign);
            }
	}

	JCMethodDecl prevMethodDef = currentMethodDef;
	MethodSymbol prevMethodSym = currentMethodSym;
	try {
	    currentMethodDef = tree;
	    currentMethodSym = tree.sym;
	    visitMethodDefInternal(tree);
	} finally {
	    currentMethodDef = prevMethodDef;
	    currentMethodSym = prevMethodSym;
	}
	
	DEBUG.P(1,this,"visitMethodDef(1)");
    }