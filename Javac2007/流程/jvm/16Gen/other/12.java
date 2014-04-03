    public void visitVarDef(JCVariableDecl tree) {
    DEBUG.P(this,"visitVarDef(1)");
    
	MethodSymbol oldMethodSym = currentMethodSym;
	tree.mods = translate(tree.mods);
	tree.vartype = translate(tree.vartype);
	
	DEBUG.P("currentMethodSym="+currentMethodSym);
	if (currentMethodSym == null) {
	    // A class or instance field initializer.
	    currentMethodSym =
		new MethodSymbol((tree.mods.flags&STATIC) | BLOCK,
				 names.empty, null,
				 currentClass);		 
	}
	if (tree.init != null) tree.init = translate(tree.init, tree.type);
	result = tree;
	currentMethodSym = oldMethodSym;
	
	DEBUG.P(1,this,"visitVarDef(1)");	
    }