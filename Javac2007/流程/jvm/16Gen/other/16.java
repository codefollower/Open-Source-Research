    /** Given an erased reference type, assume this type as the tree's type.
     *  Then, coerce to some given target type unless target type is null.
     *  This operation is used in situations like the following:
     *
     *  class Cell<A> { A value; }
     *  ...
     *  Cell<Integer> cell;
     *  Integer x = cell.value;
     *
     *  Since the erasure of Cell.value is Object, but the type
     *  of cell.value in the assignment is Integer, we need to
     *  adjust the original type of cell.value to Object, and insert
     *  a cast to Integer. That is, the last assignment becomes:
     *
     *  Integer x = (Integer)cell.value;
     *
     *  @param tree       The expression tree whose type might need adjustment.
     *  @param erasedType The expression's type after erasure.
     *  @param target     The target type, which is usually the erasure of the
     *                    expression's original type.
     */
    JCExpression retype(JCExpression tree, Type erasedType, Type target) {
    	try {//我加上的
		DEBUG.P(this,"retype(3)");
		DEBUG.P("JCExpression tree="+tree);
		DEBUG.P("Type erasedType="+erasedType);
		DEBUG.P("Type target="+target);
		DEBUG.P("erasedType.tag="+erasedType.tag);
		DEBUG.P("lastBaseTag="+lastBaseTag);

//      System.err.println("retype " + tree + " to " + erasedType);//DEBUG
        if (erasedType.tag > lastBaseTag) {
            if (target != null && target.isPrimitive())
                target = erasure(tree.type);
            tree.type = erasedType;
            if (target != null) return coerce(tree, target);
        }
        return tree;
        
        }finally{//我加上的
		DEBUG.P(0,this,"retype(3)");
		}
    }