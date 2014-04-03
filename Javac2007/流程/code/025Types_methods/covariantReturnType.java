    /**
     * Is t an appropriate return type in an overrider for a
     * method that returns s?
     */
    public boolean covariantReturnType(Type t, Type s, Warner warner) {
		try {//我加上的
		DEBUG.P(this,"covariantReturnType(3)");
		DEBUG.P("t="+t+"  t.tag="+TypeTags.toString(t.tag));
		DEBUG.P("s="+s+"  s.tag="+TypeTags.toString(s.tag));

        return
            isSameType(t, s) ||
            source.allowCovariantReturns() &&
            !t.isPrimitive() &&
            !s.isPrimitive() &&
            isAssignable(t, s, warner);

		}finally{//我加上的
		DEBUG.P(0,this,"covariantReturnType(3)");
		}
    }
    // </editor-fold>