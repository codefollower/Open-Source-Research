AST是一种嵌套式的树，最外层是JCCompilationUnit,

JCCompilationUnit是JCTree的子类，
JCTree是抽像的，JCTree只有pos和type两个实例字段，
pos的值就是在源代码中的位置，在用TreeMaker构建每一个JCTree的子类时对pos赋值，
Parser阶段不会对type字段赋值，
不同JCTree子类的type字段值会在不同阶段完成。

JCCompilationUnit有如下这些字段;
		public List<JCAnnotation> packageAnnotations; //包注解，只能用于package-info.jar中
        public JCExpression pid; //包名

		//import语句,skip(也就是";"号),最顶层的类，同一个源文件中可以声明多个最顶层的类，
		//但是只有与源文件名相同的类才能是public的，其他的必需是包私有的(package-private)
        public List<JCTree> defs;
        public JavaFileObject sourcefile;　//源文件名
        public PackageSymbol packge;
        public ImportScope namedImportScope;
        public StarImportScope starImportScope;
        public long flags; //这个字段没有使用
        public Position.LineMap lineMap = null; //有"-g:lines"选项，或者不存在"-g:"选项(大多数是这种情况)启用
        public Map<JCTree, String> docComments = null; //-printsource 或 -stubs启用
        public Map<JCTree, Integer> endPositions = null; //-Xjcov或注册了javax.tools.DiagnosticListener时启用

Parser阶段要做的事就是把JCCompilationUnit中的下面这些字段填好:
packageAnnotations
pid
defs
lineMap
docComments   //前面这5个在com.sun.tools.javac.parser.JavacParser.parseCompilationUnit()中填
endPositions  //当有-Xjcov选项或注册了javax.tools.DiagnosticListener时，会用JavacParser的子类EndPosParser要解析，
              //此时在com.sun.tools.javac.parser.EndPosParser.parseCompilationUnit()中填endPositions

sourcefile    //在com.sun.tools.javac.main.JavaCompiler.parse(JavaFileObject filename, CharSequence content)中填


