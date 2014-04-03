    /** Visitor argument: the current environment.
     */
    Env<AttrContext> env;

    /** Visitor argument: the currently expected proto-kind.
     */
    int pkind;

    /** Visitor argument: the currently expected proto-type.
     */
    Type pt;

    /** Visitor result: the computed type.
     */
    Type result;

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
    	//DEBUG.P("env="+env);
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

				
				DEBUG.P("pkind="+Kinds.toString(pkind));
				DEBUG.P("tree     ="+tree);
    			DEBUG.P("tree.tag ="+tree.myTreeTag());
			if(tree.type!=null)  {
				DEBUG.P("tree.type          ="+tree.type);
				DEBUG.P("tree.type.tsym     ="+tree.type.tsym);
				if(tree.type.tsym!=null)
					DEBUG.P("tree.type.tsym.type="+tree.type.tsym.type);
			} else 
				DEBUG.P("tree.type=null");
            
            DEBUG.P(0,this,"attribTree(4)");
        }
    }

    /** Derived visitor method: attribute an expression tree.
     */
    public Type attribExpr(JCTree tree, Env<AttrContext> env, Type pt) {
    	try {//我加上的
		DEBUG.P(this,"attribExpr(3)");
		
        return attribTree(tree, env, VAL, pt.tag != ERROR ? pt : Type.noType);
		
		}finally{//我加上的
		DEBUG.P(0,this,"attribExpr(3)");
		}
    }

    /** Derived visitor method: attribute an expression tree with
     *  no constraints on the computed type.
     */
    Type attribExpr(JCTree tree, Env<AttrContext> env) {
    	try {//我加上的
		DEBUG.P(this,"attribExpr(2)");
		
        return attribTree(tree, env, VAL, Type.noType);
		
		}finally{//我加上的
		DEBUG.P(0,this,"attribExpr(2)");
		}
        
    }

    /** Derived visitor method: attribute a type tree.
     */
    Type attribType(JCTree tree, Env<AttrContext> env) {
    	DEBUG.P(this,"attribType(2)");
        Type result = attribTree(tree, env, TYP, Type.noType);
        
        //DEBUG.P("result="+result);
        //DEBUG.P("result.tag="+TypeTags.toString(result.tag));
        DEBUG.P(0,this,"attribType(2)");
        return result;
    }

    /** Derived visitor method: attribute a statement or definition tree.
     */
    public Type attribStat(JCTree tree, Env<AttrContext> env) {
    	try {//我加上的
		DEBUG.P(this,"attribStat(2)");
		
        return attribTree(tree, env, NIL, Type.noType);
        
        }finally{//我加上的
		DEBUG.P(1,this,"attribStat(2)");
		}
    }

    /** Attribute a list of expressions, returning a list of types.
     */
    List<Type> attribExprs(List<JCExpression> trees, Env<AttrContext> env, Type pt) {
		DEBUG.P(this,"attribExprs(3)");
        ListBuffer<Type> ts = new ListBuffer<Type>();
        for (List<JCExpression> l = trees; l.nonEmpty(); l = l.tail)
            ts.append(attribExpr(l.head, env, pt));

		DEBUG.P(0,this,"attribExprs(3)");
        return ts.toList();
    }

    /** Attribute a list of statements, returning nothing.
     */
    <T extends JCTree> void attribStats(List<T> trees, Env<AttrContext> env) {
    	DEBUG.P(this,"attribStats(2)");
        for (List<T> l = trees; l.nonEmpty(); l = l.tail)
            attribStat(l.head, env);
        DEBUG.P(0,this,"attribStats(2)");
    }

    /** Attribute the arguments in a method call, returning a list of types.
     */
    List<Type> attribArgs(List<JCExpression> trees, Env<AttrContext> env) {
    	try {//我加上的
		DEBUG.P(this,"attribArgs(2)");
		DEBUG.P("trees="+trees);
		//DEBUG.P("env="+env);
		
        ListBuffer<Type> argtypes = new ListBuffer<Type>();
        for (List<JCExpression> l = trees; l.nonEmpty(); l = l.tail)
            argtypes.append(chk.checkNonVoid(
                l.head.pos(), types.upperBound(attribTree(l.head, env, VAL, Infer.anyPoly))));
        return argtypes.toList();
        
        }finally{//我加上的
		DEBUG.P(0,this,"attribArgs(2)");
		}
    }

    /** Attribute a type argument list, returning a list of types.
     */
    List<Type> attribTypes(List<JCExpression> trees, Env<AttrContext> env) {
    	DEBUG.P(this,"attribTypes(2)");
    	DEBUG.P("trees="+trees);
		//DEBUG.P("env="+env);
        ListBuffer<Type> argtypes = new ListBuffer<Type>();
        for (List<JCExpression> l = trees; l.nonEmpty(); l = l.tail)
            argtypes.append(chk.checkRefType(l.head.pos(), attribType(l.head, env)));
        
        DEBUG.P(0,this,"attribTypes(2)");
        return argtypes.toList();
    }