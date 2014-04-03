    /** Given a type t, return the extended class name of its erasure in
     *  external representation.
     */
    public Name xClassName(Type t) {
        if (t.tag == CLASS) {
            return names.fromUtf(externalize(t.tsym.flatName()));
        } else if (t.tag == ARRAY) {
            return typeSig(types.erasure(t));
        } else {
            throw new AssertionError("xClassName");
        }
    }