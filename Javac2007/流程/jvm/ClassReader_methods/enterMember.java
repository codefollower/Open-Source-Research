    /** Add member to class unless it is synthetic.
     */
    private void enterMember(ClassSymbol c, Symbol sym) {
    	//只有flags_field单单含有SYNTHETIC时才为false，
    	//其他情况(包括同时含有SYNTHETIC与BRIDGE)都为true
        if ((sym.flags_field & (SYNTHETIC|BRIDGE)) != SYNTHETIC)
            c.members_field.enter(sym);
    }