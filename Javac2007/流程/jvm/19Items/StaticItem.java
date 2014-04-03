    /** An item representing a static variable or method.
     */
    class StaticItem extends Item {
		/** The represented symbol.
		 */
		Symbol member;

		StaticItem(Symbol member) {
			super(Code.typecode(member.erasure(types)));
			this.member = member;
		}

		Item load() {
			//pool.put(member)的返回值为int类型
			code.emitop2(getstatic, pool.put(member));
			return stackItem[typecode];
		}

		void store() {
			code.emitop2(putstatic, pool.put(member));
		}

		Item invoke() {
			try {//我加上的
			DEBUG.P(this,"invoke()");
			
			MethodType mtype = (MethodType)member.erasure(types);
			int argsize = Code.width(mtype.argtypes);//没有用处
			int rescode = Code.typecode(mtype.restype);
			int sdiff = Code.width(rescode) - argsize;//没有用处
			code.emitInvokestatic(pool.put(member), mtype);
			return stackItem[rescode];

			}finally{//我加上的
			DEBUG.P(0,this,"invoke()");
			}
		}

		public String toString() {
			return "static(" + member + ")";
		}
    }