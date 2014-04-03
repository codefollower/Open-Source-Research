    /** A chain represents a list of unresolved jumps. Jump locations
     *  are sorted in decreasing order.
     */
    public static class Chain {

		/** The position of the jump instruction.
		 */
		public final int pc;

		/** The machine state after the jump instruction.
		 *  Invariant: all elements of a chain list have the same stacksize
		 *  and compatible stack and register contents.
		 */
		Code.State state;

		/** The next jump in the list.
		 */
		public final Chain next;

		/** Construct a chain from its jump position, stacksize, previous
		 *  chain, and machine state.
		 */
		public Chain(int pc, Chain next, Code.State state) {
			DEBUG.P(this,"Chain(3)");
			DEBUG.P("pc="+pc);
			DEBUG.P("next="+next);
			DEBUG.P("state="+state);

			this.pc = pc;
			this.next = next;
			this.state = state;

			DEBUG.P(0,this,"Chain(3)");
		}
		
		//我加上的
		public String toString() {
			return "Chain(pc="+pc+(next!=null?" next="+next:"")+")";
		}
    }