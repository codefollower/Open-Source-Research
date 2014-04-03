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
			//ownkind只能代表单个kind，而pkind可以是多个kind的复合
			//从下面的kindName与kindNames也可能看出来
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