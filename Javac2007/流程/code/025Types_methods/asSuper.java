//asSuper
    // <editor-fold defaultstate="collapsed" desc="asSuper">
    /**
     * Return the (most specific) base type of t that starts with the
     * given symbol.  If none exists, return null.
     *
     * @param t a type
     * @param sym a symbol
     */
    //从t开始往上查找t的继承树与实现树，直到找到第一个type且这个type.tsym与sym
	//指向同一个Symbol(也就是type.tsym==sym)，最后返回这个type，找不到时返回null
    public Type asSuper(Type t, Symbol sym) {
        //return asSuper.visit(t, sym);

		DEBUG.P(this,"asSuper(Type t, Symbol sym)");
		//DEBUG.P("t="+t+"  t.tag="+TypeTags.toString(t.tag));
		//DEBUG.P("sym="+sym);

		Type returnType = asSuper.visit(t, sym);
            
		//DEBUG.P("returnType="+returnType);

		DEBUG.P("t="+t);
		DEBUG.P("sym="+sym);
		DEBUG.P("在t的继承树上寻找sym得到 asSuper="+returnType);
		DEBUG.P(1,this,"asSuper(Type t, Symbol sym)");
		return returnType;
    }
    // where
        private SimpleVisitor<Type,Symbol> asSuper = new SimpleVisitor<Type,Symbol>() {

            public Type visitType(Type t, Symbol sym) {
                return null;
            }

            @Override
            public Type visitClassType(ClassType t, Symbol sym) {
                if (t.tsym == sym)
                    return t;

                Type st = supertype(t);
                if (st.tag == CLASS || st.tag == ERROR) {
                    Type x = asSuper(st, sym);
                    if (x != null)
                        return x;
                }
                if ((sym.flags() & INTERFACE) != 0) {
                    for (List<Type> l = interfaces(t); l.nonEmpty(); l = l.tail) {
                        Type x = asSuper(l.head, sym);
                        if (x != null)
                            return x;
                    }
                }
                return null;
            }

            @Override
            public Type visitArrayType(ArrayType t, Symbol sym) {
                return isSubtype(t, sym.type) ? sym.type : null;
            }

            @Override
            public Type visitTypeVar(TypeVar t, Symbol sym) {
                return asSuper(t.bound, sym);
            }

            @Override
            public Type visitErrorType(ErrorType t, Symbol sym) {
                return t;
            }
        };

    /**
     * Return the base type of t or any of its outer types that starts
     * with the given symbol.  If none exists, return null.
     *
     * @param t a type
     * @param sym a symbol
     */
    //先从t开始往上查找t的继承树与实现树，直到找到第一个type且这个type.tsym与sym
	//指向同一个Symbol(也就是type.tsym==sym)，找到则返回这个type，如果找不到，则将
	//t切换成t的outer_field，继续查找按前面的方式查找，直到t的outer_field.tag不是CLASS为止
    public Type asOuterSuper(Type t, Symbol sym) {
    	try {//我加上的
		DEBUG.P(this,"asOuterSuper(Type t, Symbol sym)");
		DEBUG.P("t="+t+" t.tag="+TypeTags.toString(t.tag));
		DEBUG.P("sym="+sym);
		
        switch (t.tag) {
        case CLASS:
            do {
                Type s = asSuper(t, sym);
                if (s != null) return s;
                t = t.getEnclosingType();
            } while (t.tag == CLASS);
            return null;
        case ARRAY:
            return isSubtype(t, sym.type) ? sym.type : null;
        case TYPEVAR:
            return asSuper(t, sym);
        case ERROR:
            return t;
        default:
            return null;
        }
        
        }finally{//我加上的
		DEBUG.P(1,this,"asOuterSuper(Type t, Symbol sym)");
		}
    }

    /**
     * Return the base type of t or any of its enclosing types that
     * starts with the given symbol.  If none exists, return null.
     *
     * @param t a type
     * @param sym a symbol
     */
    public Type asEnclosingSuper(Type t, Symbol sym) {
		try {//我加上的
		DEBUG.P(this,"asEnclosingSuper(Type t, Symbol sym)");
		DEBUG.P("t="+t+" t.tag="+TypeTags.toString(t.tag));
		DEBUG.P("sym="+sym);

        switch (t.tag) {
        case CLASS:
            do {
                Type s = asSuper(t, sym);
				DEBUG.P("s="+s);
                if (s != null) return s;
                Type outer = t.getEnclosingType();
				DEBUG.P("outer="+outer+" outer.tag="+TypeTags.toString(outer.tag));
                t = (outer.tag == CLASS) ? outer :
                    (t.tsym.owner.enclClass() != null) ? t.tsym.owner.enclClass().type :
                    Type.noType;
            } while (t.tag == CLASS);
            return null;
        case ARRAY:
            return isSubtype(t, sym.type) ? sym.type : null;
        case TYPEVAR:
            return asSuper(t, sym);
        case ERROR:
            return t;
        default:
            return null;
        }

		}finally{//我加上的
		DEBUG.P(1,this,"asEnclosingSuper(Type t, Symbol sym)");
		}
    }
    // </editor-fold>
//