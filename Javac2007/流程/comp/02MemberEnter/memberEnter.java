    /** Visitor argument: the current environment
     */
    protected Env<AttrContext> env;

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