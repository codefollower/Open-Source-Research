    void writeFields(Scope.Entry e) {
    	DEBUG.P(this,"writeFields(Scope.Entry e)");
		
        // process them in reverse sibling order;
        // i.e., process them in declaration order.
        List<VarSymbol> vars = List.nil();
		//按源码中声明字段的顺序组成一个新的List
        for (Scope.Entry i = e; i != null; i = i.sibling) {
            if (i.sym.kind == VAR) vars = vars.prepend((VarSymbol)i.sym);
        }
        
        DEBUG.P("vars="+vars);
        
        while (vars.nonEmpty()) {
            writeField(vars.head);
            vars = vars.tail;
        }
        
        DEBUG.P(0,this,"writeFields(Scope.Entry e)");
    }