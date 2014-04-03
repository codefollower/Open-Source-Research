    /** An item representing a literal.
     */
    class ImmediateItem extends Item {

		/** The literal's value.
		 */
		Object value;

		ImmediateItem(Type type, Object value) {
			super(Code.typecode(type));
			this.value = value;
		}

		private void ldc() {
			int idx = pool.put(value);
			if (typecode == LONGcode || typecode == DOUBLEcode) {
				//将常量池中的long或double类型的项压入堆栈(16位索引)
				code.emitop2(ldc2w, idx);
			} else if (idx <= 255) {
				code.emitop1(ldc1, idx);//将常量池中的项压入堆栈(8位索引)
			} else {
				code.emitop2(ldc2, idx);//将常量池中的项压入堆栈(16位索引)
			}
		}

		Item load() {
			DEBUG.P(this,"load()");
			DEBUG.P("typecode="+ByteCodes.typecodeNames[typecode]);
			switch (typecode) {
				case INTcode: case BYTEcode: case SHORTcode: case CHARcode:
					int ival = ((Number)value).intValue();
					if (-1 <= ival && ival <= 5)
						code.emitop0(iconst_0 + ival);
					else if (Byte.MIN_VALUE <= ival && ival <= Byte.MAX_VALUE)
						code.emitop1(bipush, ival);
					else if (Short.MIN_VALUE <= ival && ival <= Short.MAX_VALUE)
						code.emitop2(sipush, ival);
					else
						ldc();
					break;
				case LONGcode:
					long lval = ((Number)value).longValue();
					if (lval == 0 || lval == 1)
						code.emitop0(lconst_0 + (int)lval);
					else
						ldc();
					break;
				case FLOATcode:
					float fval = ((Number)value).floatValue();
					if (isPosZero(fval) || fval == 1.0 || fval == 2.0)
						code.emitop0(fconst_0 + (int)fval);
					else {
						ldc();
					}
					break;
				case DOUBLEcode:
					double dval = ((Number)value).doubleValue();
					if (isPosZero(dval) || dval == 1.0)
						code.emitop0(dconst_0 + (int)dval);
					else
						ldc();
					break;
				case OBJECTcode:
					ldc();
					break;
				default:
					assert false;
			}
			DEBUG.P(0,this,"load()");
			return stackItem[typecode];
		}
			//where
			/** Return true iff float number is positive 0.
			 */
			/*注意:
			(0.0f==-0.0f)=true
			(1.0f/0.0f)=Infinity
			(1.0f/-0.0f)=-Infinity
			(0.0d==-0.0d)=true
			(1.0d/0.0d)=Infinity
			(1.0d/-0.0d)=-Infinity
			下面两个方法是判断x是否是正的浮点数0
			*/
			private boolean isPosZero(float x) {
				return x == 0.0f && 1.0f / x > 0.0f;
			}
			/** Return true iff double number is positive 0.
			 */
			private boolean isPosZero(double x) {
				return x == 0.0d && 1.0d / x > 0.0d;
			}

		CondItem mkCond() {
			try {//我加上的
			DEBUG.P(this,"mkCond()");
			
			int ival = ((Number)value).intValue();

			DEBUG.P("ival="+ival);

			return makeCondItem(ival != 0 ? goto_ : dontgoto);

			}finally{//我加上的
			DEBUG.P(0,this,"mkCond()");
			}
		}

		Item coerce(int targetcode) {
			try {//我加上的
			DEBUG.P(this,"coerce(int targetcode)");
			DEBUG.P("typecode  ="+ByteCodes.typecodeNames[typecode]);
			DEBUG.P("targetcode="+ByteCodes.typecodeNames[targetcode]);

			if (typecode == targetcode) {
				return this;
			} else {
			switch (targetcode) {
				case INTcode:
					if (Code.truncate(typecode) == INTcode)
						return this;
					else
						return new ImmediateItem(
										syms.intType,
										((Number)value).intValue());
				case LONGcode:
					return new ImmediateItem(
					syms.longType,
								((Number)value).longValue());
				case FLOATcode:
					return new ImmediateItem(
					syms.floatType,
								((Number)value).floatValue());
				case DOUBLEcode:
					return new ImmediateItem(
					syms.doubleType,
					((Number)value).doubleValue());
				case BYTEcode:
					return new ImmediateItem(
					syms.byteType,
								(int)(byte)((Number)value).intValue());
				case CHARcode:
					return new ImmediateItem(
					syms.charType,
								(int)(char)((Number)value).intValue());
				case SHORTcode:
					return new ImmediateItem(
					syms.shortType,
								(int)(short)((Number)value).intValue());
				default:
					return super.coerce(targetcode);
				}
			}

			}finally{//我加上的
			DEBUG.P(0,this,"coerce(int targetcode)");
			}
		}

		public String toString() {
			return "immediate(" + value + ")";
		}
    }