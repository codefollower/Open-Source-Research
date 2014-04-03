    /**
     * A default visitor for types.  All visitor methods except
     * visitType are implemented by delegating to visitType.  Concrete
     * subclasses must provide an implementation of visitType and can
     * override other methods as needed.
     *
     * @param <R> the return type of the operation implemented by this
     * visitor; use Void if no return type is needed.
     * @param <S> the type of the second argument (the first being the
     * type itself) of the operation implemented by this visitor; use
     * Void if a second argument is not needed.
     */
    public static abstract class DefaultTypeVisitor<R,S> implements Type.Visitor<R,S> {
        //t.accep方法具有多态性，会根据Type类的不同子类间接调用相应的visitXXX方法
		final public R visit(Type t, S s)               { return t.accept(this, s); }
        public R visitClassType(ClassType t, S s)       { return visitType(t, s); }
        public R visitWildcardType(WildcardType t, S s) { return visitType(t, s); }
        public R visitArrayType(ArrayType t, S s)       { return visitType(t, s); }
        public R visitMethodType(MethodType t, S s)     { return visitType(t, s); }
        public R visitPackageType(PackageType t, S s)   { return visitType(t, s); }
        public R visitTypeVar(TypeVar t, S s)           { return visitType(t, s); }
        public R visitCapturedType(CapturedType t, S s) { return visitType(t, s); }
        public R visitForAll(ForAll t, S s)             { return visitType(t, s); }
        public R visitUndetVar(UndetVar t, S s)         { return visitType(t, s); }
        public R visitErrorType(ErrorType t, S s)       { return visitType(t, s); }
    }

    public interface Visitor<R,S> {
        R visitClassType(ClassType t, S s);
        R visitWildcardType(WildcardType t, S s);
        R visitArrayType(ArrayType t, S s);
        R visitMethodType(MethodType t, S s);
        R visitPackageType(PackageType t, S s);
        R visitTypeVar(TypeVar t, S s);
        R visitCapturedType(CapturedType t, S s);
        R visitForAll(ForAll t, S s);
        R visitUndetVar(UndetVar t, S s);
        R visitErrorType(ErrorType t, S s);
        R visitType(Type t, S s);
    }
