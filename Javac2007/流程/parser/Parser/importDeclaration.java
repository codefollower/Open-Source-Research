    /** ImportDeclaration = IMPORT [ STATIC ] Ident { "." Ident } [ "." "*" ] ";"
     */
    JCTree importDeclaration() {
    	DEBUG.P(this,"importDeclaration()");
        int pos = S.pos();//这个一定是import这个token的开始位置
		DEBUG.P("pos="+pos);
        S.nextToken();
        boolean importStatic = false;
        if (S.token() == STATIC) {
            checkStaticImports();
            importStatic = true;
            S.nextToken();
        }

		//如果是“import my.test;”，那么这里得到的pid的开始位置是my这个token的pos
		//pid的结束位置是my这个token的endpos，
		//对应nextToken(157,159)=|my|中的(157,159)
        JCExpression pid = toP(F.at(S.pos()).Ident(ident()));
        do {
            int pos1 = S.pos();
            accept(DOT);
            if (S.token() == STAR) {
                pid = to(F.at(pos1).Select(pid, names.asterisk));//导入“.*"的情况
                S.nextToken();
                break;
            } else {
				DEBUG.P("pos1="+pos1);
				//如果是“import my.test;”，那么这里得到的pid是一个JCFieldAccess
				//它的开始位置是“.”的pos，结束位置是test这个token的endpos
                pid = toP(F.at(pos1).Select(pid, ident()));
            }
        } while (S.token() == DOT);
        accept(SEMI);
        DEBUG.P(2,this,"importDeclaration()");
		//如果是“import my.test;”，那么这里得到的pid是一个JCImport
		//它的开始位置是“import”的pos，结束位置是";"这个token的endpos
        return toP(F.at(pos).Import(pid, importStatic));
    }