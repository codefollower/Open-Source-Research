    public JCLiteral Literal(int tag, Object value) {
        JCLiteral tree = new JCLiteral(tag, value);
        tree.pos = pos;
        return tree;
    }