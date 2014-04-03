	/** Process the option (with arg). Return true if error detected.
	 */
	public boolean process(Options options, String option, String arg) {
		//options相当于一个Map<K,V>，在以后的程序代码中经常用到，
		//如先按key取值，然后按取到的值是否为null给许多boolean变量赋值
            if (options != null)
                options.put(option, arg);
	    return false;
	}

	/** Process the option (without arg). Return true if error detected.
	 */
	public boolean process(Options options, String option) {
	    if (hasSuffix)
		return process(options, name.optionName, option.substring(name.optionName.length()));
	    else
		return process(options, option, option);
	}
        
        public OptionKind getKind() { return OptionKind.NORMAL; }
        
        public OptionName getName() { return name; }
    };