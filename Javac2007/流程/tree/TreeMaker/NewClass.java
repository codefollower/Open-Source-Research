    public JCNewClass NewClass(JCExpression encl,
                             List<JCExpression> typeargs,
                             JCExpression clazz,
                             List<JCExpression> args,
                             JCClassDecl def)
    {
        JCNewClass tree = new JCNewClass(encl, typeargs, clazz, args, def);
        tree.pos = pos;
        return tree;
    }