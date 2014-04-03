    /** Check that a given method conforms with any method it overrides.
     *  @param tree         The tree from which positions are extracted
     *			    for errors.
     *  @param m            The overriding method.
     */
    void checkOverride(JCTree tree, MethodSymbol m) {
		try {//我加上的
		DEBUG.P(this,"checkOverride(2)");
		DEBUG.P("MethodSymbol m.name="+m.name);
		
		ClassSymbol origin = (ClassSymbol)m.owner;
		DEBUG.P("origin.name="+origin.name);
		DEBUG.P("origin.flags_field="+Flags.toString(origin.flags_field));
		
		if ((origin.flags() & ENUM) != 0 && names.finalize.equals(m.name))
			if (m.overrides(syms.enumFinalFinalize, origin, types, false)) {
				log.error(tree.pos(), "enum.no.finalize");
				return;
			}
		for (Type t = types.supertype(origin.type); t.tag == CLASS;
			 t = types.supertype(t)) {
			TypeSymbol c = t.tsym;
			Scope.Entry e = c.members().lookup(m.name);
			DEBUG.P("e.scope="+e.scope);
			while (e.scope != null) {
				if (m.overrides(e.sym, origin, types, false))
					checkOverride(tree, m, (MethodSymbol)e.sym, origin);
				e = e.next();
			}
		}
		
		}finally{//我加上的
		DEBUG.P(1,this,"checkOverride(2)");
		}
    }


    /** Check that this method conforms with overridden method 'other'.
     *  where `origin' is the class where checking started.
     *  Complications:
     *  (1) Do not check overriding of synthetic methods
     *      (reason: they might be final).
     *      todo: check whether this is still necessary.
     *  (2) Admit the case where an interface proxy throws fewer exceptions
     *      than the method it implements. Augment the proxy methods with the
     *      undeclared exceptions in this case.
     *  (3) When generics are enabled, admit the case where an interface proxy
     *	    has a result type
     *      extended by the result type of the method it implements.
     *      Change the proxies result type to the smaller type in this case.
     *
     *  @param tree         The tree from which positions
     *			    are extracted for errors.
     *  @param m            The overriding method.
     *  @param other        The overridden method.
     *  @param origin       The class of which the overriding method
     *			    is a member.
     */
    void checkOverride(JCTree tree,
		       MethodSymbol m,
		       MethodSymbol other,
		       ClassSymbol origin) {
		try {//我加上的
		DEBUG.P(this,"checkOverride(4)");
		DEBUG.P("m="+m+"  m.owner="+m.owner);
		DEBUG.P("m.flags()="+Flags.toString(m.flags()));
		DEBUG.P("other="+other+"  other.owner="+other.owner);
		DEBUG.P("other.flags()="+Flags.toString(other.flags()));
		DEBUG.P("origin="+origin);
		DEBUG.P("origin.flags()="+Flags.toString(origin.flags()));
		
		// Don't check overriding of synthetic methods or by bridge methods.
		if ((m.flags() & (SYNTHETIC|BRIDGE)) != 0 || (other.flags() & SYNTHETIC) != 0) {
			return;
		}

		// Error if static method overrides instance method (JLS 8.4.6.2).
		if ((m.flags() & STATIC) != 0 &&
			   (other.flags() & STATIC) == 0) {
			log.error(TreeInfo.diagnosticPositionFor(m, tree), "override.static",
				  cannotOverride(m, other));
			return;
		}

		// Error if instance method overrides static or final
		// method (JLS 8.4.6.1).
		if ((other.flags() & FINAL) != 0 ||
			 (m.flags() & STATIC) == 0 &&
			 (other.flags() & STATIC) != 0) {
			log.error(TreeInfo.diagnosticPositionFor(m, tree), "override.meth",
				  cannotOverride(m, other),
				  TreeInfo.flagNames(other.flags() & (FINAL | STATIC)));
			return;
		}

        if ((m.owner.flags() & ANNOTATION) != 0) {
            // handled in validateAnnotationMethod
            return;
        }

		// Error if overriding method has weaker access (JLS 8.4.6.3).
		if ((origin.flags() & INTERFACE) == 0 &&
			 protection(m.flags()) > protection(other.flags())) {
			log.error(TreeInfo.diagnosticPositionFor(m, tree), "override.weaker.access",
				  cannotOverride(m, other),
				  protectionString(other.flags()));
			return;
		}

		Type mt = types.memberType(origin.type, m);
		Type ot = types.memberType(origin.type, other);
		// Error if overriding result type is different
		// (or, in the case of generics mode, not a subtype) of
		// overridden result type. We have to rename any type parameters
		// before comparing types.
		List<Type> mtvars = mt.getTypeArguments();
		List<Type> otvars = ot.getTypeArguments();
		Type mtres = mt.getReturnType();

		DEBUG.P("mtvars="+mtvars);
		DEBUG.P("otvars="+otvars);
		DEBUG.P("mtres="+mtres);

		Type otres = types.subst(ot.getReturnType(), otvars, mtvars);

		overrideWarner.warned = false;
		boolean resultTypesOK =
			types.returnTypeSubstitutable(mt, ot, otres, overrideWarner);

		DEBUG.P("resultTypesOK="+resultTypesOK);
		DEBUG.P("overrideWarner.warned="+overrideWarner.warned);

		if (!resultTypesOK) {
			if (!source.allowCovariantReturns() &&
			m.owner != origin &&
			m.owner.isSubClass(other.owner, types)) {
			// allow limited interoperability with covariant returns
			} else {
				typeError(TreeInfo.diagnosticPositionFor(m, tree),
					  JCDiagnostic.fragment("override.incompatible.ret",
							 cannotOverride(m, other)),
					  mtres, otres);
				return;
			}
		} else if (overrideWarner.warned) {
			warnUnchecked(TreeInfo.diagnosticPositionFor(m, tree),
				  "prob.found.req",
				  JCDiagnostic.fragment("override.unchecked.ret",
							  uncheckedOverrides(m, other)),
				  mtres, otres);
		}
		
		// Error if overriding method throws an exception not reported
		// by overridden method.
		List<Type> otthrown = types.subst(ot.getThrownTypes(), otvars, mtvars);
		List<Type> unhandled = unHandled(mt.getThrownTypes(), otthrown);
		DEBUG.P("unhandled="+unhandled);
		if (unhandled.nonEmpty()) {
			log.error(TreeInfo.diagnosticPositionFor(m, tree),
				  "override.meth.doesnt.throw",
				  cannotOverride(m, other),
				  unhandled.head);
			return;
		}

		DEBUG.P("m.flags()="+Flags.toString(m.flags()));
		DEBUG.P("other.flags()="+Flags.toString(other.flags()));
		DEBUG.P("(m.flags() ^ other.flags())="+Flags.toString((m.flags() ^ other.flags())));
		DEBUG.P("lint.isEnabled(Lint.LintCategory.OVERRIDES)="+lint.isEnabled(Lint.LintCategory.OVERRIDES));
		// Optional warning if varargs don't agree 
		if ((((m.flags() ^ other.flags()) & Flags.VARARGS) != 0)
			&& lint.isEnabled(Lint.LintCategory.OVERRIDES)) {
			log.warning(TreeInfo.diagnosticPositionFor(m, tree),
				((m.flags() & Flags.VARARGS) != 0)
				? "override.varargs.missing"
				: "override.varargs.extra",
				varargsOverrides(m, other));
		} 

		// Warn if instance method overrides bridge method (compiler spec ??)
		if ((other.flags() & BRIDGE) != 0) {
			log.warning(TreeInfo.diagnosticPositionFor(m, tree), "override.bridge",
				uncheckedOverrides(m, other));
		}

		// Warn if a deprecated method overridden by a non-deprecated one.
		if ((other.flags() & DEPRECATED) != 0 
			&& (m.flags() & DEPRECATED) == 0 
			&& m.outermostClass() != other.outermostClass()
			&& !isDeprecatedOverrideIgnorable(other, origin)) {
			warnDeprecated(TreeInfo.diagnosticPositionFor(m, tree), other);
		}
		
		}finally{//我加上的
		DEBUG.P(0,this,"checkOverride(4)");
		}
    }