/* ************************************************************************
 * Non-local exits
 *************************************************************************/

    /** Generate code to invoke the finalizer associated with given
     *  environment.
     *  Any calls to finalizers are appended to the environments `cont' chain.
     *  Mark beginning of gap in catch all range for finalizer.
     */
    void genFinalizer(Env<GenContext> env) {
		DEBUG.P(this,"genFinalizer(1)");
		DEBUG.P("env.info前="+env.info);

		if (code.isAlive() && env.info.finalize != null)
			env.info.finalize.gen();

		DEBUG.P("env.info后="+env.info);
		DEBUG.P(0,this,"genFinalizer(1)");
    }

    /** Generate code to call all finalizers of structures aborted by
     *  a non-local
     *  exit.  Return target environment of the non-local exit.
     *  @param target      The tree representing the structure that's aborted
     *  @param env         The environment current at the non-local exit.
     */
    Env<GenContext> unwind(JCTree target, Env<GenContext> env) {
		DEBUG.P(this,"unwind(2)");
		DEBUG.P("target="+target);
		DEBUG.P("env="+env);
		
		Env<GenContext> env1 = env;
		while (true) {
			genFinalizer(env1);
			if (env1.tree == target) break;
			env1 = env1.next;
		}
		
		DEBUG.P("env1="+env1);
		DEBUG.P(0,this,"unwind(2)");
		return env1;
    }

    /** Mark end of gap in catch-all range for finalizer.
     *  @param env   the environment which might contain the finalizer
     *               (if it does, env.info.gaps != null).
     */
    void endFinalizerGap(Env<GenContext> env) {
    	DEBUG.P(this,"endFinalizerGap(1)");
		DEBUG.P("env.info前="+env.info);
    	
        if (env.info.gaps != null && env.info.gaps.length() % 2 == 1)
            env.info.gaps.append(code.curPc());
        
		DEBUG.P("env.info后="+env.info);
        DEBUG.P(0,this,"endFinalizerGap(1)");
    }

    /** Mark end of all gaps in catch-all ranges for finalizers of environments
     *  lying between, and including to two environments.
     *  @param from    the most deeply nested environment to mark
     *  @param to      the least deeply nested environment to mark
     */
    void endFinalizerGaps(Env<GenContext> from, Env<GenContext> to) {
		DEBUG.P(this,"endFinalizerGaps(2)");
		
		Env<GenContext> last = null;
		while (last != to) {
			endFinalizerGap(from);
			last = from;
			from = from.next;
		}
		
		DEBUG.P(0,this,"endFinalizerGaps(2)");
    }

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
