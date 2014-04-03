    /** Read signature and convert to type parameters.
     */
    List<Type> readTypeParams(int i) {
    	try {//我加上的
		DEBUG.P(this,"readTypeParams(1)");
		DEBUG.P("i="+i);
	//i是常量池索引，且对应tag是CONSTANT_Utf8类型	
        int index = poolIdx[i];
        
        DEBUG.P("index="+index);
        //getChar(index + 1)是字节长度
        //index+3表示signature的开始位置
        return sigToTypeParams(buf, index + 3, getChar(index + 1));
        
        }finally{//我加上的
		DEBUG.P(0,this,"readTypeParams(1)");
		}
    }

	/** Convert signature to type parameters, where signature is a name.
     */
    List<Type> sigToTypeParams(Name name) {
    	try {//我加上的
		DEBUG.P(this,"sigToTypeParams(1)");
		DEBUG.P("name="+name);

        return sigToTypeParams(name.table.names, name.index, name.len);
        
        }finally{//我加上的
		DEBUG.P(0,this,"sigToTypeParams(1)");
		}
    }

    /** Convert signature to type parameters, where signature is a byte
     *  array segment.
     */
    List<Type> sigToTypeParams(byte[] sig, int offset, int len) {
    	try {//我加上的
		DEBUG.P(this,"sigToTypeParams(3)");
		DEBUG.P("offset="+offset);
		DEBUG.P("len="+len);
		
        signature = sig;
        sigp = offset;
        siglimit = offset + len;
        return sigToTypeParams();
        
        }finally{//我加上的
		DEBUG.P(0,this,"sigToTypeParams(3)");
		}
    }

    /** Convert signature to type parameters, where signature is implicit.
     */
    List<Type> sigToTypeParams() {
    	DEBUG.P(this,"sigToTypeParams()");
		DEBUG.P("signature[sigp]="+(char)signature[sigp]);
    	
        List<Type> tvars = List.nil();
        if (signature[sigp] == '<') {
            sigp++;
            int start = sigp;
            sigEnterPhase = true;
            while (signature[sigp] != '>')
                tvars = tvars.prepend(sigToTypeParam());
            sigEnterPhase = false;
            sigp = start;

			DEBUG.P("signature[sigp]="+(char)signature[sigp]);
            while (signature[sigp] != '>')
                sigToTypeParam();
            sigp++;
        }
        
        DEBUG.P("tvars.reverse()="+tvars.reverse());
        DEBUG.P(0,this,"sigToTypeParams()");
        return tvars.reverse();
    }
