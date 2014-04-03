    /** Construct an attributed tree to coerce an expression to some erased
     *  target type, unless the expression is already assignable to that type.
     *  If target type is a constant type, use its base type instead.
     *  @param tree    The expression tree.
     *  @param target  The target type.
     */
    JCExpression coerce(JCExpression tree, Type target) {
    	try {//我加上的
		DEBUG.P(this,"coerce(2)");
		DEBUG.P("tree="+tree);
		DEBUG.P("target="+target);

        Type btarget = target.baseType();
        
        DEBUG.P("btarget="+btarget);
        DEBUG.P("tree.type.isPrimitive()="+tree.type.isPrimitive());
        DEBUG.P("target.isPrimitive()="+target.isPrimitive());
        DEBUG.P("(tree.type.isPrimitive() == target.isPrimitive())="+(tree.type.isPrimitive() == target.isPrimitive()));
        
        if (tree.type.isPrimitive() == target.isPrimitive()) {
            return types.isAssignable(tree.type, btarget, Warner.noWarnings)
                ? tree
                : cast(tree, btarget);
        }
        return tree;
        
        }finally{//我加上的
		DEBUG.P(0,this,"coerce(2)");
		}
    }