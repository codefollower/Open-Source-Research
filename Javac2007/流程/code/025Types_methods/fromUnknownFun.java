//fromUnknownFun
    // <editor-fold defaultstate="collapsed" desc="fromUnknownFun">
    /**
     * A mapping that turns all unknown types in this type to fresh
     * unknown variables.
     */
    public Mapping fromUnknownFun = new Mapping("fromUnknownFun") {
            public Type apply(Type t) {
                if (t.tag == UNKNOWN) return new UndetVar(t);
                else return t.map(this);
            }
        };
    // </editor-fold>
//