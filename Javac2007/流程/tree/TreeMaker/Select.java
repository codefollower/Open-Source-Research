    public JCFieldAccess Select(JCExpression selected, Name selector) {
        JCFieldAccess tree = new JCFieldAccess(selected, selector, null);
        tree.pos = pos;
        return tree;
    }