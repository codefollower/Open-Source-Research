    private class Path extends LinkedHashSet<File> {
		private static final long serialVersionUID = 0;

		private boolean expandJarClassPaths = false;
        private Set<File> canonicalValues = new HashSet<File>();

		public Path expandJarClassPaths(boolean x) {
			expandJarClassPaths = x;
			return this;
		}

		/** What to use when path element is the empty string */
		private String emptyPathDefault = null;

		public Path emptyPathDefault(String x) {
			emptyPathDefault = x;
			return this;
		}

		public Path() { super(); }

		public Path addDirectories(String dirs, boolean warn) {
			DEBUG.P(this,"addDirectories(2)");
			DEBUG.P("warn="+warn+" dirs="+dirs);
			
			if (dirs != null)
				for (String dir : new PathIterator(dirs))
					addDirectory(dir, warn);
			
			DEBUG.P(1,this,"addDirectories(2)");
			return this;
		}

		public Path addDirectories(String dirs) {
			return addDirectories(dirs, warn);
		}
	
		//从给定目录下查找文件时，只找扩展名为jar与zip的文件
		private void addDirectory(String dir, boolean warn) {
            try {//我加上的
            DEBUG.P(this,"addDirectory(2)");
            DEBUG.P("warn="+warn+" dir="+dir);
            DEBUG.P("isDirectory()="+new File(dir).isDirectory());
		
			if (! new File(dir).isDirectory()) {
				//如果是像System.getProperty("java.endorsed.dirs")这种编译器假设的目录
				//要是不存在的话，因为在调用addDirectory时把warn设成false了，所以不会警告。
				if (warn)
					log.warning("dir.path.element.not.found", dir);
				return;
			}

            File[] files = new File(dir).listFiles();//列出dir目录下的文件和目录(没有递归子目录)
            
            if (files == null) DEBUG.P("files=null");
            else {
                DEBUG.P("files.length="+files.length);
                //DEBUG.P("files="+files);
                for (File direntry : files) {
                    DEBUG.P("[isArchive="+isArchive(direntry)+"]direntry="+direntry);
                }
            }
            
            if (files == null)
                return;
            
			for (File direntry : files) {
                if (isArchive(direntry)) {
                    DEBUG.P("direntry="+direntry);
                    addFile(direntry, warn);
                }
			}
	    
			} finally {
            DEBUG.P(0,this,"addDirectory(2)");
			}
		}

		public Path addFiles(String files, boolean warn) {
            DEBUG.P(this,"addFiles(2)");
            DEBUG.P("warn="+warn+" files="+files);
            
			if (files != null)
			for (String file : new PathIterator(files, emptyPathDefault)) {
				//DEBUG.P("fileName="+file);
				addFile(file, warn);
			}
                
            DEBUG.P(1,this,"addFiles(2)");
			return this;
		}

		public Path addFiles(String files) {
			return addFiles(files, warn);
		}
		
		public Path addFile(String file, boolean warn) {
			addFile(new File(file), warn);
			return this;
		}
        
        //参数file可以代表一个文件也可代表一个目录
		public void addFile(File file, boolean warn) {
            try {//我加上的
            DEBUG.P(this,"addFile(2)");
            DEBUG.P("warn="+warn+" file="+file);
		
		
            File canonFile;
            try {
                //规范化的文件(一般是包涵绝对路径的文件)
                canonFile = file.getCanonicalFile();
            } catch (IOException e) {
                canonFile = file;
            }
            DEBUG.P("canonFile="+canonFile);
        
        
            //contains(file)在哪??? 在LinkedHashSet<File>(Path继承了LinkedHashSet<File>)
			if (contains(file) || canonicalValues.contains(canonFile)) {
                /* Discard duplicates and avoid infinite recursion */

                DEBUG.P("文件已存在,返回");
                return;
			}
	    
			DEBUG.P("file.exists()="+file.exists());
			DEBUG.P("file.isFile()="+file.isFile());
			DEBUG.P("file.isArchive()="+isArchive(file));
			DEBUG.P("expandJarClassPaths="+expandJarClassPaths);
	    
            /*
            假设有：javac -Xlint:path -Xbootclasspath/p:srcs:JarTest:args.txt:classes
             * 其中srcs是一个不存在的目录，JarTest是由“JarTest.jar”删除扩展名“.jar”后得到的
             * 实际存在的jar文件，args.txt也是一个存在的文本文件，则对应如下警告:
			警告：[path] 错误的路径元素 "srcs"：无此文件或目录
            警告：[path] 以下归档文件存在意外的扩展名: JarTest
            警告：[path] 以下路径中存在意外的文件: args.txt
            */

            if (! file.exists()) {
                /* No such file or directory exists */
                if (warn)
                    log.warning("path.element.not.found", file);	
			} else if (file.isFile()) {
                /* File is an ordinary file. */ 
                if (!isArchive(file)) {
                    /* Not a recognized extension; open it to see if
                     it looks like a valid zip file. */
                    try {
                        ZipFile z = new ZipFile(file);
                        z.close();
                        if (warn)
                            log.warning("unexpected.archive.file", file);
                    } catch (IOException e) {
                        // FIXME: include e.getLocalizedMessage in warning
                        if (warn)
                            log.warning("invalid.archive.file", file);
                        return;
                    }
                }
			}
        
			/* Now what we have left is either a directory or a file name
			   confirming to archive naming convention */
			   
			//当文件或目录不存在时，作者还是同样把它加到HashSet<File>
			super.add(file);//从类 java.util.HashSet 继承的方法
				canonicalValues.add(canonFile);

				//是否展开压缩文件(如jar文件)
			if (expandJarClassPaths && file.exists() && file.isFile())
                addJarClassPath(file, warn);

            } finally {
                DEBUG.P(0,this,"addFile(2)");
            }
		}

		// Adds referenced classpath elements from a jar's Class-Path
		// Manifest entry.  In some future release, we may want to
		// update this code to recognize URLs rather than simple
		// filenames, but if we do, we should redo all path-related code.
		private void addJarClassPath(File jarFile, boolean warn) {
            try {
            DEBUG.P(this,"addJarClassPath(2)");
            DEBUG.P("warn="+warn+" jarFile="+jarFile);
            
			try {
				String jarParent = jarFile.getParent();
				
				DEBUG.P("jarParent="+jarParent);
				
				JarFile jar = new JarFile(jarFile);

				try {
					Manifest man = jar.getManifest();
							DEBUG.P("man="+man);
					if (man == null) return;

					Attributes attr = man.getMainAttributes();
							DEBUG.P("attr="+attr);
					if (attr == null) return;
					
					//是指：java.util.jar.Attributes.Name
					String path = attr.getValue(Attributes.Name.CLASS_PATH);
					DEBUG.P("Attributes.Name.CLASS_PATH="+path);
					//在System.getProperty("sun.boot.class.path")里包含的jar文件没有一个有CLASS_PATH
					if (path == null) return;

					for (StringTokenizer st = new StringTokenizer(path);
					 st.hasMoreTokens();) {
					String elt = st.nextToken();
					File f = (jarParent == null ? new File(elt) : new File(jarParent, elt));
					addFile(f, warn);
					}
				} finally {
					jar.close();
				}
			} catch (IOException e) {
				log.error("error.reading.file", jarFile, e.getLocalizedMessage());
			}
            
            
            } finally {
			DEBUG.P(0,this,"addJarClassPath(2)");
            }
		}
    }