/* **************************************************************************
 * Local variables
 ****************************************************************************/

    /** A live range of a local variable. */
    static class LocalVar {
		final VarSymbol sym;
		final char reg;
		char start_pc = Character.MAX_VALUE;
		char length = Character.MAX_VALUE;
		LocalVar(VarSymbol v) {
			this.sym = v;
			this.reg = (char)v.adr;
		}
		public LocalVar dup() {
			return new LocalVar(sym);
		}
		public String toString() {
			return "" + sym + " in register " + ((int)reg) + " starts at pc=" + ((int)start_pc) + " length=" + ((int)length);
		}
    };