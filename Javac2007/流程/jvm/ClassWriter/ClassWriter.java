    private final Options options;

    /** Switch: verbose output.
     */
    private boolean verbose;

    /** Switch: scrable private names.
     */
    private boolean scramble;

    /** Switch: scrable private names.
     */
    private boolean scrambleAll;

    /** Switch: retrofit mode.
     */
    private boolean retrofit;

    /** Switch: emit source file attribute.
     */
    private boolean emitSourceFile;

    /** Switch: generate CharacterRangeTable attribute.
     */
    private boolean genCrt;

    /** Switch: describe the generated stackmap
     */
    boolean debugstackmap;

    /**
     * Target class version.
     */
    private Target target;

    /**
     * Source language version.
     */
    private Source source;

    /** Type utilities. */
    private Types types;

    /** The initial sizes of the data and constant pool buffers.
     *  sizes are increased when buffers get full.
     */
    static final int DATA_BUF_SIZE = 0x0fff0;
    static final int POOL_BUF_SIZE = 0x1fff0;

    /** An output buffer for member info.
     */
    ByteBuffer databuf = new ByteBuffer(DATA_BUF_SIZE);

    /** An output buffer for the constant pool.
     */
    ByteBuffer poolbuf = new ByteBuffer(POOL_BUF_SIZE);

    /** An output buffer for type signatures.
     */
    ByteBuffer sigbuf = new ByteBuffer();

    /** The constant pool.
     */
    Pool pool;

    /** The inner classes to be written, as a set.
     */
    Set<ClassSymbol> innerClasses;

    /** The inner classes to be written, as a queue where
     *  enclosing classes come first.
     */
    ListBuffer<ClassSymbol> innerClassesQueue;

    /** The log to use for verbose output.
     */
    private final Log log;

    /** The name table. */
    private final Name.Table names;

    /** Access to files. */
    private final JavaFileManager fileManager;
    
    /** The tags and constants used in compressed stackmap. */
    static final int SAME_FRAME_SIZE = 64;
    static final int SAME_LOCALS_1_STACK_ITEM_EXTENDED = 247;
    static final int SAME_FRAME_EXTENDED = 251;
    static final int FULL_FRAME = 255;
    static final int MAX_LOCAL_LENGTH_DIFF = 4;