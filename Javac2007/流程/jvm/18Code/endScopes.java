    /** End scopes of all variables with registers >= first.
     */
    public void endScopes(int first) {
		DEBUG.P(this,"endScopes(int first)");
		DEBUG.P("first="+first+" nextreg="+nextreg);
		int prevNextReg = nextreg;
		nextreg = first;
		for (int i = nextreg; i < prevNextReg; i++) endScope(i);

		DEBUG.P("");
		DEBUG.P("重新赋值nextreg="+nextreg);
		DEBUG.P(0,this,"endScopes(int first)");
    }