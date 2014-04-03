//lowerBound
    // <editor-fold defaultstate="collapsed" desc="lowerBound">
    /**
     * The "lvalue conversion".<br>
     * The lower bound of most types is the type
     * itself.  Wildcards, on the other hand have upper
     * and lower bounds.
     * @param t a type
     * @return the lower bound of the given type
     */
    public Type lowerBound(Type t) {
        //return lowerBound.visit(t);
        
        
        DEBUG.P(this,"lowerBound(Type t)");
        DEBUG.P("t="+t+" t.tag="+TypeTags.toString(t.tag));
        Type returnType=lowerBound.visit(t);
        //DEBUG.P("t="+t+"  lowerBound="+returnType);
		DEBUG.P("lowerBound="+returnType);
        DEBUG.P(1,this,"lowerBound(Type t)");
        return returnType;
    }
    // where
        private final MapVisitor<Void> lowerBound = new MapVisitor<Void>() {

            @Override
            public Type visitWildcardType(WildcardType t, Void ignored) {
				//try {//我加上的
				/*
					DEBUG.P(this,"lowerBound==>visitWildcardType(2)");
					DEBUG.P("t="+t+" t.tag="+TypeTags.toString(t.tag));
					DEBUG.P("t.type="+t.type);
					DEBUG.P("t.kind="+t.kind);
					DEBUG.P("t.bound="+t.bound);
					DEBUG.P("t.isExtendsBound()="+t.isExtendsBound());
				*/

				//设: C extends B extends A
				//    D <T extends A> 
				//lowerBound( D<? super B> ) == B (isExtendsBound()=false)
				//lowerBound( D<?> ) == null  (isExtendsBound()=true)
				//lowerBound( D<? extends C> ) == null  (isExtendsBound()=true)
                return t.isExtendsBound() ? syms.botType : visit(t.type);

				//}finally{//我加上的
				//	DEBUG.P(1,this,"lowerBound==>visitWildcardType(2)");
				//}
            }

            @Override
            public Type visitCapturedType(CapturedType t, Void ignored) {
				return visit(t.getLowerBound());
            }
        };
    // </editor-fold>
//