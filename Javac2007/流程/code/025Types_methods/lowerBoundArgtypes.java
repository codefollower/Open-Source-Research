//lowerBoundArgtypes
    // <editor-fold defaultstate="collapsed" desc="lowerBoundArgtypes">
    /**
     * Returns the lower bounds of the formals of a method.
     */
    public List<Type> lowerBoundArgtypes(Type t) {
        return map(t.getParameterTypes(), lowerBoundMapping);
    }
    private final Mapping lowerBoundMapping = new Mapping("lowerBound") {
            public Type apply(Type t) {
                return lowerBound(t);
            }
        };
    // </editor-fold>
//