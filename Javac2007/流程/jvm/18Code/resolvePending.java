    /** Resolve any pending jumps.
     */
    public void resolvePending() {
		DEBUG.P(this,"resolvePending()");
		DEBUG.P("pendingJumps前="+pendingJumps);
		
		Chain x = pendingJumps;
		pendingJumps = null;
		resolve(x, cp);
		
		DEBUG.P("pendingJumps后="+pendingJumps);
		DEBUG.P(0,this,"resolvePending()");
    }