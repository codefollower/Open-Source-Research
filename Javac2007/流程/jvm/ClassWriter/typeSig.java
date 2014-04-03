    /** Return signature of given type
     */
    Name typeSig(Type type) {
    	DEBUG.P(this,"typeSig(Type type)");

        assert sigbuf.length == 0;
        //- System.out.println(" ? " + type);
        assembleSig(type);
        Name n = sigbuf.toName(names);
        sigbuf.reset();
        //- System.out.println("   " + n);
        
        DEBUG.P("return typeName="+n);
        DEBUG.P(0,this,"typeSig(Type type)");
        return n;
    }