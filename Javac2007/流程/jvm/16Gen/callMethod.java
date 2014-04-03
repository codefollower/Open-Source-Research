    /** Generate code to call a non-private method or constructor.
     *  @param pos         Position to be used for error reporting.
     *  @param site        The type of which the method is a member.
     *  @param name        The method's name.
     *  @param argtypes    The method's argument types.
     *  @param isStatic    A flag that indicates whether we call a
     *                     static or instance method.
     */
    void callMethod(DiagnosticPosition pos,
		    Type site, Name name, List<Type> argtypes,
		    boolean isStatic) {
	DEBUG.P(this,"callMethod(4)");
	DEBUG.P("site="+site);
	DEBUG.P("name="+name);
	DEBUG.P("argtypes="+argtypes);
	DEBUG.P("isStatic="+isStatic);

	Symbol msym = rs.
	    resolveInternalMethod(pos, attrEnv, site, name, argtypes, null);
	if (isStatic) items.makeStaticItem(msym).invoke();
	else items.makeMemberItem(msym, name == names.init).invoke();

	DEBUG.P(0,this,"callMethod(4)");
    }