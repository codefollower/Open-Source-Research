    /** Read name.
     */
    Name readName(int i) {
    	try {//我加上的
		DEBUG.P(this,"readName(1)");
		
        return (Name) (readPool(i));
        
        }finally{//我加上的
		DEBUG.P(0,this,"readName(1)");
		}
    }