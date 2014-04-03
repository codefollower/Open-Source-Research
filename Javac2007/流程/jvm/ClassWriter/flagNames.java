    /** Return flags as a string, separated by " ".
     */
    public static String flagNames(long flags) {
        StringBuffer sbuf = new StringBuffer();
        int i = 0;
        long f = flags & StandardFlags;//StandardFlags = 0x0fff(12位)
        while (f != 0) {
            if ((f & 1) != 0) sbuf.append(" " + flagName[i]);
            f = f >> 1;
            i++;
        }
        return sbuf.toString();
    }
    /*对应Flags类中的如下字段:
    //Standard Java flags.
    public static final int PUBLIC       = 1<<0;  0x0001
    public static final int PRIVATE      = 1<<1;  0x0002
    public static final int PROTECTED    = 1<<2;  0x0004
    public static final int STATIC       = 1<<3;  0x0008

    public static final int FINAL        = 1<<4;  0x0010
    public static final int SYNCHRONIZED = 1<<5;  0x0020//这个有点特殊
    public static final int VOLATILE     = 1<<6;  0x0040
    public static final int TRANSIENT    = 1<<7;  0x0080

    public static final int NATIVE       = 1<<8;  0x0100
    public static final int INTERFACE    = 1<<9;  0x0200
    public static final int ABSTRACT     = 1<<10; 0x0400
    public static final int STRICTFP     = 1<<11; 0x0800
    */
    //where
        private final static String[] flagName = {
            "PUBLIC", "PRIVATE", "PROTECTED", "STATIC", "FINAL",
            "SUPER", "VOLATILE", "TRANSIENT", "NATIVE", "INTERFACE",
            "ABSTRACT", "STRICTFP"};