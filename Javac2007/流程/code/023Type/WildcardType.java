    //参见Types类的upperBound方法中的注释
    public static class WildcardType extends Type
            implements javax.lang.model.type.WildcardType {

        public Type type;
        public BoundKind kind;
        public TypeVar bound;

		/*例如:
		class ClassA{}
		class ClassB extends ClassA{}
		public class Test<T extends ClassA>{
			void method(Test<? extends ClassB> t) {}
		}
		---------------------------------------------

		WildcardType=<? extends ClassB>
		type=ClassB //如果是<?>，那么type=java.lang.Object
		kind=? extends
		bound=T
		*/

        @Override
        public <R,S> R accept(Type.Visitor<R,S> v, S s) {
            return v.visitWildcardType(this, s);
        }
        
        public WildcardType(Type type, BoundKind kind, TypeSymbol tsym) {
            super(WILDCARD, tsym);
            DEBUG.P(this,"WildcardType(3)");
            assert(type != null);
            this.kind = kind;
            this.type = type;
            
            DEBUG.P("type="+type);
            DEBUG.P("kind="+kind);
            DEBUG.P("tsym="+tsym);
            DEBUG.P("bound="+bound);
            DEBUG.P("toString()="+toString());
            DEBUG.P(0,this,"WildcardType(3)");
            //DEBUG.P(this,"WildcardType(3)="+toString());
        }
        public WildcardType(WildcardType t, TypeVar bound) {
            this(t.type, t.kind, t.tsym, bound);
            
            DEBUG.P(this,"WildcardType(2)="+toString());
        }

        public WildcardType(Type type, BoundKind kind, TypeSymbol tsym, TypeVar bound) {
            this(type, kind, tsym);
            this.bound = bound;
            DEBUG.P(this,"WildcardType(4)="+toString());
        }

        public boolean isSuperBound() {
            return kind == SUPER ||
                kind == UNBOUND;
        }
        public boolean isExtendsBound() {
            return kind == EXTENDS ||
                kind == UNBOUND;
        }
        public boolean isUnbound() {
            return kind == UNBOUND;
        }

		//如果WildcardType的bound为null或不是给定的t，则重新将bound设为t
        //也就是把类型变量与WildcardType绑定
        public Type withTypeVar(Type t) {
        	try {//我加上的
			DEBUG.P(this,"withTypeVar(Type t)");
			DEBUG.P("bound="+bound);
			DEBUG.P("t    ="+t);
			
            //-System.err.println(this+".withTypeVar("+t+");");//DEBUG
            if (bound == t)
                return this;
            bound = (TypeVar)t;
            return this;
            
            }finally{//我加上的
            DEBUG.P("");
            DEBUG.P("泛型类形参："+bound);
            DEBUG.P("泛型类实参："+this);
			DEBUG.P(1,this,"withTypeVar(Type t)");
			}
        }

        boolean isPrintingBound = false;
        public String toString() {
            StringBuffer s = new StringBuffer();
            s.append(kind.toString());
            if (kind != UNBOUND)
                s.append(type);
            if (moreInfo && bound != null && !isPrintingBound)
                try {
                    isPrintingBound = true;
                    s.append("{:").append(bound.bound).append(":}");
                } finally {
                    isPrintingBound = false;
                }
            return s.toString();
        }

        public Type map(Mapping f) {
            //- System.err.println("   (" + this + ").map(" + f + ")");//DEBUG
            Type t = type;
            if (t != null)
                t = f.apply(t);
            if (t == type)
                return this;
            else
                return new WildcardType(t, kind, tsym, bound);
        }
        
        //对于泛型类定义如:  Test<A,B,C>
        //对应的参数化类型:  Test<?, ? super Integer, ? extends Long>
        //将返回:?、Integer、Long
        public Type removeBounds() {
        	try {//我加上的
			DEBUG.P(this,"removeBounds()");
			DEBUG.P("isUnbound()="+isUnbound());
			DEBUG.P("this="+toString());
			DEBUG.P("type="+type);
			
            return isUnbound() ? this : type;

			}finally{//我加上的
			DEBUG.P(0,this,"removeBounds()");
			}
        }

        public Type getExtendsBound() {
            if (kind == EXTENDS)
                return type;
            else
                return null;
        }

        public Type getSuperBound() {
            if (kind == SUPER)
                return type;
            else
                return null;
        }

        public TypeKind getKind() {
            return TypeKind.WILDCARD;
        }

        public <R, P> R accept(TypeVisitor<R, P> v, P p) {
            return v.visitWildcard(this, p);
        }
    }