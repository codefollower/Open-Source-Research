    /** An item representing an assignment expressions.
     */
    class AssignItem extends Item {

		/** The item representing the assignment's left hand side.
		 */
		Item lhs;

		AssignItem(Item lhs) {
			super(lhs.typecode);
			this.lhs = lhs;
		}

		Item load() {
			lhs.stash(typecode);
			lhs.store();
			return stackItem[typecode];
		}

		void duplicate() {
			load().duplicate();
		}

		void drop() {
			DEBUG.P(this,"drop()");
			lhs.store();//先出栈再存放到Item lhs
			DEBUG.P(0,this,"drop()");
		}

		void stash(int toscode) {
			assert false;
		}

		int width() {
			return lhs.width() + Code.width(typecode);
		}

		public String toString() {
			return "assign(lhs = " + lhs + ")";
		}
    }