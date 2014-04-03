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
        
        DEBUG.P(0,this,"bracketsSuffix(JCExpression t)");
        return t;
    }