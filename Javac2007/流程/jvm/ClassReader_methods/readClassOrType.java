    /** If name is an array type or class signature, return the
     *  corresponding type; otherwise return a ClassSymbol with given name.
     */
    Object readClassOrType(int i) {
    	try {//我加上的
		DEBUG.P(this,"readClassOrType(1)");
		DEBUG.P("i="+i);

        int index =  poolIdx[i];
        int len = getChar(index + 1);
        int start = index + 3;
        
        DEBUG.P("index="+index);
        DEBUG.P("len="+len);
        DEBUG.P("start="+start);
        DEBUG.P("buf[start]="+(char)buf[start]);
        DEBUG.P("buf[start + len - 1]="+(char)buf[start + len - 1]);
        
        assert buf[start] == '[' || buf[start + len - 1] != ';';
        // by the above assertion, the following test can be
        // simplified to (buf[start] == '[')
        return (buf[start] == '[' || buf[start + len - 1] == ';')
            ? (Object)sigToType(buf, start, len)
            : (Object)enterClass(names.fromUtf(internalize(buf, start,
                                                           len)));
        }finally{//我加上的
		DEBUG.P(0,this,"readClassOrType(1)");
		}                                                                                                
    }