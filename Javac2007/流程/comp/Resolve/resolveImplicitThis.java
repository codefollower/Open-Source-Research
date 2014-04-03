    /**
     * Resolve an appropriate implicit this instance for t's container.
     * JLS2 8.8.5.1 and 15.9.2
     */
    Type resolveImplicitThis(DiagnosticPosition pos, Env<AttrContext> env, Type t) {
		DEBUG.P(this,"resolveImplicitThis(3)");
		DEBUG.P("env="+env);
		DEBUG.P("t="+t);
		DEBUG.P("t.tsym.owner="+t.tsym.owner);
		DEBUG.P("t.tsym.owner.kind="+Kinds.toString(t.tsym.owner.kind));

        Type thisType = (((t.tsym.owner.kind & (MTH|VAR)) != 0)
                         ? resolveSelf(pos, env, t.getEnclosingType().tsym, names._this)
                         : resolveSelfContaining(pos, env, t.tsym)).type;

		DEBUG.P("env.info.isSelfCall="+env.info.isSelfCall);
		DEBUG.P("if (env.info.isSelfCall && thisType.tsym == env.enclClass.sym)="+(env.info.isSelfCall && thisType.tsym == env.enclClass.sym));
        if (env.info.isSelfCall && thisType.tsym == env.enclClass.sym)
            log.error(pos, "cant.ref.before.ctor.called", "this");
        
		DEBUG.P("thisType="+thisType);
		DEBUG.P(0,this,"resolveImplicitThis(3)");
		return thisType;
    }