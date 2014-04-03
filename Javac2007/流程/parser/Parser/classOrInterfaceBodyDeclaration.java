    /** ClassBodyDeclaration =
     *      ";"
     *    | [STATIC] Block
     *    | ModifiersOpt
     *      **********************下面这6项是并列的**********************
     *      ( Type Ident
     *        ( VariableDeclaratorsRest ";" | MethodDeclaratorRest )
     *      | VOID Ident MethodDeclaratorRest
     *      | TypeParameters (Type | VOID) Ident MethodDeclaratorRest
     *      | Ident ConstructorDeclaratorRest
     *      | TypeParameters Ident ConstructorDeclaratorRest
     *      | ClassOrInterfaceOrEnumDeclaration
     *      )
     *      **********************上面这6项是并列的**********************
     *  InterfaceBodyDeclaration =
     *      ";"
     *    | ModifiersOpt Type Ident
     *      ( ConstantDeclaratorsRest | InterfaceMethodDeclaratorRest ";" )
     */
    List<JCTree> classOrInterfaceBodyDeclaration(Name className, boolean isInterface) {
    	try {//我加上的
    	DEBUG.P(this,"classOrInterfaceBodyDeclaration(2)");
 		DEBUG.P("S.token()="+S.token());

        if (S.token() == SEMI) {//这里不把他当成JCSkip，只有与类型声明(最顶层)并排的";"才是JCSkip
            S.nextToken();
            return List.<JCTree>of(F.at(Position.NOPOS).Block(0, List.<JCStatement>nil()));
        } else {
            String dc = S.docComment();
            int pos = S.pos();
            JCModifiers mods = modifiersOpt();
            
            //内部CLASS,INTERFACE,ENUM
            if (S.token() == CLASS ||
                S.token() == INTERFACE ||
				//如果用-source 1.4 -target 1.4编译内部enum类型，错误诊断位置会很乱
                allowEnums && S.token() == ENUM) {
                return List.<JCTree>of(classOrInterfaceOrEnumDeclaration(mods, dc));
				//语句块(包括static语句块(STATIC关键字在modifiersOpt()中已分析过))
            } else if (S.token() == LBRACE && !isInterface &&
                       (mods.flags & Flags.StandardFlags & ~Flags.STATIC) == 0 &&
                       mods.annotations.isEmpty()) {
                       //语句块前不能有注释,只能有static
                return List.<JCTree>of(block(pos, mods.flags));
            } else {
                pos = S.pos();
                //只有Method和Constructor之前才有TypeParameter
                List<JCTypeParameter> typarams = typeParametersOpt();
                DEBUG.P("mods.pos="+mods.pos);
                
                // Hack alert:  if there are type arguments(注：是typeParameters) but no Modifiers, the start
                // position will be lost unless we set the Modifiers position.  There
                // should be an AST node for type parameters (BugId 5005090).
                if (typarams.length() > 0 && mods.pos == Position.NOPOS) {
                    mods.pos = pos;
                }
                Token token = S.token();
                Name name = S.name();//构造方法(Constructor)的名称 或 字段类型名 或 方法的返回值的类型名
                pos = S.pos();
                JCExpression type;//字段的类型 或 方法的返回值的类型
                
                DEBUG.P("S.token()="+S.token());
                DEBUG.P("name="+name);
                
                boolean isVoid = S.token() == VOID;
                if (isVoid) {
                	//typetag为void的JCPrimitiveTypeTree
                    type = to(F.at(pos).TypeIdent(TypeTags.VOID));
                    S.nextToken(); 
                } else {
                    type = type();
                }
                //类的Constructor,如果是类的Constructor的名称，在term3()会生成JCTree.JCIdent
                if (S.token() == LPAREN && !isInterface && type.tag == JCTree.IDENT) {
                	
                	//isInterface这个条件完全可以去掉，因为通过前一个if语句后，
                	//isInterface的值肯定为false
                    if (isInterface || name != className)
                    	//构造方法(Constructor)的名称和类名不一样时
                    	//会报错，只是报错信息是:“方法声明无效；需要返回类型”
                        log.error(pos, "invalid.meth.decl.ret.type.req");
                    return List.of(methodDeclaratorRest(
                        pos, mods, null, names.init, typarams,
                        isInterface, true, dc));
                } else {
                    pos = S.pos();
                    name = ident(); //字段名或方法名，并读取下一个token

                    if (S.token() == LPAREN) { //方法
                        return List.of(methodDeclaratorRest(
                            pos, mods, type, name, typarams,
                            isInterface, isVoid, dc));
                    } else if (!isVoid && typarams.isEmpty()) { //字段名
						//在接口中定义的字段需要显示的初始化(isInterface=true)
                        List<JCTree> defs =
                            variableDeclaratorsRest(pos, mods, type, name, isInterface, dc,
                                                    new ListBuffer<JCTree>()).toList();
                        storeEnd(defs.last(), S.endPos());
                        accept(SEMI);
                        return defs;
                    } else {
                        pos = S.pos();
                        List<JCTree> err = isVoid
                            ? List.<JCTree>of(toP(F.at(pos).MethodDef(mods, name, type, typarams,
                                List.<JCVariableDecl>nil(), List.<JCExpression>nil(), null, null)))
                            : null;
                            
                        /*
                        如:
                        bin\mysrc\my\test\Test.java:32: 需要 '('
						        public <M extends T,S> int myInt='\uuuuu5df2';
						                                        ^
						1 错误
						*/
                        return List.<JCTree>of(syntaxError(S.pos(), err, "expected", keywords.token2string(LPAREN)));
                    }
                }
            }
        }
        
        }finally{//我加上的
		DEBUG.P(2,this,"classOrInterfaceBodyDeclaration(2)");
		}   
    }