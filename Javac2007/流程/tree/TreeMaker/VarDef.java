    public JCVariableDecl VarDef(JCModifiers mods, Name name, JCExpression vartype, JCExpression init) {
        JCVariableDecl tree = new JCVariableDecl(mods, name, vartype, init, null);
        tree.pos = pos;
        return tree;
    }