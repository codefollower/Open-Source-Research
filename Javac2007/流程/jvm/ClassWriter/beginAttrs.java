    /** Leave space for attribute count and return index for
     *  number of attributes field.
     */
    int beginAttrs() {
    	//属性个数先初始为0，返回在databuf中的索引，等找出所有属性后，
    	//再根据索引修改成实际的属性个数
        databuf.appendChar(0);

		//属性个数(arrtibutes_count)实际占两字节
		//在回填时，索引位置得往下一2(见endAttrs方法)
        return databuf.length;
    }