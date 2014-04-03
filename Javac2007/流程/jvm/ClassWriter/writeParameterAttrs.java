    /** Write method parameter annotations;
     *  return number of attributes written.
     */
    int writeParameterAttrs(MethodSymbol m) {
		DEBUG.P(this,"writeParameterAttrs(1)");
		DEBUG.P("m="+m);

        boolean hasVisible = false;
        boolean hasInvisible = false;
        if (m.params != null) for (VarSymbol s : m.params) {
            for (Attribute.Compound a : s.getAnnotationMirrors()) {
                switch (getRetention(a.type.tsym)) {
                case SOURCE: break;
                case CLASS: hasInvisible = true; break;
                case RUNTIME: hasVisible = true; break;
                default: ;// /* fail soft */ throw new AssertionError(vis);
                }
            }
        }

        int attrCount = 0;
		DEBUG.P("hasVisible="+hasVisible);
        if (hasVisible) {
            int attrIndex = writeAttr(names.RuntimeVisibleParameterAnnotations);
            databuf.appendByte(m.params.length());
            for (VarSymbol s : m.params) {
                ListBuffer<Attribute.Compound> buf = new ListBuffer<Attribute.Compound>();
                for (Attribute.Compound a : s.getAnnotationMirrors())
                    if (getRetention(a.type.tsym) == RetentionPolicy.RUNTIME)
                        buf.append(a);
                databuf.appendChar(buf.length());
                for (Attribute.Compound a : buf)
                    writeCompoundAttribute(a);
            }
            endAttr(attrIndex);
            attrCount++;
        }

		DEBUG.P("attrCount="+attrCount);
		DEBUG.P("hasInvisible="+hasInvisible);
        if (hasInvisible) {
            int attrIndex = writeAttr(names.RuntimeInvisibleParameterAnnotations);
            databuf.appendByte(m.params.length());
            for (VarSymbol s : m.params) {
                ListBuffer<Attribute.Compound> buf = new ListBuffer<Attribute.Compound>();
                for (Attribute.Compound a : s.getAnnotationMirrors())
                    if (getRetention(a.type.tsym) == RetentionPolicy.CLASS)
                        buf.append(a);
                databuf.appendChar(buf.length());
                for (Attribute.Compound a : buf)
                    writeCompoundAttribute(a);
            }
            endAttr(attrIndex);
            attrCount++;
        }

		DEBUG.P("attrCount="+attrCount);
		DEBUG.P(0,this,"writeParameterAttrs(1)");
        return attrCount;
    }