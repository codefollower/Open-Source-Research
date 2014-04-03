    /** Do any of the structures aborted by a non-local exit have
     *  finalizers that require an empty stack?
     *  @param target      The tree representing the structure that's aborted
     *  @param env         The environment current at the non-local exit.
     */
    boolean hasFinally(JCTree target, Env<GenContext> env) {
	boolean hasFinally=true;//我加上的
	try {//我加上的
	DEBUG.P(this,"hasFinally(2)");

	while (env.tree != target) {
	    if (env.tree.tag == JCTree.TRY && env.info.finalize.hasFinalizer())
		return true;
	    env = env.next;
	}

	hasFinally=false;//我加上的

	return false;

	}finally{//我加上的
	DEBUG.P("hasFinally="+hasFinally);
	DEBUG.P(0,this,"hasFinally(2)");
	}
    }