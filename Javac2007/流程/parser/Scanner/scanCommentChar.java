    /** Read next character in comment, skipping over double '\' characters.
     */
    private void scanCommentChar() {
	scanChar();
	if (ch == '\\') {
	    if (buf[bp+1] == '\\' && unicodeConversionBp != bp) {
		bp++;
	    } else {
		convertUnicode();
	    }
	}
    }