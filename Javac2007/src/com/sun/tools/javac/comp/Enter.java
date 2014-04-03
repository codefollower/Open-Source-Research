/*
 * @(#)Enter.java	1.136 07/03/21
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

package com.sun.tools.javac.comp;

import java.util.*;
import java.util.Set;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileManager;

import com.sun.tools.javac.code.*;
import com.sun.tools.javac.jvm.*;
import com.sun.tools.javac.tree.*;
import com.sun.tools.javac.util.*;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;
import com.sun.tools.javac.util.List;

import com.sun.tools.javac.code.Type.*;
import com.sun.tools.javac.code.Symbol.*;
import com.sun.tools.javac.tree.JCTree.*;

import static com.sun.tools.javac.code.Flags.*;
import static com.sun.tools.javac.code.Kinds.*;
import static com.sun.tools.javac.code.TypeTags.*;

/** This class enters symbols for all encountered definitions into
 *  the symbol table. The pass consists of two phases, organized as
 *  follows:
 *
 *  <p>In the first phase, all class symbols are intered into their
 *  enclosing scope, descending recursively down the tree for classes
 *  which are members of other classes. The class symbols are given a
 *  MemberEnter object as completer.
 *
 *  <p>In the second phase classes are completed using
 *  MemberEnter.complete().  Completion might occur on demand, but
 *  any classes that are not completed that way will be eventually
 *  completed by processing the `uncompleted' queue.  Completion
 *  entails (1) determination of a class's parameters, supertype and
 *  interfaces, as well as (2) entering all symbols defined in the
 *  class into its scope, with the exception of class symbols which
 *  have been entered in phase 1.  (2) depends on (1) having been
 *  completed for a class and all its superclasses and enclosing
 *  classes. That's why, after doing (1), we put classes in a
 *  `halfcompleted' queue. Only when we have performed (1) for a class
 *  and all it's superclasses and enclosing classes, we proceed to
 *  (2).
 *
 *  <p>Whereas the first phase is organized as a sweep through all
 *  compiled syntax trees, the second phase is demand. Members of a
 *  class are entered when the contents of a class are first
 *  accessed. This is accomplished by installing completer objects in
 *  class symbols for compiled classes which invoke the member-enter
 *  phase for the corresponding class tree.
 *
 *  <p>Classes migrate from one phase to the next via queues:
 *
 *  <pre>
 *  class enter -> (Enter.uncompleted)         --> member enter (1)
 *		-> (MemberEnter.halfcompleted) --> member enter (2)
 *		-> (Todo)	               --> attribute
 *						(only for toplevel classes)
 *  </pre>
 *
 *  <p><b>This is NOT part of any API supported by Sun Microsystems.  If
 *  you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 */
@Version("@(#)Enter.java	1.136 07/03/21")
public class Enter extends JCTree.Visitor {
	private static my.Debug DEBUG=new my.Debug(my.Debug.Enter);//我加上的
	
    protected static final Context.Key<Enter> enterKey =
	new Context.Key<Enter>();

    Log log;
    Symtab syms;
    Check chk;
    TreeMaker make;
    ClassReader reader;
    Annotate annotate;
    MemberEnter memberEnter;
    Lint lint;
    JavaFileManager fileManager;

    private final Todo todo;
    
    private final Name.Table names;//我加上的

    public static Enter instance(Context context) {
		Enter instance = context.get(enterKey);
		if (instance == null)
			instance = new Enter(context);
		return instance;
    }

    protected Enter(Context context) {
		DEBUG.P(this,"Enter(1)");
		context.put(enterKey, this);

		log = Log.instance(context);
		reader = ClassReader.instance(context);
		make = TreeMaker.instance(context);
		syms = Symtab.instance(context);
		chk = Check.instance(context);
		memberEnter = MemberEnter.instance(context);
		annotate = Annotate.instance(context);
		lint = Lint.instance(context);

		predefClassDef = make.ClassDef(
			make.Modifiers(PUBLIC),
			syms.predefClass.name, null, null, null, null);
		//predefClass是一个ClassSymbol(PUBLIC|ACYCLIC, names.empty, rootPackage)
		//且它的Scope members_field已有成员(几个基本类型符号(symbols for basic types)及其他操作符)
		//请参考Systab类的predefClass字段说明
		predefClassDef.sym = syms.predefClass;

		todo = Todo.instance(context);
		fileManager = context.get(JavaFileManager.class);
		
		names = Name.Table.instance(context);    //我加上的
		DEBUG.P(0,this,"Enter(1)");
    }

    /** A hashtable mapping classes and packages to the environments current
     *  at the points of their definitions.
     */
    Map<TypeSymbol,Env<AttrContext>> typeEnvs =
	    new HashMap<TypeSymbol,Env<AttrContext>>();

    /** Accessor for typeEnvs
     */
    public Env<AttrContext> getEnv(TypeSymbol sym) {
		return typeEnvs.get(sym);
    }
    
    public Env<AttrContext> getClassEnv(TypeSymbol sym) {
        Env<AttrContext> localEnv = getEnv(sym);
        Env<AttrContext> lintEnv = localEnv;
        //lint在AttrContext中定义
        while (lintEnv.info.lint == null)
            lintEnv = lintEnv.next;
        localEnv.info.lint = lintEnv.info.lint.augment(sym.attributes_field, sym.flags());
        return localEnv;
    }

    /** The queue of all classes that might still need to be completed;
     *	saved and initialized by main().
     */
    ListBuffer<ClassSymbol> uncompleted;//它的值在Enter相应的visitXXX()中设置

    /** A dummy class to serve as enclClass for toplevel environments.
     */
    private JCClassDecl predefClassDef;

/* ************************************************************************
 * environment construction
 *************************************************************************/


    /** Create a fresh environment for class bodies.
     *	This will create a fresh scope for local symbols of a class, referred
     *	to by the environments info.scope field.
     *	This scope will contain
     *	  - symbols for this and super
     *	  - symbols for any type parameters
     *	In addition, it serves as an anchor for scopes of methods and initializers
     *	which are nested in this scope via Scope.dup().
     *	This scope should not be confused with the members scope of a class.
     *
     *	@param tree	The class definition.
     *	@param env	The environment current outside of the class definition.
     */
     
    //内部类不属于JCCompilationUnit(topLevelEnv),而只属于JCClassDecl(classEnv)
    public Env<AttrContext> classEnv(JCClassDecl tree, Env<AttrContext> env) {
		DEBUG.P(this,"classEnv(2)");
    	DEBUG.P("env="+env);
		Env<AttrContext> localEnv =
			env.dup(tree, env.info.dup(new Scope(tree.sym)));
		localEnv.enclClass = tree;
		localEnv.outer = env;
		localEnv.info.isSelfCall = false;
		localEnv.info.lint = null; // leave this to be filled in by Attr, 
								   // when annotations have been processed
		DEBUG.P("localEnv="+localEnv);
		DEBUG.P(0,this,"classEnv(2)");
		return localEnv;
    }

    /** Create a fresh environment for toplevels.
     *	@param tree	The toplevel tree.
     */
    Env<AttrContext> topLevelEnv(JCCompilationUnit tree) {
		Env<AttrContext> localEnv = new Env<AttrContext>(tree, new AttrContext());
		localEnv.toplevel = tree;
		localEnv.enclClass = predefClassDef;
		tree.namedImportScope = new Scope.ImportScope(tree.packge);
		tree.starImportScope = new Scope.ImportScope(tree.packge);
		localEnv.info.scope = tree.namedImportScope;//注意这里
		
		//都是Scope[]
		//DEBUG.P("tree.namedImportScope="+tree.namedImportScope);
		//DEBUG.P("tree.starImportScope="+tree.starImportScope);
		//DEBUG.P("localEnv.info.scope="+localEnv.info.scope);
			
		localEnv.info.lint = lint;
		return localEnv;
    } 

    public Env<AttrContext> getTopLevelEnv(JCCompilationUnit tree) {
		Env<AttrContext> localEnv = new Env<AttrContext>(tree, new AttrContext());
		localEnv.toplevel = tree;
		localEnv.enclClass = predefClassDef;
		localEnv.info.scope = tree.namedImportScope;
		localEnv.info.lint = lint;
		return localEnv;
    }

    /** The scope in which a member definition in environment env is to be entered
     *	This is usually the environment's scope, except for class environments,
     *	where the local scope is for type variables, and the this and super symbol
     *	only, and members go into the class member scope.
     */
    Scope enterScope(Env<AttrContext> env) {
		try {
    	DEBUG.P(this,"enterScope(1)");
		if((env.tree.tag == JCTree.CLASSDEF))
    		DEBUG.P("选择的Scope是 "+((JCClassDecl) env.tree).sym+" JCClassDecl.sym.members_field)");
		else
			DEBUG.P("选择的Scope是 env.info.scope 拥有者是"+env.info.scope.owner);

		return (env.tree.tag == JCTree.CLASSDEF)
			? ((JCClassDecl) env.tree).sym.members_field
			: env.info.scope;


		} finally {
    	DEBUG.P(0,this,"enterScope(1)");
    	}
    }

/* ************************************************************************
 * Visitor methods for phase 1: class enter
 *************************************************************************/

    /** Visitor argument: the current environment.
     */
    protected Env<AttrContext> env;

    /** Visitor result: the computed type.
     */
    Type result;//它的值在Enter相应的visitXXX()中设置

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



    public void visitTopLevel(JCCompilationUnit tree) {
		JavaFileObject prev = log.useSource(tree.sourcefile);
		DEBUG.P(this,"visitTopLevel(1)");
		//在没有进行到Enter阶段的时候JCCompilationUnit的PackageSymbol packge
		//是null，这也说明了:Parser的后续阶段的任务就是往各类JCTree中“塞入数据”
		DEBUG.P("JCCompilationUnit tree.sourcefile="+tree.sourcefile);
		DEBUG.P("JCCompilationUnit tree.packge="+tree.packge);
        DEBUG.P("JCCompilationUnit tree.pid="+tree.pid);

		boolean addEnv = false;
		
		//DEBUG.P("JCCompilationUnit tree.sourcefile.className="+tree.sourcefile.getClass().getName());
		//输出一般是:com.sun.tools.javac.util.JavacFileManager$RegularFileObject
		//JavacFileManager.RegularFileObject, JavacFileManager.ZipFileObject都实现了
		//JavaFileObject接口
		
		//检查JCCompilationUnit tree.sourcefile的文件名是否是package-info.java
		boolean isPkgInfo = tree.sourcefile.isNameCompatible("package-info",
									 JavaFileObject.Kind.SOURCE);
		DEBUG.P("isPkgInfo="+isPkgInfo);

		//tree.pid是源文件所在包的全名					     
		if (tree.pid != null) {
				//在执行了TreeInfo.fullName(tree.pid)后，将产生一个完整的包名，并且
				//存放在Name.Table中
				//(注:如果包名是:my.test,在Name.Table中会有三个name:(my),(test)与(my.test)
				//作者一心只想提高javac的执行速度
				//DEBUG.P(names.myNames());
			tree.packge = reader.enterPackage(TreeInfo.fullName(tree.pid));
			//DEBUG.P(names.myNames());
			DEBUG.P("tree.packageAnnotations="+tree.packageAnnotations);
			if (tree.packageAnnotations.nonEmpty()) {
					if (isPkgInfo) {
						addEnv = true;
					} else {
						//只有package-info.java才能有包注释
						//参考:Parser.compilationUnit()
						log.error(tree.packageAnnotations.head.pos(),
								  "pkg.annotations.sb.in.package-info.java");
					}
			}
		} else {
				//源文件未定义所属package的情况
			tree.packge = syms.unnamedPackage;
		}
		DEBUG.P("JCCompilationUnit tree.packge="+tree.packge);
		DEBUG.P("JCCompilationUnit tree.packge.members_field="+tree.packge.members_field);
		DEBUG.P("syms.classes.size="+syms.classes.size()+" keySet="+syms.classes.keySet());
        DEBUG.P("syms.packages.size="+syms.packages.size()+" keySet="+syms.packages.keySet());
		
		/*
		complete()在com.sun.tools.javac.code.Symbol定义
		tree.packge是com.sun.tools.javac.code.Symbol.PackageSymbol的实例引用
		com.sun.tools.javac.jvm.ClassReader实现了com.sun.tools.javac.code.Symbol.Completer接口
		调用Symbol.complete()会通过Symbol.Completer completer(在ClassReader的enterPackage方法中赋值)
		间接调用ClassReader的complete(Symbol sym)方法
		
		调用过程:com.sun.tools.javac.code.Symbol::complete()==>
				 com.sun.tools.javac.jvm.ClassReader::complete(1)

		在没执行complete()前，在执行完上面的enterPackage后，得到了一个
		PackageSymbol，但这个PackageSymbol的Scope members_field是null的，
		执行complete()的目的就是为了找出PackageSymbol所表示的包名中的
		所有类文件，并将这些类文件“包装”成一个ClassSymbol放入members_field
		*/

		//虽然complete()方法抛出CompletionFailure，
		//但因为CompletionFailure是RuntimeException的子类，
		//所以在visitTopLevel此方法中可以不捕获
		tree.packge.complete(); // Find all classes in package.

		//成员也有可能是未编译的.java文件
		//如果文件是Package-Info1.java，
		//则因为"-"不满足ClassReader的方法fillIn(3)中的SourceVersion.isIdentifier(simpleName)而被过滤掉，
		//另外文件Package-Info1.java在ClassReader的方法includeClassFile(2)中被加入tree.packge.package_info，而不是加入tree.packge.members_field
		DEBUG.P(3);
		DEBUG.P(tree.packge+"包中的所有成员装载完成(Enter)");
		DEBUG.P("JCCompilationUnit tree.packge.members_field="+tree.packge.members_field);
        DEBUG.P("syms.classes.size="+syms.classes.size()+" keySet="+syms.classes.keySet());
        DEBUG.P("syms.packages.size="+syms.packages.size()+" keySet="+syms.packages.keySet());
		DEBUG.P(3);

        Env<AttrContext> env = topLevelEnv(tree);

		// Save environment of package-info.java file.
		if (isPkgInfo) {
			Env<AttrContext> env0 = typeEnvs.get(tree.packge);
			if (env0 == null) {
				typeEnvs.put(tree.packge, env);
			} else {
				JCCompilationUnit tree0 = env0.toplevel;
				if (!fileManager.isSameFile(tree.sourcefile, tree0.sourcefile)) {
					/* 当同时编译两个在不同目录的同名package-info.java文件时，
					如果这两个package-info.java的内容都是相同的包如，:package test.enter;
					则会发出"警告：[package-info] 已找到软件包 test.enter 的 package-info.java 文件"
					//test\enter\package-info.java
					package test.enter;
					//test\enter\package-info.java
					package test.enter;
					*/
					log.warning(tree.pid != null ? tree.pid.pos()
								: null,
								"pkg-info.already.seen",
								tree.packge);
					if (addEnv || (tree0.packageAnnotations.isEmpty() &&
						   tree.docComments != null &&
						   tree.docComments.get(tree) != null)) {
						typeEnvs.put(tree.packge, env);
					}
				}
			}
		}

		classEnter(tree.defs, env);
        if (addEnv) {//包注释待处理
            todo.append(env);
        }
		log.useSource(prev);
		result = null;
	
	/*******************以下都是打印信息的语句(调试用途)********************/
        DEBUG.P(2);
        DEBUG.P("***第一阶段Enter完成***");
        DEBUG.P("-----------------------------------------------");
        DEBUG.P("包名: "+tree.packge);
        DEBUG.P("--------------------------");
        DEBUG.P("tree.packge.members_field: "+tree.packge.members_field);
        DEBUG.P("tree.namedImportScope    : "+tree.namedImportScope);
        DEBUG.P("tree.starImportScope     : "+tree.starImportScope);
        DEBUG.P("");
        
        //ListBuffer<ClassSymbol> uncompleted
        DEBUG.P("等待编译的类的总数: "+uncompleted.size());
        DEBUG.P("--------------------------");
        for(ClassSymbol myClassSymbol:uncompleted) {
        	DEBUG.P("类名             : "+myClassSymbol);
        	DEBUG.P("members_field    : "+myClassSymbol.members_field);
        	DEBUG.P("flags            : "+Flags.toString(myClassSymbol.flags_field));
        	DEBUG.P("sourcefile       : "+myClassSymbol.sourcefile);
        	DEBUG.P("classfile        : "+myClassSymbol.classfile);
        	DEBUG.P("completer        : "+myClassSymbol.completer);
        	ClassType myClassType=(ClassType)myClassSymbol.type;
        	DEBUG.P("type             : "+myClassType);
        	DEBUG.P("outer_field      : "+myClassType.getEnclosingType());
        	DEBUG.P("supertype_field  : "+myClassType.supertype_field);
        	DEBUG.P("interfaces_field : "+myClassType.interfaces_field);
        	DEBUG.P("typarams_field   : "+myClassType.typarams_field);
        	DEBUG.P("allparams_field  : "+myClassType.allparams_field);
        	DEBUG.P("");
        }
        DEBUG.P("");
        DEBUG.P("Env总数: "+typeEnvs.size());
        DEBUG.P("--------------------------");
        for(Map.Entry<TypeSymbol,Env<AttrContext>> myMapEntry:typeEnvs.entrySet())
        	DEBUG.P(""+myMapEntry);
        DEBUG.P(2);
        
        DEBUG.P("Todo总数: "+todo.size());
        DEBUG.P("--------------------------");
        for(List<Env<AttrContext>> l=todo.toList();l.nonEmpty();l=l.tail)
        	DEBUG.P(""+l.head);
        DEBUG.P(2);
        
    	DEBUG.P("syms.classes.size="+syms.classes.size()+" keySet="+syms.classes.keySet());
        DEBUG.P("syms.packages.size="+syms.packages.size()+" keySet="+syms.packages.keySet());
        DEBUG.P(2);
		DEBUG.P(2,this,"visitTopLevel(1)");
	//
    }





    public void visitClassDef(JCClassDecl tree) {
        DEBUG.P(this,"visitClassDef(1)");
        //在没有进行到Enter阶段的时候JCClassDecl的ClassSymbol sym
		//是null，这也说明了:Parser的后续阶段的任务就是往各类JCTree中“塞入数据”
		DEBUG.P("JCClassDecl tree.sym="+tree.sym);
		DEBUG.P("JCClassDecl tree.name="+tree.name);
		
		Symbol owner = env.info.scope.owner;
		Scope enclScope = enterScope(env);
		ClassSymbol c;
		
		DEBUG.P("Symbol owner.kind="+Kinds.toString(owner.kind));
		DEBUG.P("Symbol owner="+owner);
		/*
		注意Scope enclScope与
		JCCompilationUnit.PackageSymbol packge.members_field的差别
		Scope enclScope有可能是指向JCCompilationUnit.namedImportScope(参看topLevelEnv())
		所以下面的输出可能是:Scope enclScope=Scope[]
		*/
		DEBUG.P("Scope enclScope="+enclScope);
		if (owner.kind == PCK) {
				// <editor-fold defaultstate="collapsed">
			// We are seeing a toplevel class.
			PackageSymbol packge = (PackageSymbol)owner;
			//一般在ClassReader.includeClassFile()中已设过
			DEBUG.P("PackageSymbol packge.flags_field(1)="+packge.flags_field+"("+Flags.toString(packge.flags_field)+")");
			for (Symbol q = packge; q != null && q.kind == PCK; q = q.owner)
			q.flags_field |= EXISTS;//EXISTS在com.sun.tools.javac.code.Flags
			
				DEBUG.P("PackageSymbol packge.name="+packge);
				DEBUG.P("PackageSymbol packge.flags_field(2)="+packge.flags_field+"("+Flags.toString(packge.flags_field)+")");
			
				//JCClassDecl.name只是一个简单的类名(不含包名)
			c = reader.enterClass(tree.name, packge);
			
			DEBUG.P("packge.members()前="+packge.members());
			packge.members().enterIfAbsent(c);
			DEBUG.P("packge.members()后="+packge.members());
			
			//如果一个类是public的，则源文件名需和类名一样
			//否则报错:如:
			//Test4.java:25: class Test4s is public, should be declared in a file named Test4s.java
			//public class Test4s {
			//       ^
			if ((tree.mods.flags & PUBLIC) != 0 && !classNameMatchesFileName(c, env)) {
			log.error(tree.pos(),
				  "class.public.should.be.in.file", tree.name);
			}
				// </editor-fold>
		} else {
				// <editor-fold defaultstate="collapsed">
			if (tree.name.len != 0 &&
			!chk.checkUniqueClassName(tree.pos(), tree.name, enclScope)) {
				/*
				有两个或两个以上的成员类(或接口)同名时并不是在Parser阶段发现错误的
				而是在这里通过checkUniqueClassName()检查
				例如下面的代码:
				package my.test;
				public class Test {
					public class MyInnerClass {
					}
					
					public interface MyInnerClass {
					}
				}
				通过compiler.properties文件中的"compiler.err.already.defined"报错:
				bin\mysrc\my\test\Test.java:12: 已在 my.test.Test 中定义 my.test.Test.MyInnerClass
						public interface MyInnerClass {
							   ^
				1 错误
				*/
				
				DEBUG.P(2,this,"visitClassDef(1)");
				result = null;
				return;
			}
				// </editor-fold>
				// <editor-fold defaultstate="collapsed">
			if (owner.kind == TYP) {
				// We are seeing a member class.
				c = reader.enterClass(tree.name, (TypeSymbol)owner);

				DEBUG.P("owner.flags_field="+Flags.toString(owner.flags_field));
				DEBUG.P("tree.mods.flags="+Flags.toString(tree.mods.flags));
				
				//接口中的成员的修饰符都包括PUBLIC和STATIC
				//注意在接口内部也可定义接口、类、枚举类型
				if ((owner.flags_field & INTERFACE) != 0) {
					tree.mods.flags |= PUBLIC | STATIC;
				}

				DEBUG.P("tree.mods.flags="+Flags.toString(tree.mods.flags));

			} else {
				DEBUG.P("owner.kind!=TYP(注意)");
				// We are seeing a local class.
				c = reader.defineClass(tree.name, owner);
				c.flatname = chk.localClassName(c);
				DEBUG.P("c.flatname="+c.flatname);
				if (c.name.len != 0)
					chk.checkTransparentClass(tree.pos(), c, env.info.scope);
			}
				// </editor-fold>
		}
		tree.sym = c;
		
		DEBUG.P(2);
		DEBUG.P("JCClassDecl tree.sym="+tree.sym);
		DEBUG.P("JCClassDecl tree.sym.members_field="+tree.sym.members_field);
		DEBUG.P("ClassSymbol c.sourcefile="+c.sourcefile);
		DEBUG.P("ClassSymbol c.classfile="+c.classfile);
		DEBUG.P("if (chk.compiled.get(c.flatname) != null)="+(chk.compiled.get(c.flatname) != null));
		
		//在com.sun.tools.javac.comp.Check定义为:public Map<Name,ClassSymbol> compiled = new HashMap<Name, ClassSymbol>();
		
		// Enter class into `compiled' table and enclosing scope.
		if (chk.compiled.get(c.flatname) != null) {
			//在同一源文件中定义了两个同名的类
			duplicateClass(tree.pos(), c);
			result = new ErrorType(tree.name, (TypeSymbol)owner);
			tree.sym = (ClassSymbol)result.tsym;
			
			DEBUG.P(2,this,"visitClassDef(1)");
			return;
		}
		chk.compiled.put(c.flatname, c);
		enclScope.enter(c);
		DEBUG.P("Scope enclScope="+enclScope);
		//DEBUG.P("env="+env);
		

		// Set up an environment for class block and store in `typeEnvs'
		// table, to be retrieved later in memberEnter and attribution.
		Env<AttrContext> localEnv = classEnv(tree, env);
		typeEnvs.put(c, localEnv);
		
		DEBUG.P("c.flags_field="+Flags.toString(c.flags_field));
		DEBUG.P("tree.mods.flags="+Flags.toString(tree.mods.flags));

		// Fill out class fields.
		c.completer = memberEnter;//这里要注意,往后complete()的调用转到MemberEnter了
		c.flags_field = chk.checkFlags(tree.pos(), tree.mods.flags, c, tree);
		c.sourcefile = env.toplevel.sourcefile;
		c.members_field = new Scope(c);
		
		DEBUG.P("ClassSymbol c.sourcefile="+c.sourcefile);
		DEBUG.P("c.flags_field="+Flags.toString(c.flags_field));
		
		ClassType ct = (ClassType)c.type;
		DEBUG.P("owner.kind="+Kinds.toString(owner.kind));
		DEBUG.P("ct.getEnclosingType()="+ct.getEnclosingType());
		
		/*如果是非静态成员类(不包括成员接口，成员枚举类)，将owner的type设成它的outer_field
		如下代码:只有MyInnerClass符合条件，将outer_field指向Test
		public class Test {
			public class MyInnerClass {}
			public static class MyInnerClassStatic {}
			public interface MyInnerInterface {}
			public static interface MyInnerInterfaceStatic {}
			public enum MyInnerEnum {}
			public static enum MyInnerEnumStatic {}
		}
		*/
		if (owner.kind != PCK && (c.flags_field & STATIC) == 0) {
			// We are seeing a local or inner class.
			// Set outer_field of this class to closest enclosing class
			// which contains this class in a non-static context
			// (its "enclosing instance class"), provided such a class exists.
			Symbol owner1 = owner;
			//注:在静态上下文(如：静态方法体)中是不能引用非静态类的，
			//按照上面这一点来理解while的条件组合就不会莫明其妙了
				
			//是一个本地类
			/*例:
			class EnterTest {
				static void methodA() {
					class LocalClass{} //ct.getEnclosingType()=<none>
				}
				void methodB() {
					class LocalClass{} //ct.getEnclosingType()=my.test.EnterTest
				}
			}
			*/
			while ((owner1.kind & (VAR | MTH)) != 0 &&
			   (owner1.flags_field & STATIC) == 0) { //静态方法中的本地类没有outer
				owner1 = owner1.owner;
			}
			if (owner1.kind == TYP) {
				ct.setEnclosingType(owner1.type);

				DEBUG.P("ct      ="+ct.tsym);
				DEBUG.P("ct.outer="+ct.getEnclosingType());
			}
		}
		DEBUG.P("ct.getEnclosingType()="+ct.getEnclosingType());
		DEBUG.P("ct.typarams_field="+ct.typarams_field);

		// Enter type parameters.
		ct.typarams_field = classEnter(tree.typarams, localEnv);
		
		DEBUG.P("ct.typarams_field="+ct.typarams_field);

        DEBUG.P(2);
        DEBUG.P("***Enter完Type Parameter***");
        DEBUG.P("-----------------------------------------------");
        DEBUG.P("类名: "+c);
        //注意Type Parameter并不是c.members_field的成员
        DEBUG.P("成员: "+c.members_field);
        DEBUG.P("Type Parameter: "+localEnv.info.scope);
       	DEBUG.P(2);

		DEBUG.P("if (!c.isLocal() && uncompleted != null)="+(!c.isLocal() && uncompleted != null));
		
		// Add non-local class to uncompleted, to make sure it will be
		// completed later.
		if (!c.isLocal() && uncompleted != null) uncompleted.append(c);
		//	System.err.println("entering " + c.fullname + " in " + c.owner);//DEBUG

		// Recursively enter all member classes.
		

		DEBUG.P("tree.type="+tree.type);
		classEnter(tree.defs, localEnv);
		//DEBUG.P("Enter.visitClassDef(JCClassDecl tree) stop",true);

		result = c.type;
		
			DEBUG.P(2);
			DEBUG.P("***类的所有成员Enter完成***");
			DEBUG.P("-----------------------------------------------");
			DEBUG.P("类名: "+c);
			DEBUG.P("成员: "+c.members_field);
			DEBUG.P("Type Parameter: "+localEnv.info.scope);
		
		//注意:方法中定义的类(本地类)并不Enter
		DEBUG.P(2,this,"visitClassDef(1)");
    }
    
    
    
    
    
    
    //where
	/** Does class have the same name as the file it appears in?
	 */
	private static boolean classNameMatchesFileName(ClassSymbol c,
							Env<AttrContext> env) {
	    return env.toplevel.sourcefile.isNameCompatible(c.name.toString(),
							    JavaFileObject.Kind.SOURCE);
	}

    /** Complain about a duplicate class. */
    protected void duplicateClass(DiagnosticPosition pos, ClassSymbol c) {
		log.error(pos, "duplicate.class", c.fullname);
    }

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

    /** Default class enter visitor method: do nothing.
     */
    public void visitTree(JCTree tree) {
    	DEBUG.P(this,"visitTree(1)");
        result = null;
        DEBUG.P(0,this,"visitTree(1)");
    }

    /** Main method: enter all classes in a list of toplevel trees.
     *	@param trees	  The list of trees to be processed.
     */
    public void main(List<JCCompilationUnit> trees) {
		DEBUG.P(this,"main(1)");
		complete(trees, null);
		DEBUG.P(0,this,"main(1)");
    }

    /** Main method: enter one class from a list of toplevel trees and
     *  place the rest on uncompleted for later processing.
     *  @param trees      The list of trees to be processed.
     *  @param c          The class symbol to be processed.
     */
     
    //在从MemberEnter阶段进行到Resolve.loadClass(Env<AttrContext> env, Name name)时，
    //如果一个类的超类还没有编译，则先从头开始编译超类，又会从JavaCompiler.complete(ClassSymbol c)
    //转到这里，此时 ClassSymbol c就不为null了
    public void complete(List<JCCompilationUnit> trees, ClassSymbol c) {
    	DEBUG.P(this,"complete(2)");
    	//DEBUG.P("完成Enter前List<JCCompilationUnit> trees的内容: trees.size="+trees.size());
    	//DEBUG.P("------------------------------------------------------------------------------");
    	//DEBUG.P(""+trees);
    	//DEBUG.P("------------------------------------------------------------------------------");
		/*
    	if(typeEnvs!=null) {
            DEBUG.P("");
            DEBUG.P("Env总数: "+typeEnvs.size());
            DEBUG.P("--------------------------");
            for(Map.Entry<TypeSymbol,Env<AttrContext>> myMapEntry:typeEnvs.entrySet())
                    DEBUG.P(""+myMapEntry);
            DEBUG.P("");	
        }
        DEBUG.P("memberEnter.completionEnabled="+memberEnter.completionEnabled);
		*/
    	
       
        annotate.enterStart();
        ListBuffer<ClassSymbol> prevUncompleted = uncompleted;
        if (memberEnter.completionEnabled) uncompleted = new ListBuffer<ClassSymbol>();

        DEBUG.P("ListBuffer<ClassSymbol> uncompleted.size()="+uncompleted.size());//0

        try {
            // enter all classes, and construct uncompleted list
            classEnter(trees, null);


            DEBUG.P(5);
            DEBUG.P("***进入第二阶段MemberEnter***");
            DEBUG.P("-----------------------------------------------");

            //uncompleted中不含本地类
            DEBUG.P("memberEnter.completionEnabled="+memberEnter.completionEnabled);
            //DEBUG.P("ListBuffer<ClassSymbol> uncompleted.size()="+uncompleted.size());//!=0

            // complete all uncompleted classes in memberEnter
            if (memberEnter.completionEnabled) {
                if(uncompleted!=null) DEBUG.P("uncompleted="+uncompleted.size()+" "+uncompleted.toList());
                else DEBUG.P("uncompleted=null");
                
                // <editor-fold defaultstate="collapsed">

                while (uncompleted.nonEmpty()) {
                    ClassSymbol clazz = uncompleted.next();
                    DEBUG.P("Uncompleted SymbolName="+clazz);
                    DEBUG.P("clazz.completer="+clazz.completer);
                    DEBUG.P("(c == null)="+(c == null));
                    DEBUG.P("(c == clazz)="+(c == clazz));
                    DEBUG.P("(prevUncompleted == null)="+(prevUncompleted == null));
                    /*
                    if(c!=null) DEBUG.P("c.name="+c.name+" c.kind="+c.kind);
                    else DEBUG.P("c.name=null c.kind=null");
                    if(clazz!=null) DEBUG.P("clazz.name="+clazz.name+" clazz.kind="+clazz.kind);
                    else DEBUG.P("clazz.name=null clazz.kind=null");
                    */

                    //当从MemberEnter阶段进行到这里时，c!=null，c在uncompleted中，
                    //条件c == clazz至少满足一次，所以对c调用complete()，
                    //但是如果c有内部类，因为c!=null且c != clazz(内部类)且
                    //prevUncompleted != null(因第一次进入MemberEnter阶段时uncompleted!=null)
                    //所以c的所有内部类暂时不调用complete()，先放入prevUncompleted中，留到后面调用
                    if (c == null || c == clazz || prevUncompleted == null)
                        clazz.complete();
                    else
                        // defer
                        prevUncompleted.append(clazz);

                    DEBUG.P("");
                }
                // </editor-fold>

				DEBUG.P("trees="+trees);

                // if there remain any unimported toplevels (these must have
                // no classes at all), process their import statements as well.
                for (JCCompilationUnit tree : trees) {
                    DEBUG.P(2);
                    DEBUG.P("tree.starImportScope="+tree.starImportScope);
                    DEBUG.P("tree.namedImportScope="+tree.namedImportScope);
					DEBUG.P("tree.starImportScope.elems="+tree.starImportScope.elems);
                    if (tree.starImportScope.elems == null) {
                        JavaFileObject prev = log.useSource(tree.sourcefile);
                        //有点怪typeEnvs =new HashMap<TypeSymbol,Env<AttrContext>>();
                        //而tree是JCCompilationUnit，怎么get???????????

						//同时编译package-info.java时就会出现这种情况
                        Env<AttrContext> env = typeEnvs.get(tree);
						DEBUG.P("env="+env);
                        if (env == null)
                            env = topLevelEnv(tree);
                        memberEnter.memberEnter(tree, env);
                        log.useSource(prev);
                    }
                }

				DEBUG.P("Enter结束:for (JCCompilationUnit tree : trees)");
				DEBUG.P(3);
            }
        } finally {
            uncompleted = prevUncompleted;
            annotate.enterDone();

            if(uncompleted!=null) DEBUG.P("uncompleted="+uncompleted.size()+" "+uncompleted.toList());
            else DEBUG.P("uncompleted=null");

            //DEBUG.P(2);
            //DEBUG.P("完成Enter后List<JCCompilationUnit> trees的内容: trees.size="+trees.size());
            //DEBUG.P("------------------------------------------------------------------------------");
            //DEBUG.P(""+trees);
            //DEBUG.P("------------------------------------------------------------------------------");
            
			/*
			if(typeEnvs!=null) {
				DEBUG.P("");
				DEBUG.P("Env总数: "+typeEnvs.size());
				DEBUG.P("--------------------------");
				for(Map.Entry<TypeSymbol,Env<AttrContext>> myMapEntry:typeEnvs.entrySet()) {
					Env<AttrContext> e = myMapEntry.getValue();
					DEBUG.P("e.tree.type="+e.tree.type); //JCClassDecl.type为null
				}
				DEBUG.P("");	
			}
			DEBUG.P("memberEnter.completionEnabled="+memberEnter.completionEnabled);
			*/
			
			DEBUG.P(2,this,"complete(2)");
        }
    }
}
