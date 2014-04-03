    /** Is s a method symbol that overrides a method in a superclass? */
    boolean isOverrider(Symbol s) {
    try {//我加上的
	DEBUG.P(this,"isOverrider(Symbol s)");
	DEBUG.P("s="+s+"  s.kind="+Kinds.toString(s.kind)+" s.isStatic()="+s.isStatic());
	
        if (s.kind != MTH || s.isStatic())
            return false;
        MethodSymbol m = (MethodSymbol)s;
        TypeSymbol owner = (TypeSymbol)m.owner;
        
        DEBUG.P("m="+m);
        DEBUG.P("owner="+owner);
        
        for (Type sup : types.closure(owner.type)) {
            if (sup == owner.type)
                continue; // skip "this"
            Scope scope = sup.tsym.members();
			DEBUG.P("scope="+scope);
            for (Scope.Entry e = scope.lookup(m.name); e.scope != null; e = e.next()) {
                if (!e.sym.isStatic() && m.overrides(e.sym, owner, types, true))
                    return true;
            }
        }
        return false;
        
    }finally{//我加上的
	DEBUG.P(1,this,"isOverrider(Symbol s)");
	}
	    
    }