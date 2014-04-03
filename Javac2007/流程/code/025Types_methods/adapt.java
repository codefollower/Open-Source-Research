//adapt
    /**
     * Adapt a type by computing a substitution which maps a source
     * type to a target type.
     *
     * @param source    the source type
     * @param target    the target type
     * @param from      the type variables of the computed substitution
     * @param to        the types of the computed substitution.
     */
    public void adapt(Type source,
                       Type target,
                       ListBuffer<Type> from,
                       ListBuffer<Type> to) throws AdaptFailure {
		try {//我加上的
		DEBUG.P(this,"adapt(4)");
		DEBUG.P("source="+source+" source.tag="+TypeTags.toString(source.tag));
		DEBUG.P("target="+target+" target.tag="+TypeTags.toString(target.tag));
		DEBUG.P("from="+from.toList());
		DEBUG.P("to="+to.toList());

        Map<Symbol,Type> mapping = new HashMap<Symbol,Type>();
        adaptRecursive(source, target, from, to, mapping);
        List<Type> fromList = from.toList();
        List<Type> toList = to.toList();
        while (!fromList.isEmpty()) {
            Type val = mapping.get(fromList.head.tsym);
            if (toList.head != val)
                toList.head = val;
            fromList = fromList.tail;
            toList = toList.tail;
        }

		}finally{//我加上的
		DEBUG.P(1,this,"adapt(4)");
		}
    }
    // where
        private void adaptRecursive(Type source,
                                    Type target,
                                    ListBuffer<Type> from,
                                    ListBuffer<Type> to,
                                    Map<Symbol,Type> mapping) throws AdaptFailure {
            try {//我加上的
			DEBUG.P(this,"adaptRecursive(5)");
			DEBUG.P("source="+source+" source.tag="+TypeTags.toString(source.tag));
			DEBUG.P("target="+target+" target.tag="+TypeTags.toString(target.tag));
			DEBUG.P("from="+from.toList());
			DEBUG.P("to="+to.toList());
			DEBUG.P("mapping="+mapping);

			if (source.tag == TYPEVAR) {
                // Check to see if there is
                // already a mapping for $source$, in which case
                // the old mapping will be merged with the new
                Type val = mapping.get(source.tsym);
                if (val != null) {
					//val总是缩小继承树范围
					//val-->x1-->target-->object 截成target-->objec
                    if (val.isSuperBound() && target.isSuperBound()) {
                        val = isSubtype(lowerBound(val), lowerBound(target))
                            ? target : val;
                    } else if (val.isExtendsBound() && target.isExtendsBound()) {
						//x1-->val-->x2-->target 截成val-->x2-->target
                        val = isSubtype(upperBound(val), upperBound(target))
                            ? val : target;
                    } else if (!isSameType(val, target)) {
                        throw new AdaptFailure();
                    }
                } else {
                    val = target;
                    from.append(source);
                    to.append(target);
                }
                mapping.put(source.tsym, val);
            } else if (source.tag == target.tag) {
                switch (source.tag) {
                    case CLASS:
                        adapt(source.allparams(), target.allparams(),
                              from, to, mapping);
                        break;
                    case ARRAY:
                        adaptRecursive(elemtype(source), elemtype(target),
                                       from, to, mapping);
                        break;
                    case WILDCARD:
                        if (source.isExtendsBound()) {
                            adaptRecursive(upperBound(source), upperBound(target),
                                           from, to, mapping);
                        } else if (source.isSuperBound()) {
                            adaptRecursive(lowerBound(source), lowerBound(target),
                                           from, to, mapping);
                        }
                        break;
                }
            }

			}finally{//我加上的
			DEBUG.P(1,this,"adaptRecursive(5)");
			}
        }
        public static class AdaptFailure extends Exception {
            static final long serialVersionUID = -7490231548272701566L;
        }

    /**
     * Adapt a type by computing a substitution which maps a list of
     * source types to a list of target types.
     *
     * @param source    the source type
     * @param target    the target type
     * @param from      the type variables of the computed substitution
     * @param to        the types of the computed substitution.
     */
    private void adapt(List<Type> source,
                       List<Type> target,
                       ListBuffer<Type> from,
                       ListBuffer<Type> to,
                       Map<Symbol,Type> mapping) throws AdaptFailure {
		try {//我加上的
			DEBUG.P(this,"adapt(5)");
			DEBUG.P("source="+source);
			DEBUG.P("target="+target);
			DEBUG.P("from="+from.toList());
			DEBUG.P("to="+to.toList());
			DEBUG.P("mapping="+mapping);
			DEBUG.P("source.length()="+source.length());
			DEBUG.P("target.length()="+target.length());

        if (source.length() == target.length()) {
            while (source.nonEmpty()) {
                adaptRecursive(source.head, target.head, from, to, mapping);
                source = source.tail;
                target = target.tail;
            }
        }

		}finally{//我加上的
			DEBUG.P(1,this,"adapt(5)");
		}
    }

    private void adaptSelf(Type t,
                           ListBuffer<Type> from,
                           ListBuffer<Type> to) {
		try {//我加上的
		DEBUG.P(this,"adaptSelf(3)");
		DEBUG.P("t="+t+" t.tag="+TypeTags.toString(t.tag));
		DEBUG.P("from="+from.toList());
		DEBUG.P("to="+to.toList());

        try {
            //if (t.tsym.type != t)
                adapt(t.tsym.type, t, from, to);
        } catch (AdaptFailure ex) {
            // Adapt should never fail calculating a mapping from
            // t.tsym.type to t as there can be no merge problem.
            throw new AssertionError(ex);
        }

		}finally{//我加上的
		DEBUG.P(1,this,"adaptSelf(3)");
		}
    }
//