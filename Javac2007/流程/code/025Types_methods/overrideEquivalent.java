    /**
     * Returns true iff these signatures are related by <em>override
     * equivalence</em>.  This is the natural extension of
     * isSubSignature to an equivalence relation.
     *
     * @see "The Java Language Specification, Third Ed. (8.4.2)."
     * @see #isSubSignature(Type t, Type s)
     * @param t a signature (possible raw, could be subjected to
     * erasure).
     * @param s a signature (possible raw, could be subjected to
     * erasure).
     * @return true if either argument is a sub signature of the other.
     */
    public boolean overrideEquivalent(Type t, Type s) {
		DEBUG.P(this,"overrideEquivalent(Type t, Type s)");
		DEBUG.P("t="+t+"  t.tag="+TypeTags.toString(t.tag));
		DEBUG.P("s="+s+"  s.tag="+TypeTags.toString(s.tag));
		
		boolean returnResult = hasSameArgs(t, s) ||
            hasSameArgs(t, erasure(s)) || hasSameArgs(erasure(t), s);
        DEBUG.P("");    
		DEBUG.P("returnResult="+returnResult);
		DEBUG.P(1,this,"overrideEquivalent(Type t, Type s)");
		return returnResult;
		/*
        return hasSameArgs(t, s) ||
            hasSameArgs(t, erasure(s)) || hasSameArgs(erasure(t), s);*/
    }