    /** Place a byte into code at address pc. Pre: pc + 1 <= cp.
     */
    private void put1(int pc, int op) {
        code[pc] = (byte)op;
    }

    /** Place two bytes into code at address pc. Pre: pc + 2 <= cp.
     */
    private void put2(int pc, int od) {
    	DEBUG.P(this,"put2(int pc, int od)");
		DEBUG.P("pc="+pc+" od="+od);
		
        // pre: pc + 2 <= cp
        put1(pc, od >> 8);
        put1(pc+1, od);
        
        DEBUG.P(0,this,"put2(int pc, int od)");
    }

    /** Place four  bytes into code at address pc. Pre: pc + 4 <= cp.
     */
    public void put4(int pc, int od) {
        // pre: pc + 4 <= cp
        put1(pc  , od >> 24);
        put1(pc+1, od >> 16);
        put1(pc+2, od >> 8);
        put1(pc+3, od);
    }