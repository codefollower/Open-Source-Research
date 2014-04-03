    /** A mapping that turns type variables into undetermined type variables.
     */
    Mapping fromTypeVarFun = new Mapping("fromTypeVarFun") {
	    public Type apply(Type t) {
			if (t.tag == TYPEVAR) return new UndetVar(t);
			else return t.map(this);
	    }
	};


	/** Instantiate method type `mt' by finding instantiations of
     *  `tvars' so that method can be applied to `argtypes'.
     */
    public Type instantiateMethod(List<Type> tvars,
				  MethodType mt,
				  List<Type> argtypes,
				  boolean allowBoxing,
				  boolean useVarargs,
                  Warner warn) throws NoInstanceException {
		try {//我加上的
		DEBUG.P(this,"instantiateMethod(5)");
		DEBUG.P("tvars="+tvars);
		DEBUG.P("mt="+mt);
		DEBUG.P("argtypes="+argtypes);
		DEBUG.P("allowBoxing="+allowBoxing);
		DEBUG.P("useVarargs="+useVarargs);

		//-System.err.println("instantiateMethod(" + tvars + ", " + mt + ", " + argtypes + ")"); //DEBUG
		List<Type> undetvars = Type.map(tvars, fromTypeVarFun);
		List<Type> formals = mt.argtypes;

		DEBUG.P("undetvars="+undetvars);
		DEBUG.P("formals="+formals);

		// instantiate all polymorphic argument types and
		// set up lower bounds constraints for undetvars
		Type varargsFormal = useVarargs ? formals.last() : null;
		while (argtypes.nonEmpty() && formals.head != varargsFormal) {
			Type ft = formals.head;
			Type at = argtypes.head.baseType();

			DEBUG.P("ft="+ft);
			DEBUG.P("at="+at+"  at.tag="+TypeTags.toString(at.tag));

			if (at.tag == FORALL) {
				DEBUG.P("(at.tag == FORALL)");
				at = instantiateArg((ForAll) at, ft, tvars, warn);
			}
			Type sft = types.subst(ft, tvars, undetvars);

			DEBUG.P("sft="+sft);
			boolean works = allowBoxing
					? types.isConvertible(at, sft, warn)
					: types.isSubtypeUnchecked(at, sft, warn);
			DEBUG.P("works="+works);
			if (!works) {
				throw unambiguousNoInstanceException
					.setMessage("no.conforming.assignment.exists",
					tvars, at, ft);
			}
			formals = formals.tail;
			argtypes = argtypes.tail;
		}
		if (formals.head != varargsFormal || // not enough args
            !useVarargs && argtypes.nonEmpty()) { // too many args
			// argument lists differ in length
			throw unambiguousNoInstanceException
			.setMessage("arg.length.mismatch");
        }

		DEBUG.P("useVarargs="+useVarargs);
        // for varargs arguments as well
        if (useVarargs) {
            Type elt = types.elemtype(varargsFormal);
            Type sft = types.subst(elt, tvars, undetvars);
            while (argtypes.nonEmpty()) {
                Type ft = sft;
                Type at = argtypes.head.baseType();
                if (at.tag == FORALL)
                    at = instantiateArg((ForAll) at, ft, tvars, warn);
                boolean works = types.isConvertible(at, sft, warn);
                if (!works) {
                    throw unambiguousNoInstanceException
                        .setMessage("no.conforming.assignment.exists",
                                    tvars, at, ft);
                }
                argtypes = argtypes.tail;
            }
        }

        // minimize as yet undetermined type variables
        for (Type t : undetvars)
            minimizeInst((UndetVar) t, warn);

        /** Type variables instantiated to bottom */
        ListBuffer<Type> restvars = new ListBuffer<Type>();

        /** Instantiated types or TypeVars if under-constrained */
        ListBuffer<Type> insttypes = new ListBuffer<Type>();

        /** Instantiated types or UndetVars if under-constrained */
        ListBuffer<Type> undettypes = new ListBuffer<Type>();

        for (Type t : undetvars) {
            UndetVar uv = (UndetVar)t;
            if (uv.inst.tag == BOT) {
                restvars.append(uv.qtype);
                insttypes.append(uv.qtype);
                undettypes.append(uv);
                uv.inst = null;
            } else {
                insttypes.append(uv.inst);
                undettypes.append(uv.inst);
            }
        }
		DEBUG.P("restvars  ="+restvars);
		DEBUG.P("insttypes ="+insttypes);
		DEBUG.P("undettypes="+undettypes);

        checkWithinBounds(tvars, undettypes.toList(), warn);

		DEBUG.P("!restvars.isEmpty()="+!restvars.isEmpty());
        if (!restvars.isEmpty()) {
            // if there are uninstantiated variables,
            // quantify result type with them
            mt = new MethodType(mt.argtypes,//Forall的qtype变成了mt.restype
                                new ForAll(restvars.toList(), mt.restype),
                                mt.thrown, syms.methodClass);
        }

        // return instantiated version of method type
        return types.subst(mt, tvars, insttypes.toList());

		}finally{//我加上的
		DEBUG.P(0,this,"instantiateMethod(5)");
		}
    }