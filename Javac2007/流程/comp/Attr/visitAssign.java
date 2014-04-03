    public void visitAssign(JCAssign tree) {
        Type owntype = attribTree(tree.lhs, env.dup(tree), VAR, Type.noType);
        Type capturedType = capture(owntype);
        attribExpr(tree.rhs, env, owntype);
        result = check(tree, capturedType, VAL, pkind, pt);
    }