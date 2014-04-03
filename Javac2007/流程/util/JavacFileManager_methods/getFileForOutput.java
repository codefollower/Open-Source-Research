    public FileObject getFileForOutput(Location location,
                                       String packageName,
                                       String relativeName,
                                       FileObject sibling)
        throws IOException
    {
        nullCheck(location);
        // validatePackageName(packageName);
        nullCheck(packageName);
        if (!isRelativeUri(URI.create(relativeName))) // FIXME 6419701
            throw new IllegalArgumentException("relativeName is invalid");
        String name = packageName.length() == 0
            ? relativeName
            : new File(externalizeFileName(packageName), relativeName).getPath();
        return getFileForOutput(location, name, sibling);
    }

    private JavaFileObject getFileForOutput(Location location,
                                            String fileName,
                                            FileObject sibling)
        throws IOException
    {
        File dir;
        if (location == CLASS_OUTPUT) {
            if (getClassOutDir() != null) {
                dir = getClassOutDir();
            } else {
                File siblingDir = null;
                if (sibling != null && sibling instanceof RegularFileObject) {
                    siblingDir = ((RegularFileObject)sibling).f.getParentFile();
                }
                return new RegularFileObject(new File(siblingDir, baseName(fileName)));
            }
        } else if (location == SOURCE_OUTPUT) {
            dir = (getSourceOutDir() != null ? getSourceOutDir() : getClassOutDir());
        } else {
            Iterable<? extends File> path = paths.getPathForLocation(location);
            dir = null;
            for (File f: path) {
                dir = f;
                break;
            }
        }

        File file = (dir == null ? new File(fileName) : new File(dir, fileName));
        return new RegularFileObject(file);
