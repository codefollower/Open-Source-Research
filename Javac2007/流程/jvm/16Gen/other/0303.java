    /** Insert instance initializer code into initial constructor.
     *  @param md        The tree potentially representing a
     *                   constructor's definition.
     *  @param initCode  The list of instance initializer statements.
     */
    void normalizeMethod(JCMethodDecl md, List<JCStatement> initCode) {
	/*
    //注意:只将initCod插入第一条语句不是this()调用的构造方法中

	对于如下源代码:
	------------------------------------
	public class Test {
		int fieldA=10;
		{
			fieldA=20;
		}

		{
			fieldB=20;
		}
		int fieldB=10;
	}
	------------------------------------

	经过编译器调整后，看起来像这样:
	------------------------------------
	public class Test {
		Test() {
			fieldA=10;
			fieldA=20;

			fieldB=20;
			fieldB=10;
		}
	}
	------------------------------------
	最终fieldA的值是20,fieldB的值是10，说明了一点，语句块与变量初始化语句
	在源代码中的顺序决定了变量的最终取值
	*/
    DEBUG.P(this,"normalizeMethod(2)");
    DEBUG.P("md.name="+md.name);
    DEBUG.P("isInitialConstructor="+TreeInfo.isInitialConstructor(md));
	if (md.name == names.init && TreeInfo.isInitialConstructor(md)) {
		DEBUG.P("JCMethodDecl md旧="+md);
	    // We are seeing a constructor that does not call another
	    // constructor of the same class.
	    List<JCStatement> stats = md.body.stats;
	    ListBuffer<JCStatement> newstats = new ListBuffer<JCStatement>();

	    if (stats.nonEmpty()) {
		// Copy initializers of synthetic variables generated in
		// the translation of inner classes.
		while (TreeInfo.isSyntheticInit(stats.head)) {
		    newstats.append(stats.head);
		    stats = stats.tail;
		}
		// Copy superclass constructor call
		newstats.append(stats.head);
		stats = stats.tail;
		// Copy remaining synthetic initializers.
		while (stats.nonEmpty() &&
		       TreeInfo.isSyntheticInit(stats.head)) {
		    newstats.append(stats.head);
		    stats = stats.tail;
		}
		// Now insert the initializer code.
		newstats.appendList(initCode);
		// And copy all remaining statements.
		while (stats.nonEmpty()) {
		    newstats.append(stats.head);
		    stats = stats.tail;
		}
	    }
	    md.body.stats = newstats.toList();
	    DEBUG.P("JCMethodDecl md新="+md);
	    if (md.body.endpos == Position.NOPOS)
		md.body.endpos = TreeInfo.endPos(md.body.stats.last());
	}
	DEBUG.P(0,this,"normalizeMethod(2)");
    }