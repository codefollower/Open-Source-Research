/*
 * @(#)Symtab.java	1.68 07/03/21
 * 
 * Copyright (c) 2007 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *  
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *  
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *  
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *  
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package com.sun.tools.javac.code;

import java.util.*;

import com.sun.tools.javac.util.*;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.code.Symbol.*;
import com.sun.tools.javac.code.Type.*;
import com.sun.tools.javac.jvm.*;

import static com.sun.tools.javac.jvm.ByteCodes.*;
import static com.sun.tools.javac.code.Flags.*;

/** A class that defines all predefined constants and operators
 *  as well as special classes such as java.lang.Object, which need
 *  to be known to the compiler. All symbols are held in instance
 *  fields. This makes it possible to work in multiple concurrent
 *  projects, which might use different class files for library classes.
 *
 *  <p><b>This is NOT part of any API supported by Sun Microsystems.  If
 *  you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 */
@Version("@(#)Symtab.java	1.68 07/03/21")
public class Symtab {
	private static my.Debug DEBUG=new my.Debug(my.Debug.Symtab);//我加上的
	
    /** The context key for the symbol table. */
    protected static final Context.Key<Symtab> symtabKey =
	new Context.Key<Symtab>();

    /** Get the symbol table instance. */
    public static Symtab instance(Context context) {
	Symtab instance = context.get(symtabKey);
	if (instance == null)
	    instance = new Symtab(context);
	return instance;
    }

    /** Builtin types.
     */
	//总共8个基本类型
    public static final Type byteType = new Type(TypeTags.BYTE, null);       
    public static final Type charType = new Type(TypeTags.CHAR, null);       
    public static final Type shortType = new Type(TypeTags.SHORT, null);     
    public static final Type intType = new Type(TypeTags.INT, null);         
    public static final Type longType = new Type(TypeTags.LONG, null);       
    public static final Type floatType = new Type(TypeTags.FLOAT, null);     
    public static final Type doubleType = new Type(TypeTags.DOUBLE, null);   
    public static final Type booleanType = new Type(TypeTags.BOOLEAN, null); 
    //类全限定名称:com.sun.tools.javac.code.Type.BottomType
    public static final Type botType = new BottomType(); //代表null
    //类全限定名称:com.sun.tools.javac.code.Type.JCNoType
    public static final JCNoType voidType = new JCNoType(TypeTags.VOID);

    private final Name.Table names;
    private final ClassReader reader;

    /** A symbol for the root package.
     */
    public final PackageSymbol rootPackage;

    /** A symbol for the unnamed package.
     */
    public final PackageSymbol unnamedPackage;

    /** A symbol that stands for a missing symbol.
     */
    public final TypeSymbol noSymbol;

    /** The error symbol.
     */
    public final ClassSymbol errSymbol;

    /** An instance of the error type.
     */
    public final Type errType;

    /** A value for the unknown type. */
    public final Type unknownType;

    /** The builtin type of all arrays. */
    public final ClassSymbol arrayClass;
    public final MethodSymbol arrayCloneMethod;

    /** VGJ: The (singleton) type of all bound types. */
    public final ClassSymbol boundClass;

    /** The builtin type of all methods. */
    public final ClassSymbol methodClass;

    /** Predefined types.
     */
    public final Type objectType;
    public final Type classType;
    public final Type classLoaderType;
    public final Type stringType;
    public final Type stringBufferType;
    public final Type stringBuilderType;
    public final Type cloneableType;
    public final Type serializableType;
    public final Type throwableType;
    public final Type errorType;
    public final Type illegalArgumentExceptionType;
    public final Type exceptionType;
    public final Type runtimeExceptionType;
    public final Type classNotFoundExceptionType;
    public final Type noClassDefFoundErrorType;
    public final Type noSuchFieldErrorType;
    public final Type assertionErrorType;
    public final Type cloneNotSupportedExceptionType;
    public final Type annotationType;
    public final TypeSymbol enumSym;
    public final Type listType;
    public final Type collectionsType;
    public final Type comparableType;
    public final Type arraysType;
    public final Type iterableType;
    public final Type iteratorType;
    public final Type annotationTargetType;
    public final Type overrideType;
    public final Type retentionType;
    public final Type deprecatedType;
    public final Type suppressWarningsType;
    public final Type inheritedType;
    public final Type proprietaryType;//这个得注意一下

    /** The symbol representing the length field of an array.
     */
    public final VarSymbol lengthVar;

    /** The null check operator. */
    public final OperatorSymbol nullcheck;

    /** The symbol representing the final finalize method on enums */
    public final MethodSymbol enumFinalFinalize;

    /** The predefined type that belongs to a tag.
     */
    public final Type[] typeOfTag = new Type[TypeTags.TypeTagCount];

    /** The name of the class that belongs to a basix type tag.
     */
    public final Name[] boxedName = new Name[TypeTags.TypeTagCount];

    /** A hashtable containing the encountered top-level and member classes,
     *  indexed by flat names. The table does not contain local classes.
     *  It should be updated from the outside to reflect classes defined
     *  by compiled source files.
     */
    //虽然有final这个关键字使得classes总是指向固定的Map实例，但是这个Map实例的
    //内部所存放的内容是可变的。
    public final Map<Name, ClassSymbol> classes = new HashMap<Name, ClassSymbol>();

    /** A hashtable containing the encountered packages.
     *  the table should be updated from outside to reflect packages defined
     *  by compiled source files.
     */
    public final Map<Name, PackageSymbol> packages = new HashMap<Name, PackageSymbol>();

    public void initType(Type type, ClassSymbol c) {
	type.tsym = c;
	typeOfTag[type.tag] = type;
    }

    public void initType(Type type, String name) {
	initType(
	    type,
	    new ClassSymbol(
		PUBLIC, names.fromString(name), type, rootPackage));
    }

    public void initType(Type type, String name, String bname) {
	initType(type, name);
	boxedName[type.tag] = names.fromString("java.lang." + bname);
    }

    /** The class symbol that owns all predefined symbols.
     */
    public final ClassSymbol predefClass;

	//public static final ClassSymbol predefClass; //无法为最终变量 predefClass 指定值

	public static ClassSymbol MyPredefClass;

    /** Enter a constant into symbol table.
     *  @param name   The constant's name.
     *  @param type   The constant's type.
     */
    //这个方法没发现在哪里使用过
    private VarSymbol enterConstant(String name, Type type) {
        VarSymbol c = new VarSymbol(
	    PUBLIC | STATIC | FINAL,
	    names.fromString(name),
	    type,
	    predefClass);
	c.setData(type.constValue());
        predefClass.members().enter(c);
	return c;
    }

    /** Enter a binary operation into symbol table.
     *  @param name     The name of the operator.
     *  @param left     The type of the left operand.
     *  @param right    The type of the left operand.//就是right operand
     *  @param res      The operation's result type.
     *  @param opcode   The operation's bytecode instruction.
     */
    private void enterBinop(String name,
			    Type left, Type right, Type res,
			    int opcode) {
        predefClass.members().enter(
            new OperatorSymbol(
		names.fromString(name),
		new MethodType(List.of(left, right), res,
			       List.<Type>nil(), methodClass),
		opcode,
		predefClass));
    }

    /** Enter a binary operation, as above but with two opcodes,
     *  which get encoded as (opcode1 << ByteCodeTags.preShift) + opcode2.
     *  @param opcode1     First opcode.
     *  @param opcode2     Second opcode.
     */
    private void enterBinop(String name,
			    Type left, Type right, Type res,
			    int opcode1, int opcode2) {
		enterBinop(name, left, right, res, (opcode1 << ByteCodes.preShift) | opcode2);
    }

    /** Enter a unary operation into symbol table.
     *  @param name     The name of the operator.
     *  @param arg      The type of the operand.
     *  @param res      The operation's result type.
     *  @param opcode   The operation's bytecode instruction.
     */
    private OperatorSymbol enterUnop(String name,
				     Type arg,
				     Type res,
				     int opcode) {
		OperatorSymbol sym =
			new OperatorSymbol(names.fromString(name),
			       new MethodType(List.of(arg),
					      res,
					      List.<Type>nil(),
					      methodClass),
					   opcode,
					   predefClass);
        predefClass.members().enter(sym);
		return sym;
    }

    /** Enter a class into symbol table.
     *  @param    The name of the class.
     */
    private Type enterClass(String s) {
	return reader.enterClass(names.fromString(s)).type;
    }

    /** Constructor; enters all predefined identifiers and operators
     *  into symbol table.
     */
    protected Symtab(Context context) throws CompletionFailure {
		DEBUG.P(this,"Symtab(1) 符号表初始化(重要)......");
		context.put(symtabKey, this);

		names = Name.Table.instance(context);

		// Create the unknown type
		unknownType = new Type(TypeTags.UNKNOWN, null);

		// create the basic builtin symbols
		rootPackage = new PackageSymbol(names.empty, null);
		DEBUG.P("rootPackage="+rootPackage);
		//DEBUG.P("rootPackage.fullname="+rootPackage.fullname);
	
        final Messages messages = Messages.instance(context);
		unnamedPackage = new PackageSymbol(names.empty, rootPackage) {
        	//在com\sun\tools\javac\resources\compiler.properties定义
        	//提示信息是:unnamed package
			public String toString() {
				return messages.getLocalizedString("compiler.misc.unnamed.package");
			}
		};
        DEBUG.P("unnamedPackage="+unnamedPackage);
        //DEBUG.P("unnamedPackage.fullname="+unnamedPackage.fullname);
        
		noSymbol = new TypeSymbol(0, names.empty, Type.noType, rootPackage);
		noSymbol.kind = Kinds.NIL;
		
		// create the error symbols
		errSymbol = new ClassSymbol(PUBLIC|STATIC|ACYCLIC, names.any, null, rootPackage);
		/*
		注意ErrorType的构造方法: errSymbol的type与kind(由原先的TYP变成ERR)都在里面设置
			public ErrorType(ClassSymbol c) {
				this();
				tsym = c;
				c.type = this;
				c.kind = ERR;
				c.members_field = new Scope.ErrorScope(c);
			}
		*/
		errType = new ErrorType(errSymbol);

		//下面的type的tsym的owner都是rootPackage
		// initialize builtin types
		initType(byteType, "byte", "Byte");
		initType(shortType, "short", "Short");
		initType(charType, "char", "Character");
		initType(intType, "int", "Integer");
		initType(longType, "long", "Long");
		initType(floatType, "float", "Float");
		initType(doubleType, "double", "Double");
		initType(booleanType, "boolean", "Boolean");
		initType(voidType, "void", "Void");
		initType(botType, "<nulltype>");
		initType(errType, errSymbol);
		initType(unknownType, "<any?>");

		// the builtin class of all arrays
		arrayClass = new ClassSymbol(PUBLIC|ACYCLIC, names.Array, noSymbol);

		// VGJ
		boundClass = new ClassSymbol(PUBLIC|ACYCLIC, names.Bound, noSymbol);

		// the builtin class of all methods
		methodClass = new ClassSymbol(PUBLIC|ACYCLIC, names.Method, noSymbol);

		// Create class to hold all predefined constants and operations.
        predefClass = new ClassSymbol(PUBLIC|ACYCLIC, names.empty, rootPackage);
        DEBUG.P("predefClass.completer="+predefClass.completer);
		Scope scope = new Scope(predefClass);
		predefClass.members_field = scope;

		MyPredefClass = predefClass;

		// Enter symbols for basic types.
        scope.enter(byteType.tsym);
        scope.enter(shortType.tsym);
        scope.enter(charType.tsym);
        scope.enter(intType.tsym);
        scope.enter(longType.tsym);
        scope.enter(floatType.tsym);
        scope.enter(doubleType.tsym);
        scope.enter(booleanType.tsym);
        scope.enter(errType.tsym);
        
		DEBUG.P("predefClass.members_field="+predefClass.members_field);
		//predefClass.fullname是names.empty
		//DEBUG.P("predefClass.fullname="+predefClass.fullname);
		
		classes.put(predefClass.fullname, predefClass);

		reader = ClassReader.instance(context);
		//rootPackage在reader.init(this)里被加进Map<Name, PackageSymbol> packages
		//rootPackage对应的Name为names.empty,这对递归调用ClassReader.enterPackage(Name fullname)
		//起关键作用，names.empty可以作为递归终止标志
		reader.init(this);
		
		//当在这里随便加载一个没有包名的类时(如:enterClass("MyClass");)
		//在ClassReader的enterClass(Name flatName, JavaFileObject classFile)方法里
		//会引起NullPointerException
		
		// Enter predefined classes.
		objectType = enterClass("java.lang.Object");
		//测试enterClass(Name flatName, JavaFileObject classFile)中的AssertionError
		//reader.enterClass(names.fromString("java.lang.Object"),(javax.tools.JavaFileObject)null);
		classType = enterClass("java.lang.Class");
		stringType = enterClass("java.lang.String");
		stringBufferType = enterClass("java.lang.StringBuffer");
		stringBuilderType = enterClass("java.lang.StringBuilder");
		cloneableType = enterClass("java.lang.Cloneable");
		throwableType = enterClass("java.lang.Throwable");
		serializableType = enterClass("java.io.Serializable");
		errorType = enterClass("java.lang.Error");
		illegalArgumentExceptionType = enterClass("java.lang.IllegalArgumentException");
		exceptionType = enterClass("java.lang.Exception");
		runtimeExceptionType = enterClass("java.lang.RuntimeException");
		classNotFoundExceptionType = enterClass("java.lang.ClassNotFoundException");
		noClassDefFoundErrorType = enterClass("java.lang.NoClassDefFoundError");
		noSuchFieldErrorType = enterClass("java.lang.NoSuchFieldError");
		assertionErrorType = enterClass("java.lang.AssertionError");
        cloneNotSupportedExceptionType = enterClass("java.lang.CloneNotSupportedException");
		annotationType = enterClass("java.lang.annotation.Annotation");
		classLoaderType = enterClass("java.lang.ClassLoader");
		enumSym = reader.enterClass(names.java_lang_Enum);
		enumFinalFinalize =
			new MethodSymbol(PROTECTED|FINAL|HYPOTHETICAL,
					 names.finalize,
					 new MethodType(List.<Type>nil(), voidType,
							List.<Type>nil(), methodClass),
					 enumSym);
		listType = enterClass("java.util.List");
		collectionsType = enterClass("java.util.Collections");
		comparableType = enterClass("java.lang.Comparable");
		arraysType = enterClass("java.util.Arrays");
		iterableType = Target.instance(context).hasIterable()
				? enterClass("java.lang.Iterable") //JDK版本>=1.5才有
				: enterClass("java.util.Collection");
		iteratorType = enterClass("java.util.Iterator");
		annotationTargetType = enterClass("java.lang.annotation.Target");
		overrideType = enterClass("java.lang.Override");
		retentionType = enterClass("java.lang.annotation.Retention");
		deprecatedType = enterClass("java.lang.Deprecated");
		suppressWarningsType = enterClass("java.lang.SuppressWarnings");
		inheritedType = enterClass("java.lang.annotation.Inherited");

        // Enter a synthetic class that is used to mark Sun
        // proprietary classes in ct.sym.  This class does not have a
        // class file.
        ClassType proprietaryType = (ClassType)enterClass("sun.Proprietary+Annotation");
        this.proprietaryType = proprietaryType;
        ClassSymbol proprietarySymbol = (ClassSymbol)proprietaryType.tsym;
        proprietarySymbol.completer = null;
        proprietarySymbol.flags_field = PUBLIC|ACYCLIC|ANNOTATION|INTERFACE;
        proprietarySymbol.erasure_field = proprietaryType;
        proprietarySymbol.members_field = new Scope(proprietarySymbol);
        proprietaryType.typarams_field = List.nil();
        proprietaryType.allparams_field = List.nil();
        proprietaryType.supertype_field = annotationType;
        proprietaryType.interfaces_field = List.nil();

		// Enter a class for arrays.
		// The class implements java.lang.Cloneable and java.io.Serializable.
		// It has a final length field and a clone method.
		ClassType arrayClassType = (ClassType)arrayClass.type;
		//所有数组类型的超类都是java.lang.Object
		//所有数组类型都实现了java.lang.Cloneable与java.io.Serializable接口
		arrayClassType.supertype_field = objectType;
		arrayClassType.interfaces_field = List.of(cloneableType, serializableType);
		arrayClass.members_field = new Scope(arrayClass);
        lengthVar = new VarSymbol(
	    PUBLIC | FINAL,
	    names.length,//注:names不是数组,names是Name.Table,names.length表示Nmae.Table类中定义的length
	    intType,
	    arrayClass);
        arrayClass.members().enter(lengthVar);
        
		arrayCloneMethod = new MethodSymbol(
			PUBLIC,
			names.clone,
			new MethodType(List.<Type>nil(), objectType,
				   List.<Type>nil(), methodClass),
			arrayClass);
		arrayClass.members().enter(arrayCloneMethod);
		
		DEBUG.P("arrayClass.members()="+arrayClass.members());
		
		// Enter operators.
        enterUnop("+", doubleType, doubleType, nop);//最后一个参数在com.sun.tools.javac.jvm.ByteCodes定义
        enterUnop("+", floatType, floatType, nop);
        enterUnop("+", longType, longType, nop);
        enterUnop("+", intType, intType, nop);

        enterUnop("-", doubleType, doubleType, dneg);
        enterUnop("-", floatType, floatType, fneg);
        enterUnop("-", longType, longType, lneg);
        enterUnop("-", intType, intType, ineg);
        
        //注意:在虚拟机的字节码指令中并没有java语言级别的“按位取反(~)”指令，
        //java语言级别的按位取反(~)”指令用虚拟机级别的“逻辑异或”指令实现
        enterUnop("~", longType, longType, lxor);
        enterUnop("~", intType, intType, ixor);

        enterUnop("++", doubleType, doubleType, dadd);
        enterUnop("++", floatType, floatType, fadd);
        enterUnop("++", longType, longType, ladd);
        enterUnop("++", intType, intType, iadd);
        enterUnop("++", charType, charType, iadd);
        enterUnop("++", shortType, shortType, iadd);
        enterUnop("++", byteType, byteType, iadd);

        enterUnop("--", doubleType, doubleType, dsub);
        enterUnop("--", floatType, floatType, fsub);
        enterUnop("--", longType, longType, lsub);
        enterUnop("--", intType, intType, isub);
        enterUnop("--", charType, charType, isub);
        enterUnop("--", shortType, shortType, isub);
        enterUnop("--", byteType, byteType, isub);
        
        //注意:bool_not在虚拟机的字节码指令中并没有，只是虚拟的。
        //参考com.sun.tools.javac.jvm.ByteCodes的注释
        enterUnop("!", booleanType, booleanType, bool_not);
		nullcheck = enterUnop("<*nullchk*>", objectType, objectType, nullchk);

		// string concatenation
		//注意:string_add在虚拟机的字节码指令中并没有，只是虚拟的。
        //参考com.sun.tools.javac.jvm.ByteCodes的注释
        enterBinop("+", stringType, objectType, stringType, string_add);
        enterBinop("+", objectType, stringType, stringType, string_add);
        enterBinop("+", stringType, stringType, stringType, string_add);
        enterBinop("+", stringType, intType, stringType, string_add);
        enterBinop("+", stringType, longType, stringType, string_add);
        enterBinop("+", stringType, floatType, stringType, string_add);
        enterBinop("+", stringType, doubleType, stringType, string_add);
        enterBinop("+", stringType, booleanType, stringType, string_add);
        enterBinop("+", stringType, botType, stringType, string_add);
        enterBinop("+", intType, stringType, stringType, string_add);
        enterBinop("+", longType, stringType, stringType, string_add);
        enterBinop("+", floatType, stringType, stringType, string_add);
        enterBinop("+", doubleType, stringType, stringType, string_add);
        enterBinop("+", booleanType, stringType, stringType, string_add);
        enterBinop("+", botType, stringType, stringType, string_add);
	
		// these errors would otherwise be matched as string concatenation
        enterBinop("+", botType, botType, botType, error);
        enterBinop("+", botType, intType, botType, error);
        enterBinop("+", botType, longType, botType, error);
        enterBinop("+", botType, floatType, botType, error);
        enterBinop("+", botType, doubleType, botType, error);
        enterBinop("+", botType, booleanType, botType, error);
        enterBinop("+", botType, objectType, botType, error);
        enterBinop("+", intType, botType, botType, error);
        enterBinop("+", longType, botType, botType, error);
        enterBinop("+", floatType, botType, botType, error);
        enterBinop("+", doubleType, botType, botType, error);
        enterBinop("+", booleanType, botType, botType, error);
        enterBinop("+", objectType, botType, botType, error);

        enterBinop("+", doubleType, doubleType, doubleType, dadd);
        enterBinop("+", floatType, floatType, floatType, fadd);
        enterBinop("+", longType, longType, longType, ladd);
        enterBinop("+", intType, intType, intType, iadd);

        enterBinop("-", doubleType, doubleType, doubleType, dsub);
        enterBinop("-", floatType, floatType, floatType, fsub);
        enterBinop("-", longType, longType, longType, lsub);
        enterBinop("-", intType, intType, intType, isub);

        enterBinop("*", doubleType, doubleType, doubleType, dmul);
        enterBinop("*", floatType, floatType, floatType, fmul);
        enterBinop("*", longType, longType, longType, lmul);
        enterBinop("*", intType, intType, intType, imul);

        enterBinop("/", doubleType, doubleType, doubleType, ddiv);
        enterBinop("/", floatType, floatType, floatType, fdiv);
        enterBinop("/", longType, longType, longType, ldiv);
        enterBinop("/", intType, intType, intType, idiv);

        enterBinop("%", doubleType, doubleType, doubleType, dmod);
        enterBinop("%", floatType, floatType, floatType, fmod);
        enterBinop("%", longType, longType, longType, lmod);
        enterBinop("%", intType, intType, intType, imod);

        enterBinop("&", booleanType, booleanType, booleanType, iand);
        enterBinop("&", longType, longType, longType, land);
        enterBinop("&", intType, intType, intType, iand);

        enterBinop("|", booleanType, booleanType, booleanType, ior);
        enterBinop("|", longType, longType, longType, lor);
        enterBinop("|", intType, intType, intType, ior);

        enterBinop("^", booleanType, booleanType, booleanType, ixor);
        enterBinop("^", longType, longType, longType, lxor);
        enterBinop("^", intType, intType, intType, ixor);
        
        //移位运算的结果类型总是左边的操作数的类型
		//lshll可以拆开成l--shl--l，分别代表：左操作数longType--左移--移动位数为longType
		//ishl 可以拆开成l--shl，   分别代表：左操作数intType-- 左移，移动位数为intType时
		//通常将指令中的i去掉，如lshl，ishl
        enterBinop("<<", longType, longType, longType, lshll);
        enterBinop("<<", intType, longType, intType, ishll);
        enterBinop("<<", longType, intType, longType, lshl);
        enterBinop("<<", intType, intType, intType, ishl);

        enterBinop(">>", longType, longType, longType, lshrl);
        enterBinop(">>", intType, longType, intType, ishrl);
        enterBinop(">>", longType, intType, longType, lshr);
        enterBinop(">>", intType, intType, intType, ishr);

        enterBinop(">>>", longType, longType, longType, lushrl);
        enterBinop(">>>", intType, longType, intType, iushrl);
        enterBinop(">>>", longType, intType, longType, lushr);
        enterBinop(">>>", intType, intType, intType, iushr);
        
        //注意(opcode1 << ByteCodes.preShift) | opcode2);
        enterBinop("<", doubleType, doubleType, booleanType, dcmpg, iflt);
        enterBinop("<", floatType, floatType, booleanType, fcmpg, iflt);
        enterBinop("<", longType, longType, booleanType, lcmp, iflt);
        enterBinop("<", intType, intType, booleanType, if_icmplt);

        enterBinop(">", doubleType, doubleType, booleanType, dcmpl, ifgt);
        enterBinop(">", floatType, floatType, booleanType, fcmpl, ifgt);
        enterBinop(">", longType, longType, booleanType, lcmp, ifgt);
        enterBinop(">", intType, intType, booleanType, if_icmpgt);

        enterBinop("<=", doubleType, doubleType, booleanType, dcmpg, ifle);
        enterBinop("<=", floatType, floatType, booleanType, fcmpg, ifle);
        enterBinop("<=", longType, longType, booleanType, lcmp, ifle);
        enterBinop("<=", intType, intType, booleanType, if_icmple);

        enterBinop(">=", doubleType, doubleType, booleanType, dcmpl, ifge);
        enterBinop(">=", floatType, floatType, booleanType, fcmpl, ifge);
        enterBinop(">=", longType, longType, booleanType, lcmp, ifge);
        enterBinop(">=", intType, intType, booleanType, if_icmpge);
        
        //两个对象引用相等，当且仅当两个对象引用同时指向同一个对象
        enterBinop("==", objectType, objectType, booleanType, if_acmpeq);
        enterBinop("==", booleanType, booleanType, booleanType, if_icmpeq);
        enterBinop("==", doubleType, doubleType, booleanType, dcmpl, ifeq);
        enterBinop("==", floatType, floatType, booleanType, fcmpl, ifeq);
        enterBinop("==", longType, longType, booleanType, lcmp, ifeq);
        enterBinop("==", intType, intType, booleanType, if_icmpeq);

        enterBinop("!=", objectType, objectType, booleanType, if_acmpne);
        enterBinop("!=", booleanType, booleanType, booleanType, if_icmpne);
        enterBinop("!=", doubleType, doubleType, booleanType, dcmpl, ifne);
        enterBinop("!=", floatType, floatType, booleanType, fcmpl, ifne);
        enterBinop("!=", longType, longType, booleanType, lcmp, ifne);
        enterBinop("!=", intType, intType, booleanType, if_icmpne);

        enterBinop("&&", booleanType, booleanType, booleanType, bool_and);
        enterBinop("||", booleanType, booleanType, booleanType, bool_or);
        
        
        //DEBUG.P("predefClass.members_field.nelems="+predefClass.members_field.nelems);
        DEBUG.P("predefClass.members_field="+predefClass.members_field);
        DEBUG.P("classes.size="+classes.size()+" keySet="+classes.keySet());
        DEBUG.P("packages.size="+packages.size()+" keySet="+packages.keySet());
        DEBUG.P(3,this,"Symtab(1)");
/*
arrayClass.members()=Scope[(entries=2 nelems=2 owner=Array)clone(), length]
predefClass.members_field=Scope[(entries=134 nelems=134 owner=)||(), &&(), !=(), !=(), !=(), !=(), !=(), !=(), ==(), ==(), ==(), ==(), ==(), ==(), >=(), >=(), >=(), >=(), <=(), <=(), <=(), <=(), >(), >(), >(), >(), <(), <(), <(), <(), >>>(), >>>(), >>>(), >>>(), >>(), >>(), >>(), >>(), <<(), <<(), <<(), <<(), ^(), ^(), ^(), |(), |(), |(), &(), &(), &(), %(), %(), %(), %(), /(), /(), /(), /(), *(), *(), *(), *(), -(), -(), -(), -(), +(), +(), +(), +(), +(), +(), +(), +(), +(), +(), +(), +(), +(), +(), +(), +(), +(), +(), +(), +(), +(), +(), +(), +(), +(), +(), +(), +(), +(), +(), +(), +(), <*nullchk*>(), !(), --(), --(), --(), --(), --(), --(), --(), ++(), ++(), ++(), ++(), ++(), ++(), ++(), ~(), ~(), -(), -(), -(), -(), +(), +(), +(), +(), <any>, boolean, double, float, long, int, char, short, byte]
classes.size=34 keySet=[java.lang.Exception, java.io.Serializable, java.lang.NoClassDefFoundError, java.util.Iterator, java.lang.annotation.Inherited, java.lang.Deprecated, java.lang.StringBuffer, java.lang.CloneNotSupportedException, java.lang.ClassLoader, java.lang.annotation.Annotation, java.util.List, java.lang.Cloneable, java.lang.String, java.lang.Override, java.lang.ClassNotFoundException, java.lang.Comparable, java.lang.IllegalArgumentException, java.lang.SuppressWarnings, java.lang.Iterable, java.lang.RuntimeException, java.lang.Class, java.lang.NoSuchFieldError, java.lang.Enum, java.lang.StringBuilder, java.util.Collections, java.lang.annotation.Target, java.lang.Error, java.lang.Object, sun.Proprietary+Annotation, java.util.Arrays, java.lang.AssertionError, , java.lang.annotation.Retention, java.lang.Throwable]
packages.size=7 keySet=[sun, java.util, java.lang.annotation, , java, java.io, java.lang]
*/        
		//DEBUG.P("Symtab(1) stop",true);
    }
}
