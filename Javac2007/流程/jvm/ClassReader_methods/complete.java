    private boolean suppressFlush = false;

    /** Completion for classes to be loaded. Before a class is loaded
     *  we make sure its enclosing class (if any) is loaded.
     */
    //complete(Symbol sym)这个方法最终的主要功能就是
    //对ClassSymbol或PackageSymbol的members_field赋值
    public void complete(Symbol sym) throws CompletionFailure {
    	DEBUG.P(this,"complete(1)");
    	DEBUG.P("SymbolKind="+Kinds.toString(sym.kind));
        DEBUG.P("SymbolName="+sym);
        DEBUG.P("filling="+filling+" suppressFlush="+suppressFlush);
        //注:sym.kind的值是在com.sun.tools.javac.code.Kinds类中定义
        if (sym.kind == TYP) {
            ClassSymbol c = (ClassSymbol)sym;
            c.members_field = new Scope.ErrorScope(c); // make sure it's always defined
            boolean suppressFlush = this.suppressFlush;
            this.suppressFlush = true;
            try {
				DEBUG.P("c.owner="+c.owner);
                completeOwners(c.owner);
                completeEnclosing(c);
            } finally {
                this.suppressFlush = suppressFlush;
            }
            fillIn(c);
        } else if (sym.kind == PCK) {
            PackageSymbol p = (PackageSymbol)sym;
            try {
                fillIn(p);
            } catch (IOException ex) {
                throw new CompletionFailure(sym, ex.getLocalizedMessage()).initCause(ex);
            }
        }
        
        DEBUG.P("filling="+filling+" suppressFlush="+suppressFlush);
        if (!filling && !suppressFlush)
            annotate.flush(); // finish attaching annotations
       	DEBUG.P(2,this,"complete(1)");
    }