public class Code {
	private static my.Debug DEBUG=new my.Debug(my.Debug.Code);//我加上的
	
    public final boolean debugCode;
    public final boolean needStackMap;
    
    public enum StackMapFormat {
		NONE,
		CLDC {
			Name getAttributeName(Name.Table names) {
				return names.StackMap;
			} 
		},
		JSR202 {
			Name getAttributeName(Name.Table names) {
				return names.StackMapTable;
			}
		};
		Name getAttributeName(Name.Table names) {
				return names.empty;
		}
    }

    final Types types;
    final Symtab syms;

/*---------- classfile fields: --------------- */

    /** The maximum stack size.
     */
    public int max_stack = 0;

    /** The maximum number of local variable slots.
     */
    //不同作用域的局部变量可以重用局部变量数组的索引号(nextreg)，
    //但max_locals的值是不会减少的，
    //它用于跟踪某一时刻局部变量数组中的总局部变量个数，
    //如果在新加入一个局部变量后，总个数比上次大，max_locals修改成
    //当前nextreg的值，否则维持原来的值不变。参考:newLocal方法
    public int max_locals = 0;

    /** The code buffer.
     */
    public byte[] code = new byte[64];//存放所要产生的方法的字节吗，数组长度会不断扩大

    /** the current code pointer.
     */
    public int cp = 0;

    /** Check the code against VM spec limits; if
     *  problems report them and return true.
     */
    public boolean checkLimits(DiagnosticPosition pos, Log log) {
		if (cp > ClassFile.MAX_CODE) {
			log.error(pos, "limit.code");
			return true;
		}
		if (max_locals > ClassFile.MAX_LOCALS) {
			log.error(pos, "limit.locals");
			return true;
		}
		if (max_stack > ClassFile.MAX_STACK) {
			log.error(pos, "limit.stack");
			return true;
		}
		return false;
    }

    /** A buffer for expression catch data. Each enter is a vector
     *  of four unsigned shorts.
     */
    ListBuffer<char[]> catchInfo = new ListBuffer<char[]>();

    /** A buffer for line number information. Each entry is a vector
     *  of two unsigned shorts.
     */
    List<char[]> lineInfo = List.nil(); // handled in stack fashion

    /** The CharacterRangeTable
     */
    public CRTable crt;

/*---------- internal fields: --------------- */

    /** Are we generating code with jumps >= 32K?
     */
    public boolean fatcode;

    /** Code generation enabled?
     */
    private boolean alive = true;

    /** The current machine state (registers and stack).
     */
    State state;

    /** Is it forbidden to compactify code, because something is
     *  pointing to current location?
     */
    private boolean fixedPc = false;

    /** The next available register.
     */
    public int nextreg = 0;

    /** A chain for jumps to be resolved before the next opcode is emitted.
     *  We do this lazily to avoid jumps to jumps.
     */
    Chain pendingJumps = null;

    /** The position of the currently statement, if we are at the
     *  start of this statement, NOPOS otherwise.
     *  We need this to emit line numbers lazily, which we need to do
     *  because of jump-to-jump optimization.
     */
    int pendingStatPos = Position.NOPOS;

    /** Set true when a stackMap is needed at the current PC. */
    boolean pendingStackMap = false;
    
    /** The stack map format to be generated. */
    StackMapFormat stackMap;
    
    /** Switch: emit variable debug info.
     */
    boolean varDebugInfo;

    /** Switch: emit line number info.
     */
    boolean lineDebugInfo;
    
    /** Emit line number info if map supplied
     */
    Position.LineMap lineMap;

    /** The constant pool of the current class.
     */
    final Pool pool;

    final MethodSymbol meth;

    /** Construct a code object, given the settings of the fatcode,
     *  debugging info switches and the CharacterRangeTable.
     */
    public Code(MethodSymbol meth,
		boolean fatcode,
		Position.LineMap lineMap,
		boolean varDebugInfo,
		StackMapFormat stackMap,
		boolean debugCode,
		CRTable crt,
		Symtab syms,
		Types types,
		Pool pool) {
		DEBUG.P(this,"Code(10)");

		this.meth = meth;
		this.fatcode = fatcode;
		this.lineMap = lineMap;
		this.lineDebugInfo = lineMap != null;
		this.varDebugInfo = varDebugInfo;
		this.crt = crt;
		this.syms = syms;
		this.types = types;
		this.debugCode = debugCode;
		this.stackMap = stackMap;
		switch (stackMap) {
		case CLDC:
		case JSR202:
			this.needStackMap = true;
			break;
		default:
			this.needStackMap = false;
		}
		state = new State();
		lvar = new LocalVar[20];
		this.pool = pool;
		
		DEBUG.P("meth="+meth);
		DEBUG.P("fatcode="+fatcode);
		DEBUG.P("lineDebugInfo="+lineDebugInfo);
		DEBUG.P("varDebugInfo="+varDebugInfo);
		DEBUG.P("stackMap="+stackMap);
		DEBUG.P("needStackMap="+needStackMap);
		DEBUG.P(0,this,"Code(10)");
    }