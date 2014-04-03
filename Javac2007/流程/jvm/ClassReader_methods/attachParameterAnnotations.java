    /** Attach parameter annotations.
     */
    void attachParameterAnnotations(final Symbol method) {
        final MethodSymbol meth = (MethodSymbol)method;
        int numParameters = buf[bp++] & 0xFF;
        List<VarSymbol> parameters = meth.params();
        int pnum = 0;
        while (parameters.tail != null) {
            attachAnnotations(parameters.head);
            parameters = parameters.tail;
            pnum++;
        }
        if (pnum != numParameters) {
            throw badClassFile("bad.runtime.invisible.param.annotations", meth);
        }
    }