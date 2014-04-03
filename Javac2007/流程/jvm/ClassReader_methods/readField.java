/************************************************************************
 * Reading Symbols
 ***********************************************************************/

    /** Read a field.
     */
    VarSymbol readField() {
    	DEBUG.P(this,"readField()");

        long flags = adjustFieldFlags(nextChar());
        Name name = readName(nextChar());
        Type type = readType(nextChar());
        VarSymbol v = new VarSymbol(flags, name, type, currentOwner);
        readMemberAttrs(v);
        
        DEBUG.P("v="+v);
        DEBUG.P(0,this,"readField()");
        return v;
    }