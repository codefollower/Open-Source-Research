    /** Check that flag set does not contain elements of two conflicting sets. s
     *  Return true if it doesn't.
     *  @param pos           Position to be used for error reporting.
     *  @param flags         The set of flags to be checked.
     *  @param set1          Conflicting flags set #1.
     *  @param set2          Conflicting flags set #2.
     */
    boolean checkDisjoint(DiagnosticPosition pos, long flags, long set1, long set2) {
        if ((flags & set1) != 0 && (flags & set2) != 0) {
            log.error(pos,
		      "illegal.combination.of.modifiers",
		      TreeInfo.flagNames(TreeInfo.firstFlag(flags & set1)),
		      TreeInfo.flagNames(TreeInfo.firstFlag(flags & set2)));
            return false;
        } else
            return true;
    }

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