    /** VariableInitializer = ArrayInitializer | Expression
     */
    public JCExpression variableInitializer() {
    	try {//我加上的
		DEBUG.P(this,"variableInitializer()");
		        
        return S.token() == LBRACE ? arrayInitializer(S.pos(), null) : expression();

		}finally{//我加上的
		DEBUG.P(0,this,"variableInitializer()");
		}    
    }
