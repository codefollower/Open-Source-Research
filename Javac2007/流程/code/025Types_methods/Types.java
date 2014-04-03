public class Types {
	private static my.Debug DEBUG=new my.Debug(my.Debug.Types);//我加上的
//Types
    protected static final Context.Key<Types> typesKey =
        new Context.Key<Types>();

    final Symtab syms;
    final Name.Table names;
    final boolean allowBoxing;
    final ClassReader reader;
    final Source source;
    final Check chk;
    List<Warner> warnStack = List.nil();
    final Name capturedName;

    // <editor-fold defaultstate="collapsed" desc="Instantiating">
    public static Types instance(Context context) {
        Types instance = context.get(typesKey);
        if (instance == null)
            instance = new Types(context);
        return instance;
    }

    protected Types(Context context) {
    	DEBUG.P(this,"Types(1)");
    	
        context.put(typesKey, this);
        syms = Symtab.instance(context);
        names = Name.Table.instance(context);
        allowBoxing = Source.instance(context).allowBoxing();
        reader = ClassReader.instance(context);
        source = Source.instance(context);
        chk = Check.instance(context);
        capturedName = names.fromString("<captured wildcard>");
        
        DEBUG.P(0,this,"Types(1)");
    }
    // </editor-fold>
//