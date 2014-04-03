    /** Check that a qualified name is in canonical form (for import decls).
     */
    public void checkCanonical(JCTree tree) {
		DEBUG.P(this,"checkCanonical(1)");
		DEBUG.P("tree="+tree);
		if (!isCanonical(tree))
			log.error(tree.pos(), "import.requires.canonical",
				  TreeInfo.symbol(tree));
		DEBUG.P(0,this,"checkCanonical(1)");
    }
        // where
	private boolean isCanonical(JCTree tree) {
	    while (tree.tag == JCTree.SELECT) {
			JCFieldAccess s = (JCFieldAccess) tree;
			if (s.sym.owner != TreeInfo.symbol(s.selected))
				return false;
			tree = s.selected;
	    }
	    return true;
	}