/************************************************************************
 * Loading Classes
 ***********************************************************************/

    /** Define a new class given its name and owner.
     */
    public ClassSymbol defineClass(Name name, Symbol owner) {
    	//DEBUG.P("defineClass(Name name="+name+", Symbol owner="+owner+")");
        ClassSymbol c = new ClassSymbol(0, name, owner);
        
        //在ClassSymbol(0, name, owner)内部已按name和owner对flatname赋值
        if (owner.kind == PCK)
            assert classes.get(c.flatname) == null : c;//同一包下不能有同名的两个(或多个)类
        c.completer = this;
        DEBUG.P("新增ClassSymbol(name="+name+", owner="+owner+", flags=0, completer=ClassReader)");
        return c;
    }