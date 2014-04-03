    /** Attach the default value for an annotation element.
     */
    void attachAnnotationDefault(final Symbol sym) {
        final MethodSymbol meth = (MethodSymbol)sym; // only on methods
        final Attribute value = readAttributeValue();
        annotate.later(new AnnotationDefaultCompleter(meth, value));
    }