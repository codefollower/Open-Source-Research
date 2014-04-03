    /** Visitor method: generate code for an expression, catching and reporting
     *  any completion failures.
     *  @param tree    The expression to be visited.
     *  @param pt      The expression's expected type (proto-type).
     */
    public Item genExpr(JCTree tree, Type pt) {
        DEBUG.P(this,"genExpr(JCTree tree, Type pt)");
        DEBUG.P("tree="+tree);
        DEBUG.P("tree.type.constValue()="+tree.type.constValue());
        DEBUG.P("pt="+pt);
        Type prevPt = this.pt;

        Item myItemResult=null;//我加上的
		try {
			if (tree.type.constValue() != null) {
				// Short circuit any expressions which are constants
				checkStringConstant(tree.pos(), tree.type.constValue());
				result = items.makeImmediateItem(tree.type, tree.type.constValue());
			} else {
				DEBUG.P("tree.tag="+tree.myTreeTag());
				this.pt = pt;
				tree.accept(this);
			}
			
			myItemResult=result.coerce(pt);//我加上的
			return myItemResult;//我加上的
			//coerce(Type targettype),coerce(int targetcode)在Items.Item中定义,
			//只有Items.ImmediateItem覆盖了coerce(int targetcode)
			//return result.coerce(pt);
		} catch (CompletionFailure ex) {
			chk.completionError(tree.pos(), ex);
			code.state.stacksize = 1;
			return items.makeStackItem(pt);
		} finally {
			this.pt = prevPt;
			DEBUG.P("result="+result);
			DEBUG.P("myItemResult="+myItemResult);
			DEBUG.P("code.state="+code.state);
			DEBUG.P(0,this,"genExpr(JCTree tree, Type pt)");
		}
    }
