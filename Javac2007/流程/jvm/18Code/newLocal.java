    /** Create a new local variable address and return it.
     */
    private int newLocal(int typecode) {
		int reg = nextreg;
		int w = width(typecode);//double和long类型的变量在局部变量数组中也占两项
		nextreg = reg + w;
		if (nextreg > max_locals) max_locals = nextreg;
		return reg;
    }

    private int newLocal(Type type) {
		return newLocal(typecode(type));
    }

    public int newLocal(VarSymbol v) {
		DEBUG.P(this,"newLocal(VarSymbol v)");
		DEBUG.P("v="+v+" v.adr="+v.adr+" nextreg="+nextreg+" max_locals="+max_locals);
		
		int reg = v.adr = newLocal(v.erasure(types));
		addLocalVar(v);
		
		DEBUG.P("v="+v+" v.adr="+v.adr+" nextreg="+nextreg+" max_locals="+max_locals);
		DEBUG.P(1,this,"newLocal(VarSymbol v)");
		return reg;
    }