//isCastable
    // <editor-fold defaultstate="collapsed" desc="isCastable">
    public boolean isCastable(Type t, Type s) {
    	try {//我加上的
		DEBUG.P(this,"isCastable(2)");
        return isCastable(t, s, Warner.noWarnings);
        }finally{//我加上的
		DEBUG.P(1,this,"isCastable(2)");
		}
    }

    /**
     * Is t is castable to s?<br>
     * s is assumed to be an erased type.<br>
     * (not defined for Method and ForAll types).
     */
	//相当于(s)t，如
	//ClassA a;
	//classB b=(ClassB)a;
	//此时s=ClassB;t=ClassA
	//不管ClassA与classB哪个是超类哪个是子类，两者之间都可相互强制转换，
	//编译期间不会报错，只有在运行时才会判断是否合法
    public boolean isCastable(Type t, Type s, Warner warn) {
		boolean returnResult=true;//我加上的
    	try {//我加上的
		DEBUG.P(this,"isCastable(3)");
		DEBUG.P("t="+t+"  t.tag="+TypeTags.toString(t.tag));
		DEBUG.P("s="+s+"  s.tag="+TypeTags.toString(s.tag));
		DEBUG.P("t.isPrimitive()="+t.isPrimitive());
		DEBUG.P("s.isPrimitive()="+s.isPrimitive());

		DEBUG.P("if (t == s)="+(t == s));
		
		
        if (t == s)
            return true;

		DEBUG.P("if (t.isPrimitive() != s.isPrimitive())="+(t.isPrimitive() != s.isPrimitive()));

        if (t.isPrimitive() != s.isPrimitive()) {
			//return allowBoxing && isConvertible(t, s, warn);
			returnResult=allowBoxing && isConvertible(t, s, warn);
			return returnResult;
		}

		DEBUG.P("if (warn != warnStack.head)="+(warn != warnStack.head));

        if (warn != warnStack.head) {
            try {
                warnStack = warnStack.prepend(warn);
                //return isCastable.visit(t, s);
				returnResult=isCastable.visit(t, s);
				return returnResult;
            } finally {
                warnStack = warnStack.tail;
            }
        } else {
            //return isCastable.visit(t, s);
			returnResult=isCastable.visit(t, s);
			return returnResult;
        }
        
        }finally{//我加上的
		DEBUG.P("");
		DEBUG.P("returnResult="+returnResult);
		DEBUG.P(1,this,"isCastable(3)");
		}
    }
    // where
        private TypeRelation isCastable = new TypeRelation() {

            public Boolean visitType(Type t, Type s) {
                if (s.tag == ERROR)
                    return true;

                switch (t.tag) {
                case BYTE: case CHAR: case SHORT: case INT: case LONG: case FLOAT:
                case DOUBLE:
                    return s.tag <= DOUBLE;
                case BOOLEAN:
                    return s.tag == BOOLEAN;
                case VOID:
                    return false;
                case BOT:
                    return isSubtype(t, s);
                default:
                    throw new AssertionError();
                }
            }

            @Override
            public Boolean visitWildcardType(WildcardType t, Type s) {
                return isCastable(upperBound(t), s, warnStack.head);
            }

            @Override
            public Boolean visitClassType(ClassType t, Type s) {
				try {//我加上的
				DEBUG.P(this,"visitClassType(2)");
				DEBUG.P("t="+t+"  t.tag="+TypeTags.toString(t.tag));
				DEBUG.P("s="+s+"  s.tag="+TypeTags.toString(s.tag));

                if (s.tag == ERROR || s.tag == BOT)
                    return true;

                if (s.tag == TYPEVAR) {
                    if (isCastable(s.getUpperBound(), t, Warner.noWarnings)) {
                        warnStack.head.warnUnchecked();
                        return true;
                    } else {
                        return false;
                    }
                }

				DEBUG.P("t.isCompound()="+t.isCompound());
                if (t.isCompound()) {
                    if (!visit(supertype(t), s))
                        return false;
                    for (Type intf : interfaces(t)) {
                        if (!visit(intf, s))
                            return false;
                    }
                    return true;
                }

                DEBUG.P("s.isCompound()="+s.isCompound());
				if (s.isCompound()) {
                    // call recursively to reuse the above code
                    return visitClassType((ClassType)s, t);
                }

                DEBUG.P("s="+s+"  s.tag="+TypeTags.toString(s.tag));
				if (s.tag == CLASS || s.tag == ARRAY) {
                    boolean upcast;
                    if ((upcast = isSubtype(erasure(t), erasure(s)))
                        || isSubtype(erasure(s), erasure(t))) {

						DEBUG.P("upcast="+upcast);
						DEBUG.P("s="+s+"  s.tag="+TypeTags.toString(s.tag));
						DEBUG.P("s.isRaw()="+s.isRaw());
						DEBUG.P("t.isRaw()="+t.isRaw());

                        if (!upcast && s.tag == ARRAY) {
                            if (!isReifiable(s))
                                warnStack.head.warnUnchecked();
                            return true;
                        } else if (s.isRaw()) {
                            return true;
                        } else if (t.isRaw()) {
                            if (!isUnbounded(s))
                                warnStack.head.warnUnchecked();
                            return true;
                        }
                        // Assume |a| <: |b|
						//当upcast=true 时，表示从子类转换到超类
						//当upcast=false时，表示从超类转换到子类
						//a总是子类，b总是超类
						//|a| <: |b|表示在一棵继承树上，类a是类b的子类。
                        final Type a = upcast ? t : s;
                        final Type b = upcast ? s : t;
                        final boolean HIGH = true;
                        final boolean LOW = false;
                        final boolean DONT_REWRITE_TYPEVARS = false;
                        Type aHigh = rewriteQuantifiers(a, HIGH, DONT_REWRITE_TYPEVARS);
						DEBUG.P("aHigh="+aHigh+"  aHigh.tag="+TypeTags.toString(aHigh.tag));
                        Type aLow  = rewriteQuantifiers(a, LOW,  DONT_REWRITE_TYPEVARS);
						DEBUG.P("aLow="+aLow+"  aLow.tag="+TypeTags.toString(aLow.tag));
                        Type bHigh = rewriteQuantifiers(b, HIGH, DONT_REWRITE_TYPEVARS);
						DEBUG.P("bHigh="+bHigh+"  bHigh.tag="+TypeTags.toString(bHigh.tag));
                        Type bLow  = rewriteQuantifiers(b, LOW,  DONT_REWRITE_TYPEVARS);
						DEBUG.P("bLow="+bLow+"  bLow.tag="+TypeTags.toString(bLow.tag));
                        Type lowSub = asSub(bLow, aLow.tsym);
						DEBUG.P("lowSub="+lowSub);
                        Type highSub = (lowSub == null) ? null : asSub(bHigh, aHigh.tsym);
						DEBUG.P("highSub="+highSub);
                        if (highSub == null) {
                            final boolean REWRITE_TYPEVARS = true;
                            aHigh = rewriteQuantifiers(a, HIGH, REWRITE_TYPEVARS);
                            aLow  = rewriteQuantifiers(a, LOW,  REWRITE_TYPEVARS);
                            bHigh = rewriteQuantifiers(b, HIGH, REWRITE_TYPEVARS);
                            bLow  = rewriteQuantifiers(b, LOW,  REWRITE_TYPEVARS);
                            lowSub = asSub(bLow, aLow.tsym);
                            highSub = (lowSub == null) ? null : asSub(bHigh, aHigh.tsym);
                        }
                        DEBUG.P("highSub="+highSub);
                        if (highSub != null) {
                            assert a.tsym == highSub.tsym && a.tsym == lowSub.tsym
                                : a.tsym + " != " + highSub.tsym + " != " + lowSub.tsym;
                            if (!disjointTypes(aHigh.getTypeArguments(), highSub.getTypeArguments())
                                && !disjointTypes(aHigh.getTypeArguments(), lowSub.getTypeArguments())
                                && !disjointTypes(aLow.getTypeArguments(), highSub.getTypeArguments())
                                && !disjointTypes(aLow.getTypeArguments(), lowSub.getTypeArguments())) {
                                if (upcast ? giveWarning(a, highSub) || giveWarning(a, lowSub)
                                           : giveWarning(highSub, a) || giveWarning(lowSub, a))
                                    warnStack.head.warnUnchecked();
                                return true;
                            }
                        }
                        if (isReifiable(s))
                            return isSubtypeUnchecked(a, b);
                        else
                            return isSubtypeUnchecked(a, b, warnStack.head);
                    }
                    DEBUG.P("");
                    DEBUG.P("s="+s+"  s.tag="+TypeTags.toString(s.tag));
                    // Sidecast
                    if (s.tag == CLASS) {
                    	DEBUG.P("s.tsym.flags()="+Flags.toString(s.tsym.flags()));
                    	DEBUG.P("t.tsym.flags() ="+Flags.toString(t.tsym.flags() ));
                        if ((s.tsym.flags() & INTERFACE) != 0) {
                            return ((t.tsym.flags() & FINAL) == 0)
                                ? sideCast(t, s, warnStack.head)
                                : sideCastFinal(t, s, warnStack.head);
                        } else if ((t.tsym.flags() & INTERFACE) != 0) {
                            return ((s.tsym.flags() & FINAL) == 0)
                                ? sideCast(t, s, warnStack.head)
                                : sideCastFinal(t, s, warnStack.head);
                        } else {
                            // unrelated class types
                            return false;
                        }
                    }
                }
                return false;

				}finally{//我加上的
				DEBUG.P(0,this,"visitClassType(2)");
				}
            }

            @Override
            public Boolean visitArrayType(ArrayType t, Type s) {
                switch (s.tag) {
                case ERROR:
                case BOT:
                    return true;
                case TYPEVAR:
                    if (isCastable(s, t, Warner.noWarnings)) {
                        warnStack.head.warnUnchecked();
                        return true;
                    } else {
                        return false;
                    }
                case CLASS:
                    return isSubtype(t, s);
                case ARRAY:
                    if (elemtype(t).tag <= lastBaseTag) {
                        return elemtype(t).tag == elemtype(s).tag;
                    } else {
                        return visit(elemtype(t), elemtype(s));
                    }
                default:
                    return false;
                }
            }

            @Override
            public Boolean visitTypeVar(TypeVar t, Type s) {
                switch (s.tag) {
                case ERROR:
                case BOT:
                    return true;
                case TYPEVAR:
                    if (isSubtype(t, s)) {
                        return true;
                    } else if (isCastable(t.bound, s, Warner.noWarnings)) {
                        warnStack.head.warnUnchecked();
                        return true;
                    } else {
                        return false;
                    }
                default:
                    return isCastable(t.bound, s, warnStack.head);
                }
            }

            @Override
            public Boolean visitErrorType(ErrorType t, Type s) {
                return true;
            }
        };
    // </editor-fold>
//