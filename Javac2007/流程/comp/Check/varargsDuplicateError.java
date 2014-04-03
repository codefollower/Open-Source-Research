    /** Report array/varargs duplicate declaration 
     */
    void varargsDuplicateError(DiagnosticPosition pos, Symbol sym1, Symbol sym2) {
	DEBUG.P(this,"varargsDuplicateError(3)");
	DEBUG.P("sym1="+sym1+"  sym2="+sym2);
	
	if (!sym1.type.isErroneous() && !sym2.type.isErroneous()) {
	    log.error(pos, "array.and.varargs", sym1, sym2, sym2.location());
	}
	
	DEBUG.P(this,"varargsDuplicateError(3)");
    }