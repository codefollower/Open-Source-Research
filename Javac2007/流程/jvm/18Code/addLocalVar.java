	/** Local variables, indexed by register. */
    LocalVar[] lvar;

    /** Add a new local variable. */
    private void addLocalVar(VarSymbol v) {
		DEBUG.P(this,"addLocalVar(VarSymbol v)");
		DEBUG.P("v="+v+" v.adr="+v.adr+" lvar.length="+lvar.length);
		for(int i=0;i<lvar.length;i++) 
			if(lvar[i]!=null) DEBUG.P("lvar["+i+"]="+lvar[i]);
		DEBUG.P("");
			
		int adr = v.adr;
		if (adr+1 >= lvar.length) {
			int newlength = lvar.length << 1;
			if (newlength <= adr) newlength = adr + 10;
			LocalVar[] new_lvar = new LocalVar[newlength];
			System.arraycopy(lvar, 0, new_lvar, 0, lvar.length);
			lvar = new_lvar;
		}
		assert lvar[adr] == null;
		DEBUG.P("pendingJumps="+pendingJumps);
		if (pendingJumps != null) resolvePending();
		lvar[adr] = new LocalVar(v);

		DEBUG.P("state.defined.excl前="+state.defined);
		state.defined.excl(adr);
		DEBUG.P("state.defined.excl后="+state.defined);
		
		DEBUG.P("");
		DEBUG.P("lvar.length="+lvar.length);
		for(int i=0;i<lvar.length;i++) 
			if(lvar[i]!=null) DEBUG.P("lvar["+i+"]="+lvar[i]);
		DEBUG.P(1,this,"addLocalVar(VarSymbol v)");
    }