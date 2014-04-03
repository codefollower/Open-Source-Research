    /** Derived visitor method: generate code for a statement.
     */
    public void genStat(JCTree tree, Env<GenContext> env) {
    DEBUG.P(this,"genStat(2)");
    DEBUG.P("code.isAlive()="+code.isAlive());
    DEBUG.P("env.info.isSwitch="+env.info.isSwitch);
	if (code.isAlive()) {
	    code.statBegin(tree.pos);
	    genDef(tree, env);
	} else if (env.info.isSwitch && tree.tag == JCTree.VARDEF) {
	    // variables whose declarations are in a switch
	    // can be used even if the decl is unreachable.
	    code.newLocal(((JCVariableDecl) tree).sym);
	}
	
	DEBUG.P(0,this,"genStat(2)");
    }