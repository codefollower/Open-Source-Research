    /** A class for class symbols
     */
    public static class ClassSymbol extends TypeSymbol implements TypeElement {

        /** a scope for all class members; variables, methods and inner classes
         *  type parameters are not part of this scope
         */
        public Scope members_field;

        /** the fully qualified name of the class, i.e. pck.outer.inner.
         *  null for anonymous classes
         */
        public Name fullname;

        /** the fully qualified name of the class after converting to flat
         *  representation, i.e. pck.outer$inner,
         *  set externally for local and anonymous classes
         */
        public Name flatname;

        /** the sourcefile where the class came from
         */
        public JavaFileObject sourcefile;

        /** the classfile from where to load this class
         *  this will have extension .class or .java
         */
        public JavaFileObject classfile;

        /** the constant pool of the class
         */
        public Pool pool;
        
        //ClassSymbol的kind是TYP
        public ClassSymbol(long flags, Name name, Type type, Symbol owner) {
            super(flags, name, type, owner);
            this.members_field = null;
            this.fullname = formFullName(name, owner);
            this.flatname = formFlatName(name, owner);
            this.sourcefile = null;
            this.classfile = null;
            this.pool = null;
        }

        public ClassSymbol(long flags, Name name, Symbol owner) {
            this(
                flags,
                name,
                new ClassType(Type.noType, null, null),
                owner);
            this.type.tsym = this;
        }

        /** The Java source which this symbol represents.
         */
        public String toString() {
            return className();
        }

        public long flags() {
            if (completer != null) complete();
            return flags_field;
        }

        public Scope members() {
            if (completer != null) complete();
            return members_field;
        }

        public List<Attribute.Compound> getAnnotationMirrors() {
        	try {//我加上的
			DEBUG.P(this,"getAnnotationMirrors()");
		
            if (completer != null) complete();
            assert attributes_field != null;
            return attributes_field;
            
            }finally{//我加上的
	        DEBUG.P("attributes_field="+attributes_field);
			DEBUG.P(0,this,"getAnnotationMirrors()");
			}
        }

        public Type erasure(Types types) {
        	try {//我加上的
			DEBUG.P(this,"erasure(Types types)");
			DEBUG.P("erasure_field="+erasure_field);
			
            if (erasure_field == null)
                erasure_field = new ClassType(types.erasure(type.getEnclosingType()),
                                              List.<Type>nil(), this);
            return erasure_field;
            
            }finally{//我加上的
            //DEBUG.P("erasure_field="+erasure_field);
			DEBUG.P(0,this,"erasure(Types types)");
			}
        }

        public String className() {
            if (name.len == 0)
                return
                    Log.getLocalizedString("anonymous.class", flatname);
            else
                return fullname.toString();
        }

        public Name getQualifiedName() {
            return fullname;
        }

        public Name flatName() {
            return flatname;
        }
        
        //判断当前ClassSymbol是否是Symbol base的子类
        public boolean isSubClass(Symbol base, Types types) {
			/*
			if (this == base) {
                return true;
            } else if ((base.flags() & INTERFACE) != 0) {
                for (Type t = type; t.tag == CLASS; t = types.supertype(t))
                    for (List<Type> is = types.interfaces(t);
                         is.nonEmpty();
                         is = is.tail)
                        if (is.head.tsym.isSubClass(base, types)) return true;
            } else {
                for (Type t = type; t.tag == CLASS; t = types.supertype(t))
                    if (t.tsym == base) return true;
            }
            return false;
			*/

        	//当this == base时表示指向同一个ClassSymbol，返回true
        	//否则，当base是接口时，查看当前ClassSymbol实现的所有接口是否是base的子接口
        	//否则，当base不是接口时，查看当前ClassSymbol的所有超类是否等于base
        	//否则，反回false
			boolean isSubClass=false;
			try {//我加上的
			DEBUG.P(this,"isSubClass(2)");
			DEBUG.P("this="+toString());
			DEBUG.P("this.flags_field="+Flags.toString(this.flags_field));
			DEBUG.P("base="+base);
			DEBUG.P("base.flags_field="+Flags.toString(base.flags_field));

            if (this == base) {
				isSubClass=true;
                return true;
            } else if ((base.flags() & INTERFACE) != 0) {
                for (Type t = type; t.tag == CLASS; t = types.supertype(t))
                    for (List<Type> is = types.interfaces(t);
                         is.nonEmpty();
                         is = is.tail)
                        if (is.head.tsym.isSubClass(base, types)) {
							 isSubClass=true;
							 return true;
						 }
            } else {
                for (Type t = type; t.tag == CLASS; t = types.supertype(t))
                    //为什么这里不像上面那样用isSubClass(base, types)判断呢?
                    //因为上面base是接口，这里base是超类
					if (t.tsym == base) {
						isSubClass=true;
						return true;
					}
            }
            return false;

			}finally{//我加上的
			DEBUG.P("this="+toString());
			DEBUG.P("base="+base);
			DEBUG.P("isSubClass="+isSubClass);
			DEBUG.P(0,this,"isSubClass(2)");
			}
        }

        /** Complete the elaboration of this symbol's definition.
         */
        public void complete() throws CompletionFailure {
            try {
                super.complete();
            } catch (CompletionFailure ex) {
                // quiet error recovery
                flags_field |= (PUBLIC|STATIC);
                this.type = new ErrorType(this);
                throw ex;
            }
        }

        public List<Type> getInterfaces() {
			try {//我加上的
			DEBUG.P(this,"getInterfaces()");

            complete();
            if (type instanceof ClassType) {
                ClassType t = (ClassType)type;
                if (t.interfaces_field == null) // FIXME: shouldn't be null
                    t.interfaces_field = List.nil();
                return t.interfaces_field;
            } else {
                return List.nil();
            }

			}finally{//我加上的
			DEBUG.P(0,this,"getInterfaces()");
			}
        }
        
        //特别注意:Symbol中的type字段和JCTree中的type字段是不想同的两个Type对象
        public Type getSuperclass() {
			try {//我加上的
			DEBUG.P(this,"getSuperclass()");

            complete();
            DEBUG.P("type.getClass().getName()="+type.getClass().getName());
            if (type instanceof ClassType) {
                ClassType t = (ClassType)type;
                if (t.supertype_field == null) // FIXME: shouldn't be null
                    t.supertype_field = Type.noType;
				// An interface has no superclass; its supertype is Object.
				return t.isInterface()
					? Type.noType
					: t.supertype_field;
            } else {
                return Type.noType;
            }

			}finally{//我加上的
			DEBUG.P(0,this,"getSuperclass()");
			}
        }
        
        //从这里看出ClassSymbol对应java源代码中
        //的注释类型定义、接口、枚举、普通类的定义
        public ElementKind getKind() {
            long flags = flags();
            if ((flags & ANNOTATION) != 0)
                return ElementKind.ANNOTATION_TYPE;
            else if ((flags & INTERFACE) != 0)
                return ElementKind.INTERFACE;
            else if ((flags & ENUM) != 0)
                return ElementKind.ENUM;
            else
                return ElementKind.CLASS;
        }

        public NestingKind getNestingKind() {
            complete();
            if (owner.kind == PCK)
                return NestingKind.TOP_LEVEL;
            else if (name.isEmpty())
                return NestingKind.ANONYMOUS;
            else if (owner.kind == MTH)
                return NestingKind.LOCAL;
            else
                return NestingKind.MEMBER;
        }

        /**
         * @deprecated this method should never be used by javac internally.
         */
        @Override @Deprecated
        public <A extends java.lang.annotation.Annotation> A getAnnotation(Class<A> annoType) {
            return JavacElements.getAnnotation(this, annoType);
        }

        public <R, P> R accept(ElementVisitor<R, P> v, P p) {
            return v.visitType(this, p);
        }
    }