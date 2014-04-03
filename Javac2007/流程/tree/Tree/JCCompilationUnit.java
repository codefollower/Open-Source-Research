    /**
     * Everything in one source file is kept in a TopLevel structure.
     * @param pid              The tree representing the package clause.
     * @param sourcefile       The source file name.
     * @param defs             All definitions in this file (ClassDef, Import, and Skip)
     * @param packge           The package it belongs to.
     * @param namedImportScope A scope for all named imports.
     * @param starImportScope  A scope for all import-on-demands.
     * @param lineMap          Line starting positions, defined only
     *                         if option -g is set.
     * @param docComments      A hashtable that stores all documentation comments
     *                         indexed by the tree nodes they refer to.
     *                         defined only if option -s is set.
     * @param endPositions     A hashtable that stores ending positions of source
     *                         ranges indexed by the tree nodes they belong to.
     *                         Defined only if option -Xjcov is set.
     */
    public static class JCCompilationUnit extends JCTree implements CompilationUnitTree {
        public List<JCAnnotation> packageAnnotations;//包注释
        public JCExpression pid;//源文件所在包的全名
        public List<JCTree> defs;
        public JavaFileObject sourcefile; //在JavaCompiler.parse(2)设置
        
        //packge.members_field是一个Scope,这个Scope里的每一个Entry
        //代表了包名目录下的所有除成员类与本地类以外的类
        //每个Entry是在Enter阶段加入的
        public PackageSymbol packge;
        
        //在Env.topLevelEnv(JCCompilationUnit tree)中进行初始化
        public Scope namedImportScope;
        public Scope starImportScope;//含java.lang包中的所有类,接口
        
        public long flags;
       
        //在JavaCompiler.parse(2)设置
        public Position.LineMap lineMap = null;//com.sun.tools.javac.util.Position
        
        //在Parser.compilationUnit()设置
        public Map<JCTree, String> docComments = null;
        
        //在EndPosParser.compilationUnit()设置(加“-Xjcov”选项)
        public Map<JCTree, Integer> endPositions = null;
        
        protected JCCompilationUnit(List<JCAnnotation> packageAnnotations,
                        JCExpression pid,
                        List<JCTree> defs,
                        JavaFileObject sourcefile,
                        PackageSymbol packge,
                        Scope namedImportScope,
                        Scope starImportScope) {
            super(TOPLEVEL);
            this.packageAnnotations = packageAnnotations;
            this.pid = pid;
            this.defs = defs;
            this.sourcefile = sourcefile;
            this.packge = packge;
            this.namedImportScope = namedImportScope;
            this.starImportScope = starImportScope;
        }
        @Override
        public void accept(Visitor v) { v.visitTopLevel(this); }//是指JCTree.Visitor 
        
        //是指com.sun.source.tree.Tree.Kind
        //COMPILATION_UNIT(CompilationUnitTree.class)
        //JCCompilationUnit也实现了CompilationUnitTree接口
        public Kind getKind() { return Kind.COMPILATION_UNIT; }
        public List<JCAnnotation> getPackageAnnotations() {
            return packageAnnotations;
        }
        public List<JCImport> getImports() {
            ListBuffer<JCImport> imports = new ListBuffer<JCImport>();
            for (JCTree tree : defs) {
                if (tree.tag == IMPORT)
                    imports.append((JCImport)tree);
                else
                    break;//为什么退出呢?因为import语句是连着在一起出现的
            }
            return imports.toList();
        }
        public JCExpression getPackageName() { return pid; }
        public JavaFileObject getSourceFile() {
            return sourcefile;
        }
		public Position.LineMap getLineMap() {
	    	return lineMap;
        }  
        public List<JCTree> getTypeDecls() {//返回一棵没有IMPORT的JCTree
        	//List中的head是<JCTree>,tail是跟着head的子List<JCTree>
            List<JCTree> typeDefs;
            for (typeDefs = defs; !typeDefs.isEmpty(); typeDefs = typeDefs.tail)
                if (typeDefs.head.tag != IMPORT)
                    break;
            return typeDefs;
        }
        @Override
        public <R,D> R accept(TreeVisitor<R,D> v, D d) {
            return v.visitCompilationUnit(this, d);
        }
    }