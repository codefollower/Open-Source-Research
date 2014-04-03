    /** Emit an invokeinterface instruction.
     */
    public void emitInvokeinterface(int meth, Type mtype) {
		try {//我加上的
		DEBUG.P(this,"emitInvokeinterface(int meth, Type mtype)");
		DEBUG.P("meth="+meth+" mtype="+mtype);
		
		int argsize = width(mtype.getParameterTypes());
		emitop(invokeinterface);
        if (!alive) return;
		emit2(meth);//无符号16位常量池索引
		emit1(argsize + 1);//参数(包括this)字长总数
		emit1(0);//0是invokeinterface指令的占位符，固定不变
		state.pop(argsize + 1);//这里加1与上面不同，这里是因为要弹出对象引用而加1
		
		//<<深入JAVA虚拟机>>第404-409页有区别，这里还要push返回值,而书上的堆栈是空的
		state.push(mtype.getReturnType());
		
		}finally{//我加上的
		DEBUG.P(0,this,"emitInvokeinterface(int meth, Type mtype)");
		}
    }

    /** Emit an invokespecial instruction.
     */
    //invokespecial指令格式是“invokespecial 16位常量池索引”
    public void emitInvokespecial(int meth, Type mtype) {
		try {//我加上的
		DEBUG.P(this,"emitInvokespecial(int meth, Type mtype)");
		DEBUG.P("meth="+meth+" mtype="+mtype);
		
		int argsize = width(mtype.getParameterTypes());
		emitop(invokespecial);//对应invokespecial字节码
        if (!alive) return;
		emit2(meth);//对应16位常量池索引字节码
		Symbol sym = (Symbol)pool.pool[meth];
		state.pop(argsize);
		if (sym.isConstructor())
			state.markInitialized((UninitializedType)state.peek());
		state.pop(1);
		state.push(mtype.getReturnType());
		
		}finally{//我加上的
		DEBUG.P(0,this,"emitInvokespecial(int meth, Type mtype)");
		}
    }

    /** Emit an invokestatic instruction.
     */
    public void emitInvokestatic(int meth, Type mtype) {
		try {//我加上的
		DEBUG.P(this,"emitInvokestatic(int meth, Type mtype)");
		DEBUG.P("meth="+meth+" mtype="+mtype);
		
		int argsize = width(mtype.getParameterTypes());
		emitop(invokestatic);
        if (!alive) return;
		emit2(meth);
		state.pop(argsize);
		state.push(mtype.getReturnType());
		
		}finally{//我加上的
		DEBUG.P(0,this,"emitInvokestatic(int meth, Type mtype)");
		}
    }

    /** Emit an invokevirtual instruction.
     */
    public void emitInvokevirtual(int meth, Type mtype) {
		try {//我加上的
		DEBUG.P(this,"emitInvokevirtual(int meth, Type mtype)");
		DEBUG.P("meth="+meth+" mtype="+mtype);
		
		int argsize = width(mtype.getParameterTypes());
		emitop(invokevirtual);
		if (!alive) return;
		emit2(meth);
		state.pop(argsize + 1);
		state.push(mtype.getReturnType());
		
		}finally{//我加上的
		DEBUG.P(0,this,"emitInvokevirtual(int meth, Type mtype)");
		}
    }