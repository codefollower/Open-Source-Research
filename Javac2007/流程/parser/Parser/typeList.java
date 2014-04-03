    /** TypeList = Type {"," Type}
     */
    List<JCExpression> typeList() {
    	try {//我加上的
		DEBUG.P(this,"typeList()");

        ListBuffer<JCExpression> ts = new ListBuffer<JCExpression>();
        ts.append(type());
        while (S.token() == COMMA) {
            S.nextToken();
            ts.append(type());
        }
        return ts.toList();
        
        }finally{//我加上的
		DEBUG.P(0,this,"typeList()");
		}
    }