    public void visitLiteral(JCLiteral tree) {
		DEBUG.P(this,"visitLiteral(1)");
		DEBUG.P("tree="+tree);
		DEBUG.P("tree.type.tag="+TypeTags.toString(tree.type.tag));
		if (tree.type.tag == TypeTags.BOT) {
			code.emitop0(aconst_null);
			
			DEBUG.P("types.dimensions(pt)="+types.dimensions(pt));
			if (types.dimensions(pt) > 1) {//大于等于二维数组时条件才为true
				code.emitop2(checkcast, makeRef(tree.pos(), pt));
				result = items.makeStackItem(pt);
			} else {
				result = items.makeStackItem(tree.type);
			}
		}
		else
			result = items.makeImmediateItem(tree.type, tree.value);
		
		DEBUG.P(0,this,"visitLiteral(1)");
    }