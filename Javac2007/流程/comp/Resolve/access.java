/* ***************************************************************************
 *  Access checking
 *  The following methods convert ResolveErrors to ErrorSymbols, issuing
 *  an error message in the process
 ****************************************************************************/

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
            
            DEBUG.P("sym.name1="+sym.name);
            
            do {
                sym = ((ResolveError)sym).sym;

				DEBUG.P("sym.kind="+Kinds.toString(sym.kind));
            } while (sym.kind >= AMBIGUOUS);
            
            DEBUG.P("sym.name2="+sym.name);
			DEBUG.P("sym2="+sym);
            
			//||号前部份对应ResolveError，后面部分对应ResolveError的三个子类
            if (sym == syms.errSymbol // preserve the symbol name through errors
                || ((sym.kind & ERRONEOUS) == 0 // make sure an error symbol is returned
                    && (sym.kind & TYP) != 0))
                sym = new ErrorType(name, qualified?site.tsym:syms.noSymbol).tsym;
        }
		DEBUG.P("sym="+sym);
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
		/*
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
		*/
        
		DEBUG.P("sym="+sym.name+" kind="+Kinds.toString(sym.kind)+" >=AMBIGUOUS="+(sym.kind >= AMBIGUOUS));
        //没发生ResolveErrors时直接返回sym
        if (sym.kind >= AMBIGUOUS)
            return access(sym, pos, site, name, qualified, List.<Type>nil(), null);
        else
            return sym;
        
        }finally{
        DEBUG.P(0,this,"access(5)");   
        }    
    }

    /** Check that sym is not an abstract method.
     */
    void checkNonAbstract(DiagnosticPosition pos, Symbol sym) {
        if ((sym.flags() & ABSTRACT) != 0)
            log.error(pos, "abstract.cant.be.accessed.directly",
                      kindName(sym), sym, sym.location());
    }

/* ***************************************************************************
 *  Debugging
 ****************************************************************************/

    /** print all scopes starting with scope s and proceeding outwards.
     *  used for debugging.
     */
    public void printscopes(Scope s) {
        while (s != null) {
            if (s.owner != null)
                System.err.print(s.owner + ": ");
            for (Scope.Entry e = s.elems; e != null; e = e.sibling) {
                if ((e.sym.flags() & ABSTRACT) != 0)
                    System.err.print("abstract ");
                System.err.print(e.sym + " ");
            }
            System.err.println();
            s = s.next;
        }
    }

    void printscopes(Env<AttrContext> env) {
        while (env.outer != null) {
            System.err.println("------------------------------");
            printscopes(env.info.scope);
            env = env.outer;
        }
    }

    public void printscopes(Type t) {
        while (t.tag == CLASS) {
            printscopes(t.tsym.members());
            t = types.supertype(t);
        }
    }