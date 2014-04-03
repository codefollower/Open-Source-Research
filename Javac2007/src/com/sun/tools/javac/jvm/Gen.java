/*
 * @(#)Gen.java	1.148 07/03/21
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

package com.sun.tools.javac.jvm;
import java.util.*;

import com.sun.tools.javac.util.*;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.code.*;
import com.sun.tools.javac.comp.*;
import com.sun.tools.javac.tree.*;

import com.sun.tools.javac.code.Symbol.*;
import com.sun.tools.javac.code.Type.*;
import com.sun.tools.javac.jvm.Code.*;
import com.sun.tools.javac.jvm.Items.*;
import com.sun.tools.javac.tree.JCTree.*;

import static com.sun.tools.javac.code.Flags.*;
import static com.sun.tools.javac.code.Kinds.*;
import static com.sun.tools.javac.code.TypeTags.*;
import static com.sun.tools.javac.jvm.ByteCodes.*;
import static com.sun.tools.javac.jvm.CRTFlags.*;

/** This pass maps flat Java (i.e. without inner classes) to bytecodes.
 *
 *  <p><b>This is NOT part of any API supported by Sun Microsystems.  If
 *  you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 */
@Version("@(#)Gen.java	1.148 07/03/21")
public class Gen extends JCTree.Visitor {
    private static my.Debug DEBUG=new my.Debug(my.Debug.Gen);//我加上的
	
    protected static final Context.Key<Gen> genKey =
	new Context.Key<Gen>();

    private final Log log;
    private final Symtab syms;
    private final Check chk;
    private final Resolve rs;
    private final TreeMaker make;
    private final Name.Table names;
    private final Target target;
    private final Type stringBufferType;
    private final Map<Type,Symbol> stringBufferAppend;
    private Name accessDollar;
    private final Types types;

    /** Switch: GJ mode?
     */
    private final boolean allowGenerics;

    /** Set when Miranda method stubs are to be generated. */
    private final boolean generateIproxies;

    /** Format of stackmap tables to be generated. */
    private final Code.StackMapFormat stackMap;
    
    /** A type that serves as the expected type for all method expressions.
     */
    private final Type methodType;

    public static Gen instance(Context context) {
		Gen instance = context.get(genKey);
		if (instance == null)
			instance = new Gen(context);
		return instance;
    }

    protected Gen(Context context) {
		DEBUG.P(this,"Gen(1)");
		context.put(genKey, this);

		names = Name.Table.instance(context);
		log = Log.instance(context);
		syms = Symtab.instance(context);
		chk = Check.instance(context);
		rs = Resolve.instance(context);
		make = TreeMaker.instance(context);
		target = Target.instance(context);
		types = Types.instance(context);
		methodType = new MethodType(null, null, null, syms.methodClass);
		allowGenerics = Source.instance(context).allowGenerics();
		stringBufferType = target.useStringBuilder()
			? syms.stringBuilderType
			: syms.stringBufferType;
		stringBufferAppend = new HashMap<Type,Symbol>();
		accessDollar = names.
			fromString("access" + target.syntheticNameChar());

		Options options = Options.instance(context);
		lineDebugInfo =
			options.get("-g:") == null ||
			options.get("-g:lines") != null;
		varDebugInfo =
			options.get("-g:") == null
			? options.get("-g") != null
			: options.get("-g:vars") != null;
		genCrt = options.get("-Xjcov") != null;
		debugCode = options.get("debugcode") != null;

		generateIproxies =
			target.requiresIproxy() ||
			options.get("miranda") != null;

		if (target.generateStackMapTable()) {
			// ignore cldc because we cannot have both stackmap formats
			this.stackMap = StackMapFormat.JSR202;
		} else {	    
			if (target.generateCLDCStackmap()) {
				this.stackMap = StackMapFormat.CLDC;
			} else {
				this.stackMap = StackMapFormat.NONE;
			}
		}
		
		// by default, avoid jsr's for simple finalizers
		int setjsrlimit = 50;
		String jsrlimitString = options.get("jsrlimit");
		if (jsrlimitString != null) {
			try {
				setjsrlimit = Integer.parseInt(jsrlimitString);
			} catch (NumberFormatException ex) {
				// ignore ill-formed numbers for jsrlimit
			}
		}
		this.jsrlimit = setjsrlimit;

		this.useJsrLocally = false; // reset in visitTry
		DEBUG.P(0,this,"Gen(1)");
    }

    /** Switches
     */
    private final boolean lineDebugInfo;
    private final boolean varDebugInfo;
    private final boolean genCrt;
    private final boolean debugCode;

    /** Default limit of (approximate) size of finalizer to inline.
     *  Zero means always use jsr.  100 or greater means never use
     *  jsr.
     */
    private final int jsrlimit;
    
    /** True if jsr is used.
     */
    private boolean useJsrLocally;
    
    /* Constant pool, reset by genClass.
     */
    private Pool pool = new Pool();

    /** Code buffer, set by genMethod.
     */
    private Code code;

    /** Items structure, set by genMethod.
     */
    private Items items;

    /** Environment for symbol lookup, set by genClass
     */
    private Env<AttrContext> attrEnv;

    /** The top level tree.
     */
    private JCCompilationUnit toplevel;

    /** The number of code-gen errors in this class.
     */
    private int nerrs = 0;

    /** A hash table mapping syntax trees to their ending source positions.
     */
    private Map<JCTree, Integer> endPositions;

    /** Generate code to load an integer constant.
     *  @param n     The integer to be loaded.
     */
    void loadIntConst(int n) {
		DEBUG.P(this,"loadIntConst(1)");
		DEBUG.P("n="+n);

        items.makeImmediateItem(syms.intType, n).load();

		DEBUG.P(0,this,"loadIntConst(1)");
    }

    /** The opcode that loads a zero constant of a given type code.
     *  @param tc   The given type code (@see ByteCode).
     */
    public static int zero(int tc) {
        switch(tc) {
			case INTcode: case BYTEcode: case SHORTcode: case CHARcode:
				return iconst_0;
			case LONGcode:
				return lconst_0;
			case FLOATcode:
				return fconst_0;
			case DOUBLEcode:
				return dconst_0;
			default:
				throw new AssertionError("zero");
        }
    }

    /** The opcode that loads a one constant of a given type code.
     *  @param tc   The given type code (@see ByteCode).
     */
    public static int one(int tc) {
        return zero(tc) + 1;
    }

    /** Generate code to load -1 of the given type code (either int or long).
     *  @param tc   The given type code (@see ByteCode).
     */
    void emitMinusOne(int tc) {
        if (tc == LONGcode) {
			items.makeImmediateItem(syms.longType, new Long(-1)).load();
		} else {
			code.emitop0(iconst_m1);
		}
    }

    /** Construct a symbol to reflect the qualifying type that should
     *  appear in the byte code as per JLS 13.1.
     *
     *  For target >= 1.2: Clone a method with the qualifier as owner (except
     *  for those cases where we need to work around VM bugs).
     *
     *  For target <= 1.1: If qualified variable or method is defined in a
     *  non-accessible class, clone it with the qualifier class as owner.
     *
     *  @param sym    The accessed symbol
     *  @param site   The qualifier's type.
     */
    Symbol binaryQualifier(Symbol sym, Type site) {
		try {//我加上的
		DEBUG.P(this,"binaryQualifier(Symbol sym, Type site)");
		DEBUG.P("sym="+sym);
		DEBUG.P("site="+site+" site.tag="+TypeTags.toString(site.tag));

		if (site.tag == ARRAY) {
			if (sym == syms.lengthVar ||
			sym.owner != syms.arrayClass)
				return sym;
			// array clone can be qualified by the array type in later targets
			Symbol qualifier = target.arrayBinaryCompatibility()
			? new ClassSymbol(Flags.PUBLIC, site.tsym.name,
					  site, syms.noSymbol)
			: syms.objectType.tsym;
			return sym.clone(qualifier);
		}

		DEBUG.P("");
		DEBUG.P("sym.owner="+sym.owner);
		DEBUG.P("site.tsym="+site.tsym);
		DEBUG.P("sym.flags()="+Flags.toString(sym.flags()));

		if (sym.owner == site.tsym ||
			(sym.flags() & (STATIC | SYNTHETIC)) == (STATIC | SYNTHETIC)) {
			return sym;
		}

		DEBUG.P("");
		DEBUG.P("target.obeyBinaryCompatibility()="+target.obeyBinaryCompatibility());
		if (!target.obeyBinaryCompatibility())
			return rs.isAccessible(attrEnv, (TypeSymbol)sym.owner)
			? sym
			: sym.clone(site.tsym);

		DEBUG.P("");
		DEBUG.P("target.interfaceFieldsBinaryCompatibility()="+target.interfaceFieldsBinaryCompatibility());
		if (!target.interfaceFieldsBinaryCompatibility()) {
			if ((sym.owner.flags() & INTERFACE) != 0 && sym.kind == VAR)
			return sym;
		}

		// leave alone methods inherited from Object
		// JLS2 13.1.
		if (sym.owner == syms.objectType.tsym)
			return sym;

		DEBUG.P("");
		DEBUG.P("target.interfaceObjectOverridesBinaryCompatibility()="+target.interfaceObjectOverridesBinaryCompatibility());
		if (!target.interfaceObjectOverridesBinaryCompatibility()) {
			if ((sym.owner.flags() & INTERFACE) != 0 &&
			syms.objectType.tsym.members().lookup(sym.name).scope != null)
			return sym;
		}

		return sym.clone(site.tsym);
		
		}finally{//我加上的
		DEBUG.P(0,this,"binaryQualifier(Symbol sym, Type site)");
		}
    }

    /** Insert a reference to given type in the constant pool,
     *  checking for an array with too many dimensions;
     *  return the reference's index.
     *  @param type   The type for which a reference is inserted.
     */
    int makeRef(DiagnosticPosition pos, Type type) {
		try {//我加上的
		DEBUG.P(this,"makeRef(2)");
		DEBUG.P("type="+type+"  type.tag="+TypeTags.toString(type.tag));

		checkDimension(pos, type);
		return pool.put(type.tag == CLASS ? (Object)type.tsym : (Object)type);
		
		}finally{//我加上的
		DEBUG.P(0,this,"makeRef(2)");
		}
    }

    /** Check if the given type is an array with too many dimensions.
     */
    private void checkDimension(DiagnosticPosition pos, Type t) {
		switch (t.tag) {
		case METHOD:
			checkDimension(pos, t.getReturnType());
			for (List<Type> args = t.getParameterTypes(); args.nonEmpty(); args = args.tail)
			checkDimension(pos, args.head);
			break;
		case ARRAY:
		//数组维数不能大于ClassFile.MAX_DIMENSIONS(255)
			if (types.dimensions(t) > ClassFile.MAX_DIMENSIONS) {
			log.error(pos, "limit.dimensions");
			nerrs++;
			}
			break;
		default:
			break;
		}
    }

    /** Create a tempory variable.
     *  @param type   The variable's type.
     */
    LocalItem makeTemp(Type type) {
		try {//我加上的
		DEBUG.P(this,"makeTemp(1)");
		DEBUG.P("type="+type);
		VarSymbol v = new VarSymbol(Flags.SYNTHETIC,
						names.empty,
						type,
						env.enclMethod.sym);
		code.newLocal(v);
		return items.makeLocalItem(v);

		}finally{//我加上的
		DEBUG.P(0,this,"makeTemp(1)");
		}
    }

    /** Generate code to call a non-private method or constructor.
     *  @param pos         Position to be used for error reporting.
     *  @param site        The type of which the method is a member.
     *  @param name        The method's name.
     *  @param argtypes    The method's argument types.
     *  @param isStatic    A flag that indicates whether we call a
     *                     static or instance method.
     */
    void callMethod(DiagnosticPosition pos,
		    Type site, Name name, List<Type> argtypes,
		    boolean isStatic) {
		DEBUG.P(this,"callMethod(4)");
		DEBUG.P("site="+site);
		DEBUG.P("name="+name);
		DEBUG.P("argtypes="+argtypes);
		DEBUG.P("isStatic="+isStatic);

		Symbol msym = rs.
			resolveInternalMethod(pos, attrEnv, site, name, argtypes, null);
		if (isStatic) items.makeStaticItem(msym).invoke();
		else items.makeMemberItem(msym, name == names.init).invoke();

		DEBUG.P(0,this,"callMethod(4)");
    }

    /** Is the given method definition an access method
     *  resulting from a qualified super? This is signified by an odd
     *  access code.
     */
    private boolean isAccessSuper(JCMethodDecl enclMethod) {
		return
			(enclMethod.mods.flags & SYNTHETIC) != 0 &&
			isOddAccessName(enclMethod.name);
    }

    /** Does given name start with "access$" and end in an odd digit?
     */
    private boolean isOddAccessName(Name name) {
        //name的最后一个byte与1进行“按位与”运算后如果等于1就是一个基数
		return
			name.startsWith(accessDollar) &&
			(name.byteAt(name.len - 1) & 1) == 1;
    }

/* ************************************************************************
 * Non-local exits
 *************************************************************************/

    /** Generate code to invoke the finalizer associated with given
     *  environment.
     *  Any calls to finalizers are appended to the environments `cont' chain.
     *  Mark beginning of gap in catch all range for finalizer.
     */
    void genFinalizer(Env<GenContext> env) {
		DEBUG.P(this,"genFinalizer(1)");
		DEBUG.P("env.info前="+env.info);

		if (code.isAlive() && env.info.finalize != null)
			env.info.finalize.gen();

		DEBUG.P("env.info后="+env.info);
		DEBUG.P(0,this,"genFinalizer(1)");
    }

    /** Generate code to call all finalizers of structures aborted by
     *  a non-local
     *  exit.  Return target environment of the non-local exit.
     *  @param target      The tree representing the structure that's aborted
     *  @param env         The environment current at the non-local exit.
     */
    Env<GenContext> unwind(JCTree target, Env<GenContext> env) {
		DEBUG.P(this,"unwind(2)");
		DEBUG.P("target="+target);
		DEBUG.P("env="+env);
		
		Env<GenContext> env1 = env;
		while (true) {
			genFinalizer(env1);
			if (env1.tree == target) break;
			env1 = env1.next;
		}
		
		DEBUG.P("env1="+env1);
		DEBUG.P(0,this,"unwind(2)");
		return env1;
    }

    /** Mark end of gap in catch-all range for finalizer.
     *  @param env   the environment which might contain the finalizer
     *               (if it does, env.info.gaps != null).
     */
    void endFinalizerGap(Env<GenContext> env) {
    	DEBUG.P(this,"endFinalizerGap(1)");
		DEBUG.P("env.info前="+env.info);
    	
        if (env.info.gaps != null && env.info.gaps.length() % 2 == 1)
            env.info.gaps.append(code.curPc());
        
		DEBUG.P("env.info后="+env.info);
        DEBUG.P(0,this,"endFinalizerGap(1)");
    }

    /** Mark end of all gaps in catch-all ranges for finalizers of environments
     *  lying between, and including to two environments.
     *  @param from    the most deeply nested environment to mark
     *  @param to      the least deeply nested environment to mark
     */
    void endFinalizerGaps(Env<GenContext> from, Env<GenContext> to) {
		DEBUG.P(this,"endFinalizerGaps(2)");
		
		Env<GenContext> last = null;
		while (last != to) {
			endFinalizerGap(from);
			last = from;
			from = from.next;
		}
		
		DEBUG.P(0,this,"endFinalizerGaps(2)");
    }

    /** Do any of the structures aborted by a non-local exit have
     *  finalizers that require an empty stack?
     *  @param target      The tree representing the structure that's aborted
     *  @param env         The environment current at the non-local exit.
     */
    boolean hasFinally(JCTree target, Env<GenContext> env) {
		boolean hasFinally=true;//我加上的
		try {//我加上的
		DEBUG.P(this,"hasFinally(2)");

		while (env.tree != target) {
			if (env.tree.tag == JCTree.TRY && env.info.finalize.hasFinalizer())
				return true;
			env = env.next;
		}

		hasFinally=false;//我加上的

		return false;

		}finally{//我加上的
		DEBUG.P("hasFinally="+hasFinally);
		DEBUG.P(0,this,"hasFinally(2)");
		}
    }

/* ************************************************************************
 * Normalizing class-members.
 *************************************************************************/

    /** Distribute member initializer code into constructors and <clinit>
     *  method.
     *  @param defs         The list of class member declarations.
     *  @param c            The enclosing class.
     */
    List<JCTree> normalizeDefs(List<JCTree> defs, ClassSymbol c) {
		DEBUG.P(this,"normalizeDefs(2)");
		DEBUG.P("c="+c);
		
		ListBuffer<JCStatement> initCode = new ListBuffer<JCStatement>();
		ListBuffer<JCStatement> clinitCode = new ListBuffer<JCStatement>();
		ListBuffer<JCTree> methodDefs = new ListBuffer<JCTree>();
		// Sort definitions into three listbuffers:
		//  - initCode for instance initializers
		//  - clinitCode for class initializers
		//  - methodDefs for method definitions
		for (List<JCTree> l = defs; l.nonEmpty(); l = l.tail) {
			JCTree def = l.head;
			DEBUG.P("");
			DEBUG.P("def.tag="+def.myTreeTag());
			switch (def.tag) {
				case JCTree.BLOCK:
					JCBlock block = (JCBlock)def;
					DEBUG.P("block.flags="+Flags.toString(block.flags));
					if ((block.flags & STATIC) != 0)
						clinitCode.append(block);
					else
						initCode.append(block);
						break;
				case JCTree.METHODDEF:
					methodDefs.append(def);
					break;
				case JCTree.VARDEF:
					JCVariableDecl vdef = (JCVariableDecl) def;
					VarSymbol sym = vdef.sym;
					DEBUG.P("sym="+sym);
					DEBUG.P("vdef.init="+vdef.init);
					checkDimension(vdef.pos(), sym.type);//检查变量的类型是否是多维数组，如果是，则维数不能大于255
					if (vdef.init != null) {
						DEBUG.P("");
						DEBUG.P("sym.getConstValue()="+sym.getConstValue());
						DEBUG.P("sym.flags()="+Flags.toString(sym.flags()));
						if ((sym.flags() & STATIC) == 0) {
							// Always initialize instance variables.
							JCStatement init = make.at(vdef.pos()).
								Assignment(sym, vdef.init);
							initCode.append(init);
							if (endPositions != null) {
								Integer endPos = endPositions.remove(vdef);
								if (endPos != null) endPositions.put(init, endPos);
							}
						} else if (sym.getConstValue() == null) {
						// Initialize class (static) variables only if
							// they are not compile-time constants.
							JCStatement init = make.at(vdef.pos).
								Assignment(sym, vdef.init);

							DEBUG.P("");
							DEBUG.P("init="+init);
							clinitCode.append(init);
							if (endPositions != null) {
								Integer endPos = endPositions.remove(vdef);
								if (endPos != null) endPositions.put(init, endPos);
							}
						} else {//只有已初始化的static final类型变量才是compile-time constants
							checkStringConstant(vdef.init.pos(), sym.getConstValue());
						}
					}
					break;
				default:
					assert false;
			}
		}
		
		DEBUG.P(2);
		DEBUG.P("initCode="+initCode.toList());
		DEBUG.P("clinitCode="+clinitCode.toList());
		// Insert any instance initializers into all constructors.
		if (initCode.length() != 0) {
			List<JCStatement> inits = initCode.toList();
			for (JCTree t : methodDefs) {
				normalizeMethod((JCMethodDecl)t, inits);
			}
		}
		// If there are class initializers, create a <clinit> method
		// that contains them as its body.
		if (clinitCode.length() != 0) {
			MethodSymbol clinit = new MethodSymbol(
			STATIC, names.clinit,
			new MethodType(
				List.<Type>nil(), syms.voidType,
				List.<Type>nil(), syms.methodClass),
			c);
			c.members().enter(clinit);
			List<JCStatement> clinitStats = clinitCode.toList();
			JCBlock block = make.at(clinitStats.head.pos()).Block(0, clinitStats);
			block.endpos = TreeInfo.endPos(clinitStats.last());
			methodDefs.append(make.MethodDef(clinit, block));
			DEBUG.P("c.members()="+c.members());
		}
		DEBUG.P(0,this,"normalizeDefs(2)");
		// Return all method definitions.
		return methodDefs.toList();
    }

    /** Check a constant value and report if it is a string that is
     *  too large.
     */
    private void checkStringConstant(DiagnosticPosition pos, Object constValue) {
		try {//我加上的
		DEBUG.P(this,"checkStringConstant(2)");
		DEBUG.P("nerrs="+nerrs+" constValue="+constValue);

		if (nerrs != 0 || // only complain about a long string once
			constValue == null ||
			!(constValue instanceof String) ||
			((String)constValue).length() < Pool.MAX_STRING_LENGTH)
			return;
		log.error(pos, "limit.string");
		nerrs++;
		
		}finally{//我加上的
		DEBUG.P(0,this,"checkStringConstant(2)");
		}
    }

    /** Insert instance initializer code into initial constructor.
     *  @param md        The tree potentially representing a
     *                   constructor's definition.
     *  @param initCode  The list of instance initializer statements.
     */
    void normalizeMethod(JCMethodDecl md, List<JCStatement> initCode) {
		/*
		//注意:只将initCod插入第一条语句不是this()调用的构造方法中

		对于如下源代码:
		------------------------------------
		public class Test {
			int fieldA=10;
			{
				fieldA=20;
			}

			{
				fieldB=20;
			}
			int fieldB=10;
		}
		------------------------------------

		经过编译器调整后，看起来像这样:
		------------------------------------
		public class Test {
			Test() {
				fieldA=10;
				fieldA=20;

				fieldB=20;
				fieldB=10;
			}
		}
		------------------------------------
		最终fieldA的值是20,fieldB的值是10，说明了一点，语句块与变量初始化语句
		在源代码中的顺序决定了变量的最终取值
		*/
		DEBUG.P(this,"normalizeMethod(2)");
		DEBUG.P("md.name="+md.name);
		DEBUG.P("isInitialConstructor="+TreeInfo.isInitialConstructor(md));
		if (md.name == names.init && TreeInfo.isInitialConstructor(md)) {
			DEBUG.P("JCMethodDecl md旧="+md);
			// We are seeing a constructor that does not call another
			// constructor of the same class.
			List<JCStatement> stats = md.body.stats;
			ListBuffer<JCStatement> newstats = new ListBuffer<JCStatement>();

			if (stats.nonEmpty()) {
			// Copy initializers of synthetic variables generated in
			// the translation of inner classes.
			while (TreeInfo.isSyntheticInit(stats.head)) {
						
						DEBUG.P("while1->stats.head="+stats.head);
				newstats.append(stats.head);
				stats = stats.tail;
			}
			// Copy superclass constructor call
			newstats.append(stats.head);
					DEBUG.P("stats.head="+stats.head);
			stats = stats.tail;
			// Copy remaining synthetic initializers.
			while (stats.nonEmpty() &&
				   TreeInfo.isSyntheticInit(stats.head)) {
						
						DEBUG.P("while2->stats.head="+stats.head);
				newstats.append(stats.head);
				stats = stats.tail;
			}
			// Now insert the initializer code.
			newstats.appendList(initCode);
			// And copy all remaining statements.
			while (stats.nonEmpty()) {
				newstats.append(stats.head);
				stats = stats.tail;
			}
			}
			md.body.stats = newstats.toList();
			DEBUG.P("JCMethodDecl md新="+md);
			if (md.body.endpos == Position.NOPOS)
			md.body.endpos = TreeInfo.endPos(md.body.stats.last());
		}
		DEBUG.P(0,this,"normalizeMethod(2)");
    }

/* ********************************************************************
 * Adding miranda methods
 *********************************************************************/

    /** Add abstract methods for all methods defined in one of
     *  the interfaces of a given class,
     *  provided they are not already implemented in the class.
     *
     *  @param c      The class whose interfaces are searched for methods
     *                for which Miranda methods should be added.
     */
    void implementInterfaceMethods(ClassSymbol c) {
        DEBUG.P(this,"implementInterfaceMethods(1)");
		implementInterfaceMethods(c, c);
        DEBUG.P(0,this,"implementInterfaceMethods(1)");
    }

    /** Add abstract methods for all methods defined in one of
     *  the interfaces of a given class,
     *  provided they are not already implemented in the class.
     *
     *  @param c      The class whose interfaces are searched for methods
     *                for which Miranda methods should be added.
     *  @param site   The class in which a definition may be needed.
     */
    void implementInterfaceMethods(ClassSymbol c, ClassSymbol site) {
        DEBUG.P(this,"implementInterfaceMethods(2)");
        DEBUG.P("c="+c);
        DEBUG.P("site="+site);
		for (List<Type> l = types.interfaces(c.type); l.nonEmpty(); l = l.tail) {
			ClassSymbol i = (ClassSymbol)l.head.tsym;
			for (Scope.Entry e = i.members().elems;
			 e != null;
			 e = e.sibling)
			{
					DEBUG.P("e.sym="+e.sym);
			if (e.sym.kind == MTH && (e.sym.flags() & STATIC) == 0)
			{
				MethodSymbol absMeth = (MethodSymbol)e.sym;
				MethodSymbol implMeth = absMeth.binaryImplementation(site, types);
				
						DEBUG.P("implMeth="+implMeth);
						if (implMeth == null)
				addAbstractMethod(site, absMeth);
						//????????如何得到IPROXY?????????/
				else if ((implMeth.flags() & IPROXY) != 0)
				adjustAbstractMethod(site, implMeth, absMeth);
			}
			}
			implementInterfaceMethods(i, site);
		}

        DEBUG.P(0,this,"implementInterfaceMethods(2)");
    }

    /** Add an abstract methods to a class
     *  which implicitly implements a method defined in some interface
     *  implemented by the class. These methods are called "Miranda methods".
     *  Enter the newly created method into its enclosing class scope.
     *  Note that it is not entered into the class tree, as the emitter
     *  doesn't need to see it there to emit an abstract method.
     *
     *  @param c      The class to which the Miranda method is added.
     *  @param m      The interface method symbol for which a Miranda method
     *                is added.
     */
    private void addAbstractMethod(ClassSymbol c,
				   MethodSymbol m) {
        DEBUG.P(this,"addAbstractMethod(2)");
        
        DEBUG.P("c="+c);
        DEBUG.P("m="+m);
        
		MethodSymbol absMeth = new MethodSymbol(
			m.flags() | IPROXY | SYNTHETIC, m.name,
			m.type, // was c.type.memberType(m), but now only !generics supported
			c);
			
			DEBUG.P("absMeth.flags()="+Flags.toString(absMeth.flags()));
		c.members().enter(absMeth); // add to symbol table
        
        DEBUG.P("c.members()="+c.members());
        DEBUG.P(0,this,"addAbstractMethod(2)");
    }

    private void adjustAbstractMethod(ClassSymbol c,
				      MethodSymbol pm,
				      MethodSymbol im) {
        DEBUG.P(this,"adjustAbstractMethod(3)");
        
        DEBUG.P("c="+c);//c是实现类
        DEBUG.P("pm="+pm);//实现类c中的方法
        DEBUG.P("im="+im);//im是接口中的方法
        
        MethodType pmt = (MethodType)pm.type;
        Type imt = types.memberType(c.type, im);
		pmt.thrown = chk.intersect(pmt.getThrownTypes(), imt.getThrownTypes());
        
        DEBUG.P(0,this,"adjustAbstractMethod(3)");
    }

/* ************************************************************************
 * Traversal methods
 *************************************************************************/

    /** Visitor argument: The current environment.
     */
    Env<GenContext> env;

    /** Visitor argument: The expected type (prototype).
     */
    Type pt;

    /** Visitor result: The item representing the computed value.
     */
    Item result;

    /** Visitor method: generate code for a definition, catching and reporting
     *  any completion failures.
     *  @param tree    The definition to be visited.
     *  @param env     The environment current at the definition.
     */
    public void genDef(JCTree tree, Env<GenContext> env) {
        DEBUG.P(this,"genDef(2)");
        DEBUG.P("env="+env);
		DEBUG.P("tree.tag="+tree.myTreeTag());
		DEBUG.P("tree="+tree);
		
		Env<GenContext> prevEnv = this.env;
		try {
			this.env = env;
			tree.accept(this);
		} catch (CompletionFailure ex) {
			chk.completionError(tree.pos(), ex);
		} finally {
			this.env = prevEnv;
			DEBUG.P(0,this,"genDef(2)");
		}
    }

    /** Derived visitor method: check whether CharacterRangeTable
     *  should be emitted, if so, put a new entry into CRTable
     *  and call method to generate bytecode.
     *  If not, just call method to generate bytecode.
     *  @see    #genStat(Tree, Env)
     *
     *  @param  tree     The tree to be visited.
     *  @param  env      The environment to use.
     *  @param  crtFlags The CharacterRangeTable flags
     *                   indicating type of the entry.
     */
    public void genStat(JCTree tree, Env<GenContext> env, int crtFlags) {
		try {//我加上的
		DEBUG.P(this,"genStat(3)");
		DEBUG.P("env="+env);
		DEBUG.P("genCrt="+genCrt);
		if(code.crt!=null) DEBUG.P("crtFlags="+code.crt.getTypes(crtFlags));
		
		if (!genCrt) {
			genStat(tree, env);
			return;
		}
		int startpc = code.curPc();
		genStat(tree, env);
		if (tree.tag == JCTree.BLOCK) crtFlags |= CRT_BLOCK;
		code.crt.put(tree, crtFlags, startpc, code.curPc());
		
		}finally{//我加上的
		DEBUG.P(0,this,"genStat(3)");
		}
    }

    /** Derived visitor method: generate code for a statement.
     */
    public void genStat(JCTree tree, Env<GenContext> env) {
		DEBUG.P(this,"genStat(2)");
		DEBUG.P("code.isAlive()="+code.isAlive());
		DEBUG.P("env.info.isSwitch="+env.info.isSwitch);

		if (code.isAlive()) {
			code.statBegin(tree.pos);
			genDef(tree, env);
		} else if (env.info.isSwitch && tree.tag == JCTree.VARDEF) {
			// variables whose declarations are in a switch
			// can be used even if the decl is unreachable.
			code.newLocal(((JCVariableDecl) tree).sym);
		}
		
		DEBUG.P(0,this,"genStat(2)");
    }

    /** Derived visitor method: check whether CharacterRangeTable
     *  should be emitted, if so, put a new entry into CRTable
     *  and call method to generate bytecode.
     *  If not, just call method to generate bytecode.
     *  @see    #genStats(List, Env)
     *
     *  @param  trees    The list of trees to be visited.
     *  @param  env      The environment to use.
     *  @param  crtFlags The CharacterRangeTable flags
     *                   indicating type of the entry.
     */
    public void genStats(List<JCStatement> trees, Env<GenContext> env, int crtFlags) {
		try {//我加上的
		DEBUG.P(this,"genStats(3)");
		DEBUG.P("env="+env);
		if(trees!=null) DEBUG.P("trees.size="+trees.size());
		else DEBUG.P("trees=null");
		DEBUG.P("genCrt="+genCrt);
		if(code.crt!=null) DEBUG.P("crtFlags="+code.crt.getTypes(crtFlags));
		
		if (!genCrt) {
			genStats(trees, env);
			return;
		}
		if (trees.length() == 1) {        // mark one statement with the flags
			genStat(trees.head, env, crtFlags | CRT_STATEMENT);
		} else {
			int startpc = code.curPc();
			genStats(trees, env);
			code.crt.put(trees, crtFlags, startpc, code.curPc());
		}
		
		}finally{//我加上的
		DEBUG.P(0,this,"genStats(3)");
		}
    }

    /** Derived visitor method: generate code for a list of statements.
     */
    public void genStats(List<? extends JCTree> trees, Env<GenContext> env) {
		DEBUG.P(this,"genStats(2)");
		DEBUG.P("env="+env);
		if(trees!=null) DEBUG.P("trees.size="+trees.size());
		else DEBUG.P("trees=null");
		
		for (List<? extends JCTree> l = trees; l.nonEmpty(); l = l.tail)
			genStat(l.head, env, CRT_STATEMENT);
		
		DEBUG.P(0,this,"genStats(2)");    
    }

    /** Derived visitor method: check whether CharacterRangeTable
     *  should be emitted, if so, put a new entry into CRTable
     *  and call method to generate bytecode.
     *  If not, just call method to generate bytecode.
     *  @see    #genCond(Tree,boolean)
     *
     *  @param  tree     The tree to be visited.
     *  @param  crtFlags The CharacterRangeTable flags
     *                   indicating type of the entry.
     */
    public CondItem genCond(JCTree tree, int crtFlags) {
		try {//我加上的
		DEBUG.P(this,"genCond(2)");
		DEBUG.P("genCrt="+genCrt);
		if(code.crt!=null) DEBUG.P("crtFlags="+code.crt.getTypes(crtFlags));
		
		if (!genCrt) return genCond(tree, false);
		int startpc = code.curPc();
		CondItem item = genCond(tree, (crtFlags & CRT_FLOW_CONTROLLER) != 0);
		code.crt.put(tree, crtFlags, startpc, code.curPc());
		return item;
		
		}finally{//我加上的
		DEBUG.P(0,this,"genCond(2)");
		}
    }

    /** Derived visitor method: generate code for a boolean
     *  expression in a control-flow context.
     *  @param _tree         The expression to be visited.
     *  @param markBranches The flag to indicate that the condition is
     *                      a flow controller so produced conditions
     *                      should contain a proper tree to generate
     *                      CharacterRangeTable branches for them.
     */
    public CondItem genCond(JCTree _tree, boolean markBranches) {
		try {//我加上的
		DEBUG.P(this,"genCond(JCTree _tree, boolean markBranches)");
		DEBUG.P("markBranches="+markBranches);
		DEBUG.P("_tree="+_tree);
		
		JCTree inner_tree = TreeInfo.skipParens(_tree);
		DEBUG.P("inner_tree="+_tree);
		DEBUG.P("inner_tree.tag="+inner_tree.myTreeTag());

		if (inner_tree.tag == JCTree.CONDEXPR) {
			JCConditional tree = (JCConditional)inner_tree;
			CondItem cond = genCond(tree.cond, CRT_FLOW_CONTROLLER);
			
			DEBUG.P("cond="+cond);
			DEBUG.P("cond.isTrue() ="+cond.isTrue());
			DEBUG.P("cond.isFalse()="+cond.isFalse());
			if (cond.isTrue()) {
				code.resolve(cond.trueJumps);
				CondItem result = genCond(tree.truepart, CRT_FLOW_TARGET);
				if (markBranches) result.tree = tree.truepart;
				return result;
			}
			if (cond.isFalse()) {
				code.resolve(cond.falseJumps);
				CondItem result = genCond(tree.falsepart, CRT_FLOW_TARGET);
				if (markBranches) result.tree = tree.falsepart;
				return result;
			}

			Chain secondJumps = cond.jumpFalse();
			DEBUG.P("secondJumps="+secondJumps);

			code.resolve(cond.trueJumps);
			CondItem first = genCond(tree.truepart, CRT_FLOW_TARGET);
			if (markBranches) first.tree = tree.truepart;
			DEBUG.P("first="+first);

			Chain falseJumps = first.jumpFalse();
			DEBUG.P("falseJumps="+falseJumps);

			code.resolve(first.trueJumps);
			Chain trueJumps = code.branch(goto_);
			DEBUG.P("trueJumps="+trueJumps);

			code.resolve(secondJumps);
			CondItem second = genCond(tree.falsepart, CRT_FLOW_TARGET);
			DEBUG.P("second="+second);
			CondItem result = items.makeCondItem(second.opcode,
						  code.mergeChains(trueJumps, second.trueJumps),
						  code.mergeChains(falseJumps, second.falseJumps));
			if (markBranches) result.tree = tree.falsepart;
			return result;
		} else {
			CondItem result = genExpr(_tree, syms.booleanType).mkCond();
			if (markBranches) result.tree = _tree;
			
			DEBUG.P("result="+result);
			return result;
		}
		
		}finally{//我加上的
		DEBUG.P(0,this,"genCond(JCTree _tree, boolean markBranches)");
		}
    }

    /** Visitor method: generate code for an expression, catching and reporting
     *  any completion failures.
     *  @param tree    The expression to be visited.
     *  @param pt      The expression's expected type (proto-type).
     */
    public Item genExpr(JCTree tree, Type pt) {
        DEBUG.P(this,"genExpr(JCTree tree, Type pt)");
        DEBUG.P("tree="+tree);
        DEBUG.P("tree.type.constValue()="+tree.type.constValue());
        DEBUG.P("pt="+pt);
        Type prevPt = this.pt;

        Item myItemResult=null;//我加上的
		try {
			if (tree.type.constValue() != null) {
				// Short circuit any expressions which are constants
				checkStringConstant(tree.pos(), tree.type.constValue());
				result = items.makeImmediateItem(tree.type, tree.type.constValue());
			} else {
				DEBUG.P("tree.tag="+tree.myTreeTag());
				this.pt = pt;
				tree.accept(this);
			}
			
			myItemResult=result.coerce(pt);//我加上的
			return myItemResult;//我加上的
			//coerce(Type targettype),coerce(int targetcode)在Items.Item中定义,
			//只有Items.ImmediateItem覆盖了coerce(int targetcode)
			//return result.coerce(pt);
		} catch (CompletionFailure ex) {
			chk.completionError(tree.pos(), ex);
			code.state.stacksize = 1;
			return items.makeStackItem(pt);
		} finally {
			this.pt = prevPt;
			DEBUG.P("result="+result);
			DEBUG.P("myItemResult="+myItemResult);
			DEBUG.P("code.state="+code.state);
			DEBUG.P(0,this,"genExpr(JCTree tree, Type pt)");
		}
    }

    /** Derived visitor method: generate code for a list of method arguments.
     *  @param trees    The argument expressions to be visited.
     *  @param pts      The expression's expected types (i.e. the formal parameter
     *                  types of the invoked method).
     */
    public void genArgs(List<JCExpression> trees, List<Type> pts) {
		DEBUG.P(this,"genArgs(2)");
		DEBUG.P("trees="+trees);
		DEBUG.P("pts="+pts);
		for (List<JCExpression> l = trees; l.nonEmpty(); l = l.tail) {
			genExpr(l.head, pts.head).load();
			pts = pts.tail;
		}
		// require lists be of same length
		assert pts.isEmpty();
		DEBUG.P(0,this,"genArgs(2)");
    }

/* ************************************************************************
 * Visitor methods for statements and definitions
 *************************************************************************/

    /** Thrown when the byte code size exceeds limit.
     */
    public static class CodeSizeOverflow extends RuntimeException {
        private static final long serialVersionUID = 0;
        public CodeSizeOverflow() {}
    }
    
    public void visitMethodDef(JCMethodDecl tree) {
        DEBUG.P(this,"visitMethodDef(1)");
		// Create a new local environment that points pack at method
		// definition.
		Env<GenContext> localEnv = env.dup(tree);
		localEnv.enclMethod = tree;
		
		DEBUG.P("localEnv="+localEnv);

		// The expected type of every return statement in this method
		// is the method's return type.
		this.pt = tree.sym.erasure(types).getReturnType();
		DEBUG.P("tree.sym="+tree.sym);
		DEBUG.P("tree.sym.type="+tree.sym.type);
		DEBUG.P("this.pt="+this.pt);
		DEBUG.P("tree.sym.erasure(types)="+tree.sym.erasure(types));

		checkDimension(tree.pos(), tree.sym.erasure(types));
		genMethod(tree, localEnv, false);
		
		DEBUG.P(0,this,"visitMethodDef(1)");
    }
//where
        /** Generate code for a method.
	 *  @param tree     The tree representing the method definition.
	 *  @param env      The environment current for the method body.
	 *  @param fatcode  A flag that indicates whether all jumps are
	 *		    within 32K.  We first invoke this method under
	 *		    the assumption that fatcode == false, i.e. all
	 *		    jumps are within 32K.  If this fails, fatcode
	 *		    is set to true and we try again.
	 */
	//b10
	void genMethod(JCMethodDecl tree, Env<GenContext> env, boolean fatcode) {
		try {//我加上的
            DEBUG.P(this,"genMethod(3)");
            DEBUG.P("env="+env);
            DEBUG.P("fatcode="+fatcode);
			//DEBUG.P("tree.body="+tree.body);

			MethodSymbol meth = tree.sym;
			//    	System.err.println("Generating " + meth + " in " + meth.owner); //DEBUG
			
			/*
            由方法每个参数的type计算出所有参数所占的总字数(一个字是堆栈出入栈操作的基本单位)，
            (double和long类型的参数占两个字)
            如果是非静态方法(隐含this变量,在initCode方法中把this加到局部变量数组中)，
            那么总字数再加1，总字数必须小于ClassFile.MAX_PARAMETERS(255)
            */
			if (Code.width(types.erasure(env.enclMethod.sym.type).getParameterTypes())  +
			(((tree.mods.flags & STATIC) == 0 || meth.isConstructor()) ? 1 : 0) >
			ClassFile.MAX_PARAMETERS) {
				log.error(tree.pos(), "limit.parameters");
				nerrs++;
			}

			else if (tree.body != null) { //只有abstract方法时tree.body == null
				// Create a new code structure and initialize it.
				int startpcCrt = initCode(tree, env, fatcode);

				try {
                    genStat(tree.body, env);
                } catch (CodeSizeOverflow e) {
                    // Failed due to code limit, try again with jsr/ret
                    startpcCrt = initCode(tree, env, fatcode);
                    genStat(tree.body, env);
                }

				DEBUG.P("");
				DEBUG.P("code.state.stacksize="+code.state.stacksize);
				if (code.state.stacksize != 0) {
					log.error(tree.body.pos(), "stack.sim.error", tree);
					throw new AssertionError();
				}

				DEBUG.P("");
				DEBUG.P("code.isAlive()="+code.isAlive());

				// If last statement could complete normally, insert a
				// return at the end.
				if (code.isAlive()) {
					code.statBegin(TreeInfo.endPos(tree.body));
					if (env.enclMethod == null ||
					env.enclMethod.sym.type.getReturnType().tag == VOID) {
						code.emitop0(return_);
					} else {
						// sometime dead code seems alive (4415991);
						// generate a small loop instead
						int startpc = code.entryPoint();
						CondItem c = items.makeCondItem(goto_);
						code.resolve(c.jumpTrue(), startpc);
					}
				}
				if (genCrt)
					code.crt.put(tree.body,
						 CRT_BLOCK,
						 startpcCrt,
						 code.curPc());

				// End the scope of all local variables in variable info.
				code.endScopes(0);

				// If we exceeded limits, panic
				if (code.checkLimits(tree.pos(), log)) {
					nerrs++;
					return;
				}

				DEBUG.P("");
				DEBUG.P("fatcode="+fatcode);
				DEBUG.P("code.fatcode="+code.fatcode);

				// If we generated short code but got a long jump, do it again
				// with fatCode = true.
				if (!fatcode && code.fatcode) genMethod(tree, env, true);

				// Clean up
				if(stackMap == StackMapFormat.JSR202) {
					code.lastFrame = null;
					code.frameBeforeLast = null;
				}
			}

        }finally{//我加上的
            DEBUG.P(0,this,"genMethod(3)");
		}
	}

        private int initCode(JCMethodDecl tree, Env<GenContext> env, boolean fatcode) {
            try {//我加上的
            DEBUG.P(this,"initCode(3)");
            DEBUG.P("tree.sym="+tree.sym);
            DEBUG.P("env="+env);
            DEBUG.P("fatcode="+fatcode);
			DEBUG.P("lineDebugInfo="+lineDebugInfo);
			DEBUG.P("varDebugInfo="+varDebugInfo);
			DEBUG.P("stackMap="+stackMap);
			DEBUG.P("debugCode="+debugCode);
			DEBUG.P("genCrt="+genCrt);

            MethodSymbol meth = tree.sym;
            
            // Create a new code structure.
            meth.code = code = new Code(meth,
                                        fatcode, 
                                        lineDebugInfo ? toplevel.lineMap : null, 
                                        varDebugInfo,
                                        stackMap, 
                                        debugCode,
                                        genCrt ? new CRTable(tree, env.toplevel.endPositions) 
                                               : null,
                                        syms,
                                        types,
                                        pool);//常量池是所有方法共用的
            items = new Items(pool, code, syms, types);//每个方法都重新生成一个Items实例
            if (code.debugCode)
                System.err.println(meth + " for body " + tree);

            // If method is not static, create a new local variable address
            // for `this'.

            DEBUG.P("tree.mods.flags="+Flags.toString(tree.mods.flags));

            if ((tree.mods.flags & STATIC) == 0) {
                Type selfType = meth.owner.type;

                DEBUG.P("selfType="+selfType);
                DEBUG.P("meth.isConstructor()="+meth.isConstructor());

                if (meth.isConstructor() && selfType != syms.objectType)
                    selfType = UninitializedType.uninitializedThis(selfType);

                DEBUG.P("selfType="+selfType);
				
                //this变量在局部变量数组的索引总是0
                code.setDefined(
                        code.newLocal(
                            new VarSymbol(FINAL, names._this, selfType, meth.owner)));
            }

            // Mark all parameters as defined from the beginning of
            // the method.

            DEBUG.P("tree.params="+tree.params);

            for (List<JCVariableDecl> l = tree.params; l.nonEmpty(); l = l.tail) {
                checkDimension(l.head.pos(), l.head.sym.type);
                code.setDefined(code.newLocal(l.head.sym));
            }

            // Get ready to generate code for method body.
            int startpcCrt = genCrt ? code.curPc() : 0;
            code.entryPoint();

            // Suppress initial stackmap
            code.pendingStackMap = false;
            
            DEBUG.P("startpcCrt="+startpcCrt);

            return startpcCrt;

            }finally{//我加上的
            DEBUG.P(1,this,"initCode(3)");
            }
        }
        
    public void visitVarDef(JCVariableDecl tree) {
		DEBUG.P(this,"visitVarDef(1)");
		VarSymbol v = tree.sym;
		code.newLocal(v);

		/*
		final int myMethodInt; //tree.init==null
		final int myMethodInt2=100; //tree.init!=null 且getConstValue()==100
		int myMethodInt3=200; //tree.init!=null 但getConstValue()==null
		在方法中定义的final类型的且在定义时就被赋值的是编译时常量
		*/
		DEBUG.P("tree.init="+tree.init);
		if (tree.init != null) {
			checkStringConstant(tree.init.pos(), v.getConstValue());

			DEBUG.P("v.getConstValue()="+v.getConstValue());
			DEBUG.P("varDebugInfo="+varDebugInfo);
			if (v.getConstValue() == null || varDebugInfo) {
				genExpr(tree.init, v.erasure(types)).load();
				items.makeLocalItem(v).store();
			}
		}
		checkDimension(tree.pos(), v.type);
		DEBUG.P(0,this,"visitVarDef(1)");
    }

    public void visitSkip(JCSkip tree) {
    }

    public void visitBlock(JCBlock tree) {
		DEBUG.P(this,"visitBlock(JCBlock tree)");
		int limit = code.nextreg;
		DEBUG.P("limit="+limit);
		
		Env<GenContext> localEnv = env.dup(tree, new GenContext());
		genStats(tree.stats, localEnv);
		// End the scope of all block-local variables in variable info.

		DEBUG.P("");
		DEBUG.P("env.tree.tag="+env.tree.myTreeTag());
		if (env.tree.tag != JCTree.METHODDEF) {
            code.statBegin(tree.endpos);
            code.endScopes(limit);
            code.pendingStatPos = Position.NOPOS;
        }
		DEBUG.P(0,this,"visitBlock(JCBlock tree)");   
    }

    public void visitDoLoop(JCDoWhileLoop tree) {
		DEBUG.P(this,"visitDoLoop(1)");
		genLoop(tree, tree.body, tree.cond, List.<JCExpressionStatement>nil(), false);
		DEBUG.P(0,this,"visitDoLoop(1)");
    }

    public void visitWhileLoop(JCWhileLoop tree) {
		DEBUG.P(this,"visitWhileLoop(1)");
		genLoop(tree, tree.body, tree.cond, List.<JCExpressionStatement>nil(), true);
		DEBUG.P(0,this,"visitWhileLoop(1)");
    }

    public void visitForLoop(JCForLoop tree) {
		DEBUG.P(this,"visitForLoop(1)");
		int limit = code.nextreg;
		genStats(tree.init, env);
		genLoop(tree, tree.body, tree.cond, tree.step, true);
		code.endScopes(limit);
		DEBUG.P(0,this,"visitForLoop(1)");
    }
    //where
        /** Generate code for a loop.
		 *  @param loop       The tree representing the loop.
		 *  @param body       The loop's body.
		 *  @param cond       The loop's controling condition.
		 *  @param step       "Step" statements to be inserted at end of
		 *                    each iteration.
		 *  @param testFirst  True if the loop test belongs before the body.
		 */
        private void genLoop(JCStatement loop,
			     JCStatement body,
			     JCExpression cond,
			     List<JCExpressionStatement> step,
			     boolean testFirst) {
			DEBUG.P(this,"genLoop(1)");	 
			DEBUG.P("cond="+cond);
			DEBUG.P("testFirst="+testFirst);
					
			Env<GenContext> loopEnv = env.dup(loop, new GenContext());
			int startpc = code.entryPoint();
			if (testFirst) {
				CondItem c;
				if (cond != null) {
					code.statBegin(cond.pos);
					c = genCond(TreeInfo.skipParens(cond), CRT_FLOW_CONTROLLER);
				} else {
					c = items.makeCondItem(goto_);
				}
				Chain loopDone = c.jumpFalse();
				code.resolve(c.trueJumps);
				genStat(body, loopEnv, CRT_STATEMENT | CRT_FLOW_TARGET);
				code.resolve(loopEnv.info.cont);
				genStats(step, loopEnv);
				code.resolve(code.branch(goto_), startpc);
				code.resolve(loopDone);
			} else {
				genStat(body, loopEnv, CRT_STATEMENT | CRT_FLOW_TARGET);
				code.resolve(loopEnv.info.cont);
				genStats(step, loopEnv);
				CondItem c;
				if (cond != null) {
					code.statBegin(cond.pos);
					c = genCond(TreeInfo.skipParens(cond), CRT_FLOW_CONTROLLER);
				} else {
					c = items.makeCondItem(goto_);
				}
				//do-while语句生成的字节码比while语句生成的字节码高效，因为少了goto指令
				code.resolve(c.jumpTrue(), startpc);
				code.resolve(c.falseJumps);
			}
			code.resolve(loopEnv.info.exit);
			DEBUG.P(0,this,"genLoop(1)");	
		}

    public void visitForeachLoop(JCEnhancedForLoop tree) {
		throw new AssertionError(); // should have been removed by Lower.
    }

    public void visitLabelled(JCLabeledStatement tree) {
		DEBUG.P(this,"visitLabelled(1)");	
		Env<GenContext> localEnv = env.dup(tree, new GenContext());
		genStat(tree.body, localEnv, CRT_STATEMENT);
		code.resolve(localEnv.info.exit);
		DEBUG.P(0,this,"visitLabelled(1)");	
    }

    public void visitSwitch(JCSwitch tree) {
		DEBUG.P(this,"visitSwitch(1)");
		int limit = code.nextreg;
		assert tree.selector.type.tag != CLASS;
		int startpcCrt = genCrt ? code.curPc() : 0;
		Item sel = genExpr(tree.selector, syms.intType);
		List<JCCase> cases = tree.cases;
		if (cases.isEmpty()) {
			// We are seeing:  switch <sel> {}
			sel.load().drop();
			if (genCrt)
			code.crt.put(TreeInfo.skipParens(tree.selector),
					 CRT_FLOW_CONTROLLER, startpcCrt, code.curPc());
		} else {
			// We are seeing a nonempty switch.
			sel.load();
			if (genCrt)
			code.crt.put(TreeInfo.skipParens(tree.selector),
					 CRT_FLOW_CONTROLLER, startpcCrt, code.curPc());
			Env<GenContext> switchEnv = env.dup(tree, new GenContext());
			switchEnv.info.isSwitch = true;

			// Compute number of labels and minimum and maximum label values.
			// For each case, store its label in an array.
			int lo = Integer.MAX_VALUE;  // minimum label.
			int hi = Integer.MIN_VALUE;  // maximum label.
			int nlabels = 0;               // number of labels.

			int[] labels = new int[cases.length()];  // the label array.
			int defaultIndex = -1;     // the index of the default clause.

			List<JCCase> l = cases;
			for (int i = 0; i < labels.length; i++) {
			if (l.head.pat != null) {
				int val = ((Number)l.head.pat.type.constValue()).intValue();
				labels[i] = val;
				if (val < lo) lo = val;
				if (hi < val) hi = val;
				nlabels++;
			} else {
				assert defaultIndex == -1;
				defaultIndex = i;
			}
			l = l.tail;
			}

			// Determine whether to issue a tableswitch or a lookupswitch
			// instruction.
			long table_space_cost = 4 + ((long) hi - lo + 1); // words
			long table_time_cost = 3; // comparisons
			long lookup_space_cost = 3 + 2 * (long) nlabels;
			long lookup_time_cost = nlabels;
			int opcode =
			nlabels > 0 &&
			table_space_cost + 3 * table_time_cost <=
			lookup_space_cost + 3 * lookup_time_cost
			?
			tableswitch : lookupswitch;

			int startpc = code.curPc();    // the position of the selector operation
			code.emitop0(opcode);
			code.align(4);
			int tableBase = code.curPc();  // the start of the jump table
			int[] offsets = null;          // a table of offsets for a lookupswitch
			code.emit4(-1);                // leave space for default offset
			if (opcode == tableswitch) {
			code.emit4(lo);            // minimum label
			code.emit4(hi);            // maximum label
			for (long i = lo; i <= hi; i++) {  // leave space for jump table
				code.emit4(-1);
			}
			} else {
			code.emit4(nlabels);    // number of labels
			for (int i = 0; i < nlabels; i++) {
				code.emit4(-1); code.emit4(-1); // leave space for lookup table
			}
			offsets = new int[labels.length];
			}
			Code.State stateSwitch = code.state.dup();
			code.markDead();

			// For each case do:
			l = cases;
			for (int i = 0; i < labels.length; i++) {
			JCCase c = l.head;
			l = l.tail;

			int pc = code.entryPoint(stateSwitch);
			// Insert offset directly into code or else into the
			// offsets table.
			if (i != defaultIndex) {
				if (opcode == tableswitch) {
				code.put4(
					tableBase + 4 * (labels[i] - lo + 3),
					pc - startpc);
				} else {
				offsets[i] = pc - startpc;
				}
			} else {
				code.put4(tableBase, pc - startpc);
			}

			// Generate code for the statements in this case.
			genStats(c.stats, switchEnv, CRT_FLOW_TARGET);
			}

			// Resolve all breaks.
			code.resolve(switchEnv.info.exit);

			// If we have not set the default offset, we do so now.
			if (code.get4(tableBase) == -1) {
			code.put4(tableBase, code.entryPoint(stateSwitch) - startpc);
			}

			if (opcode == tableswitch) {
			// Let any unfilled slots point to the default case.
			int defaultOffset = code.get4(tableBase);
			for (long i = lo; i <= hi; i++) {
				int t = (int)(tableBase + 4 * (i - lo + 3));
				if (code.get4(t) == -1)
				code.put4(t, defaultOffset);
			}
			} else {
			// Sort non-default offsets and copy into lookup table.
			if (defaultIndex >= 0)
				for (int i = defaultIndex; i < labels.length - 1; i++) {
				labels[i] = labels[i+1];
				offsets[i] = offsets[i+1];
				}
			if (nlabels > 0)
				qsort2(labels, offsets, 0, nlabels - 1);
			for (int i = 0; i < nlabels; i++) {
				int caseidx = tableBase + 8 * (i + 1);
				code.put4(caseidx, labels[i]);
				code.put4(caseidx + 4, offsets[i]);
			}
			}
		}
		code.endScopes(limit);
		DEBUG.P(0,this,"visitSwitch(1)");
    }
//where
	/** Sort (int) arrays of keys and values
	 */
       static void qsort2(int[] keys, int[] values, int lo, int hi) {
	    int i = lo;
	    int j = hi;
	    int pivot = keys[(i+j)/2];
	    do {
		while (keys[i] < pivot) i++;
		while (pivot < keys[j]) j--;
		if (i <= j) {
		    int temp1 = keys[i];
		    keys[i] = keys[j];
		    keys[j] = temp1;
		    int temp2 = values[i];
		    values[i] = values[j];
		    values[j] = temp2;
		    i++;
		    j--;
		}
	    } while (i <= j);
	    if (lo < j) qsort2(keys, values, lo, j);
	    if (i < hi) qsort2(keys, values, i, hi);
	}

    public void visitSynchronized(JCSynchronized tree) {
		DEBUG.P(this,"visitSynchronized(1)");
		
		int limit = code.nextreg;
		// Generate code to evaluate lock and save in temporary variable.
		final LocalItem lockVar = makeTemp(syms.objectType);
		genExpr(tree.lock, tree.lock.type).load().duplicate();
		lockVar.store();

		// Generate code to enter monitor.
		code.emitop0(monitorenter);
		code.state.lock(lockVar.reg);

		// Generate code for a try statement with given body, no catch clauses
		// in a new environment with the "exit-monitor" operation as finalizer.
		final Env<GenContext> syncEnv = env.dup(tree, new GenContext());
		syncEnv.info.finalize = new GenFinalizer() {
			void gen() {
				DEBUG.P(this,"gen()");

				genLast();
				assert syncEnv.info.gaps.length() % 2 == 0;
				syncEnv.info.gaps.append(code.curPc());

				DEBUG.P(0,this,"gen()");
			}

			void genLast() {
				DEBUG.P(this,"genLast()");
				DEBUG.P("code.isAlive()="+code.isAlive());

				if (code.isAlive()) {
					lockVar.load();
					code.emitop0(monitorexit);
					code.state.unlock(lockVar.reg);
				}

				DEBUG.P(0,this,"genLast()");
			}
		};
		syncEnv.info.gaps = new ListBuffer<Integer>();
		genTry(tree.body, List.<JCCatch>nil(), syncEnv);
		code.endScopes(limit);
		
		DEBUG.P(0,this,"visitSynchronized(1)");
    }
       
    public void visitTry(final JCTry tree) {
		DEBUG.P(this,"visitTry(1)");
		// Generate code for a try statement with given body and catch clauses,
		// in a new environment which calls the finally block if there is one.
		final Env<GenContext> tryEnv = env.dup(tree, new GenContext());
		final Env<GenContext> oldEnv = env;
		DEBUG.P("tryEnv="+tryEnv);
		DEBUG.P("oldEnv="+oldEnv);
		DEBUG.P("useJsrLocally="+useJsrLocally);
		DEBUG.P("stackMap="+stackMap);
		DEBUG.P("jsrlimit="+jsrlimit);
        if (!useJsrLocally) {
            useJsrLocally =
                (stackMap == StackMapFormat.NONE) &&
                (jsrlimit <= 0 ||
                jsrlimit < 100 &&
                estimateCodeComplexity(tree.finalizer)>jsrlimit);
        }
		DEBUG.P("useJsrLocally="+useJsrLocally);
		tryEnv.info.finalize = new GenFinalizer() {
			void gen() {
				DEBUG.P(this,"gen()");
				DEBUG.P("useJsrLocally="+useJsrLocally);
				if (useJsrLocally) {
					if (tree.finalizer != null) {
						Code.State jsrState = code.state.dup();
						jsrState.push(code.jsrReturnValue);
						tryEnv.info.cont =
							new Chain(code.emitJump(jsr),
								  tryEnv.info.cont,
								  jsrState);
					}
					assert tryEnv.info.gaps.length() % 2 == 0;
					tryEnv.info.gaps.append(code.curPc());
				} else {
					assert tryEnv.info.gaps.length() % 2 == 0;
					tryEnv.info.gaps.append(code.curPc());
					genLast();
				}
				DEBUG.P(0,this,"gen()");
			}
			void genLast() {
				DEBUG.P(this,"genLast()");
				if (tree.finalizer != null)
					genStat(tree.finalizer, oldEnv, CRT_BLOCK);
				DEBUG.P(0,this,"genLast()");
			}
			boolean hasFinalizer() {
				return tree.finalizer != null;
			}
		};
		tryEnv.info.gaps = new ListBuffer<Integer>();
		genTry(tree.body, tree.catchers, tryEnv);
		
		DEBUG.P(0,this,"visitTry(1)");
    }
    //where
    /** Generate code for a try or synchronized statement
	 *  @param body      The body of the try or synchronized statement.
	 *  @param catchers  The lis of catch clauses.
	 *  @param env       the environment current for the body.
	 */
	void genTry(JCTree body, List<JCCatch> catchers, Env<GenContext> env) {
		DEBUG.P(this,"genTry(3)");
	    int limit = code.nextreg;
	    int startpc = code.curPc();
	    Code.State stateTry = code.state.dup();
	    genStat(body, env, CRT_BLOCK);
	    int endpc = code.curPc();
	    boolean hasFinalizer =
		env.info.finalize != null &&
		env.info.finalize.hasFinalizer();
	    List<Integer> gaps = env.info.gaps.toList();
	    code.statBegin(TreeInfo.endPos(body));
	    genFinalizer(env);
	    code.statBegin(TreeInfo.endPos(env.tree));
	    Chain exitChain = code.branch(goto_);
	    endFinalizerGap(env);

		DEBUG.P("startpc="+startpc);
		DEBUG.P("endpc  ="+endpc);
	    if (startpc != endpc) for (List<JCCatch> l = catchers; l.nonEmpty(); l = l.tail) {
			// start off with exception on stack
			code.entryPoint(stateTry, l.head.param.sym.type);
			genCatch(l.head, env, startpc, endpc, gaps);
			genFinalizer(env);
			if (hasFinalizer || l.tail.nonEmpty()) {
				code.statBegin(TreeInfo.endPos(env.tree));
				exitChain = code.mergeChains(exitChain,
							 code.branch(goto_));
			}
			endFinalizerGap(env);
	    }

		DEBUG.P("hasFinalizer="+hasFinalizer);
	    if (hasFinalizer) {
			// Create a new register segement to avoid allocating
			// the same variables in finalizers and other statements.
			code.newRegSegment();

			// Add a catch-all clause.

			// start off with exception on stack
			int catchallpc = code.entryPoint(stateTry, syms.throwableType);

			DEBUG.P("catchallpc="+catchallpc);

			// Register all exception ranges for catch all clause.
			// The range of the catch all clause is from the beginning
			// of the try or synchronized block until the present
			// code pointer excluding all gaps in the current
			// environment's GenContext.
			int startseg = startpc;

			DEBUG.P("startseg="+startseg);
			DEBUG.P("env.info="+env.info);
			while (env.info.gaps.nonEmpty()) {
				int endseg = env.info.gaps.next().intValue();
				DEBUG.P("");
				DEBUG.P("endseg="+endseg);
				registerCatch(body.pos(), startseg, endseg,
					  catchallpc, 0);
				startseg = env.info.gaps.next().intValue();
			}
			code.statBegin(TreeInfo.finalizerPos(env.tree));
			code.markStatBegin();

			Item excVar = makeTemp(syms.throwableType);
			excVar.store();
			genFinalizer(env);
			excVar.load();
			registerCatch(body.pos(), startseg,
					  env.info.gaps.next().intValue(),
					  catchallpc, 0);
			code.emitop0(athrow);
			code.markDead();

			// If there are jsr's to this finalizer, ...
			DEBUG.P("env.info.cont="+env.info.cont);
			if (env.info.cont != null) {
				// Resolve all jsr's.
				code.resolve(env.info.cont);

				// Mark statement line number
				code.statBegin(TreeInfo.finalizerPos(env.tree));
				code.markStatBegin();

				// Save return address.
				LocalItem retVar = makeTemp(syms.throwableType);
				retVar.store();

				// Generate finalizer code.
				env.info.finalize.genLast();

				// Return.
				code.emitop1w(ret, retVar.reg);
				code.markDead();
			}
	    }

	    // Resolve all breaks.
	    code.resolve(exitChain);

	    // End the scopes of all try-local variables in variable info.
	    code.endScopes(limit);
	    DEBUG.P(0,this,"genTry(3)");
	}

    /** Generate code for a catch clause.
	 *  @param tree     The catch clause.
	 *  @param env      The environment current in the enclosing try.
	 *  @param startpc  Start pc of try-block.
	 *  @param endpc    End pc of try-block.
	 */
    void genCatch(JCCatch tree,
		      Env<GenContext> env,
		      int startpc, int endpc,
		      List<Integer> gaps) {
		DEBUG.P(this,"genCatch(4)");
		DEBUG.P("startpc="+startpc);
		DEBUG.P("endpc="+endpc);
		DEBUG.P("gaps="+gaps);

	    if (startpc != endpc) {
			int catchType = makeRef(tree.pos(), tree.param.type);
			while (gaps.nonEmpty()) {
				int end = gaps.head.intValue();
				registerCatch(tree.pos(),
					  startpc,  end, code.curPc(),
					  catchType);
				gaps = gaps.tail;
				startpc = gaps.head.intValue();
				gaps = gaps.tail;
			}
			DEBUG.P("startpc="+startpc);
			DEBUG.P("endpc="+endpc);
			if (startpc < endpc)
				registerCatch(tree.pos(),
					  startpc, endpc, code.curPc(),
					  catchType);
			VarSymbol exparam = tree.param.sym;
			DEBUG.P("exparam="+exparam);
			code.statBegin(tree.pos);
			code.markStatBegin();
			int limit = code.nextreg;
			int exlocal = code.newLocal(exparam);
			items.makeLocalItem(exparam).store();
			code.statBegin(TreeInfo.firstStatPos(tree.body));
			genStat(tree.body, env, CRT_BLOCK);
			code.endScopes(limit);
			code.statBegin(TreeInfo.endPos(tree.body));
	    }
	    DEBUG.P(0,this,"genCatch(4)");
	}

    /** Register a catch clause in the "Exceptions" code-attribute.
	 */
	void registerCatch(DiagnosticPosition pos,
			   int startpc, int endpc,
			   int handler_pc, int catch_type) {
		//handler_pc是catch子句中第一条指令的偏移量，
		//catch_type是捕获的异常类在常量池中的索引
		DEBUG.P(this,"registerCatch(5)"); 
		DEBUG.P("startpc="+startpc);
		DEBUG.P("endpc="+endpc);

	    if (startpc != endpc) {
			char startpc1 = (char)startpc;
			char endpc1 = (char)endpc;
			char handler_pc1 = (char)handler_pc;
			if (startpc1 == startpc &&
				endpc1 == endpc &&
				handler_pc1 == handler_pc) {
				code.addCatch(startpc1, endpc1, handler_pc1,
					  (char)catch_type);
			} else {
				if (!useJsrLocally && !target.generateStackMapTable()) {
							useJsrLocally = true;
							throw new CodeSizeOverflow();
				} else {
					log.error(pos, "limit.code.too.large.for.try.stmt");
					nerrs++;
				}
			}
	    }
	    DEBUG.P(0,this,"registerCatch(5)");
	}
    /** Very roughly estimate the number of instructions needed for
     *  the given tree.
     */
    int estimateCodeComplexity(JCTree tree) {
		if (tree == null) return 0;
		class ComplexityScanner extends TreeScanner {
			int complexity = 0;
			public void scan(JCTree tree) {
				if (complexity > jsrlimit) return;
				super.scan(tree);
			}
			public void visitClassDef(JCClassDecl tree) {}
			public void visitDoLoop(JCDoWhileLoop tree)
				{ super.visitDoLoop(tree); complexity++; }
			public void visitWhileLoop(JCWhileLoop tree)
				{ super.visitWhileLoop(tree); complexity++; }
			public void visitForLoop(JCForLoop tree)
				{ super.visitForLoop(tree); complexity++; }
			public void visitSwitch(JCSwitch tree)
				{ super.visitSwitch(tree); complexity+=5; }
			public void visitCase(JCCase tree)
				{ super.visitCase(tree); complexity++; }
			public void visitSynchronized(JCSynchronized tree)
				{ super.visitSynchronized(tree); complexity+=6; }
			public void visitTry(JCTry tree)
				{ super.visitTry(tree);
			  if (tree.finalizer != null) complexity+=6; }
			public void visitCatch(JCCatch tree)
				{ super.visitCatch(tree); complexity+=2; }
			public void visitConditional(JCConditional tree)
				{ super.visitConditional(tree); complexity+=2; }
			public void visitIf(JCIf tree)
				{ super.visitIf(tree); complexity+=2; }
			// note: for break, continue, and return we don't take unwind() into account.
			public void visitBreak(JCBreak tree)
				{ super.visitBreak(tree); complexity+=1; }
			public void visitContinue(JCContinue tree)
				{ super.visitContinue(tree); complexity+=1; }
			public void visitReturn(JCReturn tree)
				{ super.visitReturn(tree); complexity+=1; }
			public void visitThrow(JCThrow tree)
				{ super.visitThrow(tree); complexity+=1; }
			public void visitAssert(JCAssert tree)
				{ super.visitAssert(tree); complexity+=5; }
			public void visitApply(JCMethodInvocation tree)
				{ super.visitApply(tree); complexity+=2; }
			public void visitNewClass(JCNewClass tree)
				{ scan(tree.encl); scan(tree.args); complexity+=2; }
			public void visitNewArray(JCNewArray tree)
				{ super.visitNewArray(tree); complexity+=5; }
			public void visitAssign(JCAssign tree)
				{ super.visitAssign(tree); complexity+=1; }
			public void visitAssignop(JCAssignOp tree)
				{ super.visitAssignop(tree); complexity+=2; }
			public void visitUnary(JCUnary tree)
				{ complexity+=1;
			  if (tree.type.constValue() == null) super.visitUnary(tree); }
			public void visitBinary(JCBinary tree)
				{ complexity+=1;
			  if (tree.type.constValue() == null) super.visitBinary(tree); }
			public void visitTypeTest(JCInstanceOf tree)
				{ super.visitTypeTest(tree); complexity+=1; }
			public void visitIndexed(JCArrayAccess tree)
				{ super.visitIndexed(tree); complexity+=1; }
			public void visitSelect(JCFieldAccess tree)
				{ super.visitSelect(tree);
			  if (tree.sym.kind == VAR) complexity+=1; }
			public void visitIdent(JCIdent tree) {
				if (tree.sym.kind == VAR) {
					complexity+=1;
					if (tree.type.constValue() == null &&
					tree.sym.owner.kind == TYP)
						complexity+=1;
				}
			}
			public void visitLiteral(JCLiteral tree)
				{ complexity+=1; }
			public void visitTree(JCTree tree) {}
			public void visitWildcard(JCWildcard tree) {
				throw new AssertionError(this.getClass().getName());
			}
		}
		ComplexityScanner scanner = new ComplexityScanner();
		tree.accept(scanner);
		return scanner.complexity;
    }

    public void visitIf(JCIf tree) {
		DEBUG.P(this,"visitIf(1)");
		int limit = code.nextreg;
		Chain thenExit = null;

		DEBUG.P("limit="+limit);
		//在genCond也调用了TreeInfo.skipParens，这里重复了
		DEBUG.P("tree.cond="+tree.cond);
		CondItem c = genCond(TreeInfo.skipParens(tree.cond),
					 CRT_FLOW_CONTROLLER);
		
		DEBUG.P("c="+c);
		Chain elseChain = c.jumpFalse();

		DEBUG.P("elseChain="+elseChain);
		DEBUG.P("c.isFalse()="+c.isFalse());
		if (!c.isFalse()) {
			code.resolve(c.trueJumps);
			genStat(tree.thenpart, env, CRT_STATEMENT | CRT_FLOW_TARGET);
			thenExit = code.branch(goto_);
		}
		if (elseChain != null) {
			code.resolve(elseChain);
			if (tree.elsepart != null)
				genStat(tree.elsepart, env,CRT_STATEMENT | CRT_FLOW_TARGET);
		}
		code.resolve(thenExit);
		code.endScopes(limit);
		DEBUG.P(0,this,"visitIf(1)");
    }

    public void visitExec(JCExpressionStatement tree) {
        DEBUG.P(this,"visitExec(1)");
        DEBUG.P("tree.expr.tag="+tree.expr.myTreeTag());
		// Optimize x++ to ++x and x-- to --x.
		if (tree.expr.tag == JCTree.POSTINC) tree.expr.tag = JCTree.PREINC;
		else if (tree.expr.tag == JCTree.POSTDEC) tree.expr.tag = JCTree.PREDEC;
		genExpr(tree.expr, tree.expr.type).drop();
		DEBUG.P(0,this,"visitExec(1)");
    }

    public void visitBreak(JCBreak tree) {
		DEBUG.P(this,"visitBreak(1)");
		DEBUG.P("tree.label="+tree.label);
		DEBUG.P("tree.target="+tree.target);

        Env<GenContext> targetEnv = unwind(tree.target, env);
		assert code.state.stacksize == 0;
		targetEnv.info.addExit(code.branch(goto_));
		endFinalizerGaps(env, targetEnv);

		DEBUG.P(0,this,"visitBreak(1)");
    }

    public void visitContinue(JCContinue tree) {
		DEBUG.P(this,"visitContinue(1)");
		DEBUG.P("tree.label="+tree.label);
		DEBUG.P("tree.target="+tree.target);

        Env<GenContext> targetEnv = unwind(tree.target, env);
		assert code.state.stacksize == 0;
		targetEnv.info.addCont(code.branch(goto_));
		endFinalizerGaps(env, targetEnv);

		DEBUG.P(0,this,"visitContinue(1)");
    }

    public void visitReturn(JCReturn tree) {
		DEBUG.P(this,"visitReturn(1)");
		DEBUG.P("tree.expr="+tree.expr);

		int limit = code.nextreg;
		final Env<GenContext> targetEnv;
		if (tree.expr != null) {
			Item r = genExpr(tree.expr, pt).load();
			if (hasFinally(env.enclMethod, env)) {
				r = makeTemp(pt);
				r.store();
			}
			targetEnv = unwind(env.enclMethod, env);
			r.load();
			code.emitop0(ireturn + Code.truncate(Code.typecode(pt)));
		} else {
			targetEnv = unwind(env.enclMethod, env);
			code.emitop0(return_);
		}
		endFinalizerGaps(env, targetEnv);
		code.endScopes(limit);

		DEBUG.P(0,this,"visitReturn(1)");
    }

    public void visitThrow(JCThrow tree) {
		DEBUG.P(this,"visitThrow(1)");	
		genExpr(tree.expr, tree.expr.type).load();
		code.emitop0(athrow);
		DEBUG.P(0,this,"visitThrow(1)");	
    }

/* ************************************************************************
 * Visitor methods for expressions
 *************************************************************************/

    public void visitApply(JCMethodInvocation tree) {
		DEBUG.P(this,"visitApply(1)");	
		// Generate code for method.
		Item m = genExpr(tree.meth, methodType);
		// Generate code for all arguments, where the expected types are
		// the parameters of the method's external type (that is, any implicit
		// outer instance of a super(...) call appears as first parameter).
		genArgs(tree.args,
			TreeInfo.symbol(tree.meth).externalType(types).getParameterTypes());
		result = m.invoke();
		DEBUG.P(0,this,"visitApply(1)");	
    }

    public void visitConditional(JCConditional tree) {
		DEBUG.P(this,"visitConditional(1)");	
		Chain thenExit = null;
		CondItem c = genCond(tree.cond, CRT_FLOW_CONTROLLER);
		Chain elseChain = c.jumpFalse();
		if (!c.isFalse()) {
			code.resolve(c.trueJumps);
			int startpc = genCrt ? code.curPc() : 0;
			genExpr(tree.truepart, pt).load();
			code.state.forceStackTop(tree.type);
			if (genCrt) code.crt.put(tree.truepart, CRT_FLOW_TARGET,
						 startpc, code.curPc());
			thenExit = code.branch(goto_);
		}
		if (elseChain != null) {
			code.resolve(elseChain);
			int startpc = genCrt ? code.curPc() : 0;
			genExpr(tree.falsepart, pt).load();
			code.state.forceStackTop(tree.type);
			if (genCrt) code.crt.put(tree.falsepart, CRT_FLOW_TARGET,
						 startpc, code.curPc());
		}
		code.resolve(thenExit);
		result = items.makeStackItem(pt);
		DEBUG.P(0,this,"visitConditional(1)");
    }

    public void visitNewClass(JCNewClass tree) {
		DEBUG.P(this,"visitNewClass(1)");
		// Enclosing instances or anonymous classes should have been eliminated
		// by now.
		assert tree.encl == null && tree.def == null;

		code.emitop2(new_, makeRef(tree.pos(), tree.type));
		code.emitop0(dup);

		// Generate code for all arguments, where the expected types are
		// the parameters of the constructor's external type (that is,
		// any implicit outer instance appears as first parameter).
		genArgs(tree.args, tree.constructor.externalType(types).getParameterTypes());

		items.makeMemberItem(tree.constructor, true).invoke();
		result = items.makeStackItem(tree.type);
		DEBUG.P(0,this,"visitNewClass(1)");
    }

    public void visitNewArray(JCNewArray tree) {
		DEBUG.P(this,"visitNewArray(1)");
		DEBUG.P("tree.elems="+tree.elems);
		if (tree.elems != null) {
			Type elemtype = types.elemtype(tree.type);
			loadIntConst(tree.elems.length());
			Item arr = makeNewArray(tree.pos(), tree.type, 1);
			DEBUG.P("arr="+arr);
			int i = 0;
			for (List<JCExpression> l = tree.elems; l.nonEmpty(); l = l.tail) {
				arr.duplicate();
				loadIntConst(i);
				i++;
				genExpr(l.head, elemtype).load();
				items.makeIndexedItem(elemtype).store();
			}
			result = arr;
		} else {
			for (List<JCExpression> l = tree.dims; l.nonEmpty(); l = l.tail) {
				genExpr(l.head, syms.intType).load();
			}
			result = makeNewArray(tree.pos(), tree.type, tree.dims.length());
		}
		DEBUG.P(0,this,"visitNewArray(1)");
    }
//where
    /** Generate code to create an array with given element type and number
	 *  of dimensions.
	 */
	Item makeNewArray(DiagnosticPosition pos, Type type, int ndims) {
		try {//我加上的
		DEBUG.P(this,"makeNewArray(3)");
		DEBUG.P("type="+type);
		DEBUG.P("ndims="+ndims);

	    Type elemtype = types.elemtype(type);
	    if (types.dimensions(elemtype) + ndims > ClassFile.MAX_DIMENSIONS) {
			log.error(pos, "limit.dimensions");
			nerrs++;
	    }
	    int elemcode = Code.arraycode(elemtype);
		DEBUG.P("elemcode="+elemcode);

	    if (elemcode == 0 || (elemcode == 1 && ndims == 1)) {
			code.emitAnewarray(makeRef(pos, elemtype), type);
	    } else if (elemcode == 1) {
			code.emitMultianewarray(ndims, makeRef(pos, type), type);
	    } else {
			code.emitNewarray(elemcode, type);
	    }
	    return items.makeStackItem(type);

		}finally{//我加上的
		DEBUG.P(0,this,"makeNewArray(3)");
		}
	}

    public void visitParens(JCParens tree) {
		DEBUG.P(this,"visitParens(1)");
		result = genExpr(tree.expr, tree.expr.type);
		DEBUG.P(0,this,"visitParens(1)");
    }

    public void visitAssign(JCAssign tree) {
		DEBUG.P(this,"visitAssign(1)");
		Item l = genExpr(tree.lhs, tree.lhs.type);
		genExpr(tree.rhs, tree.lhs.type).load();
		result = items.makeAssignItem(l);
		DEBUG.P(0,this,"visitAssign(1)");
    }

    public void visitAssignop(JCAssignOp tree) {
		DEBUG.P(this,"visitAssignop(1)");
		OperatorSymbol operator = (OperatorSymbol) tree.operator;
		DEBUG.P("operator.opcode="+code.mnem(operator.opcode));
		DEBUG.P("tree="+tree);
		DEBUG.P("tree.tag="+tree.myTreeTag());
		
		Item l;
		if (operator.opcode == string_add) {
			// Generate code to make a string buffer
			makeStringBuffer(tree.pos());

			// Generate code for first string, possibly save one
			// copy under buffer
			l = genExpr(tree.lhs, tree.lhs.type);
			DEBUG.P("Item l="+l);
			DEBUG.P("l.width()="+l.width());
			if (l.width() > 0) {
				code.emitop0(dup_x1 + 3 * (l.width() - 1));
			}

			// Load first string and append to buffer.
			l.load();
			appendString(tree.lhs);

			// Append all other strings to buffer.
			appendStrings(tree.rhs);

			// Convert buffer to string.
			bufferToString(tree.pos());
		} else {
			// Generate code for first expression
			l = genExpr(tree.lhs, tree.lhs.type);

			// If we have an increment of -32768 to +32767 of a local
			// int variable we can use an incr instruction instead of
			// proceeding further.
			if ((tree.tag == JCTree.PLUS_ASG || tree.tag == JCTree.MINUS_ASG) &&
			l instanceof LocalItem &&
			tree.lhs.type.tag <= INT &&
			tree.rhs.type.tag <= INT &&
			tree.rhs.type.constValue() != null) {
				int ival = ((Number) tree.rhs.type.constValue()).intValue();
				if (tree.tag == JCTree.MINUS_ASG) ival = -ival;
				((LocalItem)l).incr(ival);
				result = l;
				return;
			}
			// Otherwise, duplicate expression, load one copy
			// and complete binary operation.
			l.duplicate();
			l.coerce(operator.type.getParameterTypes().head).load();
			completeBinop(tree.lhs, tree.rhs, operator).coerce(tree.lhs.type);
		}
		result = items.makeAssignItem(l);
		DEBUG.P(0,this,"visitAssignop(1)");
    }

    public void visitUnary(JCUnary tree) {
		DEBUG.P(this,"visitUnary(1)");
		OperatorSymbol operator = (OperatorSymbol)tree.operator;
		DEBUG.P("tree.tag="+tree.myTreeTag());
		if (tree.tag == JCTree.NOT) {
			CondItem od = genCond(tree.arg, false);
			result = od.negate();
		} else {
			Item od = genExpr(tree.arg, operator.type.getParameterTypes().head);
			DEBUG.P("od="+od);
			DEBUG.P("tree.tag="+tree.myTreeTag());
			DEBUG.P("operator.opcode="+code.mnem(operator.opcode));
			
			switch (tree.tag) {
				case JCTree.POS:
					result = od.load();
					break;
				case JCTree.NEG:
					result = od.load();
					code.emitop0(operator.opcode);
					break;
				case JCTree.COMPL:
					result = od.load();
					emitMinusOne(od.typecode);
					code.emitop0(operator.opcode);
					break;
				case JCTree.PREINC: case JCTree.PREDEC:
					od.duplicate();
					if (od instanceof LocalItem &&
						(operator.opcode == iadd || operator.opcode == isub)) {
						((LocalItem)od).incr(tree.tag == JCTree.PREINC ? 1 : -1);
						result = od;
					} else {
						od.load();
						code.emitop0(one(od.typecode));
						code.emitop0(operator.opcode);
						// Perform narrowing primitive conversion if byte,
						// char, or short.  Fix for 4304655.
						if (od.typecode != INTcode &&
							Code.truncate(od.typecode) == INTcode)
							code.emitop0(int2byte + od.typecode - BYTEcode);
							result = items.makeAssignItem(od);
					}
					break;
				case JCTree.POSTINC: case JCTree.POSTDEC:
					od.duplicate();
					if (od instanceof LocalItem && 
								(operator.opcode == iadd || operator.opcode == isub)) {
						Item res = od.load();
						((LocalItem)od).incr(tree.tag == JCTree.POSTINC ? 1 : -1);
						result = res;
					} else {
						Item res = od.load();
						od.stash(od.typecode);
						code.emitop0(one(od.typecode));
						code.emitop0(operator.opcode);
						// Perform narrowing primitive conversion if byte,
						// char, or short.  Fix for 4304655.
						if (od.typecode != INTcode &&
						Code.truncate(od.typecode) == INTcode)
							code.emitop0(int2byte + od.typecode - BYTEcode);
						od.store();
						result = res;
					}
					break;
				case JCTree.NULLCHK:
					result = od.load();
					code.emitop0(dup);
					genNullCheck(tree.pos());
					break;
				default:
					assert false;
			}
		}
		DEBUG.P(0,this,"visitUnary(1)");
    }

    /** Generate a null check from the object value at stack top. */
    private void genNullCheck(DiagnosticPosition pos) {
		callMethod(pos, syms.objectType, names.getClass,
			   List.<Type>nil(), false);
		code.emitop0(pop);
    }

    public void visitBinary(JCBinary tree) {
		DEBUG.P(this,"visitBinary(1)");
        OperatorSymbol operator = (OperatorSymbol)tree.operator;

		DEBUG.P("tree.tag="+tree.myTreeTag());
		DEBUG.P("operator.opcode="+code.mnem(operator.opcode));
		if (operator.opcode == string_add) {
			// Create a string buffer.
			makeStringBuffer(tree.pos());
			// Append all strings to buffer.
			appendStrings(tree);
			// Convert buffer to string.
			bufferToString(tree.pos());
			result = items.makeStackItem(syms.stringType);
		} else if (tree.tag == JCTree.AND) {
			CondItem lcond = genCond(tree.lhs, CRT_FLOW_CONTROLLER);
			if (!lcond.isFalse()) {
				Chain falseJumps = lcond.jumpFalse();
				code.resolve(lcond.trueJumps);
				CondItem rcond = genCond(tree.rhs, CRT_FLOW_TARGET);
				result = items.
					makeCondItem(rcond.opcode,
						 rcond.trueJumps,
						 code.mergeChains(falseJumps,
								  rcond.falseJumps));
			} else {
				result = lcond;
			}
		} else if (tree.tag == JCTree.OR) {
			CondItem lcond = genCond(tree.lhs, CRT_FLOW_CONTROLLER);
			if (!lcond.isTrue()) {
				Chain trueJumps = lcond.jumpTrue();
				code.resolve(lcond.falseJumps);
				CondItem rcond = genCond(tree.rhs, CRT_FLOW_TARGET);
				result = items.
					makeCondItem(rcond.opcode,
						 code.mergeChains(trueJumps, rcond.trueJumps),
						 rcond.falseJumps);
			} else {
				result = lcond;
			}
		} else {
			Item od = genExpr(tree.lhs, operator.type.getParameterTypes().head);
			od.load();
			result = completeBinop(tree.lhs, tree.rhs, operator);
		}
		DEBUG.P(0,this,"visitBinary(1)");
    }
//where
		/** Make a new string buffer.
		 */
        void makeStringBuffer(DiagnosticPosition pos) {
			DEBUG.P(this,"makeStringBuffer(1)");

			code.emitop2(new_, makeRef(pos, stringBufferType));
			code.emitop0(dup);
			callMethod(
			pos, stringBufferType, names.init, List.<Type>nil(), false);

			DEBUG.P(0,this,"makeStringBuffer(1)");
		}

        /** Append value (on tos) to string buffer (on tos - 1).
		*/
        void appendString(JCTree tree) {
			DEBUG.P(this,"appendString(1)");
			DEBUG.P("tree="+tree);

			Type t = tree.type.baseType();
			if (t.tag > lastBaseTag && t.tsym != syms.stringType.tsym) {
				t = syms.objectType;
			}
			items.makeMemberItem(getStringBufferAppend(tree, t), false).invoke();

			DEBUG.P(0,this,"appendString(1)");
		}

        Symbol getStringBufferAppend(JCTree tree, Type t) {
			DEBUG.P(this,"getStringBufferAppend(2)");
			DEBUG.P("tree="+tree);
			DEBUG.P("t="+t);

			assert t.constValue() == null;
			Symbol method = stringBufferAppend.get(t);

			DEBUG.P("method="+method);

			if (method == null) {
				method = rs.resolveInternalMethod(tree.pos(),
								  attrEnv,
								  stringBufferType,
								  names.append,
								  List.of(t),
								  null);
				stringBufferAppend.put(t, method);
			}
			
			DEBUG.P("method="+method);
			DEBUG.P(0,this,"getStringBufferAppend(2)");
			return method;
		}

        /** Add all strings in tree to string buffer.
		 */
		void appendStrings(JCTree tree) {
			try {//我加上的
			DEBUG.P(this,"appendStrings(1)");

			tree = TreeInfo.skipParens(tree);

			DEBUG.P("tree="+tree);
			DEBUG.P("tree.tag="+tree.myTreeTag());

			if (tree.tag == JCTree.PLUS && tree.type.constValue() == null) {
				JCBinary op = (JCBinary) tree;
				if (op.operator.kind == MTH &&
					((OperatorSymbol) op.operator).opcode == string_add) {
					appendStrings(op.lhs);
					appendStrings(op.rhs);
					return;
				}
			}
			genExpr(tree, tree.type).load();
			appendString(tree);

			}finally{//我加上的
			DEBUG.P(0,this,"appendStrings(2)");
			}
		}

        /** Convert string buffer on tos to string.
		 */
		void bufferToString(DiagnosticPosition pos) {
			DEBUG.P(this,"bufferToString(1)");
			callMethod(
						pos,
						stringBufferType,
						names.toString,
						List.<Type>nil(),
						false);
			DEBUG.P(0,this,"bufferToString(1)");
		}

     /** Complete generating code for operation, with left operand
	 *  already on stack.
	 *  @param lhs       The tree representing the left operand.
	 *  @param rhs       The tree representing the right operand.
	 *  @param operator  The operator symbol.
	 */
	Item completeBinop(JCTree lhs, JCTree rhs, OperatorSymbol operator) {
		try {//我加上的
		DEBUG.P(this,"completeBinop(3)");
		DEBUG.P("lhs="+lhs);
		DEBUG.P("rhs="+rhs);
		DEBUG.P("operator="+operator);
		DEBUG.P("operator.opcode="+code.mnem(operator.opcode));

	    MethodType optype = (MethodType)operator.type;
	    int opcode = operator.opcode;

	    if (opcode >= if_icmpeq && opcode <= if_icmple &&
		rhs.type.constValue() instanceof Number &&
		((Number) rhs.type.constValue()).intValue() == 0) {
			//如果关系运算符右边的操作数是0，把if_icmpeq到if_icmple这6条指令
			//转换成ifeq到ifle这6条指令，这样就不用将右边的操作数0压入堆栈了
			opcode = opcode + (ifeq - if_icmpeq);
	    } else if (opcode >= if_acmpeq && opcode <= if_acmpne &&
				   TreeInfo.isNull(rhs)) {
			//如果关系运算符右边的操作数是null，把if_acmpeq转换成if_acmp_null，
			//把if_acmpne转换成if_acmp_nonnull。
			opcode = opcode + (if_acmp_null - if_acmpeq);
	    } else {
			// The expected type of the right operand is
			// the second parameter type of the operator, except for
			// shifts with long shiftcount, where we convert the opcode
			// to a short shift and the expected type to int.
			Type rtype = operator.erasure(types).getParameterTypes().tail.head;

			DEBUG.P("");
			DEBUG.P("operator.type="+operator.type);
			DEBUG.P("operator.erasure(types).getParameterTypes()="+operator.erasure(types).getParameterTypes());
			DEBUG.P("rtype="+rtype);
			if (opcode >= ishll && opcode <= lushrl) {
				//把ishll到lushrl这6条非标准指令转换成ishl到lushr这6条指令，
				opcode = opcode + (ishl - ishll);
				rtype = syms.intType;
			}

			DEBUG.P("opcode="+code.mnem(opcode));
			// Generate code for right operand and load.
			genExpr(rhs, rtype).load();
			// If there are two consecutive opcode instructions,
			// emit the first now.
			if (opcode >= (1 << preShift)) { //参考Symtab类的enterBinop方法
				code.emitop0(opcode >> preShift);
				opcode = opcode & 0xFF;
			}
	    }
	    
	    DEBUG.P("opcode="+code.mnem(opcode));
	    if (opcode >= ifeq && opcode <= if_acmpne ||
			opcode == if_acmp_null || opcode == if_acmp_nonnull) {
			return items.makeCondItem(opcode);
	    } else {
			code.emitop0(opcode);
			return items.makeStackItem(optype.restype);
	    }
	    
	    }finally{//我加上的
		DEBUG.P(0,this,"completeBinop(3)");
		}
	}

    public void visitTypeCast(JCTypeCast tree) {
		DEBUG.P(this,"visitTypeCast(1)");
		result = genExpr(tree.expr, tree.clazz.type).load();
		// Additional code is only needed if we cast to a reference type
		// which is not statically a supertype of the expression's type.
		// For basic types, the coerce(...) in genExpr(...) will do
		// the conversion.
		if (tree.clazz.type.tag > lastBaseTag &&
			types.asSuper(tree.expr.type, tree.clazz.type.tsym) == null) {
			code.emitop2(checkcast, makeRef(tree.pos(), tree.clazz.type));
		}
		DEBUG.P(0,this,"visitTypeCast(1)");
    }

    public void visitWildcard(JCWildcard tree) {
		throw new AssertionError(this.getClass().getName());
    }

    public void visitTypeTest(JCInstanceOf tree) {
		genExpr(tree.expr, tree.expr.type).load();
		code.emitop2(instanceof_, makeRef(tree.pos(), tree.clazz.type));
		result = items.makeStackItem(syms.booleanType);
    }

    public void visitIndexed(JCArrayAccess tree) {
		genExpr(tree.indexed, tree.indexed.type).load();
		genExpr(tree.index, syms.intType).load();
		result = items.makeIndexedItem(tree.type);
    }

    public void visitIdent(JCIdent tree) {
		DEBUG.P(this,"visitIdent(1)");
		Symbol sym = tree.sym;
		
		DEBUG.P("tree.name="+tree.name);
		DEBUG.P("sym="+sym);
		DEBUG.P("sym.flags()="+Flags.toString(sym.flags()));
		DEBUG.P("sym.kind="+Kinds.toString(sym.kind));
		DEBUG.P("sym.owner="+sym.owner);
		if(sym.owner!=null) DEBUG.P("sym.owner.kind="+Kinds.toString(sym.owner.kind));
		
		if (tree.name == names._this || tree.name == names._super) {
			Item res = tree.name == names._this
			? items.makeThisItem()
				: items.makeSuperItem();
			if (sym.kind == MTH) {
				// Generate code to address the constructor.
				res.load();
				
				//这里为true，说明不是一个virtual调用，而是Invokespecial
				//因为当前面两个if条件都为true时，源代码中要么是this()要么是super()
				res = items.makeMemberItem(sym, true);
			}
			result = res;
		} else if (sym.kind == VAR && sym.owner.kind == MTH) {
				//本地变量
			result = items.makeLocalItem((VarSymbol)sym);
		} else if ((sym.flags() & STATIC) != 0) {
				//类字段
			if (!isAccessSuper(env.enclMethod))
				sym = binaryQualifier(sym, env.enclClass.type);
			result = items.makeStaticItem(sym);
		} else {
				//实例字段
			items.makeThisItem().load();
			sym = binaryQualifier(sym, env.enclClass.type);
			result = items.makeMemberItem(sym, (sym.flags() & PRIVATE) != 0);
		}

		DEBUG.P(0,this,"visitIdent(1)");
    }

    public void visitSelect(JCFieldAccess tree) {
		DEBUG.P(this,"visitSelect(1)");
		Symbol sym = tree.sym;
		
		DEBUG.P("tree.name="+tree.name);
		if (tree.name == names._class) {
			assert target.hasClassLiterals();
			code.emitop2(ldc2, makeRef(tree.pos(), tree.selected.type));
			result = items.makeStackItem(pt);
			return;
		}

		Symbol ssym = TreeInfo.symbol(tree.selected);
		
		DEBUG.P("ssym="+ssym);
		if(ssym != null) {
			DEBUG.P("ssym.kind="+Kinds.toString(ssym.kind));
			DEBUG.P("ssym.name="+ssym.name);
		}

		// Are we selecting via super?
		boolean selectSuper =
			ssym != null && (ssym.kind == TYP || ssym.name == names._super);
		
		DEBUG.P("");
		DEBUG.P("selectSuper="+selectSuper);
		
		// Are we accessing a member of the superclass in an access method
		// resulting from a qualified super?
		boolean accessSuper = isAccessSuper(env.enclMethod);
		
		DEBUG.P("accessSuper="+accessSuper);
		
		Item base = (selectSuper)
			? items.makeSuperItem()
			: genExpr(tree.selected, tree.selected.type);
		
		DEBUG.P("");
		DEBUG.P("sym.kind="+Kinds.toString(sym.kind));
		if (sym.kind == VAR && ((VarSymbol) sym).getConstValue() != null) {
			// We are seeing a variable that is constant but its selecting
			// expression is not.
			if ((sym.flags() & STATIC) != 0) {
				if (!selectSuper && (ssym == null || ssym.kind != TYP))
					base = base.load();
				base.drop();
			} else {
				base.load();
				genNullCheck(tree.selected.pos());
			}
			result = items.
			makeImmediateItem(sym.type, ((VarSymbol) sym).getConstValue());
		} else {
			if (!accessSuper)
				sym = binaryQualifier(sym, tree.selected.type);
			if ((sym.flags() & STATIC) != 0) {
				if (!selectSuper && (ssym == null || ssym.kind != TYP))
					base = base.load();
				base.drop();
				result = items.makeStaticItem(sym);
			} else {
			base.load();
				if (sym == syms.lengthVar) {
					code.emitop0(arraylength);
					result = items.makeStackItem(syms.intType);
				} else {
					result = items.
					makeMemberItem(sym,
							   (sym.flags() & PRIVATE) != 0 ||
							   selectSuper || accessSuper);
				}
			}
		}
		DEBUG.P(0,this,"visitSelect(1)");
    }

    public void visitLiteral(JCLiteral tree) {
		DEBUG.P(this,"visitLiteral(1)");
		DEBUG.P("tree="+tree);
		DEBUG.P("tree.type.tag="+TypeTags.toString(tree.type.tag));
		if (tree.type.tag == TypeTags.BOT) {
			code.emitop0(aconst_null);
			
			DEBUG.P("types.dimensions(pt)="+types.dimensions(pt));
			if (types.dimensions(pt) > 1) {//大于等于二维数组时条件才为true
			//给多维数组变量赋null时，先把null转换成多维数组类型
				code.emitop2(checkcast, makeRef(tree.pos(), pt));
				result = items.makeStackItem(pt);
			} else {
				//一维数组不用转换
				result = items.makeStackItem(tree.type);
			}
		}
		else
			result = items.makeImmediateItem(tree.type, tree.value);
		
		DEBUG.P(0,this,"visitLiteral(1)");
    }

    public void visitLetExpr(LetExpr tree) {
		DEBUG.P(this,"visitLetExpr(1)");
		int limit = code.nextreg;
		genStats(tree.defs, env);
		result = genExpr(tree.expr, tree.expr.type).load();
		code.endScopes(limit);
		DEBUG.P(0,this,"visitLetExpr(1)");
    }

/* ************************************************************************
 * main method
 *************************************************************************/

    /** Generate code for a class definition.
     *  @param env   The attribution environment that belongs to the
     *               outermost class containing this class definition.
     *               We need this for resolving some additional symbols.
     *  @param cdef  The tree representing the class definition.
     *  @return      True if code is generated with no errors.
     */
    public boolean genClass(Env<AttrContext> env, JCClassDecl cdef) {
		DEBUG.P(this,"genClass(2) 正在生成字节码......");
		DEBUG.P("cdef="+cdef);
		DEBUG.P("env="+env);
		try {
			attrEnv = env;
			ClassSymbol c = cdef.sym;
			this.toplevel = env.toplevel;
			this.endPositions = toplevel.endPositions;
			
			DEBUG.P("generateIproxies="+generateIproxies);
			DEBUG.P("allowGenerics="+allowGenerics);
			DEBUG.P("c="+c);
			DEBUG.P("c.flags()="+Flags.toString(c.flags()));

			// If this is a class definition requiring Miranda methods,
			// add them.
			if (generateIproxies && //jdk1.1与jdk1.0才需要
			(c.flags() & (INTERFACE|ABSTRACT)) == ABSTRACT
			&& !allowGenerics // no Miranda methods available with generics
			)
			implementInterfaceMethods(c);
			
			cdef.defs = normalizeDefs(cdef.defs, c);
			//经过normalizeDefs(cdef.defs, c)后，类体(defs)中只包含方法(构造方法和非构造方法)
			//内部类或内部接口也不包含在类体(defs)中
			DEBUG.P("cdef.defs(规范化后的类体)="+cdef.defs);
			c.pool = pool;
			pool.reset();
			Env<GenContext> localEnv =
			new Env<GenContext>(cdef, new GenContext());
			localEnv.toplevel = env.toplevel;
			localEnv.enclClass = cdef;
			
			int myMethodCount=1;
			DEBUG.P(2);DEBUG.P("开始为每一个方法生成字节码...(方法总个数: "+cdef.defs.size()+")");
			for (List<JCTree> l = cdef.defs; l.nonEmpty(); l = l.tail) {
			DEBUG.P("第 "+myMethodCount+" 个方法开始...");
			genDef(l.head, localEnv);
			DEBUG.P("第 "+myMethodCount+" 个方法结束...");
			myMethodCount++;DEBUG.P(2);
			}
			
			if (pool.numEntries() > Pool.MAX_ENTRIES) {
			log.error(cdef.pos(), "limit.pool");
			nerrs++;
			}
			if (nerrs != 0) {
			// if errors, discard code
			for (List<JCTree> l = cdef.defs; l.nonEmpty(); l = l.tail) {
				if (l.head.tag == JCTree.METHODDEF)
				((JCMethodDecl) l.head).sym.code = null;
			}
			}
				cdef.defs = List.nil(); // discard trees
			return nerrs == 0;
		} finally {
			// note: this method does NOT support recursion.
			attrEnv = null;
			this.env = null;
			toplevel = null;
			endPositions = null;
			nerrs = 0;
			DEBUG.P(2,this,"genClass(2)");
		}
    }

/* ************************************************************************
 * Auxiliary classes
 *************************************************************************/

    /** An abstract class for finalizer generation.
     */
    abstract class GenFinalizer {
		/** Generate code to clean up when unwinding. */
		abstract void gen();

		/** Generate code to clean up at last. */
		abstract void genLast();

		/** Does this finalizer have some nontrivial cleanup to perform? */
		boolean hasFinalizer() { return true; }
    }

    /** code generation contexts,
     *  to be used as type parameter for environments.
     */
    static class GenContext {

		/** A chain for all unresolved jumps that exit the current environment.
		 */
		Chain exit = null;

		/** A chain for all unresolved jumps that continue in the
		 *  current environment.
		 */
		Chain cont = null;

		/** A closure that generates the finalizer of the current environment.
		 *  Only set for Synchronized and Try contexts.
		 */
		GenFinalizer finalize = null;

		/** Is this a switch statement?  If so, allocate registers
		 * even when the variable declaration is unreachable.
		 */
		boolean isSwitch = false;

			/** A list buffer containing all gaps in the finalizer range,
		 *  where a catch all exception should not apply.
		 */
		ListBuffer<Integer> gaps = null;

		/** Add given chain to exit chain.
		 */
		void addExit(Chain c)  {
			exit = Code.mergeChains(c, exit);
		}

		/** Add given chain to cont chain.
		 */
		void addCont(Chain c) {
			cont = Code.mergeChains(c, cont);
		}
		
		//我加上的
		public String toString() {
			return "GC[gaps="+gaps+", exit="+exit+", cont="+cont+", isSwitch="+isSwitch+", finalize="+finalize+"]";
		}
    }
}
