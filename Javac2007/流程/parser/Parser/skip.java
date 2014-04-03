    //什么时候该调用这个方法来从错误中恢复呢？当S.pos() <= errorEndPos？？？
    //那什么时候该判断S.pos() <= errorEndPos？当errorEndPos有可能改变吗？
    /** Skip forward until a suitable stop token is found.
     */
    private void skip(boolean stopAtImport, boolean stopAtMemberDecl, boolean stopAtIdentifier, boolean stopAtStatement) {
		try {//我加上的
		DEBUG.P(this,"skip(4)");
		DEBUG.P("stopAtImport    ="+stopAtImport);
		DEBUG.P("stopAtMemberDecl="+stopAtMemberDecl);
		DEBUG.P("stopAtIdentifier="+stopAtIdentifier);
		DEBUG.P("stopAtStatement ="+stopAtStatement);

		while (true) {
			switch (S.token()) {
				case SEMI:
                    S.nextToken();
                    return;
                case PUBLIC:
                case FINAL:
                case ABSTRACT:
                case MONKEYS_AT:
                case EOF:
                case CLASS:
                case INTERFACE:
                case ENUM:
                    return;
                case IMPORT:
                	//如果之前的错误是在分析import语句时发现的,经过若干次
                	//nextToken()后，找到了新的叫IMPORT的token，说明找到了
                	//一条新的import语句，现在就可以正常解析了
                    if (stopAtImport)
                        return;
                    break;
                case LBRACE:
                case RBRACE:
                case PRIVATE:
                case PROTECTED:
                case STATIC:
                case TRANSIENT:
                case NATIVE:
                case VOLATILE:
                case SYNCHRONIZED:
                case STRICTFP:
                case LT:
                case BYTE:
                case SHORT:
                case CHAR:
                case INT:
                case LONG:
                case FLOAT:
                case DOUBLE:
                case BOOLEAN:
                case VOID:
                    if (stopAtMemberDecl)
                        return;
                    break;
                case IDENTIFIER:
					if (stopAtIdentifier)
						return;
					break;
                case CASE:
                case DEFAULT:
                case IF:
                case FOR:
                case WHILE:
                case DO:
                case TRY:
                case SWITCH:
                case RETURN:
                case THROW:
                case BREAK:
                case CONTINUE:
                case ELSE:
                case FINALLY:
                case CATCH:
                    if (stopAtStatement)
                        return;
                    break;
            }
            S.nextToken();
        }
        
		}finally{//我加上的
		DEBUG.P(0,this,"skip(4)");
		}
    }