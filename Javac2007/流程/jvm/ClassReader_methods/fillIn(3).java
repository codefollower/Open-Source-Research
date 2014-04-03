    protected Location currentLoc; // FIXME

    private boolean verbosePath = true;

    /** Load directory of package into members scope.
     */
    private void fillIn(PackageSymbol p) throws IOException {
    	DEBUG.P(this,"fillIn(PackageSymbol p)");
    	DEBUG.P("Scope members_field="+p.members_field);
        if (p.members_field == null) p.members_field = new Scope(p);
        String packageName = p.fullname.toString();
        
        //这里的包名所代表的目录下面的文件可以是“.class”和“.java”
        Set<JavaFileObject.Kind> kinds = getPackageFileKinds();
        
        //PLATFORM_CLASS_PATH在javax.tools.StandardLocation中定义
        //DEBUG.P("fileManager.getClass().getName()="+fileManager.getClass().getName(),true);
        //输出如:com.sun.tools.javac.util.JavacFileManager
        
        //这里是在PLATFORM_CLASS_PATH上搜索packageName目录下的所有class文件
        fillIn(p, PLATFORM_CLASS_PATH,
               fileManager.list(PLATFORM_CLASS_PATH,
                                packageName,
                                EnumSet.of(JavaFileObject.Kind.CLASS),
                                false));
        
        DEBUG.P(2);
        DEBUG.P("***从PLATFORM_CLASS_PATH中Enter类文件结果如下***");
        DEBUG.P("-----------------------------------------------");
        DEBUG.P("包名: "+packageName);
        DEBUG.P("成员: "+p.members_field);
       	DEBUG.P(2);
 
        DEBUG.P("kinds="+kinds);                       
        Set<JavaFileObject.Kind> classKinds = EnumSet.copyOf(kinds);
        DEBUG.P("classKinds1="+classKinds); 
        classKinds.remove(JavaFileObject.Kind.SOURCE);
        DEBUG.P("classKinds2="+classKinds);
        boolean wantClassFiles = !classKinds.isEmpty();

        Set<JavaFileObject.Kind> sourceKinds = EnumSet.copyOf(kinds);
        sourceKinds.remove(JavaFileObject.Kind.CLASS);
        boolean wantSourceFiles = !sourceKinds.isEmpty();

        boolean haveSourcePath = fileManager.hasLocation(SOURCE_PATH);
        
        DEBUG.P("sourceKinds="+sourceKinds);
        DEBUG.P("wantClassFiles="+wantClassFiles);
        DEBUG.P("wantSourceFiles="+wantSourceFiles);
        DEBUG.P("haveSourcePath="+haveSourcePath);
        DEBUG.P("verbose="+verbose);
        DEBUG.P("verbosePath="+verbosePath);

        if (verbose && verbosePath) {
        	//javac加-verbose时输出[search path for source files:.....]
        	//[search path for class files:...........................]
            if (fileManager instanceof StandardJavaFileManager) {
                StandardJavaFileManager fm = (StandardJavaFileManager)fileManager;
                //加了-sourcepath选项时，打印-sourcepath所指示的路径
                //路径由com.sun.tools.javac.util.Paths.computeSourcePath()求出
                if (haveSourcePath && wantSourceFiles) {
                    List<File> path = List.nil();
                    for (File file : fm.getLocation(SOURCE_PATH)) {
                    	DEBUG.P("file="+file);
                        path = path.prepend(file);
                    }
                    printVerbose("sourcepath", path.reverse().toString());
                //没加-sourcepath选项时,默认打印类路径上的信息
                //路径由com.sun.tools.javac.util.Paths.computeUserClassPath()求出
                } else if (wantSourceFiles) {
                    List<File> path = List.nil();
                    for (File file : fm.getLocation(CLASS_PATH)) {
                        path = path.prepend(file);
                    }
                    printVerbose("sourcepath", path.reverse().toString());
                }
                if (wantClassFiles) {
                    List<File> path = List.nil();
                    //一般是jre\lib和jre\lib\ext目录下的.jar文件
                    //路径由com.sun.tools.javac.util.Paths.computeBootClassPath()求出
                    for (File file : fm.getLocation(PLATFORM_CLASS_PATH)) {
                        path = path.prepend(file);
                    }
                    
                    //路径由com.sun.tools.javac.util.Paths.computeUserClassPath()求出
                    for (File file : fm.getLocation(CLASS_PATH)) {
                        path = path.prepend(file);
                    }
                    //将上面两种类路径连在一起输出
                    printVerbose("classpath",  path.reverse().toString());
                }
            }
        }
        
        //当没指定-sourcepath时，默认在CLASS_PATH上搜索packageName目录下的所有class及java文件
        if (wantSourceFiles && !haveSourcePath) {
            fillIn(p, CLASS_PATH,
                   fileManager.list(CLASS_PATH,
                                    packageName,
                                    kinds,
                                    false));
        } else {
        	//在CLASS_PATH上搜索packageName目录下的所有class文件
            if (wantClassFiles)
                fillIn(p, CLASS_PATH,
                       fileManager.list(CLASS_PATH,
                                        packageName,
                                        classKinds,
                                        false));
            //在SOURCE_PATH上搜索packageName目录下的所有java文件
            if (wantSourceFiles)
                fillIn(p, SOURCE_PATH,
                       fileManager.list(SOURCE_PATH,
                                        packageName,
                                        sourceKinds,
                                        false));
        }
        verbosePath = false;
        
        //成员也有可能是未编译的.java文件
        DEBUG.P(2);
        DEBUG.P("***所有成员Enter结果如下***");
        DEBUG.P("-----------------------------------------------");
        DEBUG.P("包名: "+packageName);
        DEBUG.P("成员: "+p.members_field);
        DEBUG.P(2,this,"fillIn(PackageSymbol p)"); 
    }
    // where
        private void fillIn(PackageSymbol p,
                            Location location,
                            Iterable<JavaFileObject> files)
        {
            currentLoc = location;
            DEBUG.P(this,"fillIn(3)");
           
            for (JavaFileObject fo : files) {
            	DEBUG.P("fileKind="+fo.getKind()+" fileName="+fo);
                switch (fo.getKind()) {
                case CLASS:
                case SOURCE: {
                    // TODO pass binaryName to includeClassFile
                    String binaryName = fileManager.inferBinaryName(currentLoc, fo);
                    String simpleName = binaryName.substring(binaryName.lastIndexOf(".") + 1);
					DEBUG.P("fo="+fo);
                    DEBUG.P("binaryName="+binaryName);
					DEBUG.P("simpleName="+simpleName);
                    if (SourceVersion.isIdentifier(simpleName) ||
                        simpleName.equals("package-info"))
                        includeClassFile(p, fo);
                    break;
                }
                default:
                    extraFileActions(p, fo);//一个空方法
                }
                DEBUG.P(1);
            }
            DEBUG.P(2,this,"fillIn(3)");
        }