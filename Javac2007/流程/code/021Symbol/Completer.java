    /** Symbol completer interface.
     */
    public static interface Completer {
        void complete(Symbol sym) throws CompletionFailure;
    }