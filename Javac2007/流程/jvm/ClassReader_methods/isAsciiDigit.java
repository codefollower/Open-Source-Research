    /**
     * Character.isDigit answers <tt>true</tt> to some non-ascii
     * digits.  This one does not.  <b>copied from java.lang.Class</b>
     */
    private static boolean isAsciiDigit(char c) {
        return '0' <= c && c <= '9';
    }