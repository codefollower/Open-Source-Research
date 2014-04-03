    /** Process command line arguments: store all command line options
     *  in `options' table and return all source filenames.
     *  @param flags    The array of command line arguments.
     */
    public List<File> processArgs(String[] flags) { // XXX sb protected
    //String[] flags的值已由CommandLine.parse(args)处理过,args是命令行参数
    try {//我加上的
    	DEBUG.P(this,"processArgs(1)");
    	DEBUG.P("options前="+options);
		//DEBUG.P("Options options.size()="+options.size());
        //DEBUG.P("Options options.keySet()="+options.keySet());

        int ac = 0;
        while (ac < flags.length) {
        	DEBUG.P("flags["+ac+"]="+flags[ac]);
        	
            String flag = flags[ac];
            ac++;

            int j;
            // quick hack to speed up file processing: 
            // if the option does not begin with '-', there is no need to check
            // most of the compiler options.
            /*
            下面的程序代码技巧性很强，
            因为javac命令行的选项名称都是以'-'字符开头的,recognizedOptions数组中存放的
            选项除了最后一个是HiddenOption(SOURCEFILE)不以'-'字符开头外，其它所有选项
            名称都是以'-'字符开头的。如果在javac命令行中出现不是以'-'字符开头的选项，则
            查找位置firstOptionToCheck从recognizedOptions数组最末尾开始,
            (也就是直接与recognizedOptions数组的最后一个选项比较)
            它要么是要编译的源文件，要么是错误的选项。
            
            当出现在javac命令行中的选项是以'-'字符开头时，
            查找位置firstOptionToCheck从recognizedOptions数组第一个元素开始，直到
            搜索完整个recognizedOptions数组(j == recognizedOptions.length)时，才能
            确定是错误的选项。
            */
            
			//如果flag.length()的长度为0时会出现异常
			//见com.sun.tools.javac.main.CommandLine类中的注释
            int firstOptionToCheck = flag.charAt(0) == '-' ? 0 : recognizedOptions.length-1;
            
			for (j=firstOptionToCheck; j<recognizedOptions.length; j++)
                if (recognizedOptions[j].matches(flag)) break;

            if (j == recognizedOptions.length) {
                error("err.invalid.flag", flag);
                return null;
            }
            

            Option option = recognizedOptions[j];
            //参看JavacOption.hasArg()中的注释
			//另外，一个选项最多只带一个参数
            if (option.hasArg()) {
                if (ac == flags.length) {
                	/*错误例子:
                	F:\Javac>javac -d
					javac: -d 需要参数
					用法: javac <options> <source files>
					-help 用于列出可能的选项
					*/
                    error("err.req.arg", flag);
                    return null;
                }
                String operand = flags[ac];
                ac++;
                
                //大多数process()内部都是把flag与operand构成一<K,V>对，
                //存入options中,options可以看成是一个Map<K,V>
                //细节请看com.sun.tools.javac.main.RecognizedOptions类的getAll()方法
                if (option.process(options, flag, operand))
                    return null;
            } else {
            	//大多数process()内部都是把flag与flag构成一<K,V>对，
                //存入options中,options可以看成是一个Map<K,V>
                //细节请看com.sun.tools.javac.main.RecognizedOptions类的getAll()方法
                if (option.process(options, flag))
                    return null;
            }
        }
        
        //当在javac命令行中指定了“-d <目录>”选项时，
        //检查<目录>是否存在，不存在或不是目录则提示错误并返回
        if (!checkDirectory("-d"))
            return null;
        //当在javac命令行中指定了“-s <目录>”选项时，
        //检查<目录>是否存在，不存在或不是目录则提示错误并返回
        if (!checkDirectory("-s"))
            return null;
            
        //如果命令行中没带-source与-target选项，则采用默认值
        String sourceString = options.get("-source");
        Source source = (sourceString != null)
        //在这里lookup()一定不会返回null,因为在上面
        //的(recognizedOptions[j].matches(flag))时如果有错已经检测出来
            ? Source.lookup(sourceString)
            : Source.DEFAULT;
        String targetString = options.get("-target");
        //在这里lookup()一定不会返回null,因为在上面
        //的(recognizedOptions[j].matches(flag))时如果有错已经检测出来
        Target target = (targetString != null)
            ? Target.lookup(targetString)
            : Target.DEFAULT;
        // We don't check source/target consistency for CLDC, as J2ME
        // profiles are not aligned with J2SE targets; moreover, a
        // single CLDC target may have many profiles.  In addition,
        // this is needed for the continued functioning of the JSR14
        // prototype.

		DEBUG.P("sourceString="+sourceString);
		DEBUG.P("source="+source);
		DEBUG.P("source.requiredTarget()="+source.requiredTarget());
		DEBUG.P("targetString="+targetString);
		DEBUG.P("target="+target);
        //如果是"-target jsr14"，则不用执行下面的代码
        if (Character.isDigit(target.name.charAt(0))) {
        	//当target的版本号<source的版本号
            if (target.compareTo(source.requiredTarget()) < 0) {
                if (targetString != null) {
                    if (sourceString == null) {//指定-target，没指定-source的情况
                    	/*错误例子:
                    	F:\Javac>javac -target 1.4
						javac: 目标版本 1.4 与默认的源版本 1.5 冲突
						*/
                        warning("warn.target.default.source.conflict",
                                targetString,
                                source.requiredTarget().name);
                    } else {//指定-target，同时指定-source的情况
                    	/*错误例子:
                    	F:\Javac>javac -target 1.4 -source 1.5
						javac: 源版本 1.5 需要目标版本 1.5
						*/
                        warning("warn.source.target.conflict",
                                sourceString,
                                source.requiredTarget().name);
                    }
                    return null;
                } else {
                	//没有指定-target时，target取默认版本号(javac1.7默认是1.6)
                	//如果默认版本号还比source低，则target版本号由source决定
                    options.put("-target", source.requiredTarget().name);
                }
            } else {
            	//当target的版本号>=source的版本号且用户没在
            	//javac命令行中指定“-target”选项，且不允许使用
            	//泛型时，target版本默认为1.4
                if (targetString == null && !source.allowGenerics()) {
                    options.put("-target", Target.JDK1_4.name);
                }
            }
        }
        return filenames.toList();
        
    }finally{//我加上的
	DEBUG.P("");
	DEBUG.P("source="+options.get("-source"));
	DEBUG.P("target="+options.get("-target"));

	DEBUG.P("");
    DEBUG.P("ListBuffer<File> filenames.size()="+filenames.size());
    DEBUG.P("ListBuffer<String> classnames.size()="+classnames.size());
    //DEBUG.P("Options options.size()="+options.size());
    //DEBUG.P("Options options.keySet()="+options.keySet());
    
    DEBUG.P("options后="+options);
	DEBUG.P(0,this,"processArgs(1)");
	}
	
    }