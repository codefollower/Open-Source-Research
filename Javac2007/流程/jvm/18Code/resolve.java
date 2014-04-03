    /** Resolve chain to point to given target.
     */
    public void resolve(Chain chain, int target) {
		DEBUG.P(this,"resolve(2)");
		DEBUG.P("chain="+chain);
		DEBUG.P("target="+target);
		DEBUG.P("cp="+cp);
		DEBUG.P("fatcode="+fatcode);

		boolean changed = false;
		State newState = state;
		for (; chain != null; chain = chain.next) {
			assert state != chain.state;
			assert target > chain.pc || state.stacksize == 0;
				
			if (target >= cp) {
				target = cp;
			} else if (get1(target) == goto_) {
				if (fatcode) target = target + get4(target + 1);
				else target = target + get2(target + 1);
			}

			DEBUG.P("get1(chain.pc)="+mnem(get1(chain.pc)));
			DEBUG.P("chain.pc + 3="+(chain.pc + 3));
			DEBUG.P("target="+target);
			DEBUG.P("cp="+cp);
			DEBUG.P("fixedPc="+fixedPc);
			if (get1(chain.pc) == goto_ &&
					chain.pc + 3 == target && target == cp && !fixedPc) {
					// If goto the next instruction, the jump is not needed: 
					// compact the code.
					cp = cp - 3;
					target = target - 3;
					if (chain.next == null) {
						// This is the only jump to the target. Exit the loop 
						// without setting new state. The code is reachable 
						// from the instruction before goto_.
						alive = true;
						break;
					}
			} else {
				if (fatcode)
					put4(chain.pc + 1, target - chain.pc);
				else if (target - chain.pc < Short.MIN_VALUE ||
							target - chain.pc > Short.MAX_VALUE)
					fatcode = true;
				else
					//注意这里是相对于指令位置的偏移量，不要与用javap工具反编译后的
					//结果相混淆
					put2(chain.pc + 1, target - chain.pc);

				assert !alive ||
					chain.state.stacksize == newState.stacksize &&
					chain.state.nlocks == newState.nlocks;
			}
				
			fixedPc = true;
			if (cp == target) {
				changed = true;
				if (debugCode)
					System.err.println("resolving chain state=" + chain.state);
				if (alive) {
					newState = chain.state.join(newState);
				} else {
					newState = chain.state;
					alive = true;
				}
			}
		}
		assert !changed || state != newState;
		if (state != newState) {
			setDefined(newState.defined);
			state = newState;
			pendingStackMap = needStackMap;
		}

		DEBUG.P(0,this,"resolve(2)");
    }

    /** Resolve chain to point to current code pointer.
     */
    public void resolve(Chain chain) {
		DEBUG.P(this,"resolve(1)");
		DEBUG.P("alive="+alive);
		DEBUG.P("chain="+chain);
		DEBUG.P("pendingJumps前="+pendingJumps);

		assert
			!alive ||
			chain==null ||
			state.stacksize == chain.state.stacksize &&
			state.nlocks == chain.state.nlocks;
		pendingJumps = mergeChains(chain, pendingJumps);

		DEBUG.P("pendingJumps后="+pendingJumps);
		DEBUG.P(0,this,"resolve(1)");
    }