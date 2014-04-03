//isConvertible
    // <editor-fold defaultstate="collapsed" desc="isConvertible">
    /**
     * Is t a subtype of or convertiable via boxing/unboxing
     * convertions to s?
     */
    public boolean isConvertible(Type t, Type s, Warner warn) {
		try {//我加上的
		DEBUG.P(this,"isConvertible(3)");
		DEBUG.P("t="+t+"  t.tag="+TypeTags.toString(t.tag));
		DEBUG.P("s="+s+"  s.tag="+TypeTags.toString(s.tag));

        boolean tPrimitive = t.isPrimitive();
        boolean sPrimitive = s.isPrimitive();
        if (tPrimitive == sPrimitive)
            return isSubtypeUnchecked(t, s, warn);
        if (!allowBoxing) return false;
        return tPrimitive
            ? isSubtype(boxedClass(t).type, s)
            : isSubtype(unboxedType(t), s);

		}finally{//我加上的
		DEBUG.P(1,this,"isConvertible(3)");
		}
    }

    /**
     * Is t a subtype of or convertiable via boxing/unboxing
     * convertions to s?
     */
    public boolean isConvertible(Type t, Type s) {
		try {//我加上的
		DEBUG.P(this,"isConvertible(2)");
		DEBUG.P("t="+t+"  t.tag="+TypeTags.toString(t.tag));
		DEBUG.P("s="+s+"  s.tag="+TypeTags.toString(s.tag));

        return isConvertible(t, s, Warner.noWarnings);

		}finally{//我加上的
		DEBUG.P(1,this,"isConvertible(2)");
		}
    }
    // </editor-fold>
//