    /** ArgumentsOpt = [ Arguments ]
     */
    JCExpression argumentsOpt(List<JCExpression> typeArgs, JCExpression t) {
    	try {//我加上的
		DEBUG.P(this,"argumentsOpt(2)");
		DEBUG.P("mode="+myMode(mode)+" S.token()="+S.token()+" typeArgs="+typeArgs);
		
        if ((mode & EXPR) != 0 && S.token() == LPAREN || typeArgs != null) {
            mode = EXPR;
            return arguments(typeArgs, t);
        } else {
            return t;
        }
        
        }finally{//我加上的
		DEBUG.P(0,this,"argumentsOpt(2)");
		}
    }

    /** Arguments = "(" [Expression { COMMA Expression }] ")"
     */
    List<JCExpression> arguments() {
    	DEBUG.P(this,"arguments()");
		DEBUG.P("S.token()="+S.token());
		
        ListBuffer<JCExpression> args = lb();
        if (S.token() == LPAREN) {
            S.nextToken();
            if (S.token() != RPAREN) {
                args.append(expression());
                while (S.token() == COMMA) {
                    S.nextToken();
                    args.append(expression());
                }
            }
            accept(RPAREN);
        } else {
            syntaxError(S.pos(), "expected", keywords.token2string(LPAREN));
        }
        
        DEBUG.P(0,this,"arguments()");
        return args.toList();
    }

    JCMethodInvocation arguments(List<JCExpression> typeArgs, JCExpression t) {
        int pos = S.pos();
        List<JCExpression> args = arguments();
        return toP(F.at(pos).Apply(typeArgs, t, args));
    }