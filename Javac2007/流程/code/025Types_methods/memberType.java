//memberType
    // <editor-fold defaultstate="collapsed" desc="memberType">
    /**
     * The type of given symbol, seen as a member of t.
     *
     * @param t a type
     * @param sym a symbol
     */
	/*sym是t的一个成员(比如:方法、字段、构造函数)，如果在定义t时给t加了类型变量，
	t的成员有可能引用了这些类型变量，所以在带有类型参数的情况下使用t时，
	必须把引用到t的类型变量的成员换成类型参数，如果使用t时不带有类型参数，那么
	即使t的成员引用了t的类型变量，返回t的成员时类型变量会被擦除
	*/
    public Type memberType(Type t, Symbol sym) {
        //return (sym.flags() & STATIC) != 0
        //    ? sym.type
        //    : memberType.visit(t, sym);

		DEBUG.P(this,"memberType(Type t, Symbol sym)");
		DEBUG.P("t="+t+" t.tag="+TypeTags.toString(t.tag));
		DEBUG.P("sym="+sym+" sym.flags()="+Flags.toString(sym.flags()));
		
		Type returnType = (sym.flags() & STATIC) != 0
            ? sym.type
            : memberType.visit(t, sym);
            
		DEBUG.P("returnType="+returnType);
		DEBUG.P(1,this,"memberType(Type t, Symbol sym)");
		return returnType;
    }
    // where
        private SimpleVisitor<Type,Symbol> memberType = new SimpleVisitor<Type,Symbol>() {

            public Type visitType(Type t, Symbol sym) {
                return sym.type;
            }

            @Override
            public Type visitWildcardType(WildcardType t, Symbol sym) {
                return memberType(upperBound(t), sym);
            }

            @Override
            public Type visitClassType(ClassType t, Symbol sym) {
            	try {//我加上的
            	DEBUG.P(this,"visitClassType(2)");
				DEBUG.P("t="+t+" t.tag="+TypeTags.toString(t.tag));
				
                Symbol owner = sym.owner;
                long flags = sym.flags();
                DEBUG.P("sym="+sym+" sym.flags()="+Flags.toString(sym.flags()));
                DEBUG.P("owner="+owner+" owner.flags()="+Flags.toString(owner.flags()));
                DEBUG.P("owner.type.isParameterized()="+owner.type.isParameterized());
                if (((flags & STATIC) == 0) && owner.type.isParameterized()) {
                    Type base = asOuterSuper(t, owner);
                    if (base != null) {
                        List<Type> ownerParams = owner.type.allparams();
                        List<Type> baseParams = base.allparams();
                        DEBUG.P("ownerParams="+ownerParams);
                        DEBUG.P("baseParams ="+baseParams);
                        if (ownerParams.nonEmpty()) {
                            if (baseParams.isEmpty()) {
                                // then base is a raw type
                                return erasure(sym.type);
                            } else {
                                return subst(sym.type, ownerParams, baseParams);
                            }
                        }
                    }
                }
                return sym.type;
                
                }finally{//我加上的
				DEBUG.P(0,this,"visitClassType(2)");
				}
            }

            @Override
            public Type visitTypeVar(TypeVar t, Symbol sym) {
                return memberType(t.bound, sym);
            }

            @Override
            public Type visitErrorType(ErrorType t, Symbol sym) {
                return t;
            }
        };
    // </editor-fold>
//