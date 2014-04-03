    /** Emit an opcode with a one-byte operand field.
     */
    public void emitop1(int op, int od) {
		try {//我加上的
		DEBUG.P(this,"emitop1(int op, int od)");
		DEBUG.P("op="+op+"  od="+od);

		emitop(op);
		if (!alive) return;
		emit1(od);
		switch (op) {
			case bipush://此时的od是常量(8位)
				state.push(syms.intType);
				break;
			case ldc1://此时的od是常量池索引
				state.push(typeForPool(pool.pool[od]));
				break;
			default:
				throw new AssertionError(mnem(op));
		}
		postop();
		
		}finally{//我加上的
		DEBUG.P(0,this,"emitop1(int op, int od)");
		}
    }