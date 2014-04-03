    /** Warn about deprecated symbol.
     *  @param pos        Position to be used for error reporting.
     *  @param sym        The deprecated symbol.
     */ 
    void warnDeprecated(DiagnosticPosition pos, Symbol sym) {
		if (!lint.isSuppressed(LintCategory.DEPRECATION))
			deprecationHandler.report(pos, "has.been.deprecated", sym, sym.location());
    }