//setBounds
    // <editor-fold defaultstate="collapsed" desc="setBounds">
    /**
     * Set the bounds field of the given type variable to reflect a
     * (possibly multiple) list of bounds.
     * @param t                 a type variable
     * @param bounds            the bounds, must be nonempty
     * @param supertype         is objectType if all bounds are interfaces,
     *                          null otherwise.
     */
    public void setBounds(TypeVar t, List<Type> bounds, Type supertype) {
    	DEBUG.P(this,"setBounds(3)");
    	DEBUG.P("supertype="+supertype);
    	DEBUG.P("bounds.tail.isEmpty()="+bounds.tail.isEmpty());
    	
        if (bounds.tail.isEmpty())
            t.bound = bounds.head;
        else
            t.bound = makeCompoundType(bounds, supertype);
        t.rank_field = -1;
        
        DEBUG.P(0,this,"setBounds(3)");
    }

    /**
     * Same as {@link #setBounds(Type.TypeVar,List,Type)}, except that
     * third parameter is computed directly.  Note that this test
     * might cause a symbol completion.  Hence, this version of
     * setBounds may not be called during a classfile read.
     */
    public void setBounds(TypeVar t, List<Type> bounds) {
    	DEBUG.P(this,"setBounds(2)");
    	DEBUG.P("TypeVar t="+t);
    	DEBUG.P("List<Type> bounds="+bounds);
    	DEBUG.P("(bounds.head==INTERFACE)="+((bounds.head.tsym.flags() & INTERFACE) != 0));
    	
    	//接口的supertype=java.lang.Object
        Type supertype = (bounds.head.tsym.flags() & INTERFACE) != 0 ?
            supertype(bounds.head) : null;
        setBounds(t, bounds, supertype);
        t.rank_field = -1;
        
        DEBUG.P(0,this,"setBounds(2)");
    }
    // </editor-fold>
//