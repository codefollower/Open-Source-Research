    /** Check if the given type is an array with too many dimensions.
     */
    private void checkDimension(DiagnosticPosition pos, Type t) {
	switch (t.tag) {
	case METHOD:
	    checkDimension(pos, t.getReturnType());
	    for (List<Type> args = t.getParameterTypes(); args.nonEmpty(); args = args.tail)
		checkDimension(pos, args.head);
	    break;
	case ARRAY:
	//数组维数不能大于ClassFile.MAX_DIMENSIONS(255)
	    if (types.dimensions(t) > ClassFile.MAX_DIMENSIONS) {
		log.error(pos, "limit.dimensions");
		nerrs++;
	    }
	    break;
	default:
	    break;
	}
    }