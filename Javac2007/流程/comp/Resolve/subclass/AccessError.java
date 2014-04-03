    /** Resolve error class indicating that a symbol is not accessible.
     */
    class AccessError extends ResolveError {

        AccessError(Symbol sym) {
            this(null, null, sym);
        }

        AccessError(Env<AttrContext> env, Type site, Symbol sym) {
            super(HIDDEN, sym, "access error");
            this.env = env;
            this.site = site;
            if (debugResolve)
                log.error("proc.messager", sym + " @ " + site + " is inaccessible.");
        }

        private Env<AttrContext> env;
        private Type site;

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
        void report(Log log, DiagnosticPosition pos, Type site, Name name,
                    List<Type> argtypes, List<Type> typeargtypes) {
            if (sym.owner.type.tag != ERROR) {
                if (sym.name == sym.name.table.init && sym.owner != site.tsym)
                    new ResolveError(ABSENT_MTH, sym.owner, "absent method " + sym).report(
                        log, pos, site, name, argtypes, typeargtypes);
                if ((sym.flags() & PUBLIC) != 0
                    || (env != null && this.site != null
                        && !isAccessible(env, this.site)))
                    log.error(pos, "not.def.access.class.intf.cant.access",
                        sym, sym.location());
                else if ((sym.flags() & (PRIVATE | PROTECTED)) != 0)
                    log.error(pos, "report.access", sym,
                              TreeInfo.flagNames(sym.flags() & (PRIVATE | PROTECTED)),
                              sym.location());
                else
                    log.error(pos, "not.def.public.cant.access",
                              sym, sym.location());
            }
        }
    }