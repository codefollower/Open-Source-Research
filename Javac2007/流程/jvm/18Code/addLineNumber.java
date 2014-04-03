    /** Add a line number entry.
     */
    public void addLineNumber(char startPc, char lineNumber) {
		//DEBUG.P(this,"addLineNumber(2)");
		//DEBUG.P("startPc="+(int)startPc+"  lineNumber="+(int)lineNumber);
		//DEBUG.P("lineDebugInfo="+lineDebugInfo);

		if (lineDebugInfo) {
			if (lineInfo.nonEmpty() && lineInfo.head[0] == startPc)
				lineInfo = lineInfo.tail;
			if (lineInfo.isEmpty() || lineInfo.head[1] != lineNumber)
				lineInfo = lineInfo.prepend(new char[]{startPc, lineNumber});
		}

		//DEBUG.P(0,this,"addLineNumber(2)");
    }