    /** Creator = Qualident [TypeArguments] ( ArrayCreatorRest | ClassCreatorRest )
     */
    JCExpression creator(int newpos, List<JCExpression> typeArgs) {
    	try {//我加上的
		DEBUG.P(this,"creator(2)");
		
        switch (S.token()) {
        case BYTE: case SHORT: case CHAR: case INT: case LONG: case FLOAT:
        case DOUBLE: case BOOLEAN:
            if (typeArgs == null)
                return arrayCreatorRest(newpos, basicType());
            break;
        default:
        }
        JCExpression t = qualident();
        int oldmode = mode;
        mode = TYPE;
        if (S.token() == LT) {
            checkGenerics();
            t = typeArguments(t);
        }
        while (S.token() == DOT) {
            int pos = S.pos();
            S.nextToken();
            t = toP(F.at(pos).Select(t, ident()));
            if (S.token() == LT) {
                checkGenerics();
                t = typeArguments(t);
            }
        }
        mode = oldmode;
        DEBUG.P("S.token()="+S.token());
        DEBUG.P("typeArgs="+typeArgs);
        if (S.token() == LBRACKET) {
            JCExpression e = arrayCreatorRest(newpos, t);
            if (typeArgs != null) {
                int pos = newpos;
                if (!typeArgs.isEmpty() && typeArgs.head.pos != Position.NOPOS) {
                    // note: this should always happen but we should
                    // not rely on this as the parser is continuously
                    // modified to improve error recovery.
                    pos = typeArgs.head.pos;
                }
                setErrorEndPos(S.prevEndPos());
				//这个错误key在中文properties文件中没有
				/*例子:
				class MemberClassG<T> {<T> MemberClassG(T t){}}
				{ MemberClassG[] mg=new <Long>MemberClassG<String>[]{};}
				*/
                reportSyntaxError(pos, "cannot.create.array.with.type.arguments");
                return toP(F.at(newpos).Erroneous(typeArgs.prepend(e)));
            }
            return e;
        } else if (S.token() == LPAREN) {
            return classCreatorRest(newpos, null, typeArgs, t);
        } else {
            reportSyntaxError(S.pos(), "expected2",
                               keywords.token2string(LPAREN),
                               keywords.token2string(LBRACKET));
            t = toP(F.at(newpos).NewClass(null, typeArgs, t, List.<JCExpression>nil(), null));
            return toP(F.at(newpos).Erroneous(List.<JCTree>of(t)));
        }
        
    	}finally{//我加上的
		DEBUG.P(0,this,"creator(2)");
		}
    }