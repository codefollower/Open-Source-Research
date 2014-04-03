
    /** Same, but handling completion failures.
     */
    boolean isUnchecked(DiagnosticPosition pos, Type exc) {
	try {
	    return isUnchecked(exc);
	} catch (CompletionFailure ex) {
	    completionError(pos, ex);
	    return true;
	}
    }

    /** Is exc an exception type that need not be declared?
     */
    boolean isUnchecked(Type exc) {
	return
	    (exc.tag == TYPEVAR) ? isUnchecked(types.supertype(exc)) :
	    (exc.tag == CLASS) ? isUnchecked((ClassSymbol)exc.tsym) :
	    exc.tag == BOT;
    }

    /** Is exc an exception symbol that need not be declared?
     */
    boolean isUnchecked(ClassSymbol exc) {
	return
	    exc.kind == ERR ||
	    exc.isSubClass(syms.errorType.tsym, types) ||
	    exc.isSubClass(syms.runtimeExceptionType.tsym, types);
    }

