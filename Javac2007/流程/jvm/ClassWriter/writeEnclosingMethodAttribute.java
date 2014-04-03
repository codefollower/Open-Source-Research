    /** Write the EnclosingMethod attribute if needed.
     *  Returns the number of attributes written (0 or 1).
     */
    int writeEnclosingMethodAttribute(ClassSymbol c) {
		try {//我加上的
		DEBUG.P(this,"writeEnclosingMethodAttribute(1)");
		DEBUG.P("target.hasEnclosingMethodAttribute()="+target.hasEnclosingMethodAttribute());
		DEBUG.P("c.name="+c.name);
		DEBUG.P("c.owner.kind="+Kinds.toString(c.owner.kind));

        if (!target.hasEnclosingMethodAttribute() ||
            c.owner.kind != MTH && // neither a local class
            c.name != names.empty) // nor anonymous
            return 0;

        int alenIdx = writeAttr(names.EnclosingMethod);
        ClassSymbol enclClass = c.owner.enclClass();
		DEBUG.P("");
		DEBUG.P("c.owner.type="+c.owner.type);
        MethodSymbol enclMethod =
            (c.owner.type == null // local to init block
             || c.owner.kind != MTH) // or member init
            ? null
            : (MethodSymbol)c.owner;
		DEBUG.P("enclClass="+enclClass);
		DEBUG.P("enclMethod="+enclMethod);
        databuf.appendChar(pool.put(enclClass));
        databuf.appendChar(enclMethod == null ? 0 : pool.put(nameType(c.owner)));
        endAttr(alenIdx);
        return 1;

		}finally{//我加上的
		DEBUG.P(0,this,"writeEnclosingMethodAttribute(1)");
		}
    }