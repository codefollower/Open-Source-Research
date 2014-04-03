    /** Derived visitor method: check whether CharacterRangeTable
     *  should be emitted, if so, put a new entry into CRTable
     *  and call method to generate bytecode.
     *  If not, just call method to generate bytecode.
     *  @see    #genStat(Tree, Env)
     *
     *  @param  tree     The tree to be visited.
     *  @param  env      The environment to use.
     *  @param  crtFlags The CharacterRangeTable flags
     *                   indicating type of the entry.
     */
    public void genStat(JCTree tree, Env<GenContext> env, int crtFlags) {
    try {//我加上的
	DEBUG.P(this,"genStat(3)");
	DEBUG.P("env="+env);
    DEBUG.P("genCrt="+genCrt);
    DEBUG.P("crtFlags="+crtFlags);
    
	if (!genCrt) {
	    genStat(tree, env);
	    return;
	}
	int startpc = code.curPc();
	genStat(tree, env);
	if (tree.tag == JCTree.BLOCK) crtFlags |= CRT_BLOCK;
	code.crt.put(tree, crtFlags, startpc, code.curPc());
	
	}finally{//我加上的
	DEBUG.P(0,this,"genStat(3)");
	}
    }