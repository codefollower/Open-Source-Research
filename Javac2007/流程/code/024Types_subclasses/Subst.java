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
            for (List<Type> from = this.from, to = this.to;
                 from.nonEmpty();
                 from = from.tail, to = to.tail) {
                if (t == from.head) {
                    return to.head.withTypeVar(t);
                }
            }
            return t;
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
            Type bound = t.type;
            if (t.kind != BoundKind.UNBOUND)
                bound = subst(bound);
            if (bound == t.type) {
                return t;
            } else {
                if (t.isExtendsBound() && bound.isExtendsBound())
                    bound = upperBound(bound);
                return new WildcardType(bound, t.kind, syms.boundClass, t.bound);
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
            List<Type> tvars1 = substBounds(t.tvars, from, to);
            Type qtype1 = subst(t.qtype);
            if (tvars1 == t.tvars && qtype1 == t.qtype) {
                return t;
            } else if (tvars1 == t.tvars) {
                return new ForAll(tvars1, qtype1);
            } else {
                return new ForAll(tvars1, Types.this.subst(qtype1, t.tvars, tvars1));
            }
        }

        @Override
        public Type visitErrorType(ErrorType t, Void ignored) {
            return t;
        }
    }