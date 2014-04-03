    /** Visitor argument: The current environment.
     */
    Env<GenContext> env;

    /** Visitor argument: The expected type (prototype).
     */
    Type pt;

    /** Visitor result: The item representing the computed value.
     */
    Item result;

    /** Visitor method: generate code for a definition, catching and reporting
     *  any completion failures.
     *  @param tree    The definition to be visited.
     *  @param env     The environment current at the definition.
     */
    public void genDef(JCTree tree, Env<GenContext> env) {
        DEBUG.P(this,"genDef(2)");
        DEBUG.P("env="+env);
		DEBUG.P("tree.tag="+tree.myTreeTag());
		DEBUG.P("tree="+tree);
		
		Env<GenContext> prevEnv = this.env;
		try {
			this.env = env;
			tree.accept(this);
		} catch (CompletionFailure ex) {
			chk.completionError(tree.pos(), ex);
		} finally {
			this.env = prevEnv;
			DEBUG.P(0,this,"genDef(2)");
		}
    }