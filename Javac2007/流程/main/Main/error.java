    /** Report a usage error.
     */
    void error(String key, Object... args) {
        if (fatalErrors) {
            String msg = getLocalizedString(key, args);
            //类全限定名称:com.sun.tools.javac.util.PropagatedException
            throw new PropagatedException(new IllegalStateException(msg));
        }
        warning(key, args);
        Log.printLines(out, getLocalizedString("msg.usage", ownName));
    }

    /** Report a warning.
     */
    void warning(String key, Object... args) {
        Log.printLines(out, ownName + ": "
                       + getLocalizedString(key, args));
    }