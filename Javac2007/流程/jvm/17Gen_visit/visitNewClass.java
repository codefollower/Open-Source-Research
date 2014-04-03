    public void visitNewClass(JCNewClass tree) {
    DEBUG.P(this,"visitNewClass(1)");
	// Enclosing instances or anonymous classes should have been eliminated
	// by now.
	assert tree.encl == null && tree.def == null;

	code.emitop2(new_, makeRef(tree.pos(), tree.type));
	code.emitop0(dup);

	// Generate code for all arguments, where the expected types are
	// the parameters of the constructor's external type (that is,
	// any implicit outer instance appears as first parameter).
	genArgs(tree.args, tree.constructor.externalType(types).getParameterTypes());

	items.makeMemberItem(tree.constructor, true).invoke();
	result = items.makeStackItem(tree.type);
	DEBUG.P(0,this,"visitNewClass(1)");
    }