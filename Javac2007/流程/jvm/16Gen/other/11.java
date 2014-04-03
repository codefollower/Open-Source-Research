    //where
    private void visitMethodDefInternal(JCMethodDecl tree) {
    try {//我加上的
	DEBUG.P(this,"visitMethodDefInternal(1)");
	DEBUG.P("tree.name="+tree.name);
	DEBUG.P("currentClass.isInner()="+currentClass.isInner());
	DEBUG.P("currentClass.owner.kind="+Kinds.toString(currentClass.owner.kind));

	if (tree.name == names.init &&
	    (currentClass.isInner() ||
	     (currentClass.owner.kind & (VAR | MTH)) != 0)) {
	    // We are seeing a constructor of an inner class.
	    MethodSymbol m = tree.sym;

	    // Push a new proxy scope for constructor parameters.
	    // and create definitions for any this$n and proxy parameters.
	    proxies = proxies.dup(m);
	    List<VarSymbol> prevOuterThisStack = outerThisStack;
	    List<VarSymbol> fvs = freevars(currentClass);
	    JCVariableDecl otdef = null;
	    if (currentClass.hasOuterInstance())
		otdef = outerThisDef(tree.pos, m);
	    List<JCVariableDecl> fvdefs = freevarDefs(tree.pos, fvs, m);

	    // Recursively translate result type, parameters and thrown list.
	    tree.restype = translate(tree.restype);
	    tree.params = translateVarDefs(tree.params);
	    tree.thrown = translate(tree.thrown);

	    // when compiling stubs, don't process body
	    if (tree.body == null) {
		result = tree;
		return;
	    }

	    // Add this$n (if needed) in front of and free variables behind
	    // constructor parameter list.
	    tree.params = tree.params.appendList(fvdefs);
	    if (currentClass.hasOuterInstance())
		tree.params = tree.params.prepend(otdef);

	    // If this is an initial constructor, i.e., it does not start with
	    // this(...), insert initializers for this$n and proxies
	    // before (pre-1.4, after) the call to superclass constructor.
	    JCStatement selfCall = translate(tree.body.stats.head);

	    List<JCStatement> added = List.nil();
	    if (fvs.nonEmpty()) {
		List<Type> addedargtypes = List.nil();
		for (List<VarSymbol> l = fvs; l.nonEmpty(); l = l.tail) {
		    if (TreeInfo.isInitialConstructor(tree))
			added = added.prepend(
			    initField(tree.body.pos, proxyName(l.head.name)));
		    addedargtypes = addedargtypes.prepend(l.head.erasure(types));
		}
		Type olderasure = m.erasure(types);
		m.erasure_field = new MethodType(
		    olderasure.getParameterTypes().appendList(addedargtypes),
		    olderasure.getReturnType(),
		    olderasure.getThrownTypes(),
		    syms.methodClass);
	    }
	    if (currentClass.hasOuterInstance() &&
		TreeInfo.isInitialConstructor(tree))
	    {
		added = added.prepend(initOuterThis(tree.body.pos));
	    }

	    // pop local variables from proxy stack
	    proxies = proxies.leave();

	    // recursively translate following local statements and
	    // combine with this- or super-call
	    List<JCStatement> stats = translate(tree.body.stats.tail);
	    if (target.initializeFieldsBeforeSuper())
		tree.body.stats = stats.prepend(selfCall).prependList(added);
	    else
		tree.body.stats = stats.prependList(added).prepend(selfCall);

	    outerThisStack = prevOuterThisStack;
	} else {
	    super.visitMethodDef(tree);
	}
	result = tree;
	
	}finally{//我加上的
	DEBUG.P(1,this,"visitMethodDefInternal(1)");
	}
    }