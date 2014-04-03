    /** Is this symbol inherited into a given class?
     *  PRE: If symbol's owner is a interface,
     *       it is already assumed that the interface is a superinterface
     *       of given class.
     *  @param clazz  The class for which we want to establish membership.
     *                This must be a subclass of the member's owner.
     */
    //参考上面的isMemberOf，在此以假定clazz是symbol's owner的子类
    //此方法的功能是判断当前symbol能否被clazz继承
    public boolean isInheritedIn(Symbol clazz, Types types) {
    	try {//我加上的
		DEBUG.P(this,"isInheritedIn(2)");
		DEBUG.P("this.name="+this.name+" clazz="+clazz);
		DEBUG.P("flags_field="+Flags.toString(flags_field));
		DEBUG.P("flags_field & AccessFlags="+Flags.toString(flags_field & AccessFlags));
		

        switch ((int)(flags_field & Flags.AccessFlags)) {
        default: // error recovery
        case PUBLIC:
            return true;
        case PRIVATE:
            return this.owner == clazz;
        case PROTECTED:
            // we model interfaces as extending Object
            return (clazz.flags() & INTERFACE) == 0;
            //受保护的成员，只有非INTERFACE的Symbol子类才能继承
            //注意:这里只是按程序逻辑来理解，实际并不存在一个类的子类是一个接口的情况
            
        case 0:
            PackageSymbol thisPackage = this.packge();
            DEBUG.P("");DEBUG.P("case 0");
            DEBUG.P("thisPackage="+thisPackage);
			for (Symbol sup = clazz;
                 sup != null && sup != this.owner;
                 sup = types.supertype(sup.type).tsym) {
                DEBUG.P("sup != null && sup != this.owner="+(sup != null && sup != this.owner));
            	DEBUG.P("sup.type="+sup.type);
            	DEBUG.P("sup.type.isErroneous()="+sup.type.isErroneous());
                if (sup.type.isErroneous())
                    return true; // error recovery
                if ((sup.flags() & COMPOUND) != 0)
                    continue;
                DEBUG.P("(sup.packge() != thisPackage)="+(sup.packge() != thisPackage));
                if (sup.packge() != thisPackage)
                    return false;
            }
            return (clazz.flags() & INTERFACE) == 0;
        }
        
        }finally{//我加上的
		DEBUG.P(0,this,"isInheritedIn(2)");
		}
    }