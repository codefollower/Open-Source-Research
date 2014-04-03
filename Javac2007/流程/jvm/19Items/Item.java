    /** The base class of all items, which implements default behavior.
     */
    abstract class Item {
        /** The type code of values represented by this item.
		 */
		int typecode;
		
		Item(int typecode) {
			this.typecode = typecode;
		}

		/** Generate code to load this item onto stack.
		 */
		Item load() {
			throw new AssertionError();
		}

		/** Generate code to store top of stack into this item.
		 */
		void store() {
			throw new AssertionError("store unsupported: " + this);
		}

		/** Generate code to invoke method represented by this item.
		 */
		Item invoke() {
			throw new AssertionError(this);
		}

		/** Generate code to use this item twice.
		 */
		void duplicate() {
			DEBUG.P(this,"duplicate()");
			DEBUG.P("Item.duplicate() do nothing");
			DEBUG.P(0,this,"duplicate()");
		}

		/** Generate code to avoid having to use this item.
		 */
		void drop() {
			DEBUG.P(this,"drop()");
			DEBUG.P("Item.drop() do nothing");
			DEBUG.P(0,this,"drop()");
		}

		/** Generate code to stash a copy of top of stack - of typecode toscode -
		 *  under this item.
		 */
		void stash(int toscode) {
			stackItem[toscode].duplicate();
		}

		/** Generate code to turn item into a testable condition.
		 */
		//将此item压入堆栈(stack),返回一个表示ifne(如果栈顶不等于0则跳转)的CondItem
		//只有子类CondItem与ImmediateItem覆盖了这个方法。
		CondItem mkCond() {
			try {//我加上的
			DEBUG.P(this,"mkCond()");
			
			load();
			return makeCondItem(ifne); //ifne在ByteCodes定义

			}finally{//我加上的
			DEBUG.P(0,this,"mkCond()");
			}
		}
		
		/** Generate code to coerce item to given type code.
		 *  @param targetcode    The type code to coerce to.
		 */
		Item coerce(int targetcode) {
			try {//我加上的
			DEBUG.P(this,"coerce(int targetcode)");
			DEBUG.P("typecode="+typecode+" targetcode="+targetcode);
			
			if (typecode == targetcode)
				return this;
			else {
				load();

				int typecode1 = Code.truncate(typecode);
				int targetcode1 = Code.truncate(targetcode);
				if (typecode1 != targetcode1) {
					int offset = targetcode1 > typecode1 ? targetcode1 - 1
					: targetcode1;
					// <editor-fold defaultstate="collapsed">
					/*对应下面的指令之一:
					i2l		= 133,
					i2f		= 134,
					i2d		= 135,
					l2i		= 136,
					l2f		= 137,
					l2d		= 138,
					f2i		= 139,
					f2l		= 140,
					f2d		= 141,
					d2i		= 142,
					d2l		= 143,
					d2f		= 144,
					*/
					/*
					注意上面的指令是以3条为一组的,且与下面的type code相对应
					int INTcode 	= 0,
					LONGcode 	= 1,
					FLOATcode 	= 2,
					DOUBLEcode 	= 3,
					
					举例:将long转成float(也就是l2f = 137这条指令所具有的功能)
					对应程序变量值为:
					typecode=LONGcode=1,
					targetcode=FLOATcode=2
					首先判断得出typecode与targetcode不相等,
					且int typecode1 = Code.truncate(typecode) =LONGcode=1;
					  int targetcode1 = Code.truncate(targetcode)=FLOATcode=2;
					
					因为targetcode1>typecode1 
					所以int offset=targetcode1 - 1=2-1=LONGcode=1;
					
					最后：i2l + typecode1 * 3 + offset = 133 + 1 * 3 + 1=137=l2f
					
					理解关键点是:
					INTcode,LONGcode,FLOATcode,DOUBLEcode的值按1递增，
					且这四种基本类型之间的相互转换都有3条指令，
					指令码(值)也按INT,LONG,FLOAT,DOUBLE的顺序来定，
					这样就很有规律了。
					*/
					// </editor-fold>
					code.emitop0(i2l + typecode1 * 3 + offset);
				}
				/*
				当targetcode是BYTEcode、SHORTcode、CHARcode时,
				targetcode1经过Code.truncate(targetcode)后变为INTcode,
				if (targetcode != targetcode1)就为true
				*/
				if (targetcode != targetcode1) {
					/*对应下面的指令之一:
					int2byte	= 145,
					int2char	= 146,
					int2short	= 147,
					*/
					code.emitop0(int2byte + targetcode - BYTEcode);
				}
				return stackItem[targetcode];
			}
			
			}finally{//我加上的
			DEBUG.P(0,this,"coerce(int targetcode)");
			}
		}

		/** Generate code to coerce item to given type.
		 *  @param targettype    The type to coerce to.
		 */
		Item coerce(Type targettype) {
			return coerce(Code.typecode(targettype));
		}

		/** Return the width of this item on stack as a number of words.
		 */
		int width() {
			return 0;
		}

		public abstract String toString();
    }