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
		if(code.crt!=null) DEBUG.P("crtFlags="+code.crt.getTypes(crtFlags));
		
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