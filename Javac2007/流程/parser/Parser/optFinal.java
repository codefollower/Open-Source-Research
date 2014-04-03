    JCModifiers optFinal(long flags) {
    	try {//我加上的
    	DEBUG.P(this,"optFinal(long flags)");
    	DEBUG.P("flags="+Flags.toString(flags));
    	
        JCModifiers mods = modifiersOpt();
        
        DEBUG.P("mods.flags="+Flags.toString(mods.flags));
        
		//方法括号中的参数只能是final与deprecated(在JAVADOC)中指定
		//ParserTest(/** @deprecated */ final int i){}
        checkNoMods(mods.flags & ~(Flags.FINAL | Flags.DEPRECATED));
        mods.flags |= flags;
        
        DEBUG.P("mods.flags="+Flags.toString(mods.flags));
        return mods;
        
        }finally{//我加上的
		DEBUG.P(0,this,"optFinal(long flags)");
		} 
    }