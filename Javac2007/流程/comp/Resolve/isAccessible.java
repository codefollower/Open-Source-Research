    /** Is class accessible in given evironment?
     *  @param env    The current environment.
     *  @param c      The class whose accessibility is checked.
     */
    //在env这样一个环境中是否有权限访问TypeSymbol c
    public boolean isAccessible(Env<AttrContext> env, TypeSymbol c) {
		/*
			switch ((short)(c.flags() & AccessFlags)) {
			case PRIVATE:
				return
					env.enclClass.sym.outermostClass() ==
					c.owner.outermostClass();
			case 0:
				return
					env.toplevel.packge == c.owner // fast special case
					||
					env.toplevel.packge == c.packge()
					||
					// Hack: this case is added since synthesized default constructors
					// of anonymous classes should be allowed to access
					// classes which would be inaccessible otherwise.
					env.enclMethod != null &&
					(env.enclMethod.mods.flags & ANONCONSTR) != 0;
			default: // error recovery
			case PUBLIC:
				return true;
			case PROTECTED:
				return
					env.toplevel.packge == c.owner // fast special case
					||
					env.toplevel.packge == c.packge()
					||
					isInnerSubClass(env.enclClass.sym, c.owner);
			}
		*/
		boolean isAccessible=true;//我加上的
    	try {//我加上的
    	//DEBUG.ON();    		
		DEBUG.P(this,"isAccessible(2)");
		/*
		DEBUG.P("env="+env);
		DEBUG.P("c="+c);
		DEBUG.P("c.flags()="+Flags.toString(c.flags()));
		DEBUG.P("c.flags() & AccessFlags="+Flags.toString(c.flags() & AccessFlags));
		DEBUG.P("env.enclClass.sym.name="+env.enclClass.sym.name);
		DEBUG.P("env.enclClass.sym.outermostClass()="+env.enclClass.sym.outermostClass());
		DEBUG.P("c.owner.name="+c.owner.name);
		DEBUG.P("c.owner.outermostClass()="+c.owner.outermostClass());
		*/

		//AccessFlags = PUBLIC | PROTECTED | PRIVATE在Flags类中定义
        switch ((short)(c.flags() & AccessFlags)) {
        case PRIVATE:
            return isAccessible=
                env.enclClass.sym.outermostClass() ==
                c.owner.outermostClass();
        case 0:
            return isAccessible=
                env.toplevel.packge == c.owner // fast special case
                ||
                env.toplevel.packge == c.packge()
                ||
                // Hack: this case is added since synthesized default constructors
                // of anonymous classes should be allowed to access
                // classes which would be inaccessible otherwise.
                env.enclMethod != null &&
                (env.enclMethod.mods.flags & ANONCONSTR) != 0;
        default: // error recovery
        case PUBLIC:
            return true;
        case PROTECTED:
            return isAccessible=
                env.toplevel.packge == c.owner // fast special case
                ||
                env.toplevel.packge == c.packge()
                ||
                isInnerSubClass(env.enclClass.sym, c.owner);
        }
        
        }finally{//我加上的    
			DEBUG.P("c="+c+" flag="+Flags.toString(c.flags()));
			DEBUG.P("isAccessible="+isAccessible);
			DEBUG.P(0,this,"isAccessible(2)");
			//DEBUG.OFF();   
		}
    }
    //where
        /** Is given class a subclass of given base class, or an inner class
         *  of a subclass?
         *  Return null if no such class exists.
         *  @param c     The class which is the subclass or is contained in it.
         *  @param base  The base class
         */
        private boolean isInnerSubClass(ClassSymbol c, Symbol base) {
            while (c != null && !c.isSubClass(base, types)) {
                c = c.owner.enclClass();
            }
            return c != null;
        }

    boolean isAccessible(Env<AttrContext> env, Type t) {
        return (t.tag == ARRAY)
            ? isAccessible(env, types.elemtype(t))
            : isAccessible(env, t.tsym);
    }

    /** Is symbol accessible as a member of given type in given evironment?
     *  @param env    The current environment.
     *  @param site   The type of which the tested symbol is regarded
     *                as a member.
     *  @param sym    The symbol.
     */
    //假定Symbol sym是Type site的成员(member),判断
    //在env这样一个环境中是否有权限访问Symbol sym
    public boolean isAccessible(Env<AttrContext> env, Type site, Symbol sym) {
		boolean isAccessible=true;//我加上的
    	try {//我加上的
		DEBUG.P(this,"isAccessible(3)");
		DEBUG.P("env="+env);
		DEBUG.P("site="+site);
		DEBUG.P("sym.name="+sym.name);
		DEBUG.P("sym.flags_field="+Flags.toString(sym.flags_field));
		DEBUG.P("sym.flags_field & AccessFlags="+Flags.toString(sym.flags_field & AccessFlags));
		
        if (sym.name == names.init && sym.owner != site.tsym) return isAccessible=false;
        ClassSymbol sub;
        switch ((short)(sym.flags() & AccessFlags)) {
        case PRIVATE:
            return isAccessible=
                (env.enclClass.sym == sym.owner // fast special case
                 ||
                 env.enclClass.sym.outermostClass() ==
                 sym.owner.outermostClass())
                &&
                sym.isInheritedIn(site.tsym, types);
        case 0:
        	DEBUG.P("");DEBUG.P("case 0");
        	DEBUG.P("env.toplevel.packge="+env.toplevel.packge);
        	DEBUG.P("sym.owner.owner    ="+sym.owner.owner);
            DEBUG.P("sym.packge()       ="+sym.packge());
            return isAccessible=//sym.owner.owner当sym是顶层类的类型变量时 如ClassA<T,V extends T>，sym=T
                (env.toplevel.packge == sym.owner.owner // fast special case
                 ||
                 env.toplevel.packge == sym.packge())
                &&
                isAccessible(env, site)
                &&
                sym.isInheritedIn(site.tsym, types);
        case PROTECTED:
            return isAccessible=
                (env.toplevel.packge == sym.owner.owner // fast special case
                 ||
                 env.toplevel.packge == sym.packge()
                 ||
                 isProtectedAccessible(sym, env.enclClass.sym, site)
                 ||
                 // OK to select instance method or field from 'super' or type name
                 // (but type names should be disallowed elsewhere!)
                 env.info.selectSuper && (sym.flags() & STATIC) == 0 && sym.kind != TYP)
                &&
                isAccessible(env, site)
                &&
                // `sym' is accessible only if not overridden by
                // another symbol which is a member of `site'
                // (because, if it is overridden, `sym' is not strictly
                // speaking a member of `site'.)
                (sym.kind != MTH || sym.isConstructor() ||
                 ((MethodSymbol)sym).implementation(site.tsym, types, true) == sym);
        default: // this case includes erroneous combinations as well
            return isAccessible=isAccessible(env, site);
        }
        
        
        }finally{//我加上的
		DEBUG.P("sym="+sym+" flag="+Flags.toString(sym.flags()));
		DEBUG.P("site="+site);
		DEBUG.P("isAccessible="+isAccessible);
		DEBUG.P(0,this,"isAccessible(3)");
		}
    }
    //where
        /** Is given protected symbol accessible if it is selected from given site
         *  and the selection takes place in given class?
         *  @param sym     The symbol with protected access
         *  @param c       The class where the access takes place
         *  @site          The type of the qualifier
         */
        private
        boolean isProtectedAccessible(Symbol sym, ClassSymbol c, Type site) {
            while (c != null &&
                   !(c.isSubClass(sym.owner, types) &&
                     (c.flags() & INTERFACE) == 0 &&
                     // In JLS 2e 6.6.2.1, the subclass restriction applies
                     // only to instance fields and methods -- types are excluded
                     // regardless of whether they are declared 'static' or not.
                     ((sym.flags() & STATIC) != 0 || sym.kind == TYP || site.tsym.isSubClass(c, types))))
                c = c.owner.enclClass();
            return c != null;
        }