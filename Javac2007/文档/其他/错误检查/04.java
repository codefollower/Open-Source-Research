    boolean containsType(List<Type> ts, List<Type> ss) {
        while (ts.nonEmpty() && ss.nonEmpty()
               && containsType(ts.head, ss.head)) {
            ts = ts.tail;
            ss = ss.tail;
        }
        return ts.isEmpty() && ss.isEmpty();
    }