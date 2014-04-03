    /**
     * Called when a complete comment has been scanned. pos and endPos 
     * will mark the comment boundary.
     */
    protected void processComment(CommentStyle style) {
	if (scannerDebug)
	    System.out.println("processComment(" + pos
			       + "," + endPos + "," + style + ")=|"
                               + new String(getRawCharacters(pos, endPos))
			       + "|");
    }

    /**
     * Called when a complete whitespace run has been scanned. pos and endPos 
     * will mark the whitespace boundary.
     */
    protected void processWhiteSpace() {
	if (scannerDebug)
	    System.out.println("processWhitespace(" + pos
			       + "," + endPos + ")=|" +
			       new String(getRawCharacters(pos, endPos))
			       + "|");
    }

    /**
     * Called when a line terminator has been processed.
     */
    protected void processLineTerminator() {
	if (scannerDebug)
	    System.out.println("processTerminator(" + pos
			       + "," + endPos + ")=|" +
			       new String(getRawCharacters(pos, endPos))
			       + "|");
    }