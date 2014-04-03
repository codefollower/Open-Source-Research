    /** Read fractional part and 'd' or 'f' suffix of floating point number.
     */
    private void scanFractionAndSuffix() {
	this.radix = 10;
	scanFraction();
	if (ch == 'f' || ch == 'F') {
	    putChar(ch);
	    scanChar();
            token = FLOATLITERAL;
	} else {
	    if (ch == 'd' || ch == 'D') {
		putChar(ch);
		scanChar();
	    }
	    token = DOUBLELITERAL;
	}
    }