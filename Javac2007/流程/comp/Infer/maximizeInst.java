/***************************************************************************
 * Mini/Maximization of UndetVars
 ***************************************************************************/

    /** Instantiate undetermined type variable to its minimal upper bound.
     *  Throw a NoInstanceException if this not possible.
     */
    void maximizeInst(UndetVar that, Warner warn) throws NoInstanceException {
		if (that.inst == null) {
			if (that.hibounds.isEmpty())
				that.inst = syms.objectType;
			else if (that.hibounds.tail.isEmpty())
				that.inst = that.hibounds.head;
			else {
				for (List<Type> bs = that.hibounds;
					 bs.nonEmpty() && that.inst == null;
					 bs = bs.tail) {
					// System.out.println("hibounds = " + that.hibounds);//DEBUG
					if (isSubClass(bs.head, that.hibounds))
						that.inst = types.fromUnknownFun.apply(bs.head);
				}
				if (that.inst == null || !types.isSubtypeUnchecked(that.inst, that.hibounds, warn))
					throw ambiguousNoInstanceException
					.setMessage("no.unique.maximal.instance.exists",
							that.qtype, that.hibounds);
			}
		}
    }
    //where
        private boolean isSubClass(Type t, final List<Type> ts) {
            t = t.baseType();
            if (t.tag == TYPEVAR) {
                List<Type> bounds = types.getBounds((TypeVar)t);
                for (Type s : ts) {
                    if (!types.isSameType(t, s.baseType())) {
                        for (Type bound : bounds) {
                            if (!isSubClass(bound, List.of(s.baseType())))
                                return false;
                        }
                    }
                }
            } else {
                for (Type s : ts) {
                    if (!t.tsym.isSubClass(s.baseType().tsym, types))
                        return false;
                }
            }
            return true;
        }