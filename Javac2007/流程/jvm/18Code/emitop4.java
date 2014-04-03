    /** Emit an opcode with a four-byte operand field.
     */
    public void emitop4(int op, int od) {
		try {//我加上的
		DEBUG.P(this,"emitop4(int op, int od)");
		DEBUG.P("op="+op+" mnem="+mnem(op)+" od="+od);
		
		emitop(op);
		if (!alive) return;
		emit4(od);
		switch (op) {
			case goto_w:
				markDead();
				break;
			case jsr_w:
				break;
			default:
				throw new AssertionError(mnem(op));
		}

		// postop();
		}finally{//我加上的
		DEBUG.P(0,this,"emitop4(int op, int od)");
		}
    }