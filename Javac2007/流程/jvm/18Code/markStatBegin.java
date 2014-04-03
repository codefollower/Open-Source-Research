    /** Force stat begin eagerly
     */
    public void markStatBegin() {
		//DEBUG.P(this,"markStatBegin()");
		//DEBUG.P("alive="+alive+"  lineDebugInfo="+lineDebugInfo);
		
		if (alive && lineDebugInfo) {
			int line = lineMap.getLineNumber(pendingStatPos);
			char cp1 = (char)cp;
			char line1 = (char)line;
			//DEBUG.P("(cp1 == cp && line1 == line)="+(cp1 == cp && line1 == line));
			if (cp1 == cp && line1 == line)
				addLineNumber(cp1, line1);
		}
		pendingStatPos = Position.NOPOS;
		
		//DEBUG.P(0,this,"markStatBegin()");
    }