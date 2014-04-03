    /** Visitor method: enter all classes in given tree, catching any
     *	completion failure exceptions. Return the tree's type.
     *
     *	@param tree    The tree to be visited.
     *	@param env     The environment visitor argument.
     */
    Type classEnter(JCTree tree, Env<AttrContext> env) {
		DEBUG.P(this,"classEnter(JCTree tree, Env<AttrContext> env)");
		//Enter类只对JCCompilationUnit、JCClassDecl、JCTypeParameter这三种树定义了visitXXX()方法
		//其他种类的树只有一个默认的visitTree(重写了超类JCTree.Visitor的visitTree)
		DEBUG.P("tree.tag="+tree.myTreeTag());
		Env<AttrContext> prevEnv = this.env;
		DEBUG.P("先前Env="+prevEnv);
		DEBUG.P("当前Env="+env);
		try {
			this.env = env;
			//调用JCTree的子类的accept(Visitor v),括号中的Visitor用Enter替代,
			//在JCTree的子类的accept(Visitor v)内部回调Enter中对应的visitXXX()
			tree.accept(this);
			return result;
		}  catch (CompletionFailure ex) {//类全限定名称:com.sun.tools.javac.code.Symbol.CompletionFailure
			return chk.completionError(tree.pos(), ex);
		} finally {
			DEBUG.P(1,this,"classEnter(JCTree tree, Env<AttrContext> env)");
			this.env = prevEnv;
		}
    }

    /** Visitor method: enter classes of a list of trees, returning a list of types.
     */
    <T extends JCTree> List<Type> classEnter(List<T> trees, Env<AttrContext> env) {
		DEBUG.P(this,"classEnter(2)");
		DEBUG.P("List<T> trees.size()="+trees.size());
		ListBuffer<Type> ts = new ListBuffer<Type>();
		for (List<T> l = trees; l.nonEmpty(); l = l.tail)
			ts.append(classEnter(l.head, env));
		DEBUG.P(2,this,"classEnter(2)");
		return ts.toList();
    }