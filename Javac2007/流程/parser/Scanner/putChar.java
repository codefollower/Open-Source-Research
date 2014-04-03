    /** Append a character to sbuf.
     */
    private void putChar(char ch) {
	if (sp == sbuf.length) {
	    char[] newsbuf = new char[sbuf.length * 2];
	    System.arraycopy(sbuf, 0, newsbuf, 0, sbuf.length);
	    sbuf = newsbuf;
	}
	sbuf[sp++] = ch;
    }