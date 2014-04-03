	/** Check for cyclic references. Issue an error if the
     *  symbol of the type referred to has a LOCKED flag set.
     *
     *  @param pos      Position to be used for error reporting.
     *  @param t        The type referred to.
     */
    void checkNonCyclic(DiagnosticPosition pos, Type t) {
    DEBUG.P(this,"checkNonCyclic(2)");	
	checkNonCyclicInternal(pos, t);
	DEBUG.P(1,this,"checkNonCyclic(2)");
    }

    /** Check for cyclic references. Issue an error if the
     *  symbol of the type referred to has a LOCKED flag set.
     *
     *  @param pos      Position to be used for error reporting.
     *  @param t        The type referred to.
     *  @returns        True if the check completed on all attributed classes
     */
    private boolean checkNonCyclicInternal(DiagnosticPosition pos, Type t) {
	boolean complete = true; // was the check complete?
	//- System.err.println("checkNonCyclicInternal("+t+");");//DEBUG
	Symbol c = t.tsym;
	
	try {//我加上的
	DEBUG.P(this,"checkNonCyclicInternal(2)");
    DEBUG.P("Symbol c="+c);
	DEBUG.P("c.flags_field="+Flags.toString(c.flags_field));
	DEBUG.P("c.type.tag="+TypeTags.toString(c.type.tag));
	DEBUG.P("c.type.isErroneous()="+c.type.isErroneous());
	DEBUG.P("c.completer="+c.completer);
	
	//flags_field是一个复合标志位,凡是出现下面的情况(先&再与0进行!=比较)
	//都是用来判断flags_field是否包含所要比较的标志位,包含则为true,否则为false
	//例:如果c.flags_field=public unattributed,那么if ((c.flags_field & ACYCLIC) != 0)=false
	if ((c.flags_field & ACYCLIC) != 0) {
		DEBUG.P(c+" 已确认不存在循环，所以不再检测，直接返回。");
		return true;
	}
	//当同一个Symbol的flags_field在前一次置过LOCKED时,第二次checkNonCyclicInternal时
	//又是同一个Symbol,说明肯定存在循环继承
	if ((c.flags_field & LOCKED) != 0) {
	    noteCyclic(pos, (ClassSymbol)c);
	} else if (!c.type.isErroneous()) {
	    try {
		c.flags_field |= LOCKED;//加锁
		if (c.type.tag == CLASS) {
		    ClassType clazz = (ClassType)c.type;
		    //检查所有实现的接口
		    DEBUG.P("检查 "+clazz+" 的所有接口: "+clazz.interfaces_field);
		    if (clazz.interfaces_field != null)
			for (List<Type> l=clazz.interfaces_field; l.nonEmpty(); l=l.tail)
			    complete &= checkNonCyclicInternal(pos, l.head);
			    
			//检查超类
			DEBUG.P("检查 "+clazz+" 的超类: "+clazz.supertype_field);
		    if (clazz.supertype_field != null) {
			Type st = clazz.supertype_field;
			if (st != null && st.tag == CLASS)
			    complete &= checkNonCyclicInternal(pos, st);
		    }
		    
		    //检查外部类(通常是在Symbol c为一个内部类时，c.owner.kind == TYP)
		    DEBUG.P("检查 "+clazz+" 的owner: "+c.owner.type);
		    DEBUG.P("c.owner.kind="+Kinds.toString(c.owner.kind));
		    if (c.owner.kind == TYP)
			complete &= checkNonCyclicInternal(pos, c.owner.type);
		}
	    } finally {
		c.flags_field &= ~LOCKED;//解锁
	    }
	}
	if (complete)
	//((c.flags_field & UNATTRIBUTED) == 0)当flags_field不包含UNATTRIBUTED时为true
	    complete = ((c.flags_field & UNATTRIBUTED) == 0) && c.completer == null;
	if (complete) c.flags_field |= ACYCLIC;

	return complete;
	
	
	}finally{//我加上的
	DEBUG.P("");
	DEBUG.P("complete="+complete);
	DEBUG.P(c+".flags_field="+Flags.toString(c.flags_field));
	DEBUG.P(0,this,"checkNonCyclicInternal(2)");
	}
    }

    /** Note that we found an inheritance cycle. */
    private void noteCyclic(DiagnosticPosition pos, ClassSymbol c) {
    DEBUG.P(this,"noteCyclic(2)");
    DEBUG.P("ClassSymbol c="+c);
    
	log.error(pos, "cyclic.inheritance", c);
	for (List<Type> l=types.interfaces(c.type); l.nonEmpty(); l=l.tail)
	    l.head = new ErrorType((ClassSymbol)l.head.tsym);
	Type st = types.supertype(c.type);
	if (st.tag == CLASS)
	    ((ClassType)c.type).supertype_field = new ErrorType((ClassSymbol)st.tsym);
	c.type = new ErrorType(c);
	c.flags_field |= ACYCLIC;
	
	DEBUG.P("c.flags_field="+Flags.toString(c.flags_field));
	DEBUG.P(0,this,"noteCyclic(2)");
    }