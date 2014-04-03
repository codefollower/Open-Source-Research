    /** Emit an opcode with a one-byte operand field;
     *  widen if field does not fit in a byte.
     */
    public void emitop1w(int op, int od) {
		try {//我加上的
		DEBUG.P(this,"emitop1w(int op, int od)");
		DEBUG.P("op="+op+"  od="+od);

		if (od > 0xFF) {//常量池索引号或局部变量数组索引号大于255时，采用宽索引
			emitop(wide);
			emitop(op);
			emit2(od);
		} else {
			emitop(op);
			emit1(od);
		}
		if (!alive) return;
		switch (op) {
			case iload:
				state.push(syms.intType);
				break;
			case lload:
				state.push(syms.longType);
				break;
			case fload:
				state.push(syms.floatType);
				break;
			case dload:
				state.push(syms.doubleType);
				break;
			case aload:
				state.push(lvar[od].sym.type);
				break;
			case lstore:
			case dstore:
				state.pop(2);
				break;
			case istore:
			case fstore:
			case astore:
				state.pop(1);
				break;
			case ret:
				markDead();
				break;
			default:
				throw new AssertionError(mnem(op));
		}
		postop();
		}finally{//我加上的
		DEBUG.P(0,this,"emitop1w(int op, int od)");
		}
    }

    /** Emit an opcode with two one-byte operand fields;
     *  widen if either field does not fit in a byte.
     */
    public void emitop1w(int op, int od1, int od2) {
		try {//我加上的
		DEBUG.P(this,"emitop1w(int op, int od1, int od2)");
		DEBUG.P("op="+op+"  od1="+od1+"  od2="+od2);
		if (od1 > 0xFF || od2 < -128 || od2 > 127) {
			emitop(wide);
			emitop(op);
			emit2(od1);
			emit2(od2);
		} else {
			emitop(op);
			emit1(od1);
			emit1(od2);
		}
		if (!alive) return;
		switch (op) {
			case iinc:
				break;
			default:
				throw new AssertionError(mnem(op));
		}
		}finally{//我加上的
		DEBUG.P(0,this,"emitop1w(int op, int od1, int od2)");
		}
    }