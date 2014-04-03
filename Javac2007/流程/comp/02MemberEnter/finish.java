    /** Enter member fields and methods of a class
     *  @param env        the environment current for the class block.
     */
    private void finish(Env<AttrContext> env) {
    	DEBUG.P(this,"finish(Env<AttrContext> env)");
    	DEBUG.P("env="+env);
    	
        JavaFileObject prev = log.useSource(env.toplevel.sourcefile);
        try {
            JCClassDecl tree = (JCClassDecl)env.tree;
            finishClass(tree, env);
        } finally {
            log.useSource(prev);
            
            DEBUG.P(0,this,"finish(Env<AttrContext> env)");
        }
    }