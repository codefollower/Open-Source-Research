//Box/unbox support
    // <editor-fold defaultstate="collapsed" desc="Box/unbox support">
    /**
     * Return the class that boxes the given primitive.
     */
    public ClassSymbol boxedClass(Type t) {
        return reader.enterClass(syms.boxedName[t.tag]);
    }

    /**
     * Return the primitive type corresponding to a boxed type.
     */
    public Type unboxedType(Type t) {
		try {//我加上的
		DEBUG.P(this,"unboxedType(1)");
        if (allowBoxing) {
            for (int i=0; i<syms.boxedName.length; i++) {
                Name box = syms.boxedName[i];
				DEBUG.P("box="+box);
                if (box != null &&
                    asSuper(t, reader.enterClass(box)) != null)
                    return syms.typeOfTag[i];
            }
        }
        return Type.noType;

		}finally{//我加上的
		DEBUG.P(0,this,"unboxedType(1)");
		}
    }
    // </editor-fold>
//