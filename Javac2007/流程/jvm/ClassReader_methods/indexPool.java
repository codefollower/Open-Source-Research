/************************************************************************
 * Constant Pool Access
 ***********************************************************************/

    /** Index all constant pool entries, writing their start addresses into
     *  poolIdx.
     */
    void indexPool() {
    	try {//我加上的
		DEBUG.P(this,"indexPool()");
		
        poolIdx = new int[nextChar()];
        poolObj = new Object[poolIdx.length];
        
        DEBUG.P("poolIdx.length="+poolIdx.length);
        
        int i = 1;//常量池索引0保留不用
        while (i < poolIdx.length) {
            poolIdx[i++] = bp;
            byte tag = buf[bp++];
            //DEBUG.P("i="+(i-1)+" tag="+tag+" bp="+(bp-1));
            switch (tag) {
            case CONSTANT_Utf8: case CONSTANT_Unicode: {
                int len = nextChar();
                bp = bp + len;
                break;
            }
            case CONSTANT_Class:
            case CONSTANT_String:
                bp = bp + 2;
                break;
            case CONSTANT_Fieldref:
            case CONSTANT_Methodref:
            case CONSTANT_InterfaceMethodref:
            case CONSTANT_NameandType:
            case CONSTANT_Integer:
            case CONSTANT_Float:
                bp = bp + 4;
                break;
            case CONSTANT_Long:
            case CONSTANT_Double:
                bp = bp + 8;
                i++;
                break;
            default:
                throw badClassFile("bad.const.pool.tag.at",
                                   Byte.toString(tag),
                                   Integer.toString(bp -1));
            }
        }
        
        StringBuffer sb=new StringBuffer();
        for(int n:poolIdx) sb.append(n).append(" ");
        DEBUG.P("poolIdx="+sb.toString());
        
        }finally{//我加上的
		DEBUG.P(0,this,"indexPool()");
		}
    }
