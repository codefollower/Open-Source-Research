    /**
     * The number of dimensions of an array type.
     */
    public int dimensions(Type t) {
        int result = 0;
        while (t.tag == ARRAY) {
            result++;
            t = elemtype(t);
        }
        return result;
    }