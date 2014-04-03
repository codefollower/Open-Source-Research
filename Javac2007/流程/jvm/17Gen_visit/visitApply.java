    public void visitApply(JCMethodInvocation tree) {
    DEBUG.P(this,"visitApply(1)");	
	// Generate code for method.
	Item m = genExpr(tree.meth, methodType);
	// Generate code for all arguments, where the expected types are
	// the parameters of the method's external type (that is, any implicit
	// outer instance of a super(...) call appears as first parameter).
	genArgs(tree.args,
		TreeInfo.symbol(tree.meth).externalType(types).getParameterTypes());
	result = m.invoke();
	DEBUG.P(0,this,"visitApply(1)");	
    }