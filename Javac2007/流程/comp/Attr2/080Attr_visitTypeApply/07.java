
    /** Is exc handled by given exception list?
     */
    boolean isHandled(Type exc, List<Type> handled) {
	return isUnchecked(exc) || subset(exc, handled);
    }

    /** Is given type a subtype of some of the types in given list?
     */
    boolean subset(Type t, List<Type> ts) {
	for (List<Type> l = ts; l.nonEmpty(); l = l.tail)
	    if (types.isSubtype(t, l.head)) return true;
	return false;
    }