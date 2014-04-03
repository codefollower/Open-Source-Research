/* **************************************************************************
 * Catch clauses
 ****************************************************************************/

    /** Add a catch clause to code.
     */
    public void addCatch(
		char startPc, char endPc, char handlerPc, char catchType) {
		DEBUG.P(this,"addCatch(4)");
		DEBUG.P("startPc="+(int)startPc+" endPc="+(int)endPc);
		DEBUG.P("handlerPc="+(int)handlerPc+" catchType="+(int)catchType);
		
		catchInfo.append(new char[]{startPc, endPc, handlerPc, catchType});
		
		DEBUG.P(0,this,"addCatch(4)");
    }