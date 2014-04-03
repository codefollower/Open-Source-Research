    /** Is exc an exception symbol that need not be declared?
     */
    boolean isUnchecked(ClassSymbol exc) {
	return
	    exc.kind == ERR ||
	    exc.isSubClass(syms.errorType.tsym, types) ||
	    exc.isSubClass(syms.runtimeExceptionType.tsym, types);
    }

    /** Is exc an exception type that need not be declared?
     */
    boolean isUnchecked(Type exc) {
	return
	    (exc.tag == TYPEVAR) ? isUnchecked(types.supertype(exc)) :
	    (exc.tag == CLASS) ? isUnchecked((ClassSymbol)exc.tsym) :
	    exc.tag == BOT;
    }

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

    /** Is exc handled by given exception list?
     */
    boolean isHandled(Type exc, List<Type> handled) {
	return isUnchecked(exc) || subset(exc, handled);
    }

    /** Return all exceptions in thrown list that are not in handled list.
     *  @param thrown     The list of thrown exceptions.
     *  @param handled    The list of handled exceptions.
     */
    List<Type> unHandled(List<Type> thrown, List<Type> handled) {
	DEBUG.P(this,"unHandled(2)");
	DEBUG.P("thrown="+thrown);
	DEBUG.P("handled="+handled);

	List<Type> unhandled = List.nil();
	for (List<Type> l = thrown; l.nonEmpty(); l = l.tail)
	    if (!isHandled(l.head, handled)) unhandled = unhandled.prepend(l.head);

	DEBUG.P("unhandled="+unhandled);
	DEBUG.P(0,this,"unHandled(2)");
	return unhandled;
    }