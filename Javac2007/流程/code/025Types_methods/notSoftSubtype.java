//notSoftSubtype
    // <editor-fold defaultstate="collapsed" desc="notSoftSubtype">
    /**
     * This relation answers the question: is impossible that
     * something of type `t' can be a subtype of `s'? This is
     * different from the question "is `t' not a subtype of `s'?"
     * when type variables are involved: Integer is not a subtype of T
     * where <T extends Number> but it is not true that Integer cannot
     * possibly be a subtype of T.
     */
    public boolean notSoftSubtype(Type t, Type s) {
    	try {//我加上的
		DEBUG.P(this,"notSoftSubtype(2)");
		DEBUG.P("t="+t+" t.tag="+TypeTags.toString(t.tag));
		DEBUG.P("s="+s+" s.tag="+TypeTags.toString(s.tag));
		
        if (t == s) return false;
        if (t.tag == TYPEVAR) {
            TypeVar tv = (TypeVar) t;
            if (s.tag == TYPEVAR)
                s = s.getUpperBound();
            return !isCastable(tv.bound,
                               s,
                               Warner.noWarnings);
        }
        if (s.tag != WILDCARD)
            s = upperBound(s);
        if (s.tag == TYPEVAR)
            s = s.getUpperBound();
        return !isSubtype(t, s);
        
        }finally{//我加上的
		DEBUG.P(1,this,"notSoftSubtype(2)");
		}
    }
    // </editor-fold>
//