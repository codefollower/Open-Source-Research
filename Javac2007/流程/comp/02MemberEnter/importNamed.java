    /** Import given class.
     *  @param pos           Position to be used for error reporting.
     *  @param tsym          The class to be imported.
     *  @param env           The environment containing the named import
     *                  scope to add to.
     */
    private void importNamed(DiagnosticPosition pos, Symbol tsym, Env<AttrContext> env) {
    	DEBUG.P(this,"importNamed(3)");
        DEBUG.P("tsym="+tsym);
        DEBUG.P("env="+env);
        DEBUG.P("tsym.kind="+Kinds.toString(tsym.kind));
        DEBUG.P("env.toplevel.namedImportScope前="+env.toplevel.namedImportScope);
        

		//像这样，导入两个一样的类不会报错，会重复加入namedImportScope
		//import test.memberEnter.UniqueImport;
		//import test.memberEnter.UniqueImport;

        if (tsym.kind == TYP &&
            chk.checkUniqueImport(pos, tsym, env.toplevel.namedImportScope))
            env.toplevel.namedImportScope.enter(tsym, tsym.owner.members());
        
        DEBUG.P("env.toplevel.namedImportScope后="+env.toplevel.namedImportScope);
        DEBUG.P(0,this,"importNamed(3)");
    }