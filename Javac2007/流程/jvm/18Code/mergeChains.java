    /** Merge the jumps in of two chains into one.
     */
    public static Chain mergeChains(Chain chain1, Chain chain2) {
		try {//我加上的
		DEBUG.P(Code.class,"mergeChains(2)");
		DEBUG.P("chain1="+chain1);
		DEBUG.P("chain2="+chain2);

		// recursive merge sort
        if (chain2 == null) return chain1;
        if (chain1 == null) return chain2;
		assert
			chain1.state.stacksize == chain2.state.stacksize &&
			chain1.state.nlocks == chain2.state.nlocks;
	    
	    //按指令码偏移量(pc)从大到小的顺序合并两个链
        if (chain1.pc < chain2.pc)
            return new Chain(
                chain2.pc,
                mergeChains(chain1, chain2.next),
                chain2.state);
        return new Chain(
                chain1.pc,
                mergeChains(chain1.next, chain2),
                chain1.state);

		}finally{//我加上的
		DEBUG.P(0,Code.class,"mergeChains(2)");
		}
    }