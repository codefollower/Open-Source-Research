    /** Given a type, return its type code (used implicitly in the
     *  JVM architecture).
     */
    public static int typecode(Type type) {
        switch (type.tag) {
			case BYTE: return BYTEcode;
			case SHORT: return SHORTcode;
			case CHAR: return CHARcode;
			case INT: return INTcode;
			case LONG: return LONGcode;
			case FLOAT: return FLOATcode;
			case DOUBLE: return DOUBLEcode;
			case BOOLEAN: return BYTEcode;//boolean当成byte看待
			case VOID: return VOIDcode;
			case CLASS:
			case ARRAY:
			case METHOD:
			case BOT:
			case TYPEVAR:
			case UNINITIALIZED_THIS:
			case UNINITIALIZED_OBJECT:
				return OBJECTcode;
			default: throw new AssertionError("typecode " + type.tag);
        }
    }