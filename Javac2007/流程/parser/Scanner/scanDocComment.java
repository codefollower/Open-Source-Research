    /**
     * Scan a documention comment; determine if a deprecated tag is present.
     * Called once the initial /, * have been skipped, positioned at the second *
     * (which is treated as the beginning of the first line).
     * Stops positioned at the closing '/'.
     */
    @SuppressWarnings("fallthrough")
    private void scanDocComment() {
	boolean deprecatedPrefix = false;

	forEachLine:
	while (bp < buflen) {

	    // Skip optional WhiteSpace at beginning of line
	    while (bp < buflen && (ch == ' ' || ch == '\t' || ch == FF)) {
		scanCommentChar();
	    }

	    // Skip optional consecutive Stars
	    while (bp < buflen && ch == '*') {
		scanCommentChar();
		if (ch == '/') {
		    return;
		}
	    }
	
	    // Skip optional WhiteSpace after Stars
	    while (bp < buflen && (ch == ' ' || ch == '\t' || ch == FF)) {
		scanCommentChar();
	    }

	    deprecatedPrefix = false;
	    // At beginning of line in the JavaDoc sense.
	    if (bp < buflen && ch == '@' && !deprecatedFlag) {
		scanCommentChar();
		if (bp < buflen && ch == 'd') {
		    scanCommentChar();
		    if (bp < buflen && ch == 'e') {
			scanCommentChar();
			if (bp < buflen && ch == 'p') {
			    scanCommentChar();
			    if (bp < buflen && ch == 'r') {
				scanCommentChar();
				if (bp < buflen && ch == 'e') {
				    scanCommentChar();
				    if (bp < buflen && ch == 'c') {
					scanCommentChar();
					if (bp < buflen && ch == 'a') {
					    scanCommentChar();
					    if (bp < buflen && ch == 't') {
						scanCommentChar();
						if (bp < buflen && ch == 'e') {
						    scanCommentChar();
						    if (bp < buflen && ch == 'd') {
							deprecatedPrefix = true;
							scanCommentChar();
						    }}}}}}}}}}}
	    if (deprecatedPrefix && bp < buflen) {
		if (Character.isWhitespace(ch)) {
		    deprecatedFlag = true;
		} else if (ch == '*') {
		    scanCommentChar();
		    if (ch == '/') {
			deprecatedFlag = true;
			return;
		    }
		}
	    }

	    // Skip rest of line
	    while (bp < buflen) {
		switch (ch) {
		case '*':
		    scanCommentChar();
		    if (ch == '/') {
			return;
		    }
		    break;
		case CR: // (Spec 3.4)
		    scanCommentChar();
		    if (ch != LF) {
			continue forEachLine;
		    }
		    /* fall through to LF case */
		case LF: // (Spec 3.4)
		    scanCommentChar();
		    continue forEachLine;
		default:
		    scanCommentChar();
		}
	    } // rest of line
	} // forEachLine
	return;
    }