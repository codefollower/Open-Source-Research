    static class BottomType extends Type implements NullType {
	public BottomType() {
	    super(TypeTags.BOT, null);
	}

	@Override
        public TypeKind getKind() {
            return TypeKind.NULL;
        }

	@Override
        public <R, P> R accept(TypeVisitor<R, P> v, P p) {
            return v.visitNull(this, p);
        }
	
	@Override
	public Type constType(Object value) {
	    return this;
	}
	
	@Override 
	public String stringValue() {
	    return "null";
	}
    }