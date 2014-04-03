    /** Check that a given type is assignable to a given proto-type.
     *  If it is, return the type, otherwise return errType.
     *  @param pos        Position to be used for error reporting.
     *  @param found      The type that was found.
     *  @param req        The type that was required.
     */
    Type checkType(DiagnosticPosition pos, Type found, Type req) {
		try {//我加上的
		DEBUG.P(this,"checkType(3)");
		DEBUG.P("found.tag="+TypeTags.toString(found.tag));
		DEBUG.P("req.tag="+TypeTags.toString(req.tag));

		if (req.tag == ERROR)
			return req;
		if (found.tag == FORALL)
			return instantiatePoly(pos, (ForAll)found, req, convertWarner(pos, found, req));
		if (req.tag == NONE)
			return found;
		if (types.isAssignable(found, req, convertWarner(pos, found, req)))
			return found;
		if (found.tag <= DOUBLE && req.tag <= DOUBLE)
			return typeError(pos, JCDiagnostic.fragment("possible.loss.of.precision"), found, req);
		if (found.isSuperBound()) {
			log.error(pos, "assignment.from.super-bound", found);
			return syms.errType;
		}
		if (req.isExtendsBound()) {
			log.error(pos, "assignment.to.extends-bound", req);
			return syms.errType;
		}
		return typeError(pos, JCDiagnostic.fragment("incompatible.types"), found, req);
		
		
		}finally{//我加上的
		DEBUG.P(0,this,"checkType(3)");
		}
    }