    /** Statement =
     *       Block
     *     | IF ParExpression Statement [ELSE Statement]
     *     | FOR "(" ForInitOpt ";" [Expression] ";" ForUpdateOpt ")" Statement
     *     | FOR "(" FormalParameter : Expression ")" Statement
     *     | WHILE ParExpression Statement
     *     | DO Statement WHILE ParExpression ";"
     *     | TRY Block ( Catches | [Catches] FinallyPart )
     *     | SWITCH ParExpression "{" SwitchBlockStatementGroups "}"
     *     | SYNCHRONIZED ParExpression Block
     *     | RETURN [Expression] ";"
     *     | THROW Expression ";"
     *     | BREAK [Ident] ";"
     *     | CONTINUE [Ident] ";"
     *     | ASSERT Expression [ ":" Expression ] ";"
     *     | ";"
     *     | ExpressionStatement
     *     | Ident ":" Statement
     */
    @SuppressWarnings("fallthrough")
    public JCStatement statement() {
    	try {//我加上的
		DEBUG.P(this,"statement()");

        int pos = S.pos();
        switch (S.token()) {
        case LBRACE:
            return block();
        case IF: {
            S.nextToken();
            JCExpression cond = parExpression();
            JCStatement thenpart = statement();
            JCStatement elsepart = null;
            if (S.token() == ELSE) {
                S.nextToken();
                elsepart = statement();
            }
            return F.at(pos).If(cond, thenpart, elsepart);
        }
        case FOR: {
            S.nextToken();
            accept(LPAREN);
            List<JCStatement> inits = S.token() == SEMI ? List.<JCStatement>nil() : forInit();
            DEBUG.P("inits.length()="+inits.length());
            if (inits.length() == 1 &&
                inits.head.tag == JCTree.VARDEF &&
                ((JCVariableDecl) inits.head).init == null &&
                S.token() == COLON) {
                checkForeach();
                JCVariableDecl var = (JCVariableDecl)inits.head;
                accept(COLON);
                JCExpression expr = expression();
                accept(RPAREN);
                JCStatement body = statement();
                return F.at(pos).ForeachLoop(var, expr, body);
            } else {
                accept(SEMI);
                JCExpression cond = S.token() == SEMI ? null : expression();
                accept(SEMI);
                List<JCExpressionStatement> steps = S.token() == RPAREN ? List.<JCExpressionStatement>nil() : forUpdate();
                accept(RPAREN);
                JCStatement body = statement();
                return F.at(pos).ForLoop(inits, cond, steps, body);
            }
        }
        case WHILE: {
            S.nextToken();
            JCExpression cond = parExpression();
            JCStatement body = statement();
            return F.at(pos).WhileLoop(cond, body);
        }
        case DO: {
            S.nextToken();
            JCStatement body = statement();
            accept(WHILE);
            JCExpression cond = parExpression();
            JCDoWhileLoop t = to(F.at(pos).DoLoop(body, cond));
            accept(SEMI);
            return t;
        }
        case TRY: {
            S.nextToken();
            JCBlock body = block();
            ListBuffer<JCCatch> catchers = new ListBuffer<JCCatch>();
            JCBlock finalizer = null;
            if (S.token() == CATCH || S.token() == FINALLY) {
                while (S.token() == CATCH) catchers.append(catchClause());
                if (S.token() == FINALLY) {
                    S.nextToken();
                    finalizer = block();
                }
            } else {
                log.error(pos, "try.without.catch.or.finally");
            }
            return F.at(pos).Try(body, catchers.toList(), finalizer);
        }
        case SWITCH: {
            S.nextToken();
            JCExpression selector = parExpression();
            accept(LBRACE);
            List<JCCase> cases = switchBlockStatementGroups();
            JCSwitch t = to(F.at(pos).Switch(selector, cases));
            accept(RBRACE);
            return t;
        }
        case SYNCHRONIZED: {
            S.nextToken();
            JCExpression lock = parExpression();
            JCBlock body = block();
            return F.at(pos).Synchronized(lock, body);
        }
        case RETURN: {
            S.nextToken();
            JCExpression result = S.token() == SEMI ? null : expression();
            JCReturn t = to(F.at(pos).Return(result));
            accept(SEMI);
            return t;
        }
        case THROW: {
            S.nextToken();
            JCExpression exc = expression();
            JCThrow t = to(F.at(pos).Throw(exc));
            accept(SEMI);
            return t;
        }
        case BREAK: {
            S.nextToken();
            /*
            bin\mysrc\my\test\Test.java:80: 从版本 1.4 开始，'assert' 是一个关键字，但不能用
			作标识符
			（请使用 -source 1.3 或更低版本以便将 'assert' 用作标识符）
			                        break assert;
			                              ^
			1 错误
			*/
            Name label = (S.token() == IDENTIFIER || S.token() == ASSERT || S.token() == ENUM) ? ident() : null;
            JCBreak t = to(F.at(pos).Break(label));
            accept(SEMI);
            return t;
        }
        case CONTINUE: {
            S.nextToken();
            Name label = (S.token() == IDENTIFIER || S.token() == ASSERT || S.token() == ENUM) ? ident() : null;
            JCContinue t =  to(F.at(pos).Continue(label));
            accept(SEMI);
            return t;
        }
        case SEMI:
            S.nextToken();
            return toP(F.at(pos).Skip());
        case ELSE:
            return toP(F.Exec(syntaxError("else.without.if")));
        case FINALLY:
            return toP(F.Exec(syntaxError("finally.without.try")));
        case CATCH:
            return toP(F.Exec(syntaxError("catch.without.try")));
        case ASSERT: {
            if (allowAsserts && S.token() == ASSERT) {
                S.nextToken();
                JCExpression assertion = expression();
                JCExpression message = null;
                if (S.token() == COLON) {
                    S.nextToken();
                    message = expression();
                }
                JCAssert t = to(F.at(pos).Assert(assertion, message));
                accept(SEMI);
                return t;
            }
            /* else fall through to default case */
        }
        case ENUM:
        default:
            Name name = S.name();
            JCExpression expr = expression();
            if (S.token() == COLON && expr.tag == JCTree.IDENT) {
                S.nextToken();
                JCStatement stat = statement();
                return F.at(pos).Labelled(name, stat);
            } else {
                // This Exec is an "ExpressionStatement"; it subsumes the terminating semicolon
                JCExpressionStatement stat = to(F.at(pos).Exec(checkExprStat(expr)));
                accept(SEMI);
                return stat;
            }
        }

        }finally{//我加上的
		DEBUG.P(0,this,"statement()");
		}        
    }