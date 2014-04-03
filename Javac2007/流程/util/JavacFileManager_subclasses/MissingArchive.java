    public class MissingArchive implements Archive {
        final File zipFileName;
        public MissingArchive(File name) {
            zipFileName = name;
        }
        public boolean contains(String name) {
              return false;
        }

        public void close() {
        }

        public JavaFileObject getFileObject(String subdirectory, String file) {
            return null;
        }

        public List<String> getFiles(String subdirectory) {
            return List.nil();
        }

        public Set<String> getSubdirectories() {
            return Collections.emptySet();
        }
    }
