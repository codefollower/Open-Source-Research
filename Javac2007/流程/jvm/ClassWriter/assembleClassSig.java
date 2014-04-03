    void assembleClassSig(Type type) {
		DEBUG.P(this,"assembleClassSig(Type type)");
		DEBUG.P("type="+type+" type.tag="+TypeTags.toString(type.tag));

        ClassType ct = (ClassType)type;
        ClassSymbol c = (ClassSymbol)ct.tsym;
        enterInner(c);
        Type outer = ct.getEnclosingType();

		DEBUG.P("outer="+outer+" outer.tag="+TypeTags.toString(outer.tag));
		DEBUG.P("outer.allparams()="+outer.allparams());
        if (outer.allparams().nonEmpty()) {
            boolean rawOuter =
                c.owner.kind == MTH || // either a local class
                c.name == names.empty; // or anonymous
			DEBUG.P("rawOuter="+rawOuter);
            assembleClassSig(rawOuter
                             ? types.erasure(outer)
                             : outer);
            sigbuf.appendByte('.');
            assert c.flatname.startsWith(c.owner.enclClass().flatname);
            sigbuf.appendName(rawOuter
                              ? c.flatname.subName(c.owner.enclClass()
                                                   .flatname.len+1,
                                                   c.flatname.len)
                              : c.name);
        } else {
            sigbuf.appendBytes(externalize(c.flatname));
        }
        if (ct.getTypeArguments().nonEmpty()) {
            sigbuf.appendByte('<');
            assembleSig(ct.getTypeArguments());
            sigbuf.appendByte('>');
        }

		DEBUG.P(0,this,"assembleClassSig(Type type)");
    }