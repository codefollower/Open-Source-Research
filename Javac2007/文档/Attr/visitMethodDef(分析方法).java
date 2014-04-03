注意:术语“方法” 包括:实例方法、静态方法、构造函数

声明一个方法时，可以在前面加上 @Deprecated 或加上如下的JAVADOC:
---------------------
	/**
     * @deprecated
     */
---------------------
这两种方式都可以使得方法的flags_field(修饰符标志字段)含DEPRECATED(在Flags类中定义)


@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
1.1

Lint lint = env.info.lint.augment(m.attributes_field, m.flags());
------------------------------------------
class VisitMethodDefTest {

	@SuppressWarnings({"fallthrough","unchecked"})
	@Deprecated
	VisitMethodDefTest() {}
}
------------------------------------------
加-Xlint
env.info.lint=Lint:[values(11)[CAST, DEPRECATION, DEP_ANN, DIVZERO, EMPTY, FALLTHROUGH, FINALLY, OVERRIDES, PATH, SERIAL, UNCHECKED] suppressedValues(0)[]]
lint=Lint:[values(8)[CAST, DEP_ANN, DIVZERO, EMPTY, FINALLY, OVERRIDES, PATH, SERIAL] suppressedValues(3)[DEPRECATION, FALLTHROUGH, UNCHECKED]]


@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
1.2

chk.checkDeprecatedAnnotation(tree.pos(), m);
------------------------------------------
class VisitMethodDefTest {
	/**
     * @deprecated
     */
	VisitMethodDefTest() {}
}
------------------------------------------
加-Xlint  key=missing.deprecated.annotation
警告：[dep-ann] 未使用 @Deprecated 对已过时的项目进行注释
        VisitMethodDefTest() {}
        ^
1 警告


@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
1.3 分析类型变量的compound bounds (extends ClassA & InterfaceA)

attribBounds(tree.typarams);
------------------------------------------
class VisitMethodDefTest {
	class ClassA{}
	interface InterfaceA {}

	<TA,TB extends ClassA & InterfaceA> VisitMethodDefTest() {}
}
------------------------------------------
com.sun.tools.javac.comp.Attr===>attribBounds(1)
-------------------------------------------------------------------------
typarams=TA,TB extends ClassA & InterfaceA

typaram=TA
bound=java.lang.Object
bound.tsym.className=com.sun.tools.javac.code.Symbol$ClassSymbol
bound.tsym.flags_field=0x40000001 public acyclic 

typaram=TB extends ClassA & InterfaceA
bound=test.attr.VisitMethodDefTest.ClassA&test.attr.VisitMethodDefTest.InterfaceA
bound.tsym.className=com.sun.tools.javac.code.Symbol$ClassSymbol
bound.tsym.flags_field=0x51001401 public abstract synthetic compound unattributed acyclic 


@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
1.4

chk.checkOverride(tree, m);
------------------------------------------
class VisitMethodDefTest {
	public enum enum_no_finalize {
		;
		protected final void finalize(){}
		public final void finalize(){}
		public final void finalize(int i){}
		public final int finalize(){return 0;}
	}
}
------------------------------------------
key=enum.no.finalize

test\attr\VisitMethodDefTest.java:7: 已在 test.attr.VisitMethodDefTest.enum_no_f
inalize 中定义 finalize()
                public final void finalize(){}
                                  ^
test\attr\VisitMethodDefTest.java:9: 已在 test.attr.VisitMethodDefTest.enum_no_f
inalize 中定义 finalize()
                public final int finalize(){return 0;}
                                 ^
test\attr\VisitMethodDefTest.java:6: 枚举不能有 finalize 方法
                protected final void finalize(){}
                                     ^
test\attr\VisitMethodDefTest.java:7: 枚举不能有 finalize 方法
                public final void finalize(){}
                                  ^
test\attr\VisitMethodDefTest.java:9: 枚举不能有 finalize 方法
                public final int finalize(){return 0;}
                                 ^
5 错误


@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
1.5

com.sun.tools.javac.comp.Attr===>visitMethodDef(JCMethodDecl tree)
com.sun.tools.javac.comp.Check===>checkOverride(2)
com.sun.tools.javac.comp.Check===>checkOverride(4)
------------------------------------------
class ClassA {
	void m1() {}
}
class VisitMethodDefTest extends ClassA {
	static void m1() {}
}
------------------------------------------
key=override.static   key2=cant.override
test\attr\VisitMethodDefTest.java:7: test.attr.VisitMethodDefTest 中的 m1() 无法
覆盖 test.attr.ClassA 中的 m1()；覆盖的方法为静态
        static void m1() {}
                    ^
1 错误

备注(静态方法无法实现接口中的方法)
com.sun.tools.javac.comp.Attr===>attribClassBody(2)
com.sun.tools.javac.comp.Check===>checkImplementations(1)
com.sun.tools.javac.comp.Check===>checkImplementations(2)
com.sun.tools.javac.comp.Check===>checkOverride(4)
------------------------------------------
interface InterfaceA {
	void m1();
}
class VisitMethodDefTest implements InterfaceA {
	static void m1() {}
}
------------------------------------------
key=override.static   key2=cant.implement
test\attr\VisitMethodDefTest.java:7: test.attr.VisitMethodDefTest 中的 m1() 无法
实现 test.attr.InterfaceA 中的 m1()；覆盖的方法为静态
        static void m1() {}
                    ^
1 错误


com.sun.tools.javac.comp.Attr===>visitMethodDef(JCMethodDecl tree)
com.sun.tools.javac.comp.Check===>checkOverride(2)
com.sun.tools.javac.comp.Check===>checkOverride(4)
------------------------------------------
class ClassA {
	final void m1() {}
}
class VisitMethodDefTest extends ClassA {
	void m1() {}
}
------------------------------------------
key=override.meth   key2=cant.override
test\attr\VisitMethodDefTest.java:7: test.attr.VisitMethodDefTest 中的 m1() 无法
覆盖 test.attr.ClassA 中的 m1()；被覆盖的方法为 0x10 final
        void m1() {}
             ^
1 错误


com.sun.tools.javac.comp.Attr===>visitMethodDef(JCMethodDecl tree)
com.sun.tools.javac.comp.Check===>checkOverride(2)
com.sun.tools.javac.comp.Check===>checkOverride(4)
------------------------------------------
class ClassA {
	static void m1() {}
}
class VisitMethodDefTest extends ClassA {
	void m1() {}
}
------------------------------------------
key=override.meth   key2=cant.override
test\attr\VisitMethodDefTest.java:6: test.attr.VisitMethodDefTest 中的 m1() 无法
覆盖 test.attr.ClassA 中的 m1()；被覆盖的方法为 0x8 static
        void m1() {}
             ^
1 错误


com.sun.tools.javac.comp.Attr===>visitMethodDef(JCMethodDecl tree)
com.sun.tools.javac.comp.Check===>checkOverride(2)
com.sun.tools.javac.comp.Check===>checkOverride(4)
------------------------------------------
class ClassA {
	void m1(){}
	private void m2(){}
	public void m3(){}
	protected void m4(){}
}
class VisitMethodDefTest extends ClassA {
	protected void m1(){}
	void m2(){}
	void m3(){}
	private void m4(){}
}
------------------------------------------
key=override.weaker.access   key2=cant.override
test\attr\VisitMethodDefTest.java:12: test.attr.VisitMethodDefTest 中的 m3() 无
法覆盖 test.attr.ClassA 中的 m3()；正在尝试指定更低的访问权限；为 0x1 public
        void m3(){}
             ^
test\attr\VisitMethodDefTest.java:13: test.attr.VisitMethodDefTest 中的 m4() 无
法覆盖 test.attr.ClassA 中的 m4()；正在尝试指定更低的访问权限；为 0x4 protected
        private void m4(){}
                     ^
2 错误





















