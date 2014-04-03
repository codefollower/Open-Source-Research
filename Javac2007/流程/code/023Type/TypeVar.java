    public static class TypeVar extends Type implements TypeVariable {

        /** The bound of this type variable; set from outside.
         *  Must be nonempty once it is set.
         *  For a bound, `bound' is the bound type itself.
         *  Multiple bounds are expressed as a single class type which has the
         *  individual bounds as superclass, respectively interfaces.
         *  The class type then has as `tsym' a compiler generated class `c',
         *  which has a flag COMPOUND and whose owner is the type variable
         *  itself. Furthermore, the erasure_field of the class
         *  points to the first class or interface bound.
         */
        public Type bound = null;
        
        //name:类型变量名,owner是指定义类型变量的类、接口、方法、构造函数
        public TypeVar(Name name, Symbol owner) {
            super(TYPEVAR, null);
            tsym = new TypeSymbol(0, name, this, owner);
        }

        public TypeVar(TypeSymbol tsym, Type bound) {
            super(TYPEVAR, tsym);
            this.bound = bound;
        }

        @Override
        public <R,S> R accept(Type.Visitor<R,S> v, S s) {
            return v.visitTypeVar(this, s);
        }

        public Type getUpperBound() { return bound; }

        int rank_field = -1;

        public Type getLowerBound() {
            return Symtab.botType;
        }

        public TypeKind getKind() {
            return TypeKind.TYPEVAR;
        }

        public <R, P> R accept(TypeVisitor<R, P> v, P p) {
            return v.visitTypeVariable(this, p);
        }
        
        //我加上的
        public String toString() {
        	return tsym+"{ bound="+bound+" }";
        }
    }