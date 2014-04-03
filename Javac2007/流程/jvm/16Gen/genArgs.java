    /** Derived visitor method: generate code for a list of method arguments.
     *  @param trees    The argument expressions to be visited.
     *  @param pts      The expression's expected types (i.e. the formal parameter
     *                  types of the invoked method).
     */
    public void genArgs(List<JCExpression> trees, List<Type> pts) {
		DEBUG.P(this,"genArgs(2)");
		DEBUG.P("trees="+trees);
		DEBUG.P("pts="+pts);
		for (List<JCExpression> l = trees; l.nonEmpty(); l = l.tail) {
			genExpr(l.head, pts.head).load();
			pts = pts.tail;
		}
		// require lists be of same length
		assert pts.isEmpty();
		DEBUG.P(0,this,"genArgs(2)");
    }