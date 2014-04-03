    /** Check that type is a class or interface type.
     *  @param pos           Position to be used for error reporting.
     *  @param t             The type to be checked.
     */
    Type checkClassType(DiagnosticPosition pos, Type t) {
    try {//我加上的
	DEBUG.P(this,"checkClassType(2)");
	DEBUG.P("t="+t+"  t.tag="+TypeTags.toString(t.tag));
	
	if (t.tag != CLASS && t.tag != ERROR)
            return typeTagError(pos,
                                JCDiagnostic.fragment("type.req.class"),
                                (t.tag == TYPEVAR)
                                ? JCDiagnostic.fragment("type.parameter", t)
                                : t); 
	else
	    return t;
	    
	}finally{//我加上的
	DEBUG.P(0,this,"checkClassType(2)");
	}

    }

	/** Derived visitor method: attribute a type tree.
     */
    Type attribType(JCTree tree, Env<AttrContext> env) {
    	DEBUG.P(this,"attribType(2)");
        Type result = attribTree(tree, env, TYP, Type.noType);
        
        DEBUG.P("result="+result);
		DEBUG.P("result.tag="+TypeTags.toString(result.tag));
        DEBUG.P(0,this,"attribType(2)");
        return result;
    }

    /** Visitor method: attribute a tree, catching any completion failure
     *  exceptions. Return the tree's type.
     *
     *  @param tree    The tree to be visited.
     *  @param env     The environment visitor argument.
     *  @param pkind   The protokind visitor argument.
     *  @param pt      The prototype visitor argument.
     */
    Type attribTree(JCTree tree, Env<AttrContext> env, int pkind, Type pt) {
    	DEBUG.P(this,"attribTree(4)");
    	DEBUG.P("tree="+tree);
    	DEBUG.P("tree.tag="+tree.myTreeTag());
    	DEBUG.P("env="+env);
    	DEBUG.P("pkind="+Kinds.toString(pkind));
    	DEBUG.P("pt="+pt);
    	DEBUG.P("pt.tag="+TypeTags.toString(pt.tag));
    	
        Env<AttrContext> prevEnv = this.env;
        int prevPkind = this.pkind;
        Type prevPt = this.pt;
        try {
            this.env = env;
            this.pkind = pkind;
            this.pt = pt;
            tree.accept(this);
            if (tree == breakTree) //当breakTree==tree==null时
                throw new BreakAttr(env);//是java.lang.RuntimeException的子类
            return result;
        } catch (CompletionFailure ex) {
            tree.type = syms.errType;
            return chk.completionError(tree.pos(), ex);
        } finally {
            this.env = prevEnv;
            this.pkind = prevPkind;
            this.pt = prevPt;
            
            DEBUG.P(0,this,"attribTree(4)");
        }
    }