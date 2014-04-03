    /** Given a symbol, return its name-and-type.
     */
    NameAndType nameType(Symbol sym) {
        return new NameAndType(fieldName(sym),
                               retrofit
                               ? sym.erasure(types)
                               : sym.externalType(types));
        // if we retrofit, then the NameAndType has been read in as is
        // and no change is necessary. If we compile normally, the
        // NameAndType is generated from a symbol reference, and the
        // adjustment of adding an additional this$n parameter needs to be made.
    }