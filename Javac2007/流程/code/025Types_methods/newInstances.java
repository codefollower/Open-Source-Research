//newInstances
    // <editor-fold defaultstate="collapsed" desc="newInstances">
    /** Create new vector of type variables from list of variables
     *  changing all recursive bounds from old to new list.
     */
    public List<Type> newInstances(List<Type> tvars) {
		DEBUG.P(this,"newInstances(1)");
		DEBUG.P("tvars="+tvars);

        List<Type> tvars1 = Type.map(tvars, newInstanceFun);
		DEBUG.P("tvars1="+tvars1);
        for (List<Type> l = tvars1; l.nonEmpty(); l = l.tail) {
            TypeVar tv = (TypeVar) l.head;
            tv.bound = subst(tv.bound, tvars, tvars1);
        }

		DEBUG.P("tvars1="+tvars1);
		DEBUG.P(0,this,"newInstances(1)");
        return tvars1;
    }
    static private Mapping newInstanceFun = new Mapping("newInstanceFun") {
            public Type apply(Type t) { return new TypeVar(t.tsym, t.getUpperBound()); }
        };
    // </editor-fold>
//