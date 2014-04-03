    public void visitIdent(JCIdent tree) {
		DEBUG.P(this,"visitIdent(1)");
		DEBUG.P("tree.sym.kind="+Kinds.toString(tree.sym.kind));
		
		//这里的JCIdent可能是方法名或者别的东西，所以要判断一下
		if (tree.sym.kind == VAR)
			checkInit(tree.pos(), (VarSymbol)tree.sym);
			
		DEBUG.P(0,this,"visitIdent(1)");    
    }