    /** Create a fresh environment for a variable's initializer.
     *  If the variable is a field, the owner of the environment's scope
     *  is be the variable itself, otherwise the owner is the method
     *  enclosing the variable definition.
     *
     *  @param tree     The variable definition.
     *  @param env      The environment current outside of the variable definition.
     */
    Env<AttrContext> initEnv(JCVariableDecl tree, Env<AttrContext> env) {
		DEBUG.P(this,"initEnv(2)");
        DEBUG.P("tree="+tree);
		DEBUG.P("env="+env); 
        
        Env<AttrContext> localEnv = env.dupto(new AttrContextEnv(tree, env.info.dup()));

		DEBUG.P("tree.sym.owner.kind="+Kinds.toString(tree.sym.owner.kind));

        if (tree.sym.owner.kind == TYP) {
            localEnv.info.scope = new Scope.DelegatedScope(env.info.scope);
            localEnv.info.scope.owner = tree.sym;
        }
        if ((tree.mods.flags & STATIC) != 0 ||
            (env.enclClass.sym.flags() & INTERFACE) != 0)
            localEnv.info.staticLevel++;

		DEBUG.P("localEnv="+localEnv);
		DEBUG.P(1,this,"initEnv(2)");
        return localEnv;
    }