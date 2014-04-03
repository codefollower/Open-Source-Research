    /** Write Java-language annotations; return number of JVM
     *  attributes written (zero or one).
     */
    
    int writeJavaAnnotations(List<Attribute.Compound> attrs) {
        try {//我加上的
        DEBUG.P(this,"writeJavaAnnotations(1)");
        DEBUG.P("attrs.isEmpty()="+attrs.isEmpty());
        
        if (attrs.isEmpty()) return 0;

        DEBUG.P("attrs="+attrs);

        ListBuffer<Attribute.Compound> visibles = new ListBuffer<Attribute.Compound>();
        ListBuffer<Attribute.Compound> invisibles = new ListBuffer<Attribute.Compound>();
        for (Attribute.Compound a : attrs) {
            switch (getRetention(a.type.tsym)) {
            case SOURCE: break;
            case CLASS: invisibles.append(a); break;
            case RUNTIME: visibles.append(a); break;
            default: ;// /* fail soft */ throw new AssertionError(vis);
            }
        }

        DEBUG.P("visibles.length()="+visibles.length());

        int attrCount = 0;
        if (visibles.length() != 0) {
            /*表示：
            RuntimeVisibleAnnotations {
                u2 attribute_name_index;
                u4 attribute_length;
                u2 annotations_count;
                annotation_info annotations[annotations_count];
            }
            */
            int attrIndex = writeAttr(names.RuntimeVisibleAnnotations);
            databuf.appendChar(visibles.length());
            for (Attribute.Compound a : visibles)
                writeCompoundAttribute(a);
            endAttr(attrIndex);
            attrCount++;
        }

        DEBUG.P("attrCount="+attrCount);
        DEBUG.P("invisibles.length()="+invisibles.length());

        if (invisibles.length() != 0) {
            /*表示：
            RuntimeInvisibleAnnotations {
                u2 attribute_name_index;
                u4 attribute_length;
                u2 annotations_count;
                annotation_info annotations[annotations_count];
            }
            */
            int attrIndex = writeAttr(names.RuntimeInvisibleAnnotations);
            databuf.appendChar(invisibles.length());
            for (Attribute.Compound a : invisibles)
                writeCompoundAttribute(a);
            endAttr(attrIndex);
            attrCount++;
        }

        DEBUG.P("attrCount="+attrCount);
        return attrCount;

        }finally {//我加上的
        DEBUG.P(0,this,"writeJavaAnnotations(1)");
        }
    }