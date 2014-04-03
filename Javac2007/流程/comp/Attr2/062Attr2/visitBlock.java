    public void visitBlock(JCBlock tree) {
		DEBUG.P(this,"visitBlock(1)");
		DEBUG.P("env.info.scope.owner="+env.info.scope.owner);
		DEBUG.P("env.info.scope.owner.kind="+Kinds.toString(env.info.scope.owner.kind));

        if (env.info.scope.owner.kind == TYP) {
            // Block is a static or instance initializer;
            // let the owner of the environment be a freshly
            // created BLOCK-method.
            Env<AttrContext> localEnv =
                env.dup(tree, env.info.dup(env.info.scope.dupUnshared()));
            localEnv.info.scope.owner =
                new MethodSymbol(tree.flags | BLOCK, names.empty, null,
                                 env.info.scope.owner);
            if ((tree.flags & STATIC) != 0) localEnv.info.staticLevel++;
            attribStats(tree.stats, localEnv);
        } else {
            // Create a new local environment with a local scope.
            Env<AttrContext> localEnv =
                env.dup(tree, env.info.dup(env.info.scope.dup()));
            attribStats(tree.stats, localEnv);
            localEnv.info.scope.leave();
        }
        result = null;
        DEBUG.P(0,this,"visitBlock(1)");
    }