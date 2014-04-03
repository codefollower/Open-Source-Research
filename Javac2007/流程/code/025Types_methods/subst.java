// <editor-fold defaultstate="collapsed" desc="subst">
    public List<Type> subst(List<Type> ts,
                            List<Type> from,
                            List<Type> to) {
        //return new Subst(from, to).subst(ts);

		DEBUG.P(this,"subst(3)");
		DEBUG.P("ts="+ts);
		DEBUG.P("from="+from);
		DEBUG.P("to  ="+to);
		
		DEBUG.off();
		List<Type> returnTypes = new Subst(from, to).subst(ts);
		DEBUG.on();
            
		DEBUG.P("returnTypes="+returnTypes);
		DEBUG.P(1,this,"subst(3)");
		return returnTypes;
    }

    /**
     * Substitute all occurrences of a type in `from' with the
     * corresponding type in `to' in 't'. Match lists `from' and `to'
     * from the right: If lists have different length, discard leading
     * elements of the longer list.
     */
    public Type subst(Type t, List<Type> from, List<Type> to) {
        //return new Subst(from, to).subst(t);
		DEBUG.P(this,"subst(3)");
		DEBUG.P("t="+t+" t.tag="+TypeTags.toString(t.tag));
		DEBUG.P("from="+from);
		DEBUG.P("to  ="+to);
		DEBUG.off();
		Type returnType = new Subst(from, to).subst(t);
		DEBUG.on();
            
		DEBUG.P("returnType="+returnType);
		DEBUG.P(1,this,"subst(3)");
		return returnType;
    }

    private class Subst extends UnaryVisitor<Type> {
        List<Type> from;
        List<Type> to;

        public Subst(List<Type> from, List<Type> to) {
			DEBUG.P(this,"Subst(2)");
            int fromLength = from.length();
            int toLength = to.length();

			DEBUG.P("fromLength="+fromLength);
			DEBUG.P("toLength  ="+toLength);

            while (fromLength > toLength) {
                fromLength--;
                from = from.tail;
            }
            while (fromLength < toLength) {
                toLength--;
                to = to.tail;
            }
            this.from = from;
            this.to = to;
			DEBUG.P("this.from="+this.from);
			DEBUG.P("this.to  ="+this.to);
			DEBUG.P(0,this,"Subst(2)");
        }

        Type subst(Type t) {
			try {//我加上的
			DEBUG.P(this,"subst(Type t)");
			DEBUG.P("t="+t+" t.tag="+TypeTags.toString(t.tag));

            if (from.tail == null)
                return t;
            else
                return visit(t);

			}finally{//我加上的
			DEBUG.P(0,this,"subst(Type t)");
			}
        }

        List<Type> subst(List<Type> ts) {
			try {//我加上的
			DEBUG.P(this,"subst(List<Type> ts)");
			DEBUG.P("ts="+ts);

            if (from.tail == null)
                return ts;
            boolean wild = false;
            if (ts.nonEmpty() && from.nonEmpty()) {
                Type head1 = subst(ts.head);
				DEBUG.P("(head1 == ts.head)="+(head1 == ts.head));
                List<Type> tail1 = subst(ts.tail);
                if (head1 != ts.head || tail1 != ts.tail)
                    return tail1.prepend(head1);
            }
            return ts;

			}finally{//我加上的
			DEBUG.P(0,this,"subst(List<Type> ts)");
			}
        }

        public Type visitType(Type t, Void ignored) {
            return t;
        }

        @Override
        public Type visitMethodType(MethodType t, Void ignored) {
			try {//我加上的
			DEBUG.P(this,"visitMethodType(2)");

            List<Type> argtypes = subst(t.argtypes);
            Type restype = subst(t.restype);
            List<Type> thrown = subst(t.thrown);
            if (argtypes == t.argtypes &&
                restype == t.restype &&
                thrown == t.thrown)
                return t;
            else
                return new MethodType(argtypes, restype, thrown, t.tsym);

			}finally{//我加上的
			DEBUG.P(0,this,"visitMethodType(2)");
			}
        }

        @Override
        public Type visitTypeVar(TypeVar t, Void ignored) {
			try {//我加上的
			DEBUG.P(this,"visitTypeVar(2)");

            for (List<Type> from = this.from, to = this.to;
                 from.nonEmpty();
                 from = from.tail, to = to.tail) {
				DEBUG.P("t="+t);
				DEBUG.P("from.head="+from.head);
				DEBUG.P("(t == from.head)="+(t == from.head));
                if (t == from.head) {
					DEBUG.P("return to.head="+to.head);
                    return to.head.withTypeVar(t);
                }
            }
            return t;

			}finally{//我加上的
			DEBUG.P(0,this,"visitTypeVar(2)");
			}
        }

        @Override
        public Type visitClassType(ClassType t, Void ignored) {
			try {//我加上的
			DEBUG.P(this,"visitClassType(2)");
			DEBUG.P("t="+t+" t.tag="+TypeTags.toString(t.tag));
			DEBUG.P("t.isCompound()="+t.isCompound());

            if (!t.isCompound()) {
                List<Type> typarams = t.getTypeArguments();
                List<Type> typarams1 = subst(typarams);
                Type outer = t.getEnclosingType();
                Type outer1 = subst(outer);
                if (typarams1 == typarams && outer1 == outer)
                    return t;
                else
                    return new ClassType(outer1, typarams1, t.tsym);
            } else {
                Type st = subst(supertype(t));
                List<Type> is = upperBounds(subst(interfaces(t)));
                if (st == supertype(t) && is == interfaces(t))
                    return t;
                else
                    return makeCompoundType(is.prepend(st));
            }

			}finally{//我加上的
			DEBUG.P(0,this,"visitClassType(2)");
			}
        }

        @Override
        public Type visitWildcardType(WildcardType t, Void ignored) {
			try {//我加上的
			DEBUG.P(this,"visitWildcardType(2)");
			DEBUG.P("t="+t);
			DEBUG.P("t.type="+t.type);
			DEBUG.P("t.bound="+t.bound);

            Type bound = t.type; //注意这里不是t.bound
            if (t.kind != BoundKind.UNBOUND)
                bound = subst(bound);

			DEBUG.P("t.type="+t.type);
			DEBUG.P("bound="+bound);
			DEBUG.P("(bound == t.type)="+(bound == t.type));
            if (bound == t.type) {
                return t;
            } else {
				DEBUG.P("t.isExtendsBound()="+t.isExtendsBound());
				if(bound!=null) DEBUG.P("bound.isExtendsBound()="+bound.isExtendsBound());
                if (t.isExtendsBound() && bound.isExtendsBound())
                    bound = upperBound(bound);
                return new WildcardType(bound, t.kind, syms.boundClass, t.bound);
            }

			}finally{//我加上的
			DEBUG.P(0,this,"visitWildcardType(2)");
			}
        }

        @Override
        public Type visitArrayType(ArrayType t, Void ignored) {
            Type elemtype = subst(t.elemtype);
            if (elemtype == t.elemtype)
                return t;
            else
                return new ArrayType(upperBound(elemtype), t.tsym);
        }

        @Override
        public Type visitForAll(ForAll t, Void ignored) {
			try {//我加上的
			DEBUG.P(this,"visitForAll(2)");
			DEBUG.P("t="+t);

            List<Type> tvars1 = substBounds(t.tvars, from, to);

			DEBUG.P("t.tvars="+t.tvars);
			DEBUG.P("tvars1 ="+tvars1);
            Type qtype1 = subst(t.qtype);

			DEBUG.P("(tvars1 == t.tvars) ="+(tvars1 == t.tvars));
			DEBUG.P("(qtype1 == t.qtype) ="+(qtype1 == t.qtype));
            if (tvars1 == t.tvars && qtype1 == t.qtype) {
                return t;
            } else if (tvars1 == t.tvars) {
                return new ForAll(tvars1, qtype1);
            } else {
                return new ForAll(tvars1, Types.this.subst(qtype1, t.tvars, tvars1));
            }

			}finally{//我加上的
			DEBUG.P(0,this,"visitForAll(2)");
			}
        }

        @Override
        public Type visitErrorType(ErrorType t, Void ignored) {
            return t;
        }
    }

    public List<Type> substBounds(List<Type> tvars,
                                  List<Type> from,
                                  List<Type> to) {
		try {//我加上的
		DEBUG.P(this,"substBounds(3)");
		DEBUG.P("tvars="+tvars);
		DEBUG.P("from ="+from);
		DEBUG.P("to   ="+to);

		DEBUG.P("tvars.isEmpty()="+tvars.isEmpty());
        if (tvars.isEmpty())
            return tvars;

		DEBUG.P("tvars.tail.isEmpty()="+tvars.tail.isEmpty());
        if (tvars.tail.isEmpty())
            // fast common case
            return List.<Type>of(substBound((TypeVar)tvars.head, from, to));
        ListBuffer<Type> newBoundsBuf = lb();
        boolean changed = false;
        // calculate new bounds
        for (Type t : tvars) {
            TypeVar tv = (TypeVar) t;
            Type bound = subst(tv.bound, from, to);
            if (bound != tv.bound)
                changed = true;
            newBoundsBuf.append(bound);
        }
		DEBUG.P("changed="+changed);
        if (!changed)
            return tvars;
        ListBuffer<Type> newTvars = lb();
        // create new type variables without bounds
        for (Type t : tvars) {
            newTvars.append(new TypeVar(t.tsym, null));
        }
        // the new bounds should use the new type variables in place
        // of the old
        List<Type> newBounds = newBoundsBuf.toList();
        from = tvars;
        to = newTvars.toList();
        for (; !newBounds.isEmpty(); newBounds = newBounds.tail) {
            newBounds.head = subst(newBounds.head, from, to);
        }
        newBounds = newBoundsBuf.toList();
        // set the bounds of new type variables to the new bounds
        for (Type t : newTvars.toList()) {
            TypeVar tv = (TypeVar) t;
            tv.bound = newBounds.head;
            newBounds = newBounds.tail;
        }
        return newTvars.toList();

		}finally{//我加上的
		DEBUG.P(0,this,"substBounds(3)");
		}
    }

    public TypeVar substBound(TypeVar t, List<Type> from, List<Type> to) {
    	try {//我加上的
		DEBUG.P(this,"substBound(3)");
		DEBUG.P("t="+t+" t.tag="+TypeTags.toString(t.tag));
		DEBUG.P("from="+from);
		DEBUG.P("to="+to);
		
        Type bound1 = subst(t.bound, from, to);
        DEBUG.P("(bound1 == t.bound)="+(bound1 == t.bound));
        if (bound1 == t.bound)
            return t;
        else
            return new TypeVar(t.tsym, bound1);
            
        }finally{//我加上的
		DEBUG.P(1,this,"substBound(3)");
		}
    }
// </editor-fold>