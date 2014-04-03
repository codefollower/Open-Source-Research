    /** An item representing a value on stack.
     */
    class StackItem extends Item {

		StackItem(int typecode) {
			super(typecode);
		}

		Item load() {
			return this;
		}

		void duplicate() {
			DEBUG.P(this,"duplicate()");
			code.emitop0(width() == 2 ? dup2 : dup);
			DEBUG.P(0,this,"duplicate()");
		}

		void drop() {
			DEBUG.P(this,"drop()");
			code.emitop0(width() == 2 ? pop2 : pop);
			DEBUG.P(0,this,"drop()");
		}

		void stash(int toscode) {
			/*对应下面的指令之一(参考<<深入java虚拟机>>P375--P377:
			dup_x1		= 90,//复制1个，弹出2(2=1+1)个
			dup_x2		= 91,//复制1个，弹出3(3=1+2)个

			dup2_x1		= 93,//复制2个，弹出3(3=2+1)个
			dup2_x2		= 94,//复制2个，弹出4(4=2+2)个
			
			//(记忆方式:
			//加号左边的数1表示dup，2表示dup2，
			//加号右边的数就是指令名称x字母旁边的数字)
			*/
			code.emitop0(//toscode不会是VOIDcode
			(width() == 2 ? dup_x2 : dup_x1) + 3 * (Code.width(toscode) - 1));
		}

		int width() {
			//LONGcode与DOUBLEcode占两个字长,VOIDcode不占字长，其他为1个字长。
			//注意字长是相对于堆栈而言的，与java的基本类型所占的bit位长度无关。
			//如果把一个堆栈看成是一个元素类型为Object的数组的话，一个字长就是
			//这个数组中的一个元素。
			return Code.width(typecode);
		}

		public String toString() {
			return "stack(" + typecodeNames[typecode] + ")";
		}
    }
