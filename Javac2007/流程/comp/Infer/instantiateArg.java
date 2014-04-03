	/** Try to instantiate argument type `that' to given type `to'.
	 *  If this fails, try to insantiate `that' to `to' where
	 *  every occurrence of a type variable in `tvars' is replaced
	 *  by an unknown type.
	 */
	private Type instantiateArg(ForAll that,
				    Type to,
				    List<Type> tvars,
                    Warner warn) throws NoInstanceException {
	    List<Type> targs;
	    try {
			return instantiateExpr(that, to, warn);
	    } catch (NoInstanceException ex) {
			Type to1 = to;
			for (List<Type> l = tvars; l.nonEmpty(); l = l.tail)
				to1 = types.subst(to1, List.of(l.head), List.of(syms.unknownType));
			return instantiateExpr(that, to1, warn);
	    }
	}