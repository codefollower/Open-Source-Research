    public void visitSynchronized(JCSynchronized tree) {
		DEBUG.P(this,"visitSynchronized(1)");
		
		int limit = code.nextreg;
		// Generate code to evaluate lock and save in temporary variable.
		final LocalItem lockVar = makeTemp(syms.objectType);
		genExpr(tree.lock, tree.lock.type).load().duplicate();
		lockVar.store();

		// Generate code to enter monitor.
		code.emitop0(monitorenter);
		code.state.lock(lockVar.reg);

		// Generate code for a try statement with given body, no catch clauses
		// in a new environment with the "exit-monitor" operation as finalizer.
		final Env<GenContext> syncEnv = env.dup(tree, new GenContext());
		syncEnv.info.finalize = new GenFinalizer() {
			void gen() {
				DEBUG.P(this,"gen()");

				genLast();
				assert syncEnv.info.gaps.length() % 2 == 0;
				syncEnv.info.gaps.append(code.curPc());

				DEBUG.P(0,this,"gen()");
			}

			void genLast() {
				DEBUG.P(this,"genLast()");
				DEBUG.P("code.isAlive()="+code.isAlive());

				if (code.isAlive()) {
					lockVar.load();
					code.emitop0(monitorexit);
					code.state.unlock(lockVar.reg);
				}

				DEBUG.P(0,this,"genLast()");
			}
		};
		syncEnv.info.gaps = new ListBuffer<Integer>();
		genTry(tree.body, List.<JCCatch>nil(), syncEnv);
		code.endScopes(limit);
		
		DEBUG.P(0,this,"visitSynchronized(1)");
    }