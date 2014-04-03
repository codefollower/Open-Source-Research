    public void visitIf(JCIf tree) {
    	DEBUG.P(this,"visitIf(1)");
        attribExpr(tree.cond, env, syms.booleanType);
        attribStat(tree.thenpart, env);
        if (tree.elsepart != null)
            attribStat(tree.elsepart, env);
        chk.checkEmptyIf(tree);
        result = null;
        DEBUG.P(0,this,"visitIf(1)");
    }