    /** The current output code pointer.
     */
    public int curPc() {
		//DEBUG.P(this,"curPc()");

		if (pendingJumps != null) resolvePending();
		if (pendingStatPos != Position.NOPOS) markStatBegin();
		
		fixedPc = true;

		//DEBUG.P("cp="+cp);
		//DEBUG.P(0,this,"curPc()");
		return cp;
    }