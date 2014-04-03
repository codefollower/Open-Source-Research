    /** Check that classes (or interfaces) do not each define an abstract
     *  method with same name and arguments but incompatible return types.
     *  @param pos          Position to be used for error reporting.
     *  @param t1           The first argument type.
     *  @param t2           The second argument type.
     */
        public boolean checkCompatibleAbstracts(DiagnosticPosition pos,
					    Type t1,
					    Type t2) {
	try {//我加上的
	DEBUG.P(this,"checkCompatibleAbstracts(3)");
	DEBUG.P("t1="+t1);
	DEBUG.P("t2="+t2);

        return checkCompatibleAbstracts(pos, t1, t2,
                                        types.makeCompoundType(t1, t2));
    }finally{//我加上的
	DEBUG.P(0,this,"checkCompatibleAbstracts(3)");
	}
    }

	public boolean checkCompatibleAbstracts(DiagnosticPosition pos,
					    Type t1,
					    Type t2,
					    Type site) {
	boolean checkCompatibleAbstracts=false;//我加上的
	try {//我加上的
	DEBUG.P(this,"checkCompatibleAbstracts(4)");
	DEBUG.P("t1="+t1);
	DEBUG.P("t2="+t2);
	DEBUG.P("site="+site);
	
	Symbol sym = firstIncompatibility(t1, t2, site);
	DEBUG.P("");DEBUG.P("sym="+sym);
	if (sym != null) {
	    log.error(pos, "types.incompatible.diff.ret",
		      t1, t2, sym.name +
		      "(" + types.memberType(t2, sym).getParameterTypes() + ")");
	    return false;
	}
	checkCompatibleAbstracts=true;//我加上的
	return true;

	}finally{//我加上的
	DEBUG.P("checkCompatibleAbstracts="+checkCompatibleAbstracts);
	DEBUG.P(0,this,"checkCompatibleAbstracts(4)");
	}
    }