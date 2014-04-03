    /** Generate code to load an integer constant.
     *  @param n     The integer to be loaded.
     */
    void loadIntConst(int n) {
		DEBUG.P(this,"loadIntConst(1)");
		DEBUG.P("n="+n);

        items.makeImmediateItem(syms.intType, n).load();

		DEBUG.P(0,this,"loadIntConst(1)");
    }