    public JCPrimitiveTypeTree TypeIdent(int typetag) {
        JCPrimitiveTypeTree tree = new JCPrimitiveTypeTree(typetag);
        tree.pos = pos;
        return tree;
    }