    /** Queue processing of an attribute default value. */
    void annotateDefaultValueLater(final JCExpression defaultValue,
                                   final Env<AttrContext> localEnv,
                                   final MethodSymbol m) {
		DEBUG.P(this,"annotateDefaultValueLater(3)");   
        DEBUG.P("defaultValue="+defaultValue);
        DEBUG.P("localEnv="+localEnv);
        DEBUG.P("m="+m);
        annotate.later(new Annotate.Annotator() {
                public String toString() {
                    return "annotate " + m.owner + "." +
                        m + " default " + defaultValue;
                }
                public void enterAnnotation() {
                    JavaFileObject prev = log.useSource(localEnv.toplevel.sourcefile);
                    try {
                        enterDefaultValue(defaultValue, localEnv, m);
                    } finally {
                        log.useSource(prev);
                    }
                }
            });
		DEBUG.P(0,this,"annotateDefaultValueLater(3)");   
    }