/******************************************************************
 * Output routines
 ******************************************************************/

    /** Write a character into given byte buffer;
     *  byte buffer will not be grown.
     */
    void putChar(ByteBuffer buf, int op, int x) {
        buf.elems[op  ] = (byte)((x >>  8) & 0xFF);
        buf.elems[op+1] = (byte)((x      ) & 0xFF);
    }

    /** Write an integer into given byte buffer;
     *  byte buffer will not be grown.
     */
    void putInt(ByteBuffer buf, int adr, int x) {
        buf.elems[adr  ] = (byte)((x >> 24) & 0xFF);
        buf.elems[adr+1] = (byte)((x >> 16) & 0xFF);
        buf.elems[adr+2] = (byte)((x >>  8) & 0xFF);
        buf.elems[adr+3] = (byte)((x      ) & 0xFF);
    }