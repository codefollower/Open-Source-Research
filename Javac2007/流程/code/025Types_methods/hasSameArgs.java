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