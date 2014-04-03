    /** Check that class does not have the same name as one of
     *	its enclosing classes, or as a class defined in its enclosing scope.
     *	return true if class is unique in its enclosing scope.
     *	@param pos	     Position for error reporting.
     *	@param name	     The class name.
     *	@param s	     The enclosing scope.
     */
    boolean checkUniqueClassName(DiagnosticPosition pos, Name name, Scope s) {
		try {//我加上的
		DEBUG.P(this,"checkUniqueClassName(3)");
		DEBUG.P("name="+name);
		DEBUG.P("Scope s="+s);
		
		//各成员名不能重复
		for (Scope.Entry e = s.lookup(name); e.scope == s; e = e.next()) {
			if (e.sym.kind == TYP && e.sym.name != names.error) {
			duplicateError(pos, e.sym);
			return false;
			}
		}
		
		//各成员名不能与此成员的直接或间接owner有相同名称
		for (Symbol sym = s.owner; sym != null; sym = sym.owner) {
			if (sym.kind == TYP && sym.name == name && sym.name != names.error) {
			duplicateError(pos, sym);
			return true;
			}
		}
		return true;
		
		}finally{//我加上的
		DEBUG.P(0,this,"checkUniqueClassName(3)");
		}
    }