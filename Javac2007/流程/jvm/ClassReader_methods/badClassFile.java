/************************************************************************
 * Error Diagnoses
 ***********************************************************************/

    public static class BadClassFile extends CompletionFailure {
        private static final long serialVersionUID = 0;

        /**
         * @param msg A localized message.
         */
        public BadClassFile(ClassSymbol c, Object cname, Object msg) {
            super(c, Log.getLocalizedString("bad.class.file.header",
                                            cname, msg));
        }
    }

    public BadClassFile badClassFile(String key, Object... args) {
        return new BadClassFile (
            currentOwner.enclClass(),
            currentClassFile,
            Log.getLocalizedString(key, args));
    }
