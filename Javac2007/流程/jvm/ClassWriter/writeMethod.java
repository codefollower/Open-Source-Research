    /** Write method symbol, entering all references into constant pool.
     */
    void writeMethod(MethodSymbol m) {
		DEBUG.P(this,"writeMethod(1)");
		DEBUG.P("m="+m);
		DEBUG.P("m.flags()="+Flags.toString(m.flags()));
        int flags = adjustFlags(m.flags());
		DEBUG.P("flags="+Flags.toString(flags));
		//DEBUG.P("m="+m+" flagNames="+flagNames(m.flags()));

        databuf.appendChar(flags);
        if (dumpMethodModifiers) {
            log.errWriter.println("METHOD  " + fieldName(m));
            log.errWriter.println("---" + flagNames(m.flags()));
        }
        databuf.appendChar(pool.put(fieldName(m)));
        databuf.appendChar(pool.put(typeSig(m.externalType(types))));
        int acountIdx = beginAttrs();
        int acount = 0;
        if (m.code != null) {
            int alenIdx = writeAttr(names.Code);
            writeCode(m.code);
            m.code = null; // to conserve space
            endAttr(alenIdx);
            acount++;
        }
        List<Type> thrown = m.erasure(types).getThrownTypes();

		DEBUG.P("thrown="+thrown);
        if (thrown.nonEmpty()) {
            int alenIdx = writeAttr(names.Exceptions);
            databuf.appendChar(thrown.length());
            for (List<Type> l = thrown; l.nonEmpty(); l = l.tail)
                databuf.appendChar(pool.put(l.head.tsym));
            endAttr(alenIdx);
            acount++;
        }

		DEBUG.P("m.defaultValue="+m.defaultValue);
        if (m.defaultValue != null) {
            int alenIdx = writeAttr(names.AnnotationDefault);
            m.defaultValue.accept(awriter);
            endAttr(alenIdx);
            acount++;
        }
        acount += writeMemberAttrs(m);
        acount += writeParameterAttrs(m);
        endAttrs(acountIdx, acount);

		DEBUG.P(0,this,"writeMethod(1)");
    }