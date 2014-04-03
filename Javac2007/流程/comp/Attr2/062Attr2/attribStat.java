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