//getBounds
    // <editor-fold defaultstate="collapsed" desc="getBounds">
    /**
     * Return list of bounds of the given type variable.
     */
    public List<Type> getBounds(TypeVar t) {
		/*
		if (t.bound.isErroneous() || !t.bound.isCompound())
            return List.of(t.bound);//如果是ErrorType或其他非Compound类型直接返回
        else if ((erasure(t).tsym.flags() & INTERFACE) == 0)
            return interfaces(t).prepend(supertype(t));
        else
            // No superclass was given in bounds.
            // In this case, supertype is Object, erasure is first interface.
            return interfaces(t);
		*/

		List<Type> returnBounds;
		DEBUG.P(this,"getBounds(TypeVar t)");
		DEBUG.P("t="+t);
		DEBUG.P("t.bound="+t.bound);
		DEBUG.P("t.bound.isErroneous()="+t.bound.isErroneous());
		DEBUG.P("t.bound.isCompound() ="+t.bound.isCompound());
		
        if (t.bound.isErroneous() || !t.bound.isCompound())
            returnBounds = List.of(t.bound);//如果是ErrorType或其他非Compound类型直接返回
        else if ((erasure(t).tsym.flags() & INTERFACE) == 0)
            returnBounds = interfaces(t).prepend(supertype(t));
        else
            // No superclass was given in bounds.
            // In this case, supertype is Object, erasure is first interface.
            returnBounds = interfaces(t);
            
		DEBUG.P("");
		DEBUG.P("returnBounds="+returnBounds);
		DEBUG.P(1,this,"getBounds(TypeVar t)");
		return returnBounds;
    }
    // </editor-fold>
//