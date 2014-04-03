    /**
     * Create given tree node at current position.
     * @param defs a list of ClassDef, Import, and Skip
     */
    public JCCompilationUnit TopLevel(List<JCAnnotation> packageAnnotations,
				      JCExpression pid,
				      List<JCTree> defs) {
        assert packageAnnotations != null;
        for (JCTree node : defs)
            assert node instanceof JCClassDecl
		|| node instanceof JCImport
		|| node instanceof JCSkip
                || node instanceof JCErroneous
		|| (node instanceof JCExpressionStatement
		    && ((JCExpressionStatement)node).expr instanceof JCErroneous)
                 : node.getClass().getSimpleName();
        JCCompilationUnit tree = new JCCompilationUnit(packageAnnotations, pid, defs,
                                     null, null, null, null);
        tree.pos = pos;
        return tree;
    }
