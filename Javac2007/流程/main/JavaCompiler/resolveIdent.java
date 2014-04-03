    /** Resolve an identifier.
     * @param name      The identifier to resolve
     */
    public Symbol resolveIdent(String name) {
    	try {//我加上的
		DEBUG.P(this,"resolveIdent(1)");
		DEBUG.P("name="+name);

        if (name.equals(""))
            return syms.errSymbol;
        JavaFileObject prev = log.useSource(null);
        try {
            JCExpression tree = null;
            for (String s : name.split("\\.", -1)) {
                if (!SourceVersion.isIdentifier(s)) // TODO: check for keywords
                    return syms.errSymbol;
                tree = (tree == null) ? make.Ident(names.fromString(s))
                                      : make.Select(tree, names.fromString(s));
            }
			DEBUG.P("tree="+tree);
            JCCompilationUnit toplevel =
                make.TopLevel(List.<JCTree.JCAnnotation>nil(), null, List.<JCTree>nil());
            toplevel.packge = syms.unnamedPackage;
            return attr.attribIdent(tree, toplevel);
        } finally {
            log.useSource(prev);
        }
        
        }finally{//我加上的
		DEBUG.P(0,this,"resolveIdent(1)");
		}
    }