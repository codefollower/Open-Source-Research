//isSubtype
    // <editor-fold defaultstate="collapsed" desc="isSubtype">
    /**
     * Is t an unchecked subtype of s?
     */
    public boolean isSubtypeUnchecked(Type t, Type s) {
        //return isSubtypeUnchecked(t, s, Warner.noWarnings);

		DEBUG.P(this,"isSubtypeUnchecked(2)");
		DEBUG.P("t="+t+"  t.tag="+TypeTags.toString(t.tag));
		DEBUG.P("s="+s+"  s.tag="+TypeTags.toString(s.tag));
		boolean returnResult=isSubtypeUnchecked(t, s, Warner.noWarnings);
		DEBUG.P("returnResult="+returnResult);
		DEBUG.P(1,this,"isSubtypeUnchecked(2)");
		return returnResult;
    }
    /**
     * Is t an unchecked subtype of s?
     */
    public boolean isSubtypeUnchecked(Type t, Type s, Warner warn) {
		try {//我加上的
		DEBUG.P(this,"isSubtypeUnchecked(3)");
		DEBUG.P("t="+t+"  t.tag="+TypeTags.toString(t.tag));
		DEBUG.P("s="+s+"  s.tag="+TypeTags.toString(s.tag));
		DEBUG.P("s.isRaw()="+s.isRaw());

		/*
        if (t.tag == ARRAY && s.tag == ARRAY) {
            return (((ArrayType)t).elemtype.tag <= lastBaseTag) //8个基本类型
                ? isSameType(elemtype(t), elemtype(s))
                : isSubtypeUnchecked(elemtype(t), elemtype(s), warn);
        } else if (isSubtype(t, s)) {
            return true;
        } else if (!s.isRaw()) {
            Type t2 = asSuper(t, s.tsym);
            if (t2 != null && t2.isRaw()) {
                if (isReifiable(s))
                    warn.silentUnchecked();
                else
                    warn.warnUnchecked();
                return true;
            }
        }
        return false;
		*/

		boolean returnResult= myIsSubtypeUnchecked(t, s, warn);
            
		
		DEBUG.P("t="+t+"  t.tag="+TypeTags.toString(t.tag));
		DEBUG.P("s="+s+"  s.tag="+TypeTags.toString(s.tag));
		DEBUG.P("returnResult="+returnResult);
		return returnResult;

		}finally{//我加上的
		DEBUG.P(1,this,"isSubtypeUnchecked(3)");
		}
    }

	private boolean myIsSubtypeUnchecked(Type t, Type s, Warner warn) {
		if (t.tag == ARRAY && s.tag == ARRAY) {
            return (((ArrayType)t).elemtype.tag <= lastBaseTag) //8个基本类型
                ? isSameType(elemtype(t), elemtype(s))
                : isSubtypeUnchecked(elemtype(t), elemtype(s), warn);
        } else if (isSubtype(t, s)) {
            return true;
        } else if (!s.isRaw()) {
            Type t2 = asSuper(t, s.tsym);
			if(t2 != null) DEBUG.P("t2.isRaw()="+t2.isRaw());
			else DEBUG.P("t2="+null);
            if (t2 != null && t2.isRaw()) {
                if (isReifiable(s))
                    warn.silentUnchecked();
                else
                    warn.warnUnchecked();
                return true;
            }
        }
        return false;
    }

    /**
     * Is t a subtype of s?<br>
     * (not defined for Method and ForAll types)
     */
    final public boolean isSubtype(Type t, Type s) {
        //return isSubtype(t, s, true);

        DEBUG.P(this,"isSubtype(2)");
		boolean returnResult= isSubtype(t, s, true);
            
		DEBUG.P("returnResult="+returnResult);
		DEBUG.P(1,this,"isSubtype(2)");
		return returnResult;
    }
    final public boolean isSubtypeNoCapture(Type t, Type s) {
        //return isSubtype(t, s, false);

		DEBUG.P(this,"isSubtypeNoCapture(2)");
		boolean returnResult= isSubtype(t, s, false);
            
		DEBUG.P("returnResult="+returnResult);
		DEBUG.P(1,this,"isSubtypeNoCapture(2)");
		return returnResult;
    }
    public boolean isSubtype(Type t, Type s, boolean capture) {
		try {//我加上的
		DEBUG.P(this,"isSubtype(3)");
		DEBUG.P("t="+t+"  t.tag="+TypeTags.toString(t.tag));
		DEBUG.P("s="+s+"  s.tag="+TypeTags.toString(s.tag));
		DEBUG.P("capture="+capture);

		DEBUG.P("if (t == s)="+(t == s));
        if (t == s)
            return true;

		DEBUG.P("if (s.tag >= firstPartialTag)="+(s.tag >= firstPartialTag));
        if (s.tag >= firstPartialTag)
            return isSuperType(s, t);
        Type lower = lowerBound(s);

		DEBUG.P("if (s != lower)="+(s != lower));
        if (s != lower)
            return isSubtype(capture ? capture(t) : t, lower, false);

        return isSubtype.visit(capture ? capture(t) : t, s);

		}finally{//我加上的
		DEBUG.P(0,this,"isSubtype(3)");
		}
    }
    // where
        private TypeRelation isSubtype = new TypeRelation()
        {
            public Boolean visitType(Type t, Type s) {
                switch (t.tag) {
                case BYTE: case CHAR:
                    return (t.tag == s.tag ||
                              t.tag + 2 <= s.tag && s.tag <= DOUBLE);
                case SHORT: case INT: case LONG: case FLOAT: case DOUBLE:
                    return t.tag <= s.tag && s.tag <= DOUBLE;
                case BOOLEAN: case VOID:
                    return t.tag == s.tag;
                case TYPEVAR:
                    return isSubtypeNoCapture(t.getUpperBound(), s);
                case BOT:
                    return
                        s.tag == BOT || s.tag == CLASS ||
                        s.tag == ARRAY || s.tag == TYPEVAR;
                case NONE:
                    return false;
                default:
                    throw new AssertionError("isSubtype " + t.tag);
                }
            }

            private Set<TypePair> cache = new HashSet<TypePair>();

            private boolean containsTypeRecursive(Type t, Type s) {
				try {//我加上的
				DEBUG.P(this,"containsTypeRecursive(2)");
				DEBUG.P("t="+t+" t.tag="+TypeTags.toString(t.tag));
				DEBUG.P("s="+s+" s.tag="+TypeTags.toString(s.tag));
				DEBUG.P("");
                TypePair pair = new TypePair(t, s);
                if (cache.add(pair)) {
                    try {
                        return containsType(t.getTypeArguments(),
                                            s.getTypeArguments());
                    } finally {
                        cache.remove(pair);
                    }
                } else {
                    return containsType(t.getTypeArguments(),
                                        rewriteSupers(s).getTypeArguments());
                }

				}finally{//我加上的
				DEBUG.P(0,this,"containsTypeRecursive(2)");
				} 
            }
            private Type rewriteSupers(Type t) {
				try {//我加上的
				DEBUG.P(this,"rewriteSupers(1)");
				DEBUG.P("t="+t+" t.tag="+TypeTags.toString(t.tag));

				DEBUG.P("t.isParameterized()="+t.isParameterized());

                if (!t.isParameterized())
                    return t;
                ListBuffer<Type> from = lb();
                ListBuffer<Type> to = lb();
                adaptSelf(t, from, to);

				DEBUG.P("from.isEmpty()="+from.isEmpty());
                if (from.isEmpty())
                    return t;
                ListBuffer<Type> rewrite = lb();
                boolean changed = false;
                for (Type orig : to.toList()) {
                    Type s = rewriteSupers(orig);
                    if (s.isSuperBound() && !s.isExtendsBound()) {
                        s = new WildcardType(syms.objectType,
                                             BoundKind.UNBOUND,
                                             syms.boundClass);
                        changed = true;
                    } else if (s != orig) {
                        s = new WildcardType(upperBound(s),
                                             BoundKind.EXTENDS,
                                             syms.boundClass);
                        changed = true;
                    }
                    rewrite.append(s);
                }

				DEBUG.P("changed="+changed);
                if (changed)
                    return subst(t.tsym.type, from.toList(), rewrite.toList());
                else
                    return t;

				}finally{//我加上的
				DEBUG.P(0,this,"rewriteSupers(1)");
				} 
            }

            @Override
            public Boolean visitClassType(ClassType t, Type s) {
            	try {//我加上的
				DEBUG.P(this,"visitClassType(2)");
				DEBUG.P("t="+t+" t.tag="+TypeTags.toString(t.tag));
				DEBUG.P("s="+s+" s.tag="+TypeTags.toString(s.tag));
				DEBUG.P("");
                Type sup = asSuper(t, s.tsym);
                
                DEBUG.P("");
                DEBUG.P("sup="+sup);
                if(sup != null) DEBUG.P("(sup.tsym == s.tsym)="+(sup.tsym == s.tsym));
				DEBUG.P("s.isParameterized()="+s.isParameterized());
                return sup != null
                    && sup.tsym == s.tsym
                    // You're not allowed to write
                    //     Vector<Object> vec = new Vector<String>();
                    // But with wildcards you can write
                    //     Vector<? extends Object> vec = new Vector<String>();
                    // which means that subtype checking must be done
                    // here instead of same-type checking (via containsType).
                    && (!s.isParameterized() || containsTypeRecursive(s, sup))
                    && isSubtypeNoCapture(sup.getEnclosingType(),
                                          s.getEnclosingType());
               	}finally{//我加上的
				DEBUG.P(0,this,"visitClassType(2)");
				}                           
            }

            @Override
            public Boolean visitArrayType(ArrayType t, Type s) {
                if (s.tag == ARRAY) {
                    if (t.elemtype.tag <= lastBaseTag)
                        return isSameType(t.elemtype, elemtype(s));
                    else
                        return isSubtypeNoCapture(t.elemtype, elemtype(s));
                }

                if (s.tag == CLASS) {
                    Name sname = s.tsym.getQualifiedName();
                    return sname == names.java_lang_Object
                        || sname == names.java_lang_Cloneable
                        || sname == names.java_io_Serializable;
                }

                return false;
            }

            @Override
            public Boolean visitUndetVar(UndetVar t, Type s) {
                //todo: test against origin needed? or replace with substitution?
                if (t == s || t.qtype == s || s.tag == ERROR || s.tag == UNKNOWN)
                    return true;

                if (t.inst != null)
                    return isSubtypeNoCapture(t.inst, s); // TODO: ", warn"?

                t.hibounds = t.hibounds.prepend(s);
                return true;
            }

            @Override
            public Boolean visitErrorType(ErrorType t, Type s) {
                return true;
            }
        };

    /**
     * Is t a subtype of every type in given list `ts'?<br>
     * (not defined for Method and ForAll types)<br>
     * Allows unchecked conversions.
     */
    public boolean isSubtypeUnchecked(Type t, List<Type> ts, Warner warn) {
        for (List<Type> l = ts; l.nonEmpty(); l = l.tail)
            if (!isSubtypeUnchecked(t, l.head, warn))
                return false;
        return true;
    }

    /**
     * Are corresponding elements of ts subtypes of ss?  If lists are
     * of different length, return false.
     */
    public boolean isSubtypes(List<Type> ts, List<Type> ss) {
        while (ts.tail != null && ss.tail != null
               /*inlined: ts.nonEmpty() && ss.nonEmpty()*/ &&
               isSubtype(ts.head, ss.head)) {
            ts = ts.tail;
            ss = ss.tail;
        }
        return ts.tail == null && ss.tail == null;
        /*inlined: ts.isEmpty() && ss.isEmpty();*/
    }

    /**
     * Are corresponding elements of ts subtypes of ss, allowing
     * unchecked conversions?  If lists are of different length,
     * return false.
     **/
    public boolean isSubtypesUnchecked(List<Type> ts, List<Type> ss, Warner warn) {
        while (ts.tail != null && ss.tail != null
               /*inlined: ts.nonEmpty() && ss.nonEmpty()*/ &&
               isSubtypeUnchecked(ts.head, ss.head, warn)) {
            ts = ts.tail;
            ss = ss.tail;
        }
        return ts.tail == null && ss.tail == null;
        /*inlined: ts.isEmpty() && ss.isEmpty();*/
    }
    // </editor-fold>
//