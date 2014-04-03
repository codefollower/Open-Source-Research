    /** Read class attribute.
     */
    void readClassAttr(ClassSymbol c, Name attrName, int attrLen) {
    	DEBUG.P(this,"readClassAttr(3)");
		DEBUG.P("c="+c);
		DEBUG.P("attrName="+attrName);
		DEBUG.P("attrLen="+attrLen);
		
        if (attrName == names.SourceFile) {
            Name n = readName(nextChar());
            c.sourcefile = new SourceFileObject(n);
        } else if (attrName == names.InnerClasses) {
            readInnerClasses(c);
        } else if (allowGenerics && attrName == names.Signature) {
            readingClassAttr = true;
            try {
                ClassType ct1 = (ClassType)c.type;
                assert c == currentOwner;
                ct1.typarams_field = readTypeParams(nextChar());
                ct1.supertype_field = sigToType();
                ListBuffer<Type> is = new ListBuffer<Type>();
                while (sigp != siglimit) is.append(sigToType());
                ct1.interfaces_field = is.toList();
            } finally {
                readingClassAttr = false;
            }
        } else {
            readMemberAttr(c, attrName, attrLen);
        }
        
        DEBUG.P(0,this,"readClassAttr(3)");
    }
    private boolean readingClassAttr = false;
    private List<Type> missingTypeVariables = List.nil();
    private List<Type> foundTypeVariables = List.nil();

    /** Read class attributes.
     */
    void readClassAttrs(ClassSymbol c) {
		DEBUG.P(this,"readClassAttrs(1)");
		DEBUG.P("c="+c);

        char ac = nextChar();
		DEBUG.P("ac="+(int)ac);
        for (int i = 0; i < ac; i++) {
            Name attrName = readName(nextChar());
			DEBUG.P("attrName="+attrName);
            int attrLen = nextInt();
            readClassAttr(c, attrName, attrLen);
        }

		DEBUG.P(0,this,"readClassAttrs(1)");
    }