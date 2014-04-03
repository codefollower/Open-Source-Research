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