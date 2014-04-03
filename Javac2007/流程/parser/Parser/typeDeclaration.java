    /** TypeDeclaration = ClassOrInterfaceOrEnumDeclaration
     *                  | ";"
     */
    JCTree typeDeclaration(JCModifiers mods) {
        try {//我加上的
        DEBUG.P(this,"typeDeclaration(1)");
        if(mods!=null) DEBUG.P("mods.flags="+Flags.toString(mods.flags));
        else DEBUG.P("mods=null");
        DEBUG.P("S.token()="+S.token()+"  S.pos()="+S.pos());

        int pos = S.pos();

		//单独的“;"号前面不能有修饰符
        if (mods == null && S.token() == SEMI) {
            S.nextToken();
            return toP(F.at(pos).Skip());
        } else {
            String dc = S.docComment();
			DEBUG.P("dc="+dc);
            return classOrInterfaceOrEnumDeclaration(modifiersOpt(mods), dc);
        }


        }finally{//我加上的
        DEBUG.P(2,this,"typeDeclaration(1)");
        }
    }