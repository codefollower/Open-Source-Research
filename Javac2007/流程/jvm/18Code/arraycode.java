    /** Given a type, return its code for allocating arrays of that type.
     */
    //arraycode方法中定义的数字的原由是什么??????我不知道。。。。。。
    //在Gen类的makeNewArray方法中有应用，在<<深入JAVA虚拟机>>第430页也有说明
    public static int arraycode(Type type) {
		switch (type.tag) {
			case BYTE: return 8;
			case BOOLEAN: return 4;
			case SHORT: return 9;
			case CHAR: return 5;
			case INT: return 10;
			case LONG: return 11;
			case FLOAT: return 6;
			case DOUBLE: return 7;
			case CLASS: return 0;
			case ARRAY: return 1;
			default: throw new AssertionError("arraycode " + type);
		}
    }