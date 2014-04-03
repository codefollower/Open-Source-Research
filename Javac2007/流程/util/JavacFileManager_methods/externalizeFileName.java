    /** Return external representation of name,
     *  converting '.' to File.separatorChar.
     */
    private static String externalizeFileName(CharSequence name) {
        return name.toString().replace('.', File.separatorChar);
    }

    private static String externalizeFileName(CharSequence n, JavaFileObject.Kind kind) {
        return externalizeFileName(n) + kind.extension;
    }