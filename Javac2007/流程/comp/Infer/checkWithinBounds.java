    /** check that type parameters are within their bounds.
     */
    private void checkWithinBounds(List<Type> tvars,
                                   List<Type> arguments,
                                   Warner warn)
        throws NoInstanceException {
		DEBUG.P(this,"checkWithinBounds(3)");
		DEBUG.P("tvars="+tvars);
		DEBUG.P("arguments="+arguments);

		for (List<Type> tvs = tvars, args = arguments;
			 tvs.nonEmpty();
			 tvs = tvs.tail, args = args.tail) {
			if (args.head instanceof UndetVar) continue;
			List<Type> bounds = types.subst(types.getBounds((TypeVar)tvs.head), tvars, arguments);
			if (!types.isSubtypeUnchecked(args.head, bounds, warn))
				throw unambiguousNoInstanceException
						.setMessage("inferred.do.not.conform.to.bounds",
						arguments, tvars);
		}

		DEBUG.P(0,this,"checkWithinBounds(3)");
    }