    /** Collapse type code for subtypes of int to INTcode.
     */
    public static int truncate(int tc) {
        switch (tc) {
			case BYTEcode: case SHORTcode: case CHARcode: return INTcode;
			default: return tc;
        }
    }