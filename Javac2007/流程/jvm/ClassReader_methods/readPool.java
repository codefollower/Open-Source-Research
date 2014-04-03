    /** Read constant pool entry at start address i, use pool as a cache.
     */
    //注意:参数i不是constant pool entry的start address，而是constant pool entry
    //在constant pool的索引
    Object readPool(int i) {
		try {//我加上的
		DEBUG.P(this,"readPool(1)");

        Object result = poolObj[i];

		DEBUG.P("i="+i);
		DEBUG.P("result="+result);

        if (result != null) return result;

        int index = poolIdx[i];
        if (index == 0) return null;

        byte tag = buf[index];
		DEBUG.P("tag="+myTAG(tag));
        switch (tag) {
        case CONSTANT_Utf8:
            poolObj[i] = names.fromUtf(buf, index + 3, getChar(index + 1));
            break;
        case CONSTANT_Unicode:
            throw badClassFile("unicode.str.not.supported");
        case CONSTANT_Class:
            poolObj[i] = readClassOrType(getChar(index + 1));
            break;
        case CONSTANT_String:
            // FIXME: (footprint) do not use toString here
            poolObj[i] = readName(getChar(index + 1)).toString();
            break;
        case CONSTANT_Fieldref: {
            ClassSymbol owner = readClassSymbol(getChar(index + 1));
            NameAndType nt = (NameAndType)readPool(getChar(index + 3));
            poolObj[i] = new VarSymbol(0, nt.name, nt.type, owner);
            break;
        }
        case CONSTANT_Methodref:
        case CONSTANT_InterfaceMethodref: {
            ClassSymbol owner = readClassSymbol(getChar(index + 1));
            NameAndType nt = (NameAndType)readPool(getChar(index + 3));
            poolObj[i] = new MethodSymbol(0, nt.name, nt.type, owner);
            break;
        }
        case CONSTANT_NameandType:
            poolObj[i] = new NameAndType(
                readName(getChar(index + 1)),
                readType(getChar(index + 3)));
            break;
        case CONSTANT_Integer:
            poolObj[i] = getInt(index + 1);
            break;
        case CONSTANT_Float:
            poolObj[i] = new Float(getFloat(index + 1));
            break;
        case CONSTANT_Long:
            poolObj[i] = new Long(getLong(index + 1));
            break;
        case CONSTANT_Double:
            poolObj[i] = new Double(getDouble(index + 1));
            break;
        default:
            throw badClassFile("bad.const.pool.tag", Byte.toString(tag));
        }
		DEBUG.P("poolObj[i]="+poolObj[i]);
        return poolObj[i];

		}finally{//我加上的
		DEBUG.P(0,this,"readPool(1)");
		}
    }