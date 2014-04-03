    /** Find an unqualified identifier which matches a specified kind set.
     *  @param env       The current environment.
     *  @param name      The indentifier's name.
     *  @param kind      Indicates the possible symbol kinds
     *                   (a subset of VAL, TYP, PCK).
     */
    Symbol findIdent(Env<AttrContext> env, Name name, int kind) {
    	try {
    	DEBUG.P(this,"findIdent(3)");
    	DEBUG.P("name="+name);
    	DEBUG.P("kind="+Kinds.toString(kind));
    	DEBUG.P("syms.packages.size="+syms.packages.size()+" keySet="+syms.packages.keySet());
    	
        Symbol bestSoFar = typeNotFound; //kind=ABSENT_TYP是Kinds类中定义的最大值
        Symbol sym;

        if ((kind & VAR) != 0) {
            sym = findVar(env, name);
            if (sym.exists()) return sym;
            else if (sym.kind < bestSoFar.kind) bestSoFar = sym;
        }

        if ((kind & TYP) != 0) {
            sym = findType(env, name);
            DEBUG.P("sym="+sym);
            DEBUG.P("sym.exists()="+sym.exists());
            DEBUG.P("sym.kind="+sym.kind+" "+Kinds.toString(sym.kind));
            DEBUG.P("bestSoFar.kind="+bestSoFar.kind+" "+Kinds.toString(bestSoFar.kind));
            
            if (sym.exists()) return sym;
            else if (sym.kind < bestSoFar.kind) bestSoFar = sym;
        }
        
        //如果是一个不存在的包名也同样加入syms.packages中
		DEBUG.P("((kind & PCK) != 0)="+((kind & PCK) != 0));
        if ((kind & PCK) != 0) return reader.enterPackage(name);
        else return bestSoFar;
        
    	}finally{
    	DEBUG.P("syms.packages.size="+syms.packages.size()+" keySet="+syms.packages.keySet());
    	DEBUG.P(0,this,"findIdent(3)");
    	}
    }