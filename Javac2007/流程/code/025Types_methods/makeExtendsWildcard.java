    /**
     * Create a wildcard with the given upper (extends) bound; create
     * an unbounded wildcard if bound is Object.
     *
     * @param bound the upper bound
     * @param formal the formal type parameter that will be
     * substituted by the wildcard
     */
    private WildcardType makeExtendsWildcard(Type bound, TypeVar formal) {
        if (bound == syms.objectType) {
            return new WildcardType(syms.objectType,
                                    BoundKind.UNBOUND,
                                    syms.boundClass,
                                    formal);
        } else {
            return new WildcardType(bound,
                                    BoundKind.EXTENDS,
                                    syms.boundClass,
                                    formal);
        }
    }