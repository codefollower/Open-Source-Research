/******************************************************************
 * Writing Attributes
 ******************************************************************/

    /** Write header for an attribute to data buffer and return
     *  position past attribute length index.
     */
    int writeAttr(Name attrName) {
    	DEBUG.P(this,"writeAttr(Name attrName)");
		DEBUG.P("attrName="+attrName);
		
        databuf.appendChar(pool.put(attrName));
        //指attribute_length，占4字节
        databuf.appendInt(0);//先初始为0，以后再回填
        
		DEBUG.P("alenIdx="+databuf.length);//属性长度索引
		DEBUG.P(0,this,"writeAttr(Name attrName)");

        return databuf.length;
    }