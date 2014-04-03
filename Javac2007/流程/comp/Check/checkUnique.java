    /** Check that symbol is unique in given scope.
     *	@param pos	     Position for error reporting.
     *	@param sym	     The symbol.
     *	@param s	     The scope.
     */
    boolean checkUnique(DiagnosticPosition pos, Symbol sym, Scope s) {
    try {//我加上的
	DEBUG.P(this,"checkUnique(3)");
	DEBUG.P("Scope s="+s);
	DEBUG.P("sym.name="+sym.name);
	DEBUG.P("sym.type.isErroneous()="+sym.type.isErroneous());
	
	if (sym.type.isErroneous())
	    return true;
	DEBUG.P("sym.owner.name="+sym.owner.name);    
	if (sym.owner.name == names.any) return false;//errSymbol见Symtab类
		/*
		注意这里for的结束条件不能是e.scope != null，例如在MemberEnter===>methodEnv(2)中
		方法对应的scope的next指向类的scope，如果类中定义了与方法相同名称的类型变量
		如:
		class VisitMethodDefTest<T> {
			<T> void m1(int i1,int i2) throws T{}
		}
		就会出现错误:
		test\memberEnter\VisitMethodDefTest.java:13: 已在 test.memberEnter.VisitMethodDefTest 中定义 T
		这是因为s.lookup(sym.name)会查找完所有的scope链表
		*/
		//for (Scope.Entry e = s.lookup(sym.name); e.scope != null; e = e.next()) {
	for (Scope.Entry e = s.lookup(sym.name); e.scope == s; e = e.next()) {
		DEBUG.P("e.scope="+e.scope);
		DEBUG.P("e.sym="+e.sym);
	    if (sym != e.sym &&
		sym.kind == e.sym.kind &&
		sym.name != names.error &&
		/*
		//两个方法，不管是不是范型方法，也不管两个方法的返回值是否一样，
		//只要方法名一样，参数类型一样，就认为是错误的
		例如:
		void m2(int[] i1) {}
		<T> void m2(int... i1) {}
		或
		void m2(int[] i1) {}
		<T> int m2(int... i1) {}

		错误:
		test\memberEnter\VisitMethodDefTest.java:22: 无法在 test.memberEnter.VisitMethod
		DefTest 中同时声明 <T {bound=Object}>m2(int...) 和 m2(int[])
				<T> int m2(int... i1) {}
						^
		1 错误
		*/

		(sym.kind != MTH || types.overrideEquivalent(sym.type, e.sym.type))) {
		if ((sym.flags() & VARARGS) != (e.sym.flags() & VARARGS))
		    varargsDuplicateError(pos, sym, e.sym);
		else 
		    duplicateError(pos, e.sym);
		return false;
	    }
	}
	return true;
    
    
    }finally{//我加上的
	DEBUG.P(0,this,"checkUnique(3)");
	}
	
    }