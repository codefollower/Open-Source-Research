    /** Write class `c' to outstream `out'.
     */
    public void writeClassFile(OutputStream out, ClassSymbol c)
        throws IOException, PoolOverflow, StringOverflow {
        DEBUG.P(this,"writeClassFile(OutputStream out, ClassSymbol c)");
        assert (c.flags() & COMPOUND) == 0;
        databuf.reset();
        poolbuf.reset();
        sigbuf.reset();
        pool = c.pool;
        innerClasses = null;
        innerClassesQueue = null;

        Type supertype = types.supertype(c.type);
        List<Type> interfaces = types.interfaces(c.type);
        List<Type> typarams = c.type.getTypeArguments();

		DEBUG.P("supertype="+supertype+"  supertype.tag="+TypeTags.toString(supertype.tag));
		DEBUG.P("interfaces="+interfaces);
		DEBUG.P("typarams="+typarams);

        int flags = adjustFlags(c.flags());
        if ((flags & PROTECTED) != 0) flags |= PUBLIC;
        flags = flags & ClassFlags & ~STRICTFP;
        if ((flags & INTERFACE) == 0) flags |= ACC_SUPER;
        if (dumpClassModifiers) {
            log.errWriter.println();
            log.errWriter.println("CLASSFILE  " + c.getQualifiedName());
            log.errWriter.println("---" + flagNames(flags));
        }
        DEBUG.P("flagNames="+flagNames(flags));
		DEBUG.P("flags ="+Flags.toString(flags));
        //参考<<深入java虚拟机>>P122
        databuf.appendChar(flags);//指access_flags 占两字节
        //指this_class,指向常量池索引，占两字节
        databuf.appendChar(pool.put(c));
        //指super_class,指向常量池索引或0，占两字节
        databuf.appendChar(supertype.tag == CLASS ? pool.put(supertype.tsym) : 0);
        //指interfaces_count,占两字节
        databuf.appendChar(interfaces.length());
        for (List<Type> l = interfaces; l.nonEmpty(); l = l.tail)
        //指interfaces,指向常量池索引，占两字节
            databuf.appendChar(pool.put(l.head.tsym));
        int fieldsCount = 0;
        int methodsCount = 0;
		DEBUG.P("c.members()="+c.members());
        for (Scope.Entry e = c.members().elems; e != null; e = e.sibling) {
            switch (e.sym.kind) {
            case VAR: fieldsCount++; break;
            //HYPOTHETICAL看Flags中的注释
            case MTH: if ((e.sym.flags() & HYPOTHETICAL) == 0) methodsCount++;
                      break;
            case TYP: enterInner((ClassSymbol)e.sym); break;
            default : assert false;
            }
        }
		DEBUG.P("fieldsCount ="+fieldsCount);
		DEBUG.P("methodsCount="+methodsCount);
        //指fields_count,占两字节
        databuf.appendChar(fieldsCount);
        //指fields
        writeFields(c.members().elems);
        //指methods_count,占两字节
        databuf.appendChar(methodsCount);
        //指methods
        writeMethods(c.members().elems);

        int acountIdx = beginAttrs();
        int acount = 0;
        
        DEBUG.P("acountIdx="+acountIdx);
        
		//是否是泛型类或泛型接口
        boolean sigReq =
            typarams.length() != 0 || supertype.getTypeArguments().length() != 0;
        for (List<Type> l = interfaces; !sigReq && l.nonEmpty(); l = l.tail)
            sigReq = l.head.getTypeArguments().length() != 0;
        
        DEBUG.P("sigReq="+sigReq);
        
        if (sigReq) {
			DEBUG.P("sigbuf.toName(names)前="+sigbuf.toName(names));
            assert source.allowGenerics();
            int alenIdx = writeAttr(names.Signature);
            if (typarams.length() != 0) assembleParamsSig(typarams);
            assembleSig(supertype);
            for (List<Type> l = interfaces; l.nonEmpty(); l = l.tail)
                assembleSig(l.head);
            databuf.appendChar(pool.put(sigbuf.toName(names)));
			DEBUG.P("sigbuf.toName(names)后="+sigbuf.toName(names));
            sigbuf.reset();
            endAttr(alenIdx);
            acount++;
        }
        
        DEBUG.P("c.sourcefile="+c.sourcefile);
        DEBUG.P("emitSourceFile="+emitSourceFile);
		DEBUG.P("");

        if (c.sourcefile != null && emitSourceFile) {
            int alenIdx = writeAttr(names.SourceFile);
            // WHM 6/29/1999: Strip file path prefix.  We do it here at
            // the last possible moment because the sourcefile may be used
            // elsewhere in error diagnostics. Fixes 4241573.
            //databuf.appendChar(c.pool.put(c.sourcefile));
            String filename = c.sourcefile.toString();
			DEBUG.P("filename="+filename);

            int sepIdx = filename.lastIndexOf(File.separatorChar);
			DEBUG.P("sepIdx="+sepIdx);

            // Allow '/' as separator on all platforms, e.g., on Win32.
            int slashIdx = filename.lastIndexOf('/');
			DEBUG.P("slashIdx="+slashIdx);

            if (slashIdx > sepIdx) sepIdx = slashIdx;
			DEBUG.P("sepIdx="+sepIdx);

            if (sepIdx >= 0) filename = filename.substring(sepIdx + 1);
			DEBUG.P("filename="+filename);

            databuf.appendChar(c.pool.put(names.fromString(filename)));
            endAttr(alenIdx);
            acount++;
        }
        
        DEBUG.P("genCrt="+genCrt);
        
        if (genCrt) {
            // Append SourceID attribute
            int alenIdx = writeAttr(names.SourceID);
            databuf.appendChar(c.pool.put(names.fromString(Long.toString(getLastModified(c.sourcefile)))));
            endAttr(alenIdx);
            acount++;
            // Append CompilationID attribute
            alenIdx = writeAttr(names.CompilationID);
            databuf.appendChar(c.pool.put(names.fromString(Long.toString(System.currentTimeMillis()))));
            endAttr(alenIdx);
            acount++;
        }

        acount += writeFlagAttrs(c.flags());
        acount += writeJavaAnnotations(c.getAnnotationMirrors());
        acount += writeEnclosingMethodAttribute(c);

        poolbuf.appendInt(JAVA_MAGIC);
        poolbuf.appendChar(target.minorVersion);
        poolbuf.appendChar(target.majorVersion);

        writePool(c.pool);

        if (innerClasses != null) {
            writeInnerClasses();
            acount++;
        }
        endAttrs(acountIdx, acount);

        poolbuf.appendBytes(databuf.elems, 0, databuf.length);
        out.write(poolbuf.elems, 0, poolbuf.length);

        pool = c.pool = null; // to conserve space
        DEBUG.P(0,this,"writeClassFile(OutputStream out, ClassSymbol c)");
     }