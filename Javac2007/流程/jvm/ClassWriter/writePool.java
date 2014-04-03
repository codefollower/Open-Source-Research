    /** Write constant pool to pool buffer.
     *  Note: during writing, constant pool
     *  might grow since some parts of constants still need to be entered.
     */
    void writePool(Pool pool) throws PoolOverflow, StringOverflow {
		DEBUG.P(this,"writePool(1)");

        int poolCountIdx = poolbuf.length;
        poolbuf.appendChar(0);
        int i = 1;
        while (i < pool.pp) {
            Object value = pool.pool[i];
            assert value != null;
            if (value instanceof Pool.Method)
                value = ((Pool.Method)value).m;
            else if (value instanceof Pool.Variable)
                value = ((Pool.Variable)value).v;
            //参考<<深入java虚拟机>>P129 6.4池量池那一节
            if (value instanceof MethodSymbol) {
                MethodSymbol m = (MethodSymbol)value;
                //指tag，占1字节
                poolbuf.appendByte((m.owner.flags() & INTERFACE) != 0
                          ? CONSTANT_InterfaceMethodref
                          : CONSTANT_Methodref);
                //指class_index，指向常量池索引,占2字节
                poolbuf.appendChar(pool.put(m.owner));
                //指name_and_type_index，指向常量池索引,占2字节
                poolbuf.appendChar(pool.put(nameType(m)));
            } else if (value instanceof VarSymbol) {
                VarSymbol v = (VarSymbol)value;
                poolbuf.appendByte(CONSTANT_Fieldref);
                poolbuf.appendChar(pool.put(v.owner));
                poolbuf.appendChar(pool.put(nameType(v)));
            } else if (value instanceof Name) {
                poolbuf.appendByte(CONSTANT_Utf8);
                byte[] bs = ((Name)value).toUtf();
                poolbuf.appendChar(bs.length);
                poolbuf.appendBytes(bs, 0, bs.length);
                if (bs.length > Pool.MAX_STRING_LENGTH)
                    throw new StringOverflow(value.toString());
            } else if (value instanceof ClassSymbol) {
                ClassSymbol c = (ClassSymbol)value;
                if (c.owner.kind == TYP) pool.put(c.owner);
                poolbuf.appendByte(CONSTANT_Class);
                if (c.type.tag == ARRAY) {
                    poolbuf.appendChar(pool.put(typeSig(c.type)));
                } else {
                    poolbuf.appendChar(pool.put(names.fromUtf(externalize(c.flatname))));
                    enterInner(c);
                }
            } else if (value instanceof NameAndType) {
                NameAndType nt = (NameAndType)value;
                poolbuf.appendByte(CONSTANT_NameandType);
                poolbuf.appendChar(pool.put(nt.name));
                poolbuf.appendChar(pool.put(typeSig(nt.type)));
            } else if (value instanceof Integer) {
                poolbuf.appendByte(CONSTANT_Integer);
                poolbuf.appendInt(((Integer)value).intValue());
            } else if (value instanceof Long) {
                poolbuf.appendByte(CONSTANT_Long);
                poolbuf.appendLong(((Long)value).longValue());
                i++;
            } else if (value instanceof Float) {
                poolbuf.appendByte(CONSTANT_Float);
                poolbuf.appendFloat(((Float)value).floatValue());
            } else if (value instanceof Double) {
                poolbuf.appendByte(CONSTANT_Double);
                poolbuf.appendDouble(((Double)value).doubleValue());
                i++;
            } else if (value instanceof String) {
                poolbuf.appendByte(CONSTANT_String);
                poolbuf.appendChar(pool.put(names.fromString((String)value)));
            } else if (value instanceof Type) {
                Type type = (Type)value;
                if (type.tag == CLASS) enterInner((ClassSymbol)type.tsym);
                poolbuf.appendByte(CONSTANT_Class);
                poolbuf.appendChar(pool.put(xClassName(type)));
            } else {
                assert false : "writePool " + value;
            }
            i++;
        }
        if (pool.pp > Pool.MAX_ENTRIES)
            throw new PoolOverflow();
        putChar(poolbuf, poolCountIdx, pool.pp);

		DEBUG.P(0,this,"writePool(1)");
    }