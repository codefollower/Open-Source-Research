    /** Represents VOID or NONE.
     */
    static class JCNoType extends Type implements NoType {
	public JCNoType(int tag) {
	    super(tag, null);
	}

	@Override
        public TypeKind getKind() {
	    switch (tag) {
	    case VOID:  return TypeKind.VOID;
	    case NONE:  return TypeKind.NONE;
            default:
		throw new AssertionError("Unexpected tag: " + tag);
	    }
        }

	@Override
        public <R, P> R accept(TypeVisitor<R, P> v, P p) {
            return v.visitNoType(this, p);
        }
    }







































