    private static <T> T nullCheck(T o) {
    	//如果o为null，将在运行时抛出java.lang.NullPointerException
        o.getClass(); // null check
        return o;
    }

    private static <T> Iterable<T> nullCheck(Iterable<T> it) {
        for (T t : it)
            t.getClass(); // null check
        return it;
    }