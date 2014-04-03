    //内部类不属于JCCompilationUnit(topLevelEnv),而只属于JCClassDecl(classEnv)
    public Env<AttrContext> classEnv(JCClassDecl tree, Env<AttrContext> env) {
		Env<AttrContext> localEnv =
			env.dup(tree, env.info.dup(new Scope(tree.sym)));
		localEnv.enclClass = tree;
		localEnv.outer = env;
		localEnv.info.isSelfCall = false;
		localEnv.info.lint = null; // leave this to be filled in by Attr, 
								   // when annotations have been processed
		return localEnv;
    }