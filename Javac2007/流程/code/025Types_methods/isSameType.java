//isSameType
    // <editor-fold defaultstate="collapsed" desc="isSameType">
    /**
     * Are corresponding elements of the lists the same type?  If
     * lists are of different length, return false.
     */
    public boolean isSameTypes(List<Type> ts, List<Type> ss) {
        while (ts.tail != null && ss.tail != null
               /*inlined: ts.nonEmpty() && ss.nonEmpty()*/ &&
               isSameType(ts.head, ss.head)) {
            ts = ts.tail;
            ss = ss.tail;
        }
        return ts.tail == null && ss.tail == null;
        /*inlined: ts.isEmpty() && ss.isEmpty();*/
    }

    /**
     * Is t the same type as s?
     */
    public boolean isSameType(Type t, Type s) {
        //return isSameType.visit(t, s);
        
        DEBUG.P(this,"isSameType(Type t, Type s)");
		DEBUG.P("t="+t+"  t.tag="+TypeTags.toString(t.tag));
		DEBUG.P("s="+s+"  s.tag="+TypeTags.toString(s.tag));
		
		boolean returnResult= isSameType.visit(t, s);
            
		DEBUG.P("returnResult="+returnResult);
		DEBUG.P(0,this,"isSameType(Type t, Type s)");
		return returnResult;
    }
    // where
        private TypeRelation isSameType = new TypeRelation() {

            public Boolean visitType(Type t, Type s) {
				try {//我加上的
				DEBUG.P(this,"visitType(Type t, Type s)");
				DEBUG.P("t="+t+"  t.tag="+TypeTags.toString(t.tag));
				DEBUG.P("s="+s+"  s.tag="+TypeTags.toString(s.tag));

                if (t == s)
                    return true;

                if (s.tag >= firstPartialTag)
                    return visit(s, t);

                switch (t.tag) {
                case BYTE: case CHAR: case SHORT: case INT: case LONG: case FLOAT:
                case DOUBLE: case BOOLEAN: case VOID: case BOT: case NONE:
                    return t.tag == s.tag;
                case TYPEVAR:
					DEBUG.P("s.isSuperBound()   ="+s.isSuperBound());
					DEBUG.P("!s.isExtendsBound()="+!s.isExtendsBound());
                    return s.isSuperBound()
                        && !s.isExtendsBound()
                        && visit(t, upperBound(s));
                default:
                    throw new AssertionError("isSameType " + t.tag);
                }

				}finally{//我加上的
				DEBUG.P(0,this,"visitType(Type t, Type s)");
				}
            }

            @Override
            public Boolean visitWildcardType(WildcardType t, Type s) {
                if (s.tag >= firstPartialTag)
                    return visit(s, t);
                else
                    return false;
            }

            @Override
            public Boolean visitClassType(ClassType t, Type s) {
            	try {//我加上的
				DEBUG.P(this,"visitClassType(ClassType t, Type s)");
				DEBUG.P("t="+t+"  t.tag="+TypeTags.toString(t.tag));
				DEBUG.P("s="+s+"  s.tag="+TypeTags.toString(s.tag));
				
				DEBUG.P("(t == s)="+(t == s));
                if (t == s)
                    return true;

				DEBUG.P("(s.tag="+s.tag+") >= (firstPartialTag="+firstPartialTag+")="+(s.tag >= firstPartialTag));
				
                if (s.tag >= firstPartialTag)
                    return visit(s, t);
                /*    
                泛型类定义  :Test<T extends Number>
                参数化类型t :Test<Number>
                参数化类型s :Test<? super Float>
                
                则t是一个ClassType，而s是一个super型的WildcardType
                upperBound(s)=Number,lowerBound(s)=Float
                所以t=upperBound(s)，但t!=lowerBound(s)
                所以isSameType(t,s)=false
                */

				DEBUG.P("s.isSuperBound()   ="+s.isSuperBound());
				DEBUG.P("!s.isExtendsBound()="+!s.isExtendsBound());
                if (s.isSuperBound() && !s.isExtendsBound())
                    return visit(t, upperBound(s)) && visit(t, lowerBound(s));

				DEBUG.P("t.isCompound()="+t.isCompound());
				DEBUG.P("s.isCompound()="+s.isCompound());
                if (t.isCompound() && s.isCompound()) {
                    if (!visit(supertype(t), supertype(s)))
                        return false;

                    HashSet<SingletonType> set = new HashSet<SingletonType>();
                    for (Type x : interfaces(t))
                        set.add(new SingletonType(x));
                    for (Type x : interfaces(s)) {
                    	//在调用HashSet的remove时会间接调用SingletonType中
                    	//定义的equals方法，由此再调用isSameType方法。
                        if (!set.remove(new SingletonType(x)))
                            return false;
                    }
                    return (set.size() == 0);
                }

				DEBUG.P("(t.tsym == s.tsym)="+(t.tsym == s.tsym));
                return t.tsym == s.tsym
                    && visit(t.getEnclosingType(), s.getEnclosingType())
                    && containsTypeEquivalent(t.getTypeArguments(), s.getTypeArguments());
                    
                }finally{//我加上的
				DEBUG.P(0,this,"visitClassType(ClassType t, Type s)");
				}
            }

            @Override
            public Boolean visitArrayType(ArrayType t, Type s) {
                if (t == s)
                    return true;

                if (s.tag >= firstPartialTag)
                    return visit(s, t);

                return s.tag == ARRAY
                    && containsTypeEquivalent(t.elemtype, elemtype(s));
            }

            @Override
            public Boolean visitMethodType(MethodType t, Type s) {
                // isSameType for methods does not take thrown
                // exceptions into account!
                return hasSameArgs(t, s) && visit(t.getReturnType(), s.getReturnType());
            }

            @Override
            public Boolean visitPackageType(PackageType t, Type s) {
                return t == s;
            }

            @Override
            public Boolean visitForAll(ForAll t, Type s) {
                if (s.tag != FORALL)
                    return false;

                ForAll forAll = (ForAll)s;
                return hasSameBounds(t, forAll)
                    && visit(t.qtype, subst(forAll.qtype, forAll.tvars, t.tvars));
            }

            @Override
            public Boolean visitUndetVar(UndetVar t, Type s) {
                if (s.tag == WILDCARD)
                    // FIXME, this might be leftovers from before capture conversion
                    return false;

                if (t == s || t.qtype == s || s.tag == ERROR || s.tag == UNKNOWN)
                    return true;

                if (t.inst != null)
                    return visit(t.inst, s);

                t.inst = fromUnknownFun.apply(s);
                for (List<Type> l = t.lobounds; l.nonEmpty(); l = l.tail) {
                    if (!isSubtype(l.head, t.inst))
                        return false;
                }
                for (List<Type> l = t.hibounds; l.nonEmpty(); l = l.tail) {
                    if (!isSubtype(t.inst, l.head))
                        return false;
                }
                return true;
            }

            @Override
            public Boolean visitErrorType(ErrorType t, Type s) {
                return true;
            }
        };
    // </editor-fold>
//