    //下面的Expr是指Expression(参考18.1. The Grammar of the Java Programming Language)
    /** Expression3    = PrefixOp Expression3
     *                 | "(" Expr | TypeNoParams ")" Expression3
     *                 | Primary {Selector} {PostfixOp}
     *  Primary        = "(" Expression ")"
     *                 | Literal
     *                 | [TypeArguments] THIS [Arguments]
     *                 | [TypeArguments] SUPER SuperSuffix
     *                 | NEW [TypeArguments] Creator
     *                 | Ident { "." Ident }
     *                   [ "[" ( "]" BracketsOpt "." CLASS | Expression "]" )
     *                   | Arguments
     *                   | "." ( CLASS | THIS | [TypeArguments] SUPER Arguments | NEW [TypeArguments] InnerCreator )
     *                   ]
     *                 | BasicType BracketsOpt "." CLASS
     *  PrefixOp       = "++" | "--" | "!" | "~" | "+" | "-"
     *  PostfixOp      = "++" | "--"
     *  Type3          = Ident { "." Ident } [TypeArguments] {TypeSelector} BracketsOpt
     *                 | BasicType
     *  TypeNoParams3  = Ident { "." Ident } BracketsOpt
     *  Selector       = "." [TypeArguments] Ident [Arguments]
     *                 | "." THIS
     *                 | "." [TypeArguments] SUPER SuperSuffix
     *                 | "." NEW [TypeArguments] InnerCreator
     *                 | "[" Expression "]"
     *  TypeSelector   = "." Ident [TypeArguments]
     *  SuperSuffix    = Arguments | "." Ident [Arguments]
     */
    protected JCExpression term3() {
    	try {//我加上的
		DEBUG.P(this,"term3()");

        int pos = S.pos();
        JCExpression t;
        List<JCExpression> typeArgs = typeArgumentsOpt(EXPR);

        switch (S.token()) {
        case QUES: //TypeArguments不能像这样 expr=<?>
        	DEBUG.P("case QUES:");
			//如: ClassB<?> c=(ClassB<?>)cb;(见:case LPAREN)
            if ((mode & TYPE) != 0 && (mode & (TYPEARG|NOPARAMS)) == TYPEARG) {
                mode = TYPE;
                return typeArgument();
            } else
                return illegal();
                
                
        /*
        表达式以运算符: ++、--、BANG("!")、TILDE("~")、+、-  开始,
        这几个运算符都是一元运算符，从下面的代码“t = term3()”可以
        看出结合顺序是从右到左的,如:++--myInt 相当于:++(--myInt)
        ++--myInt将生成两棵JCUnary树
        
        但非常值得注意的是不管是++--myInt或是++(--myInt)这样的语法却是
        错误的(错误在Parser阶段没有发现):
        
        bin\mysrc\my\test\Test.java:98: 意外的类型
		需要： 变量
		找到： 值
		                ++(--myInt);
		                   ^
		1 错误
        */
        case PLUSPLUS: case SUBSUB: case BANG: case TILDE: case PLUS: case SUB:
        	DEBUG.P("(case PrefixOp) mode="+myMode(mode));
            if (typeArgs == null && (mode & EXPR) != 0) {
                Token token = S.token();
                S.nextToken();
                mode = EXPR;
                if (token == SUB &&
                    (S.token() == INTLITERAL || S.token() == LONGLITERAL) &&
                    S.radix() == 10) {
                    mode = EXPR;
                    t = literal(names.hyphen);
                } else {
                    t = term3();
                    return F.at(pos).Unary(unoptag(token), t);
                }
            } else return illegal();
            break;
        case LPAREN:
        	DEBUG.P("case LPAREN:");
            if (typeArgs == null && (mode & EXPR) != 0) {
                S.nextToken();
                mode = EXPR | TYPE | NOPARAMS;
                t = term3();
				//如: ClassB<?> c=(ClassB<?>)cb;
                if ((mode & TYPE) != 0 && S.token() == LT) {
                    // Could be a cast to a parameterized type
                    int op = JCTree.LT;
                    int pos1 = S.pos();
                    S.nextToken();
                    mode &= (EXPR | TYPE);
                    mode |= TYPEARG;
                    JCExpression t1 = term3();
                    if ((mode & TYPE) != 0 &&
                        (S.token() == COMMA || S.token() == GT)) {
                        mode = TYPE;
                        ListBuffer<JCExpression> args = new ListBuffer<JCExpression>();
                        args.append(t1);
                        while (S.token() == COMMA) {
                            S.nextToken();
                            args.append(typeArgument());
                        }
                        accept(GT);
                        t = F.at(pos1).TypeApply(t, args.toList());
                        checkGenerics();
                        t = bracketsOpt(toP(t));
                    } else if ((mode & EXPR) != 0) {
                        mode = EXPR;
                        t = F.at(pos1).Binary(op, t, term2Rest(t1, TreeInfo.shiftPrec));
                        t = termRest(term1Rest(term2Rest(t, TreeInfo.orPrec)));
                    } else {
                        accept(GT);
                    }
                } else {
                    t = termRest(term1Rest(term2Rest(t, TreeInfo.orPrec)));
                }
                accept(RPAREN);
                lastmode = mode;
                mode = EXPR;
				DEBUG.P("lastmode="+myMode(lastmode));
                if ((lastmode & EXPR) == 0) {//如：byte b=(byte)++i;
                    JCExpression t1 = term3();
                    return F.at(pos).TypeCast(t, t1);
                } else if ((lastmode & TYPE) != 0) {
                    switch (S.token()) {
                    /*case PLUSPLUS: case SUBSUB: */
                    case BANG: case TILDE:
                    case LPAREN: case THIS: case SUPER:
                    case INTLITERAL: case LONGLITERAL: case FLOATLITERAL:
                    case DOUBLELITERAL: case CHARLITERAL: case STRINGLITERAL:
                    case TRUE: case FALSE: case NULL:
                    case NEW: case IDENTIFIER: case ASSERT: case ENUM:
                    case BYTE: case SHORT: case CHAR: case INT:
                    case LONG: case FLOAT: case DOUBLE: case BOOLEAN: case VOID:
                        JCExpression t1 = term3();
                        return F.at(pos).TypeCast(t, t1);
                    }
                }
            } else return illegal();
            t = toP(F.at(pos).Parens(t));
            break;
        case THIS:
            if ((mode & EXPR) != 0) {
                mode = EXPR;
                t = to(F.at(pos).Ident(names._this));
                S.nextToken();
                if (typeArgs == null)
                    t = argumentsOpt(null, t);
                else
                    t = arguments(typeArgs, t);
                typeArgs = null;
            } else return illegal();
            break;
        case SUPER:
            if ((mode & EXPR) != 0) {
                mode = EXPR;
                t = to(superSuffix(typeArgs, F.at(pos).Ident(names._super)));
                typeArgs = null;
            } else return illegal();
            break;
        case INTLITERAL: case LONGLITERAL: case FLOATLITERAL: case DOUBLELITERAL:
        case CHARLITERAL: case STRINGLITERAL:
        case TRUE: case FALSE: case NULL:
            if (typeArgs == null && (mode & EXPR) != 0) {
                mode = EXPR;
                t = literal(names.empty);
            } else return illegal();
            break;
        case NEW:
            if (typeArgs != null) return illegal();
            if ((mode & EXPR) != 0) {
                mode = EXPR;
                S.nextToken();
                if (S.token() == LT) typeArgs = typeArguments();
                t = creator(pos, typeArgs);
                typeArgs = null;
            } else return illegal();
            break;
        case IDENTIFIER: case ASSERT: case ENUM:
            if (typeArgs != null) return illegal();
            t = toP(F.at(S.pos()).Ident(ident()));
            loop: while (true) {
                pos = S.pos();
                switch (S.token()) {
                case LBRACKET:
                    S.nextToken();
                    if (S.token() == RBRACKET) {
                        S.nextToken();
                        t = bracketsOpt(t);
                        t = toP(F.at(pos).TypeArray(t));
                        t = bracketsSuffix(t);//如:Class c=ParserTest[][].class;
                    } else {
                        if ((mode & EXPR) != 0) {
							//例:{ int a1[]={1,2}, a2; a1[0]=3; a2=a1[1]; }
                            mode = EXPR;
                            JCExpression t1 = term();
                            DEBUG.P("(case IDENTIFIER LBRACKET) t="+t+" t1="+t1);
                            t = to(F.at(pos).Indexed(t, t1));
                        }
                        accept(RBRACKET);
                    }
                    break loop;
                case LPAREN:
                    if ((mode & EXPR) != 0) {
                        mode = EXPR;
						DEBUG.P("(case IDENTIFIER LPAREN) t="+t+" typeArgs="+typeArgs);
						/*例:
						static class MemberClassB {
							static <R> R methodA(R r) { return r; }
						}
						{ MemberClassB.methodA(this); }
						{ MemberClassB.methodA("str"); }
						{ MemberClassB.<ParserTest>methodA(this); }
						{ MemberClassB.<String>methodA("str"); }

						//输出
						t=MemberClassB.methodA typeArgs=null
						t=MemberClassB.methodA typeArgs=null
						t=MemberClassB.methodA typeArgs=ParserTest
						t=MemberClassB.methodA typeArgs=String
						*/
                        t = arguments(typeArgs, t);
                        typeArgs = null;
                    }
                    break loop;
                case DOT:
                    S.nextToken();
                    typeArgs = typeArgumentsOpt(EXPR);
                    if ((mode & EXPR) != 0) {
                        switch (S.token()) {
                        case CLASS:
                            if (typeArgs != null) return illegal();
                            mode = EXPR;
                            t = to(F.at(pos).Select(t, names._class));
                            S.nextToken();
                            break loop;
                        case THIS:
							/*例
							class MemberClassC {
								{ ParserTest.this(); } //有错
								{ ParserTest pt=ParserTest.this; } //正确
							}
							*/
							DEBUG.P("(case IDENTIFIER THIS) t="+t+" typeArgs="+typeArgs);
                            if (typeArgs != null) return illegal();
                            mode = EXPR;
                            t = to(F.at(pos).Select(t, names._this));
                            S.nextToken();
                            break loop;
                        case SUPER:
							DEBUG.P("(case IDENTIFIER SUPER) t="+t+" typeArgs="+typeArgs);
							/*例
							int superField;
							<T> ParserTest(T t){}
							static <T> void methodB(T t){}
							class MemberClassD extends ParserTest {
								MemberClassD() { <String>super("str"); }
								{ int sf=MemberClassD.super.superField; }
								{ MemberClassD.super.<String>methodB("str"); }
							}
							*/
                            mode = EXPR;
                            t = to(F.at(pos).Select(t, names._super));
                            t = superSuffix(typeArgs, t);
                            typeArgs = null;
                            break loop;
                        case NEW:
							/*例子
							class MemberClassE {
								class MemberClassF<T> {
									<T> MemberClassF(T t){}
								}
							}
							{
								MemberClassE me=new MemberClassE();
								MemberClassE.MemberClassF<Long> mf=me.new <String>MemberClassF<Long>("str");
								//类型的格式不正确，缺少某些参数(在Check类中检查)
								//MemberClassE.MemberClassF mf=me.new <String>MemberClassF<Long>("str");
							}
							*/
                            if (typeArgs != null) return illegal();
                            mode = EXPR;
                            int pos1 = S.pos();
                            S.nextToken();
                            if (S.token() == LT) typeArgs = typeArguments();
                            t = innerCreator(pos1, typeArgs, t);
                            typeArgs = null;
                            break loop;
                        }
                    }
                    // typeArgs saved for next loop iteration.
                    t = toP(F.at(pos).Select(t, ident()));
                    break;
                default:
                    break loop;
                }
            }
            if (typeArgs != null) illegal();
            t = typeArgumentsOpt(t);
            break;
        case BYTE: case SHORT: case CHAR: case INT: case LONG: case FLOAT:
        case DOUBLE: case BOOLEAN:
            if (typeArgs != null) illegal();
            t = bracketsSuffix(bracketsOpt(basicType()));
            break;
        case VOID:
            if (typeArgs != null) illegal();
            if ((mode & EXPR) != 0) {
                S.nextToken();
                if (S.token() == DOT) {
                    JCPrimitiveTypeTree ti = toP(F.at(pos).TypeIdent(TypeTags.VOID));
                    t = bracketsSuffix(ti);
                } else {
                    return illegal(pos);
                }
            } else {
                return illegal();
            }
            break;
        default:
            return illegal();
        }
        if (typeArgs != null) illegal();
        while (true) { //对应{Selector}
            int pos1 = S.pos();
            if (S.token() == LBRACKET) {
                S.nextToken();
				DEBUG.P("mode="+myMode(mode));
                if ((mode & TYPE) != 0) {
                    int oldmode = mode;
                    mode = TYPE;
                    if (S.token() == RBRACKET) {
                        S.nextToken();
                        t = bracketsOpt(t);
                        t = toP(F.at(pos1).TypeArray(t));
                        return t;
                    }
                    mode = oldmode;
                }
                if ((mode & EXPR) != 0) {
                    mode = EXPR;
                    JCExpression t1 = term();
					//对于像这样的二维数组
					//int[][] ii2={{1,2},{3,4}};
					//int i2=ii2[1][2]; //主要是这句
					//先在case IDENTIFIER中处理ii2[1]，再转到这里处理[2]
					//(while (true) t=ii2[1] t1=2
					//Indexed t=ii2[1][2]
					DEBUG.P("(while (true) t="+t+" t1="+t1);
                    t = to(F.at(pos1).Indexed(t, t1));
					DEBUG.P("Indexed t="+t);
                }
                accept(RBRACKET);
            } else if (S.token() == DOT) {
                S.nextToken();
                typeArgs = typeArgumentsOpt(EXPR);
                if (S.token() == SUPER && (mode & EXPR) != 0) {
                    mode = EXPR;
                    t = to(F.at(pos1).Select(t, names._super));
                    S.nextToken();
                    t = arguments(typeArgs, t);
                    typeArgs = null;
                } else if (S.token() == NEW && (mode & EXPR) != 0) {
                    if (typeArgs != null) return illegal();
                    mode = EXPR;
                    int pos2 = S.pos();
                    S.nextToken();
                    if (S.token() == LT) typeArgs = typeArguments();
                    t = innerCreator(pos2, typeArgs, t);
                    typeArgs = null;
                } else {
                    t = toP(F.at(pos1).Select(t, ident()));
                    t = argumentsOpt(typeArgs, typeArgumentsOpt(t));
                    typeArgs = null;
                }
            } else {
                break;
            }
        }
		 //对应{PostfixOp}
        while ((S.token() == PLUSPLUS || S.token() == SUBSUB) && (mode & EXPR) != 0) {
			/* 在语法分析阶段:i++--++--是正确的，并且从左到右生成JCUnary
			PostfixOp t=i++
			PostfixOp t=i++--
			PostfixOp t=i++--++
			PostfixOp t=i++--++--
			----------------------------------------------
			test\parser\ParserTest.java:200: 意外的类型
			需要： 变量
			找到： 值
							int i2=i++--++--;
									^
			1 错误
			*/
            mode = EXPR;
            t = to(F.at(S.pos()).Unary(
                  S.token() == PLUSPLUS ? JCTree.POSTINC : JCTree.POSTDEC, t));
			DEBUG.P("PostfixOp t="+t);
            S.nextToken();
        }
        return toP(t);
        
        }finally{//我加上的
		DEBUG.P(0,this,"term3()");
		}
    }