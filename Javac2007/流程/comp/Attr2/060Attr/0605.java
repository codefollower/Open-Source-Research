    /** Fully check membership: hierarchy, protection, and hiding.
     *  Does not exclude methods not inherited due to overriding.
     */
    public boolean isMemberOf(TypeSymbol clazz, Types types) {
    	try {//我加上的
		DEBUG.P(this,"isMemberOf(2)");
		DEBUG.P("owner.name="+owner.name);
		DEBUG.P("clazz.name="+clazz.name);
		DEBUG.P("(owner == clazz)="+(owner == clazz));

    	//当owner == clazz时，说明当前symbol是clazz的成员，直接返回true
    	//当clazz.isSubClass(owner, types)返回true时，可知clazz是owner
    	//的子类,但必须再用isInheritedIn(clazz, types)来判断当
    	//前symbol(owner的成员,如字段,方法等)是否能被子类clazz继承下来。
        /*return
            owner == clazz ||
            clazz.isSubClass(owner, types) &&
            isInheritedIn(clazz, types) &&
            !hiddenIn((ClassSymbol)clazz, types);*/

		boolean isMemberOf=
			owner == clazz ||
            clazz.isSubClass(owner, types) &&
            isInheritedIn(clazz, types) &&
            !hiddenIn((ClassSymbol)clazz, types);
        
		DEBUG.P("");
		DEBUG.P("isMemberOf="+isMemberOf);	
		return isMemberOf;
        }finally{//我加上的
		DEBUG.P(0,this,"isMemberOf(2)");
		}
    }