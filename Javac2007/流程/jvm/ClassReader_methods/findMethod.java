    private MethodSymbol findMethod(NameAndType nt, Scope scope, long flags) {
        if (nt == null)
            return null;

        MethodType type = nt.type.asMethodType();

        for (Scope.Entry e = scope.lookup(nt.name); e.scope != null; e = e.next())
            if (e.sym.kind == MTH && isSameBinaryType(e.sym.type.asMethodType(), type))
                return (MethodSymbol)e.sym;

        if (nt.name != names.init)
            // not a constructor
            return null;
        if ((flags & INTERFACE) != 0)
            // no enclosing instance
            return null;
        if (nt.type.getParameterTypes().isEmpty())
            // no parameters
            return null;

        // A constructor of an inner class.
        // Remove the first argument (the enclosing instance)
        nt.type = new MethodType(nt.type.getParameterTypes().tail,
                                 nt.type.getReturnType(),
                                 nt.type.getThrownTypes(),
                                 syms.methodClass);
        // Try searching again
        return findMethod(nt, scope, flags);
    }