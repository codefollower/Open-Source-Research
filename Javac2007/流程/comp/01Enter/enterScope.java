    /** The scope in which a member definition in environment env is to be entered
     *	This is usually the environment's scope, except for class environments,
     *	where the local scope is for type variables, and the this and super symbol
     *	only, and members go into the class member scope.
     */
    Scope enterScope(Env<AttrContext> env) {
		try {
    	DEBUG.P(this,"enterScope(1)");
		if((env.tree.tag == JCTree.CLASSDEF))
    		DEBUG.P("选择的Scope是 "+((JCClassDecl) env.tree).sym+" JCClassDecl.sym.members_field)");
		else
			DEBUG.P("选择的Scope是 env.info.scope 拥有者是"+env.info.scope.owner);

		return (env.tree.tag == JCTree.CLASSDEF)
			? ((JCClassDecl) env.tree).sym.members_field
			: env.info.scope;


		} finally {
    	DEBUG.P(0,this,"enterScope(1)");
    	}
    }