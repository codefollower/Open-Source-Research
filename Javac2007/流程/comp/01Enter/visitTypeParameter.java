    /** Class enter visitor method for type parameters.
     *	Enter a symbol for type parameter in local scope, after checking that it
     *	is unique.
     */
    /*
    TypeParameter不会加入ClassSymbol.members_field中，
    只加入与JCClassDecl对应的Env<AttrContext>.info.Scope中。

    另外，在方法与类定义的TypeParameter可以有相同的类型变量名，
    两者互不影响。如下所示:
    class Test<T,S> {
            public <T> void method(T t){}
    }
    */
    public void visitTypeParameter(JCTypeParameter tree) {
        DEBUG.P(this,"visitTypeParameter(JCTypeParameter tree)");
        DEBUG.P("tree.name="+tree.name);
        DEBUG.P("tree.type="+tree.type);
        DEBUG.P("env.info.scope.owner="+env.info.scope.owner);
        if(env.info.scope.owner instanceof ClassSymbol)
            DEBUG.P("env.info.scope.owner.members_field="+((ClassSymbol)env.info.scope.owner).members_field);
        DEBUG.P("env.info.scope="+env.info.scope);
    
    
		TypeVar a = (tree.type != null)
			? (TypeVar)tree.type
			: new TypeVar(tree.name, env.info.scope.owner);
		tree.type = a;
		/*TypeParameter不能重名，如果有重名的TypeParameter，
		并不是在Parser阶段检查出错误的，而在下面的checkUnique()方法中。
		
		错误例子:
		bin\mysrc\my\test\Test.java:64: 已在 my.test.Test2 中定义 T
		class Test2<T,T>{}
					  ^
		1 错误
		*/
		if (chk.checkUnique(tree.pos(), a.tsym, env.info.scope)) {
			env.info.scope.enter(a.tsym);
		}
		result = a;
		
		
		if(env.info.scope.owner instanceof ClassSymbol)
            DEBUG.P("env.info.scope.owner.members_field="+((ClassSymbol)env.info.scope.owner).members_field);
        DEBUG.P("env.info.scope="+env.info.scope);
        DEBUG.P(0,this,"visitTypeParameter(JCTypeParameter tree)");
    }