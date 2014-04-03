    /** Emit an opcode with a two-byte operand field.
     */
    public void emitop2(int op, int od) {
		try {//我加上的
		DEBUG.P(this,"emitop2(int op, int od)");
		DEBUG.P("op="+op+" mnem="+mnem(op)+" od="+od);
		
		emitop(op);
		if (!alive) return;
		emit2(od);
		switch (op) {
			case getstatic:
				state.push(((Symbol)(pool.pool[od])).erasure(types));
				break;
			case putstatic:
				state.pop(((Symbol)(pool.pool[od])).erasure(types));
				break;
			case new_:
				state.push(uninitializedObject(((Symbol)(pool.pool[od])).erasure(types), cp-3));
				break;
			case sipush:
				state.push(syms.intType);
				break;
			case if_acmp_null:
			case if_acmp_nonnull:
			case ifeq:
			case ifne:
			case iflt:
			case ifge:
			case ifgt:
			case ifle:
				state.pop(1);
				break;
			case if_icmpeq:
			case if_icmpne:
			case if_icmplt:
			case if_icmpge:
			case if_icmpgt:
			case if_icmple:
			case if_acmpeq:
			case if_acmpne:
				state.pop(2);
				break;
			case goto_:
				markDead();
				break;
			case putfield:
				state.pop(((Symbol)(pool.pool[od])).erasure(types));
				state.pop(1); // object ref
				break;
			case getfield:
				state.pop(1); // object ref
				state.push(((Symbol)(pool.pool[od])).erasure(types));
				break;
			case checkcast: {
				state.pop(1); // object ref
				Object o = pool.pool[od];
				Type t = (o instanceof Symbol)
				? ((Symbol)o).erasure(types)
				: types.erasure(((Type)o));
				state.push(t);
				break; }
			case ldc2w:
				state.push(typeForPool(pool.pool[od]));
				break;
			case instanceof_:
				state.pop(1);
				state.push(syms.intType);
				break;
			case ldc2:
				state.push(typeForPool(pool.pool[od]));
				break;
			case jsr:
				break;
			default:
				throw new AssertionError(mnem(op));
		}
		// postop();
		
		}finally{//我加上的
		DEBUG.P(0,this,"emitop2(int op, int od)");
		}
    }