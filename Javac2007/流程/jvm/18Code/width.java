    /** The width in bytes of objects of the type.
     */
    public static int width(int typecode) {
        switch (typecode) {
			case LONGcode: case DOUBLEcode: return 2;
			case VOIDcode: return 0;
			default: return 1;
        }
    }

    public static int width(Type type) {
		return type == null ? 1 : width(typecode(type));
    }

    /** The total width taken up by a vector of objects.
     */
    public static int width(List<Type> types) {
        int w = 0;
        for (List<Type> l = types; l.nonEmpty(); l = l.tail)
			w = w + width(l.head);
        return w;
    }