    /** Check that a class or interface does not hide a class or
     *	interface with same name in immediately enclosing local scope.
     *	@param pos	     Position for error reporting.
     *	@param c	     The symbol.
     *	@param s	     The scope.
     */
    void checkTransparentClass(DiagnosticPosition pos, ClassSymbol c, Scope s) {
		try {//我加上的
		DEBUG.P(this,"checkTransparentClass(3)");
		DEBUG.P("c="+c);
		DEBUG.P("s="+s);
		/*例:
		class EnterTest {
			void methodA() {
				class EnterTest{}
			}
		}*/
		if (s.next != null) {
			for (Scope.Entry e = s.next.lookup(c.name);
			 e.scope != null && e.sym.owner == c.owner;
			 e = e.next()) {
				if (e.sym.kind == TYP &&
					(e.sym.owner.kind & (VAR | MTH)) != 0 &&
					c.name != names.error) {
					duplicateError(pos, e.sym);
					return;
				}
			}
		}

		}finally{//我加上的
		DEBUG.P(0,this,"checkTransparentClass(3)");
		}
    }