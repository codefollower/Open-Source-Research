/* ********************************************************************
 * Adding miranda methods
 *********************************************************************/

    /** Add abstract methods for all methods defined in one of
     *  the interfaces of a given class,
     *  provided they are not already implemented in the class.
     *
     *  @param c      The class whose interfaces are searched for methods
     *                for which Miranda methods should be added.
     */
    void implementInterfaceMethods(ClassSymbol c) {
	implementInterfaceMethods(c, c);
    }

    /** Add abstract methods for all methods defined in one of
     *  the interfaces of a given class,
     *  provided they are not already implemented in the class.
     *
     *  @param c      The class whose interfaces are searched for methods
     *                for which Miranda methods should be added.
     *  @param site   The class in which a definition may be needed.
     */
    void implementInterfaceMethods(ClassSymbol c, ClassSymbol site) {
	for (List<Type> l = types.interfaces(c.type); l.nonEmpty(); l = l.tail) {
	    ClassSymbol i = (ClassSymbol)l.head.tsym;
	    for (Scope.Entry e = i.members().elems;
		 e != null;
		 e = e.sibling)
	    {
		if (e.sym.kind == MTH && (e.sym.flags() & STATIC) == 0)
		{
		    MethodSymbol absMeth = (MethodSymbol)e.sym;
		    MethodSymbol implMeth = absMeth.binaryImplementation(site, types);
		    if (implMeth == null)
			addAbstractMethod(site, absMeth);
		    else if ((implMeth.flags() & IPROXY) != 0)
			adjustAbstractMethod(site, implMeth, absMeth);
		}
	    }
	    implementInterfaceMethods(i, site);
	}
    }

    /** Add an abstract methods to a class
     *  which implicitly implements a method defined in some interface
     *  implemented by the class. These methods are called "Miranda methods".
     *  Enter the newly created method into its enclosing class scope.
     *  Note that it is not entered into the class tree, as the emitter
     *  doesn't need to see it there to emit an abstract method.
     *
     *  @param c      The class to which the Miranda method is added.
     *  @param m      The interface method symbol for which a Miranda method
     *                is added.
     */
    private void addAbstractMethod(ClassSymbol c,
				   MethodSymbol m) {
	MethodSymbol absMeth = new MethodSymbol(
	    m.flags() | IPROXY | SYNTHETIC, m.name,
	    m.type, // was c.type.memberType(m), but now only !generics supported
	    c);
	c.members().enter(absMeth); // add to symbol table
    }

    private void adjustAbstractMethod(ClassSymbol c,
				      MethodSymbol pm,
				      MethodSymbol im) {
        MethodType pmt = (MethodType)pm.type;
        Type imt = types.memberType(c.type, im);
	pmt.thrown = chk.intersect(pmt.getThrownTypes(), imt.getThrownTypes());
    }