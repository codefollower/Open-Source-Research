    /**
     * A subclass of JavaFileObject representing regular files.
     */
    private class RegularFileObject extends BaseFileObject {
        /** Have the parent directories been created?
         */
        private boolean hasParents=false;

        /** The file's name.
         */
        private String name;

        /** The underlying file.
         */
        final File f;

        public RegularFileObject(File f) {
            this(f.getName(), f);
        }

        public RegularFileObject(String name, File f) {
            DEBUG.P(this,"RegularFileObject(2)");
            DEBUG.P("name="+name);
            DEBUG.P("f="+f);
            DEBUG.P("f.isDirectory()="+f.isDirectory());
            
            if (f.isDirectory())
                throw new IllegalArgumentException("directories not supported");
            this.name = name;
            this.f = f;
            
            DEBUG.P(0,this,"RegularFileObject(2)");
        }

        public InputStream openInputStream() throws IOException {
            return new FileInputStream(f);
        }

        protected CharsetDecoder getDecoder(boolean ignoreEncodingErrors) {
            return JavacFileManager.this.getDecoder(getEncodingName(), ignoreEncodingErrors);
        }

        public OutputStream openOutputStream() throws IOException {
            ensureParentDirectoriesExist();
            return new FileOutputStream(f);
        }

        public Writer openWriter() throws IOException {
            ensureParentDirectoriesExist();
            return new OutputStreamWriter(new FileOutputStream(f), getEncodingName());
        }

        private void ensureParentDirectoriesExist() throws IOException {
            if (!hasParents) {
                File parent = f.getParentFile();
                if (parent != null && !parent.exists()) {
                    if (!parent.mkdirs()) {
                        // if the mkdirs failed, it may be because another process concurrently
                        // created the directory, so check if the directory got created
                        // anyway before throwing an exception
                        if (!parent.exists() || !parent.isDirectory())
                            throw new IOException("could not create parent directories");
                    }
                }
                hasParents = true;
            }
        }

        /** @deprecated see bug 6410637 */
        @Deprecated
        public String getName() {
            return name;
        }

        public boolean isNameCompatible(String cn, JavaFileObject.Kind kind) {
            try {//我加上的
            DEBUG.P(this,"isNameCompatible(2)");
        	
            cn.getClass(); // null check
            //getKind()是在超类BaseFileObject中定义
            if (kind == Kind.OTHER && getKind() != kind)
                return false;
            String n = cn + kind.extension;
            
            DEBUG.P("name="+name+" n="+n);
            DEBUG.P("f.getCanonicalFile()="+f.getCanonicalFile());
            DEBUG.P("f.getCanonicalFile().getName()="+f.getCanonicalFile().getName());
            
            if (name.equals(n))
                return true;
            if (name.equalsIgnoreCase(n)) {
				/*
				test\enter\Package-Info.java:20: 软件包注释应在文件 package-info.java 中
				@PackageAnnotation
				^
				1 错误
				*/
                try {
                    // allow for Windows
                    return (f.getCanonicalFile().getName().equals(n));
                } catch (IOException e) {
                }
            }
            return false;
            
            //f.getCanonicalFile()会抛出IOException
            //DEBUG.P("f.getCanonicalFile()="+f.getCanonicalFile());
            }catch (IOException e) {//我加上的
                return false;
            }finally{//我加上的
            DEBUG.P(1,this,"isNameCompatible(2)");
            }
        }

        /** @deprecated see bug 6410637 */
        @Deprecated
        public String getPath() {
            return f.getPath();
        }

        public long getLastModified() {
            return f.lastModified();
        }

        public boolean delete() {
            return f.delete();
        }

        public CharBuffer getCharContent(boolean ignoreEncodingErrors) throws IOException {
            try {//我加上的
            DEBUG.P(this,"getCharContent(1)");
            DEBUG.P("ignoreEncodingErrors="+ignoreEncodingErrors);
			
            SoftReference<CharBuffer> r = contentCache.get(this);
            CharBuffer cb = (r == null ? null : r.get());
            if (cb == null) {
                InputStream in = new FileInputStream(f);
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
            
            }finally{//我加上的
            DEBUG.P(0,this,"getCharContent(1)");
            }
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof RegularFileObject))
                return false;
            RegularFileObject o = (RegularFileObject) other;
            try {
                return f.equals(o.f)
                    || f.getCanonicalFile().equals(o.f.getCanonicalFile());
            } catch (IOException e) {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return f.hashCode();
        }

        public URI toUri() {
            try {
                // Do no use File.toURI to avoid file system access
                String path = f.getAbsolutePath().replace(File.separatorChar, '/');
                return new URI("file://" + path).normalize();
            } catch (URISyntaxException ex) {
                return f.toURI();
            }
        }

    }