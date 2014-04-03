    /** An item representing an indexed expression.
     */
    class IndexedItem extends Item {

		IndexedItem(Type type) {
			super(Code.typecode(type));
		}

		Item load() {
			/*对应下面的指令之一
			iaload		= 46,
			laload		= 47,
			faload		= 48,
			daload		= 49,
			aaload		= 50,
			baload		= 51,
			caload		= 52,
			saload		= 53,
			*/
			code.emitop0(iaload + typecode);
			return stackItem[typecode];
		}

		void store() {
			/*对应下面的指令之一
			iastore		= 79,
			lastore		= 80,
			fastore		= 81,
			dastore		= 82,
			aastore		= 83,
			bastore		= 84,
			castore		= 85,
			sastore		= 86,
			*/
			code.emitop0(iastore + typecode);
		}

		void duplicate() {
			code.emitop0(dup2);
		}

		void drop() {
			code.emitop0(pop2);
		}

		void stash(int toscode) {
			code.emitop0(dup_x2 + 3 * (Code.width(toscode) - 1));
		}

		int width() {
			return 2;
		}

		public String toString() {
			return "indexed(" + ByteCodes.typecodeNames[typecode] + ")";
		}
    }