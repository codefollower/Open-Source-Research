    protected JavaFileObject.Kind getKind(String extension) {
        if (extension.equals(JavaFileObject.Kind.CLASS.extension))
            return JavaFileObject.Kind.CLASS;
        else if (extension.equals(JavaFileObject.Kind.SOURCE.extension))
            return JavaFileObject.Kind.SOURCE;
        else if (extension.equals(JavaFileObject.Kind.HTML.extension))
            return JavaFileObject.Kind.HTML;
        else
            return JavaFileObject.Kind.OTHER;
    }