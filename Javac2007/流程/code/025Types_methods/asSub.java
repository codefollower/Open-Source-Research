//asSub
    // <editor-fold defaultstate="collapsed" desc="asSub">
    /**
     * Return the least specific subtype of t that starts with symbol
     * sym.  If none exists, return null.  The least specific subtype
     * is determined as follows:
     *
     * <p>If there is exactly one parameterized instance of sym that is a
     * subtype of t, that parameterized instance is returned.<br>
     * Otherwise, if the plain type or raw type `sym' is a subtype of
     * type t, the type `sym' itself is returned.  Otherwise, null is
     * returned.
     */
    public Type asSub(Type t, Symbol sym) {
        //return asSub.visit(t, sym);

		DEBUG.P(this,"asSub(2)");
		DEBUG.P("t="+t+"  t.tag="+TypeTags.toString(t.tag));
		DEBUG.P("sym="+sym);
		
		Type returnType = asSub.visit(t, sym);
            
		DEBUG.P("returnType="+returnType);
		DEBUG.P(1,this,"asSub(2)");
		return returnType;
    }
    // where
        private final SimpleVisitor<Type,Symbol> asSub = new SimpleVisitor<Type,Symbol>() {

            public Type visitType(Type t, Symbol sym) {
                return null;
            }

            @Override
            public Type visitClassType(ClassType t, Symbol sym) {
				try {//我加上的
				DEBUG.P(this,"visitClassType(2)");
				DEBUG.P("t="+t+"  t.tag="+TypeTags.toString(t.tag));
				DEBUG.P("sym="+sym);
				DEBUG.P("(t.tsym == sym)="+(t.tsym == sym));

                if (t.tsym == sym)
                    return t;
                Type base = asSuper(sym.type, t.tsym);
                if (base == null)
                    return null;
                ListBuffer<Type> from = new ListBuffer<Type>();
                ListBuffer<Type> to = new ListBuffer<Type>();
                try {
                    adapt(base, t, from, to);
                } catch (AdaptFailure ex) {
                    return null;
                }
                Type res = subst(sym.type, from.toList(), to.toList());
                if (!isSubtype(res, t))
                    return null;
                ListBuffer<Type> openVars = new ListBuffer<Type>();
                for (List<Type> l = sym.type.allparams();
                     l.nonEmpty(); l = l.tail)
                    if (res.contains(l.head) && !t.contains(l.head))
                        openVars.append(l.head);
                if (openVars.nonEmpty()) {
                    if (t.isRaw()) {
                        // The subtype of a raw type is raw
                        res = erasure(res);
                    } else {
                        // Unbound type arguments default to ?
                        List<Type> opens = openVars.toList();
                        ListBuffer<Type> qs = new ListBuffer<Type>();
                        for (List<Type> iter = opens; iter.nonEmpty(); iter = iter.tail) {
                            qs.append(new WildcardType(syms.objectType, BoundKind.UNBOUND, syms.boundClass, (TypeVar) iter.head));
                        }
                        res = subst(res, opens, qs.toList());
                    }
                }
                return res;

				}finally{//我加上的
				DEBUG.P(0,this,"visitClassType(2)");
				}
            }

            @Override
            public Type visitErrorType(ErrorType t, Symbol sym) {
                return t;
            }
        };
    // </editor-fold>
//