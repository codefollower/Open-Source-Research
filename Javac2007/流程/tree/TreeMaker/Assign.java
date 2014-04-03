    public JCAssign Assign(JCExpression lhs, JCExpression rhs) {
        JCAssign tree = new JCAssign(lhs, rhs);
        tree.pos = pos;
        return tree;
    }