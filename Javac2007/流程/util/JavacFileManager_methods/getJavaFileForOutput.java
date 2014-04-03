    public JavaFileObject getJavaFileForOutput(Location location,
                                               String className,
                                               JavaFileObject.Kind kind,
                                               FileObject sibling)
        throws IOException
    {
        nullCheck(location);
        // validateClassName(className);
        nullCheck(className);
        nullCheck(kind);
        if (!sourceOrClass.contains(kind))
            throw new IllegalArgumentException("Invalid kind " + kind);
        return getFileForOutput(location, externalizeFileName(className, kind), sibling);
    }