    public void visitTry(final JCTry tree) {
		DEBUG.P(this,"visitTry(1)");
		// Generate code for a try statement with given body and catch clauses,
		// in a new environment which calls the finally block if there is one.
		final Env<GenContext> tryEnv = env.dup(tree, new GenContext());
		final Env<GenContext> oldEnv = env;
		DEBUG.P("tryEnv="+tryEnv);
		DEBUG.P("oldEnv="+oldEnv);
		DEBUG.P("useJsrLocally="+useJsrLocally);
		DEBUG.P("stackMap="+stackMap);
		DEBUG.P("jsrlimit="+jsrlimit);
        if (!useJsrLocally) {
            useJsrLocally =
                (stackMap == StackMapFormat.NONE) &&
                (jsrlimit <= 0 ||
                jsrlimit < 100 &&
                estimateCodeComplexity(tree.finalizer)>jsrlimit);
        }
		DEBUG.P("useJsrLocally="+useJsrLocally);
		tryEnv.info.finalize = new GenFinalizer() {
			void gen() {
				DEBUG.P(this,"gen()");
				DEBUG.P("useJsrLocally="+useJsrLocally);
				if (useJsrLocally) {
					if (tree.finalizer != null) {
						Code.State jsrState = code.state.dup();
						jsrState.push(code.jsrReturnValue);
						tryEnv.info.cont =
							new Chain(code.emitJump(jsr),
								  tryEnv.info.cont,
								  jsrState);
					}
					assert tryEnv.info.gaps.length() % 2 == 0;
					tryEnv.info.gaps.append(code.curPc());
				} else {
					assert tryEnv.info.gaps.length() % 2 == 0;
					tryEnv.info.gaps.append(code.curPc());
					genLast();
				}
				DEBUG.P(0,this,"gen()");
			}
			void genLast() {
				DEBUG.P(this,"genLast()");
				if (tree.finalizer != null)
					genStat(tree.finalizer, oldEnv, CRT_BLOCK);
				DEBUG.P(0,this,"genLast()");
			}
			boolean hasFinalizer() {
				return tree.finalizer != null;
			}
		};
		tryEnv.info.gaps = new ListBuffer<Integer>();
		genTry(tree.body, tree.catchers, tryEnv);
		
		DEBUG.P(0,this,"visitTry(1)");
    }