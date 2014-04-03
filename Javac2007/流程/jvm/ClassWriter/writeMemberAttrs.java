    /** Write member (field or method) attributes;
     *  return number of attributes written.
     */
    int writeMemberAttrs(Symbol sym) {
    	DEBUG.P(this,"writeMemberAttrs(Symbol sym)");
        DEBUG.P("sym="+sym);
        if(sym.type!=null) {
            DEBUG.P("sym.type="+sym.type);
            DEBUG.P("sym.erasure(types)="+sym.erasure(types));
            DEBUG.P("sym.type.getThrownTypes()="+sym.type.getThrownTypes());
        }
        
        int acount = writeFlagAttrs(sym.flags());
        long flags = sym.flags();
        DEBUG.P("flags="+Flags.toString(flags));
        if (source.allowGenerics() &&
            (flags & (SYNTHETIC|BRIDGE)) != SYNTHETIC &&
            (flags & ANONCONSTR) == 0 &&
            (!types.isSameType(sym.type, sym.erasure(types)) ||
             hasTypeVar(sym.type.getThrownTypes()))) {
            // <editor-fold defaultstate="collapsed">
            /*样例:private T t;
            输出:
            com.sun.tools.javac.jvm.ClassWriter===>writeMemberAttrs(Symbol sym)
            -------------------------------------------------------------------------
            sym=t
            sym.type=T
            sym.erasure(types)=java.lang.Object
            sym.type.getThrownTypes()=
            com.sun.tools.javac.jvm.ClassWriter===>writeFlagAttrs(long flags)
            -------------------------------------------------------------------------
            flags=0x2 private 
            acount=0
            com.sun.tools.javac.jvm.ClassWriter===>writeFlagAttrs(long flags)  END
            -------------------------------------------------------------------------

            flags=0x2 private 
            com.sun.tools.javac.jvm.ClassWriter===>writeAttr(Name attrName)
            -------------------------------------------------------------------------
            attrName=Signature
            alenIdx=188
            com.sun.tools.javac.jvm.ClassWriter===>writeAttr(Name attrName)  END
            -------------------------------------------------------------------------
            */
            // </editor-fold>
            // note that a local class with captured variables
            // will get a signature attribute
            int alenIdx = writeAttr(names.Signature);
            databuf.appendChar(pool.put(typeSig(sym.type)));
            endAttr(alenIdx);
            acount++;
        }
        acount += writeJavaAnnotations(sym.getAnnotationMirrors());
        DEBUG.P("acount="+acount);
        DEBUG.P(0,this,"writeMemberAttrs(Symbol sym)");
        return acount;
    }