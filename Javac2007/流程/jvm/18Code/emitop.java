    /** Emit an opcode.
     */
    private void emitop(int op) {
		DEBUG.P(this,"emitop(int op)");
		DEBUG.P("alive="+alive+"  pendingJumps="+pendingJumps);
		
		if (pendingJumps != null) resolvePending();
		if (alive) {
			DEBUG.P("pendingStatPos="+pendingStatPos);
			if (pendingStatPos != Position.NOPOS)
				markStatBegin();

			DEBUG.P("pendingStackMap="+pendingStackMap);
			if (pendingStackMap) {
				pendingStackMap = false;
				emitStackMap(); 
			}

			DEBUG.P("emit@cp=" + cp + " stack=" +
					state.stacksize + ": " +
					mnem(op)+"("+op+")");

			if (debugCode)
				System.err.println("emit@" + cp + " stack=" +
								   state.stacksize + ": " +
								   mnem(op));
			emit1(op);
		}
		
		DEBUG.P(0,this,"emitop(int op)");
    }