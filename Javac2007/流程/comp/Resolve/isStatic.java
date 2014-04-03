    /** An environment is "static" if its static level is greater than
     *  the one of its outer environment
     */
    static boolean isStatic(Env<AttrContext> env) {
        return env.info.staticLevel > env.outer.info.staticLevel;
    }