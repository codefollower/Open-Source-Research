//Array Utils
    // <editor-fold defaultstate="collapsed" desc="Array Utils">
    public boolean isArray(Type t) {
        while (t.tag == WILDCARD)
            t = upperBound(t);
        return t.tag == ARRAY;
    }

    /**
     * The element type of an array.
     */
    public Type elemtype(Type t) {
		try {//我加上的
		DEBUG.P(this,"elemtype(1)");
		DEBUG.P("t="+t+" t.tag="+TypeTags.toString(t.tag));

        switch (t.tag) {
        case WILDCARD:
            return elemtype(upperBound(t));
        case ARRAY:
            return ((ArrayType)t).elemtype;
        case FORALL:
            return elemtype(((ForAll)t).qtype);
        case ERROR:
            return t;
        default:
            return null;
        }

		}finally{//我加上的
		DEBUG.P(1,this,"elemtype(1)");
		}
    }

    /**
     * Mapping to take element type of an arraytype
     */
    private Mapping elemTypeFun = new Mapping ("elemTypeFun") {
        public Type apply(Type t) { return elemtype(t); }
    };

    /**
     * The number of dimensions of an array type.
     */
    public int dimensions(Type t) {
        int result = 0;
        while (t.tag == ARRAY) {
            result++;
            t = elemtype(t);
        }
        return result;
    }
    // </editor-fold>
//