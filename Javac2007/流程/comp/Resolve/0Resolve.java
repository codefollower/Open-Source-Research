public class Resolve {
    private static my.Debug DEBUG=new my.Debug(my.Debug.Resolve);//我加上的
	
    protected static final Context.Key<Resolve> resolveKey =
        new Context.Key<Resolve>();

    Name.Table names;
    Log log;
    Symtab syms;
    Check chk;
    Infer infer;
    ClassReader reader;
    TreeInfo treeinfo;
    Types types;
    public final boolean boxingEnabled; // = source.allowBoxing();
    public final boolean varargsEnabled; // = source.allowVarargs();
    private final boolean debugResolve;

    public static Resolve instance(Context context) {
        Resolve instance = context.get(resolveKey);
        if (instance == null)
            instance = new Resolve(context);
        return instance;
    }

    protected Resolve(Context context) {
    	DEBUG.P(this,"Resolve(1)");
    	
        context.put(resolveKey, this);
        syms = Symtab.instance(context);

        varNotFound = new
            ResolveError(ABSENT_VAR, syms.errSymbol, "variable not found");
        wrongMethod = new
            ResolveError(WRONG_MTH, syms.errSymbol, "method not found");
        wrongMethods = new
            ResolveError(WRONG_MTHS, syms.errSymbol, "wrong methods");
        methodNotFound = new
            ResolveError(ABSENT_MTH, syms.errSymbol, "method not found");
        typeNotFound = new
            ResolveError(ABSENT_TYP, syms.errSymbol, "type not found");

        names = Name.Table.instance(context);
        log = Log.instance(context);
        chk = Check.instance(context);
        infer = Infer.instance(context);
        reader = ClassReader.instance(context);
        treeinfo = TreeInfo.instance(context);
        types = Types.instance(context);
        Source source = Source.instance(context);
        boxingEnabled = source.allowBoxing();
        varargsEnabled = source.allowVarargs();
        Options options = Options.instance(context);
        debugResolve = options.get("debugresolve") != null;
        
        DEBUG.P(0,this,"Resolve(1)");
    }

    /** error symbols, which are returned when resolution fails
     */
    final ResolveError varNotFound;
    final ResolveError wrongMethod;
    final ResolveError wrongMethods;
    final ResolveError methodNotFound;
    final ResolveError typeNotFound;
