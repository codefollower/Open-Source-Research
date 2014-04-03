/* *************************************************************************
 * Class name generation
 **************************************************************************/

    /** Return name of local class.
     *  This is of the form    <enclClass> $ n <classname>
     *  where
     *    enclClass is the flat name of the enclosing class,
     *    classname is the simple name of the local class
     */
    Name localClassName(ClassSymbol c) {
		for (int i=1; ; i++) {
			Name flatname = names.
			fromString("" + c.owner.enclClass().flatname +
							   target.syntheticNameChar() + i +
							   c.name);
			if (compiled.get(flatname) == null) return flatname;
		}
    }