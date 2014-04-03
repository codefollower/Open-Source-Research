数据结构
JCTree	Symbol	Type	Scope	Env

variables, methods and operators,types, packages

Symbol子类有:
TypeSymbol
PackageSymbol		包含		Scope members_field
ClassSymbol			包含		Scope members_field;
MethodSymbol
VarSymbol

DelegatedSymbol
OperatorSymbol


每个JCTree			包含		Type type

每个Type			包含		TypeSymbol tsym

每个Symbol			包含		Type type

JCCompilationUnit	包含		PackageSymbol packge
					包含		Scope namedImportScope;
					包含		Scope starImportScope;

JCClassDecl			包含		ClassSymbol sym

JCMethodDecl		包含		MethodSymbol sym

JCVariableDecl		包含		VarSymbol sym

JCNewClass			包含		Symbol constructor

JCAssignOp			包含		Symbol operator;

JCUnary				包含		Symbol operator;

JCBinary			包含		Symbol operator;

JCFieldAccess		包含		Symbol sym;

JCIdent				包含		Symbol sym;


Parser阶段只生成基本的JCTree，此时所有种类的JCTree的Type为null，
上面所示的几种JCTree的Symbol也为null，Scope也为null

在Enter阶段，在visitClassDef(JCClassDecl tree)方法中
填写JCClassDecl的ClassSymbol sym字段，并填写ClassSymbol sym
的内部字段flags_field,sourcefile,members_field
内部类的classfile字段一定为null
ClassType的outer_field与typarams_field字段也在Enter阶段的
visitClassDef(JCClassDecl tree)方法中设置,
supertype_field,interfaces_field,allparams_field为null

***第一阶段Enter完成***
-----------------------------------------------
包名: my.test
--------------------------
tree.packge.members_field: Scope[(nelems=17 owner=test)MyUncompletedClass, Test07, Test06, Test05, Test01, Test04, Test03, ExtendsTestBound, TestBound, TestOhter2, Test02, MyInterfaceB, MyInterfaceA, TestOhter, Test$TestInner, Test$1, Test]
tree.namedImportScope    : Scope[(nelems=1 owner=test)Test]
tree.starImportScope     : Scope[(nelems=0 owner=test)]

等待编译的类的总数: 5
--------------------------
类名             : my.test.Test
members_field    : Scope[(nelems=4 owner=Test)MyInnerEnum, MyInnerInterface, MyInnerClassStatic, MyInnerClass]
flags            : public 
sourcefile       : bin\mysrc\my\test\Test.java
classfile        : bin\mysrc\my\test\Test.java
type             : my.test.Test<S2704014,T13673945,E3705235>
outer_field      : <none>
supertype_field  : null
interfaces_field : null
typarams_field   : S2704014,T13673945,E3705235
allparams_field  : null

类名             : my.test.Test.MyInnerClass
members_field    : Scope[(nelems=0 owner=MyInnerClass)]
flags            : public 
sourcefile       : bin\mysrc\my\test\Test.java
classfile        : null
type             : my.test.Test<S2704014,T13673945,E3705235>.MyInnerClass
outer_field      : my.test.Test<S2704014,T13673945,E3705235>
supertype_field  : null
interfaces_field : null
typarams_field   : 
allparams_field  : null

类名             : my.test.Test.MyInnerClassStatic
members_field    : Scope[(nelems=0 owner=MyInnerClassStatic)]
flags            : public static 
sourcefile       : bin\mysrc\my\test\Test.java
classfile        : null
type             : my.test.Test.MyInnerClassStatic
outer_field      : <none>
supertype_field  : null
interfaces_field : null
typarams_field   : 
allparams_field  : null

类名             : my.test.Test.MyInnerInterface
members_field    : Scope[(nelems=0 owner=MyInnerInterface)]
flags            : public static interface abstract 
sourcefile       : bin\mysrc\my\test\Test.java
classfile        : null
type             : my.test.Test.MyInnerInterface
outer_field      : <none>
supertype_field  : null
interfaces_field : null
typarams_field   : 
allparams_field  : null

类名             : my.test.Test.MyInnerEnum
members_field    : Scope[(nelems=0 owner=MyInnerEnum)]
flags            : public static final enum 
sourcefile       : bin\mysrc\my\test\Test.java
classfile        : null
type             : my.test.Test.MyInnerEnum
outer_field      : <none>
supertype_field  : null
interfaces_field : null
typarams_field   : 
allparams_field  : null



在MemberEnter阶段:
1.先将java.lang包中的所有类导入每个JCCompilationUnit的starImportScope;

2.处理所有non-static与static导入(import)语句
因为所有的导入(import)语句都是用一棵JCFieldAccess树表示的(参见Parser.importDeclaration())，
JCFieldAccess树也含有JCIdent，
在MemberEnter阶段的visitImport(1)方法中会设
置JCFieldAccess与JCIdent的Symbol sym字段.
在com.sun.tools.javac.comp.Attr===>check(5)中设置JCTree的type字段

在complete(Symbol sym)中设置Type的supertype_field和interfaces_field

在com.sun.tools.javac.comp.Attr===>visitTypeParameter(1)中
将COMPOUND型TypeVar生成一个JCClassDecl放入enter.typeEnvs

如果JCMethodDecl有TypeParameter，它的type为ForAll类型，否则为MethodType
参考com.sun.tools.javac.comp.MemberEnter===>signature(5)

在com.sun.tools.javac.comp.MemberEnter===>visitMethodDef(1)中
将JCMethodDecl对应的MethodSymbol填入JCClassDecl的ClassSymbol sym的members_field

在com.sun.tools.javac.comp.MemberEnter===>signature(5)中
还将方法的TypeParameter及方法括号中的普通参数放入对应MethodSymbol的scope

默认构造方法在:
com.sun.tools.javac.comp.MemberEnter.DefaultConstructor()方法设置

在构造方法中加入super()是在:
com.sun.tools.javac.comp.Attr.visitMethodDef()方法设置

JCTree.type都是erasure后的type

成员类的MemberEnter：
是通过成员类所对的ClassSymbol的一些能够触发
ClassSymbol.complete()的方法来间接调用
com.sun.tools.javac.comp.MemberEnter===>complete(Symbol sym)而完成
对成员类的MemberEnter。

如:
public class Test{
	public class MyTestInnerClass<Z>{}
	
	public void myMethod(MyTestInnerClass<String> m) {}
}

当解析到myMethod方法的参数：MyTestInnerClass<String> m时
会通过com.sun.tools.javac.comp.Attr===>visitIdent(1)查找MyTestInnerClass，
当想要知道MyTestInnerClass对应的ClassSymbol的flags_field字段内容时，
会通过ClassSymbol.flags()方法来查看，如果MyTestInnerClass从未complete，那么
就调用com.sun.tools.javac.comp.MemberEnter===>complete(Symbol sym)来完成
对成员类MyTestInnerClass的MemberEnter。


JCTypeApply.type与JCTypeApply.type.tsym.type是不一样的，
前者是实参，后者只是形参。
如:
tree.type=my.test.Test<S12122157,P28145575,V25864734,T10923757,E19300430>.ExtendsTest<?{:java.lang.Object:},? super my.test.Test.ExtendsTest{:java.lang.Object:}>
tree.type.tsym.type=my.test.Test<S12122157,P28145575,V25864734,T10923757,E19300430>.ExtendsTest<T471035,S31406333>
com.sun.tools.javac.comp.Attr===>visitTypeApply(JCTypeApply tree)  END




