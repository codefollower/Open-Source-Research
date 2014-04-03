    /** Check kind and type of given tree against protokind and prototype.
     *  If check succeeds, store type in tree and return it.
     *  If check fails, store errType in tree and return it.
     *  No checks are performed if the prototype is a method type.
     *  Its not necessary in this case since we know that kind and type
     *  are correct.
     *
     *  @param tree     The tree whose kind and type is checked
     *  @param owntype  The computed type of the tree
     *  @param ownkind  The computed kind of the tree
     *  @param pkind    The expected kind (or: protokind) of the tree
     *  @param pt       The expected type (or: prototype) of the tree
     */
	//如果pkind是PCK与TYP，就表示当前symbol的kind(也就是ownkind)
	//要么是PCK,要么是TYP，如果都不是就报错，期待的kind(PCK或TYP)没找到
	//最后给tree.type赋值
    Type check(JCTree tree, Type owntype, int ownkind, int pkind, Type pt) {
    	DEBUG.P(this,"check(5)");
    	DEBUG.P("tree.type="+tree.type);
    	DEBUG.P("ownkind="+Kinds.toString(ownkind));
    	DEBUG.P("owntype.tag="+TypeTags.toString(owntype.tag));
    	DEBUG.P("pkind="+Kinds.toString(pkind));
    	DEBUG.P("pt.tag="+TypeTags.toString(pt.tag));

        if (owntype.tag != ERROR && pt.tag != METHOD && pt.tag != FORALL) {
        	//如果ownkind所代表的Kinds在pkind中没有，则报错
        	/*比如：如果ownkind是VAR,而pkind是PCK与TYP
        	bin\mysrc\my\test\Test.java:3: 意外的类型
			需要： 类、软件包
			找到： 变量
			*/
            if ((ownkind & ~pkind) == 0) {
                owntype = chk.checkType(tree.pos(), owntype, pt);
            } else {
                log.error(tree.pos(), "unexpected.type",
                          Resolve.kindNames(pkind),
                          Resolve.kindName(ownkind));
                owntype = syms.errType;
            }
        }
        tree.type = owntype;
        DEBUG.P(0,this,"check(5)");
        return owntype;
    }

	    /** Check that a given type is assignable to a given proto-type.
     *  If it is, return the type, otherwise return errType.
     *  @param pos        Position to be used for error reporting.
     *  @param found      The type that was found.
     *  @param req        The type that was required.
     */
    Type checkType(DiagnosticPosition pos, Type found, Type req) {
    try {//我加上的
	DEBUG.P(this,"checkType(3)");
	DEBUG.P("found.tag="+TypeTags.toString(found.tag));
	DEBUG.P("req.tag="+TypeTags.toString(req.tag));

	if (req.tag == ERROR)
	    return req;
	if (found.tag == FORALL)
	    return instantiatePoly(pos, (ForAll)found, req, convertWarner(pos, found, req));
	if (req.tag == NONE)
	    return found;
	if (types.isAssignable(found, req, convertWarner(pos, found, req)))
	    return found;
	if (found.tag <= DOUBLE && req.tag <= DOUBLE)
	    return typeError(pos, JCDiagnostic.fragment("possible.loss.of.precision"), found, req);
	if (found.isSuperBound()) {
	    log.error(pos, "assignment.from.super-bound", found);
	    return syms.errType;
	}
	if (req.isExtendsBound()) {
	    log.error(pos, "assignment.to.extends-bound", req);
	    return syms.errType;
	}
	return typeError(pos, JCDiagnostic.fragment("incompatible.types"), found, req);
	
	
	}finally{//我加上的
	DEBUG.P(0,this,"checkType(3)");
	}
    }