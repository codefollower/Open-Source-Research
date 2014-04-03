    // <editor-fold defaultstate="collapsed" desc="Determining least upper bounds of types">
    /**
     * A cache for closures.
     *
     * <p>A closure is a list of all the supertypes and interfaces of
     * a class or interface type, ordered by ClassSymbol.precedes
     * (that is, subclasses come first, arbitrary but fixed
     * otherwise).
     */
    private Map<Type,List<Type>> closureCache = new HashMap<Type,List<Type>>();

    /**
     * Returns the closure of a class or interface type.
     */
    public List<Type> closure(Type t) {
		DEBUG.P(this,"closure(Type t)");
		DEBUG.P("t="+t+" t.tag="+TypeTags.toString(t.tag));

        List<Type> cl = closureCache.get(t);
		DEBUG.P("cl="+cl);
        if (cl == null) {
            Type st = supertype(t);
			DEBUG.P("t.isCompound()="+t.isCompound());
			DEBUG.P("st.tag="+TypeTags.toString(t.tag));
            if (!t.isCompound()) {
                if (st.tag == CLASS) {
                    cl = insert(closure(st), t);
                } else if (st.tag == TYPEVAR) {
                    cl = closure(st).prepend(t);
                } else {
                    cl = List.of(t);
                }
            } else {
                cl = closure(supertype(t));
            }
            for (List<Type> l = interfaces(t); l.nonEmpty(); l = l.tail)
                cl = union(cl, closure(l.head));
            closureCache.put(t, cl);
        }
		DEBUG.P("cl="+cl);
		DEBUG.P(0,this,"closure(Type t)");
        return cl;
    }
    /**
     * Insert a type in a closure
     */
    public List<Type> insert(List<Type> cl, Type t) {
		try {//我加上的
		DEBUG.P(this,"insert(2)");
		DEBUG.P("cl="+cl);
		DEBUG.P("t="+t+"  t.tag="+TypeTags.toString(t.tag));

        if (cl.isEmpty() || t.tsym.precedes(cl.head.tsym, this)) {
            return cl.prepend(t);
        } else if (cl.head.tsym.precedes(t.tsym, this)) {
            return insert(cl.tail, t).prepend(cl.head);
        } else {
            return cl;
        }

		}finally{//我加上的
		DEBUG.P(0,this,"insert(2)");
		}
    }

    /**
     * Form the union of two closures
     */
    public List<Type> union(List<Type> cl1, List<Type> cl2) {
		try {//我加上的
		DEBUG.P(this,"union(2)");
		DEBUG.P("cl1="+cl1);
		DEBUG.P("cl2="+cl2);

        if (cl1.isEmpty()) {
            return cl2;
        } else if (cl2.isEmpty()) {
            return cl1;
        } else if (cl1.head.tsym.precedes(cl2.head.tsym, this)) {
            return union(cl1.tail, cl2).prepend(cl1.head);
        } else if (cl2.head.tsym.precedes(cl1.head.tsym, this)) {
            return union(cl1, cl2.tail).prepend(cl2.head);
        } else {
            return union(cl1.tail, cl2.tail).prepend(cl1.head);
        }

		}finally{//我加上的
		DEBUG.P(0,this,"union(2)");;
		}
    }

    /**
     * Intersect two closures
     */
    public List<Type> intersect(List<Type> cl1, List<Type> cl2) {
        if (cl1 == cl2)
            return cl1;
        if (cl1.isEmpty() || cl2.isEmpty())
            return List.nil();
        if (cl1.head.tsym.precedes(cl2.head.tsym, this))
            return intersect(cl1.tail, cl2);
        if (cl2.head.tsym.precedes(cl1.head.tsym, this))
            return intersect(cl1, cl2.tail);
        if (isSameType(cl1.head, cl2.head))
            return intersect(cl1.tail, cl2.tail).prepend(cl1.head);
        if (cl1.head.tsym == cl2.head.tsym &&
            cl1.head.tag == CLASS && cl2.head.tag == CLASS) {
            if (cl1.head.isParameterized() && cl2.head.isParameterized()) {
                Type merge = merge(cl1.head,cl2.head);
                return intersect(cl1.tail, cl2.tail).prepend(merge);
            }
            if (cl1.head.isRaw() || cl2.head.isRaw())
                return intersect(cl1.tail, cl2.tail).prepend(erasure(cl1.head));
        }
        return intersect(cl1.tail, cl2.tail);
    }
    // where
        class TypePair {
            final Type t1;
            final Type t2;
            TypePair(Type t1, Type t2) {
                this.t1 = t1;
                this.t2 = t2;
            }
            @Override
            public int hashCode() {
                return 127 * Types.this.hashCode(t1) + Types.this.hashCode(t2);
            }
            @Override
            public boolean equals(Object obj) {
                if (!(obj instanceof TypePair))
                    return false;
                TypePair typePair = (TypePair)obj;
                return isSameType(t1, typePair.t1)
                    && isSameType(t2, typePair.t2);
            }
        }
        Set<TypePair> mergeCache = new HashSet<TypePair>();
        private Type merge(Type c1, Type c2) {
            ClassType class1 = (ClassType) c1;
            List<Type> act1 = class1.getTypeArguments();
            ClassType class2 = (ClassType) c2;
            List<Type> act2 = class2.getTypeArguments();
            ListBuffer<Type> merged = new ListBuffer<Type>();
            List<Type> typarams = class1.tsym.type.getTypeArguments();

            while (act1.nonEmpty() && act2.nonEmpty() && typarams.nonEmpty()) {
                if (containsType(act1.head, act2.head)) {
                    merged.append(act1.head);
                } else if (containsType(act2.head, act1.head)) {
                    merged.append(act2.head);
                } else {
                    TypePair pair = new TypePair(c1, c2);
                    Type m;
                    if (mergeCache.add(pair)) {
                        m = new WildcardType(lub(upperBound(act1.head),
                                                 upperBound(act2.head)),
                                             BoundKind.EXTENDS,
                                             syms.boundClass);
                        mergeCache.remove(pair);
                    } else {
                        m = new WildcardType(syms.objectType,
                                             BoundKind.UNBOUND,
                                             syms.boundClass);
                    }
                    merged.append(m.withTypeVar(typarams.head));
                }
                act1 = act1.tail;
                act2 = act2.tail;
                typarams = typarams.tail;
            }
            assert(act1.isEmpty() && act2.isEmpty() && typarams.isEmpty());
            return new ClassType(class1.getEnclosingType(), merged.toList(), class1.tsym);
        }

    /**
     * Return the minimum type of a closure, a compound type if no
     * unique minimum exists.
     */
    private Type compoundMin(List<Type> cl) {
        if (cl.isEmpty()) return syms.objectType;
        List<Type> compound = closureMin(cl);
        if (compound.isEmpty())
            return null;
        else if (compound.tail.isEmpty())
            return compound.head;
        else
            return makeCompoundType(compound);
    }

    /**
     * Return the minimum types of a closure, suitable for computing
     * compoundMin or glb.
     */
    private List<Type> closureMin(List<Type> cl) {
        ListBuffer<Type> classes = lb();
        ListBuffer<Type> interfaces = lb();
        while (!cl.isEmpty()) {
            Type current = cl.head;
            if (current.isInterface())
                interfaces.append(current);
            else
                classes.append(current);
            ListBuffer<Type> candidates = lb();
            for (Type t : cl.tail) {
                if (!isSubtypeNoCapture(current, t))
                    candidates.append(t);
            }
            cl = candidates.toList();
        }
        return classes.appendList(interfaces).toList();
    }

    /**
     * Return the least upper bound of pair of types.  if the lub does
     * not exist return null.
     */
    public Type lub(Type t1, Type t2) {
        return lub(List.of(t1, t2));
    }

    /**
     * Return the least upper bound (lub) of set of types.  If the lub
     * does not exist return the type of null (bottom).
     */
    public Type lub(List<Type> ts) {
        final int ARRAY_BOUND = 1;
        final int CLASS_BOUND = 2;
        int boundkind = 0;
        for (Type t : ts) {
            switch (t.tag) {
            case CLASS:
                boundkind |= CLASS_BOUND;
                break;
            case ARRAY:
                boundkind |= ARRAY_BOUND;
                break;
            case  TYPEVAR:
                do {
                    t = t.getUpperBound();
                } while (t.tag == TYPEVAR);
                if (t.tag == ARRAY) {
                    boundkind |= ARRAY_BOUND;
                } else {
                    boundkind |= CLASS_BOUND;
                }
                break;
            default:
                if (t.isPrimitive())
                    return syms.botType;
            }
        }
        switch (boundkind) {
        case 0:
            return syms.botType;

        case ARRAY_BOUND:
            // calculate lub(A[], B[])
            List<Type> elements = Type.map(ts, elemTypeFun);
            for (Type t : elements) {
                if (t.isPrimitive()) {
                    // if a primitive type is found, then return
                    // arraySuperType unless all the types are the
                    // same
                    Type first = ts.head;
                    for (Type s : ts.tail) {
                        if (!isSameType(first, s)) {
                             // lub(int[], B[]) is Cloneable & Serializable
                            return arraySuperType();
                        }
                    }
                    // all the array types are the same, return one
                    // lub(int[], int[]) is int[]
                    return first;
                }
            }
            // lub(A[], B[]) is lub(A, B)[]
            return new ArrayType(lub(elements), syms.arrayClass);

        case CLASS_BOUND:
            // calculate lub(A, B)
            while (ts.head.tag != CLASS && ts.head.tag != TYPEVAR)
                ts = ts.tail;
            assert !ts.isEmpty();
            List<Type> cl = closure(ts.head);
            for (Type t : ts.tail) {
                if (t.tag == CLASS || t.tag == TYPEVAR)
                    cl = intersect(cl, closure(t));
            }
            return compoundMin(cl);

        default:
            // calculate lub(A, B[])
            List<Type> classes = List.of(arraySuperType());
            for (Type t : ts) {
                if (t.tag != ARRAY) // Filter out any arrays
                    classes = classes.prepend(t);
            }
            // lub(A, B[]) is lub(A, arraySuperType)
            return lub(classes);
        }
    }
    // where
        private Type arraySuperType = null;
        private Type arraySuperType() {
            // initialized lazily to avoid problems during compiler startup
            if (arraySuperType == null) {
                synchronized (this) {
                    if (arraySuperType == null) {
                        // JLS 10.8: all arrays implement Cloneable and Serializable.
                        arraySuperType = makeCompoundType(List.of(syms.serializableType,
                                                                  syms.cloneableType),
                                                          syms.objectType);
                    }
                }
            }
            return arraySuperType;
        }
    // </editor-fold>