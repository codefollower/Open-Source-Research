//where
        /** Generate code for a method.
	 *  @param tree     The tree representing the method definition.
	 *  @param env      The environment current for the method body.
	 *  @param fatcode  A flag that indicates whether all jumps are
	 *		    within 32K.  We first invoke this method under
	 *		    the assumption that fatcode == false, i.e. all
	 *		    jumps are within 32K.  If this fails, fatcode
	 *		    is set to true and we try again.
	 */
	//b10
	void genMethod(JCMethodDecl tree, Env<GenContext> env, boolean fatcode) {
		try {//我加上的
		DEBUG.P(this,"genMethod(3)");
		DEBUG.P("env="+env);
		DEBUG.P("fatcode="+fatcode);

	    MethodSymbol meth = tree.sym;
//    	System.err.println("Generating " + meth + " in " + meth.owner); //DEBUG

		/*
    	由方法每个参数的type计算出所有参数所占的总字数(一个字是堆栈出入栈操作的基本单位)，
    	(double和long类型的参数占两个字)
    	如果是非静态方法(隐含this变量,在initCode方法中把this加到局部变量数组中)，
    	那么总字数再加1，总字数必须小于ClassFile.MAX_PARAMETERS(255)
    	*/
	    if (Code.width(types.erasure(env.enclMethod.sym.type).getParameterTypes())  +
		(((tree.mods.flags & STATIC) == 0 || meth.isConstructor()) ? 1 : 0) >
		ClassFile.MAX_PARAMETERS) {
		log.error(tree.pos(), "limit.parameters");
		nerrs++;
	    }

	    else if (tree.body != null) {
		// Create a new code structure and initialize it.
                int startpcCrt = initCode(tree, env, fatcode);

		try {
                    genStat(tree.body, env);
                } catch (CodeSizeOverflow e) {
                    // Failed due to code limit, try again with jsr/ret
                    startpcCrt = initCode(tree, env, fatcode);
                    genStat(tree.body, env);
                }
        
		DEBUG.P("");
		DEBUG.P("code.state.stacksize="+code.state.stacksize);
		if (code.state.stacksize != 0) {
		    log.error(tree.body.pos(), "stack.sim.error", tree);
		    throw new AssertionError();
		}

		DEBUG.P("");
		DEBUG.P("code.isAlive()="+code.isAlive());

		// If last statement could complete normally, insert a
		// return at the end.
		if (code.isAlive()) {
		    code.statBegin(TreeInfo.endPos(tree.body));
		    if (env.enclMethod == null ||
			env.enclMethod.sym.type.getReturnType().tag == VOID) {
			code.emitop0(return_);
		    } else {
			// sometime dead code seems alive (4415991);
			// generate a small loop instead
			int startpc = code.entryPoint();
			CondItem c = items.makeCondItem(goto_);
			code.resolve(c.jumpTrue(), startpc);
		    }
		}
		if (genCrt)
		    code.crt.put(tree.body,
				 CRT_BLOCK,
				 startpcCrt,
				 code.curPc());

		// End the scope of all local variables in variable info.
		code.endScopes(0);

		// If we exceeded limits, panic
		if (code.checkLimits(tree.pos(), log)) {
		    nerrs++;
		    return;
		}

		DEBUG.P("");
		DEBUG.P("fatcode="+fatcode);
		DEBUG.P("code.fatcode="+code.fatcode);

		// If we generated short code but got a long jump, do it again
		// with fatCode = true.
		if (!fatcode && code.fatcode) genMethod(tree, env, true);

		// Clean up
		if(stackMap == StackMapFormat.JSR202) {
		    code.lastFrame = null;
		    code.frameBeforeLast = null;
		}
	    }

		}finally{//我加上的
		DEBUG.P(0,this,"genMethod(3)");
		}
	}