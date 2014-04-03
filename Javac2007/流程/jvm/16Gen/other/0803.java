    /** The currently enclosing outermost class definition.
     */
    JCClassDecl outermostClassDef;

    /** The currently enclosing outermost member definition.
     */
    JCTree outermostMemberDef;

    /** A navigator class for assembling a mapping from local class symbols
     *  to class definition trees.
     *  There is only one case; all other cases simply traverse down the tree.
     */
    class ClassMap extends TreeScanner {

	/** All encountered class defs are entered into classdefs table.
	 */
	public void visitClassDef(JCClassDecl tree) {
	    classdefs.put(tree.sym, tree);
	    super.visitClassDef(tree);
	}
    }
    ClassMap classMap = new ClassMap();