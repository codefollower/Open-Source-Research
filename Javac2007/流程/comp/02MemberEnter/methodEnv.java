    /** Create a fresh environment for method bodies.
     *  @param tree     The method definition.
     *  @param env      The environment current outside of the method definition.
     */
    Env<AttrContext> methodEnv(JCMethodDecl tree, Env<AttrContext> env) {
    	DEBUG.P(this,"methodEnv(2)");
    	DEBUG.P("env="+env);
    	
    	//dupUnshared()后会有两个scope,新scope的next指向原来的scope,
    	//新scope的Entry[] table由原来的scope的table复制而来，但新scope的
    	//elems 开始时为 null，所以不会显示原来的scope的table。
    	//如下面的(nelems=0 owner=<init>()):
    	//localEnv=Env(TK=METHOD EC=Test)[AttrContext[Scope[(nelems=0 owner=<init>()) | (nelems=6 owner=Test)super, this, E, T, V, S]],outer=Env(TK=COMPILATION_UNIT EC=)[AttrContext[Scope[(nelems=3 owner=test)MyInnerClass, MyInnerClassStaticPublic, Test]]]]
        Env<AttrContext> localEnv =
            env.dup(tree, env.info.dup(env.info.scope.dupUnshared()));
        localEnv.enclMethod = tree;
        localEnv.info.scope.owner = tree.sym;
        if ((tree.mods.flags & STATIC) != 0) localEnv.info.staticLevel++;
        DEBUG.P("localEnv="+localEnv);
        DEBUG.P(0,this,"methodEnv(2)");
        return localEnv;
    }