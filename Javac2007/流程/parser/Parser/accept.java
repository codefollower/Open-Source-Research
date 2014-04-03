    /** If next input token matches given token, skip it, otherwise report
     *  an error.
     */
    public void accept(Token token) {
    	DEBUG.P(this,"accept(1)");
    	DEBUG.P("accToken="+token);
    	DEBUG.P("curToken="+S.token());
        if (S.token() == token) {
            S.nextToken();
        } else {
            setErrorEndPos(S.pos());
            reportSyntaxError(S.prevEndPos(), "expected", keywords.token2string(token));
        }
        DEBUG.P(0,this,"accept(1)");
    }