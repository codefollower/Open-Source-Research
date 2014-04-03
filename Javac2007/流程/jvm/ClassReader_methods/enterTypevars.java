    /** Enter type variables of this classtype and all enclosing ones in
     *  `typevars'.
     */
    protected void enterTypevars(Type t) {
    	DEBUG.P(this,"enterTypevars(Type t)");
		DEBUG.P("t="+t);
		DEBUG.P("t.getEnclosingType()="+t.getEnclosingType());
		DEBUG.P("t.getTypeArguments()="+t.getTypeArguments());
		DEBUG.P("typevars="+typevars);
		
        if (t.getEnclosingType() != null && t.getEnclosingType().tag == CLASS)
            enterTypevars(t.getEnclosingType());
        for (List<Type> xs = t.getTypeArguments(); xs.nonEmpty(); xs = xs.tail)
            typevars.enter(xs.head.tsym);
        
		DEBUG.P("typevars="+typevars);
        DEBUG.P(0,this,"enterTypevars(Type t)");
    }

    protected void enterTypevars(Symbol sym) {
        if (sym.owner.kind == MTH) {
            enterTypevars(sym.owner);
            enterTypevars(sym.owner.owner);
        }
        enterTypevars(sym.type);
    }