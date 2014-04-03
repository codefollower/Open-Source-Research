    /**
     * specifies types of files to be read when filling in a package symbol
     */
    protected EnumSet<JavaFileObject.Kind> getPackageFileKinds() {
        return EnumSet.of(JavaFileObject.Kind.CLASS, JavaFileObject.Kind.SOURCE);
    }

    /**
     * this is used to support javadoc
     */
    protected void extraFileActions(PackageSymbol pack, JavaFileObject fe) {
    }