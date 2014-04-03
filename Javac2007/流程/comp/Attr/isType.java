    /** Is this symbol a type?
     */
    static boolean isType(Symbol sym) {
        return sym != null && sym.kind == TYP;
    }