    /**
     * A visitor for implementing a mapping from types to types.  The
     * default behavior of this class is to implement the identity
     * mapping (mapping a type to itself).  This can be overridden in
     * subclasses.
     *
     * @param <S> the type of the second argument (the first being the
     * type itself) of this mapping; use Void if a second argument is
     * not needed.
     */
    public static class MapVisitor<S> extends DefaultTypeVisitor<Type,S> {
        final public Type visit(Type t) { return t.accept(this, null); }
        public Type visitType(Type t, S s) { return t; }
    }