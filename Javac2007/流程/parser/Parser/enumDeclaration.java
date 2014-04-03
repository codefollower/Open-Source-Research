    /** EnumDeclaration = ENUM Ident [IMPLEMENTS TypeList] EnumBody
     *  @param mods    The modifiers starting the enum declaration
     *  @param dc       The documentation comment for the enum, or null.
     */
    JCClassDecl enumDeclaration(JCModifiers mods, String dc) {
    	DEBUG.P(this,"enumDeclaration(2)");
    	DEBUG.P("startPos="+S.pos());
        int pos = S.pos();
        accept(ENUM);
        Name name = ident();

        List<JCExpression> implementing = List.nil();
        if (S.token() == IMPLEMENTS) {
            S.nextToken();
            implementing = typeList();
        }

        List<JCTree> defs = enumBody(name);
        JCModifiers newMods = //在modifiersOpt()已加Flags.ENUM
            F.at(mods.pos).Modifiers(mods.flags|Flags.ENUM, mods.annotations);
        //枚举类没有TypeParameters也没有EXTENDS TypeList
        JCClassDecl result = toP(F.at(pos).
            ClassDef(newMods, name, List.<JCTypeParameter>nil(),
                null, implementing, defs));
        attach(result, dc);
        DEBUG.P(2,this,"enumDeclaration(2)");
        return result;
    }