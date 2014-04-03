    /** Check that variable does not hide variable with same name in
     *	immediately enclosing local scope.
     *	@param pos	     Position for error reporting.
     *	@param v	     The symbol.
     *	@param s	     The scope.
     */
    void checkTransparentVar(DiagnosticPosition pos, VarSymbol v, Scope s) {
		try {//我加上的
		DEBUG.P(this,"checkTransparentVar(3)");
		DEBUG.P("VarSymbol v="+v);
		DEBUG.P("Scope s="+s);
		DEBUG.P("s.next="+s.next);
		
		if (s.next != null) {
			Scope.Entry e2 = s.next.lookup(v.name);
			DEBUG.P("e2.scope="+e2.scope);
			if(e2.scope != null) {
				DEBUG.P("e.sym.owner="+e2.sym.owner);
				DEBUG.P("v.owner="+v.owner);
			}
			for (Scope.Entry e = s.next.lookup(v.name);
				 e.scope != null && e.sym.owner == v.owner;
				 e = e.next()) {
				if (e.sym.kind == VAR &&
				   (e.sym.owner.kind & (VAR | MTH)) != 0 &&
				    v.name != names.error) {
					//如:void methodD(int i) { int i; }
					duplicateError(pos, e.sym);
					return;
				}
			}
		}		
		}finally{//我加上的
		DEBUG.P(0,this,"checkTransparentVar(3)");
		}
    }