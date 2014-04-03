    /** A captured type variable comes from wildcards which can have
     *  both upper and lower bound.  CapturedType extends TypeVar with
     *  a lower bound.
     */
    public static class CapturedType extends TypeVar {

        public Type lower;
        public WildcardType wildcard;

        public CapturedType(Name name,
			    Symbol owner,
			    Type upper,
			    Type lower,
			    WildcardType wildcard) {
            super(name, owner);
            assert lower != null;
            
            DEBUG.P(this,"CapturedType(5)");
            
            this.bound = upper;
            this.lower = lower;
	    	this.wildcard = wildcard;
	    	
	    	DEBUG.P("name="+name);
	    	DEBUG.P("owner="+owner);
	    	DEBUG.P("upper="+upper);
	    	DEBUG.P("lower="+lower);
	    	DEBUG.P("wildcard="+wildcard);
	    	DEBUG.P("toString()="+toString());
	    	DEBUG.P(0,this,"CapturedType(5)");
        }

        @Override
        public <R,S> R accept(Type.Visitor<R,S> v, S s) {
            return v.visitCapturedType(this, s);
        }

        public Type getLowerBound() {
            return lower;
        }

	@Override
	public String toString() {
            return "capture#"
		+ (hashCode() & 0xFFFFFFFFL) % PRIME
		+ " of "
		+ wildcard;
        }
	static final int PRIME = 997;  // largest prime less than 1000
    }