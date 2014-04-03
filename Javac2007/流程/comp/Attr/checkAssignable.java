    /** Is given blank final variable assignable, i.e. in a scope where it
     *  may be assigned to even though it is final?
     *  @param v      The blank final variable.
     *  @param env    The current environment.
     */
    boolean isAssignableAsBlankFinal(VarSymbol v, Env<AttrContext> env) {
		try {//我加上的
        DEBUG.P(this,"isAssignableAsBlankFinal(2)");
		

        Symbol owner = env.info.scope.owner;

		DEBUG.P("v="+v);
		DEBUG.P("v.flags()="+Flags.toString(v.flags()));
		DEBUG.P("v.owner="+v.owner);
		DEBUG.P("owner="+owner);
		DEBUG.P("owner.flags()="+Flags.toString(owner.flags()));
           // owner refers to the innermost variable, method or
           // initializer block declaration at this point.
        return
            v.owner == owner
            ||
            ((owner.name == names.init ||    // i.e. we are in a constructor
              owner.kind == VAR ||           // i.e. we are in a variable initializer
              (owner.flags() & BLOCK) != 0)  // i.e. we are in an initializer block
             &&
             v.owner == owner.owner
             &&
             ((v.flags() & STATIC) != 0) == Resolve.isStatic(env));

		}finally{//我加上的
            DEBUG.P(0,this,"isAssignableAsBlankFinal(2)");
        }
    }

    /** Check that variable can be assigned to.
     *  @param pos    The current source code position.
     *  @param v      The assigned varaible
     *  @param base   If the variable is referred to in a Select, the part
     *                to the left of the `.', null otherwise.
     *  @param env    The current environment.
     */
    void checkAssignable(DiagnosticPosition pos, VarSymbol v, JCTree base, Env<AttrContext> env) {
		DEBUG.P(this,"checkAssignable(4)");
		DEBUG.P("v="+v);
		DEBUG.P("v.flags()="+Flags.toString(v.flags()));
		DEBUG.P("base="+base);

		if(base != null) {
			DEBUG.P("base.tag="+base.myTreeTag());
			DEBUG.P("TreeInfo.name(base)="+TreeInfo.name(base));
		}

        if ((v.flags() & FINAL) != 0 &&
            ((v.flags() & HASINIT) != 0
             ||
             !((base == null ||
               (base.tag == JCTree.IDENT && TreeInfo.name(base) == names._this)) &&
               isAssignableAsBlankFinal(v, env)))) {
            log.error(pos, "cant.assign.val.to.final.var", v);
        }

		DEBUG.P(0,this,"checkAssignable(4)");
    }