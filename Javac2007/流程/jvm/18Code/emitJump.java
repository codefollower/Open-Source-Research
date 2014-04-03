    /** Emit a jump instruction.
     *  Return code pointer of instruction to be patched.
     */
    public int emitJump(int opcode) {
		try {//我加上的
		DEBUG.P(this,"emitJump(1)");
		DEBUG.P("opcode="+mnem(opcode));
		DEBUG.P("fatcode="+fatcode);
		
		if (fatcode) {
			if (opcode == goto_ || opcode == jsr) {
				//goto_转换成goto_w，jsr转换成jsr_w，采用4个字节的偏移量
				emitop4(opcode + goto_w - goto_, 0);
			} else {
				emitop2(negate(opcode), 8);
				emitop4(goto_w, 0);
				alive = true;
				pendingStackMap = needStackMap;
			}
			return cp - 5;
		} else {
			emitop2(opcode, 0);//先置0，之后会在resolve(2)方法中回填
			//保存指令位置(因为emitop2(opcode, 0)往code数组中放入3个字节
			//后cp还多加了1，所以cp-3相当于回退到存放指令码的索引位置)
			return cp - 3;
		}
		
		}finally{//我加上的
		DEBUG.P(0,this,"emitJump(1)");
		}
    }