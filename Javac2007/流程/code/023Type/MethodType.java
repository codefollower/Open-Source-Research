    //注意:MethodType并不包含TypeParameter信息
    //包含TypeParameter信息的MethodType用ForAll代替(参看MemberEnter)
    public static class MethodType extends Type
                    implements Cloneable, ExecutableType {

        public List<Type> argtypes;//方法参数的类型
        public Type restype;
        public List<Type> thrown;

        public MethodType(List<Type> argtypes,
                          Type restype,
                          List<Type> thrown,
                          TypeSymbol methodClass) {
            super(METHOD, methodClass);
            this.argtypes = argtypes;
            this.restype = restype;
            this.thrown = thrown;
        }

        @Override
        public <R,S> R accept(Type.Visitor<R,S> v, S s) {
            return v.visitMethodType(this, s);
        }

        /** The Java source which this type represents.
         *
         *  XXX 06/09/99 iris This isn't correct Java syntax, but it probably
         *  should be.
         */
        public String toString() {
            //return "(" + argtypes + ")" + restype;
			
			//我加上的
			if(tsym != null) return tsym.name+"(" + argtypes + ")" + restype;
			else return "(" + argtypes + ")" + restype;
        }

        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof MethodType))
                return false;
            MethodType m = (MethodType)obj;
            List<Type> args1 = argtypes;
            List<Type> args2 = m.argtypes;
            while (!args1.isEmpty() && !args2.isEmpty()) {
                if (!args1.head.equals(args2.head))
                    return false;
                args1 = args1.tail;
                args2 = args2.tail;
            }
            if (!args1.isEmpty() || !args2.isEmpty())
                return false;
            return restype.equals(m.restype);
        }

        public int hashCode() {
            int h = METHOD;
            for (List<Type> thisargs = this.argtypes;
                 thisargs.tail != null; /*inlined: thisargs.nonEmpty()*/
                 thisargs = thisargs.tail)
                h = (h << 5) + thisargs.head.hashCode();
            return (h << 5) + this.restype.hashCode();
        }

        public List<Type>        getParameterTypes() { return argtypes; }
        public Type              getReturnType()     { return restype; }
        public List<Type>        getThrownTypes()    { return thrown; }

        public void setThrown(List<Type> t) {
            thrown = t;
        }

        public boolean isErroneous() {
            return
                isErroneous(argtypes) ||
                restype != null && restype.isErroneous();
        }

        public Type map(Mapping f) {
        	try {//我加上的
			DEBUG.P(this,"map(Mapping f)");
			DEBUG.P("f="+f);
			
            List<Type> argtypes1 = map(argtypes, f);
            Type restype1 = f.apply(restype);
            List<Type> thrown1 = map(thrown, f);
            if (argtypes1 == argtypes &&
                restype1 == restype &&
                thrown1 == thrown) return this;
            else return new MethodType(argtypes1, restype1, thrown1, tsym);
            
            }finally{//我加上的
			DEBUG.P(0,this,"map(Mapping f)");
			}
        }

        public boolean contains(Type elem) {
            return elem == this || contains(argtypes, elem) || restype.contains(elem);
        }

        public MethodType asMethodType() { return this; }

        public void complete() {
            for (List<Type> l = argtypes; l.nonEmpty(); l = l.tail)
                l.head.complete();
            restype.complete();
            for (List<Type> l = thrown; l.nonEmpty(); l = l.tail)
                l.head.complete();
        }

        public List<TypeVar> getTypeVariables() {
            return List.nil();
        }

	public TypeSymbol asElement() {
	    return null;
	}

        public TypeKind getKind() {
            return TypeKind.EXECUTABLE;
        }

        public <R, P> R accept(TypeVisitor<R, P> v, P p) {
            return v.visitExecutable(this, p);
        }
    }