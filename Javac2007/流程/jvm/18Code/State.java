/* **************************************************************************
 * Simulated VM machine state
 ****************************************************************************/
    //State类就像一个简单的JVM，字段Type[] stack就相当于JVM中的一个堆栈，
    //在它之上实现pop,push等与堆栈相关的操作，Code类中的emit方法每加入一条
    //JVM指令时，都用State模拟了JVM执行指令的过程
    class State implements Cloneable {
		/** The set of registers containing values. */
		Bits defined;

		/** The (types of the) contents of the machine stack. */
		Type[] stack;

		/** The first stack position currently unused. */
		int stacksize;

		/** The numbers of registers containing locked monitors. */
		int[] locks;
		int nlocks;

		State() {
			defined = new Bits();
			stack = new Type[16];
		}

		State dup() {
			try {
				State state = (State)super.clone();
				state.defined = defined.dup();
				state.stack = stack.clone();
				if (locks != null) state.locks = locks.clone();
				if (debugCode) {
					System.err.println("duping state " + this);
					dump();
				}
				return state;
			} catch (CloneNotSupportedException ex) {
				throw new AssertionError(ex);
			}
		}

		void lock(int register) {
			DEBUG.P(this,"lock(1)");
			DEBUG.P("register="+register);

			if (locks == null) {
				locks = new int[20];
			} else if (locks.length == nlocks) {
				int[] newLocks = new int[locks.length << 1];
				System.arraycopy(locks, 0, newLocks, 0, locks.length);
				locks = newLocks;
			}
			locks[nlocks] = register;
			nlocks++;

			DEBUG.P("nlocks="+nlocks);
			DEBUG.P(0,this,"lock(1)");
		}

		void unlock(int register) {
			DEBUG.P(this,"unlock(1)");
			DEBUG.P("register="+register);

			nlocks--;
			assert locks[nlocks] == register;
			locks[nlocks] = -1;

			DEBUG.P("unlock="+nlocks);
			DEBUG.P(0,this,"lock(1)");
		}

		void push(Type t) {
			try {//我加上的
			DEBUG.P(this,"push(Type t)");
			DEBUG.P("t="+t);
			DEBUG.P("stack.push前="+toString());
			
			if (debugCode) System.err.println("   pushing " + t);
			switch (t.tag) {
				case TypeTags.VOID:
					return;
				case TypeTags.BYTE:
				case TypeTags.CHAR:
				case TypeTags.SHORT:
				case TypeTags.BOOLEAN:
					t = syms.intType;
					break;
				default:
					break;
			}
			//stacksize+2与width(t)有关
			if (stacksize+2 >= stack.length) {
				Type[] newstack = new Type[2*stack.length];
				System.arraycopy(stack, 0, newstack, 0, stack.length);
				stack = newstack;
			}
			stack[stacksize++] = t;
			switch (width(t)) {
				case 1:
					break;
				case 2:
					stack[stacksize++] = null;
					break;
				default:
					throw new AssertionError(t);
			}
			if (stacksize > max_stack)
				max_stack = stacksize;
			
			}finally{//我加上的
			DEBUG.P("stack.push后="+toString());
			DEBUG.P(0,this,"push(Type t)");
			}
		}

		Type pop1() {
			if (debugCode) System.err.println("   popping " + 1);
			stacksize--;
			Type result = stack[stacksize];
			stack[stacksize] = null;
			assert result != null && width(result) == 1;
			return result;
		}

		Type peek() { //返回栈顶type
			try {//我加上的
			DEBUG.P(this,"peek()");

			return stack[stacksize-1];

			}finally{//我加上的
			DEBUG.P(0,this,"peek()");
			}
		}

		Type pop2() {
			if (debugCode) System.err.println("   popping " + 2);
			stacksize -= 2;
			Type result = stack[stacksize];
			stack[stacksize] = null;
			assert stack[stacksize+1] == null;
			assert result != null && width(result) == 2;
			return result;
		}

		void pop(int n) {
			try {//我加上的
			DEBUG.P(this,"pop(int n)");
			DEBUG.P("n="+n);
			DEBUG.P("stack.pop前="+toString());
			
			if (debugCode) System.err.println("   popping " + n);
			while (n > 0) {
				stack[--stacksize] = null;
				n--;
			}
			
			}finally{//我加上的
			DEBUG.P("stack.pop后="+toString());
			DEBUG.P(0,this,"pop(int n)");
			}
		}

		void pop(Type t) {
			pop(width(t));
		}

		/** Force the top of the stack to be treated as this supertype
		 *  of its current type. */
		//如果栈顶是CLASS或ARRAY类型，把栈顶的类型替换成它的超类型
		void forceStackTop(Type t) {
			DEBUG.P(this,"forceStackTop(Type t)");
			DEBUG.P("t="+t+"  t.tag="+TypeTags.toString(t.tag));
			DEBUG.P("stack前="+toString());

			if (!alive) return;
			switch (t.tag) {
				case CLASS:
				case ARRAY:
					int width = width(t);
					Type old = stack[stacksize-width];
					assert types.isSubtype(types.erasure(old),
										   types.erasure(t));
					stack[stacksize-width] = t;
					break;
				default:
			}

			DEBUG.P("stack后="+toString());
			DEBUG.P(0,this,"forceStackTop(Type t)");
		}

		void markInitialized(UninitializedType old) {
			DEBUG.P(this,"markInitialized(1)");
			DEBUG.P("old="+old+"  old.tag="+TypeTags.toString(old.tag));
			DEBUG.P("stack前="+toString());

			Type newtype = old.initializedType();
			for (int i=0; i<stacksize; i++)
			if (stack[i] == old) stack[i] = newtype;
			for (int i=0; i<lvar.length; i++) {
				LocalVar lv = lvar[i];
				if (lv != null && lv.sym.type == old) {
					VarSymbol sym = lv.sym;
					sym = sym.clone(sym.owner);
					sym.type = newtype;
					LocalVar newlv = lvar[i] = new LocalVar(sym);
					// should the following be initialized to cp?
					newlv.start_pc = lv.start_pc;
				}
			}

			DEBUG.P("stack后="+toString());
			DEBUG.P(0,this,"markInitialized(1)");
		}
		
		//对照当前State与other的堆栈中的每一项的类型，
		//并替换成超类型存放到State的堆栈中
		State join(State other) {
			try {//我加上的
			DEBUG.P(this,"join(1)");
			DEBUG.P("this ="+toString());
			DEBUG.P("other="+other);

			defined = defined.andSet(other.defined);
			assert stacksize == other.stacksize;
			assert nlocks == other.nlocks;
			for (int i=0; i<stacksize; ) {
				Type t = stack[i];
				Type tother = other.stack[i];
				Type result =
							t==tother ? t :
							types.isSubtype(t, tother) ? tother :
							types.isSubtype(tother, t) ? t :
							error();
				int w = width(result);
				stack[i] = result;
				if (w == 2) assert stack[i+1] == null;
				i += w;
			}
			return this;

			}finally{//我加上的
			DEBUG.P(0,this,"join(1)");
			}
		}

		Type error() {
			throw new AssertionError("inconsistent stack types at join point");
		}

		void dump() {
			dump(-1);
		}

		void dump(int pc) {
			System.err.print("stackMap for " + meth.owner + "." + meth);
			if (pc == -1)
				System.out.println();
			else
				System.out.println(" at " + pc);
			System.err.println(" stack (from bottom):");
			for (int i=0; i<stacksize; i++)
				System.err.println("  " + i + ": " + stack[i]);

			int lastLocal = 0;
			for (int i=max_locals-1; i>=0; i--) {
				if (defined.isMember(i)) {
					lastLocal = i;
					break;
				}
			}
			if (lastLocal >= 0)
				System.err.println(" locals:");
			for (int i=0; i<=lastLocal; i++) {
			System.err.print("  " + i + ": ");
				if (defined.isMember(i)) {
					LocalVar var = lvar[i];
					if (var == null) {
					System.err.println("(none)");
					} else if (var.sym == null)
					System.err.println("UNKNOWN!");
					else
					System.err.println("" + var.sym + " of type " +
							   var.sym.erasure(types));
				} else {
					System.err.println("undefined");
				}
			}
			if (nlocks != 0) {
				System.err.print(" locks:");
				for (int i=0; i<nlocks; i++) {
					System.err.print(" " + locks[i]);
				}
				System.err.println();
			}
		}
		
		//toString是我加上的
		public String toString() {
			StringBuffer sb=new StringBuffer("stack(");
			if(stack!=null) {
				sb.append("size=").append(stack.length);
				for(int i=0;i<stack.length;i++)
					sb.append(", ").append(stack[i]);
			}
			sb.append(")");
			return sb.toString();
		}
    }

    static Type jsrReturnValue = new Type(TypeTags.INT, null);