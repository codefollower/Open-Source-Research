    /** Set the current variable defined state. */
    public void setDefined(Bits newDefined) {
		if (alive && newDefined != state.defined) {
			Bits diff = state.defined.dup().xorSet(newDefined);
			for (int adr = diff.nextBit(0);
			 adr >= 0;
			 adr = diff.nextBit(adr+1)) {
			if (adr >= nextreg)
				state.defined.excl(adr);
			else if (state.defined.isMember(adr))
				setUndefined(adr);
			else
				setDefined(adr);
			}
		}
    }

    /** Mark a register as being (possibly) defined. */
    public void setDefined(int adr) {
		DEBUG.P(this,"setDefined(int adr)");
		DEBUG.P("adr="+adr);
		LocalVar v = lvar[adr];
		DEBUG.P("LocalVar v="+v);
		DEBUG.P("cp="+cp);

		DEBUG.P("");
		DEBUG.P("state.defined.excl前="+state.defined);
		
		if (v == null) {
			state.defined.excl(adr);
		} else {
			state.defined.incl(adr);
			if (cp < Character.MAX_VALUE) {
				if (v.start_pc == Character.MAX_VALUE)
					v.start_pc = (char)cp;
			}
		}

		DEBUG.P("state.defined.excl后="+state.defined);
		DEBUG.P("LocalVar v="+v);
		DEBUG.P(1,this,"setDefined(int adr)");
    }