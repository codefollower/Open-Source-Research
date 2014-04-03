    /** MethodDeclaratorRest =
     *      FormalParameters BracketsOpt [Throws TypeList] ( MethodBody | [DEFAULT AnnotationValue] ";")
     *  VoidMethodDeclaratorRest =
     *      FormalParameters [Throws TypeList] ( MethodBody | ";")
     *  InterfaceMethodDeclaratorRest =
     *      FormalParameters BracketsOpt [THROWS TypeList] ";"
     *  VoidInterfaceMethodDeclaratorRest =
     *      FormalParameters [THROWS TypeList] ";"
     *  ConstructorDeclaratorRest =
     *      "(" FormalParameterListOpt ")" [THROWS TypeList] MethodBody
     */
    JCTree methodDeclaratorRest(int pos,
                              JCModifiers mods,
                              JCExpression type,
                              Name name,
                              List<JCTypeParameter> typarams,
                              boolean isInterface, boolean isVoid,
                              String dc) {    
        DEBUG.P(this,"methodDeclaratorRest(6)");
        DEBUG.P("!isVoid="+!isVoid);          
        List<JCVariableDecl> params = formalParameters();//这是方法的参数
        if(params!=null) DEBUG.P("params.size="+params.size());  
        
        
        /*
        类似下面的语法也可以(返回值是数组的话,[]可以放在右括号')'后面):
	    public int myMethod()[] {
			return new int[0];
		}
		*/
        if (!isVoid) type = bracketsOpt(type);
        
        
         
        List<JCExpression> thrown = List.nil();
        if (S.token() == THROWS) {
            S.nextToken();
            thrown = qualidentList();
        }
        JCBlock body = null;
        JCExpression defaultValue;
        //DEBUG.P("S.token() ="+S.token());
        
	//如果接口中的方法有方法体并不在语法分析时检查
	//interface MemberInterfaceB {
	//	void methodA(){};
	//}
        if (S.token() == LBRACE) {
            body = block();
            defaultValue = null;
        } else {
        	/*
        	注释类型定义中的"default"
        	如jdk1.6.0docs/technotes/guides/language/annotations.html的例子:
        	public @interface RequestForEnhancement {
			    int    id();
			    String synopsis();
			    String engineer() default "[unassigned]"; 
			    String date()    default "[unimplemented]"; 
			}
			*/
            if (S.token() == DEFAULT) {
                accept(DEFAULT);
                defaultValue = annotationValue();
            } else {
                defaultValue = null;
            }
            accept(SEMI);
            if (S.pos() <= errorEndPos) {
                // error recovery
                skip(false, true, false, false);
                if (S.token() == LBRACE) {
                    body = block();
                }
            }
        }
        JCMethodDecl result =
            toP(F.at(pos).MethodDef(mods, name, type, typarams,
                                    params, thrown,
                                    body, defaultValue));
        DEBUG.P(2,this,"methodDeclaratorRest(6)");                            
        attach(result, dc);
        return result;
    }