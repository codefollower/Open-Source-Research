    public void visitMethodDef(JCMethodDecl tree) {
    DEBUG.P(this,"visitMethodDef(JCMethodDecl tree)");
	// Create a new local environment that points pack at method
	// definition.
	Env<GenContext> localEnv = env.dup(tree);
	localEnv.enclMethod = tree;

	DEBUG.P("localEnv="+localEnv);

	// The expected type of every return statement in this method
	// is the method's return type.
	this.pt = tree.sym.erasure(types).getReturnType();
	DEBUG.P("tree.sym="+tree.sym);
	DEBUG.P("this.pt="+this.pt);

	checkDimension(tree.pos(), tree.sym.erasure(types));
	genMethod(tree, localEnv, false);
	
	DEBUG.P(0,this,"visitMethodDef(JCMethodDecl tree)");
    }