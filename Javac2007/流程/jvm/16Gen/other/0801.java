    public void visitIdent(JCIdent tree) {
    DEBUG.P(this,"visitIdent(1)");
	Symbol sym = tree.sym;
	
	DEBUG.P("tree.name="+tree.name);
	DEBUG.P("sym="+sym);
	DEBUG.P("sym.flags()="+Flags.toString(sym.flags()));
	DEBUG.P("sym.kind="+Kinds.toString(sym.kind));
	DEBUG.P("sym.owner="+sym.owner);
	if(sym.owner!=null) DEBUG.P("sym.owner.kind="+Kinds.toString(sym.owner.kind));
	
	DEBUG.P("code.state前="+code.state);
	if (tree.name == names._this || tree.name == names._super) {
	    Item res = tree.name == names._this
		? items.makeThisItem()
	        : items.makeSuperItem();
	    if (sym.kind == MTH) {
		// Generate code to address the constructor.
		res.load();
		
		//这里为true，说明不是一个virtual调用，而是Invokespecial
		//因为当前面两个if条件都为true时，源代码中要么是this()要么是super()
		res = items.makeMemberItem(sym, true);
	    }
	    result = res;
	} else if (sym.kind == VAR && sym.owner.kind == MTH) {
	    result = items.makeLocalItem((VarSymbol)sym);
	} else if ((sym.flags() & STATIC) != 0) {
	    if (!isAccessSuper(env.enclMethod))
		sym = binaryQualifier(sym, env.enclClass.type);
	    result = items.makeStaticItem(sym);
	} else {
	    items.makeThisItem().load();
	    sym = binaryQualifier(sym, env.enclClass.type);
	    result = items.makeMemberItem(sym, (sym.flags() & PRIVATE) != 0);
	}
	DEBUG.P("code.state后="+code.state);
	DEBUG.P(0,this,"visitIdent(1)");
    }