    /** Output for "-verbose" option.
     *  @param key The key to look up the correct internationalized string.
     *  @param arg An argument for substitution into the output string.
     */
    private void printVerbose(String key, CharSequence arg) {
        Log.printLines(log.noticeWriter, Log.getLocalizedString("verbose." + key, arg));
    }

    /** Output for "-checkclassfile" option.
     *  @param key The key to look up the correct internationalized string.
     *  @param arg An argument for substitution into the output string.
     */
    private void printCCF(String key, Object arg) {
        Log.printLines(log.noticeWriter, Log.getLocalizedString(key, arg));
    }