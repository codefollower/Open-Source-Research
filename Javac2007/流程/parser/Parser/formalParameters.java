    /** FormalParameters = "(" [ FormalParameterList ] ")"
     *  FormalParameterList = [ FormalParameterListNovarargs , ] LastFormalParameter
     *  FormalParameterListNovarargs = [ FormalParameterListNovarargs , ] FormalParameter
     */
    List<JCVariableDecl> formalParameters() { //指在一个方法的括号中声明的参数
    	try {//我加上的
    	DEBUG.P(this,"formalParameters()");
    	
        ListBuffer<JCVariableDecl> params = new ListBuffer<JCVariableDecl>();
        JCVariableDecl lastParam = null;
        accept(LPAREN);
        DEBUG.P("S.token()="+S.token());
        if (S.token() != RPAREN) {
            params.append(lastParam = formalParameter());
            //Vararrgs参数存在的话，总是方法的括号中声明的参数的最后一个
            while ((lastParam.mods.flags & Flags.VARARGS) == 0 && S.token() == COMMA) {
                S.nextToken();
                params.append(lastParam = formalParameter());
            }
        }
        accept(RPAREN);
        return params.toList();
        
        }finally{//我加上的
		DEBUG.P(0,this,"formalParameters()");
		}
    }

    JCModifiers optFinal(long flags) {
    	try {//我加上的
    	DEBUG.P(this,"optFinal(long flags)");
    	DEBUG.P("flags="+Flags.toString(flags));
    	
        JCModifiers mods = modifiersOpt();
        
        DEBUG.P("mods.flags="+Flags.toString(mods.flags));
        
		//方法括号中的参数只能是final与deprecated(在JAVADOC)中指定
		//ParserTest(/** @deprecated */ final int i){}

		//注意下面两句的编译结果是不一样的
		//ParserTest(final /** @deprecated */ int i){} //有错
		//ParserTest(/** @deprecated */ final int i){} //无错
		//因为在modifiersOpt()中先看是否有DEPRECATED再进入while循环，
		//当final在先，进入while循环nextToken后忘了分析是否有DEPRECATED了
        checkNoMods(mods.flags & ~(Flags.FINAL | Flags.DEPRECATED));
        mods.flags |= flags;
        
        DEBUG.P("mods.flags="+Flags.toString(mods.flags));
        return mods;
        
        }finally{//我加上的
		DEBUG.P(0,this,"optFinal(long flags)");
		} 
    }

    /** FormalParameter = { FINAL | '@' Annotation } Type VariableDeclaratorId
     *  LastFormalParameter = { FINAL | '@' Annotation } Type '...' Ident | FormalParameter
     */
    JCVariableDecl formalParameter() {
    	try {//我加上的
    	DEBUG.P(this,"formalParameter()");
    	
        JCModifiers mods = optFinal(Flags.PARAMETER);
        JCExpression type = type();
        if (S.token() == ELLIPSIS) { //最后一个形参是varargs的情况
            checkVarargs();
            mods.flags |= Flags.VARARGS;
            type = to(F.at(S.pos()).TypeArray(type));
            S.nextToken();
        }
        return variableDeclaratorId(mods, type);
        
        }finally{//我加上的
		DEBUG.P(0,this,"formalParameter()");
		}        
    }