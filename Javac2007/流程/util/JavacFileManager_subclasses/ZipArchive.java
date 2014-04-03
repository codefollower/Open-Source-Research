	public class ZipArchive implements Archive {
        protected final Map<String,List<String>> map;
        protected final ZipFile zdir;
        public ZipArchive(ZipFile zdir) throws IOException {
        	DEBUG.P(this,"ZipArchive(1)");
            this.zdir = zdir;
            this.map = new HashMap<String,List<String>>();
            for (Enumeration<? extends ZipEntry> e = zdir.entries(); e.hasMoreElements(); ) {
                ZipEntry entry;
                try {
                    entry = e.nextElement();
                } catch (InternalError ex) {
                    IOException io = new IOException();
                    io.initCause(ex); // convenience constructors added in Mustang :-(
                    throw io;
                }
                addZipEntry(entry);
            }
            //DEBUG.P("map="+map);
            DEBUG.P("map.size()="+map.size());
            DEBUG.P(0,this,"ZipArchive(1)");
        }
        
        //只提取文件条目(entry)
        void addZipEntry(ZipEntry entry) {
        	//DEBUG.P(this,"addZipEntry(1)");
            String name = entry.getName();
            //DEBUG.P("name="+name);
            int i = name.lastIndexOf('/');//DEBUG.P("name.lastIndexOf('/')="+i);
            String dirname = name.substring(0, i+1);
            //DEBUG.P("dirname="+dirname);
            String basename = name.substring(i+1);
            //DEBUG.P("basename="+basename);
            if (basename.length() == 0)
                return;
            List<String> list = map.get(dirname);
            if (list == null)
                list = List.nil();
            list = list.prepend(basename);
            map.put(dirname, list);
            //DEBUG.P(0,this,"addZipEntry(1)");
        }

        public boolean contains(String name) {
            int i = name.lastIndexOf('/');
            String dirname = name.substring(0, i+1);
            String basename = name.substring(i+1);
            if (basename.length() == 0)
                return false;
            List<String> list = map.get(dirname);
            return (list != null && list.contains(basename));
        }

        public List<String> getFiles(String subdirectory) {
            return map.get(subdirectory);
        }

        public JavaFileObject getFileObject(String subdirectory, String file) {
            ZipEntry ze = zdir.getEntry(subdirectory + file);
            return new ZipFileObject(file, zdir, ze);
        }

        public Set<String> getSubdirectories() {
            return map.keySet();
        }

        public void close() throws IOException {
            zdir.close();
        }
    }