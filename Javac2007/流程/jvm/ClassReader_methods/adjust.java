/************************************************************************
 * Adjusting flags
 ***********************************************************************/

    long adjustFieldFlags(long flags) {
    	DEBUG.P(this,"adjustFieldFlags(1)");
		DEBUG.P("flags="+Flags.toString(flags));
		DEBUG.P(0,this,"adjustFieldFlags(1)");
        return flags;
    }
    long adjustMethodFlags(long flags) {
    	try {//我加上的
		DEBUG.P(this,"adjustMethodFlags(1)");
		DEBUG.P("flags="+Flags.toString(flags));

        if ((flags & ACC_BRIDGE) != 0) {
            flags &= ~ACC_BRIDGE;
            flags |= BRIDGE;
            if (!allowGenerics)
                flags &= ~SYNTHETIC;
        }
        if ((flags & ACC_VARARGS) != 0) {
            flags &= ~ACC_VARARGS;
            flags |= VARARGS;
        }
        
        DEBUG.P("flags="+Flags.toString(flags));
        return flags;
        
        }finally{//我加上的
		DEBUG.P(0,this,"adjustMethodFlags(1)");
		}
    }
    long adjustClassFlags(long flags) {
    	try {//我加上的
		DEBUG.P(this,"adjustClassFlags((1)");
		DEBUG.P("flags="+Flags.toString(flags));
		
        return flags & ~ACC_SUPER; // SUPER and SYNCHRONIZED bits overloaded
        
        }finally{//我加上的
        DEBUG.P("flags="+Flags.toString(flags & ~ACC_SUPER));
		DEBUG.P(0,this,"adjustMethodFlags(1)");
		}
    }