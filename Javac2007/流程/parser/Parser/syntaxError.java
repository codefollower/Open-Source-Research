    private JCErroneous syntaxError(int pos, String key, Object... arg) {
	    try {//我加上的
	    DEBUG.P(this,"syntaxError(3)");
	    DEBUG.P("pos="+pos);
	    DEBUG.P("key="+key);

        return syntaxError(pos, null, key, arg);

		}finally{//我加上的
		DEBUG.P(0,this,"syntaxError(3)");
		}
    }

    private JCErroneous syntaxError(int pos, List<JCTree> errs, String key, Object... arg) {
        try {//我加上的
		DEBUG.P(this,"syntaxError(4)");
	    DEBUG.P("pos="+pos);
	    DEBUG.P("key="+key);
	    DEBUG.P("errs="+errs);

		setErrorEndPos(pos);
        reportSyntaxError(pos, key, arg);
        return toP(F.at(pos).Erroneous(errs));

		}finally{//我加上的
		DEBUG.P(0,this,"syntaxError(4)");
		}
    }

    private int errorPos = Position.NOPOS;
    /**
     * Report a syntax error at given position using the given
     * argument unless one was already reported at the same position.
     */
    private void reportSyntaxError(int pos, String key, Object... arg) {
	    DEBUG.P(this,"reportSyntaxError(3)");
    	DEBUG.P("pos="+pos);
    	DEBUG.P("S.errPos()="+S.errPos());
		DEBUG.P("S.token()="+S.token());

        if (pos > S.errPos() || pos == Position.NOPOS) {
            if (S.token() == EOF)
                log.error(pos, "premature.eof");
            else
                log.error(pos, key, arg);
        }
        S.errPos(pos);

		DEBUG.P("errorPos="+errorPos);
    	DEBUG.P("S.pos()="+S.pos());
		
		//例:Class c=int[][].char;
        if (S.pos() == errorPos)
            S.nextToken(); // guarantee progress
        errorPos = S.pos();

		DEBUG.P(0,this,"reportSyntaxError(3)");
    }


    /** Generate a syntax error at current position unless one was already
     *  reported at the same position.
     */
    private JCErroneous syntaxError(String key) {
        return syntaxError(S.pos(), key); //调用syntaxError(int pos, String key, Object... arg)
    }

    /** Generate a syntax error at current position unless one was
     *  already reported at the same position.
     */
    private JCErroneous syntaxError(String key, String arg) {
        return syntaxError(S.pos(), key, arg);
    }