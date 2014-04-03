    public void visitNewArray(JCNewArray tree) {
    DEBUG.P(this,"visitNewArray(1)");
    DEBUG.P("tree.elems="+tree.elems);
	if (tree.elems != null) {
	    Type elemtype = types.elemtype(tree.type);
	    loadIntConst(tree.elems.length());
	    Item arr = makeNewArray(tree.pos(), tree.type, 1);
		DEBUG.P("arr="+arr);
	    int i = 0;
	    for (List<JCExpression> l = tree.elems; l.nonEmpty(); l = l.tail) {
		arr.duplicate();
		loadIntConst(i);
		i++;
		genExpr(l.head, elemtype).load();
		items.makeIndexedItem(elemtype).store();
	    }
	    result = arr;
	} else {
	    for (List<JCExpression> l = tree.dims; l.nonEmpty(); l = l.tail) {
		genExpr(l.head, syms.intType).load();
	    }
	    result = makeNewArray(tree.pos(), tree.type, tree.dims.length());
	}
	DEBUG.P(0,this,"visitNewArray(1)");
    }