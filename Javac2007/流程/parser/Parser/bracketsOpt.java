    /*
    bracketsOpt和bracketsOptCont这两个方法用来生成一棵JCArrayTypeTree
    如:int a[]将对应一棵elemtype为int的JCArrayTypeTree；
    如:int a[][]将对应一棵elemtype为int型数组的JCArrayTypeTree；
    多维数组通过bracketsOpt和bracketsOptCont这两个方法互相调用实现
    
    int a[][]用JCArrayTypeTree表示为"
    JCArrayTypeTree = {
    	JCExpression elemtype = {
    		JCArrayTypeTree = {
    			JCExpression elemtype = int;
    		}
    	}
    }
    
    int a[][]与int[][] a这两种表示方式都是一样的
    */
    
    /** BracketsOpt = {"[" "]"}
     */
    private JCExpression bracketsOpt(JCExpression t) {
    	try {//我加上的
		DEBUG.P(this,"bracketsOpt(JCExpression t)");
		DEBUG.P("t="+t);
		DEBUG.P("S.token()="+S.token());
		
        if (S.token() == LBRACKET) {
            int pos = S.pos();
            S.nextToken();
            t = bracketsOptCont(t, pos);
            F.at(pos);
        }
        DEBUG.P("t="+t);
        return t;
        
        }finally{//我加上的
		DEBUG.P(0,this,"bracketsOpt(JCExpression t)");
		}    
    }

    private JCArrayTypeTree bracketsOptCont(JCExpression t, int pos) {
        accept(RBRACKET);
        t = bracketsOpt(t);
        return toP(F.at(pos).TypeArray(t));
    }

    /** BracketsSuffixExpr = "." CLASS
     *  BracketsSuffixType =
     */
    JCExpression bracketsSuffix(JCExpression t) {
    	DEBUG.P(this,"bracketsSuffix(JCExpression t)");
		DEBUG.P("t="+t);
		DEBUG.P("mode="+myMode(mode)+" S.token()="+S.token());
		//例:Class c=int[][].class;
        if ((mode & EXPR) != 0 && S.token() == DOT) {
            mode = EXPR;
            int pos = S.pos();
            S.nextToken();
            accept(CLASS);
            if (S.pos() == errorEndPos) {
                // error recovery
                Name name = null;
                if (S.token() == IDENTIFIER) {//例:Class c=int[][].classA;
                    name = S.name();
                    S.nextToken();
                } else {//例:Class c=int[][].char;//可以触发两次错语，但只报一次
                    name = names.error;
                }
				DEBUG.P("name="+name);
                t = F.at(pos).Erroneous(List.<JCTree>of(toP(F.at(pos).Select(t, name))));
            } else {
                t = toP(F.at(pos).Select(t, names._class));
            }
        } else if ((mode & TYPE) != 0) {
            mode = TYPE; //注意这里 如:public int[][] i1={{1,2},{3,4}};
        } else {
			//例:Class c=int[][];
			//例:Class c=int[][].123;
            syntaxError(S.pos(), "dot.class.expected");
        }
        
		DEBUG.P("t="+t);
		DEBUG.P("mode="+myMode(mode)+" S.token()="+S.token());
        DEBUG.P(0,this,"bracketsSuffix(JCExpression t)");
        return t;
    }