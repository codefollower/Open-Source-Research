    public void visitVarDef(JCVariableDecl tree) {
		DEBUG.P(this,"visitVarDef(1)");
		VarSymbol v = tree.sym;
		code.newLocal(v);

		/*
		final int myMethodInt; //tree.init==null
		final int myMethodInt2=100; //tree.init!=null 且getConstValue()==100
		int myMethodInt3=200; //tree.init!=null 但getConstValue()==null
		在方法中定义的final类型的且在定义时就被赋值的是编译时常量
		*/
		DEBUG.P("tree.init="+tree.init);
		if (tree.init != null) {
			checkStringConstant(tree.init.pos(), v.getConstValue());

			DEBUG.P("v.getConstValue()="+v.getConstValue());
			DEBUG.P("varDebugInfo="+varDebugInfo);
			if (v.getConstValue() == null || varDebugInfo) {
				genExpr(tree.init, v.erasure(types)).load();
				items.makeLocalItem(v).store();
			}
		}
		checkDimension(tree.pos(), v.type);
		DEBUG.P(0,this,"visitVarDef(1)");
    }