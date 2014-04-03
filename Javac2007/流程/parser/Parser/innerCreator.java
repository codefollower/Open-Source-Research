    /** InnerCreator = Ident [TypeArguments] ClassCreatorRest
     */
    JCExpression innerCreator(int newpos, List<JCExpression> typeArgs, JCExpression encl) {
        try {//我加上的
		DEBUG.P(this,"innerCreator(3)");
		DEBUG.P("typeArgs="+typeArgs);
		DEBUG.P("encl="+encl);
		
        JCExpression t = toP(F.at(S.pos()).Ident(ident()));
        if (S.token() == LT) {
            checkGenerics();
            t = typeArguments(t);
        }
        return classCreatorRest(newpos, encl, typeArgs, t);
        
        }finally{//我加上的
		DEBUG.P(0,this,"innerCreator(3)");
		}
    }