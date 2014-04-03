    /** Derived visitor method: check whether CharacterRangeTable
     *  should be emitted, if so, put a new entry into CRTable
     *  and call method to generate bytecode.
     *  If not, just call method to generate bytecode.
     *  @see    #genStats(List, Env)
     *
     *  @param  trees    The list of trees to be visited.
     *  @param  env      The environment to use.
     *  @param  crtFlags The CharacterRangeTable flags
     *                   indicating type of the entry.
     */
    public void genStats(List<JCStatement> trees, Env<GenContext> env, int crtFlags) {
		try {//我加上的
		DEBUG.P(this,"genStats(3)");
		DEBUG.P("env="+env);
		if(trees!=null) DEBUG.P("trees.size="+trees.size());
		else DEBUG.P("trees=null");
		DEBUG.P("genCrt="+genCrt);
		if(code.crt!=null) DEBUG.P("crtFlags="+code.crt.getTypes(crtFlags));
		
		if (!genCrt) {
			genStats(trees, env);
			return;
		}
		if (trees.length() == 1) {        // mark one statement with the flags
			genStat(trees.head, env, crtFlags | CRT_STATEMENT);
		} else {
			int startpc = code.curPc();
			genStats(trees, env);
			code.crt.put(trees, crtFlags, startpc, code.curPc());
		}
		
		}finally{//我加上的
		DEBUG.P(0,this,"genStats(3)");
		}
    }

    /** Derived visitor method: generate code for a list of statements.
     */
    public void genStats(List<? extends JCTree> trees, Env<GenContext> env) {
		DEBUG.P(this,"genStats(2)");
		DEBUG.P("env="+env);
		if(trees!=null) DEBUG.P("trees.size="+trees.size());
		else DEBUG.P("trees=null");
		
		for (List<? extends JCTree> l = trees; l.nonEmpty(); l = l.tail)
			genStat(l.head, env, CRT_STATEMENT);
		
		DEBUG.P(0,this,"genStats(2)");    
    }