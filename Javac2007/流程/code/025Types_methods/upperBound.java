	/*
	upperBound方法对应“<? extends Type>”，
	lowerBound方法对应“<? super Type>”，
	但是“<?>”不在上面的两个方法中处理。
	其他type直接返回

	如下源码:
	==========================================
	class ClassA {}
	class ClassB extends ClassA{}
	class ClassC<T extends ClassA> {}

	public class Test{
		void m222(ClassC<?>c,ClassC<? extends ClassB> c1,ClassC<? super ClassB> c2) {}
	}
	==========================================

	有如下的输出结果:
==========================================================================
com.sun.tools.javac.code.Types===>upperBound(Type t)
-------------------------------------------------------------------------
t=? extends my.test.ClassB{:my.test.ClassA:} t.tag=WILDCARD
com.sun.tools.javac.code.Types$1===>upperBound==>visitWildcardType(2)
-------------------------------------------------------------------------
t=? extends my.test.ClassB{:my.test.ClassA:} t.tag=WILDCARD
t.type=my.test.ClassB
t.kind=? extends 
t.bound=T22052786
t.bound.bound=my.test.ClassA
t.isSuperBound()=false
com.sun.tools.javac.code.Types$1===>upperBound==>visitWildcardType(2)  END
-------------------------------------------------------------------------

t=? extends my.test.ClassB{:my.test.ClassA:}  upperBound=my.test.ClassB
com.sun.tools.javac.code.Types===>upperBound(Type t)  END
-------------------------------------------------------------------------


com.sun.tools.javac.code.Types===>lowerBound(Type t)
-------------------------------------------------------------------------
t=? super my.test.ClassB{:my.test.ClassA:} t.tag=WILDCARD
com.sun.tools.javac.code.Types$2===>lowerBound==>visitWildcardType(2)
-------------------------------------------------------------------------
t=? super my.test.ClassB{:my.test.ClassA:} t.tag=WILDCARD
t.type=my.test.ClassB
t.kind=? super 
t.bound=T22052786
t.isExtendsBound()=false
com.sun.tools.javac.code.Types$2===>lowerBound==>visitWildcardType(2)  END
-------------------------------------------------------------------------

t=? super my.test.ClassB{:my.test.ClassA:}  lowerBound=my.test.ClassB
com.sun.tools.javac.code.Types===>lowerBound(Type t)  END
-------------------------------------------------------------------------
*/
    // <editor-fold defaultstate="collapsed" desc="upperBound">
    /**
     * The "rvalue conversion".<br>
     * The upper bound of most types is the type
     * itself.  Wildcards, on the other hand have upper
     * and lower bounds.
     * @param t a type
     * @return the upper bound of the given type
     */
    public Type upperBound(Type t) {
        //return upperBound.visit(t);
        
        DEBUG.P(this,"upperBound(Type t)");
        DEBUG.P("t="+t+" t.tag="+TypeTags.toString(t.tag));
        Type returnType=upperBound.visit(t);
        DEBUG.P("t="+t+"  upperBound="+returnType);
        DEBUG.P(1,this,"upperBound(Type t)");
        return returnType;
    }
    // where
        private final MapVisitor<Void> upperBound = new MapVisitor<Void>() {

            @Override
            public Type visitWildcardType(WildcardType t, Void ignored) {
				try {//我加上的
					DEBUG.P(this,"upperBound==>visitWildcardType(2)");
					DEBUG.P("t="+t+" t.tag="+TypeTags.toString(t.tag));
					DEBUG.P("t.type="+t.type);
					DEBUG.P("t.kind="+t.kind);
					DEBUG.P("t.bound="+t.bound);
					DEBUG.P("t.bound.bound="+t.bound.bound);
					DEBUG.P("t.isSuperBound()="+t.isSuperBound());
				//设: C extends B extends A
				//    D <T extends A> 
				//upperBound( D<? extends B> ) == B (isSuperBound()=false)
				//upperBound( D<?> ) == Object  (isSuperBound()=true)
				//upperBound( D<? super C> ) == A  (isSuperBound()=true)
                if (t.isSuperBound())
                    return t.bound == null ? syms.objectType : t.bound.bound;
                else
                    return visit(t.type);

			    }finally{//我加上的
					DEBUG.P(1,this,"upperBound==>visitWildcardType(2)");
				}
            }

            @Override
            public Type visitCapturedType(CapturedType t, Void ignored) {
                return visit(t.bound);
            }
        };
    // </editor-fold>

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
        DEBUG.P("t="+t+"  lowerBound="+returnType);
        DEBUG.P(1,this,"lowerBound(Type t)");
        return returnType;
    }
    // where
        private final MapVisitor<Void> lowerBound = new MapVisitor<Void>() {

            @Override
            public Type visitWildcardType(WildcardType t, Void ignored) {
				try {//我加上的
					DEBUG.P(this,"lowerBound==>visitWildcardType(2)");
					DEBUG.P("t="+t+" t.tag="+TypeTags.toString(t.tag));
					DEBUG.P("t.type="+t.type);
					DEBUG.P("t.kind="+t.kind);
					DEBUG.P("t.bound="+t.bound);
					DEBUG.P("t.isExtendsBound()="+t.isExtendsBound());

				//设: C extends B extends A
				//    D <T extends A> 
				//lowerBound( D<? super B> ) == B (isExtendsBound()=false)
				//lowerBound( D<?> ) == null  (isExtendsBound()=true)
				//lowerBound( D<? extends C> ) == null  (isExtendsBound()=true)
                return t.isExtendsBound() ? syms.botType : visit(t.type);

				}finally{//我加上的
					DEBUG.P(1,this,"lowerBound==>visitWildcardType(2)");
				}
            }

            @Override
            public Type visitCapturedType(CapturedType t, Void ignored) {
				return visit(t.getLowerBound());
            }
        };
    // </editor-fold>