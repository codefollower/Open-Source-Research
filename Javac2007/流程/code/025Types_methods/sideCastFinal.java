    private boolean sideCastFinal(Type from, Type to, Warner warn) {
        // We are casting from type $from$ to type $to$, which are
        // unrelated types one of which is final and the other of
        // which is an interface.  This method
        // tries to reject a cast by transferring type parameters
        // from the final class to the interface.
        boolean reverse = false;
        Type target = to;
        if ((to.tsym.flags() & INTERFACE) == 0) {
            assert (from.tsym.flags() & INTERFACE) != 0;
            reverse = true;
            to = from;
            from = target;
        }
        assert (from.tsym.flags() & FINAL) != 0;
        Type t1 = asSuper(from, to.tsym);
        if (t1 == null) return false;
        Type t2 = to;
        if (disjointTypes(t1.getTypeArguments(), t2.getTypeArguments()))
            return false;
        if (!source.allowCovariantReturns())
            // reject if there is a common method signature with
            // incompatible return types.
            chk.checkCompatibleAbstracts(warn.pos(), from, to);
        if (!isReifiable(target) &&
            (reverse ? giveWarning(t2, t1) : giveWarning(t1, t2)))
            warn.warnUnchecked();
        return true;
    }