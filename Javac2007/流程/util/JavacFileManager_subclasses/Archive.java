    /**
     * An archive provides a flat directory structure of a ZipFile by
     * mapping directory names to lists of files (basenames).
     */
    public interface Archive {
        void close() throws IOException;

        boolean contains(String name);

        JavaFileObject getFileObject(String subdirectory, String file);

        List<String> getFiles(String subdirectory);

        Set<String> getSubdirectories();
    }