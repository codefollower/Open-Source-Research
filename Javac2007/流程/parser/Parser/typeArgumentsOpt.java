    /**  TypeArgumentsOpt = [ TypeArguments ]
     */
    JCExpression typeArgumentsOpt(JCExpression t) {
    	try {//我加上的
		DEBUG.P(this,"typeArgumentsOpt(JCExpression t)");
		DEBUG.P("t="+t);
		DEBUG.P("S.token()="+S.token());
		/*这里必须是参数化的类型声明
		class MemberClassH<T> {}
		MemberClassH<?> Mh1;
		MemberClassH<String> Mh2;
		MemberClassH<? extends Number> Mh3;
		*/
		
        if (S.token() == LT &&
            (mode & TYPE) != 0 &&
            (mode & NOPARAMS) == 0) {
            mode = TYPE;
            checkGenerics();
            return typeArguments(t);
        } else {
            return t;
        }

        }finally{//我加上的
		DEBUG.P(0,this,"typeArgumentsOpt(JCExpression t)");
		}       
    }
    
    List<JCExpression> typeArgumentsOpt() {
    	try {//我加上的
		DEBUG.P(this,"typeArgumentsOpt()");
		
        return typeArgumentsOpt(TYPE);
        
        }finally{//我加上的
		DEBUG.P(0,this,"typeArgumentsOpt()");
		}
    }

    List<JCExpression> typeArgumentsOpt(int useMode) {
    	try {//我加上的
        DEBUG.P(this,"typeArgumentsOpt(int useMode)");
        DEBUG.P("useMode="+myMode(useMode));
        DEBUG.P("mode="+myMode(mode));
        DEBUG.P("S.token()="+S.token());

        if (S.token() == LT) {
            checkGenerics();
            if ((mode & useMode) == 0 ||
                (mode & NOPARAMS) != 0) {
                illegal();
            }
            mode = useMode;
            return typeArguments();
        }
        return null;
        
        }finally{//我加上的
        DEBUG.P(0,this,"typeArgumentsOpt(int useMode)");
        }
    }

    /**  TypeArguments  = "<" TypeArgument {"," TypeArgument} ">"
     */
    List<JCExpression> typeArguments() {
    	try {//我加上的
		DEBUG.P(this,"typeArguments()");
		DEBUG.P("S.token()="+S.token()+" mode="+myMode(mode));
		
        ListBuffer<JCExpression> args = lb();
        if (S.token() == LT) {
            S.nextToken();
            //TypeArguments不能像这样 expr=<?>
            
            //只有mode不含EXPR时((mode & EXPR) == 0)，
            //才能在“<>”中放入“？”号
            args.append(((mode & EXPR) == 0) ? typeArgument() : type());
            while (S.token() == COMMA) {
                S.nextToken();
                args.append(((mode & EXPR) == 0) ? typeArgument() : type());
            }
            switch (S.token()) {
            case GTGTGTEQ:
                S.token(GTGTEQ);
                break;
            case GTGTEQ:
                S.token(GTEQ);
                break;
            case GTEQ:
                S.token(EQ);
                break;
            case GTGTGT:
                S.token(GTGT);
                break;
            case GTGT:
                S.token(GT);
                break;
            default:
                accept(GT);
                break;
            }
        } else {
            syntaxError(S.pos(), "expected", keywords.token2string(LT));
        }
        return args.toList();
        
        }finally{//我加上的
		DEBUG.P(0,this,"typeArguments()");
		}
    }

    /** TypeArgument = Type
     *               | "?"
     *               | "?" EXTENDS Type {"&" Type}
     *               | "?" SUPER Type
     */
     
     /*
     在Java Language Specification, Third Edition
	 18.1. The Grammar of the Java Programming Language
	 中的定义如下:
     TypeArgument:
      Type
      ? [( extends | super ) Type]
     所以上面的语法是错误的。
     "?" EXTENDS Type {"&" Type} 应改成 "?" EXTENDS Type
     */
    JCExpression typeArgument() {
    	try {//我加上的
		DEBUG.P(this,"typeArgument()");
		
        if (S.token() != QUES) return type();
		//以下JCWildcard树结点的开始位置pos是从"?"号这个token的开始位置算起的
        int pos = S.pos();
        S.nextToken();
        if (S.token() == EXTENDS) {
            TypeBoundKind t = to(F.at(S.pos()).TypeBoundKind(BoundKind.EXTENDS));
            S.nextToken();
            return F.at(pos).Wildcard(t, type());
        } else if (S.token() == SUPER) {
            TypeBoundKind t = to(F.at(S.pos()).TypeBoundKind(BoundKind.SUPER));
            S.nextToken();
            return F.at(pos).Wildcard(t, type());
        } else if (S.token() == IDENTIFIER) {
			/*例子:
			class MemberClassH<T> {}
			MemberClassH<? mh;
			*/
            //error recovery
            reportSyntaxError(S.prevEndPos(), "expected3",
                    keywords.token2string(GT),
                    keywords.token2string(EXTENDS),
                    keywords.token2string(SUPER));
            TypeBoundKind t = F.at(Position.NOPOS).TypeBoundKind(BoundKind.UNBOUND);
            JCExpression wc = toP(F.at(pos).Wildcard(t, null));
            JCIdent id = toP(F.at(S.pos()).Ident(ident()));
            return F.at(pos).Erroneous(List.<JCTree>of(wc, id));
        } else {
			/*如果是这样的例子:
			class MemberClassH<T> {}
			MemberClassH<? <;

			那么在这个方法里并不报错，照样生成UNBOUND类型的JCWildcard，
			而是将不合法的"<"字符留给调用这个方法的调用者自行处理，
			比如通过typeArguments()调用这个方法时，在typeArguments()里的
			"default:
                accept(GT);"这段代码里就会报告"需要 >"这样的错误提示
			*/
            TypeBoundKind t = F.at(Position.NOPOS).TypeBoundKind(BoundKind.UNBOUND);
            return toP(F.at(pos).Wildcard(t, null));
        }
        
        }finally{//我加上的
		DEBUG.P(0,this,"typeArgument()");
		}
    }

    JCTypeApply typeArguments(JCExpression t) {
    	try {//我加上的
		DEBUG.P(this,"typeArguments(JCExpression t)");
		
        int pos = S.pos();
        List<JCExpression> args = typeArguments();
        return toP(F.at(pos).TypeApply(t, args));
        
        }finally{//我加上的
		DEBUG.P(0,this,"typeArguments(JCExpression t)");
		}
    }