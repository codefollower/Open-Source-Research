    /** Enter a set of annotations. */
    private void enterAnnotations(List<JCAnnotation> annotations,
                          Env<AttrContext> env,
                          Symbol s) {
        DEBUG.P(this,"enterAnnotations(3)");   
        DEBUG.P("annotations="+annotations);
        DEBUG.P("env="+env);
        DEBUG.P("s="+s);
        DEBUG.P("skipAnnotations="+skipAnnotations);
                       
        ListBuffer<Attribute.Compound> buf =
            new ListBuffer<Attribute.Compound>();
        Set<TypeSymbol> annotated = new HashSet<TypeSymbol>();
        if (!skipAnnotations)
        for (List<JCAnnotation> al = annotations; al.nonEmpty(); al = al.tail) {
            JCAnnotation a = al.head;
            DEBUG.P("a="+a);
            Attribute.Compound c = annotate.enterAnnotation(a,
                                                            syms.annotationType,
                                                            env);
            DEBUG.P("c="+c);
        
            if (c == null) continue;
            buf.append(c);
            // Note: @Deprecated has no effect on local variables and parameters
            if (!c.type.isErroneous()
                && s.owner.kind != MTH
                && types.isSameType(c.type, syms.deprecatedType))
                s.flags_field |= Flags.DEPRECATED;
            if (!annotated.add(a.type.tsym))
                log.error(a.pos, "duplicate.annotation");
        }
        s.attributes_field = buf.toList();
        
        DEBUG.P(0,this,"enterAnnotations(3)");  
    }
