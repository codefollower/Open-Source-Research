    /** Read fractional part and 'd' or 'f' suffix of floating point number.
     */
    private void scanHexFractionAndSuffix(boolean seendigit) {
	this.radix = 16;
	assert ch == '.';
	putChar(ch);
	scanChar();
        while (digit(16) >= 0) {
	    seendigit = true;
	    putChar(ch);
            scanChar();
        }
	if (!seendigit)
	    lexError("invalid.hex.number");//十六进制数字必须包含至少一位十六进制数,错例如:0x.p-1f;
	else
	    scanHexExponentAndSuffix();
    }