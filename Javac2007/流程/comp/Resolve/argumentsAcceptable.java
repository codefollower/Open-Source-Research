    /** Check if a parameter list accepts a list of args.
     */
    boolean argumentsAcceptable(List<Type> argtypes,
                                List<Type> formals,
                                boolean allowBoxing,
                                boolean useVarargs,
                                Warner warn) {
        Type varargsFormal = useVarargs ? formals.last() : null;
        while (argtypes.nonEmpty() && formals.head != varargsFormal) {
            boolean works = allowBoxing
                ? types.isConvertible(argtypes.head, formals.head, warn)
                : types.isSubtypeUnchecked(argtypes.head, formals.head, warn);
            if (!works) return false;
            argtypes = argtypes.tail;
            formals = formals.tail;
        }
        if (formals.head != varargsFormal) return false; // not enough args
        if (!useVarargs)
            return argtypes.isEmpty();
        Type elt = types.elemtype(varargsFormal);
        while (argtypes.nonEmpty()) {
            if (!types.isConvertible(argtypes.head, elt, warn))
                return false;
            argtypes = argtypes.tail;
        }
        return true;
    }