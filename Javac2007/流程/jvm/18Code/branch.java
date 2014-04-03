    /** Emit a branch with given opcode; return its chain.
     *  branch differs from jump in that jsr is treated as no-op.
     */
    public Chain branch(int opcode) {
		try {//我加上的
		DEBUG.P(this,"branch(1)");
		DEBUG.P("opcode="+mnem(opcode));
		DEBUG.P("pendingJumps="+pendingJumps);
		DEBUG.P("(opcode != dontgoto)="+(opcode != dontgoto));
		
		Chain result = null;
		if (opcode == goto_) {
			result = pendingJumps;
			pendingJumps = null;
		}
		
		//dontgoto就是jsr指令    
		if (opcode != dontgoto && isAlive()) {
			result = new Chain(emitJump(opcode),
					   result,
					   state.dup());
			fixedPc = fatcode;
			if (opcode == goto_) alive = false;
		}

		DEBUG.P("result="+result);
		DEBUG.P("pendingJumps="+pendingJumps);
		DEBUG.P("fixedPc="+fixedPc);
		DEBUG.P("alive="+alive);

		return result;
		
		}finally{//我加上的
		DEBUG.P(0,this,"branch(1)");
		}
    }