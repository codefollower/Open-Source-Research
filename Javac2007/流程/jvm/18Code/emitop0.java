    /** Emit an opcode with no operand field.
     */
    public void emitop0(int op) {//加入不带操作数的虚拟机指令
		try {//我加上的
		DEBUG.P(this,"emitop0(int op)");
		//DEBUG.P("op="+op+" mnem="+mnem(op));
		
		emitop(op);
		if (!alive) return;
		switch (op) {
			case aaload: {
				//以下四条语句可以看成是JVM执行aaload指令的过程(以下所有指令都类似)
				
				//从栈中弹出数组索引
				state.pop(1);// index
				//先保存栈顶的type
				Type a = state.stack[state.stacksize-1];
				//从栈中弹出数组引用(type)
				state.pop(1);  
				//由数组引用与数组索引得出此索引位置的value(一般是指向某一type的引用),再压入栈
				state.push(types.erasure(types.elemtype(a))); }
				break;
			case goto_:
				markDead();
				break;
			case nop:
			case ineg:
			case lneg:
			case fneg:
			case dneg:
				break;
			case aconst_null:
				state.push(syms.botType);
				break;
			case iconst_m1:
			case iconst_0:
			case iconst_1:
			case iconst_2:
			case iconst_3:
			case iconst_4:
			case iconst_5:
			case iload_0:
			case iload_1:
			case iload_2:
			case iload_3:
				state.push(syms.intType);
				break;
			case lconst_0:
			case lconst_1:
			case lload_0:
			case lload_1:
			case lload_2:
			case lload_3:
				state.push(syms.longType);
				break;
			case fconst_0:
			case fconst_1:
			case fconst_2:
			case fload_0:
			case fload_1:
			case fload_2:
			case fload_3:
				state.push(syms.floatType);
				break;
			case dconst_0:
			case dconst_1:
			case dload_0:
			case dload_1:
			case dload_2:
			case dload_3:
				state.push(syms.doubleType);
				break;
			case aload_0:
				state.push(lvar[0].sym.type);//从局部变量数组索引0处加载引用类型
				break;
			case aload_1:
				state.push(lvar[1].sym.type);
				break;
			case aload_2:
				state.push(lvar[2].sym.type);
				break;
			case aload_3:
				state.push(lvar[3].sym.type);
				break;
			case iaload:
			case baload:
			case caload:
			case saload:
				state.pop(2);
				state.push(syms.intType);
				break;
			case laload:
				state.pop(2);
				state.push(syms.longType);
				break;
			case faload:
				state.pop(2);
				state.push(syms.floatType);
				break;
			case daload:
				state.pop(2);
				state.push(syms.doubleType);
				break;
			case istore_0:
			case istore_1:
			case istore_2:
			case istore_3:
			case fstore_0:
			case fstore_1:
			case fstore_2:
			case fstore_3:
			case astore_0:
			case astore_1:
			case astore_2:
			case astore_3:
			case pop:
			case lshr:
			case lshl:
			case lushr:
				state.pop(1);
				break;
			case areturn:
			case ireturn:
			case freturn:
				assert state.nlocks == 0;
				state.pop(1);
				markDead();
				break;
			case athrow:
				state.pop(1);
				markDead();
				break;
			case lstore_0:
			case lstore_1:
			case lstore_2:
			case lstore_3:
			case dstore_0:
			case dstore_1:
			case dstore_2:
			case dstore_3:
			case pop2:
				state.pop(2);
				break;
			case lreturn:
			case dreturn:
				assert state.nlocks == 0;
				state.pop(2);
				markDead();
				break;
			case dup:
				state.push(state.stack[state.stacksize-1]);
				break;
			case return_:
				assert state.nlocks == 0;
				markDead();
				break;
			case arraylength:
				state.pop(1);
				state.push(syms.intType);
				break;
			case isub:
			case iadd:
			case imul:
			case idiv:
			case imod:
			case ishl:
			case ishr:
			case iushr:
			case iand:
			case ior:
			case ixor:
				state.pop(1);
				// state.pop(1);
				// state.push(syms.intType);
				break;
			case aastore:
				state.pop(3);
				break;
			case land:
			case lor:
			case lxor:
			case lmod:
			case ldiv:
			case lmul:
			case lsub:
			case ladd:
				state.pop(2);
				break;
			case lcmp:
				state.pop(4);
				state.push(syms.intType);
				break;
			case l2i:
				state.pop(2);
				state.push(syms.intType);
				break;
			case i2l:
				state.pop(1);
				state.push(syms.longType);
				break;
			case i2f:
				state.pop(1);
				state.push(syms.floatType);
				break;
			case i2d:
				state.pop(1);
				state.push(syms.doubleType);
				break;
			case l2f:
				state.pop(2);
				state.push(syms.floatType);
				break;
			case l2d:
				state.pop(2);
				state.push(syms.doubleType);
				break;
			case f2i:
				state.pop(1);
				state.push(syms.intType);
				break;
			case f2l:
				state.pop(1);
				state.push(syms.longType);
				break;
			case f2d:
				state.pop(1);
				state.push(syms.doubleType);
				break;
			case d2i:
				state.pop(2);
				state.push(syms.intType);
				break;
			case d2l:
				state.pop(2);
				state.push(syms.longType);
				break;
			case d2f:
				state.pop(2);
				state.push(syms.floatType);
				break;
			case tableswitch:
			case lookupswitch:
				state.pop(1);
				// the caller is responsible for patching up the state
				break;
			case dup_x1: {
				Type val1 = state.pop1();
				Type val2 = state.pop1();
				state.push(val1);
				state.push(val2);
				state.push(val1);
				break;
			}
			case bastore:
				state.pop(3);
				break;
			case int2byte:
			case int2char:
			case int2short:
				break;
			case fmul:
			case fadd:
			case fsub:
			case fdiv:
			case fmod:
				state.pop(1);
				break;
			case castore:
			case iastore:
			case fastore:
			case sastore:
				state.pop(3);
				break;
			case lastore:
			case dastore:
				state.pop(4);
				break;
			case dup2:
				if (state.stack[state.stacksize-1] != null) {
					Type value1 = state.pop1();
					Type value2 = state.pop1();
					state.push(value2);
					state.push(value1);
					state.push(value2);
					state.push(value1);
				} else {
					Type value = state.pop2();
					state.push(value);
					state.push(value);
				}
				break;
			case dup2_x1:
				if (state.stack[state.stacksize-1] != null) {
					Type value1 = state.pop1();
					Type value2 = state.pop1();
					Type value3 = state.pop1();
					state.push(value2);
					state.push(value1);
					state.push(value3);
					state.push(value2);
					state.push(value1);
				} else {
					Type value1 = state.pop2();
					Type value2 = state.pop1();
					state.push(value1);
					state.push(value2);
					state.push(value1);
				}
				break;
			case dup2_x2:
				if (state.stack[state.stacksize-1] != null) {
					Type value1 = state.pop1();
					Type value2 = state.pop1();
					if (state.stack[state.stacksize-1] != null) {
						// form 1
						Type value3 = state.pop1();
						Type value4 = state.pop1();
						state.push(value2);
						state.push(value1);
						state.push(value4);
						state.push(value3);
						state.push(value2);
						state.push(value1);
					} else {
						// form 3
						Type value3 = state.pop2();
						state.push(value2);
						state.push(value1);
						state.push(value3);
						state.push(value2);
						state.push(value1);
					}
				} else {
					Type value1 = state.pop2();
					if (state.stack[state.stacksize-1] != null) {
						// form 2
						Type value2 = state.pop1();
						Type value3 = state.pop1();
						state.push(value1);
						state.push(value3);
						state.push(value2);
						state.push(value1);
					} else {
						// form 4
						Type value2 = state.pop2();
						state.push(value1);
						state.push(value2);
						state.push(value1);
					}
				}
				break;
			case dup_x2: {
				Type value1 = state.pop1();
				if (state.stack[state.stacksize-1] != null) {
					// form 1
					Type value2 = state.pop1();
					Type value3 = state.pop1();
					state.push(value1);
					state.push(value3);
					state.push(value2);
					state.push(value1);
				} else {
					// form 2
					Type value2 = state.pop2();
					state.push(value1);
					state.push(value2);
					state.push(value1);
				}
			}
				break;
			case fcmpl:
			case fcmpg:
				state.pop(2);
				state.push(syms.intType);
				break;
			case dcmpl:
			case dcmpg:
				state.pop(4);
				state.push(syms.intType);
				break;
			case swap: {
				Type value1 = state.pop1();
				Type value2 = state.pop1();
				state.push(value1);
				state.push(value2);
				break;
			}
			case dadd:
			case dsub:
			case dmul:
			case ddiv:
			case dmod:
				state.pop(2);
				break;
			case ret:
				markDead();
				break;
			case wide:
				// must be handled by the caller.
				return;
			case monitorenter:
			case monitorexit:
				state.pop(1);
				break;

			default:
				throw new AssertionError(mnem(op));
		}
		postop();
		
		}finally{//我加上的
		DEBUG.P(0,this,"emitop0(int op)");
		}
    }