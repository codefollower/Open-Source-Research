    /** Check a constant value and report if it is a string that is
     *  too large.
     */
    private void checkStringConstant(DiagnosticPosition pos, Object constValue) {
	try {//我加上的
	DEBUG.P(this,"checkStringConstant(2)");
	DEBUG.P("nerrs="+nerrs+" constValue="+constValue);

	if (nerrs != 0 || // only complain about a long string once
	    constValue == null ||
	    !(constValue instanceof String) ||
	    ((String)constValue).length() < Pool.MAX_STRING_LENGTH)
	    return;
	log.error(pos, "limit.string");
	nerrs++;
	
	}finally{//我加上的
	DEBUG.P(0,this,"checkStringConstant(2)");
	}
    }