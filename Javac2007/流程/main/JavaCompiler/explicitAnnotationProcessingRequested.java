    boolean explicitAnnotationProcessingRequested() {
        Options options = Options.instance(context);
        return
            explicitAnnotationProcessingRequested ||
            options.get("-processor") != null ||
            options.get("-processorpath") != null ||
            options.get("-proc:only") != null ||
            options.get("-Xprint") != null;
    }