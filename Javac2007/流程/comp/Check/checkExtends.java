    /** Check that a type is within some bounds.
     *
     *  Used in TypeApply to verify that, e.g., X in V<X> is a valid
     *  type argument.
     *  @param pos           Position to be used for error reporting.
     *  @param a             The type that should be bounded by bs.
     *  @param bs            The bound.
     */
    private void checkExtends(DiagnosticPosition pos, Type a, TypeVar bs) {
		try {//我加上的
		DEBUG.P(this,"checkExtends(3)");
		DEBUG.P("a="+a);
		DEBUG.P("a.tag="+TypeTags.toString(a.tag));
		DEBUG.P("a.isUnbound()="+a.isUnbound());
		DEBUG.P("a.isExtendsBound()="+a.isExtendsBound());
		DEBUG.P("a.isSuperBound()="+a.isSuperBound());
		DEBUG.P("bs="+bs);

		//测试upperBound与lowerBound
		//types.upperBound(a);
		//types.lowerBound(a);

		if (a.isUnbound()) {
			return;
		} else if (a.tag != WILDCARD) {
			a = types.upperBound(a);
			for (List<Type> l = types.getBounds(bs); l.nonEmpty(); l = l.tail) {
				if (!types.isSubtype(a, l.head)) {
					log.error(pos, "not.within.bounds", a);
					return;
				}
			}
		} else if (a.isExtendsBound()) {
			if (!types.isCastable(bs.getUpperBound(), types.upperBound(a), Warner.noWarnings))
				log.error(pos, "not.within.bounds", a);
		} else if (a.isSuperBound()) {
			if (types.notSoftSubtype(types.lowerBound(a), bs.getUpperBound()))
				log.error(pos, "not.within.bounds", a);
		}
		
		}finally{//我加上的
		DEBUG.P(1,this,"checkExtends(3)");
		}
    }