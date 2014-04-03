    /** Report duplicate declaration error.
     */
    void duplicateError(DiagnosticPosition pos, Symbol sym) {
    DEBUG.P(this,"duplicateError(2)");
	DEBUG.P("sym="+sym);
	
	if (!sym.type.isErroneous()) {
	    log.error(pos, "already.defined", sym, sym.location());
	}