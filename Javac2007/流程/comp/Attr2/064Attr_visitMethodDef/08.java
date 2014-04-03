       /** The implementation of this (abstract) symbol in class origin;
         *  null if none exists. Synthetic methods are not considered
         *  as possible implementations.
         */
        public MethodSymbol implementation(TypeSymbol origin, Types types, boolean checkResult) {
        	//当前的MethodSymbol代表一个抽象方法，检查origin类中是否实现了该方法
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
                        if (m.overrides(this, origin, types, checkResult) &&
                            (m.flags() & SYNTHETIC) == 0)
                            return m;
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


        /** Does this symbol override `other' symbol, when both are seen as
         *  members of class `origin'?  It is assumed that _other is a member
         *  of origin.
         *
         *  It is assumed that both symbols have the same name.  The static
         *  modifier is ignored for this test.
         *
         *  See JLS 8.4.6.1 (without transitivity) and 8.4.6.4
         */
        public boolean overrides(Symbol _other, TypeSymbol origin, Types types, boolean checkResult) {
        	try {//我加上的
			DEBUG.P(this,"overrides(4)");
			DEBUG.P("Symbol _other="+_other);
			DEBUG.P("TypeSymbol origin="+origin);
			DEBUG.P("boolean checkResult="+checkResult);
            if (isConstructor() || _other.kind != MTH) return false;

            if (this == _other) return true;
            MethodSymbol other = (MethodSymbol)_other;

            // check for a direct implementation
            if (other.isOverridableIn((TypeSymbol)owner) &&
                types.asSuper(owner.type, other.owner) != null) {
                Type mt = types.memberType(owner.type, this);
                Type ot = types.memberType(owner.type, other);
                if (types.isSubSignature(mt, ot)) {
                    if (!checkResult)
                        return true;
                    if (types.returnTypeSubstitutable(mt, ot))
                        return true;
                }
            }

            // check for an inherited implementation
            if ((flags() & ABSTRACT) != 0 ||
                (other.flags() & ABSTRACT) == 0 ||
                !other.isOverridableIn(origin) ||
                !this.isMemberOf(origin, types))
                return false;

            // assert types.asSuper(origin.type, other.owner) != null;
            Type mt = types.memberType(origin.type, this);
            Type ot = types.memberType(origin.type, other);
            return
                types.isSubSignature(mt, ot) &&
                (!checkResult || types.resultSubtype(mt, ot, Warner.noWarnings));
            }finally{//我加上的
			DEBUG.P(0,this,"overrides(4)");
			}
        }

        //根据方法前的修饰符(PRIVATE,PUBLIC,PROTECTED或没有)
        //来决定实现类是否能覆盖此方法
        private boolean isOverridableIn(TypeSymbol origin) {
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
        }