    /** An environment is an "initializer" if it is a constructor or
     *  an instance initializer.
     */
    static boolean isInitializer(Env<AttrContext> env) {
		try {//我加上的
    	DEBUG.P(Resolve.class,"isInitializer(Env<AttrContext> env)");
    	DEBUG.P("env="+env);

        Symbol owner = env.info.scope.owner;

		DEBUG.P("owner="+owner);
		DEBUG.P("owner.isConstructor()="+owner.isConstructor());

		if(!owner.isConstructor()) {
			DEBUG.P("owner.owner="+owner.owner);
			DEBUG.P("owner.owner.kind="+Kinds.toString(owner.owner.kind));
			DEBUG.P("owner.kind="+Kinds.toString(owner.kind));
			DEBUG.P("owner.flags()="+Flags.toString(owner.flags()));
		}

        return owner.isConstructor() ||
            owner.owner.kind == TYP &&
            (owner.kind == VAR ||
             owner.kind == MTH && (owner.flags() & BLOCK) != 0) &&
            (owner.flags() & STATIC) == 0;

		}finally{
        DEBUG.P(0,Resolve.class,"isInitializer(Env<AttrContext> env)");
        }
    }