    /** ClassOrInterfaceOrEnumDeclaration = ModifiersOpt
     *           (ClassDeclaration | InterfaceDeclaration | EnumDeclaration)
     *  @param mods     Any modifiers starting the class or interface declaration
     *  @param dc       The documentation comment for the class, or null.
     */
    JCStatement classOrInterfaceOrEnumDeclaration(JCModifiers mods, String dc) {
    	try {//我加上的
    	DEBUG.P(this,"classOrInterfaceOrEnumDeclaration(2)");
    	if(mods!=null) DEBUG.P("mods.flags="+Flags.toString(mods.flags));
    	else DEBUG.P("mods=null");
    	DEBUG.P("S.token()="+S.token()+"  dc="+dc);
    	
    	
        if (S.token() == CLASS) {
            return classDeclaration(mods, dc);
        } else if (S.token() == INTERFACE) {
			//这里同时包含接口声明和注释类型声明，
			//因为在modifiersOpt(mods)时，首先遇到@,
			//接着nextToken()后发现是INTERFACE，
			//给flags加上INTERFACE后退出modifiersOpt(mods)
            return interfaceDeclaration(mods, dc);
        } else if (allowEnums) {
            if (S.token() == ENUM) {
                return enumDeclaration(mods, dc);
            } else {
                int pos = S.pos();
                DEBUG.P("pos="+pos);
                List<JCTree> errs;
                if (S.token() == IDENTIFIER) {
                    errs = List.<JCTree>of(mods, toP(F.at(pos).Ident(ident())));
                    DEBUG.P("S.pos()="+S.pos());
                    //虽然在下面的syntaxError()内部也调用了setErrorEndPos()
                    //但因S.pos()>上面的int pos,所以错误结束位置是S.pos().
                    setErrorEndPos(S.pos());
                } else {
                    errs = List.<JCTree>of(mods);
                }
                //用JCExpressionStatement将JCErroneous“包装”起来
                return toP(F.Exec(syntaxError(pos, errs, "expected3",
                                              keywords.token2string(CLASS),
                                              keywords.token2string(INTERFACE),
                                              keywords.token2string(ENUM))));
            }
        } else {
            if (S.token() == ENUM) {
                log.error(S.pos(), "enums.not.supported.in.source", source.name);
                allowEnums = true;
                return enumDeclaration(mods, dc);
            }
            int pos = S.pos();
            List<JCTree> errs;
            if (S.token() == IDENTIFIER) {
                errs = List.<JCTree>of(mods, toP(F.at(pos).Ident(ident())));
                setErrorEndPos(S.pos());
            } else {
                errs = List.<JCTree>of(mods);
            }
            return toP(F.Exec(syntaxError(pos, errs, "expected2",
                                          keywords.token2string(CLASS),
                                          keywords.token2string(INTERFACE))));
        }
        
        
        }finally{//我加上的
        DEBUG.P(0,this,"classOrInterfaceOrEnumDeclaration(2)");
        }
    }