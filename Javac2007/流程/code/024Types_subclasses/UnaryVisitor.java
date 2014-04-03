/**
     * A convenience visitor for implementing operations that only
     * require one argument (the type itself), that is, unary
     * operations.
     *
     * @param <R> the return type of the operation implemented by this
     * visitor; use Void if no return type is needed.
     */
    public static abstract class UnaryVisitor<R> extends SimpleVisitor<R,Void> {
        final public R visit(Type t) { return t.accept(this, null); }
    }







































