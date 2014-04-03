    /** ClassCreatorRest = Arguments [ClassBody]
     */
    JCExpression classCreatorRest(int newpos,
                                  JCExpression encl,
                                  List<JCExpression> typeArgs,
                                  JCExpression t)
    {
    	try {//我加上的
		DEBUG.P(this,"classCreatorRest(4)");
		DEBUG.P("encl="+encl);
		DEBUG.P("typeArgs="+typeArgs);
		DEBUG.P("t="+t);
		
        List<JCExpression> args = arguments();
        JCClassDecl body = null;
        if (S.token() == LBRACE) {
            int pos = S.pos();
            List<JCTree> defs = classOrInterfaceBody(names.empty, false);
            JCModifiers mods = F.at(Position.NOPOS).Modifiers(0);
            body = toP(F.at(pos).AnonymousClassDef(mods, defs));
        }
        return toP(F.at(newpos).NewClass(encl, typeArgs, t, args, body));
        
        }finally{//我加上的
		DEBUG.P(0,this,"classCreatorRest(4)");
		}
    }