/* ********************************************************************
 * Type completion
 *********************************************************************/

    Type attribImportType(JCTree tree, Env<AttrContext> env) {
        assert completionEnabled;
        try {
            DEBUG.P(this,"attribImportType(JCTree tree, Env<AttrContext> env)");
            DEBUG.P("tree="+tree);
            DEBUG.P("env="+env);
            // To prevent deep recursion, suppress completion of some
            // types.
            completionEnabled = false;
            //由import my.StaticImportTest.MyInnerClass;构成的JCFieldAccess树
            //JCFieldAccess树里每一个selector的sym在attribType后都不为null
            return attr.attribType(tree, env);
        } finally {
            DEBUG.P(0,this,"attribImportType(JCTree tree, Env<AttrContext> env)");
            completionEnabled = true;
        }
    }