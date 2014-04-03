    /** Write field symbol, entering all references into constant pool.
     */
    void writeField(VarSymbol v) {
    	DEBUG.P(this,"writeField(VarSymbol v)");
		DEBUG.P("v="+v);
		DEBUG.P("v.flags()="+Flags.toString(v.flags()));
        int flags = adjustFlags(v.flags());
		DEBUG.P("flags="+Flags.toString(flags));
        //DEBUG.P("v="+v+" flagNames="+flagNames(v.flags()));

        databuf.appendChar(flags);
        if (dumpFieldModifiers) {
            log.errWriter.println("FIELD  " + fieldName(v));
            log.errWriter.println("---" + flagNames(v.flags()));
        }
        databuf.appendChar(pool.put(fieldName(v)));
        databuf.appendChar(pool.put(typeSig(v.erasure(types))));
        int acountIdx = beginAttrs();
        int acount = 0;
		DEBUG.P("v.getConstValue()="+v.getConstValue());
		//有STATIC FINAL标志的字段才有“ConstantValue”属性
        if (v.getConstValue() != null) {
            int alenIdx = writeAttr(names.ConstantValue);
            databuf.appendChar(pool.put(v.getConstValue()));
            endAttr(alenIdx);
            acount++;
        }
        acount += writeMemberAttrs(v);
        endAttrs(acountIdx, acount);
        
        DEBUG.P(0,this,"writeField(VarSymbol v)");
    }