    /** A directory of zip files already opened.
     */
    Map<File, Archive> archives = new HashMap<File,Archive>();

    private static final String[] symbolFileLocation = { "lib", "ct.sym" };
    private static final String symbolFilePrefix = "META-INF/sym/rt.jar/";

    /** Open a new zip file directory.
     */
    protected Archive openArchive(File zipFileName) throws IOException {
    	DEBUG.P(this,"openArchive(1)");
    	DEBUG.P("zipFileName="+zipFileName);
        Archive archive = archives.get(zipFileName);
        if (archive == null) {
        	DEBUG.P("archive=null");
        	DEBUG.P("ignoreSymbolFile="+ignoreSymbolFile);
        	DEBUG.P("bootClassPathRtJar="+paths.getBootClassPathRtJar());
            File origZipFileName = zipFileName;
            //ignoreSymbolFile在javac初始过程中已在setContext()设置
            if (!ignoreSymbolFile && paths.isBootClassPathRtJar(zipFileName)) {
                File file = zipFileName.getParentFile().getParentFile(); // ${java.home}
                
                DEBUG.P("zipFileName.getParentFile()1次="+zipFileName.getParentFile());
                DEBUG.P("zipFileName.getParentFile()2次="+zipFileName.getParentFile().getParentFile());
                DEBUG.P("file.getName()="+file.getName());
                DEBUG.P("new File(file.getName())="+new File(file.getName()));
                DEBUG.P("new File(\"jre\"))="+new File("jre"));
                DEBUG.P("if (new File(file.getName()).equals(new File(\"jre\")))="+new File(file.getName()).equals(new File("jre")));
                //在我的JDK1.6上有点不一样:
                //bootClassPathRtJar是D:\Java\jre1.6.0\lib\rt.jar
                //而ct.sym在D:\Java\jdk1.6.0\lib\ct.sym
                if (new File(file.getName()).equals(new File("jre")))
                    file = file.getParentFile();
                // file == ${jdk.home}
                for (String name : symbolFileLocation)
                    file = new File(file, name);
                // file == ${jdk.home}/lib/ct.sym
                DEBUG.P("file="+file);
                DEBUG.P("file.exists()="+file.exists());
                if (file.exists())
                    zipFileName = file;
            }
            DEBUG.P("zipFileName="+zipFileName);

            try {
                ZipFile zdir = new ZipFile(zipFileName);
                if (origZipFileName == zipFileName)
                    archive = new ZipArchive(zdir);
                else
                    archive = new SymbolArchive(origZipFileName, zdir);
            } catch (FileNotFoundException ex) {
                archive = new MissingArchive(zipFileName);
            } catch (IOException ex) {
                log.error("error.reading.file", zipFileName, ex.getLocalizedMessage());
                archive = new MissingArchive(zipFileName);
            }

            archives.put(origZipFileName, archive);
        }
        DEBUG.P(0,this,"openArchive(1)");
        return archive;
    }