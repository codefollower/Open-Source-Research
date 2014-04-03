/* -------- source positions ------- */

    private int errorEndPos = -1;

    private void setErrorEndPos(int errPos) {
	    DEBUG.P(this,"setErrorEndPos(1)");
	    DEBUG.P("errPos="+errPos);
	    DEBUG.P("errorEndPos="+errorEndPos);

        if (errPos > errorEndPos)
            errorEndPos = errPos;

		DEBUG.P(0,this,"setErrorEndPos(1)");
    }

    protected int getErrorEndPos() {
        return errorEndPos;
    }

    /**
     * Store ending position for a tree.
     * @param tree   The tree.
     * @param endpos The ending position to associate with the tree.
     */
    protected void storeEnd(JCTree tree, int endpos) {}

    /**
     * Store ending position for a tree.  The ending position should
     * be the ending position of the current token.
     * @param t The tree.
     */
    protected <T extends JCTree> T to(T t) { return t; }

    /**
     * Store ending position for a tree.  The ending position should
     * be greater of the ending position of the previous token and errorEndPos.
     * @param t The tree.
     */
    protected <T extends JCTree> T toP(T t) { return t; }

    /** Get the start position for a tree node.  The start position is
     * defined to be the position of the first character of the first
     * token of the node's source text.
     * @param tree  The tree node
     */
    public int getStartPos(JCTree tree) {
        return TreeInfo.getStartPos(tree);
    }

    /**
     * Get the end position for a tree node.  The end position is
     * defined to be the position of the last character of the last
     * token of the node's source text.  Returns Position.NOPOS if end
     * positions are not generated or the position is otherwise not
     * found.
     * @param tree  The tree node
     */
    public int getEndPos(JCTree tree) {
        return Position.NOPOS;
    }


    /** A hashtable to store ending positions
     *  of source ranges indexed by the tree nodes.
     *  Defined only if option flag genEndPos is set.
     */
    Map<JCTree, Integer> endPositions;

    /** {@inheritDoc} */
    @Override
    protected void storeEnd(JCTree tree, int endpos) {
	    DEBUG.P(this,"storeEnd(2)");
        
        int errorEndPos = getErrorEndPos();

		DEBUG.P("endpos="+endpos);
		DEBUG.P("errorEndPos="+errorEndPos);

		endPositions.put(tree, errorEndPos > endpos ? errorEndPos : endpos);

		DEBUG.P(0,this,"storeEnd(2)");
    }

    /** {@inheritDoc} */
    @Override
    protected <T extends JCTree> T to(T t) {
		storeEnd(t, S.endPos());
		return t;
    }

    /** {@inheritDoc} */
    @Override
    protected <T extends JCTree> T toP(T t) {
		storeEnd(t, S.prevEndPos());
		return t;
    }


	/** {@inheritDoc} */
    @Override
    public int getEndPos(JCTree tree) {
        return TreeInfo.getEndPos(tree, endPositions);
    }




