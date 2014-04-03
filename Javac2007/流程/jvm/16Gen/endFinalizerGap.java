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