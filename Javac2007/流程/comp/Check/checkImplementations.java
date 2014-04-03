    /** Check that all methods which implement some
     *  method conform to the method they implement.
     *  @param tree         The class definition whose members are checked.
     */
    void checkImplementations(JCClassDecl tree) {
		DEBUG.P(this,"checkImplementations(1)");
		checkImplementations(tree, tree.sym);
		DEBUG.P(1,this,"checkImplementations(1)");
    }
//where
        /** Check that all methods which implement some
	 *  method in `ic' conform to the method they implement.
	 */
	void checkImplementations(JCClassDecl tree, ClassSymbol ic) {
		DEBUG.P(this,"checkImplementations(2)");
		DEBUG.P("ClassSymbol ic="+ic);
		DEBUG.P("tree.sym="+tree.sym);
	    ClassSymbol origin = tree.sym;
	    for (List<Type> l = types.closure(ic.type); l.nonEmpty(); l = l.tail) {
		ClassSymbol lc = (ClassSymbol)l.head.tsym;
		DEBUG.P("origin="+origin);
		DEBUG.P("lc="+lc);
		if ((allowGenerics || origin != lc) && (lc.flags() & ABSTRACT) != 0) {
		    for (Scope.Entry e=lc.members().elems; e != null; e=e.sibling) {
		    DEBUG.P("e.sym.name="+e.sym.name);	
		    DEBUG.P("e.sym.kind="+com.sun.tools.javac.code.Kinds.toString(e.sym.kind));
			if (e.sym.kind == MTH &&
			    (e.sym.flags() & (STATIC|ABSTRACT)) == ABSTRACT) {
			    MethodSymbol absmeth = (MethodSymbol)e.sym;
			    MethodSymbol implmeth = absmeth.implementation(origin, types, false);
			    DEBUG.P("implmeth="+implmeth);
			    DEBUG.P("absmeth="+absmeth);
			    DEBUG.P("(implmeth != absmeth)="+(implmeth != absmeth));
			    if (implmeth != null && implmeth != absmeth &&
				(implmeth.owner.flags() & INTERFACE) ==
				(origin.flags() & INTERFACE)) {
				// don't check if implmeth is in a class, yet
				// origin is an interface. This case arises only
				// if implmeth is declared in Object. The reason is
				// that interfaces really don't inherit from
				// Object it's just that the compiler represents
				// things that way.
				checkOverride(tree, implmeth, absmeth, origin);
			    }
			}
		    }
		}
	    }
	    DEBUG.P(0,this,"checkImplementations(2)");
	}