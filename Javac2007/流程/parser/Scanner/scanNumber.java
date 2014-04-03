    /** Read a number.
     *  @param radix  The radix of the number; one of 8, 10, 16.
     */
    private void scanNumber(int radix) {
	this.radix = radix;
	// for octal, allow base-10 digit in case it's a float literal
	int digitRadix = (radix <= 10) ? 10 : 16;
	boolean seendigit = false;
	while (digit(digitRadix) >= 0) {
	    seendigit = true;
	    putChar(ch);
	    scanChar();
	}
	if (radix == 16 && ch == '.') {
	    scanHexFractionAndSuffix(seendigit);
	} else if (seendigit && radix == 16 && (ch == 'p' || ch == 'P')) {
		//如:0x1p-1f的情况
	    scanHexExponentAndSuffix();
	} else if (radix <= 10 && ch == '.') {
	    putChar(ch);
	    scanChar();
	    scanFractionAndSuffix();
	} else if (radix <= 10 &&
		   (ch == 'e' || ch == 'E' ||
		    ch == 'f' || ch == 'F' ||
		    ch == 'd' || ch == 'D')) {
		//如: 2e2f、2f、2d
	    scanFractionAndSuffix();
	} else {
	    if (ch == 'l' || ch == 'L') {
		scanChar();
		token = LONGLITERAL;
	    } else {
		token = INTLITERAL;
	    }
	}
    }