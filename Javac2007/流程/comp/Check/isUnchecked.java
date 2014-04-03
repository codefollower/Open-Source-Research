    /** Is exc an exception symbol that need not be declared?
     */
	//平常所说的未检查异常:
	//就是java.lang.Error与java.lang.RuntimeException及这两者的子类
	//所谓“未检查”就是说编译器不会在源代码中检查哪些地方使用到了上面所说的
	//异常，即使你在方法中用throws或throw抛出了上面所说的异常，
	//当前方法或别的方法用到这样的异常也不需要用try/catch捕获或重新抛出
	//除了上面所说的异常之外的异常都是“已检查异常”，
	//只要方法中用throws或throw抛出了“已检查异常”，
	//那么当前方法或别的方法用到这样的异常就必需用try/catch捕获或重新抛出
    boolean isUnchecked(ClassSymbol exc) {
		return
			exc.kind == ERR ||
			exc.isSubClass(syms.errorType.tsym, types) ||
			exc.isSubClass(syms.runtimeExceptionType.tsym, types);
    }

    /** Is exc an exception type that need not be declared?
     */
    boolean isUnchecked(Type exc) {
		return
			(exc.tag == TYPEVAR) ? isUnchecked(types.supertype(exc)) :
			(exc.tag == CLASS) ? isUnchecked((ClassSymbol)exc.tsym) :
			exc.tag == BOT;
    }

    /** Same, but handling completion failures.
     */
    boolean isUnchecked(DiagnosticPosition pos, Type exc) {
		try {
			return isUnchecked(exc);
		} catch (CompletionFailure ex) {
			completionError(pos, ex);
			return true;
		}
    }