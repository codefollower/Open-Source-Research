    /**
     * A subclass of JavaFileObject for the sourcefile attribute found in a classfile.
     * The attribute is only the last component of the original filename, so is unlikely
     * to be valid as is, so operations other than those to access the name throw
     * UnsupportedOperationException
     */
    private static class SourceFileObject extends BaseFileObject {

        /** The file's name.
         */
        private Name name;

        public SourceFileObject(Name name) {
            this.name = name;
        }

        public InputStream openInputStream() {
            throw new UnsupportedOperationException();
        }

        public OutputStream openOutputStream() {
            throw new UnsupportedOperationException();
        }

        public Reader openReader() {
            throw new UnsupportedOperationException();
        }

        public Writer openWriter() {
            throw new UnsupportedOperationException();
        }

        /** @deprecated see bug 6410637 */
        @Deprecated
        public String getName() {
            return name.toString();
        }

        public long getLastModified() {
            throw new UnsupportedOperationException();
        }

        public boolean delete() {
            throw new UnsupportedOperationException();
        }

        public CharBuffer getCharContent(boolean ignoreEncodingErrors) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof SourceFileObject))
                return false;
            SourceFileObject o = (SourceFileObject) other;
            return name.equals(o.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        public boolean isNameCompatible(String simpleName, JavaFileObject.Kind kind) {
            return true; // fail-safe mode
        }

        public URI toUri() {
            return URI.create(name.toString());
        }

        public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
            throw new UnsupportedOperationException();
        }

    }