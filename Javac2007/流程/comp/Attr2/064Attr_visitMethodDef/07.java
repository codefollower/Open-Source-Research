    /** Check that all abstract members of given class have definitions.
     *  @param pos          Position to be used for error reporting.
     *  @param c            The class.
     */
    void checkAllDefined(DiagnosticPosition pos, ClassSymbol c) {
    DEBUG.P(this,"checkAllDefined(2)");	
	try {
	    MethodSymbol undef = firstUndef(c, c);
	    if (undef != null) {
                if ((c.flags() & ENUM) != 0 &&
                    types.supertype(c.type).tsym == syms.enumSym &&
                    (c.flags() & FINAL) == 0) {
                    // add the ABSTRACT flag to an enum
                    c.flags_field |= ABSTRACT;
                } else {
                    MethodSymbol undef1 =
                        new MethodSymbol(undef.flags(), undef.name,
                                         types.memberType(c.type, undef), undef.owner);
                    log.error(pos, "does.not.override.abstract",
                              c, undef1, undef1.location());
                }
            }
	} catch (CompletionFailure ex) {
	    completionError(pos, ex);
	}
	
	DEBUG.P(0,this,"checkAllDefined(2)");	
    }

//where
        /** Return first abstract member of class `c' that is not defined
	 *  in `impl', null if there is none.
	 */
	private MethodSymbol firstUndef(ClassSymbol impl, ClassSymbol c) {
		DEBUG.P(this,"firstUndef(2)");	
	    MethodSymbol undef = null;
	    DEBUG.P("ClassSymbol impl="+impl);
	    DEBUG.P("ClassSymbol c   ="+c);
	    DEBUG.P("c.flags()="+Flags.toString(c.flags_field));
	    // Do not bother to search in classes that are not abstract,
	    // since they cannot have abstract members.
	    if (c == impl || (c.flags() & (ABSTRACT | INTERFACE)) != 0) {
			Scope s = c.members();
			DEBUG.P("Scope s="+s);
			for (Scope.Entry e = s.elems;
			     undef == null && e != null;
			     e = e.sibling) {
			     	
			    DEBUG.P("e.sym.kind="+Kinds.toString(e.sym.kind));
			    DEBUG.P("e.sym.flags()="+Flags.toString(e.sym.flags_field));
	
			    if (e.sym.kind == MTH &&
				(e.sym.flags() & (ABSTRACT|IPROXY)) == ABSTRACT) {
				MethodSymbol absmeth = (MethodSymbol)e.sym;
				DEBUG.P("absmeth="+absmeth);
				
				MethodSymbol implmeth = absmeth.implementation(impl, types, true);
				
				DEBUG.P("implmeth="+implmeth);
				if (implmeth == null || implmeth == absmeth)
				    undef = absmeth;
			    }
			}
			DEBUG.P("undef="+undef);
			if (undef == null) {
			    Type st = types.supertype(c.type);
			    
			    DEBUG.P("st.tag="+TypeTags.toString(st.tag));
			    
			    if (st.tag == CLASS)
				undef = firstUndef(impl, (ClassSymbol)st.tsym);
			}
			for (List<Type> l = types.interfaces(c.type);
			     undef == null && l.nonEmpty();
			     l = l.tail) {
			    undef = firstUndef(impl, (ClassSymbol)l.head.tsym);
			}
	    }
	    
	    DEBUG.P("undef="+undef);
	    DEBUG.P(0,this,"firstUndef(2)");	
	    return undef;
	}