    /** Create a tempory variable.
     *  @param type   The variable's type.
     */
    LocalItem makeTemp(Type type) {
	try {//我加上的
	DEBUG.P(this,"makeTemp(1)");
	DEBUG.P("type="+type);
	VarSymbol v = new VarSymbol(Flags.SYNTHETIC,
				    names.empty,
				    type,
				    env.enclMethod.sym);
	code.newLocal(v);
	return items.makeLocalItem(v);

	}finally{//我加上的
	DEBUG.P(0,this,"makeTemp(1)");
	}
    }