    /** Construct an attributed tree for a cast of expression to target type,
     *  unless it already has precisely that type.
     *  @param tree    The expression tree.
     *  @param target  The target type.
     */
    JCExpression cast(JCExpression tree, Type target) {
    	try {//我加上的
		DEBUG.P(this,"cast(2)");
		DEBUG.P("tree="+tree);
		DEBUG.P("target="+target);
		
        int oldpos = make.pos;
        make.at(tree.pos);
        if (!types.isSameType(tree.type, target)) {
            tree = make.TypeCast(make.Type(target), tree).setType(target);
        }
        make.pos = oldpos;
        
        DEBUG.P("tree="+tree);
        
        return tree;
        
        }finally{//我加上的
		DEBUG.P(0,this,"cast(2)");
		}
    }