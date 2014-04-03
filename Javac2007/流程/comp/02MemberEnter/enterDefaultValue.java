    /** Enter a default value for an attribute method. */
    private void enterDefaultValue(final JCExpression defaultValue,
                                   final Env<AttrContext> localEnv,
                                   final MethodSymbol m) {
        m.defaultValue = annotate.enterAttributeValue(m.type.getReturnType(),
                                                      defaultValue,
                                                      localEnv);
    }