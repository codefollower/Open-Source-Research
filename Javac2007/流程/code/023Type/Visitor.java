    /**
     * A visitor for types.  A visitor is used to implement operations
     * (or relations) on types.  Most common operations on types are
     * binary relations and this interface is designed for binary
     * relations, that is, operations on the form
     * Type&nbsp;&times;&nbsp;S&nbsp;&rarr;&nbsp;R.
     * <!-- In plain text: Type x S -> R -->
     *
     * @param <R> the return type of the operation implemented by this
     * visitor; use Void if no return type is needed.
     * @param <S> the type of the second argument (the first being the
     * type itself) of the operation implemented by this visitor; use
     * Void if a second argument is not needed.
     */
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