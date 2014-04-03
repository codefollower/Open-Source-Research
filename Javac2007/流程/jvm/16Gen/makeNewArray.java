//where
        /** Generate code to create an array with given element type and number
	 *  of dimensions.
	 */
	Item makeNewArray(DiagnosticPosition pos, Type type, int ndims) {
		try {//我加上的
		DEBUG.P(this,"makeNewArray(3)");
		DEBUG.P("type="+type);
		DEBUG.P("ndims="+ndims);

	    Type elemtype = types.elemtype(type);
	    if (types.dimensions(elemtype) + ndims > ClassFile.MAX_DIMENSIONS) {
		log.error(pos, "limit.dimensions");
		nerrs++;
	    }
	    int elemcode = Code.arraycode(elemtype);
		DEBUG.P("elemcode="+elemcode);

	    if (elemcode == 0 || (elemcode == 1 && ndims == 1)) {
		code.emitAnewarray(makeRef(pos, elemtype), type);
	    } else if (elemcode == 1) {
		code.emitMultianewarray(ndims, makeRef(pos, type), type);
	    } else {
		code.emitNewarray(elemcode, type);
	    }
	    return items.makeStackItem(type);

		}finally{//我加上的
		DEBUG.P(0,this,"makeNewArray(3)");
		}
	}