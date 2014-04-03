    /** Similar to Types.isSameType but avoids completion */
    private boolean isSameBinaryType(MethodType mt1, MethodType mt2) {
        List<Type> types1 = types.erasure(mt1.getParameterTypes())
            .prepend(types.erasure(mt1.getReturnType()));
        List<Type> types2 = mt2.getParameterTypes().prepend(mt2.getReturnType());
        while (!types1.isEmpty() && !types2.isEmpty()) {
            if (types1.head.tsym != types2.head.tsym)
                return false;
            types1 = types1.tail;
            types2 = types2.tail;
        }
        return types1.isEmpty() && types2.isEmpty();
    }