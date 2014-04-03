public class Enter extends JCTree.Visitor {
	private static my.Debug DEBUG=new my.Debug(my.Debug.Enter);//我加上的
	
    protected static final Context.Key<Enter> enterKey =
	new Context.Key<Enter>();

    Log log;
    Symtab syms;
    Check chk;
    TreeMaker make;
    ClassReader reader;
    Annotate annotate;
    MemberEnter memberEnter;
    Lint lint;
    JavaFileManager fileManager;

    private final Todo todo;
    
    private final Name.Table names;//我加上的

    public static Enter instance(Context context) {
		Enter instance = context.get(enterKey);
		if (instance == null)
			instance = new Enter(context);
		return instance;
    }

    protected Enter(Context context) {
		DEBUG.P(this,"Enter(1)");
		context.put(enterKey, this);

		log = Log.instance(context);
		reader = ClassReader.instance(context);
		make = TreeMaker.instance(context);
		syms = Symtab.instance(context);
		chk = Check.instance(context);
		memberEnter = MemberEnter.instance(context);
		annotate = Annotate.instance(context);
		lint = Lint.instance(context);

		predefClassDef = make.ClassDef(
			make.Modifiers(PUBLIC),
			syms.predefClass.name, null, null, null, null);
		//predefClass是一个ClassSymbol(PUBLIC|ACYCLIC, names.empty, rootPackage)
		//且它的Scope members_field已有成员(几个基本类型符号(symbols for basic types)及其他操作符)
		//请参考Systab类的predefClass字段说明
		predefClassDef.sym = syms.predefClass;

		todo = Todo.instance(context);
		fileManager = context.get(JavaFileManager.class);
		
		names = Name.Table.instance(context);    //我加上的
		DEBUG.P(0,this,"Enter(1)");
    }

    /** A hashtable mapping classes and packages to the environments current
     *  at the points of their definitions.
     */
    Map<TypeSymbol,Env<AttrContext>> typeEnvs =
	    new HashMap<TypeSymbol,Env<AttrContext>>();

    /** Accessor for typeEnvs
     */
    public Env<AttrContext> getEnv(TypeSymbol sym) {
		return typeEnvs.get(sym);
    }
    
    public Env<AttrContext> getClassEnv(TypeSymbol sym) {
        Env<AttrContext> localEnv = getEnv(sym);
        Env<AttrContext> lintEnv = localEnv;
        //lint在AttrContext中定义
        while (lintEnv.info.lint == null)
            lintEnv = lintEnv.next;
        localEnv.info.lint = lintEnv.info.lint.augment(sym.attributes_field, sym.flags());
        return localEnv;
    }

    /** The queue of all classes that might still need to be completed;
     *	saved and initialized by main().
     */
    ListBuffer<ClassSymbol> uncompleted;//它的值在Enter相应的visitXXX()中设置

    /** A dummy class to serve as enclClass for toplevel environments.
     */
    private JCClassDecl predefClassDef;
