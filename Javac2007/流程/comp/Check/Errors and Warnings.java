/* *************************************************************************
 * Errors and Warnings
 **************************************************************************/

    Lint setLint(Lint newLint) {
		Lint prev = lint;
		lint = newLint;
		return prev;
    }

    /** Warn about deprecated symbol.
     *  @param pos        Position to be used for error reporting.
     *  @param sym        The deprecated symbol.
     */ 
    void warnDeprecated(DiagnosticPosition pos, Symbol sym) {
		if (!lint.isSuppressed(LintCategory.DEPRECATION))
			deprecationHandler.report(pos, "has.been.deprecated", sym, sym.location());
    }

    /** Warn about unchecked operation.
     *  @param pos        Position to be used for error reporting.
     *  @param msg        A string describing the problem.
     */
    public void warnUnchecked(DiagnosticPosition pos, String msg, Object... args) {
	if (!lint.isSuppressed(LintCategory.UNCHECKED))
	    uncheckedHandler.report(pos, msg, args);
    }

    /**
     * Report any deferred diagnostics.
     */
    public void reportDeferredDiagnostics() {
	deprecationHandler.reportDeferredDiagnostic();
	uncheckedHandler.reportDeferredDiagnostic();
    }


    /** Report a failure to complete a class.
     *  @param pos        Position to be used for error reporting.
     *  @param ex         The failure to report.
     */
    public Type completionError(DiagnosticPosition pos, CompletionFailure ex) {
		log.error(pos, "cant.access", ex.sym, ex.errmsg);
		//com.sun.tools.javac.jvm.ClassReader.BadClassFile继承自
		//com.sun.tools.javac.code.Symbol.CompletionFailure
		if (ex instanceof ClassReader.BadClassFile) throw new Abort();
		else return syms.errType;
    }

    /** Report a type error.
     *  @param pos        Position to be used for error reporting.
     *  @param problem    A string describing the error.
     *  @param found      The type that was found.
     *  @param req        The type that was required.
     */
    Type typeError(DiagnosticPosition pos, Object problem, Type found, Type req) {
	log.error(pos, "prob.found.req",
		  problem, found, req);
	return syms.errType;
    }

    Type typeError(DiagnosticPosition pos, String problem, Type found, Type req, Object explanation) {
	log.error(pos, "prob.found.req.1", problem, found, req, explanation);
	return syms.errType;
    }

    /** Report an error that wrong type tag was found.
     *  @param pos        Position to be used for error reporting.
     *  @param required   An internationalized string describing the type tag
     *                    required.
     *  @param found      The type that was found.
     */
    Type typeTagError(DiagnosticPosition pos, Object required, Object found) {
	log.error(pos, "type.found.req", found, required);
	return syms.errType;
    }

    /** Report an error that symbol cannot be referenced before super
     *  has been called.
     *  @param pos        Position to be used for error reporting.
     *  @param sym        The referenced symbol.
     */
    void earlyRefError(DiagnosticPosition pos, Symbol sym) {
	log.error(pos, "cant.ref.before.ctor.called", sym);
    }

    /** Report duplicate declaration error.
     */
    void duplicateError(DiagnosticPosition pos, Symbol sym) {
    DEBUG.P(this,"duplicateError(2)");
	DEBUG.P("sym="+sym);
	
	if (!sym.type.isErroneous()) {
	    log.error(pos, "already.defined", sym, sym.location());
	}
	
	DEBUG.P(0,this,"duplicateError(2)");
    }

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
