    /** Enter members for a class.
     */
    void finishClass(JCClassDecl tree, Env<AttrContext> env) {
    	DEBUG.P(this,"finishClass(2)");
    	DEBUG.P("tree="+tree);
    	DEBUG.P("env="+env);
    	
        if ((tree.mods.flags & Flags.ENUM) != 0 &&
            (types.supertype(tree.sym.type).tsym.flags() & Flags.ENUM) == 0) {
            addEnumMembers(tree, env);
        }
        memberEnter(tree.defs, env);
        
        DEBUG.P(0,this,"finishClass(2)");
    }