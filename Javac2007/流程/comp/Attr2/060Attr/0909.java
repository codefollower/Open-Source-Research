    /** Warn about unchecked operation.
     *  @param pos        Position to be used for error reporting.
     *  @param msg        A string describing the problem.
     */
    public void warnUnchecked(DiagnosticPosition pos, String msg, Object... args) {
	if (!lint.isSuppressed(LintCategory.UNCHECKED))
	    uncheckedHandler.report(pos, msg, args);
    }