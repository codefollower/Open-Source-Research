    /** TypeParametersOpt = ["<" TypeParameter {"," TypeParameter} ">"]
     */
    List<JCTypeParameter> typeParametersOpt() {
    	try {//我加上的
    	DEBUG.P(this,"typeParametersOpt()");
    	
        if (S.token() == LT) {
            checkGenerics();
            ListBuffer<JCTypeParameter> typarams = new ListBuffer<JCTypeParameter>();
            S.nextToken();
            typarams.append(typeParameter());
            while (S.token() == COMMA) {
                S.nextToken();
                typarams.append(typeParameter());
            }
            accept(GT);
            return typarams.toList();
        } else {
            return List.nil();
        }
        
        }finally{//我加上的
		DEBUG.P(0,this,"typeParametersOpt()");
		}
    }
    
    /*注意TypeParameter和TypeArgument的差别
     *	TypeArgument = Type
     *               | "?"
     *               | "?" EXTENDS Type
     *               | "?" SUPER Type
    
    对比方法参数的形参与实参来理解TypeParameter和TypeArgument
    */
    
    /** TypeParameter = TypeVariable [TypeParameterBound]
     *  TypeParameterBound = EXTENDS Type {"&" Type}
     *  TypeVariable = Ident
     */
    JCTypeParameter typeParameter() {
    	try {//我加上的
    	DEBUG.P(this,"typeParameter()");
    	
        int pos = S.pos();
        Name name = ident();
        ListBuffer<JCExpression> bounds = new ListBuffer<JCExpression>();
        if (S.token() == EXTENDS) {
            S.nextToken();
            bounds.append(type());
            while (S.token() == AMP) {
                S.nextToken();
                bounds.append(type());
            }
        }
		//如果只是<T>，那么bounds.toList()是一个new List<JCExpression>(null,null)
        return toP(F.at(pos).TypeParameter(name, bounds.toList()));
        
        }finally{//我加上的
		DEBUG.P(0,this,"typeParameter()");
		}
    }