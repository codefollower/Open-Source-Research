    /** Read code block.
     */
    Code readCode(Symbol owner) {
        nextChar(); // max_stack
        nextChar(); // max_locals
        final int  code_length = nextInt();
        bp += code_length;
        final char exception_table_length = nextChar();
        bp += exception_table_length * 8;
        readMemberAttrs(owner);
        return null;
    }
