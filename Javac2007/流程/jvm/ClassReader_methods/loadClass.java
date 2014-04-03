    /** Load a toplevel class with given fully qualified name
     *  The class is entered into `classes' only if load was successful.
     */
    public ClassSymbol loadClass(Name flatname) throws CompletionFailure {
        try {
        DEBUG.on();
        DEBUG.P(this,"loadClass(Name flatname)");
        DEBUG.P("flatname="+flatname);
			

        boolean absent = classes.get(flatname) == null;
        DEBUG.P("absent="+absent);
		//DEBUG.off();
        ClassSymbol c = enterClass(flatname);
        if (c.members_field == null && c.completer != null) {
            try {
                c.complete();
            } catch (CompletionFailure ex) {
				DEBUG.P("absent="+absent);
				DEBUG.P("ex="+ex);
                if (absent) classes.remove(flatname);
                throw ex;
            }
        }
        
        return c;
        
        }finally{
		//DEBUG.on();
        DEBUG.P(0,this,"loadClass(Name flatname)");
        DEBUG.off();
        }
    }