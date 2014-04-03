    public JCNewArray NewArray(JCExpression elemtype,
			     List<JCExpression> dims,
			     List<JCExpression> elems)
    {
        JCNewArray tree = new JCNewArray(elemtype, dims, elems);
        tree.pos = pos;
        return tree;
    }