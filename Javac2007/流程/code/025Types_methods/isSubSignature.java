//sub signature / override equivalence
    // <editor-fold defaultstate="collapsed" desc="sub signature / override equivalence">
    /**
     * Returns true iff the first signature is a <em>sub
     * signature</em> of the other.  This is <b>not</b> an equivalence
     * relation.
     *
     * @see "The Java Language Specification, Third Ed. (8.4.2)."
     * @see #overrideEquivalent(Type t, Type s)
     * @param t first signature (possibly raw).
     * @param s second signature (could be subjected to erasure).
     * @return true if t is a sub signature of s.
     */
    public boolean isSubSignature(Type t, Type s) {
        //return hasSameArgs(t, s) || hasSameArgs(t, erasure(s));

		DEBUG.P(this,"isSubSignature(Type t, Type s)");
		DEBUG.P("t="+t+"  t.tag="+TypeTags.toString(t.tag));
		DEBUG.P("s="+s+"  s.tag="+TypeTags.toString(s.tag));
		
		boolean isSubSignature = hasSameArgs(t, s) || hasSameArgs(t, erasure(s));
            
		DEBUG.P("isSubSignature="+isSubSignature);
		DEBUG.P(1,this,"isSubSignature(Type t, Type s)");
		return isSubSignature;
    }

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

    /**
     * Does t have the same arguments as s?  It is assumed that both
     * types are (possibly polymorphic) method types.  Monomorphic
     * method types "have the same arguments", if their argument lists
     * are equal.  Polymorphic method types "have the same arguments",
     * if they have the same arguments after renaming all type
     * variables of one to corresponding type variables in the other,
     * where correspondence is by position in the type parameter list.
     */
    public boolean hasSameArgs(Type t, Type s) {
        //return hasSameArgs.visit(t, s);

		DEBUG.P(this,"hasSameArgs(Type t, Type s)");
		DEBUG.P("t="+t+"  t.tag="+TypeTags.toString(t.tag));
		DEBUG.P("s="+s+"  s.tag="+TypeTags.toString(s.tag));
		
		boolean returnResult = hasSameArgs.visit(t, s);
            
		DEBUG.P("hasSameArgs="+returnResult);
		DEBUG.P(0,this,"hasSameArgs(Type t, Type s)");
		return returnResult;
    }
    // where
        private TypeRelation hasSameArgs = new TypeRelation() {

            public Boolean visitType(Type t, Type s) {
                throw new AssertionError();
            }

            @Override
            public Boolean visitMethodType(MethodType t, Type s) {
                return s.tag == METHOD
                    && containsTypeEquivalent(t.argtypes, s.getParameterTypes());
            }

            @Override
            public Boolean visitForAll(ForAll t, Type s) {
                if (s.tag != FORALL)
                    return false;

                ForAll forAll = (ForAll)s;
                return hasSameBounds(t, forAll)
                    && visit(t.qtype, subst(forAll.qtype, forAll.tvars, t.tvars));
            }

            @Override
            public Boolean visitErrorType(ErrorType t, Type s) {
                return false;
            }
        };
    // </editor-fold>
//