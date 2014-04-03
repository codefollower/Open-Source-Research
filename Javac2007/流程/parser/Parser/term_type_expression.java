    /** terms can be either expressions or types.
     */
    public JCExpression expression() {
    	try {//我加上的
		DEBUG.P(this,"expression()");
		
        return term(EXPR);

        }finally{//我加上的
		DEBUG.P(0,this,"expression()");
		}        
    }

    public JCExpression type() {
    	try {//我加上的
		DEBUG.P(this,"type()");

        return term(TYPE);
        
        }finally{//我加上的
		DEBUG.P(0,this,"type()");
		}
    }

    JCExpression term(int newmode) {
    	try {//我加上的
		DEBUG.P(this,"term(int newmode)");
		DEBUG.P("newmode="+myMode(newmode)+"  mode="+myMode(mode));
		
        int prevmode = mode;
        mode = newmode;
        JCExpression t = term();
        lastmode = mode;
        mode = prevmode;
        return t;
        
        }finally{//我加上的
		DEBUG.P(0,this,"term(int newmode)");
		}
    }