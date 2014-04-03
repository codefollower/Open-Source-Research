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


	    /** Enter members for a class.
     */
    void finishClass(JCClassDecl tree, Env<AttrContext> env) {
    	DEBUG.P(this,"finishClass(2)");
    	DEBUG.P("tree="+tree);
    	DEBUG.P("env="+env);
    	
        if ((tree.mods.flags & Flags.ENUM) != 0 &&
            (types.supertype(tree.sym.type).tsym.flags() & Flags.ENUM) == 0) {
            addEnumMembers(tree, env);
        }
        memberEnter(tree.defs, env);
        
        DEBUG.P(0,this,"finishClass(2)");
    }

	    /** Enter members from a list of trees.
     */
    void memberEnter(List<? extends JCTree> trees, Env<AttrContext> env) {
    	DEBUG.P(this,"memberEnter(List<? extends JCTree> trees, Env<AttrContext> env)");
        DEBUG.P("trees.size="+trees.size());
        for (List<? extends JCTree> l = trees; l.nonEmpty(); l = l.tail) {
            memberEnter(l.head, env);
        }
        DEBUG.P(0,this,"memberEnter(List<? extends JCTree> trees, Env<AttrContext> env)");
    }

	    /** Enter field and method definitions and process import
     *  clauses, catching any completion failure exceptions.
     */
    protected void memberEnter(JCTree tree, Env<AttrContext> env) {
    	DEBUG.P(this,"memberEnter(2)");
    	DEBUG.P("tree.tag="+tree.myTreeTag());
    	DEBUG.P("先前Env="+this.env);
		DEBUG.P("当前Env="+env);

        Env<AttrContext> prevEnv = this.env;       
        try {
            this.env = env;
            tree.accept(this);
        }  catch (CompletionFailure ex) {
            chk.completionError(tree.pos(), ex);
        } finally {
            this.env = prevEnv;
            DEBUG.P(1,this,"memberEnter(2)");
        }
    }