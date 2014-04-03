    /* ************************************************************************
     * Internationalization
     *************************************************************************/

    /** Find a localized string in the resource bundle.
     *  @param key     The key for the localized string.
     */
    public static String getLocalizedString(String key, Object... args) { // FIXME sb private
        try {
            if (messages == null)
                messages = new Messages(javacBundleName);
            return messages.getLocalizedString("javac." + key, args);
        }
        catch (MissingResourceException e) {
            throw new Error("Fatal Error: Resource for javac is missing", e);
        }
    }

    public static void useRawMessages(boolean enable) {
        if (enable) {
            messages = new Messages(javacBundleName) {
                    public String getLocalizedString(String key, Object... args) {
                        return key;
                    }
                };
        } else {
            messages = new Messages(javacBundleName);
        }
    }
    
    //资源绑定名称的字符串通常精确到文件名，而且文件名之前
    //的限定名称(如下面的"com.sun.tools.javac.resources")还
    //必须紧跟在类路径的某一目录下
    private static final String javacBundleName =
        "com.sun.tools.javac.resources.javac";
        
    //类全限定名称:com.sun.tools.javac.util.Messages
    private static Messages messages;
}