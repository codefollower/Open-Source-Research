    private boolean sideCast(Type from, Type to, Warner warn) {
        // We are casting from type $from$ to type $to$, which are
        // non-final unrelated types.  This method
        // tries to reject a cast by transferring type parameters
        // from $to$ to $from$ by common superinterfaces.
        boolean reverse = false;
        Type target = to;
        if ((to.tsym.flags() & INTERFACE) == 0) {
            assert (from.tsym.flags() & INTERFACE) != 0;
            reverse = true;
            to = from;
            from = target;
        }
        List<Type> commonSupers = superClosure(to, erasure(from));
        boolean giveWarning = commonSupers.isEmpty();
        // The arguments to the supers could be unified here to
        // get a more accurate analysis
        while (commonSupers.nonEmpty()) {
            Type t1 = asSuper(from, commonSupers.head.tsym);
            Type t2 = commonSupers.head; // same as asSuper(to, commonSupers.head.tsym);
            if (disjointTypes(t1.getTypeArguments(), t2.getTypeArguments()))
                return false;
            giveWarning = giveWarning || (reverse ? giveWarning(t2, t1) : giveWarning(t1, t2));
            commonSupers = commonSupers.tail;
        }
        if (giveWarning && !isReifiable(to))
            warn.warnUnchecked();
        if (!source.allowCovariantReturns())
            // reject if there is a common method signature with
            // incompatible return types.
            chk.checkCompatibleAbstracts(warn.pos(), from, to);
        return true;
    }