    /** Fully check membership: hierarchy, protection, and hiding.
     *  Does not exclude methods not inherited due to overriding.
     */
    public boolean isMemberOf(TypeSymbol clazz, Types types) {
    	try {//我加上的
		DEBUG.P(this,"isMemberOf(2)");
		DEBUG.P("this.name="+this.name);
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

    /** Is this symbol the same as or enclosed by the given class? */
    public boolean isEnclosedBy(ClassSymbol clazz) {
    	//如果clazz与当前smybol相同，或与当前smybol的(直接的或间接的)owner相同，则返回true
		/*
		for (Symbol sym = this; sym.kind != PCK; sym = sym.owner)
            if (sym == clazz) return true;
        return false;
		*/
		
		//我加上的
		DEBUG.P(this,"isEnclosedBy(ClassSymbol clazz)");
		DEBUG.P("clazz="+clazz);
        boolean result=false;
		for (Symbol sym = this; sym.kind != PCK; sym = sym.owner) {
			DEBUG.P("sym="+sym);
            if (sym == clazz) {
				result=true;
				break;
			}
		}
		DEBUG.P("result="+result);
		DEBUG.P(0,this,"isEnclosedBy(ClassSymbol clazz)");
		return result;
    }

    /** Check for hiding.  Note that this doesn't handle multiple
     *  (interface) inheritance. */
    private boolean hiddenIn(ClassSymbol clazz, Types types) {
		boolean hiddenIn=false;
		try {//我加上的
		DEBUG.P(this,"hiddenIn(2)");
		DEBUG.P("this.name ="+this.name);
		DEBUG.P("owner.name="+owner.name);
		DEBUG.P("clazz.name="+clazz.name);
		DEBUG.P("this.kind="+Kinds.toString(kind));
		DEBUG.P("this.flags_field="+Flags.toString(flags_field));
		
    	//超类的非STATIC方法不能被子类hidden，直接返回false
        if (kind == MTH && (flags() & STATIC) == 0) return false;
        
        while (true) {
            if (owner == clazz) return false;
            Scope.Entry e = clazz.members().lookup(name);
            while (e.scope != null) {
                if (e.sym == this) return false;
                
                //子类与超类的成员如果有相同kind与name的成员，
                //那么子类不会继承超类同kind与name的成员
                if (e.sym.kind == kind &&
                    (kind != MTH ||
                     (e.sym.flags() & STATIC) != 0 &&
                     types.isSubSignature(e.sym.type, type))) {
					hiddenIn=true;
                    return true;
					}
                e = e.next();
            }
            Type superType = types.supertype(clazz.type);
            if (superType.tag != TypeTags.CLASS) return false;
            clazz = (ClassSymbol)superType.tsym;
        }

		}finally{//我加上的
		DEBUG.P("");
		DEBUG.P("this.name ="+this.name);
		DEBUG.P("owner.name="+owner.name);
		DEBUG.P("clazz.name="+clazz.name);
		DEBUG.P("hiddenIn="+hiddenIn);	
		DEBUG.P(0,this,"hiddenIn(2)");
		}
    }

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
        //访问标志缺省的成员，只有同包的非INTERFACE的Symbol子类才能继承
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
				/*
				//clazz所在的直到this.owner为根的继承树(含clazz)上的所有类所在的包必须都是thisPackage
				//只要有一个不是thisPackage都返回false

				例子:
				clazz代表ClassC，this代表Class1，
				通过"import static my.test.ClassC.*;"语句转到此方法

				package my.test;
				public class ClassA {
					static class Class1{}
				}

				package my;
				public class ClassB extends my.test.ClassA {}

				package my.test;
				public class ClassC extends my.ClassB {}
				*/
                if (sup.packge() != thisPackage)
                    return false;
            }
            return (clazz.flags() & INTERFACE) == 0;
        }
        
        }finally{//我加上的
		DEBUG.P(0,this,"isInheritedIn(2)");
		}
    }

    /** The (variable or method) symbol seen as a member of given
     *  class type`site' (this might change the symbol's type).
     *  This is used exclusively for producing diagnostics.
     */
    public Symbol asMemberOf(Type site, Types types) {
        throw new AssertionError();
    }

    /** Does this method symbol override `other' symbol, when both are seen as
     *  members of class `origin'?  It is assumed that _other is a member
     *  of origin.
     *
     *  It is assumed that both symbols have the same name.  The static
     *  modifier is ignored for this test.
     *
     *  See JLS 8.4.6.1 (without transitivity) and 8.4.6.4
     */
    public boolean overrides(Symbol _other, TypeSymbol origin, Types types, boolean checkResult) {
        return false;
    }

    /** Complete the elaboration of this symbol's definition.
     */
    public void complete() throws CompletionFailure {
    	DEBUG.P(this,"complete()");
    	DEBUG.P("name="+name+"   completer="+completer);
        if (completer != null) {
            Completer c = completer;
            completer = null;
            //DEBUG.P("c.getClass().getName()="+c.getClass().getName(),true);
            //输出如:com.sun.tools.javac.jvm.ClassReader
            //另外也请注意com.sun.tools.javac.comp.MemberEnter
            c.complete(this);
        }
        DEBUG.P(0,this,"complete()");
    }

    /** True if the symbol represents an entity that exists.
     */
    //只有子类PackageSymbol覆盖了此方法，其他子类没有覆盖。
    //在com.sun.tools.javac.comp.Resolve类中对此方法有大量运用，一般都返回true
    public boolean exists() {
        return true;
    }

    public Type asType() {
        return type;
    }

    public Symbol getEnclosingElement() {
        return owner;
    }

    public ElementKind getKind() {
        return ElementKind.OTHER;       // most unkind
    }

    public Set<Modifier> getModifiers() {
        return Flags.asModifierSet(flags());
    }

    public Name getSimpleName() {
        return name;
    }

    /**
     * @deprecated this method should never be used by javac internally.
     */
    @Deprecated
    public <A extends java.lang.annotation.Annotation> A getAnnotation(Class<A> annoType) {
        return JavacElements.getAnnotation(this, annoType);
    }

    // TODO: getEnclosedElements should return a javac List, fix in FilteredMemberList
    public java.util.List<Symbol> getEnclosedElements() {
        return List.nil();
    }

    public List<TypeSymbol> getTypeParameters() {
        ListBuffer<TypeSymbol> l = ListBuffer.lb();
        for (Type t : type.getTypeArguments()) {
            l.append(t.tsym);
        }
        return l.toList();
    }







































