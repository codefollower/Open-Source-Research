    /** Resolve error class indicating that an instance member was accessed
     *  from a static context.
     */
    class StaticError extends ResolveError {
        StaticError(Symbol sym) {
            super(STATICERR, sym, "static error");
        }

        /** Report error.
         *  @param log       The error log to be used for error reporting.
         *  @param pos       The position to be used for error reporting.
         *  @param site      The original type from where the selection took place.
         *  @param name      The name of the symbol to be resolved.
         *  @param argtypes  The invocation's value arguments,
         *                   if we looked for a method.
         *  @param typeargtypes  The invocation's type arguments,
         *                   if we looked for a method.
         */
        void report(Log log,
                    DiagnosticPosition pos,
                    Type site,
                    Name name,
                    List<Type> argtypes,
                    List<Type> typeargtypes) {
            String symstr = ((sym.kind == TYP && sym.type.tag == CLASS)
                ? types.erasure(sym.type)
                : sym).toString();
            log.error(pos, "non-static.cant.be.ref",
                      kindName(sym), symstr);
        }
    }