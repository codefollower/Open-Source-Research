/* *************************************************************************
 * Exception checking
 **************************************************************************/

    /* The following methods treat classes as sets that contain
     * the class itself and all their subclasses
     */

    /** Is given type a subtype of some of the types in given list?
     */
    boolean subset(Type t, List<Type> ts) {
		for (List<Type> l = ts; l.nonEmpty(); l = l.tail)
			if (types.isSubtype(t, l.head)) return true;
		return false;
    }

    /** Is given type a subtype or supertype of
     *  some of the types in given list?
     */
    boolean intersects(Type t, List<Type> ts) {
		for (List<Type> l = ts; l.nonEmpty(); l = l.tail)
			if (types.isSubtype(t, l.head) || types.isSubtype(l.head, t)) return true;
		return false;
    }

    /** Add type set to given type list, unless it is a subclass of some class
     *  in the list.
     */
    List<Type> incl(Type t, List<Type> ts) {
		return subset(t, ts) ? ts : excl(t, ts).prepend(t);
    }

    /** Remove type set from type set list.
     */
    List<Type> excl(Type t, List<Type> ts) {
		if (ts.isEmpty()) {
			return ts;
		} else {
			List<Type> ts1 = excl(t, ts.tail);
			if (types.isSubtype(ts.head, t)) return ts1;
			else if (ts1 == ts.tail) return ts;
			else return ts1.prepend(ts.head);
		}
    }

    /** Form the union of two type set lists.
     */
    List<Type> union(List<Type> ts1, List<Type> ts2) {
		DEBUG.P(this,"union(2)");	
		List<Type> ts = ts1;
		for (List<Type> l = ts2; l.nonEmpty(); l = l.tail)
			ts = incl(l.head, ts);
		DEBUG.P(0,this,"union(2)");	    
		return ts;
    }

    /** Form the difference of two type lists.
     */
    List<Type> diff(List<Type> ts1, List<Type> ts2) {
		List<Type> ts = ts1;
		for (List<Type> l = ts2; l.nonEmpty(); l = l.tail)
			ts = excl(l.head, ts);
		return ts;
    }

    /** Form the intersection of two type lists.
     */
    public List<Type> intersect(List<Type> ts1, List<Type> ts2) {
		DEBUG.P(this,"intersect(2)");
		List<Type> ts = List.nil();
		for (List<Type> l = ts1; l.nonEmpty(); l = l.tail)
			if (subset(l.head, ts2)) ts = incl(l.head, ts);
		for (List<Type> l = ts2; l.nonEmpty(); l = l.tail)
			if (subset(l.head, ts1)) ts = incl(l.head, ts);
		DEBUG.P(0,this,"intersect(2)");
		return ts;
    }

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