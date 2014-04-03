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