    /** Check that single-type import is not already imported or top-level defined,
     *	but make an exception for two single-type imports which denote the same type.
     *	@param pos	     Position for error reporting.
     *	@param sym	     The symbol.
     *	@param s	     The scope
     */
    boolean checkUniqueImport(DiagnosticPosition pos, Symbol sym, Scope s) {
	return checkUniqueImport(pos, sym, s, false);
    }

    /** Check that static single-type import is not already imported or top-level defined,
     *	but make an exception for two single-type imports which denote the same type.
     *	@param pos	     Position for error reporting.
     *	@param sym	     The symbol.
     *	@param s	     The scope
     *  @param staticImport  Whether or not this was a static import
     */
    boolean checkUniqueStaticImport(DiagnosticPosition pos, Symbol sym, Scope s) {
	try {//我加上的
	DEBUG.P(this,"checkUniqueStaticImport(3)");
	
	return checkUniqueImport(pos, sym, s, true);
	
	}finally{//我加上的
	DEBUG.P(0,this,"checkUniqueStaticImport(3)");
	}
    }

    /** Check that single-type import is not already imported or top-level defined,
     *	but make an exception for two single-type imports which denote the same type.
     *	@param pos	     Position for error reporting.
     *	@param sym	     The symbol.
     *	@param s	     The scope.
     *  @param staticImport  Whether or not this was a static import
     */
    private boolean checkUniqueImport(DiagnosticPosition pos, Symbol sym, Scope s, boolean staticImport) {
		try {//我加上的
		DEBUG.P(this,"checkUniqueImport(4)");
		DEBUG.P("Symbol sym="+sym);
		DEBUG.P("Scope s="+s);
		DEBUG.P("staticImport="+staticImport);
		
		for (Scope.Entry e = s.lookup(sym.name); e.scope != null; e = e.next()) {
			// is encountered class entered via a class declaration?
			boolean isClassDecl = e.scope == s;
			DEBUG.P("e.scope="+e.scope);
			DEBUG.P("isClassDecl="+isClassDecl);
			DEBUG.P("(sym != e.sym)="+(sym != e.sym));

			if ((isClassDecl || sym != e.sym) &&
			sym.kind == e.sym.kind &&
			sym.name != names.error) {
				if (!e.sym.type.isErroneous()) {
					String what = e.sym.toString();
					if (!isClassDecl) {
						/*如:
						import static my.StaticImportTest.MyInnerClassStaticPublic;
						import static my.ExtendsTest.MyInnerClassStaticPublic;
						import java.util.Date;
						import java.sql.Date;
						
						bin\mysrc\my\test\Test.java:5: 已在静态 single-type 导入中定义 my.StaticImportTest.MyInnerClassStaticPublic
						import static my.ExtendsTest.MyInnerClassStaticPublic;
						^
						bin\mysrc\my\test\Test.java:7: 已在 single-type 导入中定义 java.util.Date
						import java.sql.Date;
						^
						2 错误
						*/
						if (staticImport)
							log.error(pos, "already.defined.static.single.import", what);
						else
							log.error(pos, "already.defined.single.import", what);
					}
						/*
						src/my/test/EnterTest.java:9: 已在该编译单元中定义 my.test.InnerInterface
						import static my.test.EnterTest.InnerInterface;
						^
						
						源码：
						import static my.test.EnterTest.InnerInterface;

						interface InnerInterface{}
						public class EnterTest {
							public static interface InnerInterface<T extends EnterTest> {}
							public void m() {
								class LocalClass{}
							}
						}*/
					//如果是import static my.test.InnerInterface就不会报错
					//因为此时sym == e.sym，虽然没报错，但是还是返回false，指明不用
					//把这个sym加入env.toplevel.namedImportScope
					else if (sym != e.sym)
						log.error(pos, "already.defined.this.unit", what);//已在该编译单元中定义
				}
				return false;
			}
		}
		return true;
		
		}finally{//我加上的
		DEBUG.P(0,this,"checkUniqueImport(4)");
		}
    }