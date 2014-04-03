	/*
	<T extends ListBuffer<? super JCVariableDecl>> T vdefs怎样理解?
	意思是:传给“T vdefs”的“type argument”的类型必须是ListBuffer及其子类,
	并且ListBuffer及其子类的“parameterized type”又是JCVariableDecl或其超类。
	
	例子参考forInit()方法中的如下代码片断:
	ListBuffer<JCStatement> stats......
	variableDeclarators(......, stats)

	其中“type argument”指的是stats，它是指向ListBuffer<JCStatement>类实例的引用，
	ListBuffer的“parameterized type”指的是JCStatement，而JCStatement
	又是JCVariableDecl的超类。
	*/

    /** VariableDeclarators = VariableDeclarator { "," VariableDeclarator }
     */
    public <T extends ListBuffer<? super JCVariableDecl>> T variableDeclarators(JCModifiers mods,
                                                                         JCExpression type,
                                                                         T vdefs)
    {
    	try {//我加上的
		DEBUG.P(this,"variableDeclarators(3)");
		
        return variableDeclaratorsRest(S.pos(), mods, type, ident(), false, null, vdefs);

        }finally{//我加上的
		DEBUG.P(0,this,"variableDeclarators(3)");
		}         
    }

    /** VariableDeclaratorsRest = VariableDeclaratorRest { "," VariableDeclarator }
     *  ConstantDeclaratorsRest = ConstantDeclaratorRest { "," ConstantDeclarator }
     *
     *  @param reqInit  Is an initializer always required?
     *  @param dc       The documentation comment for the variable declarations, or null.
     */
    <T extends ListBuffer<? super JCVariableDecl>> T variableDeclaratorsRest(int pos,
                                                                     JCModifiers mods,
                                                                     JCExpression type,
                                                                     Name name,
                                                                     boolean reqInit,
                                                                     String dc,
                                                                     T vdefs) {
    	try {//我加上的
		DEBUG.P(this,"variableDeclaratorsRest(7)");
		
        vdefs.append(variableDeclaratorRest(pos, mods, type, name, reqInit, dc));
        while (S.token() == COMMA) {
            // All but last of multiple declarators subsume a comma
			DEBUG.P("S.endPos()="+S.endPos());
            storeEnd((JCTree)vdefs.elems.last(), S.endPos());
            S.nextToken();
            vdefs.append(variableDeclarator(mods, type, reqInit, dc));
        }
        return vdefs;
        
        }finally{//我加上的
		DEBUG.P(0,this,"variableDeclaratorsRest(7)");
		}          
    }

    /** VariableDeclarator = Ident VariableDeclaratorRest
     *  ConstantDeclarator = Ident ConstantDeclaratorRest
     */
    JCVariableDecl variableDeclarator(JCModifiers mods, JCExpression type, boolean reqInit, String dc) {
        try {//我加上的
		DEBUG.P(this,"variableDeclarator(4)");
		
        return variableDeclaratorRest(S.pos(), mods, type, ident(), reqInit, dc);
       
        }finally{//我加上的
		DEBUG.P(0,this,"variableDeclarator(4)");
		}      
    }

    /** VariableDeclaratorRest = BracketsOpt ["=" VariableInitializer]
     *  ConstantDeclaratorRest = BracketsOpt "=" VariableInitializer
     *
     *  @param reqInit  Is an initializer always required?
     *  @param dc       The documentation comment for the variable declarations, or null.
     */
    JCVariableDecl variableDeclaratorRest(int pos, JCModifiers mods, JCExpression type, Name name,
                                  boolean reqInit, String dc) {
        try {//我加上的
		DEBUG.P(this,"variableDeclaratorRest(6)");
		DEBUG.P("pos="+pos);
		DEBUG.P("mods="+mods);
		DEBUG.P("type="+type);
		DEBUG.P("name="+name);
		//接口中定义的成员变量需要初始化
		//reqInit有时等于isInterface的值
		DEBUG.P("reqInit="+reqInit);
		DEBUG.P("dc="+dc);
		
        type = bracketsOpt(type); //例如:String s1[]
        JCExpression init = null;
        if (S.token() == EQ) {
            S.nextToken();
            init = variableInitializer();
        }
        else if (reqInit) syntaxError(S.pos(), "expected", keywords.token2string(EQ));
        //对于接口中定义的成员变量，如果没有指定修饰符，
        //在Parser阶断也不会自动加上
        //DEBUG.P("mods="+mods);
        JCVariableDecl result =
            toP(F.at(pos).VarDef(mods, name, type, init));
        attach(result, dc);
        return result;

        }finally{//我加上的
		DEBUG.P(0,this,"variableDeclaratorRest(6)");
		}       
    }

    /** VariableDeclaratorId = Ident BracketsOpt
     */
    JCVariableDecl variableDeclaratorId(JCModifiers mods, JCExpression type) {
    	try {//我加上的
        DEBUG.P(this,"variableDeclaratorId(2)");
		
        int pos = S.pos();
        Name name = ident();
        if ((mods.flags & Flags.VARARGS) == 0)
		//mothodName(N[] n[],S s)这种语法也不会报错
		//mothodName(N... n[],S s)这种语法就会报错
		//mothodName(N[8] n[9],S s)这种语法也会报错，
		//因为方法参数中的数组类型参数是不能指定数组大小的
            type = bracketsOpt(type);
        //方法形参没有初始化部分，所以VarDef方法的第4个参数为null
        return toP(F.at(pos).VarDef(mods, name, type, null));

        }finally{//我加上的
        DEBUG.P(0,this,"variableDeclaratorId(2)");
        }  
    }