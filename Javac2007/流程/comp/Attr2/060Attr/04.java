    void attribBounds(List<JCTypeParameter> typarams) {
    	DEBUG.P(this,"attribBounds(1)");
    	DEBUG.P("typarams="+typarams);
        for (JCTypeParameter typaram : typarams) {
            Type bound = typaram.type.getUpperBound();
			DEBUG.P("");
            DEBUG.P("typaram="+typaram);
			DEBUG.P("bound="+bound);
			if (bound != null) DEBUG.P("bound.tsym.className="+bound.tsym.getClass().getName());

            if (bound != null && bound.tsym instanceof ClassSymbol) {
                ClassSymbol c = (ClassSymbol)bound.tsym;
                DEBUG.P("bound.tsym.flags_field="+Flags.toString(c.flags_field));
                if ((c.flags_field & COMPOUND) != 0) {
                    assert (c.flags_field & UNATTRIBUTED) != 0 : c;
                    attribClass(typaram.pos(), c);
                }
            }
        }
        DEBUG.P(1,this,"attribBounds(1)");
    }
