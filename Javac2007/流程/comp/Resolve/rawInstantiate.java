
    /** Try to instantiate the type of a method so that it fits
     *  given type arguments and argument types. If succesful, return
     *  the method's instantiated type, else return null.
     *  The instantiation will take into account an additional leading
     *  formal parameter if the method is an instance method seen as a member
     *  of un underdetermined site In this case, we treat site as an additional
     *  parameter and the parameters of the class containing the method as
     *  additional type variables that get instantiated.
     *
     *  @param env         The current environment
     *  @param site        The type of which the method is a member.
     *  @param m           The method symbol.
     *  @param argtypes    The invocation's given value arguments.
     *  @param typeargtypes    The invocation's given type arguments.
     *  @param allowBoxing Allow boxing conversions of arguments.
     *  @param useVarargs Box trailing arguments into an array for varargs.
     */
    Type rawInstantiate(Env<AttrContext> env,
                        Type site,
                        Symbol m,
                        List<Type> argtypes,
                        List<Type> typeargtypes,
                        boolean allowBoxing,
                        boolean useVarargs,
                        Warner warn)
        throws Infer.NoInstanceException {
		try {//我加上的
		DEBUG.P(this,"rawInstantiate(8)");
		DEBUG.P("site="+site);
		DEBUG.P("m="+m);
		DEBUG.P("m.flags()="+Flags.toString(m.flags()));
		DEBUG.P("argtypes="+argtypes);
		DEBUG.P("typeargtypes="+typeargtypes);
		DEBUG.P("allowBoxing="+allowBoxing);
		DEBUG.P("useVarargs="+useVarargs);

        if (useVarargs && (m.flags() & VARARGS) == 0) return null;
        Type mt = types.memberType(site, m);

		DEBUG.P("mt="+mt);
		DEBUG.P("mt.tag="+TypeTags.toString(mt.tag));

        // tvars is the list of formal type variables for which type arguments
        // need to inferred.
        List<Type> tvars = env.info.tvars;
		DEBUG.P("tvars="+tvars);
        if (typeargtypes == null) typeargtypes = List.nil();
        if (mt.tag != FORALL && typeargtypes.nonEmpty()) {
			//在定义一个方法时没有指定类型变量，但是在调用此方法时
			//如果加上类型参数也是合法的
			DEBUG.P("This is not a polymorphic method");
            // This is not a polymorphic method, but typeargs are supplied
            // which is fine, see JLS3 15.12.2.1
        } else if (mt.tag == FORALL && typeargtypes.nonEmpty()) {
            ForAll pmt = (ForAll) mt;
            if (typeargtypes.length() != pmt.tvars.length())
                return null;
            // Check type arguments are within bounds
            List<Type> formals = pmt.tvars;
            List<Type> actuals = typeargtypes;
			DEBUG.P("formals="+formals);
			DEBUG.P("actuals="+actuals);
            while (formals.nonEmpty() && actuals.nonEmpty()) {
                List<Type> bounds = types.subst(types.getBounds((TypeVar)formals.head),
                                                pmt.tvars, typeargtypes);
                for (; bounds.nonEmpty(); bounds = bounds.tail)
                    if (!types.isSubtypeUnchecked(actuals.head, bounds.head, warn))
                        return null;
                formals = formals.tail;
                actuals = actuals.tail;
            }
            mt = types.subst(pmt.qtype, pmt.tvars, typeargtypes);
        } else if (mt.tag == FORALL) {
			DEBUG.P("(mt.tag == FORALL)");
            ForAll pmt = (ForAll) mt;
            List<Type> tvars1 = types.newInstances(pmt.tvars);
			DEBUG.P("tvars1="+tvars1);
            tvars = tvars.appendList(tvars1);
			DEBUG.P("tvars="+tvars);
            mt = types.subst(pmt.qtype, pmt.tvars, tvars1);
			DEBUG.P("mt="+mt);
        }

        // find out whether we need to go the slow route via infer
        boolean instNeeded = tvars.tail != null/*inlined: tvars.nonEmpty()*/;
        DEBUG.P("instNeeded="+instNeeded);
		DEBUG.P("argtypes="+argtypes);
		for (List<Type> l = argtypes;
             l.tail != null/*inlined: l.nonEmpty()*/ && !instNeeded;
             l = l.tail) {
            if (l.head.tag == FORALL) instNeeded = true;
        }

		DEBUG.P("instNeeded="+instNeeded);
        if (instNeeded)
            return
            infer.instantiateMethod(tvars,
                                    (MethodType)mt,
                                    argtypes,
                                    allowBoxing,
                                    useVarargs,
                                    warn);
        return
            argumentsAcceptable(argtypes, mt.getParameterTypes(),
                                allowBoxing, useVarargs, warn)
            ? mt
            : null;

		}finally{//我加上的
		DEBUG.P(0,this,"rawInstantiate(8)");
		}
    }

    /** Same but returns null instead throwing a NoInstanceException
     */
    Type instantiate(Env<AttrContext> env,
                     Type site,
                     Symbol m,
                     List<Type> argtypes,
                     List<Type> typeargtypes,
                     boolean allowBoxing,
                     boolean useVarargs,
                     Warner warn) {
		try {//我加上的
		DEBUG.P(this,"instantiate(8)");

        try {
            return rawInstantiate(env, site, m, argtypes, typeargtypes,
                                  allowBoxing, useVarargs, warn);
        } catch (Infer.NoInstanceException ex) {
            return null;
        }

		}finally{//我加上的
		DEBUG.P(0,this,"instantiate(8)");
		}
    }

    /** Check if a parameter list accepts a list of args.
     */
    boolean argumentsAcceptable(List<Type> argtypes,
                                List<Type> formals,
                                boolean allowBoxing,
                                boolean useVarargs,
                                Warner warn) {
		boolean argumentsAcceptable=false;//我加上的
		try {//我加上的
		DEBUG.P(this,"argumentsAcceptable(5)");
		DEBUG.P("argtypes="+argtypes);
		DEBUG.P("formals="+formals);
		DEBUG.P("allowBoxing="+allowBoxing);
		DEBUG.P("useVarargs="+useVarargs);

        Type varargsFormal = useVarargs ? formals.last() : null;
        while (argtypes.nonEmpty() && formals.head != varargsFormal) {
            boolean works = allowBoxing
                ? types.isConvertible(argtypes.head, formals.head, warn)
                : types.isSubtypeUnchecked(argtypes.head, formals.head, warn);
            DEBUG.P("works="+works);
			if (!works) return false;
            argtypes = argtypes.tail;
            formals = formals.tail;
        }
		DEBUG.P("formals.head="+formals.head);
		DEBUG.P("varargsFormal="+varargsFormal);
        if (formals.head != varargsFormal) return false; // not enough args
        if (!useVarargs)
            //return argtypes.isEmpty();
			return argumentsAcceptable=argtypes.isEmpty();
        Type elt = types.elemtype(varargsFormal);
        while (argtypes.nonEmpty()) {
            if (!types.isConvertible(argtypes.head, elt, warn))
                return false;
            argtypes = argtypes.tail;
        }
        //return true;
		return argumentsAcceptable=true;

		}finally{//我加上的
		DEBUG.P("argumentsAcceptable="+argumentsAcceptable);
		DEBUG.P(0,this,"argumentsAcceptable(5)");
		}
    }