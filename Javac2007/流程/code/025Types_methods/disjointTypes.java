//disjointTypes
    // <editor-fold defaultstate="collapsed" desc="disjointTypes">
    public boolean disjointTypes(List<Type> ts, List<Type> ss) {
		boolean returnResult=true;//我加上的
		try {//我加上的
		DEBUG.P(this,"disjointTypes(2)");
		DEBUG.P("ts="+ts);
		DEBUG.P("ss="+ss);

        while (ts.tail != null && ss.tail != null) {
            if (disjointType(ts.head, ss.head)) return true;
            ts = ts.tail;
            ss = ss.tail;
        }
		returnResult=false;//我加上的
        return false;

		}finally{//我加上的
		DEBUG.P("returnResult="+returnResult);
		DEBUG.P(1,this,"disjointTypes(2)");
		}
    }

    /**
     * Two types or wildcards are considered disjoint if it can be
     * proven that no type can be contained in both. It is
     * conservative in that it is allowed to say that two types are
     * not disjoint, even though they actually are.
     *
     * The type C<X> is castable to C<Y> exactly if X and Y are not
     * disjoint.
     */
    public boolean disjointType(Type t, Type s) {
        //return disjointType.visit(t, s);

		DEBUG.P(this,"disjointType(2)");
		DEBUG.P("t="+t+"  t.tag="+TypeTags.toString(t.tag));
		DEBUG.P("s="+s+"  s.tag="+TypeTags.toString(s.tag));
		boolean returnResult=disjointType.visit(t, s);
		DEBUG.P("returnResult="+returnResult);
		DEBUG.P(1,this,"disjointType(2)");
		return returnResult;
    }
    // where
        private TypeRelation disjointType = new TypeRelation() {

            private Set<TypePair> cache = new HashSet<TypePair>();

            public Boolean visitType(Type t, Type s) {
                if (s.tag == WILDCARD)
                    return visit(s, t);
                else
                    return notSoftSubtypeRecursive(t, s) || notSoftSubtypeRecursive(s, t);
            }

            private boolean isCastableRecursive(Type t, Type s) {
                TypePair pair = new TypePair(t, s);
                if (cache.add(pair)) {
                    try {
                        return Types.this.isCastable(t, s);
                    } finally {
                        cache.remove(pair);
                    }
                } else {
                    return true;
                }
            }

            private boolean notSoftSubtypeRecursive(Type t, Type s) {
                TypePair pair = new TypePair(t, s);
                if (cache.add(pair)) {
                    try {
                        return Types.this.notSoftSubtype(t, s);
                    } finally {
                        cache.remove(pair);
                    }
                } else {
                    return false;
                }
            }

            @Override
            public Boolean visitWildcardType(WildcardType t, Type s) {
                if (t.isUnbound())
                    return false;

                if (s.tag != WILDCARD) {
                    if (t.isExtendsBound())
                        return notSoftSubtypeRecursive(s, t.type);
                    else // isSuperBound()
                        return notSoftSubtypeRecursive(t.type, s);
                }

                if (s.isUnbound())
                    return false;

                if (t.isExtendsBound()) {
                    if (s.isExtendsBound())
                        return !isCastableRecursive(t.type, upperBound(s));
                    else if (s.isSuperBound())
                        return notSoftSubtypeRecursive(lowerBound(s), t.type);
                } else if (t.isSuperBound()) {
                    if (s.isExtendsBound())
                        return notSoftSubtypeRecursive(t.type, upperBound(s));
                }
                return false;
            }
        };
    // </editor-fold>
//