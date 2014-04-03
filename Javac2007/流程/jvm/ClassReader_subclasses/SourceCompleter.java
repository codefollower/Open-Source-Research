    public interface SourceCompleter {
        void complete(ClassSymbol sym)
            throws CompletionFailure;
    }