    /** This class represents an option recognized by the main program
     */
    static class Option implements JavacOption {

	/** Option string.
	 */
	OptionName name;

	/** Documentation key for arguments.
	 */
	String argsNameKey;

	/** Documentation key for description.
	 */
	String descrKey;

	/** Suffix option (-foo=bar or -foo:bar)
	 */
	boolean hasSuffix; //选项名称最后一个字符是'=' 或 ':'
	
	/*
	argsNameKey与descrKey的Documentation都放在下面的文件中:
	com\sun\tools\javac\resources\javac.properties(分国际化版本)
	
	如:-classpath <路径> 指定查找用户类文件和注释处理程序的位置
	OptionName name    对应CLASSPATH     (-classpath);
	String argsNameKey 对应opt.arg.path  (<路径>);
	String descrKey    对应opt.classpath (指定查找用户类文件和注释处理程序的位置); 
	
	
	在RecognizedOptions类的getAll()方法里按照各类
	参数生成了所有Option(包括它的子类:XOption与HiddenOption)
	*/
	Option(OptionName name, String argsNameKey, String descrKey) {
	    this.name = name;
	    this.argsNameKey = argsNameKey;
	    this.descrKey = descrKey;
	    char lastChar = name.optionName.charAt(name.optionName.length()-1);
	    hasSuffix = lastChar == ':' || lastChar == '=';
	}
	Option(OptionName name, String descrKey) {
	    this(name, null, descrKey);
	}