public class Flow extends TreeScanner {
  //Flow变量声明部份
    private static my.Debug DEBUG=new my.Debug(my.Debug.Flow);//我加上的
	
    protected static final Context.Key<Flow> flowKey =
	new Context.Key<Flow>();

    private final Name.Table names;
    private final Log log;
    private final Symtab syms;
    private final Types types;
    private final Check chk;
    private       TreeMaker make;
    private       Lint lint;

    public static Flow instance(Context context) {
		Flow instance = context.get(flowKey);
		if (instance == null)
			instance = new Flow(context);
		return instance;
    }

    protected Flow(Context context) {
        DEBUG.P(this,"Flow(1)");

		context.put(flowKey, this);

		names = Name.Table.instance(context);
		log = Log.instance(context);
		syms = Symtab.instance(context);
		types = Types.instance(context);
		chk = Check.instance(context);
		lint = Lint.instance(context);
        DEBUG.P(0,this,"Flow(1)");
    }

    /** A flag that indicates whether the last statement could
     *	complete normally.
     */
    private boolean alive;

    /** The set of definitely assigned variables.
     */
    Bits inits;

    /** The set of definitely unassigned variables.
     */
    Bits uninits;

    /** The set of variables that are definitely unassigned everywhere
     *	in current try block. This variable is maintained lazily; it is
     *	updated only when something gets removed from uninits,
     *	typically by being assigned in reachable code.	To obtain the
     *	correct set of variables which are definitely unassigned
     *	anywhere in current try block, intersect uninitsTry and
     *	uninits.
     */
    Bits uninitsTry;

    /** When analyzing a condition, inits and uninits are null.
     *	Instead we have:
     */
    Bits initsWhenTrue;
    Bits initsWhenFalse;
    Bits uninitsWhenTrue;
    Bits uninitsWhenFalse;

    /** A mapping from addresses to variable symbols.
     */
    VarSymbol[] vars;

    /** The current class being defined.
     */
    JCClassDecl classDef;

    /** The first variable sequence number in this class definition.
     */
    int firstadr;

    /** The next available variable sequence number.
     */
    int nextadr;

    /** The list of possibly thrown declarable exceptions.
     */
    List<Type> thrown;

    /** The list of exceptions that are either caught or declared to be
     *	thrown.
     */
    List<Type> caught;

    /** Set when processing a loop body the second time for DU analysis. */
    boolean loopPassTwo = false;
  //