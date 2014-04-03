    /** Mark beginning of statement.
     */
    public void statBegin(int pos) {
		//DEBUG.P(this,"statBegin(int pos)");
		//DEBUG.P("pos="+pos);
		
		if (pos != Position.NOPOS) {
			pendingStatPos = pos;
		}
		
		//DEBUG.P("pendingStatPos="+pendingStatPos);
		//DEBUG.P(0,this,"statBegin(int pos)");
    }