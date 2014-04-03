    /** Declare an entry point; return current code pointer
     */
    public int entryPoint() {
		DEBUG.P(this,"entryPoint()");
		
		int pc = curPc();
		alive = true;
		pendingStackMap = needStackMap;

		DEBUG.P("pc="+pc+" pendingStackMap="+pendingStackMap);
		DEBUG.P(0,this,"entryPoint()");
		return pc;
    }

    /** Declare an entry point with initial state;
     *  return current code pointer
     */
    public int entryPoint(State state) {
		DEBUG.P(this,"entryPoint(1)");
		DEBUG.P("state="+state);
		
		int pc = curPc();
		alive = true;
		this.state = state.dup();
		assert state.stacksize <= max_stack;
		if (debugCode) System.err.println("entry point " + state);
		pendingStackMap = needStackMap;
		
		DEBUG.P("pc="+pc+" pendingStackMap="+pendingStackMap);
		DEBUG.P(0,this,"entryPoint(1)");
		return pc;
    }

    /** Declare an entry point with initial state plus a pushed value;
     *  return current code pointer
     */
    public int entryPoint(State state, Type pushed) {
		DEBUG.P(this,"entryPoint(2)");
		DEBUG.P("state="+state);
		DEBUG.P("pushed="+pushed);
		
		int pc = curPc();
		alive = true;
		this.state = state.dup();
		assert state.stacksize <= max_stack;
		this.state.push(pushed);
		if (debugCode) System.err.println("entry point " + state);
		pendingStackMap = needStackMap;
		
		DEBUG.P("pc="+pc+" pendingStackMap="+pendingStackMap);
		DEBUG.P(0,this,"entryPoint(2)");
		return pc;
    }