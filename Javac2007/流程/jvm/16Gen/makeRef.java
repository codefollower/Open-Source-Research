    /** Insert a reference to given type in the constant pool,
     *  checking for an array with too many dimensions;
     *  return the reference's index.
     *  @param type   The type for which a reference is inserted.
     */
    int makeRef(DiagnosticPosition pos, Type type) {
    try {//我加上的
	DEBUG.P(this,"makeRef(2)");
	DEBUG.P("type="+type+"  type.tag="+TypeTags.toString(type.tag));

	checkDimension(pos, type);
	return pool.put(type.tag == CLASS ? (Object)type.tsym : (Object)type);
	
	}finally{//我加上的
	DEBUG.P(0,this,"makeRef(2)");
	}
    }