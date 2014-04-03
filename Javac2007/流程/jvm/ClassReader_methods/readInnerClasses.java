    /** Read inner class info. For each inner/outer pair allocate a
     *  member class.
     */
    void readInnerClasses(ClassSymbol c) {
        int n = nextChar();
        for (int i = 0; i < n; i++) {
            nextChar(); // skip inner class symbol
            ClassSymbol outer = readClassSymbol(nextChar());
            Name name = readName(nextChar());
            if (name == null) name = names.empty;
            long flags = adjustClassFlags(nextChar());
            if (outer != null) { // we have a member class
                if (name == names.empty)
                    name = names.one;
                ClassSymbol member = enterClass(name, outer);
                if ((flags & STATIC) == 0) {
                    ((ClassType)member.type).setEnclosingType(outer.type);
                    if (member.erasure_field != null)
                        ((ClassType)member.erasure_field).setEnclosingType(types.erasure(outer.type));
                }
                if (c == outer) {
                    member.flags_field = flags;
                    enterMember(c, member);
                }
            }
        }
    }