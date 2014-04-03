    /**
     * Insert all files in subdirectory `subdirectory' of `directory' which end
     * in one of the extensions in `extensions' into packageSym.
     */
    private void listDirectory(File directory,
                               String subdirectory,
                               Set<JavaFileObject.Kind> fileKinds,
                               boolean recurse,
                               ListBuffer<JavaFileObject> l) {
        
        DEBUG.P("directory.isFile()="+directory.isFile()+" directory="+directory);
        DEBUG.P("recurse="+recurse+" subdirectory="+subdirectory+" fileKinds="+fileKinds);
        //DEBUG.P("ListBuffer<JavaFileObject>.size()="+l.size());
        //DEBUG.P("archive="+archives.get(directory));
        	                     	
        Archive archive = archives.get(directory);
        
        //在压缩文件(如jar,zip)中查找是否有subdirectory目录,有则按文件类型集合fileKinds
        //将找到的文件构造成一个ZipFileObject存入ListBuffer<JavaFileObject>
        //当recurse=ture时，递归查找子目录
        if (archive != null || directory.isFile()) {
            if (archive == null) {
                try {
                    archive = openArchive(directory);
                } catch (IOException ex) {
                    log.error("error.reading.file",
                       directory, ex.getLocalizedMessage());
                    return;
                }
            }
            if (subdirectory.length() != 0) {
                subdirectory = subdirectory.replace('\\', '/');
                if (!subdirectory.endsWith("/")) subdirectory = subdirectory + "/";
            }
            //DEBUG.P("subdirectory="+subdirectory);
            //DEBUG.P("archiveClassName="+archive.getClass());
            
            List<String> files = archive.getFiles(subdirectory);
            if (files != null) {
                for (String file; !files.isEmpty(); files = files.tail) {
                    file = files.head;
                    if (isValidFile(file, fileKinds)) {
                    	//DEBUG.P("fname="+file);
                        l.append(archive.getFileObject(subdirectory, file));
                    }
                }
            }
            if (recurse) {
                for (String s: archive.getSubdirectories()) {
                    if (s.startsWith(subdirectory) && !s.equals(subdirectory)) {
                        // Because the archive map is a flat list of directories,
                        // the enclosing loop will pick up all child subdirectories.
                        // Therefore, there is no need to recurse deeper.
                        //意思就是说ZipArchive中的map已经列出所有目录(包含子目录)，
                        //只要逐个查找map中的key就相当于查找所有目录了
                        listDirectory(directory, s, fileKinds, false, l);
                    }
                }
            }
        } else {
        	//按文件类型集合fileKinds查找目录directory\subdirectory\下的所有文件
        	//将找到的文件构造成一个RegularFileObject存入ListBuffer<JavaFileObject>
        	//当recurse=ture时，递归查找子目录
            File d = subdirectory.length() != 0
                ? new File(directory, subdirectory)
                : directory;
            
            //DEBUG.P("File(directory, subdirectory).name="+d);  
            
            //if (!caseMapCheck(d, subdirectory))
            //    return;
			boolean caseMapCheckFlag=caseMapCheck(d, subdirectory);
			DEBUG.P("caseMapCheckFlag="+caseMapCheckFlag);
			if (!caseMapCheckFlag)
                return;

            File[] files = d.listFiles();
			if (files == null) DEBUG.P("files=null");
            else {
                DEBUG.P("files.length="+files.length);
                //DEBUG.P("files="+files);
                for (File direntry : files) {
					String fname = direntry.getName();
					DEBUG.P("direntry="+direntry);
                    DEBUG.P("fname="+fname);
                }
            }

            if (files == null)
                return;

            for (File f: files) {
                String fname = f.getName();
                if (f.isDirectory()) {
                    if (recurse && SourceVersion.isIdentifier(fname)) {
                        listDirectory(directory,
                                      subdirectory + File.separator + fname,
                                      fileKinds,
                                      recurse,
                                      l);
                    }
                } else {
                    if (isValidFile(fname, fileKinds)) {
                    	DEBUG.P("fname="+fname);
                        JavaFileObject fe =
                        new RegularFileObject(fname, new File(d, fname));
                        l.append(fe);
                    }
                }
            }
        }
    }

	//判断给定文件s的扩展名是否在给定的文件类型集合fileKinds里
    private boolean isValidFile(String s, Set<JavaFileObject.Kind> fileKinds) {
        int lastDot = s.lastIndexOf(".");
        String extn = (lastDot == -1 ? s : s.substring(lastDot));
        JavaFileObject.Kind kind = getKind(extn);
        return fileKinds.contains(kind);
    }