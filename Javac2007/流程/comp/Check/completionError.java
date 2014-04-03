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