    /** Check that type is different from 'void'.
     *  @param pos           Position to be used for error reporting.
     *  @param t             The type to be checked.
     */
    Type checkNonVoid(DiagnosticPosition pos, Type t) {
		if (t.tag == VOID) {
			log.error(pos, "void.not.allowed.here");
			return syms.errType;
		} else {
			return t;
		}
    }