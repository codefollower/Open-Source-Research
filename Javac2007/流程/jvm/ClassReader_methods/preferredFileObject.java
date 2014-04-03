    /** Implement policy to choose to derive information from a source
     *  file or a class file when both are present.  May be overridden
     *  by subclasses.
     */
    protected JavaFileObject preferredFileObject(JavaFileObject a,
                                           JavaFileObject b) {
        
        if (preferSource)
            return (a.getKind() == JavaFileObject.Kind.SOURCE) ? a : b;
        else {
            long adate = a.getLastModified();
            long bdate = b.getLastModified();
            // 6449326: policy for bad lastModifiedTime in ClassReader
            //assert adate >= 0 && bdate >= 0;
            return (adate > bdate) ? a : b;
        }
    }