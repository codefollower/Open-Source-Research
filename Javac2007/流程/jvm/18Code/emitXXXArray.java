    /** Emit a multinewarray instruction.
     */
    public void emitMultianewarray(int ndims, int type, Type arrayType) {
		try {//我加上的
		DEBUG.P(this,"emitMultianewarray(3)");
		DEBUG.P("ndims="+ndims);
		DEBUG.P("type="+type);
		DEBUG.P("arrayType="+arrayType);

		emitop(multianewarray);
        if (!alive) return;
		emit2(type);//无符号16位常量池索引(int type这个参数的命名让人费解，也许是用type表示常量池中存放的数组元素类型)
		emit1(ndims);//数组维数
		state.pop(ndims);//从堆栈弹出ndims个字长，每个字长的值代表数组每一维的宽度
		state.push(arrayType);//将arrayType压入堆栈

		}finally{//我加上的
		DEBUG.P(0,this,"emitMultianewarray(3)");
		}
    }

    /** Emit newarray.
     */
    public void emitNewarray(int elemcode, Type arrayType) {
		try {//我加上的
		DEBUG.P(this,"emitNewarray(2)");
		DEBUG.P("elemcode="+elemcode);
		DEBUG.P("arrayType="+arrayType);

		emitop(newarray);
		if (!alive) return;
		emit1(elemcode);//数组元素类型(对应arraycode方法的返回值)
		state.pop(1); // count 数组元素个数
		state.push(arrayType);

		}finally{//我加上的
		DEBUG.P(0,this,"emitNewarray(2)");
		}
    }

    /** Emit anewarray.
     */
    //分配一个数组元素类型为引用类型的数组
    public void emitAnewarray(int od, Type arrayType) {
		try {//我加上的
		DEBUG.P(this,"emitAnewarray(2)");
		DEBUG.P("od="+od);
		DEBUG.P("arrayType="+arrayType);

        emitop(anewarray);
		if (!alive) return;
		emit2(od);//无符号16位常量池索引
		state.pop(1);
		state.push(arrayType);

		}finally{//我加上的
		DEBUG.P(0,this,"emitAnewarray(2)");
		}
    }