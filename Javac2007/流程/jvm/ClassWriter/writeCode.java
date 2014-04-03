    /** Write code attribute of method.
     */
    void writeCode(Code code) {
    	DEBUG.P(this,"writeCode((1)");
    	
        databuf.appendChar(code.max_stack);
        databuf.appendChar(code.max_locals);
        databuf.appendInt(code.cp);
        databuf.appendBytes(code.code, 0, code.cp);
        databuf.appendChar(code.catchInfo.length());
		//code.catchInfo对应exception_info表
		//每个char[]数组有四个元素:startPc, endPc, handlerPc, catchType
        for (List<char[]> l = code.catchInfo.toList();
             l.nonEmpty();
             l = l.tail) {
            for (int i = 0; i < l.head.length; i++)
                databuf.appendChar(l.head[i]);
        }
        int acountIdx = beginAttrs();
        int acount = 0;

        if (code.lineInfo.nonEmpty()) {
            int alenIdx = writeAttr(names.LineNumberTable);
            databuf.appendChar(code.lineInfo.length());
			//code.lineInfo对应line_number_info表
			//每个char[]数组有两个元素:startPc, lineNumber
            for (List<char[]> l = code.lineInfo.reverse();
                 l.nonEmpty();
                 l = l.tail)
                for (int i = 0; i < l.head.length; i++)
                    databuf.appendChar(l.head[i]);
            endAttr(alenIdx);
            acount++;
        }
        
        //"CharacterRangeTable"属性在
        //“The JavaTM Virtual Machine Specification Second Edition”中未定义
        if (genCrt && (code.crt != null)) {
            CRTable crt = code.crt;
            int alenIdx = writeAttr(names.CharacterRangeTable);
            int crtIdx = beginAttrs();
            int crtEntries = crt.writeCRT(databuf, code.lineMap, log);
            endAttrs(crtIdx, crtEntries);
            endAttr(alenIdx);
            acount++;
        }

        // counter for number of generic local variables
        int nGenericVars = 0;

        if (code.varBufferSize > 0) {
            int alenIdx = writeAttr(names.LocalVariableTable);
            databuf.appendChar(code.varBufferSize);

            for (int i=0; i<code.varBufferSize; i++) {
                Code.LocalVar var = code.varBuffer[i];

                // write variable info
                assert var.start_pc >= 0;
                assert var.start_pc <= code.cp;
                databuf.appendChar(var.start_pc);
                assert var.length >= 0;
                assert (var.start_pc + var.length) <= code.cp;
                databuf.appendChar(var.length);
                VarSymbol sym = var.sym;
                databuf.appendChar(pool.put(sym.name));
                Type vartype = sym.erasure(types);
                
                //泛型本地变量的sym.type与vartype(擦除后的类型)是不同的
                if (!types.isSameType(sym.type, vartype))
                    nGenericVars++;
                databuf.appendChar(pool.put(typeSig(vartype)));
                databuf.appendChar(var.reg);
            }
            endAttr(alenIdx);
            acount++;
        }
        
        //"LocalVariableTypeTable"属性在
        //“The JavaTM Virtual Machine Specification Second Edition”中未定义
        if (nGenericVars > 0) {
            int alenIdx = writeAttr(names.LocalVariableTypeTable);
            databuf.appendChar(nGenericVars);
            int count = 0;

            for (int i=0; i<code.varBufferSize; i++) {
                Code.LocalVar var = code.varBuffer[i];
                VarSymbol sym = var.sym;
                //泛型本地变量的sym.type与sym.erasure(types)是不同的
                if (types.isSameType(sym.type, sym.erasure(types)))
                    continue;
                count++;
                // write variable info
                databuf.appendChar(var.start_pc);
                databuf.appendChar(var.length);
                databuf.appendChar(pool.put(sym.name));
                
                //注意这里和上面的区别，这里是使用未擦除的类型
                databuf.appendChar(pool.put(typeSig(sym.type)));
                databuf.appendChar(var.reg);
            }
            assert count == nGenericVars;
            endAttr(alenIdx);
            acount++;
        }

        if (code.stackMapBufferSize > 0) {
            if (debugstackmap) System.out.println("Stack map for " + code.meth);
            int alenIdx = writeAttr(code.stackMap.getAttributeName(names));
            writeStackMap(code);
            endAttr(alenIdx);
            acount++;
        }
        endAttrs(acountIdx, acount);
        
        DEBUG.P(0,this,"writeCode((1)");
    }