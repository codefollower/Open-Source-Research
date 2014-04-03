    /** A mirror of java.lang.annotation.RetentionPolicy. */
    enum RetentionPolicy {
        SOURCE,
        CLASS,
        RUNTIME
    }

    RetentionPolicy getRetention(TypeSymbol annotationType) {
        DEBUG.P(this,"getRetention(1)");
        DEBUG.P("annotationType="+annotationType);
        RetentionPolicy vis = RetentionPolicy.CLASS; // the default
        Attribute.Compound c = annotationType.attribute(syms.retentionType.tsym);
        
        DEBUG.P("c="+c);
        if (c != null) {
            Attribute value = c.member(names.value);
            DEBUG.P("value="+value);
            if (value != null && value instanceof Attribute.Enum) {
                Name levelName = ((Attribute.Enum)value).value.name;
                if (levelName == names.SOURCE) vis = RetentionPolicy.SOURCE;
                else if (levelName == names.CLASS) vis = RetentionPolicy.CLASS;
                else if (levelName == names.RUNTIME) vis = RetentionPolicy.RUNTIME;
                else ;// /* fail soft */ throw new AssertionError(levelName);
            }
        }
        
        DEBUG.P("vis="+vis);
        DEBUG.P(0,this,"getRetention(1)");
        return vis;
    }