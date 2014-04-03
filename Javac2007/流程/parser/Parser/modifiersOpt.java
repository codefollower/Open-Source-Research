    /** ModifiersOpt = { Modifier }
     *  Modifier = PUBLIC | PROTECTED | PRIVATE | STATIC | ABSTRACT | FINAL
     *           | NATIVE | SYNCHRONIZED | TRANSIENT | VOLATILE | "@"(单独一个@是不行的)
     *           | "@" Annotation
     */
    JCModifiers modifiersOpt() {
        return modifiersOpt(null);
    }
    JCModifiers modifiersOpt(JCModifiers partial) {
    	DEBUG.P(this,"modifiersOpt(1)");	
    	
    	//flags是各种Modifier通过“位或运算(|)”得到
    	//在com.sun.tools.javac.code.Flags类中用一位(bit)表示一个Modifier
    	//因flags是long类型，所以可表示64个不同的Modifier
    	//如flags=0x01时表示Flags.PUBLIC,当flags=0x03时表示Flags.PUBLIC与Flags.PRIVATE
    	//把flags传到Flags.toString(long flags)方法就可以知道flags代表哪个(哪些)Modifier
        long flags = (partial == null) ? 0 : partial.flags;

        //当Scanner在Javadoc中扫描到有@deprecated时S.deprecatedFlag()返回true
        if (S.deprecatedFlag()) {
            flags |= Flags.DEPRECATED;
            S.resetDeprecatedFlag();
        }
        DEBUG.P("(while前) flags="+flags+" modifiers=("+Flags.toString(flags)+")");
        
        ListBuffer<JCAnnotation> annotations = new ListBuffer<JCAnnotation>();
        if (partial != null) annotations.appendList(partial.annotations);
        int pos = S.pos();
        int lastPos = Position.NOPOS;
    loop:
        while (true) {
            // <editor-fold defaultstate="collapsed">
            long flag;
			/*
			在Flags类中定义了12个Standard Java flags，
			但是下面的switch语句中少了INTERFACE，
			这是因为INTERFACE(还有ENUM)后面不能再有其他修饰符了，
			当S.token()==INTERFACE时，退出while循环，最后再追加INTERFACE修饰符标志
			*/
            switch (S.token()) {
	            case PRIVATE     : flag = Flags.PRIVATE; break;
	            case PROTECTED   : flag = Flags.PROTECTED; break;
	            case PUBLIC      : flag = Flags.PUBLIC; break;
	            case STATIC      : flag = Flags.STATIC; break;
	            case TRANSIENT   : flag = Flags.TRANSIENT; break;
	            case FINAL       : flag = Flags.FINAL; break;
	            case ABSTRACT    : flag = Flags.ABSTRACT; break;
	            case NATIVE      : flag = Flags.NATIVE; break;
	            case VOLATILE    : flag = Flags.VOLATILE; break;
	            case SYNCHRONIZED: flag = Flags.SYNCHRONIZED; break;
	            case STRICTFP    : flag = Flags.STRICTFP; break;
	            case MONKEYS_AT  : flag = Flags.ANNOTATION; break;
	            default: break loop;
            }
            //修饰符重复,错误提示信息在com\sun\tools\javac\resources\compiler.properties定义
            if ((flags & flag) != 0) log.error(S.pos(), "repeated.modifier");
            //报告错误后并没有中断程序的运行，只是在Log中记录下错误发生次数
            //DEBUG.P("Log.nerrors="+log.nerrors);
            
            lastPos = S.pos();
            S.nextToken();
           
            if (flag == Flags.ANNOTATION) {
                checkAnnotations();//检查当前的-source版本是否支持注释
                
                //非“@interface”语法注释识别(@interface用于注释类型的定义)
                //“@interface”语法在com.sun.tools.javac.util.Version类中有这样的例子
                //JDK1.6中有关于Annotations的文档在technotes/guides/language/annotations.html
                if (S.token() != INTERFACE) {
					//lastPos是@的开始位置
                    JCAnnotation ann = annotation(lastPos);
					DEBUG.P("pos="+pos);
					DEBUG.P("ann.pos="+ann.pos);
                    // if first modifier is an annotation, set pos to annotation's.
                    if (flags == 0 && annotations.isEmpty())
                        pos = ann.pos;
                    annotations.append(ann);
                    lastPos = ann.pos;

                    //注意这里,对下面的checkNoMods(mods.flags)有影响
                    flag = 0;
                }
            }
            flags |= flag;
            // </editor-fold>
        }
        switch (S.token()) {
	        case ENUM: flags |= Flags.ENUM; break;
	        case INTERFACE: flags |= Flags.INTERFACE; break;
	        default: break;
        }
        
        DEBUG.P("(while后)  flags="+flags+" modifiers=("+Flags.toString(flags)+")");
        DEBUG.P("JCAnnotation count="+annotations.size());

        /* A modifiers tree with no modifier tokens or annotations
         * has no text position. */
        if (flags == 0 && annotations.isEmpty())
            pos = Position.NOPOS;
            
        JCModifiers mods = F.at(pos).Modifiers(flags, annotations.toList());
        
        if (pos != Position.NOPOS)
            storeEnd(mods, S.prevEndPos());//storeEnd()只是一个空方法,子类EndPosParser已重写
            
        DEBUG.P(1,this,"modifiersOpt(1)");	
        return mods;
    }