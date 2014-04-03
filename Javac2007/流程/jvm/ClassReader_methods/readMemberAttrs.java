    /** Read member attributes.
     */
    void readMemberAttrs(Symbol sym) {
    	DEBUG.P(this,"readMemberAttrs(1)");
		DEBUG.P("sym="+sym);
		
        char ac = nextChar();
        
        DEBUG.P("ac="+(int)ac);
        
        for (int i = 0; i < ac; i++) {
            Name attrName = readName(nextChar());
            int attrLen = nextInt();
            readMemberAttr(sym, attrName, attrLen);
        }
        
        DEBUG.P(0,this,"readMemberAttrs(1)");
    }