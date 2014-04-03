    /** Read contents of a given class symbol `c'. Both external and internal
     *  versions of an inner class are read.
     */
    void readClass(ClassSymbol c) {
    	try {//我加上的
		DEBUG.P(this,"readClass(1)");

        ClassType ct = (ClassType)c.type;

        // allocate scope for members
        c.members_field = new Scope(c);

        // prepare type variable table
        typevars = typevars.dup(currentOwner);
        
        DEBUG.P("c="+c);
        DEBUG.P("ct="+ct);
        DEBUG.P("c.members_field="+c.members_field);
        DEBUG.P("currentOwner="+currentOwner);
        DEBUG.P("typevars="+typevars);
        DEBUG.P("ct.getEnclosingType()="+ct.getEnclosingType());
        DEBUG.P("ct.getEnclosingType().tag="+TypeTags.toString(ct.getEnclosingType().tag));
        
        if (ct.getEnclosingType().tag == CLASS) enterTypevars(ct.getEnclosingType());

        // read flags, or skip if this is an inner class
        long flags = adjustClassFlags(nextChar());
        DEBUG.P("");
        DEBUG.P("flags="+Flags.toString(flags));
        DEBUG.P("c.owner="+c.owner);
        DEBUG.P("c.owner.kind="+Kinds.toString(c.owner.kind));
        if (c.owner.kind == PCK) c.flags_field = flags;

        // read own class name and check that it matches
        ClassSymbol self = readClassSymbol(nextChar());
        DEBUG.P("self="+self);
        if (c != self)
            throw badClassFile("class.file.wrong.class",
                               self.flatname);

        // class attributes must be read before class
        // skip ahead to read class attributes
        int startbp = bp;
        nextChar();
        char interfaceCount = nextChar();
        bp += interfaceCount * 2;
        char fieldCount = nextChar();
        for (int i = 0; i < fieldCount; i++) skipMember();
        char methodCount = nextChar();
        for (int i = 0; i < methodCount; i++) skipMember();
        readClassAttrs(c);

		DEBUG.P("readAllOfClassFile="+readAllOfClassFile);
        if (readAllOfClassFile) {
            for (int i = 1; i < poolObj.length; i++) readPool(i);
            c.pool = new Pool(poolObj.length, poolObj);
        }

        // reset and read rest of classinfo
        bp = startbp;
        int n = nextChar();
		DEBUG.P("n="+n);
		DEBUG.P("ct.supertype_field="+ct.supertype_field);
        if (ct.supertype_field == null)
            ct.supertype_field = (n == 0)
                ? Type.noType
                : readClassSymbol(n).erasure(types);
		DEBUG.P("ct.supertype_field="+ct.supertype_field);
        n = nextChar();
        List<Type> is = List.nil();
        for (int i = 0; i < n; i++) {
            Type _inter = readClassSymbol(nextChar()).erasure(types);
            is = is.prepend(_inter);
        }
        if (ct.interfaces_field == null)
            ct.interfaces_field = is.reverse();

        if (fieldCount != nextChar()) assert false;
        for (int i = 0; i < fieldCount; i++) enterMember(c, readField());
        if (methodCount != nextChar()) assert false;
        for (int i = 0; i < methodCount; i++) enterMember(c, readMethod());

        typevars = typevars.leave();
        
        }finally{//我加上的
		DEBUG.P(0,this,"readClass(1)");
		}
    }