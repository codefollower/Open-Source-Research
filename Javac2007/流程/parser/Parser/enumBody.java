    /** EnumBody = "{" { EnumeratorDeclarationList } [","]
     *                  [ ";" {ClassBodyDeclaration} ] "}"
     */
    List<JCTree> enumBody(Name enumName) {
    	DEBUG.P(this,"enumBody(Name enumName)");
        accept(LBRACE);
        ListBuffer<JCTree> defs = new ListBuffer<JCTree>();
        if (S.token() == COMMA) {
            S.nextToken();
        } else if (S.token() != RBRACE && S.token() != SEMI) {
            defs.append(enumeratorDeclaration(enumName));
            while (S.token() == COMMA) {
                S.nextToken();
                if (S.token() == RBRACE || S.token() == SEMI) break;
                defs.append(enumeratorDeclaration(enumName));
            }
            if (S.token() != SEMI && S.token() != RBRACE) {
                defs.append(syntaxError(S.pos(), "expected3",
                                keywords.token2string(COMMA),
                                keywords.token2string(RBRACE),
                                keywords.token2string(SEMI)));
                S.nextToken();
            }
        }
        if (S.token() == SEMI) {
            S.nextToken();
            while (S.token() != RBRACE && S.token() != EOF) {
                defs.appendList(classOrInterfaceBodyDeclaration(enumName,
                                                                false));
                if (S.pos() <= errorEndPos) {
                    // error recovery
                   skip(false, true, true, false);
                }
            }
        }
        accept(RBRACE);
        DEBUG.P(0,this,"enumBody(Name enumName)");
        return defs.toList();
    }