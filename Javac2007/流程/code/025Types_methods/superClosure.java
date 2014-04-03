    private List<Type> superClosure(Type t, Type s) {
        List<Type> cl = List.nil();
        for (List<Type> l = interfaces(t); l.nonEmpty(); l = l.tail) {
            if (isSubtype(s, erasure(l.head))) {
                cl = insert(cl, l.head);
            } else {
                cl = union(cl, superClosure(l.head, s));
            }
        }
        return cl;
    }