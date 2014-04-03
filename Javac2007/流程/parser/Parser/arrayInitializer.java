    /** ArrayInitializer = "{" [VariableInitializer {"," VariableInitializer}] [","] "}"
     */
    JCExpression arrayInitializer(int newpos, JCExpression t) {
    	try {//我加上的
		DEBUG.P(this,"arrayInitializer(2)");
		
        accept(LBRACE);
        ListBuffer<JCExpression> elems = new ListBuffer<JCExpression>();
        if (S.token() == COMMA) {
            S.nextToken();
        } else if (S.token() != RBRACE) {
        	//arrayInitializer()与variableInitializer()两者相互调用
        	//可以实现多维数组(如{{1,2},{3,4}}的初始化
            elems.append(variableInitializer());
            while (S.token() == COMMA) {
                S.nextToken();
                if (S.token() == RBRACE) break;
                elems.append(variableInitializer());
            }
        }
        accept(RBRACE);
        return toP(F.at(newpos).NewArray(t, List.<JCExpression>nil(), elems.toList()));
    	
    	}finally{//我加上的
		DEBUG.P(0,this,"arrayInitializer(2)");
		}  
    }