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
        
        
        if (sym.kind >= AMBIGUOUS)
            return access(sym, pos, site, name, qualified, List.<Type>nil(), null);
        else
            return sym;
        
        }finally{
        DEBUG.P(this,"access(5)");   
        }    
    }