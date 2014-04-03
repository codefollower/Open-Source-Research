    /** An item representing `this' or `super'.
     */
    class SelfItem extends Item {

        /** Flag which determines whether this item represents `this' or `super'.
		 */
		boolean isSuper;

		SelfItem(boolean isSuper) {
			super(OBJECTcode);
			this.isSuper = isSuper;
		}

		Item load() {
			DEBUG.P(this,"load()");
			code.emitop0(aload_0);
			DEBUG.P(0,this,"load()");
			return stackItem[typecode];
		}

		public String toString() {
			return isSuper ? "super" : "this";
		}
    }