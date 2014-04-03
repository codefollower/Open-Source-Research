public interface JavacOption {
	
	OptionKind getKind();

    /** Does this option take a (separate) operand? */
    boolean hasArg();

    /** Does argument string match option pattern?
     *  @param arg        The command line argument string.
     */
    boolean matches(String arg);

    /** Process the option (with arg). Return true if error detected.
     */
    boolean process(Options options, String option, String arg);

    /** Process the option (without arg). Return true if error detected.
     */
    boolean process(Options options, String option);
    
    OptionName getName();

    enum OptionKind {
        NORMAL,  //标准选项
        EXTENDED,//非标准选项(也称扩展选项,用标准选项“-X”来查看所有扩展选项)
        HIDDEN,  //隐藏选项(内部使用，不会显示)
    }