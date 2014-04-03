    /** Read a method.
     */
    MethodSymbol readMethod() {
    	DEBUG.P(this,"readMethod()");
    	
        long flags = adjustMethodFlags(nextChar());
        Name name = readName(nextChar());
        Type type = readType(nextChar());
        if (name == names.init && currentOwner.hasOuterInstance()) {
            // Sometimes anonymous classes don't have an outer
            // instance, however, there is no reliable way to tell so
            // we never strip this$n
            if (currentOwner.name.len != 0)
                type = new MethodType(type.getParameterTypes().tail,
                                      type.getReturnType(),
                                      type.getThrownTypes(),
                                      syms.methodClass);
        }
        MethodSymbol m = new MethodSymbol(flags, name, type, currentOwner);
        Symbol prevOwner = currentOwner;
        currentOwner = m;
        try {
            readMemberAttrs(m);
        } finally {
            currentOwner = prevOwner;
        }
        
        DEBUG.P("m="+m);
        DEBUG.P(0,this,"readMethod()");
        return m;
    }