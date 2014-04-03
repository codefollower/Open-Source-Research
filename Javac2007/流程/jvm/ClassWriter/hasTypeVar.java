    boolean hasTypeVar(List<Type> l) {
        while (l.nonEmpty()) {
            if (l.head.tag == TypeTags.TYPEVAR) return true;
            l = l.tail;
        }
        return false;
    }