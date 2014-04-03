    public class SymbolArchive extends ZipArchive {
        final File origFile;
        public SymbolArchive(File orig, ZipFile zdir) throws IOException {
            super(zdir);
            this.origFile = orig;
        }

        @Override
        void addZipEntry(ZipEntry entry) {
            // called from super constructor, may not refer to origFile.
            String name = entry.getName();
            if (!name.startsWith(symbolFilePrefix))
                return;
            name = name.substring(symbolFilePrefix.length());
            int i = name.lastIndexOf('/');
            String dirname = name.substring(0, i+1);
            String basename = name.substring(i+1);
            if (basename.length() == 0)
                return;
            List<String> list = map.get(dirname);
            if (list == null)
                list = List.nil();
            list = list.prepend(basename);
            map.put(dirname, list);
        }

        @Override
        public JavaFileObject getFileObject(String subdirectory, String file) {
            return super.getFileObject(symbolFilePrefix + subdirectory, file);
        }
    }