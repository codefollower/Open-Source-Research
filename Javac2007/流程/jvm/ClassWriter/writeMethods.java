    void writeMethods(Scope.Entry e) {
		DEBUG.P(this,"writeMethods(1)");

        List<MethodSymbol> methods = List.nil();
        for (Scope.Entry i = e; i != null; i = i.sibling) {
            if (i.sym.kind == MTH && (i.sym.flags() & HYPOTHETICAL) == 0)
                methods = methods.prepend((MethodSymbol)i.sym);
        }
        
        DEBUG.P("methods="+methods);
        
        while (methods.nonEmpty()) {
            writeMethod(methods.head);
            methods = methods.tail;
        }

		DEBUG.P(0,this,"writeMethods(1)");
    }