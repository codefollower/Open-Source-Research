    /** A class for method symbols.
     */
    public static class MethodSymbol extends Symbol implements ExecutableElement {

        /** The code of the method. */
        public Code code = null;

        /** The parameters of the method. */
        public List<VarSymbol> params = null;

        /** The names of the parameters */
        public List<Name> savedParameterNames;

        /** For an attribute field accessor, its default value if any.
         *  The value is null if none appeared in the method
         *  declaration.
         */
        public Attribute defaultValue = null;

        /** Construct a method symbol, given its flags, name, type and owner.
         */
        public MethodSymbol(long flags, Name name, Type type, Symbol owner) {
            super(MTH, flags, name, type, owner);
            assert owner.type.tag != TYPEVAR : owner + "." + name;
        }

        /** Clone this symbol with new owner.
         */
        public MethodSymbol clone(Symbol newOwner) {
            MethodSymbol m = new MethodSymbol(flags_field, name, type, newOwner);
            m.code = code;
            return m;
        }

        /** The Java source which this symbol represents.
         */
        public String toString() {
            if ((flags() & BLOCK) != 0) {
                return owner.name.toString();
            } else {
                String s = (name == name.table.init)
                    ? owner.name.toString()
                    : name.toString();
                if (type != null) {
                    if (type.tag == FORALL)
                        s = "<" + ((ForAll)type).getTypeArguments() + ">" + s;
                    s += "(" + type.argtypes((flags() & VARARGS) != 0) + ")";
                }
                return s;
            }
        }

        /** find a symbol that this (proxy method) symbol implements.
         *  @param    c       The class whose members are searched for
         *                    implementations
         */
        public Symbol implemented(TypeSymbol c, Types types) {
            Symbol impl = null;
            for (List<Type> is = types.interfaces(c.type);
                 impl == null && is.nonEmpty();
                 is = is.tail) {
                TypeSymbol i = is.head.tsym;
                for (Scope.Entry e = i.members().lookup(name);
                     impl == null && e.scope != null;
                     e = e.next()) {
                    if (this.overrides(e.sym, (TypeSymbol)owner, types, true) &&
                        // FIXME: I suspect the following requires a
                        // subst() for a parametric return type.
                        types.isSameType(type.getReturnType(),
                                         types.memberType(owner.type, e.sym).getReturnType())) {
                        impl = e.sym;
                    }
                    if (impl == null)
                        impl = implemented(i, types);
                }
            }
            return impl;
        }

        /** Will the erasure of this method be considered by the VM to
         *  override the erasure of the other when seen from class `origin'?
         */
        public boolean binaryOverrides(Symbol _other, TypeSymbol origin, Types types) {
            if (isConstructor() || _other.kind != MTH) return false;

            if (this == _other) return true;
            MethodSymbol other = (MethodSymbol)_other;

            // check for a direct implementation
            if (other.isOverridableIn((TypeSymbol)owner) &&
                types.asSuper(owner.type, other.owner) != null &&
                types.isSameType(erasure(types), other.erasure(types)))
                return true;

            // check for an inherited implementation
            return
                (flags() & ABSTRACT) == 0 &&
                other.isOverridableIn(origin) &&
                this.isMemberOf(origin, types) &&
                types.isSameType(erasure(types), other.erasure(types));
        }

        /** The implementation of this (abstract) symbol in class origin,
         *  from the VM's point of view, null if method does not have an
         *  implementation in class.
         *  @param origin   The class of which the implementation is a member.
         */
        public MethodSymbol binaryImplementation(ClassSymbol origin, Types types) {
            for (TypeSymbol c = origin; c != null; c = types.supertype(c.type).tsym) {
                for (Scope.Entry e = c.members().lookup(name);
                     e.scope != null;
                     e = e.next()) {
                    if (e.sym.kind == MTH &&
                        ((MethodSymbol)e.sym).binaryOverrides(this, origin, types))
                        return (MethodSymbol)e.sym;
                }
            }
            return null;
        }

        /** Does this symbol override `other' symbol, when both are seen as
         *  members of class `origin'?  It is assumed that _other is a member
         *  of origin.
         *
         *  It is assumed that both symbols have the same name.  The static
         *  modifier is ignored for this test.
         *
         *  See JLS 8.4.6.1 (without transitivity) and 8.4.6.4
         */
        //检查当前Symbol是否覆盖了Symbol _other
        //当前Symbol有可能是原始实现类(origin)或者超类中的方法
        public boolean overrides(Symbol _other, TypeSymbol origin, Types types, boolean checkResult) {
			boolean overrides=true;//我加上的
        	try {//我加上的
			DEBUG.P(this,"overrides(4)");
			DEBUG.P("this  ="+toString());
			DEBUG.P("_other="+_other);
			DEBUG.P("this.owner  ="+this.owner);
			DEBUG.P("_other.owner="+_other.owner);
			DEBUG.P("_other.kind ="+Kinds.toString(_other.kind));
			DEBUG.P("isConstructor()="+isConstructor());
            if (isConstructor() || _other.kind != MTH) return overrides=false;
            
            DEBUG.P("");
			DEBUG.P("TypeSymbol origin="+origin);
			DEBUG.P("boolean checkResult="+checkResult);
            DEBUG.P("(this == _other)="+(this == _other));
            if (this == _other) return true;
            MethodSymbol other = (MethodSymbol)_other;

            // check for a direct implementation
            
            /*在判断当前方法能否覆盖other方法前，先调用isOverridableIn
			判别other方法的修饰符(PRIVATE,PUBLIC,PROTECTED或没有)
			是否能在当前方法的owner中覆盖other，比如说，如果other
			方法的修饰符是PRIVATE，那么在owner中不能覆盖他。

			如果isOverridableIn返回true了，还必须确认other方法的owner
			是当前当前方法的owner的超类
			*/
            if (other.isOverridableIn((TypeSymbol)owner) &&
                types.asSuper(owner.type, other.owner) != null) {
                Type mt = types.memberType(owner.type, this);
                Type ot = types.memberType(owner.type, other);
                if (types.isSubSignature(mt, ot)) {
                    if (!checkResult) //检查方法返回类型
                        return true;
                    if (types.returnTypeSubstitutable(mt, ot))
                        return true;
                }
            }
			DEBUG.P("");
			DEBUG.P("this  ="+toString());
			DEBUG.P("_other="+_other);
			DEBUG.P("this.owner  ="+this.owner);
			DEBUG.P("_other.owner="+_other.owner);
			DEBUG.P("");
			DEBUG.P("this.flags() ="+Flags.toString(this.flags()));
			DEBUG.P("other.flags()="+Flags.toString(other.flags()));

            // check for an inherited implementation
            if ((flags() & ABSTRACT) != 0 ||
                (other.flags() & ABSTRACT) == 0 ||
                !other.isOverridableIn(origin) ||
                !this.isMemberOf(origin, types))
                return overrides=false;

            // assert types.asSuper(origin.type, other.owner) != null;
            Type mt = types.memberType(origin.type, this);
            Type ot = types.memberType(origin.type, other);
            return overrides=
                types.isSubSignature(mt, ot) &&
                (!checkResult || types.resultSubtype(mt, ot, Warner.noWarnings));
            }finally{//我加上的
			DEBUG.P("overrides="+overrides);
			DEBUG.P(1,this,"overrides(4)");
			}
        }
        
        //根据方法前的修饰符(PRIVATE,PUBLIC,PROTECTED或没有)
        //来决定实现类是否能覆盖此方法
        private boolean isOverridableIn(TypeSymbol origin) {
			/*
            // JLS3 8.4.6.1
            switch ((int)(flags_field & Flags.AccessFlags)) {
            case Flags.PRIVATE:
                return false;
            case Flags.PUBLIC:
                return true;
            case Flags.PROTECTED:
                return (origin.flags() & INTERFACE) == 0;
            case 0:
                // for package private: can only override in the same
                // package
                return
                    this.packge() == origin.packge() &&
                    (origin.flags() & INTERFACE) == 0;
            default:
                return false;
            }
			*/

			boolean isOverridableIn=false;
			DEBUG.P(this,"isOverridableIn(TypeSymbol origin)");
			DEBUG.P("flags_field="+Flags.toString(flags_field));
			DEBUG.P("flags_field & AccessFlags="+Flags.toString(flags_field & AccessFlags));
			DEBUG.P("  this="+toString()+"    this.owner="+this.owner);
			DEBUG.P("this.packge()="+this.packge());
			DEBUG.P("origin.packge()="+origin.packge());
			DEBUG.P("origin="+origin);
			DEBUG.P("origin.flags_field="+Flags.toString(origin.flags_field));

			switch ((int)(flags_field & Flags.AccessFlags)) {
            case Flags.PRIVATE:
                isOverridableIn= false;break;
            case Flags.PUBLIC:
                isOverridableIn= true;break;
            case Flags.PROTECTED:
                isOverridableIn= (origin.flags() & INTERFACE) == 0;break;
            case 0:
                // for package private: can only override in the same
                // package
                isOverridableIn=
                    this.packge() == origin.packge() &&
                    (origin.flags() & INTERFACE) == 0;break;
            default:
                isOverridableIn= false;
            }

			DEBUG.P("");
			DEBUG.P("isOverridableIn="+isOverridableIn);
			DEBUG.P(0,this,"isOverridableIn(TypeSymbol origin)");
			return isOverridableIn;
        }

        /** The implementation of this (abstract) symbol in class origin;
         *  null if none exists. Synthetic methods are not considered
         *  as possible implementations.
         */
        public MethodSymbol implementation(TypeSymbol origin, Types types, boolean checkResult) {
        	//当前的MethodSymbol代表一个抽象方法，检查origin类及超类中是否实现了该方法
        	try {//我加上的
			DEBUG.P(this,"implementation(3)");
			DEBUG.P("TypeSymbol origin="+origin);
			DEBUG.P("boolean checkResult="+checkResult);
			
            for (Type t = origin.type; t.tag == CLASS; t = types.supertype(t)) {
                TypeSymbol c = t.tsym;
                DEBUG.P("第一层for:");
                DEBUG.P("TypeSymbol c="+c);
                DEBUG.P("c.members()="+c.members());
                DEBUG.P("lookup(name)="+name);
                DEBUG.P("t.tag="+TypeTags.toString(t.tag));
                for (Scope.Entry e = c.members().lookup(name);
                     e.scope != null;
                     e = e.next()) {
                    DEBUG.P("第二层for:");
                    DEBUG.P("e.sym="+e.sym);
                    DEBUG.P("e.scope="+e.scope);
                    DEBUG.P("e.sym.kind="+Kinds.toString(e.sym.kind));
                    if (e.sym.kind == MTH) {
                        MethodSymbol m = (MethodSymbol) e.sym;
                        
						//m有可能是原始实现类(origin)或者超类中的方法，this是被实现的抽象方法
                        boolean overrides=m.overrides(this, origin, types, checkResult);
						//如果非abstract类中含有abstract方法，m和this是指向这个非abstract类中
						//的同一个方法，在调用overrides方法时，
						//有一条“if (this == _other) return true;”的语句，
						//也就是说，直接就认为他们相互覆盖。
                        DEBUG.P("overrides="+overrides);
                        if(overrides) {
                        	if((m.flags() & SYNTHETIC) == 0) {
                        		DEBUG.P(m+".flags() 没有SYNTHETIC");
                        		return m;
                        	}
                        }
                        /*		
                        if (m.overrides(this, origin, types, checkResult) &&
                            (m.flags() & SYNTHETIC) == 0)
                            return m;
                            */
                    }
                }
            }
            DEBUG.P("结束第一层for");
            DEBUG.P("origin.type="+origin.type);
            // if origin is derived from a raw type, we might have missed
            // an implementation because we do not know enough about instantiations.
            // in this case continue with the supertype as origin.
            if (types.isDerivedRaw(origin.type))
                return implementation(types.supertype(origin.type).tsym, types, checkResult);
            else
                return null;
                
            }finally{//我加上的
			DEBUG.P(0,this,"implementation(3)");
			}
        }

        public List<VarSymbol> params() {
            owner.complete();
            if (params == null) {
                List<Name> names = savedParameterNames;
                savedParameterNames = null;
                if (names == null) {
                    names = List.nil();
                    int i = 0;
                    for (Type t : type.getParameterTypes())
                        names = names.prepend(name.table.fromString("arg" + i++));
                    names = names.reverse();
                }
                ListBuffer<VarSymbol> buf = new ListBuffer<VarSymbol>();
                for (Type t : type.getParameterTypes()) {
                    buf.append(new VarSymbol(PARAMETER, names.head, t, this));
                    names = names.tail;
                }
                params = buf.toList();
            }
            return params;
        }

        public Symbol asMemberOf(Type site, Types types) {
            return new MethodSymbol(flags_field, name, types.memberType(site, this), owner);
        }

        public ElementKind getKind() {
            if (name == name.table.init)
                return ElementKind.CONSTRUCTOR;
            else if (name == name.table.clinit)
                return ElementKind.STATIC_INIT;
            else
                return ElementKind.METHOD;
        }

        public Attribute getDefaultValue() {
            return defaultValue;
        }

        public List<VarSymbol> getParameters() {
            return params();
        }

        public boolean isVarArgs() {
            return (flags() & VARARGS) != 0;
        }

        public <R, P> R accept(ElementVisitor<R, P> v, P p) {
            return v.visitExecutable(this, p);
        }

        public Type getReturnType() {
            return asType().getReturnType();
        }

        public List<Type> getThrownTypes() {
            return asType().getThrownTypes();
        }
    }