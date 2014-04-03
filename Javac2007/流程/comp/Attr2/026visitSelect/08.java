    /** If `sym' is a bad symbol: report error and return errSymbol
     *  else pass through unchanged,
     *  additional arguments duplicate what has been used in trying to find the
     *  symbol (--> flyweight pattern). This improves performance since we
     *  expect misses to happen frequently.
     *
     *  @param sym       The symbol that was found, or a ResolveError.
     *  @param pos       The position to use for error reporting.
     *  @param site      The original type from where the selection took place.
     *  @param name      The symbol's name.
     *  @param argtypes  The invocation's value arguments,
     *                   if we looked for a method.
     *  @param typeargtypes  The invocation's type arguments,
     *                   if we looked for a method.
     */
    Symbol access(Symbol sym,
                  DiagnosticPosition pos,
                  Type site,
                  Name name,
                  boolean qualified,
                  List<Type> argtypes,
                  List<Type> typeargtypes) {
        DEBUG.P(this,"access(7)");            	
        if (sym.kind >= AMBIGUOUS) {
//          printscopes(site.tsym.members());//DEBUG
            if (!site.isErroneous() &&
                !Type.isErroneous(argtypes) &&
                (typeargtypes==null || !Type.isErroneous(typeargtypes)))
                ((ResolveError)sym).report(log, pos, site, name, argtypes, typeargtypes);
            do {
                sym = ((ResolveError)sym).sym;
            } while (sym.kind >= AMBIGUOUS);
            if (sym == syms.errSymbol // preserve the symbol name through errors
                || ((sym.kind & ERRONEOUS) == 0 // make sure an error symbol is returned
                    && (sym.kind & TYP) != 0))
                sym = new ErrorType(name, qualified?site.tsym:syms.noSymbol).tsym;
        }
        DEBUG.P(0,this,"access(7)");     
        return sym;
    }

    /** Same as above, but without type arguments and arguments.
     */
    Symbol access(Symbol sym,
                  DiagnosticPosition pos,
                  Type site,
                  Name name,
                  boolean qualified) {
        try {
        DEBUG.P(this,"access(5)");   
        DEBUG.P("sym.name="+sym.name);
        //DEBUG.P("site="+site);
        DEBUG.P("site.tag="+TypeTags.toString(site.tag));
        if(site.tag==CLASS && site.tsym!=null) {
        	ClassSymbol myClassSymbol=(ClassSymbol)site.tsym;
        	DEBUG.P("site.tsym.members_field="+myClassSymbol.members_field);
        }
        DEBUG.P("name="+name);
        DEBUG.P("qualified="+qualified);
        DEBUG.P("sym.kind="+Kinds.toString(sym.kind));
        DEBUG.P("if (sym.kind >= AMBIGUOUS)="+(sym.kind >= AMBIGUOUS));
        
        //没发生ResolveErrors时直接返回sym
        if (sym.kind >= AMBIGUOUS)
            return access(sym, pos, site, name, qualified, List.<Type>nil(), null);
        else
            return sym;
        
        }finally{
        DEBUG.P(0,this,"access(5)");   
        }    
    }