/* ************************************************************************
 * main method
 *************************************************************************/

    /** Generate code for a class definition.
     *  @param env   The attribution environment that belongs to the
     *               outermost class containing this class definition.
     *               We need this for resolving some additional symbols.
     *  @param cdef  The tree representing the class definition.
     *  @return      True if code is generated with no errors.
     */
    public boolean genClass(Env<AttrContext> env, JCClassDecl cdef) {
    DEBUG.P(this,"genClass(2) 正在生成字节码......");
	DEBUG.P("cdef="+cdef);
    DEBUG.P("env="+env);
	try {
	    attrEnv = env;
	    ClassSymbol c = cdef.sym;
	    this.toplevel = env.toplevel;
	    this.endPositions = toplevel.endPositions;
	    
	    DEBUG.P("generateIproxies="+generateIproxies);
	    DEBUG.P("allowGenerics="+allowGenerics);
	    DEBUG.P("c="+c);
	    DEBUG.P("c.flags()="+Flags.toString(c.flags()));

	    // If this is a class definition requiring Miranda methods,
	    // add them.
	    if (generateIproxies && //jdk1.1与jdk1.0才需要
		(c.flags() & (INTERFACE|ABSTRACT)) == ABSTRACT
		&& !allowGenerics // no Miranda methods available with generics
		)
		implementInterfaceMethods(c);
		
        cdef.defs = normalizeDefs(cdef.defs, c);
        //经过normalizeDefs(cdef.defs, c)后，类体(defs)中只包含方法(构造方法和非构造方法)
        //内部类或内部接口也不包含在类体(defs)中
        DEBUG.P("cdef.defs(规范化后的类体)="+cdef.defs);
	    c.pool = pool;
	    pool.reset();
	    Env<GenContext> localEnv =
		new Env<GenContext>(cdef, new GenContext());
	    localEnv.toplevel = env.toplevel;
	    localEnv.enclClass = cdef;
	    
	    int myMethodCount=1;
	    DEBUG.P(2);DEBUG.P("开始为每一个方法生成字节码...(方法总个数: "+cdef.defs.size()+")");
	    for (List<JCTree> l = cdef.defs; l.nonEmpty(); l = l.tail) {
	    DEBUG.P("第 "+myMethodCount+" 个方法开始...");
		genDef(l.head, localEnv);
		DEBUG.P("第 "+myMethodCount+" 个方法结束...");
		myMethodCount++;DEBUG.P(2);
	    }
	    
	    if (pool.numEntries() > Pool.MAX_ENTRIES) {
		log.error(cdef.pos(), "limit.pool");
		nerrs++;
	    }
	    if (nerrs != 0) {
		// if errors, discard code
		for (List<JCTree> l = cdef.defs; l.nonEmpty(); l = l.tail) {
		    if (l.head.tag == JCTree.METHODDEF)
			((JCMethodDecl) l.head).sym.code = null;
		}
	    }
            cdef.defs = List.nil(); // discard trees
	    return nerrs == 0;
	} finally {
	    // note: this method does NOT support recursion.
	    attrEnv = null;
	    this.env = null;
	    toplevel = null;
	    endPositions = null;
	    nerrs = 0;
	    DEBUG.P(2,this,"genClass(2)");
	}
    }