    /** Try to instantiate expression type `that' to given type `to'.
     *  If a maximal instantiation exists which makes this type
     *  a subtype of type `to', return the instantiated type.
     *  If no instantiation exists, or if several incomparable
     *  best instantiations exist throw a NoInstanceException.
     */
    public Type instantiateExpr(ForAll that,
                                Type to,
                                Warner warn) throws NoInstanceException {
        List<Type> undetvars = Type.map(that.tvars, fromTypeVarFun);
        for (List<Type> l = undetvars; l.nonEmpty(); l = l.tail) {
            UndetVar v = (UndetVar) l.head;
            ListBuffer<Type> hibounds = new ListBuffer<Type>();
            for (List<Type> l1 = types.getBounds((TypeVar) v.qtype); l1.nonEmpty(); l1 = l1.tail) {
                if (!l1.head.containsSome(that.tvars)) {
                    hibounds.append(l1.head);
                }
            }
            v.hibounds = hibounds.toList();
        }
        Type qtype1 = types.subst(that.qtype, that.tvars, undetvars);
        if (!types.isSubtype(qtype1, to)) {
            throw unambiguousNoInstanceException
                .setMessage("no.conforming.instance.exists",
                            that.tvars, that.qtype, to);
        }
        for (List<Type> l = undetvars; l.nonEmpty(); l = l.tail)
            maximizeInst((UndetVar) l.head, warn);
        // System.out.println(" = " + qtype1.map(getInstFun));//DEBUG

        // check bounds
        List<Type> targs = Type.map(undetvars, getInstFun);
        targs = types.subst(targs, that.tvars, targs);
        checkWithinBounds(that.tvars, targs, warn);

        return getInstFun.apply(qtype1);
    }