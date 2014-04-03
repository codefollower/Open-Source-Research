com.sun.tools.javac.jvm.ClassWriter===>ClassWriter(1)
-------------------------------------------------------------------------
com.sun.tools.javac.jvm.ClassWriter===>ClassWriter(1)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writeClass(ClassSymbol c)
-------------------------------------------------------------------------
c=test.jvm.Test.MemberClass
c.sourcefile=test\jvm\ClassWriterTest.java
outFile=classes\test\jvm\Test$MemberClass.class
com.sun.tools.javac.jvm.ClassWriter===>writeClassFile(OutputStream out, ClassSymbol c)
-------------------------------------------------------------------------
supertype=java.lang.Object  supertype.tag=CLASS
interfaces=
typarams=A {bound=Number},B {bound=Number},C {bound=Object}
com.sun.tools.javac.jvm.ClassWriter===>adjustFlags(1)
-------------------------------------------------------------------------
flags =
result=
com.sun.tools.javac.jvm.ClassWriter===>adjustFlags(1)  END
-------------------------------------------------------------------------

flagNames= SUPER
flags =synchronized 
c.members()=Scope[(entries=2 nelems=2 owner=MemberClass)this$0, <init>()]
fieldsCount =1
methodsCount=1
com.sun.tools.javac.jvm.ClassWriter===>writeFields(Scope.Entry e)
-------------------------------------------------------------------------
vars=this$0
com.sun.tools.javac.jvm.ClassWriter===>writeField(VarSymbol v)
-------------------------------------------------------------------------
v=this$0
v.flags()=final synthetic 
com.sun.tools.javac.jvm.ClassWriter===>adjustFlags(1)
-------------------------------------------------------------------------
flags =final synthetic 
result=final synthetic 
com.sun.tools.javac.jvm.ClassWriter===>adjustFlags(1)  END
-------------------------------------------------------------------------

flags=final synthetic 
com.sun.tools.javac.jvm.ClassWriter===>typeSig(Type type)
-------------------------------------------------------------------------
com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)
-------------------------------------------------------------------------
type=test.jvm.Test type.tag=CLASS
com.sun.tools.javac.jvm.ClassWriter===>assembleClassSig(Type type)
-------------------------------------------------------------------------
type=test.jvm.Test type.tag=CLASS
com.sun.tools.javac.jvm.ClassWriter===>enterInner(1)
-------------------------------------------------------------------------
c=test.jvm.Test
innerClasses前=null
innerClassesQueue前=null

c.type=test.jvm.Test<A {bound=Number},B {bound=Number},C {bound=Object}>  c.type.tag=CLASS
pool=com.sun.tools.javac.jvm.Pool@12f0999
c.owner.kind=PCK 
innerClasses后=null
innerClassesQueue后=null
com.sun.tools.javac.jvm.ClassWriter===>enterInner(1)  END
-------------------------------------------------------------------------

outer=<none> outer.tag=NONE
outer.allparams()=
c.flatname=test.jvm.Test
ct.getTypeArguments()=
com.sun.tools.javac.jvm.ClassWriter===>assembleClassSig(Type type)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)  END
-------------------------------------------------------------------------

return typeName=Ltest/jvm/Test;
com.sun.tools.javac.jvm.ClassWriter===>typeSig(Type type)  END
-------------------------------------------------------------------------

v.getConstValue()=null
com.sun.tools.javac.jvm.ClassWriter===>writeMemberAttrs(Symbol sym)
-------------------------------------------------------------------------
sym=this$0
sym.type=test.jvm.Test
sym.erasure(types)=test.jvm.Test
sym.type.getThrownTypes()=
com.sun.tools.javac.jvm.ClassWriter===>writeFlagAttrs(long flags)
-------------------------------------------------------------------------
flags=final synthetic 
acount=0
com.sun.tools.javac.jvm.ClassWriter===>writeFlagAttrs(long flags)  END
-------------------------------------------------------------------------

flags=final synthetic 
com.sun.tools.javac.jvm.ClassWriter===>writeJavaAnnotations(1)
-------------------------------------------------------------------------
attrs.isEmpty()=true
com.sun.tools.javac.jvm.ClassWriter===>writeJavaAnnotations(1)  END
-------------------------------------------------------------------------

acount=0
com.sun.tools.javac.jvm.ClassWriter===>writeMemberAttrs(Symbol sym)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writeField(VarSymbol v)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writeFields(Scope.Entry e)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writeMethods(1)
-------------------------------------------------------------------------
methods=MemberClass()
com.sun.tools.javac.jvm.ClassWriter===>writeMethod(1)
-------------------------------------------------------------------------
m=MemberClass()
m.flags()=acyclic generatedconstr 
com.sun.tools.javac.jvm.ClassWriter===>adjustFlags(1)
-------------------------------------------------------------------------
flags =acyclic generatedconstr 
result=acyclic 
com.sun.tools.javac.jvm.ClassWriter===>adjustFlags(1)  END
-------------------------------------------------------------------------

flags=acyclic 
com.sun.tools.javac.jvm.ClassWriter===>typeSig(Type type)
-------------------------------------------------------------------------
com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)
-------------------------------------------------------------------------
type=Method(test.jvm.Test)void type.tag=METHOD
com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)
-------------------------------------------------------------------------
type=test.jvm.Test type.tag=CLASS
com.sun.tools.javac.jvm.ClassWriter===>assembleClassSig(Type type)
-------------------------------------------------------------------------
type=test.jvm.Test type.tag=CLASS
com.sun.tools.javac.jvm.ClassWriter===>enterInner(1)
-------------------------------------------------------------------------
c=test.jvm.Test
innerClasses前=null
innerClassesQueue前=null

c.type=test.jvm.Test<A {bound=Number},B {bound=Number},C {bound=Object}>  c.type.tag=CLASS
pool=com.sun.tools.javac.jvm.Pool@12f0999
c.owner.kind=PCK 
innerClasses后=null
innerClassesQueue后=null
com.sun.tools.javac.jvm.ClassWriter===>enterInner(1)  END
-------------------------------------------------------------------------

outer=<none> outer.tag=NONE
outer.allparams()=
c.flatname=test.jvm.Test
ct.getTypeArguments()=
com.sun.tools.javac.jvm.ClassWriter===>assembleClassSig(Type type)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)
-------------------------------------------------------------------------
type=void type.tag=VOID
com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)  END
-------------------------------------------------------------------------

return typeName=(Ltest/jvm/Test;)V
com.sun.tools.javac.jvm.ClassWriter===>typeSig(Type type)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writeAttr(Name attrName)
-------------------------------------------------------------------------
attrName=Code
alenIdx=34
com.sun.tools.javac.jvm.ClassWriter===>writeAttr(Name attrName)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writeCode((1)
-------------------------------------------------------------------------
com.sun.tools.javac.jvm.ClassWriter===>writeAttr(Name attrName)
-------------------------------------------------------------------------
attrName=LineNumberTable
alenIdx=62
com.sun.tools.javac.jvm.ClassWriter===>writeAttr(Name attrName)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>endAttr(int index)
-------------------------------------------------------------------------
attribute.index =58
attribute.length=6
com.sun.tools.javac.jvm.ClassWriter===>endAttr(int index)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writeAttr(Name attrName)
-------------------------------------------------------------------------
attrName=CharacterRangeTable
alenIdx=74
com.sun.tools.javac.jvm.ClassWriter===>writeAttr(Name attrName)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>endAttr(int index)
-------------------------------------------------------------------------
attribute.index =70
attribute.length=2
com.sun.tools.javac.jvm.ClassWriter===>endAttr(int index)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writeCode((1)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>endAttr(int index)
-------------------------------------------------------------------------
attribute.index =30
attribute.length=42
com.sun.tools.javac.jvm.ClassWriter===>endAttr(int index)  END
-------------------------------------------------------------------------

thrown=
m.defaultValue=null
com.sun.tools.javac.jvm.ClassWriter===>writeMemberAttrs(Symbol sym)
-------------------------------------------------------------------------
sym=MemberClass()
sym.type=Method()void
sym.erasure(types)=Method()void
sym.type.getThrownTypes()=
com.sun.tools.javac.jvm.ClassWriter===>writeFlagAttrs(long flags)
-------------------------------------------------------------------------
flags=acyclic generatedconstr 
acount=0
com.sun.tools.javac.jvm.ClassWriter===>writeFlagAttrs(long flags)  END
-------------------------------------------------------------------------

flags=acyclic generatedconstr 
com.sun.tools.javac.jvm.ClassWriter===>writeJavaAnnotations(1)
-------------------------------------------------------------------------
attrs.isEmpty()=true
com.sun.tools.javac.jvm.ClassWriter===>writeJavaAnnotations(1)  END
-------------------------------------------------------------------------

acount=0
com.sun.tools.javac.jvm.ClassWriter===>writeMemberAttrs(Symbol sym)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writeParameterAttrs(1)
-------------------------------------------------------------------------
m=MemberClass()
hasVisible=false
attrCount=0
hasInvisible=false
attrCount=0
com.sun.tools.javac.jvm.ClassWriter===>writeParameterAttrs(1)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writeMethod(1)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writeMethods(1)  END
-------------------------------------------------------------------------

acountIdx=78
sigReq=true
sigbuf.toName(names)前=
com.sun.tools.javac.jvm.ClassWriter===>writeAttr(Name attrName)
-------------------------------------------------------------------------
attrName=Signature
alenIdx=84
com.sun.tools.javac.jvm.ClassWriter===>writeAttr(Name attrName)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>assembleParamsSig(1)
-------------------------------------------------------------------------
typarams=A {bound=Number},B {bound=Number},C {bound=Object}
sigbuf前=
com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)
-------------------------------------------------------------------------
type=java.lang.Number type.tag=CLASS
com.sun.tools.javac.jvm.ClassWriter===>assembleClassSig(Type type)
-------------------------------------------------------------------------
type=java.lang.Number type.tag=CLASS
com.sun.tools.javac.jvm.ClassWriter===>enterInner(1)
-------------------------------------------------------------------------
c=java.lang.Number
innerClasses前=null
innerClassesQueue前=null

c.type=java.lang.Number  c.type.tag=CLASS
pool=com.sun.tools.javac.jvm.Pool@12f0999
c.owner.kind=PCK 
innerClasses后=null
innerClassesQueue后=null
com.sun.tools.javac.jvm.ClassWriter===>enterInner(1)  END
-------------------------------------------------------------------------

outer=<none> outer.tag=NONE
outer.allparams()=
c.flatname=java.lang.Number
ct.getTypeArguments()=
com.sun.tools.javac.jvm.ClassWriter===>assembleClassSig(Type type)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)
-------------------------------------------------------------------------
type=java.lang.Number type.tag=CLASS
com.sun.tools.javac.jvm.ClassWriter===>assembleClassSig(Type type)
-------------------------------------------------------------------------
type=java.lang.Number type.tag=CLASS
com.sun.tools.javac.jvm.ClassWriter===>enterInner(1)
-------------------------------------------------------------------------
c=java.lang.Number
innerClasses前=null
innerClassesQueue前=null

c.type=java.lang.Number  c.type.tag=CLASS
pool=com.sun.tools.javac.jvm.Pool@12f0999
c.owner.kind=PCK 
innerClasses后=null
innerClassesQueue后=null
com.sun.tools.javac.jvm.ClassWriter===>enterInner(1)  END
-------------------------------------------------------------------------

outer=<none> outer.tag=NONE
outer.allparams()=
c.flatname=java.lang.Number
ct.getTypeArguments()=
com.sun.tools.javac.jvm.ClassWriter===>assembleClassSig(Type type)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)
-------------------------------------------------------------------------
type=java.lang.Object type.tag=CLASS
com.sun.tools.javac.jvm.ClassWriter===>assembleClassSig(Type type)
-------------------------------------------------------------------------
type=java.lang.Object type.tag=CLASS
com.sun.tools.javac.jvm.ClassWriter===>enterInner(1)
-------------------------------------------------------------------------
c=java.lang.Object
innerClasses前=null
innerClassesQueue前=null

c.type=java.lang.Object  c.type.tag=CLASS
pool=com.sun.tools.javac.jvm.Pool@12f0999
c.owner.kind=PCK 
innerClasses后=null
innerClassesQueue后=null
com.sun.tools.javac.jvm.ClassWriter===>enterInner(1)  END
-------------------------------------------------------------------------

outer=<none> outer.tag=NONE
outer.allparams()=
c.flatname=java.lang.Object
ct.getTypeArguments()=
com.sun.tools.javac.jvm.ClassWriter===>assembleClassSig(Type type)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)  END
-------------------------------------------------------------------------

sigbuf后=<A:Ljava/lang/Number;B:Ljava/lang/Number;C:Ljava/lang/Object;>
com.sun.tools.javac.jvm.ClassWriter===>assembleParamsSig(1)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)
-------------------------------------------------------------------------
type=java.lang.Object type.tag=CLASS
com.sun.tools.javac.jvm.ClassWriter===>assembleClassSig(Type type)
-------------------------------------------------------------------------
type=java.lang.Object type.tag=CLASS
com.sun.tools.javac.jvm.ClassWriter===>enterInner(1)
-------------------------------------------------------------------------
c=java.lang.Object
innerClasses前=null
innerClassesQueue前=null

c.type=java.lang.Object  c.type.tag=CLASS
pool=com.sun.tools.javac.jvm.Pool@12f0999
c.owner.kind=PCK 
innerClasses后=null
innerClassesQueue后=null
com.sun.tools.javac.jvm.ClassWriter===>enterInner(1)  END
-------------------------------------------------------------------------

outer=<none> outer.tag=NONE
outer.allparams()=
c.flatname=java.lang.Object
ct.getTypeArguments()=
com.sun.tools.javac.jvm.ClassWriter===>assembleClassSig(Type type)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)  END
-------------------------------------------------------------------------

sigbuf.toName(names)后=<A:Ljava/lang/Number;B:Ljava/lang/Number;C:Ljava/lang/Object;>Ljava/lang/Object;
com.sun.tools.javac.jvm.ClassWriter===>endAttr(int index)
-------------------------------------------------------------------------
attribute.index =80
attribute.length=2
com.sun.tools.javac.jvm.ClassWriter===>endAttr(int index)  END
-------------------------------------------------------------------------

c.sourcefile=test\jvm\ClassWriterTest.java
emitSourceFile=true

com.sun.tools.javac.jvm.ClassWriter===>writeAttr(Name attrName)
-------------------------------------------------------------------------
attrName=SourceFile
alenIdx=92
com.sun.tools.javac.jvm.ClassWriter===>writeAttr(Name attrName)  END
-------------------------------------------------------------------------

filename=test\jvm\ClassWriterTest.java
sepIdx=8
slashIdx=-1
sepIdx=8
filename=ClassWriterTest.java
com.sun.tools.javac.jvm.ClassWriter===>endAttr(int index)
-------------------------------------------------------------------------
attribute.index =88
attribute.length=2
com.sun.tools.javac.jvm.ClassWriter===>endAttr(int index)  END
-------------------------------------------------------------------------

genCrt=true
com.sun.tools.javac.jvm.ClassWriter===>writeAttr(Name attrName)
-------------------------------------------------------------------------
attrName=SourceID
alenIdx=100
com.sun.tools.javac.jvm.ClassWriter===>writeAttr(Name attrName)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>endAttr(int index)
-------------------------------------------------------------------------
attribute.index =96
attribute.length=2
com.sun.tools.javac.jvm.ClassWriter===>endAttr(int index)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writeAttr(Name attrName)
-------------------------------------------------------------------------
attrName=CompilationID
alenIdx=108
com.sun.tools.javac.jvm.ClassWriter===>writeAttr(Name attrName)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>endAttr(int index)
-------------------------------------------------------------------------
attribute.index =104
attribute.length=2
com.sun.tools.javac.jvm.ClassWriter===>endAttr(int index)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writeFlagAttrs(long flags)
-------------------------------------------------------------------------
flags=
acount=0
com.sun.tools.javac.jvm.ClassWriter===>writeFlagAttrs(long flags)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writeJavaAnnotations(1)
-------------------------------------------------------------------------
attrs.isEmpty()=true
com.sun.tools.javac.jvm.ClassWriter===>writeJavaAnnotations(1)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writeEnclosingMethodAttribute(1)
-------------------------------------------------------------------------
target.hasEnclosingMethodAttribute()=true
c.name=MemberClass
c.owner.kind=TYP 
com.sun.tools.javac.jvm.ClassWriter===>writeEnclosingMethodAttribute(1)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writePool(1)
-------------------------------------------------------------------------
com.sun.tools.javac.jvm.ClassWriter===>enterInner(1)
-------------------------------------------------------------------------
c=test.jvm.Test.MemberClass
innerClasses前=null
innerClassesQueue前=null

c.type=test.jvm.Test<A {bound=Number},B {bound=Number},C {bound=Object}>.MemberClass<A {bound=Number},B {bound=Number},C {bound=Object}>  c.type.tag=CLASS
pool=com.sun.tools.javac.jvm.Pool@12f0999
c.owner.kind=TYP 
新增内部类
com.sun.tools.javac.jvm.ClassWriter===>enterInner(1)
-------------------------------------------------------------------------
c=test.jvm.Test
innerClasses前=null
innerClassesQueue前=null

c.type=test.jvm.Test<A {bound=Number},B {bound=Number},C {bound=Object}>  c.type.tag=CLASS
pool=com.sun.tools.javac.jvm.Pool@12f0999
c.owner.kind=PCK 
innerClasses后=null
innerClassesQueue后=null
com.sun.tools.javac.jvm.ClassWriter===>enterInner(1)  END
-------------------------------------------------------------------------

innerClasses后=[test.jvm.Test.MemberClass]
innerClassesQueue后=test.jvm.Test.MemberClass
com.sun.tools.javac.jvm.ClassWriter===>enterInner(1)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>enterInner(1)
-------------------------------------------------------------------------
c=java.lang.Object
innerClasses前=[test.jvm.Test.MemberClass]
innerClassesQueue前=test.jvm.Test.MemberClass

c.type=java.lang.Object  c.type.tag=CLASS
pool=com.sun.tools.javac.jvm.Pool@12f0999
c.owner.kind=PCK 
innerClasses后=[test.jvm.Test.MemberClass]
innerClassesQueue后=test.jvm.Test.MemberClass
com.sun.tools.javac.jvm.ClassWriter===>enterInner(1)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>typeSig(Type type)
-------------------------------------------------------------------------
com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)
-------------------------------------------------------------------------
type=test.jvm.Test type.tag=CLASS
com.sun.tools.javac.jvm.ClassWriter===>assembleClassSig(Type type)
-------------------------------------------------------------------------
type=test.jvm.Test type.tag=CLASS
com.sun.tools.javac.jvm.ClassWriter===>enterInner(1)
-------------------------------------------------------------------------
c=test.jvm.Test
innerClasses前=[test.jvm.Test.MemberClass]
innerClassesQueue前=test.jvm.Test.MemberClass

c.type=test.jvm.Test<A {bound=Number},B {bound=Number},C {bound=Object}>  c.type.tag=CLASS
pool=com.sun.tools.javac.jvm.Pool@12f0999
c.owner.kind=PCK 
innerClasses后=[test.jvm.Test.MemberClass]
innerClassesQueue后=test.jvm.Test.MemberClass
com.sun.tools.javac.jvm.ClassWriter===>enterInner(1)  END
-------------------------------------------------------------------------

outer=<none> outer.tag=NONE
outer.allparams()=
c.flatname=test.jvm.Test
ct.getTypeArguments()=
com.sun.tools.javac.jvm.ClassWriter===>assembleClassSig(Type type)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)  END
-------------------------------------------------------------------------

return typeName=Ltest/jvm/Test;
com.sun.tools.javac.jvm.ClassWriter===>typeSig(Type type)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>typeSig(Type type)
-------------------------------------------------------------------------
com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)
-------------------------------------------------------------------------
type=Method()void type.tag=METHOD
com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)
-------------------------------------------------------------------------
type=void type.tag=VOID
com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)  END
-------------------------------------------------------------------------

return typeName=()V
com.sun.tools.javac.jvm.ClassWriter===>typeSig(Type type)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>enterInner(1)
-------------------------------------------------------------------------
c=test.jvm.Test
innerClasses前=[test.jvm.Test.MemberClass]
innerClassesQueue前=test.jvm.Test.MemberClass

c.type=test.jvm.Test<A {bound=Number},B {bound=Number},C {bound=Object}>  c.type.tag=CLASS
pool=com.sun.tools.javac.jvm.Pool@12f0999
c.owner.kind=PCK 
innerClasses后=[test.jvm.Test.MemberClass]
innerClassesQueue后=test.jvm.Test.MemberClass
com.sun.tools.javac.jvm.ClassWriter===>enterInner(1)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writePool(1)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writeInnerClasses()
-------------------------------------------------------------------------
com.sun.tools.javac.jvm.ClassWriter===>writeAttr(Name attrName)
-------------------------------------------------------------------------
attrName=InnerClasses
alenIdx=116
com.sun.tools.javac.jvm.ClassWriter===>writeAttr(Name attrName)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>adjustFlags(1)
-------------------------------------------------------------------------
flags =
result=
com.sun.tools.javac.jvm.ClassWriter===>adjustFlags(1)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>endAttr(int index)
-------------------------------------------------------------------------
attribute.index =112
attribute.length=10
com.sun.tools.javac.jvm.ClassWriter===>endAttr(int index)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writeInnerClasses()  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writeClassFile(OutputStream out, ClassSymbol c)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writeClass(ClassSymbol c)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writeClass(ClassSymbol c)
-------------------------------------------------------------------------
c=LocalClassInInitBlock
c.sourcefile=test\jvm\ClassWriterTest.java
outFile=classes\test\jvm\Test$1LocalClassInInitBlock.class
com.sun.tools.javac.jvm.ClassWriter===>writeClassFile(OutputStream out, ClassSymbol c)
-------------------------------------------------------------------------
supertype=java.lang.Object  supertype.tag=CLASS
interfaces=
typarams=
com.sun.tools.javac.jvm.ClassWriter===>adjustFlags(1)
-------------------------------------------------------------------------
flags =
result=
com.sun.tools.javac.jvm.ClassWriter===>adjustFlags(1)  END
-------------------------------------------------------------------------

flagNames= SUPER
flags =synchronized 
c.members()=Scope[(entries=2 nelems=2 owner=LocalClassInInitBlock)this$0, <init>()]
fieldsCount =1
methodsCount=1
com.sun.tools.javac.jvm.ClassWriter===>writeFields(Scope.Entry e)
-------------------------------------------------------------------------
vars=this$0
com.sun.tools.javac.jvm.ClassWriter===>writeField(VarSymbol v)
-------------------------------------------------------------------------
v=this$0
v.flags()=final synthetic 
com.sun.tools.javac.jvm.ClassWriter===>adjustFlags(1)
-------------------------------------------------------------------------
flags =final synthetic 
result=final synthetic 
com.sun.tools.javac.jvm.ClassWriter===>adjustFlags(1)  END
-------------------------------------------------------------------------

flags=final synthetic 
com.sun.tools.javac.jvm.ClassWriter===>typeSig(Type type)
-------------------------------------------------------------------------
com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)
-------------------------------------------------------------------------
type=test.jvm.Test type.tag=CLASS
com.sun.tools.javac.jvm.ClassWriter===>assembleClassSig(Type type)
-------------------------------------------------------------------------
type=test.jvm.Test type.tag=CLASS
com.sun.tools.javac.jvm.ClassWriter===>enterInner(1)
-------------------------------------------------------------------------
c=test.jvm.Test
innerClasses前=null
innerClassesQueue前=null

c.type=test.jvm.Test<A {bound=Number},B {bound=Number},C {bound=Object}>  c.type.tag=CLASS
pool=com.sun.tools.javac.jvm.Pool@12f0999
c.owner.kind=PCK 
innerClasses后=null
innerClassesQueue后=null
com.sun.tools.javac.jvm.ClassWriter===>enterInner(1)  END
-------------------------------------------------------------------------

outer=<none> outer.tag=NONE
outer.allparams()=
c.flatname=test.jvm.Test
ct.getTypeArguments()=
com.sun.tools.javac.jvm.ClassWriter===>assembleClassSig(Type type)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)  END
-------------------------------------------------------------------------

return typeName=Ltest/jvm/Test;
com.sun.tools.javac.jvm.ClassWriter===>typeSig(Type type)  END
-------------------------------------------------------------------------

v.getConstValue()=null
com.sun.tools.javac.jvm.ClassWriter===>writeMemberAttrs(Symbol sym)
-------------------------------------------------------------------------
sym=this$0
sym.type=test.jvm.Test
sym.erasure(types)=test.jvm.Test
sym.type.getThrownTypes()=
com.sun.tools.javac.jvm.ClassWriter===>writeFlagAttrs(long flags)
-------------------------------------------------------------------------
flags=final synthetic 
acount=0
com.sun.tools.javac.jvm.ClassWriter===>writeFlagAttrs(long flags)  END
-------------------------------------------------------------------------

flags=final synthetic 
com.sun.tools.javac.jvm.ClassWriter===>writeJavaAnnotations(1)
-------------------------------------------------------------------------
attrs.isEmpty()=true
com.sun.tools.javac.jvm.ClassWriter===>writeJavaAnnotations(1)  END
-------------------------------------------------------------------------

acount=0
com.sun.tools.javac.jvm.ClassWriter===>writeMemberAttrs(Symbol sym)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writeField(VarSymbol v)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writeFields(Scope.Entry e)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writeMethods(1)
-------------------------------------------------------------------------
methods=LocalClassInInitBlock()
com.sun.tools.javac.jvm.ClassWriter===>writeMethod(1)
-------------------------------------------------------------------------
m=LocalClassInInitBlock()
m.flags()=acyclic generatedconstr 
com.sun.tools.javac.jvm.ClassWriter===>adjustFlags(1)
-------------------------------------------------------------------------
flags =acyclic generatedconstr 
result=acyclic 
com.sun.tools.javac.jvm.ClassWriter===>adjustFlags(1)  END
-------------------------------------------------------------------------

flags=acyclic 
com.sun.tools.javac.jvm.ClassWriter===>typeSig(Type type)
-------------------------------------------------------------------------
com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)
-------------------------------------------------------------------------
type=Method(test.jvm.Test)void type.tag=METHOD
com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)
-------------------------------------------------------------------------
type=test.jvm.Test type.tag=CLASS
com.sun.tools.javac.jvm.ClassWriter===>assembleClassSig(Type type)
-------------------------------------------------------------------------
type=test.jvm.Test type.tag=CLASS
com.sun.tools.javac.jvm.ClassWriter===>enterInner(1)
-------------------------------------------------------------------------
c=test.jvm.Test
innerClasses前=null
innerClassesQueue前=null

c.type=test.jvm.Test<A {bound=Number},B {bound=Number},C {bound=Object}>  c.type.tag=CLASS
pool=com.sun.tools.javac.jvm.Pool@12f0999
c.owner.kind=PCK 
innerClasses后=null
innerClassesQueue后=null
com.sun.tools.javac.jvm.ClassWriter===>enterInner(1)  END
-------------------------------------------------------------------------

outer=<none> outer.tag=NONE
outer.allparams()=
c.flatname=test.jvm.Test
ct.getTypeArguments()=
com.sun.tools.javac.jvm.ClassWriter===>assembleClassSig(Type type)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)
-------------------------------------------------------------------------
type=void type.tag=VOID
com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)  END
-------------------------------------------------------------------------

return typeName=(Ltest/jvm/Test;)V
com.sun.tools.javac.jvm.ClassWriter===>typeSig(Type type)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writeAttr(Name attrName)
-------------------------------------------------------------------------
attrName=Code
alenIdx=34
com.sun.tools.javac.jvm.ClassWriter===>writeAttr(Name attrName)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writeCode((1)
-------------------------------------------------------------------------
com.sun.tools.javac.jvm.ClassWriter===>writeAttr(Name attrName)
-------------------------------------------------------------------------
attrName=LineNumberTable
alenIdx=62
com.sun.tools.javac.jvm.ClassWriter===>writeAttr(Name attrName)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>endAttr(int index)
-------------------------------------------------------------------------
attribute.index =58
attribute.length=6
com.sun.tools.javac.jvm.ClassWriter===>endAttr(int index)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writeAttr(Name attrName)
-------------------------------------------------------------------------
attrName=CharacterRangeTable
alenIdx=74
com.sun.tools.javac.jvm.ClassWriter===>writeAttr(Name attrName)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>endAttr(int index)
-------------------------------------------------------------------------
attribute.index =70
attribute.length=2
com.sun.tools.javac.jvm.ClassWriter===>endAttr(int index)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writeCode((1)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>endAttr(int index)
-------------------------------------------------------------------------
attribute.index =30
attribute.length=42
com.sun.tools.javac.jvm.ClassWriter===>endAttr(int index)  END
-------------------------------------------------------------------------

thrown=
m.defaultValue=null
com.sun.tools.javac.jvm.ClassWriter===>writeMemberAttrs(Symbol sym)
-------------------------------------------------------------------------
sym=LocalClassInInitBlock()
sym.type=Method()void
sym.erasure(types)=Method()void
sym.type.getThrownTypes()=
com.sun.tools.javac.jvm.ClassWriter===>writeFlagAttrs(long flags)
-------------------------------------------------------------------------
flags=acyclic generatedconstr 
acount=0
com.sun.tools.javac.jvm.ClassWriter===>writeFlagAttrs(long flags)  END
-------------------------------------------------------------------------

flags=acyclic generatedconstr 
com.sun.tools.javac.jvm.ClassWriter===>writeJavaAnnotations(1)
-------------------------------------------------------------------------
attrs.isEmpty()=true
com.sun.tools.javac.jvm.ClassWriter===>writeJavaAnnotations(1)  END
-------------------------------------------------------------------------

acount=0
com.sun.tools.javac.jvm.ClassWriter===>writeMemberAttrs(Symbol sym)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writeParameterAttrs(1)
-------------------------------------------------------------------------
m=LocalClassInInitBlock()
hasVisible=false
attrCount=0
hasInvisible=false
attrCount=0
com.sun.tools.javac.jvm.ClassWriter===>writeParameterAttrs(1)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writeMethod(1)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writeMethods(1)  END
-------------------------------------------------------------------------

acountIdx=78
sigReq=false
c.sourcefile=test\jvm\ClassWriterTest.java
emitSourceFile=true

com.sun.tools.javac.jvm.ClassWriter===>writeAttr(Name attrName)
-------------------------------------------------------------------------
attrName=SourceFile
alenIdx=84
com.sun.tools.javac.jvm.ClassWriter===>writeAttr(Name attrName)  END
-------------------------------------------------------------------------

filename=test\jvm\ClassWriterTest.java
sepIdx=8
slashIdx=-1
sepIdx=8
filename=ClassWriterTest.java
com.sun.tools.javac.jvm.ClassWriter===>endAttr(int index)
-------------------------------------------------------------------------
attribute.index =80
attribute.length=2
com.sun.tools.javac.jvm.ClassWriter===>endAttr(int index)  END
-------------------------------------------------------------------------

genCrt=true
com.sun.tools.javac.jvm.ClassWriter===>writeAttr(Name attrName)
-------------------------------------------------------------------------
attrName=SourceID
alenIdx=92
com.sun.tools.javac.jvm.ClassWriter===>writeAttr(Name attrName)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>endAttr(int index)
-------------------------------------------------------------------------
attribute.index =88
attribute.length=2
com.sun.tools.javac.jvm.ClassWriter===>endAttr(int index)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writeAttr(Name attrName)
-------------------------------------------------------------------------
attrName=CompilationID
alenIdx=100
com.sun.tools.javac.jvm.ClassWriter===>writeAttr(Name attrName)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>endAttr(int index)
-------------------------------------------------------------------------
attribute.index =96
attribute.length=2
com.sun.tools.javac.jvm.ClassWriter===>endAttr(int index)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writeFlagAttrs(long flags)
-------------------------------------------------------------------------
flags=
acount=0
com.sun.tools.javac.jvm.ClassWriter===>writeFlagAttrs(long flags)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writeJavaAnnotations(1)
-------------------------------------------------------------------------
attrs.isEmpty()=true
com.sun.tools.javac.jvm.ClassWriter===>writeJavaAnnotations(1)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writeEnclosingMethodAttribute(1)
-------------------------------------------------------------------------
target.hasEnclosingMethodAttribute()=true
c.name=LocalClassInInitBlock
c.owner.kind=MTH 
com.sun.tools.javac.jvm.ClassWriter===>writeAttr(Name attrName)
-------------------------------------------------------------------------
attrName=EnclosingMethod
alenIdx=108
com.sun.tools.javac.jvm.ClassWriter===>writeAttr(Name attrName)  END
-------------------------------------------------------------------------


c.owner.type=null
enclClass=test.jvm.Test
enclMethod=null
com.sun.tools.javac.jvm.ClassWriter===>endAttr(int index)
-------------------------------------------------------------------------
attribute.index =104
attribute.length=4
com.sun.tools.javac.jvm.ClassWriter===>endAttr(int index)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writeEnclosingMethodAttribute(1)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writePool(1)
-------------------------------------------------------------------------
com.sun.tools.javac.jvm.ClassWriter===>enterInner(1)
-------------------------------------------------------------------------
c=LocalClassInInitBlock
innerClasses前=null
innerClassesQueue前=null

c.type=LocalClassInInitBlock  c.type.tag=CLASS
pool=com.sun.tools.javac.jvm.Pool@12f0999
c.owner.kind=MTH 
新增内部类
innerClasses后=[LocalClassInInitBlock]
innerClassesQueue后=LocalClassInInitBlock
com.sun.tools.javac.jvm.ClassWriter===>enterInner(1)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>enterInner(1)
-------------------------------------------------------------------------
c=java.lang.Object
innerClasses前=[LocalClassInInitBlock]
innerClassesQueue前=LocalClassInInitBlock

c.type=java.lang.Object  c.type.tag=CLASS
pool=com.sun.tools.javac.jvm.Pool@12f0999
c.owner.kind=PCK 
innerClasses后=[LocalClassInInitBlock]
innerClassesQueue后=LocalClassInInitBlock
com.sun.tools.javac.jvm.ClassWriter===>enterInner(1)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>enterInner(1)
-------------------------------------------------------------------------
c=test.jvm.Test
innerClasses前=[LocalClassInInitBlock]
innerClassesQueue前=LocalClassInInitBlock

c.type=test.jvm.Test<A {bound=Number},B {bound=Number},C {bound=Object}>  c.type.tag=CLASS
pool=com.sun.tools.javac.jvm.Pool@12f0999
c.owner.kind=PCK 
innerClasses后=[LocalClassInInitBlock]
innerClassesQueue后=LocalClassInInitBlock
com.sun.tools.javac.jvm.ClassWriter===>enterInner(1)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>typeSig(Type type)
-------------------------------------------------------------------------
com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)
-------------------------------------------------------------------------
type=test.jvm.Test type.tag=CLASS
com.sun.tools.javac.jvm.ClassWriter===>assembleClassSig(Type type)
-------------------------------------------------------------------------
type=test.jvm.Test type.tag=CLASS
com.sun.tools.javac.jvm.ClassWriter===>enterInner(1)
-------------------------------------------------------------------------
c=test.jvm.Test
innerClasses前=[LocalClassInInitBlock]
innerClassesQueue前=LocalClassInInitBlock

c.type=test.jvm.Test<A {bound=Number},B {bound=Number},C {bound=Object}>  c.type.tag=CLASS
pool=com.sun.tools.javac.jvm.Pool@12f0999
c.owner.kind=PCK 
innerClasses后=[LocalClassInInitBlock]
innerClassesQueue后=LocalClassInInitBlock
com.sun.tools.javac.jvm.ClassWriter===>enterInner(1)  END
-------------------------------------------------------------------------

outer=<none> outer.tag=NONE
outer.allparams()=
c.flatname=test.jvm.Test
ct.getTypeArguments()=
com.sun.tools.javac.jvm.ClassWriter===>assembleClassSig(Type type)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)  END
-------------------------------------------------------------------------

return typeName=Ltest/jvm/Test;
com.sun.tools.javac.jvm.ClassWriter===>typeSig(Type type)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>typeSig(Type type)
-------------------------------------------------------------------------
com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)
-------------------------------------------------------------------------
type=Method()void type.tag=METHOD
com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)
-------------------------------------------------------------------------
type=void type.tag=VOID
com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)  END
-------------------------------------------------------------------------

return typeName=()V
com.sun.tools.javac.jvm.ClassWriter===>typeSig(Type type)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writePool(1)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writeInnerClasses()
-------------------------------------------------------------------------
com.sun.tools.javac.jvm.ClassWriter===>writeAttr(Name attrName)
-------------------------------------------------------------------------
attrName=InnerClasses
alenIdx=118
com.sun.tools.javac.jvm.ClassWriter===>writeAttr(Name attrName)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>adjustFlags(1)
-------------------------------------------------------------------------
flags =
result=
com.sun.tools.javac.jvm.ClassWriter===>adjustFlags(1)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>endAttr(int index)
-------------------------------------------------------------------------
attribute.index =114
attribute.length=10
com.sun.tools.javac.jvm.ClassWriter===>endAttr(int index)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writeInnerClasses()  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writeClassFile(OutputStream out, ClassSymbol c)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writeClass(ClassSymbol c)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writeClass(ClassSymbol c)
-------------------------------------------------------------------------
c=test.jvm.Test
c.sourcefile=test\jvm\ClassWriterTest.java
outFile=classes\test\jvm\Test.class
com.sun.tools.javac.jvm.ClassWriter===>writeClassFile(OutputStream out, ClassSymbol c)
-------------------------------------------------------------------------
supertype=java.lang.Object  supertype.tag=CLASS
interfaces=
typarams=A {bound=Number},B {bound=Number},C {bound=Object}
com.sun.tools.javac.jvm.ClassWriter===>adjustFlags(1)
-------------------------------------------------------------------------
flags =acyclic 
result=acyclic 
com.sun.tools.javac.jvm.ClassWriter===>adjustFlags(1)  END
-------------------------------------------------------------------------

flagNames= SUPER
flags =synchronized 
c.members()=Scope[(entries=3 nelems=3 owner=Test)test, <init>(), MemberClass]
com.sun.tools.javac.jvm.ClassWriter===>enterInner(1)
-------------------------------------------------------------------------
c=test.jvm.Test.MemberClass
innerClasses前=null
innerClassesQueue前=null

c.type=test.jvm.Test<A {bound=Number},B {bound=Number},C {bound=Object}>.MemberClass<A {bound=Number},B {bound=Number},C {bound=Object}>  c.type.tag=CLASS
pool=com.sun.tools.javac.jvm.Pool@12f0999
c.owner.kind=TYP 
新增内部类
com.sun.tools.javac.jvm.ClassWriter===>enterInner(1)
-------------------------------------------------------------------------
c=test.jvm.Test
innerClasses前=null
innerClassesQueue前=null

c.type=test.jvm.Test<A {bound=Number},B {bound=Number},C {bound=Object}>  c.type.tag=CLASS
pool=com.sun.tools.javac.jvm.Pool@12f0999
c.owner.kind=PCK 
innerClasses后=null
innerClassesQueue后=null
com.sun.tools.javac.jvm.ClassWriter===>enterInner(1)  END
-------------------------------------------------------------------------

innerClasses后=[test.jvm.Test.MemberClass]
innerClassesQueue后=test.jvm.Test.MemberClass
com.sun.tools.javac.jvm.ClassWriter===>enterInner(1)  END
-------------------------------------------------------------------------

fieldsCount =1
methodsCount=1
com.sun.tools.javac.jvm.ClassWriter===>writeFields(Scope.Entry e)
-------------------------------------------------------------------------
vars=test
com.sun.tools.javac.jvm.ClassWriter===>writeField(VarSymbol v)
-------------------------------------------------------------------------
v=test
v.flags()=
com.sun.tools.javac.jvm.ClassWriter===>adjustFlags(1)
-------------------------------------------------------------------------
flags =
result=
com.sun.tools.javac.jvm.ClassWriter===>adjustFlags(1)  END
-------------------------------------------------------------------------

flags=
com.sun.tools.javac.jvm.ClassWriter===>typeSig(Type type)
-------------------------------------------------------------------------
com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)
-------------------------------------------------------------------------
type=test.jvm.Test type.tag=CLASS
com.sun.tools.javac.jvm.ClassWriter===>assembleClassSig(Type type)
-------------------------------------------------------------------------
type=test.jvm.Test type.tag=CLASS
com.sun.tools.javac.jvm.ClassWriter===>enterInner(1)
-------------------------------------------------------------------------
c=test.jvm.Test
innerClasses前=[test.jvm.Test.MemberClass]
innerClassesQueue前=test.jvm.Test.MemberClass

c.type=test.jvm.Test<A {bound=Number},B {bound=Number},C {bound=Object}>  c.type.tag=CLASS
pool=com.sun.tools.javac.jvm.Pool@12f0999
c.owner.kind=PCK 
innerClasses后=[test.jvm.Test.MemberClass]
innerClassesQueue后=test.jvm.Test.MemberClass
com.sun.tools.javac.jvm.ClassWriter===>enterInner(1)  END
-------------------------------------------------------------------------

outer=<none> outer.tag=NONE
outer.allparams()=
c.flatname=test.jvm.Test
ct.getTypeArguments()=
com.sun.tools.javac.jvm.ClassWriter===>assembleClassSig(Type type)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)  END
-------------------------------------------------------------------------

return typeName=Ltest/jvm/Test;
com.sun.tools.javac.jvm.ClassWriter===>typeSig(Type type)  END
-------------------------------------------------------------------------

v.getConstValue()=null
com.sun.tools.javac.jvm.ClassWriter===>writeMemberAttrs(Symbol sym)
-------------------------------------------------------------------------
sym=test
sym.type=test.jvm.Test<? super java.lang.Integer,? extends java.lang.Number,?>
sym.erasure(types)=test.jvm.Test
sym.type.getThrownTypes()=
com.sun.tools.javac.jvm.ClassWriter===>writeFlagAttrs(long flags)
-------------------------------------------------------------------------
flags=
acount=0
com.sun.tools.javac.jvm.ClassWriter===>writeFlagAttrs(long flags)  END
-------------------------------------------------------------------------

flags=
com.sun.tools.javac.jvm.ClassWriter===>writeAttr(Name attrName)
-------------------------------------------------------------------------
attrName=Signature
alenIdx=24
com.sun.tools.javac.jvm.ClassWriter===>writeAttr(Name attrName)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>typeSig(Type type)
-------------------------------------------------------------------------
com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)
-------------------------------------------------------------------------
type=test.jvm.Test<? super java.lang.Integer,? extends java.lang.Number,?> type.tag=CLASS
com.sun.tools.javac.jvm.ClassWriter===>assembleClassSig(Type type)
-------------------------------------------------------------------------
type=test.jvm.Test<? super java.lang.Integer,? extends java.lang.Number,?> type.tag=CLASS
com.sun.tools.javac.jvm.ClassWriter===>enterInner(1)
-------------------------------------------------------------------------
c=test.jvm.Test
innerClasses前=[test.jvm.Test.MemberClass]
innerClassesQueue前=test.jvm.Test.MemberClass

c.type=test.jvm.Test<A {bound=Number},B {bound=Number},C {bound=Object}>  c.type.tag=CLASS
pool=com.sun.tools.javac.jvm.Pool@12f0999
c.owner.kind=PCK 
innerClasses后=[test.jvm.Test.MemberClass]
innerClassesQueue后=test.jvm.Test.MemberClass
com.sun.tools.javac.jvm.ClassWriter===>enterInner(1)  END
-------------------------------------------------------------------------

outer=<none> outer.tag=NONE
outer.allparams()=
c.flatname=test.jvm.Test
ct.getTypeArguments()=? super java.lang.Integer,? extends java.lang.Number,?
com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)
-------------------------------------------------------------------------
type=? super java.lang.Integer type.tag=WILDCARD
com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)
-------------------------------------------------------------------------
type=java.lang.Integer type.tag=CLASS
com.sun.tools.javac.jvm.ClassWriter===>assembleClassSig(Type type)
-------------------------------------------------------------------------
type=java.lang.Integer type.tag=CLASS
com.sun.tools.javac.jvm.ClassWriter===>enterInner(1)
-------------------------------------------------------------------------
c=java.lang.Integer
innerClasses前=[test.jvm.Test.MemberClass]
innerClassesQueue前=test.jvm.Test.MemberClass

c.type=java.lang.Integer  c.type.tag=CLASS
pool=com.sun.tools.javac.jvm.Pool@12f0999
c.owner.kind=PCK 
innerClasses后=[test.jvm.Test.MemberClass]
innerClassesQueue后=test.jvm.Test.MemberClass
com.sun.tools.javac.jvm.ClassWriter===>enterInner(1)  END
-------------------------------------------------------------------------

outer=<none> outer.tag=NONE
outer.allparams()=
c.flatname=java.lang.Integer
ct.getTypeArguments()=
com.sun.tools.javac.jvm.ClassWriter===>assembleClassSig(Type type)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)
-------------------------------------------------------------------------
type=? extends java.lang.Number type.tag=WILDCARD
com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)
-------------------------------------------------------------------------
type=java.lang.Number type.tag=CLASS
com.sun.tools.javac.jvm.ClassWriter===>assembleClassSig(Type type)
-------------------------------------------------------------------------
type=java.lang.Number type.tag=CLASS
com.sun.tools.javac.jvm.ClassWriter===>enterInner(1)
-------------------------------------------------------------------------
c=java.lang.Number
innerClasses前=[test.jvm.Test.MemberClass]
innerClassesQueue前=test.jvm.Test.MemberClass

c.type=java.lang.Number  c.type.tag=CLASS
pool=com.sun.tools.javac.jvm.Pool@12f0999
c.owner.kind=PCK 
innerClasses后=[test.jvm.Test.MemberClass]
innerClassesQueue后=test.jvm.Test.MemberClass
com.sun.tools.javac.jvm.ClassWriter===>enterInner(1)  END
-------------------------------------------------------------------------

outer=<none> outer.tag=NONE
outer.allparams()=
c.flatname=java.lang.Number
ct.getTypeArguments()=
com.sun.tools.javac.jvm.ClassWriter===>assembleClassSig(Type type)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)
-------------------------------------------------------------------------
type=? type.tag=WILDCARD
com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>assembleClassSig(Type type)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)  END
-------------------------------------------------------------------------

return typeName=Ltest/jvm/Test<-Ljava/lang/Integer;+Ljava/lang/Number;*>;
com.sun.tools.javac.jvm.ClassWriter===>typeSig(Type type)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>endAttr(int index)
-------------------------------------------------------------------------
attribute.index =20
attribute.length=2
com.sun.tools.javac.jvm.ClassWriter===>endAttr(int index)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writeJavaAnnotations(1)
-------------------------------------------------------------------------
attrs.isEmpty()=true
com.sun.tools.javac.jvm.ClassWriter===>writeJavaAnnotations(1)  END
-------------------------------------------------------------------------

acount=1
com.sun.tools.javac.jvm.ClassWriter===>writeMemberAttrs(Symbol sym)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writeField(VarSymbol v)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writeFields(Scope.Entry e)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writeMethods(1)
-------------------------------------------------------------------------
methods=Test()
com.sun.tools.javac.jvm.ClassWriter===>writeMethod(1)
-------------------------------------------------------------------------
m=Test()
m.flags()=acyclic generatedconstr 
com.sun.tools.javac.jvm.ClassWriter===>adjustFlags(1)
-------------------------------------------------------------------------
flags =acyclic generatedconstr 
result=acyclic 
com.sun.tools.javac.jvm.ClassWriter===>adjustFlags(1)  END
-------------------------------------------------------------------------

flags=acyclic 
com.sun.tools.javac.jvm.ClassWriter===>typeSig(Type type)
-------------------------------------------------------------------------
com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)
-------------------------------------------------------------------------
type=Method()void type.tag=METHOD
com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)
-------------------------------------------------------------------------
type=void type.tag=VOID
com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)  END
-------------------------------------------------------------------------

return typeName=()V
com.sun.tools.javac.jvm.ClassWriter===>typeSig(Type type)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writeAttr(Name attrName)
-------------------------------------------------------------------------
attrName=Code
alenIdx=42
com.sun.tools.javac.jvm.ClassWriter===>writeAttr(Name attrName)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writeCode((1)
-------------------------------------------------------------------------
com.sun.tools.javac.jvm.ClassWriter===>writeAttr(Name attrName)
-------------------------------------------------------------------------
attrName=LineNumberTable
alenIdx=65
com.sun.tools.javac.jvm.ClassWriter===>writeAttr(Name attrName)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>endAttr(int index)
-------------------------------------------------------------------------
attribute.index =61
attribute.length=10
com.sun.tools.javac.jvm.ClassWriter===>endAttr(int index)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writeAttr(Name attrName)
-------------------------------------------------------------------------
attrName=CharacterRangeTable
alenIdx=81
com.sun.tools.javac.jvm.ClassWriter===>writeAttr(Name attrName)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>endAttr(int index)
-------------------------------------------------------------------------
attribute.index =77
attribute.length=16
com.sun.tools.javac.jvm.ClassWriter===>endAttr(int index)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writeCode((1)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>endAttr(int index)
-------------------------------------------------------------------------
attribute.index =38
attribute.length=55
com.sun.tools.javac.jvm.ClassWriter===>endAttr(int index)  END
-------------------------------------------------------------------------

thrown=
m.defaultValue=null
com.sun.tools.javac.jvm.ClassWriter===>writeMemberAttrs(Symbol sym)
-------------------------------------------------------------------------
sym=Test()
sym.type=Method()void
sym.erasure(types)=Method()void
sym.type.getThrownTypes()=
com.sun.tools.javac.jvm.ClassWriter===>writeFlagAttrs(long flags)
-------------------------------------------------------------------------
flags=acyclic generatedconstr 
acount=0
com.sun.tools.javac.jvm.ClassWriter===>writeFlagAttrs(long flags)  END
-------------------------------------------------------------------------

flags=acyclic generatedconstr 
com.sun.tools.javac.jvm.ClassWriter===>writeJavaAnnotations(1)
-------------------------------------------------------------------------
attrs.isEmpty()=true
com.sun.tools.javac.jvm.ClassWriter===>writeJavaAnnotations(1)  END
-------------------------------------------------------------------------

acount=0
com.sun.tools.javac.jvm.ClassWriter===>writeMemberAttrs(Symbol sym)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writeParameterAttrs(1)
-------------------------------------------------------------------------
m=Test()
hasVisible=false
attrCount=0
hasInvisible=false
attrCount=0
com.sun.tools.javac.jvm.ClassWriter===>writeParameterAttrs(1)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writeMethod(1)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writeMethods(1)  END
-------------------------------------------------------------------------

acountIdx=99
sigReq=true
sigbuf.toName(names)前=
com.sun.tools.javac.jvm.ClassWriter===>writeAttr(Name attrName)
-------------------------------------------------------------------------
attrName=Signature
alenIdx=105
com.sun.tools.javac.jvm.ClassWriter===>writeAttr(Name attrName)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>assembleParamsSig(1)
-------------------------------------------------------------------------
typarams=A {bound=Number},B {bound=Number},C {bound=Object}
sigbuf前=
com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)
-------------------------------------------------------------------------
type=java.lang.Number type.tag=CLASS
com.sun.tools.javac.jvm.ClassWriter===>assembleClassSig(Type type)
-------------------------------------------------------------------------
type=java.lang.Number type.tag=CLASS
com.sun.tools.javac.jvm.ClassWriter===>enterInner(1)
-------------------------------------------------------------------------
c=java.lang.Number
innerClasses前=[test.jvm.Test.MemberClass]
innerClassesQueue前=test.jvm.Test.MemberClass

c.type=java.lang.Number  c.type.tag=CLASS
pool=com.sun.tools.javac.jvm.Pool@12f0999
c.owner.kind=PCK 
innerClasses后=[test.jvm.Test.MemberClass]
innerClassesQueue后=test.jvm.Test.MemberClass
com.sun.tools.javac.jvm.ClassWriter===>enterInner(1)  END
-------------------------------------------------------------------------

outer=<none> outer.tag=NONE
outer.allparams()=
c.flatname=java.lang.Number
ct.getTypeArguments()=
com.sun.tools.javac.jvm.ClassWriter===>assembleClassSig(Type type)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)
-------------------------------------------------------------------------
type=java.lang.Number type.tag=CLASS
com.sun.tools.javac.jvm.ClassWriter===>assembleClassSig(Type type)
-------------------------------------------------------------------------
type=java.lang.Number type.tag=CLASS
com.sun.tools.javac.jvm.ClassWriter===>enterInner(1)
-------------------------------------------------------------------------
c=java.lang.Number
innerClasses前=[test.jvm.Test.MemberClass]
innerClassesQueue前=test.jvm.Test.MemberClass

c.type=java.lang.Number  c.type.tag=CLASS
pool=com.sun.tools.javac.jvm.Pool@12f0999
c.owner.kind=PCK 
innerClasses后=[test.jvm.Test.MemberClass]
innerClassesQueue后=test.jvm.Test.MemberClass
com.sun.tools.javac.jvm.ClassWriter===>enterInner(1)  END
-------------------------------------------------------------------------

outer=<none> outer.tag=NONE
outer.allparams()=
c.flatname=java.lang.Number
ct.getTypeArguments()=
com.sun.tools.javac.jvm.ClassWriter===>assembleClassSig(Type type)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)
-------------------------------------------------------------------------
type=java.lang.Object type.tag=CLASS
com.sun.tools.javac.jvm.ClassWriter===>assembleClassSig(Type type)
-------------------------------------------------------------------------
type=java.lang.Object type.tag=CLASS
com.sun.tools.javac.jvm.ClassWriter===>enterInner(1)
-------------------------------------------------------------------------
c=java.lang.Object
innerClasses前=[test.jvm.Test.MemberClass]
innerClassesQueue前=test.jvm.Test.MemberClass

c.type=java.lang.Object  c.type.tag=CLASS
pool=com.sun.tools.javac.jvm.Pool@12f0999
c.owner.kind=PCK 
innerClasses后=[test.jvm.Test.MemberClass]
innerClassesQueue后=test.jvm.Test.MemberClass
com.sun.tools.javac.jvm.ClassWriter===>enterInner(1)  END
-------------------------------------------------------------------------

outer=<none> outer.tag=NONE
outer.allparams()=
c.flatname=java.lang.Object
ct.getTypeArguments()=
com.sun.tools.javac.jvm.ClassWriter===>assembleClassSig(Type type)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)  END
-------------------------------------------------------------------------

sigbuf后=<A:Ljava/lang/Number;B:Ljava/lang/Number;C:Ljava/lang/Object;>
com.sun.tools.javac.jvm.ClassWriter===>assembleParamsSig(1)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)
-------------------------------------------------------------------------
type=java.lang.Object type.tag=CLASS
com.sun.tools.javac.jvm.ClassWriter===>assembleClassSig(Type type)
-------------------------------------------------------------------------
type=java.lang.Object type.tag=CLASS
com.sun.tools.javac.jvm.ClassWriter===>enterInner(1)
-------------------------------------------------------------------------
c=java.lang.Object
innerClasses前=[test.jvm.Test.MemberClass]
innerClassesQueue前=test.jvm.Test.MemberClass

c.type=java.lang.Object  c.type.tag=CLASS
pool=com.sun.tools.javac.jvm.Pool@12f0999
c.owner.kind=PCK 
innerClasses后=[test.jvm.Test.MemberClass]
innerClassesQueue后=test.jvm.Test.MemberClass
com.sun.tools.javac.jvm.ClassWriter===>enterInner(1)  END
-------------------------------------------------------------------------

outer=<none> outer.tag=NONE
outer.allparams()=
c.flatname=java.lang.Object
ct.getTypeArguments()=
com.sun.tools.javac.jvm.ClassWriter===>assembleClassSig(Type type)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)  END
-------------------------------------------------------------------------

sigbuf.toName(names)后=<A:Ljava/lang/Number;B:Ljava/lang/Number;C:Ljava/lang/Object;>Ljava/lang/Object;
com.sun.tools.javac.jvm.ClassWriter===>endAttr(int index)
-------------------------------------------------------------------------
attribute.index =101
attribute.length=2
com.sun.tools.javac.jvm.ClassWriter===>endAttr(int index)  END
-------------------------------------------------------------------------

c.sourcefile=test\jvm\ClassWriterTest.java
emitSourceFile=true

com.sun.tools.javac.jvm.ClassWriter===>writeAttr(Name attrName)
-------------------------------------------------------------------------
attrName=SourceFile
alenIdx=113
com.sun.tools.javac.jvm.ClassWriter===>writeAttr(Name attrName)  END
-------------------------------------------------------------------------

filename=test\jvm\ClassWriterTest.java
sepIdx=8
slashIdx=-1
sepIdx=8
filename=ClassWriterTest.java
com.sun.tools.javac.jvm.ClassWriter===>endAttr(int index)
-------------------------------------------------------------------------
attribute.index =109
attribute.length=2
com.sun.tools.javac.jvm.ClassWriter===>endAttr(int index)  END
-------------------------------------------------------------------------

genCrt=true
com.sun.tools.javac.jvm.ClassWriter===>writeAttr(Name attrName)
-------------------------------------------------------------------------
attrName=SourceID
alenIdx=121
com.sun.tools.javac.jvm.ClassWriter===>writeAttr(Name attrName)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>endAttr(int index)
-------------------------------------------------------------------------
attribute.index =117
attribute.length=2
com.sun.tools.javac.jvm.ClassWriter===>endAttr(int index)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writeAttr(Name attrName)
-------------------------------------------------------------------------
attrName=CompilationID
alenIdx=129
com.sun.tools.javac.jvm.ClassWriter===>writeAttr(Name attrName)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>endAttr(int index)
-------------------------------------------------------------------------
attribute.index =125
attribute.length=2
com.sun.tools.javac.jvm.ClassWriter===>endAttr(int index)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writeFlagAttrs(long flags)
-------------------------------------------------------------------------
flags=acyclic 
acount=0
com.sun.tools.javac.jvm.ClassWriter===>writeFlagAttrs(long flags)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writeJavaAnnotations(1)
-------------------------------------------------------------------------
attrs.isEmpty()=true
com.sun.tools.javac.jvm.ClassWriter===>writeJavaAnnotations(1)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writeEnclosingMethodAttribute(1)
-------------------------------------------------------------------------
target.hasEnclosingMethodAttribute()=true
c.name=Test
c.owner.kind=PCK 
com.sun.tools.javac.jvm.ClassWriter===>writeEnclosingMethodAttribute(1)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writePool(1)
-------------------------------------------------------------------------
com.sun.tools.javac.jvm.ClassWriter===>enterInner(1)
-------------------------------------------------------------------------
c=test.jvm.Test
innerClasses前=[test.jvm.Test.MemberClass]
innerClassesQueue前=test.jvm.Test.MemberClass

c.type=test.jvm.Test<A {bound=Number},B {bound=Number},C {bound=Object}>  c.type.tag=CLASS
pool=com.sun.tools.javac.jvm.Pool@12f0999
c.owner.kind=PCK 
innerClasses后=[test.jvm.Test.MemberClass]
innerClassesQueue后=test.jvm.Test.MemberClass
com.sun.tools.javac.jvm.ClassWriter===>enterInner(1)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>enterInner(1)
-------------------------------------------------------------------------
c=java.lang.Object
innerClasses前=[test.jvm.Test.MemberClass]
innerClassesQueue前=test.jvm.Test.MemberClass

c.type=java.lang.Object  c.type.tag=CLASS
pool=com.sun.tools.javac.jvm.Pool@12f0999
c.owner.kind=PCK 
innerClasses后=[test.jvm.Test.MemberClass]
innerClassesQueue后=test.jvm.Test.MemberClass
com.sun.tools.javac.jvm.ClassWriter===>enterInner(1)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>enterInner(1)
-------------------------------------------------------------------------
c=test.jvm.Test.MemberClass
innerClasses前=[test.jvm.Test.MemberClass]
innerClassesQueue前=test.jvm.Test.MemberClass

c.type=test.jvm.Test<A {bound=Number},B {bound=Number},C {bound=Object}>.MemberClass<A {bound=Number},B {bound=Number},C {bound=Object}>  c.type.tag=CLASS
pool=com.sun.tools.javac.jvm.Pool@12f0999
c.owner.kind=TYP 
innerClasses后=[test.jvm.Test.MemberClass]
innerClassesQueue后=test.jvm.Test.MemberClass
com.sun.tools.javac.jvm.ClassWriter===>enterInner(1)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>typeSig(Type type)
-------------------------------------------------------------------------
com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)
-------------------------------------------------------------------------
type=Method()void type.tag=METHOD
com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)
-------------------------------------------------------------------------
type=void type.tag=VOID
com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>assembleSig(Type type)  END
-------------------------------------------------------------------------

return typeName=()V
com.sun.tools.javac.jvm.ClassWriter===>typeSig(Type type)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writePool(1)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writeInnerClasses()
-------------------------------------------------------------------------
com.sun.tools.javac.jvm.ClassWriter===>writeAttr(Name attrName)
-------------------------------------------------------------------------
attrName=InnerClasses
alenIdx=137
com.sun.tools.javac.jvm.ClassWriter===>writeAttr(Name attrName)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>adjustFlags(1)
-------------------------------------------------------------------------
flags =
result=
com.sun.tools.javac.jvm.ClassWriter===>adjustFlags(1)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>endAttr(int index)
-------------------------------------------------------------------------
attribute.index =133
attribute.length=10
com.sun.tools.javac.jvm.ClassWriter===>endAttr(int index)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writeInnerClasses()  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writeClassFile(OutputStream out, ClassSymbol c)  END
-------------------------------------------------------------------------

com.sun.tools.javac.jvm.ClassWriter===>writeClass(ClassSymbol c)  END
-------------------------------------------------------------------------

