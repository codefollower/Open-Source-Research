    /**
     * A subclass of JavaFileObject representing zip entries.
     */
    public class ZipFileObject extends BaseFileObject {

        /** The entry's name.
         */
        private String name;

        /** The zipfile containing the entry.
         */
        ZipFile zdir;

        /** The underlying zip entry object.
         */
        ZipEntry entry;

        public ZipFileObject(String name, ZipFile zdir, ZipEntry entry) {
            this.name = name;
            this.zdir = zdir;
            this.entry = entry;
        }

        public InputStream openInputStream() throws IOException {
            return zdir.getInputStream(entry);
        }

        public OutputStream openOutputStream() throws IOException {
            throw new UnsupportedOperationException();
        }

        protected CharsetDecoder getDecoder(boolean ignoreEncodingErrors) {
            return JavacFileManager.this.getDecoder(getEncodingName(), ignoreEncodingErrors);
        }

        public Writer openWriter() throws IOException {
            throw new UnsupportedOperationException();
        }

        /** @deprecated see bug 6410637 */
        @Deprecated
        public String getName() {
            return name;
        }

        public boolean isNameCompatible(String cn, JavaFileObject.Kind k) {
            cn.getClass(); // null check
            if (k == Kind.OTHER && getKind() != k)
                return false;
            return name.equals(cn + k.extension);
        }

        /** @deprecated see bug 6410637 */
        @Deprecated
        public String getPath() {
            return zdir.getName() + "(" + entry + ")";
        }

        public long getLastModified() {
            return entry.getTime();
        }

        public boolean delete() {
            throw new UnsupportedOperationException();
        }

        public CharBuffer getCharContent(boolean ignoreEncodingErrors) throws IOException {
            SoftReference<CharBuffer> r = contentCache.get(this);
            CharBuffer cb = (r == null ? null : r.get());
            if (cb == null) {
                InputStream in = zdir.getInputStream(entry);
                try {
                    ByteBuffer bb = makeByteBuffer(in);
                    JavaFileObject prev = log.useSource(this);
                    try {
                        cb = decode(bb, ignoreEncodingErrors);
                    } finally {
                        log.useSource(prev);
                    }
                    byteBufferCache.put(bb); // save for next time
                    if (!ignoreEncodingErrors)
                        contentCache.put(this, new SoftReference<CharBuffer>(cb));
                } finally {
                    in.close();
                }
            }
            return cb;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof ZipFileObject))
                return false;
            ZipFileObject o = (ZipFileObject) other;
            return zdir.equals(o.zdir) || name.equals(o.name);
        }

        @Override
        public int hashCode() {
            return zdir.hashCode() + name.hashCode();
        }

        public String getZipName() {
            return zdir.getName();
        }

        public String getZipEntryName() {
            return entry.getName();
        }

        public URI toUri() {
            String zipName = new File(getZipName()).toURI().normalize().getPath();
            String entryName = getZipEntryName();
            return URI.create("jar:" + zipName + "!" + entryName);
        }

    }
