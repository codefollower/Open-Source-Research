    /**
     * Rewrite all type variables (universal quantifiers) in the given
     * type to wildcards (existential quantifiers).  This is used to
     * determine if a cast is allowed.  For example, if high is true
     * and {@code T <: Number}, then {@code List<T>} is rewritten to
     * {@code List<?  extends Number>}.  Since {@code List<Integer> <:
     * List<? extends Number>} a {@code List<T>} can be cast to {@code
     * List<Integer>} with a warning.
     * @param t a type
     * @param high if true return an upper bound; otherwise a lower
     * bound
     * @param rewriteTypeVars only rewrite captured wildcards if false;
     * otherwise rewrite all type variables
     * @return the type rewritten with wildcards (existential
     * quantifiers) only
     */
    private Type rewriteQuantifiers(Type t, boolean high, boolean rewriteTypeVars) {
        try {//我加上的
		DEBUG.P(this,"rewriteQuantifiers(3)");
		DEBUG.P("t="+t+" t.tag="+TypeTags.toString(t.tag));
		DEBUG.P("high="+high);
		DEBUG.P("rewriteTypeVars="+rewriteTypeVars);

		ListBuffer<Type> from = new ListBuffer<Type>();
        ListBuffer<Type> to = new ListBuffer<Type>();
        adaptSelf(t, from, to);
        DEBUG.P("from="+from.toList());
		DEBUG.P("to="+to.toList());
        ListBuffer<Type> rewritten = new ListBuffer<Type>();
        List<Type> formals = from.toList();
        boolean changed = false;
        for (Type arg : to.toList()) {
            Type bound;
            if (rewriteTypeVars && arg.tag == TYPEVAR) {
                TypeVar tv = (TypeVar)arg;
                bound = high ? tv.bound : syms.botType;
            } else {
                bound = high ? upperBound(arg) : lowerBound(arg);
            }
            Type newarg = bound;
            if (arg != bound) {
                changed = true;
                newarg = high ? makeExtendsWildcard(bound, (TypeVar)formals.head)
                              : makeSuperWildcard(bound, (TypeVar)formals.head);
            }
            rewritten.append(newarg);
            formals = formals.tail;
        }
        if (changed)
            return subst(t.tsym.type, from.toList(), rewritten.toList());
        else
            return t;

		}finally{//我加上的
		DEBUG.P(1,this,"rewriteQuantifiers(3)");
		}
    }