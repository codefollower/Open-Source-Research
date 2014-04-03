    public static class ClassType extends Type implements DeclaredType {

        /** The enclosing type of this type. If this is the type of an inner
         *  class, outer_field refers to the type of its enclosing
         *  instance class, in all other cases it referes to noType.
         */
        //起初在构造ClassSymbol实例的时候，也构造一个ClassType的实例，
        //此时ClassType实例的字段outer_field=Type.noType，
        //(参看ClassSymbol(long flags, Name name, Symbol owner))
        
        //当进入Enter阶段时，如果ClassSymbol是一个成员类(非成员接口)，
        //那么将outer_field指向它的owner
        //(参看com.sun.tools.javac.comp.Enter===>visitClassDef(1)中的相关注释)
        private Type outer_field;

        /** The type parameters of this type (to be set once class is loaded).
         */
        /*
        //指的是:TypeVar，如Test<S,T extends ExtendsTest,E>中的S,T,E
        //在com.sun.tools.javac.comp.Enter===>visitClassDef(1)中设置
        //在Enter阶段设置的typarams_field实际上是TypeVar类型，但每个
        //TypeVar都不包含bound(也就是在Enter阶段bound=null)，
        //而是在MemberEnter阶段，
        //在com.sun.tools.javac.comp.MemberEnter===>complete(Symbol sym)方法中
        //调用com.sun.tools.javac.comp.Attr===>attribTypeVariables(2)方法来
        //给TypeVar.bound赋值
        //(如果在类型变量之后没有接extends，如上面的S，那么它的bound=java.lang.Object)
        
        //typarams_field还可以是泛型类的实参:
		//见com.sun.tools.javac.comp.Attr===>visitTypeApply(JCTypeApply tree)

		在Enter中:ct.typarams_field = classEnter(tree.typarams, localEnv);
		如果没有类型变量，typarams_field!=null，而是typarams_field.size=0
		*/
		public List<Type> typarams_field;

        /** A cache variable for the type parameters of this type,
         *  appended to all parameters of its enclosing class.
         *  @see #allparams
         */
        public List<Type> allparams_field;

        /** The supertype of this class (to be set once class is loaded).
         */
        //在com.sun.tools.javac.comp.MemberEnter===>complete(Symbol sym)中设置
        public Type supertype_field;

        /** The interfaces of this class (to be set once class is loaded).
         */
        //在com.sun.tools.javac.comp.MemberEnter===>complete(Symbol sym)中设置
        public List<Type> interfaces_field;

        public ClassType(Type outer, List<Type> typarams, TypeSymbol tsym) {
            super(CLASS, tsym);
            this.outer_field = outer;
            this.typarams_field = typarams;
            this.allparams_field = null;
            this.supertype_field = null;
            this.interfaces_field = null;
            /*
            // this can happen during error recovery
            assert
                outer.isParameterized() ?
                typarams.length() == tsym.type.typarams().length() :
                outer.isRaw() ?
                typarams.length() == 0 :
                true;
            */
        }

        @Override
        public <R,S> R accept(Type.Visitor<R,S> v, S s) {
            return v.visitClassType(this, s);
        }

        public Type constType(Object constValue) {
            final Object value = constValue;
            return new ClassType(getEnclosingType(), typarams_field, tsym) {
                    @Override
                    public Object constValue() {
                        return value;
                    }
                    @Override
                    public Type baseType() {
                        return tsym.type;
                    }
                };
        }

        /** The Java source which this type represents.
         */
        public String toString() {
            StringBuffer buf = new StringBuffer();
            if (getEnclosingType().tag == CLASS && tsym.owner.kind == TYP) {
                buf.append(getEnclosingType().toString());
                buf.append(".");
                buf.append(className(tsym, false));
            } else {
                buf.append(className(tsym, true));
            }
            //getTypeArguments()返回的就是TypeVar
            if (getTypeArguments().nonEmpty()) {
                buf.append('<');
                buf.append(getTypeArguments().toString());
                buf.append(">");
            }
            return buf.toString();
        }
        /* toString()返回字符串例子:
        1. outer_field非<none>时: my.test.Test<S30426707,T12122157,E28145575>.MyTestInnerClass
		2. outer_field为<none>时: my.test.Test<S30426707,T12122157,E28145575>
		*/
//where
            private String className(Symbol sym, boolean longform) {
                if (sym.name.len == 0 && (sym.flags() & COMPOUND) != 0) {
                    StringBuffer s = new StringBuffer(supertype_field.toString());
                    for (List<Type> is=interfaces_field; is.nonEmpty(); is = is.tail) {
                        s.append("&");
                        s.append(is.head.toString());
                    }
                    return s.toString();
                } else if (sym.name.len == 0) {
                    String s;
                    ClassType norm = (ClassType) tsym.type;
                    if (norm == null) {
                        s = Log.getLocalizedString("anonymous.class", (Object)null);
                    } else if (norm.interfaces_field != null && norm.interfaces_field.nonEmpty()) {
                        s = Log.getLocalizedString("anonymous.class",
                                                   norm.interfaces_field.head);
                    } else {
                        s = Log.getLocalizedString("anonymous.class",
                                                   norm.supertype_field);
                    }
                    if (moreInfo)
                        s += String.valueOf(sym.hashCode());
                    return s;
                } else if (longform) {
                    return sym.getQualifiedName().toString();
                } else {
                    return sym.name.toString();
                }
            }

        public List<Type> getTypeArguments() {
            if (typarams_field == null) {
                complete();
                if (typarams_field == null)
                    typarams_field = List.nil();
            }
            return typarams_field;
        }

        public Type getEnclosingType() {
            return outer_field;
        }

        public void setEnclosingType(Type outer) {
            outer_field = outer;
        }

        public List<Type> allparams() {
            if (allparams_field == null) {
                allparams_field = getTypeArguments().prependList(getEnclosingType().allparams());
            }
            return allparams_field;
        }

        public boolean isErroneous() {
            return
                getEnclosingType().isErroneous() ||
                isErroneous(getTypeArguments()) ||
                this != tsym.type && tsym.type.isErroneous();
        }

        public boolean isParameterized() {
            return allparams().tail != null;
            // optimization, was: allparams().nonEmpty();
        }

        /** A cache for the rank. */
        int rank_field = -1;

        /** A class type is raw if it misses some
         *  of its type parameter sections.
         *  After validation, this is equivalent to:
         *  allparams.isEmpty() && tsym.type.allparams.nonEmpty();
         */
        public boolean isRaw() {
            return
                this != tsym.type && // necessary, but not sufficient condition
                tsym.type.allparams().nonEmpty() &&
                allparams().isEmpty();
        }

        public Type map(Mapping f) {
            Type outer = getEnclosingType();
            Type outer1 = f.apply(outer);
            List<Type> typarams = getTypeArguments();
            List<Type> typarams1 = map(typarams, f);
            if (outer1 == outer && typarams1 == typarams) return this;
            else return new ClassType(outer1, typarams1, tsym);
        }

		//如果isParameterized()=true，说明还没有擦去类型变量
        public boolean contains(Type elem) {
            return
                elem == this
                || (isParameterized()
                    && (getEnclosingType().contains(elem) || contains(getTypeArguments(), elem)));
        }

        public void complete() {
            if (tsym.completer != null) tsym.complete();
        }

        public TypeKind getKind() {
            return TypeKind.DECLARED;
        }

        public <R, P> R accept(TypeVisitor<R, P> v, P p) {
            return v.visitDeclared(this, p);
        }
    }