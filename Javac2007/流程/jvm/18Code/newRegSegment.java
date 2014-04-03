    /** Start a set of fresh registers.
     */
    public void newRegSegment() {
    DEBUG.P(this,"newRegSegment()");
    DEBUG.P("nextreg前="+nextreg);
    
	nextreg = max_locals;
	
	DEBUG.P("nextreg后="+nextreg);
	DEBUG.P(0,this,"newRegSegment()");
    }