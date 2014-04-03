    /** Map a class symbol to its definition.
     *  @param c    The class symbol of which we want to determine the definition.
     */
    JCClassDecl classDef(ClassSymbol c) {
	DEBUG.P(this,"classDef(1)");
	DEBUG.P("c="+c);

	// First lookup the class in the classdefs table.
	JCClassDecl def = classdefs.get(c);

	DEBUG.P("(def == null && outermostMemberDef != null)="+(def == null && outermostMemberDef != null));
	if (def == null && outermostMemberDef != null) {
	    // If this fails, traverse outermost member definition, entering all
	    // local classes into classdefs, and try again.
	    classMap.scan(outermostMemberDef);
	    def = classdefs.get(c);
	}
	DEBUG.P("(def == null)="+(def == null));
	if (def == null) {
	    // If this fails, traverse outermost class definition, entering all
	    // local classes into classdefs, and try again.
	    classMap.scan(outermostClassDef);
	    def = classdefs.get(c);
	}

	DEBUG.P(1,this,"classDef(1)");
	return def;
    }