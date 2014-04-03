    /** Generate code for a try or synchronized statement
	 *  @param body      The body of the try or synchronized statement.
	 *  @param catchers  The lis of catch clauses.
	 *  @param env       the environment current for the body.
	 */
	void genTry(JCTree body, List<JCCatch> catchers, Env<GenContext> env) {
		DEBUG.P(this,"genTry(3)");
	    int limit = code.nextreg;
	    int startpc = code.curPc();
	    Code.State stateTry = code.state.dup();
	    genStat(body, env, CRT_BLOCK);
	    int endpc = code.curPc();
	    boolean hasFinalizer =
		env.info.finalize != null &&
		env.info.finalize.hasFinalizer();
	    List<Integer> gaps = env.info.gaps.toList();
	    code.statBegin(TreeInfo.endPos(body));
	    genFinalizer(env);
	    code.statBegin(TreeInfo.endPos(env.tree));
	    Chain exitChain = code.branch(goto_);
	    endFinalizerGap(env);

		DEBUG.P("startpc="+startpc);
		DEBUG.P("endpc  ="+endpc);
	    if (startpc != endpc) for (List<JCCatch> l = catchers; l.nonEmpty(); l = l.tail) {
			// start off with exception on stack
			code.entryPoint(stateTry, l.head.param.sym.type);
			genCatch(l.head, env, startpc, endpc, gaps);
			genFinalizer(env);
			if (hasFinalizer || l.tail.nonEmpty()) {
				code.statBegin(TreeInfo.endPos(env.tree));
				exitChain = code.mergeChains(exitChain,
							 code.branch(goto_));
			}
			endFinalizerGap(env);
	    }

		DEBUG.P("hasFinalizer="+hasFinalizer);
	    if (hasFinalizer) {
			// Create a new register segement to avoid allocating
			// the same variables in finalizers and other statements.
			code.newRegSegment();

			// Add a catch-all clause.

			// start off with exception on stack
			int catchallpc = code.entryPoint(stateTry, syms.throwableType);

			DEBUG.P("catchallpc="+catchallpc);

			// Register all exception ranges for catch all clause.
			// The range of the catch all clause is from the beginning
			// of the try or synchronized block until the present
			// code pointer excluding all gaps in the current
			// environment's GenContext.
			int startseg = startpc;

			DEBUG.P("startseg="+startseg);
			DEBUG.P("env.info="+env.info);
			while (env.info.gaps.nonEmpty()) {
				int endseg = env.info.gaps.next().intValue();
				DEBUG.P("");
				DEBUG.P("endseg="+endseg);
				registerCatch(body.pos(), startseg, endseg,
					  catchallpc, 0);
				startseg = env.info.gaps.next().intValue();
			}
			code.statBegin(TreeInfo.finalizerPos(env.tree));
			code.markStatBegin();

			Item excVar = makeTemp(syms.throwableType);
			excVar.store();
			genFinalizer(env);
			excVar.load();
			registerCatch(body.pos(), startseg,
					  env.info.gaps.next().intValue(),
					  catchallpc, 0);
			code.emitop0(athrow);
			code.markDead();

			// If there are jsr's to this finalizer, ...
			DEBUG.P("env.info.cont="+env.info.cont);
			if (env.info.cont != null) {
				// Resolve all jsr's.
				code.resolve(env.info.cont);

				// Mark statement line number
				code.statBegin(TreeInfo.finalizerPos(env.tree));
				code.markStatBegin();

				// Save return address.
				LocalItem retVar = makeTemp(syms.throwableType);
				retVar.store();

				// Generate finalizer code.
				env.info.finalize.genLast();

				// Return.
				code.emitop1w(ret, retVar.reg);
				code.markDead();
			}
	    }

	    // Resolve all breaks.
	    code.resolve(exitChain);

	    // End the scopes of all try-local variables in variable info.
	    code.endScopes(limit);
	    DEBUG.P(0,this,"genTry(3)");
	}
