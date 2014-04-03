    /** An item representing a local variable.
     */
    class LocalItem extends Item {

		/** The variable's register.
		 */
		int reg;

		/** The variable's type.
		 */
		Type type;

		LocalItem(Type type, int reg) {
			super(Code.typecode(type));
			assert reg >= 0;
			this.type = type;
			this.reg = reg;
		}

		Item load() {
			try {//我加上的
			DEBUG.P(this,"load()");
			DEBUG.P("reg="+reg+" typecode="+ByteCodes.typecodeNames[typecode]);

			//reg是局部变量的位置，JVM指令中有直接取局部变量位置0到3的指令
			if (reg <= 3)//对应指令iload_0到aload_3之一(每一种类型有四条指令，所以乘以4)
				code.emitop0(iload_0 + Code.truncate(typecode) * 4 + reg);
			else
				code.emitop1w(iload + Code.truncate(typecode), reg);
			return stackItem[typecode];

			}finally{//我加上的
			DEBUG.P(0,this,"load()");
			}
		}

		void store() {
			DEBUG.P(this,"store()");
			DEBUG.P("reg="+reg+" typecode="+ByteCodes.typecodeNames[typecode]);
			if (reg <= 3)//对应指令istore_0到astore_3之一
				code.emitop0(istore_0 + Code.truncate(typecode) * 4 + reg);
			else
				code.emitop1w(istore + Code.truncate(typecode), reg);
			code.setDefined(reg);
			DEBUG.P(0,this,"store()");
		}

		void incr(int x) {
			DEBUG.P(this,"incr(int x)");
			DEBUG.P("x="+x+" typecode="+ByteCodes.typecodeNames[typecode]);

			//typecode与x同为INTcode时，直接iinc
			if (typecode == INTcode && x >= -32768 && x <= 32767) {
				//把常量值x加到索引为reg的局部变量，这个局部变量是int类型
				code.emitop1w(iinc, reg, x);
			} else {
				//把LocalItem压入堆栈，把ImmediateItem(常数x)压入堆栈，
				//相加或相减后，结果类型转换成LocalItem，最后保存到LocalItem
				
				load();//把LocalItem压入堆栈
				if (x >= 0) {
					makeImmediateItem(syms.intType, x).load();//把ImmediateItem(常数x)压入堆栈
					code.emitop0(iadd);//相加
				} else {
					makeImmediateItem(syms.intType, -x).load();//把ImmediateItem(常数-x)压入堆栈
					code.emitop0(isub);//相减
				}		
				makeStackItem(syms.intType).coerce(typecode);//结果类型转换成LocalItem
				store();//保存到LocalItem
			}

			DEBUG.P(0,this,"incr(int x)");
		}

		public String toString() {
			return "localItem(type=" + type + "; reg=" + reg + ")";
		}
    }