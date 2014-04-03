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
