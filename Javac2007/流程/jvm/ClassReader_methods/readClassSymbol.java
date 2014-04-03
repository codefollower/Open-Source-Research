    /** Read class entry.
     */
    ClassSymbol readClassSymbol(int i) {
		try {//我加上的
		DEBUG.P(this,"readClassSymbol(int i)");
		DEBUG.P("i="+i);

        return (ClassSymbol) (readPool(i));

		}finally{//我加上的
		DEBUG.P(0,this,"readClassSymbol(int i)");
		}
    }