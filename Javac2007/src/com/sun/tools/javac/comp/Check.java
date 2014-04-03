/*
 * @(#)Check.java	1.169 07/03/21
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

import com.sun.tools.javac.code.*;
import com.sun.tools.javac.jvm.*;
import com.sun.tools.javac.tree.*;
import com.sun.tools.javac.util.*;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;
import com.sun.tools.javac.util.List;

import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.code.Lint;
import com.sun.tools.javac.code.Lint.LintCategory;
import com.sun.tools.javac.code.Type.*;
import com.sun.tools.javac.code.Symbol.*;

import static com.sun.tools.javac.code.Flags.*;
import static com.sun.tools.javac.code.Kinds.*;
import static com.sun.tools.javac.code.TypeTags.*;

/** Type checking helper class for the attribution phase.
 *
 *  <p><b>This is NOT part of any API supported by Sun Microsystems.  If
 *  you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 */
@Version("@(#)Check.java	1.169 07/03/21")
public class Check {
    private static my.Debug DEBUG=new my.Debug(my.Debug.Check);//我加上的
	
    protected static final Context.Key<Check> checkKey =
	new Context.Key<Check>();

    private final Name.Table names;
    private final Log log;
    private final Symtab syms;
    private final Infer infer;
    private final Target target;
    private final Source source;
    private final Types types;
    private final boolean skipAnnotations;
    private final TreeInfo treeinfo;

    // The set of lint options currently in effect. It is initialized
    // from the context, and then is set/reset as needed by Attr as it 
    // visits all the various parts of the trees during attribution.
    private Lint lint;

    public static Check instance(Context context) {
	Check instance = context.get(checkKey);
	if (instance == null)
	    instance = new Check(context);
	return instance;
    }

    protected Check(Context context) {
        DEBUG.P(this,"Check(1)");
    
	context.put(checkKey, this);

	names = Name.Table.instance(context);
	log = Log.instance(context);
	syms = Symtab.instance(context);
	infer = Infer.instance(context);
	this.types = Types.instance(context);
	Options options = Options.instance(context);
	target = Target.instance(context);
        source = Source.instance(context);
	lint = Lint.instance(context);
        treeinfo = TreeInfo.instance(context);

	Source source = Source.instance(context);
	allowGenerics = source.allowGenerics();
	allowAnnotations = source.allowAnnotations();
	complexInference = options.get("-complexinference") != null;
	skipAnnotations = options.get("skipAnnotations") != null;

	boolean verboseDeprecated = lint.isEnabled(LintCategory.DEPRECATION);
	boolean verboseUnchecked = lint.isEnabled(LintCategory.UNCHECKED);

	deprecationHandler = new MandatoryWarningHandler(log,verboseDeprecated, "deprecated");
	uncheckedHandler = new MandatoryWarningHandler(log, verboseUnchecked, "unchecked");
    
        DEBUG.P(0,this,"Check(1)");
    }

    /** Switch: generics enabled?
     */
    boolean allowGenerics;

    /** Switch: annotations enabled?
     */
    boolean allowAnnotations;

    /** Switch: -complexinference option set?
     */
    boolean complexInference;

    /** A table mapping flat names of all compiled classes in this run to their
     *  symbols; maintained from outside.
     */
    public Map<Name,ClassSymbol> compiled = new HashMap<Name, ClassSymbol>();

    /** A handler for messages about deprecated usage.
     */
    private MandatoryWarningHandler deprecationHandler;

    /** A handler for messages about unchecked or unsafe usage.
     */
    private MandatoryWarningHandler uncheckedHandler;


/* *************************************************************************
 * Errors and Warnings
 **************************************************************************/

    Lint setLint(Lint newLint) {
		Lint prev = lint;
		lint = newLint;
		return prev;
    }

    /** Warn about deprecated symbol.
     *  @param pos        Position to be used for error reporting.
     *  @param sym        The deprecated symbol.
     */ 
    void warnDeprecated(DiagnosticPosition pos, Symbol sym) {
		if (!lint.isSuppressed(LintCategory.DEPRECATION))
			deprecationHandler.report(pos, "has.been.deprecated", sym, sym.location());
    }

    /** Warn about unchecked operation.
     *  @param pos        Position to be used for error reporting.
     *  @param msg        A string describing the problem.
     */
    public void warnUnchecked(DiagnosticPosition pos, String msg, Object... args) {
	if (!lint.isSuppressed(LintCategory.UNCHECKED))
	    uncheckedHandler.report(pos, msg, args);
    }

    /**
     * Report any deferred diagnostics.
     */
    public void reportDeferredDiagnostics() {
	deprecationHandler.reportDeferredDiagnostic();
	uncheckedHandler.reportDeferredDiagnostic();
    }


    /** Report a failure to complete a class.
     *  @param pos        Position to be used for error reporting.
     *  @param ex         The failure to report.
     */
    public Type completionError(DiagnosticPosition pos, CompletionFailure ex) {
		log.error(pos, "cant.access", ex.sym, ex.errmsg);
		//com.sun.tools.javac.jvm.ClassReader.BadClassFile继承自
		//com.sun.tools.javac.code.Symbol.CompletionFailure
		if (ex instanceof ClassReader.BadClassFile) throw new Abort();
		else return syms.errType;
    }

    /** Report a type error.
     *  @param pos        Position to be used for error reporting.
     *  @param problem    A string describing the error.
     *  @param found      The type that was found.
     *  @param req        The type that was required.
     */
    Type typeError(DiagnosticPosition pos, Object problem, Type found, Type req) {
	log.error(pos, "prob.found.req",
		  problem, found, req);
	return syms.errType;
    }

    Type typeError(DiagnosticPosition pos, String problem, Type found, Type req, Object explanation) {
	log.error(pos, "prob.found.req.1", problem, found, req, explanation);
	return syms.errType;
    }

    /** Report an error that wrong type tag was found.
     *  @param pos        Position to be used for error reporting.
     *  @param required   An internationalized string describing the type tag
     *                    required.
     *  @param found      The type that was found.
     */
    Type typeTagError(DiagnosticPosition pos, Object required, Object found) {
	log.error(pos, "type.found.req", found, required);
	return syms.errType;
    }

    /** Report an error that symbol cannot be referenced before super
     *  has been called.
     *  @param pos        Position to be used for error reporting.
     *  @param sym        The referenced symbol.
     */
    void earlyRefError(DiagnosticPosition pos, Symbol sym) {
	log.error(pos, "cant.ref.before.ctor.called", sym);
    }

    /** Report duplicate declaration error.
     */
    void duplicateError(DiagnosticPosition pos, Symbol sym) {
    DEBUG.P(this,"duplicateError(2)");
	DEBUG.P("sym="+sym);
	
	if (!sym.type.isErroneous()) {
	    log.error(pos, "already.defined", sym, sym.location());
	}
	
	DEBUG.P(0,this,"duplicateError(2)");
    }

    /** Report array/varargs duplicate declaration 
     */
    void varargsDuplicateError(DiagnosticPosition pos, Symbol sym1, Symbol sym2) {
	DEBUG.P(this,"varargsDuplicateError(3)");
	DEBUG.P("sym1="+sym1+"  sym2="+sym2);
	
	if (!sym1.type.isErroneous() && !sym2.type.isErroneous()) {
	    log.error(pos, "array.and.varargs", sym1, sym2, sym2.location());
	}
	
	DEBUG.P(this,"varargsDuplicateError(3)");
    }

/* ************************************************************************
 * duplicate declaration checking
 *************************************************************************/

    /** Check that variable does not hide variable with same name in
     *	immediately enclosing local scope.
     *	@param pos	     Position for error reporting.
     *	@param v	     The symbol.
     *	@param s	     The scope.
     */
    void checkTransparentVar(DiagnosticPosition pos, VarSymbol v, Scope s) {
		try {//我加上的
		DEBUG.P(this,"checkTransparentVar(3)");
		DEBUG.P("VarSymbol v="+v);
		DEBUG.P("Scope s="+s);
		DEBUG.P("s.next="+s.next);
		
		if (s.next != null) {
			Scope.Entry e2 = s.next.lookup(v.name);
			DEBUG.P("e2.scope="+e2.scope);
			if(e2.scope != null) {
				DEBUG.P("e.sym.owner="+e2.sym.owner);
				DEBUG.P("v.owner="+v.owner);
			}
			for (Scope.Entry e = s.next.lookup(v.name);
				 e.scope != null && e.sym.owner == v.owner;
				 e = e.next()) {
				if (e.sym.kind == VAR &&
				   (e.sym.owner.kind & (VAR | MTH)) != 0 &&
				    v.name != names.error) {
					//如:void methodD(int i) { int i; }
					duplicateError(pos, e.sym);
					return;
				}
			}
		}		
		}finally{//我加上的
		DEBUG.P(0,this,"checkTransparentVar(3)");
		}
    }

    /** Check that a class or interface does not hide a class or
     *	interface with same name in immediately enclosing local scope.
     *	@param pos	     Position for error reporting.
     *	@param c	     The symbol.
     *	@param s	     The scope.
     */
    void checkTransparentClass(DiagnosticPosition pos, ClassSymbol c, Scope s) {
		try {//我加上的
		DEBUG.P(this,"checkTransparentClass(3)");
		DEBUG.P("c="+c);
		DEBUG.P("s="+s);
		DEBUG.P("s.next="+s.next);
		/*例:这个例子不对
		class EnterTest {
			void methodA() {
				class EnterTest{}
			}
		}*/
		if (s.next != null) {
			for (Scope.Entry e = s.next.lookup(c.name);
			 e.scope != null && e.sym.owner == c.owner;
			 e = e.next()) {
				if (e.sym.kind == TYP &&
					(e.sym.owner.kind & (VAR | MTH)) != 0 &&
					c.name != names.error) {
					duplicateError(pos, e.sym);
					return;
				}
			}
		}

		}finally{//我加上的
		DEBUG.P(0,this,"checkTransparentClass(3)");
		}
    }

    /** Check that class does not have the same name as one of
     *	its enclosing classes, or as a class defined in its enclosing scope.
     *	return true if class is unique in its enclosing scope.
     *	@param pos	     Position for error reporting.
     *	@param name	     The class name.
     *	@param s	     The enclosing scope.
     */
    boolean checkUniqueClassName(DiagnosticPosition pos, Name name, Scope s) {
		try {//我加上的
		DEBUG.P(this,"checkUniqueClassName(3)");
		DEBUG.P("name="+name);
		DEBUG.P("Scope s="+s);
		
		//各成员名不能重复
		for (Scope.Entry e = s.lookup(name); e.scope == s; e = e.next()) {
			if (e.sym.kind == TYP && e.sym.name != names.error) {
			duplicateError(pos, e.sym);
			return false;
			}
		}
		
		//各成员名不能与此成员的直接或间接owner有相同名称
		for (Symbol sym = s.owner; sym != null; sym = sym.owner) {
			if (sym.kind == TYP && sym.name == name && sym.name != names.error) {
			duplicateError(pos, sym);
			return true;
			}
		}
		return true;
		
		}finally{//我加上的
		DEBUG.P(0,this,"checkUniqueClassName(3)");
		}
    }

/* *************************************************************************
 * Class name generation
 **************************************************************************/

    /** Return name of local class.
     *  This is of the form    <enclClass> $ n <classname>
     *  where
     *    enclClass is the flat name of the enclosing class,
     *    classname is the simple name of the local class
     */
    Name localClassName(ClassSymbol c) {
		for (int i=1; ; i++) {
			Name flatname = names.
			fromString("" + c.owner.enclClass().flatname +
							   target.syntheticNameChar() + i +
							   c.name);
			if (compiled.get(flatname) == null) return flatname;
		}
    }

/* *************************************************************************
 * Type Checking
 **************************************************************************/

    /** Check that a given type is assignable to a given proto-type.
     *  If it is, return the type, otherwise return errType.
     *  @param pos        Position to be used for error reporting.
     *  @param found      The type that was found.
     *  @param req        The type that was required.
     */
    Type checkType(DiagnosticPosition pos, Type found, Type req) {
		try {//我加上的
		DEBUG.P(this,"checkType(3)");
		DEBUG.P("found.tag="+TypeTags.toString(found.tag));
		DEBUG.P("req.tag="+TypeTags.toString(req.tag));

		if (req.tag == ERROR)
			return req;
		if (found.tag == FORALL)
			return instantiatePoly(pos, (ForAll)found, req, convertWarner(pos, found, req));
		if (req.tag == NONE)
			return found;
		if (types.isAssignable(found, req, convertWarner(pos, found, req)))
			return found;
		if (found.tag <= DOUBLE && req.tag <= DOUBLE)
			return typeError(pos, JCDiagnostic.fragment("possible.loss.of.precision"), found, req);
		if (found.isSuperBound()) {
			log.error(pos, "assignment.from.super-bound", found);
			return syms.errType;
		}
		if (req.isExtendsBound()) {
			log.error(pos, "assignment.to.extends-bound", req);
			return syms.errType;
		}
		return typeError(pos, JCDiagnostic.fragment("incompatible.types"), found, req);
		
		
		}finally{//我加上的
		DEBUG.P(0,this,"checkType(3)");
		}
    }

    /** Instantiate polymorphic type to some prototype, unless
     *  prototype is `anyPoly' in which case polymorphic type
     *  is returned unchanged.
     */
    Type instantiatePoly(DiagnosticPosition pos, ForAll t, Type pt, Warner warn) {
	if (pt == Infer.anyPoly && complexInference) {
	    return t;
	} else if (pt == Infer.anyPoly || pt.tag == NONE) {
	    Type newpt = t.qtype.tag <= VOID ? t.qtype : syms.objectType;
	    return instantiatePoly(pos, t, newpt, warn);
	} else if (pt.tag == ERROR) {
	    return pt;
	} else {
	    try {
		return infer.instantiateExpr(t, pt, warn);
	    } catch (Infer.NoInstanceException ex) {
		if (ex.isAmbiguous) {
		    JCDiagnostic d = ex.getDiagnostic();
		    log.error(pos,
			      "undetermined.type" + (d!=null ? ".1" : ""),
			      t, d);
		    return syms.errType;
		} else {
		    JCDiagnostic d = ex.getDiagnostic();
		    return typeError(pos,
                                     JCDiagnostic.fragment("incompatible.types" + (d!=null ? ".1" : ""), d),
                                     t, pt);
		}
	    }
	}
    }

    /** Check that a given type can be cast to a given target type.
     *  Return the result of the cast.
     *  @param pos        Position to be used for error reporting.
     *  @param found      The type that is being cast.
     *  @param req        The target type of the cast.
     */
    Type checkCastable(DiagnosticPosition pos, Type found, Type req) {
	if (found.tag == FORALL) {
	    instantiatePoly(pos, (ForAll) found, req, castWarner(pos, found, req));
	    return req;
	} else if (types.isCastable(found, req, castWarner(pos, found, req))) {
	    return req;
	} else {
	    return typeError(pos,
			     JCDiagnostic.fragment("inconvertible.types"),
			     found, req);
	}
    }
//where
        /** Is type a type variable, or a (possibly multi-dimensional) array of
	 *  type variables?
	 */
	boolean isTypeVar(Type t) {
	    return t.tag == TYPEVAR || t.tag == ARRAY && isTypeVar(types.elemtype(t));
	}

    /** Check that a type is within some bounds.
     *
     *  Used in TypeApply to verify that, e.g., X in V<X> is a valid
     *  type argument.
     *  @param pos           Position to be used for error reporting.
     *  @param a             The type that should be bounded by bs.
     *  @param bs            The bound.
     */
    private void checkExtends(DiagnosticPosition pos, Type a, TypeVar bs) {
		try {//我加上的
		DEBUG.P(this,"checkExtends(3)");
		DEBUG.P("a="+a);
		DEBUG.P("a.tag="+TypeTags.toString(a.tag));
		DEBUG.P("a.isUnbound()="+a.isUnbound());
		DEBUG.P("a.isExtendsBound()="+a.isExtendsBound());
		DEBUG.P("a.isSuperBound()="+a.isSuperBound());
		DEBUG.P("bs="+bs);

		//测试upperBound与lowerBound
		//types.upperBound(a);
		//types.lowerBound(a);

		if (a.isUnbound()) {
			return;
		} else if (a.tag != WILDCARD) {
			a = types.upperBound(a);
			for (List<Type> l = types.getBounds(bs); l.nonEmpty(); l = l.tail) {
				if (!types.isSubtype(a, l.head)) {
					log.error(pos, "not.within.bounds", a);
					return;
				}
			}
		} else if (a.isExtendsBound()) {
			if (!types.isCastable(bs.getUpperBound(), types.upperBound(a), Warner.noWarnings))
				log.error(pos, "not.within.bounds", a);
		} else if (a.isSuperBound()) {
			if (types.notSoftSubtype(types.lowerBound(a), bs.getUpperBound()))
				log.error(pos, "not.within.bounds", a);
		}
		
		}finally{//我加上的
		DEBUG.P(1,this,"checkExtends(3)");
		}
    }

    /** Check that type is different from 'void'.
     *  @param pos           Position to be used for error reporting.
     *  @param t             The type to be checked.
     */
    Type checkNonVoid(DiagnosticPosition pos, Type t) {
		if (t.tag == VOID) {
			log.error(pos, "void.not.allowed.here");
			return syms.errType;
		} else {
			return t;
		}
    }

    /** Check that type is a class or interface type.
     *  @param pos           Position to be used for error reporting.
     *  @param t             The type to be checked.
     */
    Type checkClassType(DiagnosticPosition pos, Type t) {
		try {//我加上的
		DEBUG.P(this,"checkClassType(2)");
		DEBUG.P("t="+t+"  t.tag="+TypeTags.toString(t.tag));
	
        /*src/my/test/EnterTest.java:23: 意外的类型
        找到： 类型参数 T 
        需要： 类
        public class EnterTest<T,S> extends T implements EnterTestInterfaceA,EnterTestInterfaceB {                                         ^
        */
		if (t.tag != CLASS && t.tag != ERROR)
            return typeTagError(pos,
                                JCDiagnostic.fragment("type.req.class"),
                                (t.tag == TYPEVAR)
                                ? JCDiagnostic.fragment("type.parameter", t)
                                : t); 
		else
			return t;
	    
		}finally{//我加上的
		DEBUG.P(0,this,"checkClassType(2)");
		}
    }
    

    /** Check that type is a class or interface type.
     *  @param pos           Position to be used for error reporting.
     *  @param t             The type to be checked.
     *  @param noBounds    True if type bounds are illegal here.
     */
    Type checkClassType(DiagnosticPosition pos, Type t, boolean noBounds) {
    try {//我加上的
	DEBUG.P(this,"checkClassType(3)");
	DEBUG.P("t="+t);
	DEBUG.P("t.tag="+TypeTags.toString(t.tag));
	DEBUG.P("t.isParameterized()="+t.isParameterized());
	DEBUG.P("noBounds="+noBounds);
	
	t = checkClassType(pos, t);
	DEBUG.P("t="+t);
	DEBUG.P("t.tag="+TypeTags.toString(t.tag));
	DEBUG.P("t.isParameterized()="+t.isParameterized());
	DEBUG.P("noBounds="+noBounds);
	//noBounds为true时表示t的类型参数不能是WILDCARD(即: <?>、<? extends ...>、<? super ...>)
	if (noBounds && t.isParameterized()) {
	    List<Type> args = t.getTypeArguments();
	    while (args.nonEmpty()) {
	    DEBUG.P("args.head.tag="+TypeTags.toString(args.head.tag));
	    /*报错如下:
	    bin\mysrc\my\test\Test.java:85: unexpected type
		found   : ?
		required: class or interface without bounds
		public class Test<S,T> extends TestOhter<?,String> implements MyInterfaceA,MyInterfaceB {                                       ^
		1 error
		*/
		if (args.head.tag == WILDCARD)
		    return typeTagError(pos,
					log.getLocalizedString("type.req.exact"),
					args.head);
		args = args.tail;
	    }
	}
	return t;
	
	}finally{//我加上的
	DEBUG.P(0,this,"checkClassType(3)");
	}
	
    }

    /** Check that type is a reifiable class, interface or array type.
     *  @param pos           Position to be used for error reporting.
     *  @param t             The type to be checked.
     */
    Type checkReifiableReferenceType(DiagnosticPosition pos, Type t) {
	if (t.tag != CLASS && t.tag != ARRAY && t.tag != ERROR) {
	    return typeTagError(pos,
				JCDiagnostic.fragment("type.req.class.array"),
				t);
	} else if (!types.isReifiable(t)) {
	    log.error(pos, "illegal.generic.type.for.instof");
	    return syms.errType;
	} else {
	    return t;
	}
    }

    /** Check that type is a reference type, i.e. a class, interface or array type
     *  or a type variable.
     *  @param pos           Position to be used for error reporting.
     *  @param t             The type to be checked.
     */
    Type checkRefType(DiagnosticPosition pos, Type t) {
		try {//我加上的
		DEBUG.P(this,"checkRefType(2)");
		DEBUG.P("t="+t+"  t.tag="+TypeTags.toString(t.tag));
		
		switch (t.tag) {
		case CLASS:
		case ARRAY:
		case TYPEVAR:
		case WILDCARD:
		case ERROR:
			return t;
		default:
		/*例子:
		bin\mysrc\my\test\Test.java:8: 意外的类型
		找到： int
		需要： 引用
				MyTestInnerClass<Z extends ExtendsTest<int,? super ExtendsTest>>
													   ^
		*/
	    return typeTagError(pos,
				JCDiagnostic.fragment("type.req.ref"),
				t);
		}
		
		}finally{//我加上的
		DEBUG.P(0,this,"checkRefType(2)");
		}
    }

    /** Check that type is a null or reference type.
     *  @param pos           Position to be used for error reporting.
     *  @param t             The type to be checked.
     */
    Type checkNullOrRefType(DiagnosticPosition pos, Type t) {
	switch (t.tag) {
	case CLASS:
	case ARRAY:
	case TYPEVAR:
	case WILDCARD:
	case BOT:
	case ERROR:
	    return t;
	default:
	    return typeTagError(pos,
				JCDiagnostic.fragment("type.req.ref"),
				t);
	}
    }

    /** Check that flag set does not contain elements of two conflicting sets. s
     *  Return true if it doesn't.
     *  @param pos           Position to be used for error reporting.
     *  @param flags         The set of flags to be checked.
     *  @param set1          Conflicting flags set #1.
     *  @param set2          Conflicting flags set #2.
     */
    boolean checkDisjoint(DiagnosticPosition pos, long flags, long set1, long set2) {
        if ((flags & set1) != 0 && (flags & set2) != 0) {
            log.error(pos,
		      "illegal.combination.of.modifiers",
		      TreeInfo.flagNames(TreeInfo.firstFlag(flags & set1)),
		      TreeInfo.flagNames(TreeInfo.firstFlag(flags & set2)));
            return false;
        } else
            return true;
    }

    /** Check that given modifiers are legal for given symbol and
     *  return modifiers together with any implicit modififiers for that symbol.
     *  Warning: we can't use flags() here since this method
     *  is called during class enter, when flags() would cause a premature
     *  completion.
     *  @param pos           Position to be used for error reporting.
     *  @param flags         The set of modifiers given in a definition.
     *  @param sym           The defined symbol.
     */
    long checkFlags(DiagnosticPosition pos, long flags, Symbol sym, JCTree tree) {
		DEBUG.P(this,"checkFlags(4)");
		DEBUG.P("flags="+Flags.toString(flags));
		DEBUG.P("sym.kind="+Kinds.toString(sym.kind));

		long mask;
		long implicit = 0;
		switch (sym.kind) {
			case VAR:
				if (sym.owner.kind != TYP)
					mask = LocalVarFlags; //本地变量
				else if ((sym.owner.flags_field & INTERFACE) != 0)
					mask = implicit = InterfaceVarFlags; //接口中定义的字段
				else
					mask = VarFlags; //类中定义的字段
				break;
			case MTH:
				DEBUG.P("sym.name="+sym.name);
				DEBUG.P("if (sym.name == names.init)="+(sym.name == names.init));
				DEBUG.P("sym.owner.flags_field="+Flags.toString(sym.owner.flags_field));
				if (sym.name == names.init) {
					if ((sym.owner.flags_field & ENUM) != 0) { 
						/*错误例子:
						bin\mysrc\my\test\Test.java:16: 此处不允许使用修饰符 public
						   public MyInnerEnum() {}
								  ^
						bin\mysrc\my\test\Test.java:16: 此处不允许使用修饰符 protected
							protected MyInnerEnum() {}
									  ^         
						*/
						// enum constructors cannot be declared public or
						// protected and must be implicitly or explicitly
						// private
						implicit = PRIVATE;
						mask = PRIVATE;
					} else
						mask = ConstructorFlags;
				} else if ((sym.owner.flags_field & INTERFACE) != 0)
					mask = implicit = InterfaceMethodFlags;
				else {
					mask = MethodFlags;
				}

				//如果方法不是抽象的(abstract)，
				//并且定义方法的类型含有strictfp修饰符，
				//则此方法也默认含有strictfp修饰符
				//接口方法默认是public abstract，不会有strictfp修饰符
				DEBUG.P("((flags|implicit) & Flags.ABSTRACT)="+Flags.toString(((flags|implicit) & Flags.ABSTRACT)));
				// Imply STRICTFP if owner has STRICTFP set.
				if (((flags|implicit) & Flags.ABSTRACT) == 0) //参考<<深入java虚拟机>>P290
					implicit |= sym.owner.flags_field & STRICTFP;
				DEBUG.P("implicit="+Flags.toString(implicit));
				break;
			case TYP:
				DEBUG.P("sym.isLocal()="+sym.isLocal());
				DEBUG.P("sym.owner.kind="+Kinds.toString(sym.owner.kind));
				if (sym.isLocal()) {
					mask = LocalClassFlags;
					if (sym.name.len == 0) { // Anonymous class
						// Anonymous classes in static methods are themselves static;
						// that's why we admit STATIC here.
						mask |= STATIC;
						// JLS: Anonymous classes are final.
						implicit |= FINAL;
					}
						
					if ((sym.owner.flags_field & STATIC) == 0 &&
						(flags & ENUM) != 0)
						log.error(pos, "enums.must.be.static");
				} else if (sym.owner.kind == TYP) {
					mask = MemberClassFlags;
					if (sym.owner.owner.kind == PCK ||
						(sym.owner.flags_field & STATIC) != 0)
						mask |= STATIC;
					/*源码例子:
					public class Test {
						public class MyInnerClass {
							public enum MyInnerEnum2{}
						}
					}
					
					错误提示:
					bin\mysrc\my\test\Test.java:11: 只有在静态上下文中才允许使用枚举声明
							public enum MyInnerEnum2{}
								   ^
					*/
					else if ((flags & ENUM) != 0)
						log.error(pos, "enums.must.be.static");
					// Nested interfaces and enums are always STATIC (Spec ???)
					if ((flags & (INTERFACE | ENUM)) != 0 ) implicit = STATIC;
				} else {
					mask = ClassFlags;
				}
				// Interfaces are always ABSTRACT
				if ((flags & INTERFACE) != 0) implicit |= ABSTRACT;

				if ((flags & ENUM) != 0) {
					// enums can't be declared abstract or final
					mask &= ~(ABSTRACT | FINAL);
					implicit |= implicitEnumFinalFlag(tree);
				}
				// Imply STRICTFP if owner has STRICTFP set.
				implicit |= sym.owner.flags_field & STRICTFP;
				break;
			default:
				throw new AssertionError();
		}

		//mask的值表示能用在VAR、MTH、TYP前的所有修饰符的集合(见Flags类中的Modifier masks)
		//如果在一个接口中这样定义一个方法:strictfp void methodA();
		//因为mask = InterfaceMethodFlags = ABSTRACT | PUBLIC
		//所以就会报错:此处不允许使用修饰符 strictfp
		long illegal = flags & StandardFlags & ~mask;
        if (illegal != 0) {
			if ((illegal & INTERFACE) != 0) {
				log.error(pos, "intf.not.allowed.here");
				mask |= INTERFACE;
			}
			else {
				log.error(pos, "mod.not.allowed.here", TreeInfo.flagNames(illegal));
			}
		}
        else if ((sym.kind == TYP ||
		  // ISSUE: Disallowing abstract&private is no longer appropriate
		  // in the presence of inner classes. Should it be deleted here?
		  checkDisjoint(pos, flags,
				ABSTRACT,  //ABSTRACT与"PRIVATE,STATIC"其中之一不能同时出现
				PRIVATE | STATIC))//如static abstract void methodC();非法的修饰符组合 abstract 和  static
		 && //下面的checkDisjoint同上，都是第三个参数与第四个参数中所含的修饰符不能同时出现在flags中
		 checkDisjoint(pos, flags,
			       ABSTRACT | INTERFACE,
			       FINAL | NATIVE | SYNCHRONIZED)
		 &&
                 checkDisjoint(pos, flags,
                               PUBLIC,
                               PRIVATE | PROTECTED)
		 &&
                 checkDisjoint(pos, flags,
                               PRIVATE,
                               PUBLIC | PROTECTED)
		 &&
		 checkDisjoint(pos, flags,
			       FINAL,
			       VOLATILE)
		 &&
		 (sym.kind == TYP ||
		  checkDisjoint(pos, flags,
				ABSTRACT | NATIVE,
				STRICTFP))) {
	    // skip
        }
        DEBUG.P("flags="+Flags.toString(flags));
        DEBUG.P("mask="+Flags.toString(mask));
        DEBUG.P("implicit="+Flags.toString(implicit));
        DEBUG.P("returnFlags="+(flags & (mask | ~StandardFlags) | implicit)+"("+Flags.toString((flags & (mask | ~StandardFlags) | implicit))+")");
        DEBUG.P(0,this,"checkFlags(4)");
        return flags & (mask | ~StandardFlags) | implicit;
    }


    /** Determine if this enum should be implicitly final.
     *
     *  If the enum has no specialized enum contants, it is final.
     *
     *  If the enum does have specialized enum contants, it is
     *  <i>not</i> final.
     */
    private long implicitEnumFinalFlag(JCTree tree) {
	if (tree.tag != JCTree.CLASSDEF) return 0;
        class SpecialTreeVisitor extends JCTree.Visitor {
            boolean specialized;
            SpecialTreeVisitor() {
                this.specialized = false;
            };
		
            public void visitTree(JCTree tree) { /* no-op */ }
		
            public void visitVarDef(JCVariableDecl tree) {
                if ((tree.mods.flags & ENUM) != 0) {
                    if (tree.init instanceof JCNewClass &&
                        ((JCNewClass) tree.init).def != null) {
                        specialized = true;
                    }
                }
            }
        }

        SpecialTreeVisitor sts = new SpecialTreeVisitor();
        JCClassDecl cdef = (JCClassDecl) tree;
        for (JCTree defs: cdef.defs) {
            defs.accept(sts);
            if (sts.specialized) return 0;
        }
        return FINAL;
    }

/* *************************************************************************
 * Type Validation
 **************************************************************************/

    /** Validate a type expression. That is,
     *  check that all type arguments of a parametric type are within
     *  their bounds. This must be done in a second phase after type attributon
     *  since a class might have a subclass as type parameter bound. E.g:
     *
     *  class B<A extends C> { ... }
     *  class C extends B<C> { ... }
     *
     *  and we can't make sure that the bound is already attributed because
     *  of possible cycles.
     */
    private Validator validator = new Validator();

    /** Visitor method: Validate a type expression, if it is not null, catching
     *  and reporting any completion failures.
     */
    void validate(JCTree tree) {
		DEBUG.P(this,"validate(JCTree tree)");
		if (tree != null) {
			//DEBUG.P("tree.type="+tree.type);
			DEBUG.P("tree.tag="+tree.myTreeTag());
		}else DEBUG.P("tree=null");
		
		try {
			if (tree != null) tree.accept(validator);
		} catch (CompletionFailure ex) {
			completionError(tree.pos(), ex);
		}
		DEBUG.P(1,this,"validate(JCTree tree)");
    }

    /** Visitor method: Validate a list of type expressions.
     */
    void validate(List<? extends JCTree> trees) {
		DEBUG.P(this,"validate(List<? extends JCTree> trees)");
		DEBUG.P("trees.size="+trees.size());
		DEBUG.P("trees="+trees);
		for (List<? extends JCTree> l = trees; l.nonEmpty(); l = l.tail)
			validate(l.head);
		DEBUG.P(1,this,"validate(List<? extends JCTree> trees)");
    }

    /** Visitor method: Validate a list of type parameters.
     */
    void validateTypeParams(List<JCTypeParameter> trees) {
		DEBUG.P(this,"validateTypeParams(1)");
		DEBUG.P("trees="+trees);
		
		for (List<JCTypeParameter> l = trees; l.nonEmpty(); l = l.tail)
			validate(l.head);
		DEBUG.P(1,this,"validateTypeParams(1)");
    }

    /** A visitor class for type validation.
     */
    class Validator extends JCTree.Visitor {

        public void visitTypeArray(JCArrayTypeTree tree) {
			DEBUG.P(this,"visitTypeArray(1)");
			DEBUG.P("tree="+tree);
			validate(tree.elemtype);
			DEBUG.P(0,this,"visitTypeArray(1)");
		}

        public void visitTypeApply(JCTypeApply tree) {
			DEBUG.P(this,"visitTypeApply(1)");
			DEBUG.P("tree="+tree);
			DEBUG.P("tree.type="+tree.type);
			DEBUG.P("tree.type.tag="+TypeTags.toString(tree.type.tag));
				
			if (tree.type.tag == CLASS) {
				List<Type> formals = tree.type.tsym.type.getTypeArguments();
				List<Type> actuals = tree.type.getTypeArguments();
				List<JCExpression> args = tree.arguments;
				List<Type> forms = formals;
				ListBuffer<TypeVar> tvars_buf = new ListBuffer<TypeVar>();
				
				DEBUG.P("formals="+formals);
				DEBUG.P("actuals="+actuals);
				DEBUG.P("args="+args);
				
				// For matching pairs of actual argument types `a' and
				// formal type parameters with declared bound `b' ...
				while (args.nonEmpty() && forms.nonEmpty()) {
					validate(args.head);

					// exact type arguments needs to know their
					// bounds (for upper and lower bound
					// calculations).  So we create new TypeVars with
					// bounds substed with actuals.
					tvars_buf.append(types.substBound(((TypeVar)forms.head),
									  formals,
									  Type.removeBounds(actuals)));

					args = args.tail;
					forms = forms.tail;
				}

				args = tree.arguments;
				List<TypeVar> tvars = tvars_buf.toList();

				DEBUG.P("");
				DEBUG.P("tvars="+tvars);
				DEBUG.P("args ="+args);
				while (args.nonEmpty() && tvars.nonEmpty()) {
					DEBUG.P("");
					DEBUG.P("args.head.type="+args.head.type);
					DEBUG.P("args.head.type.tag="+TypeTags.toString(args.head.type.tag));
					// Let the actual arguments know their bound
					args.head.type.withTypeVar(tvars.head);
					args = args.tail;
					tvars = tvars.tail;
				}

				args = tree.arguments;
				tvars = tvars_buf.toList();
				while (args.nonEmpty() && tvars.nonEmpty()) {
					checkExtends(args.head.pos(),
						 args.head.type,
						 tvars.head);
					args = args.tail;
					tvars = tvars.tail;
				}
				
				DEBUG.P("");
				DEBUG.P("tree.type="+tree.type);
				DEBUG.P("tree.type.getEnclosingType()="+tree.type.getEnclosingType());
				DEBUG.P("tree.type.getEnclosingType().isRaw()="+tree.type.getEnclosingType().isRaw());
				DEBUG.P("tree.clazz="+tree.clazz);
				DEBUG.P("tree.clazz.tag="+tree.clazz.myTreeTag());

                // Check that this type is either fully parameterized, or
                // not parameterized at all.
                /*错误例子:
				bin\mysrc\my\test\Test.java:47: 类型的格式不正确，给出了普通类型的类型参数
                Test.MyTestInnerClass<?> myTestInnerClass =
                                     ^
				bin\mysrc\my\test\Test.java:47: improperly formed type, type parameters given on a raw type
                Test.MyTestInnerClass<?> myTestInnerClass =
                                     ^
				*/
                if (tree.type.getEnclosingType().isRaw())
                    log.error(tree.pos(), "improperly.formed.type.inner.raw.param");
                if (tree.clazz.tag == JCTree.SELECT)
                    visitSelectInternal((JCFieldAccess)tree.clazz);
			}

			DEBUG.P(0,this,"visitTypeApply(1)");
		}

		public void visitTypeParameter(JCTypeParameter tree) {
			DEBUG.P(this,"visitTypeParameter(1)");
			DEBUG.P("tree="+tree);
			validate(tree.bounds);
			checkClassBounds(tree.pos(), tree.type);
			DEBUG.P(0,this,"visitTypeParameter(1)");
		}

		@Override
        public void visitWildcard(JCWildcard tree) {
			DEBUG.P(this,"visitWildcard(1)");
			DEBUG.P("tree="+tree);
			if (tree.inner != null)
			validate(tree.inner);
			DEBUG.P(0,this,"visitWildcard(1)");
		}

        public void visitSelect(JCFieldAccess tree) {
			DEBUG.P(this,"visitSelect(1)");
			DEBUG.P("tree="+tree);
			DEBUG.P("tree.type.tag="+TypeTags.toString(tree.type.tag));
			
			if (tree.type.tag == CLASS) {
					visitSelectInternal(tree);

					// Check that this type is either fully parameterized, or
					// not parameterized at all.
					DEBUG.P("tree.selected.type.isParameterized()="+tree.selected.type.isParameterized());
					DEBUG.P("tree.type.tsym.type.getTypeArguments().nonEmpty()="+tree.type.tsym.type.getTypeArguments().nonEmpty());
					if (tree.selected.type.isParameterized() && tree.type.tsym.type.getTypeArguments().nonEmpty())
						log.error(tree.pos(), "improperly.formed.type.param.missing");
			}
			
			DEBUG.P(0,this,"visitSelect(1)");
		}
        public void visitSelectInternal(JCFieldAccess tree) {
        	DEBUG.P(this,"visitSelectInternal(1)");
        	DEBUG.P("tree.type.getEnclosingType().tag="+TypeTags.toString(tree.type.getEnclosingType().tag));
        	DEBUG.P("tree.selected.type.isParameterized()="+tree.selected.type.isParameterized());
            DEBUG.P("tree.selected.type.allparams()="+tree.selected.type.allparams());
            if (tree.type.getEnclosingType().tag != CLASS &&
                tree.selected.type.isParameterized()) {
                /*错误例子:
                bin\mysrc\my\test\Test.java:7: 无法从参数化的类型中选择静态类
				public class Test<S,T extends ExtendsTest,E extends ExtendsTest & MyInterfaceA>
				extends my.ExtendsTest<String>.MyInnerClassStatic {
				
				                              ^
				1 错误
				
				打印结果:
				com.sun.tools.javac.comp.Check$Validator===>visitSelect(1)
				-------------------------------------------------------------------------
				tree=my.ExtendsTest<String>.MyInnerClassStatic
				tree.type.tag=CLASS
				com.sun.tools.javac.comp.Check$Validator===>visitSelectInternal(1)
				-------------------------------------------------------------------------
				tree.type.getEnclosingType().tag=NONE
				tree.selected.type.isParameterized()=true
				tree.selected.type.allparams()=java.lang.String
				com.sun.tools.javac.comp.Check$Validator===>visitSelectInternal(1)  END
				-------------------------------------------------------------------------
				com.sun.tools.javac.comp.Check$Validator===>visitSelect(1)  END
				-------------------------------------------------------------------------
                */
                
                // The enclosing type is not a class, so we are
                // looking at a static member type.  However, the
                // qualifying expression is parameterized.
                log.error(tree.pos(), "cant.select.static.class.from.param.type");
            } else {
                // otherwise validate the rest of the expression
                validate(tree.selected);
            }
            
            DEBUG.P(0,this,"visitSelectInternal(1)");
        }

		/** Default visitor method: do nothing.
		 */
		public void visitTree(JCTree tree) {
			DEBUG.P(this,"visitTree(1)");
			DEBUG.P("tree="+tree);
			DEBUG.P("do nothing");
			DEBUG.P(0,this,"visitTree(1)");
		}
    }

/* *************************************************************************
 * Exception checking
 **************************************************************************/

    /* The following methods treat classes as sets that contain
     * the class itself and all their subclasses
     */

    /** Is given type a subtype of some of the types in given list?
     */
    boolean subset(Type t, List<Type> ts) {
		for (List<Type> l = ts; l.nonEmpty(); l = l.tail)
			if (types.isSubtype(t, l.head)) return true;
		return false;
    }

    /** Is given type a subtype or supertype of
     *  some of the types in given list?
     */
    boolean intersects(Type t, List<Type> ts) {
		for (List<Type> l = ts; l.nonEmpty(); l = l.tail)
			if (types.isSubtype(t, l.head) || types.isSubtype(l.head, t)) return true;
		return false;
    }

    /** Add type set to given type list, unless it is a subclass of some class
     *  in the list.
     */
    List<Type> incl(Type t, List<Type> ts) {
		return subset(t, ts) ? ts : excl(t, ts).prepend(t);
    }

    /** Remove type set from type set list.
     */
    List<Type> excl(Type t, List<Type> ts) {
		if (ts.isEmpty()) {
			return ts;
		} else {
			List<Type> ts1 = excl(t, ts.tail);
			if (types.isSubtype(ts.head, t)) return ts1;
			else if (ts1 == ts.tail) return ts;
			else return ts1.prepend(ts.head);
		}
    }

    /** Form the union of two type set lists.
     */
    List<Type> union(List<Type> ts1, List<Type> ts2) {
		DEBUG.P(this,"union(2)");	
		List<Type> ts = ts1;
		for (List<Type> l = ts2; l.nonEmpty(); l = l.tail)
			ts = incl(l.head, ts);
		DEBUG.P(0,this,"union(2)");	    
		return ts;
    }

    /** Form the difference of two type lists.
     */
    List<Type> diff(List<Type> ts1, List<Type> ts2) {
		List<Type> ts = ts1;
		for (List<Type> l = ts2; l.nonEmpty(); l = l.tail)
			ts = excl(l.head, ts);
		return ts;
    }

    /** Form the intersection of two type lists.
     */
    public List<Type> intersect(List<Type> ts1, List<Type> ts2) {
		DEBUG.P(this,"intersect(2)");
		List<Type> ts = List.nil();
		for (List<Type> l = ts1; l.nonEmpty(); l = l.tail)
			if (subset(l.head, ts2)) ts = incl(l.head, ts);
		for (List<Type> l = ts2; l.nonEmpty(); l = l.tail)
			if (subset(l.head, ts1)) ts = incl(l.head, ts);
		DEBUG.P(0,this,"intersect(2)");
		return ts;
    }

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

    /** Is exc handled by given exception list?
     */
    boolean isHandled(Type exc, List<Type> handled) {
		return isUnchecked(exc) || subset(exc, handled);
    }

    /** Return all exceptions in thrown list that are not in handled list.
     *  @param thrown     The list of thrown exceptions.
     *  @param handled    The list of handled exceptions.
     */
    List<Type> unHandled(List<Type> thrown, List<Type> handled) {
		DEBUG.P(this,"unHandled(2)");
		DEBUG.P("thrown="+thrown);
		DEBUG.P("handled="+handled);

		List<Type> unhandled = List.nil();
		for (List<Type> l = thrown; l.nonEmpty(); l = l.tail)
			if (!isHandled(l.head, handled)) unhandled = unhandled.prepend(l.head);

		DEBUG.P("unhandled="+unhandled);
		DEBUG.P(0,this,"unHandled(2)");
		return unhandled;
    }

/* *************************************************************************
 * Overriding/Implementation checking
 **************************************************************************/

    /** The level of access protection given by a flag set,
     *  where PRIVATE is highest and PUBLIC is lowest.
     */
    static int protection(long flags) {
    	//当(flags & AccessFlags)是0时，表示的是“包访问级别”，
    	//“包访问级别”比“PROTECTED”的限制还大，所以数字也大
        switch ((short)(flags & AccessFlags)) {
        case PRIVATE: return 3;
        case PROTECTED: return 1;
        default:
        case PUBLIC: return 0;
        case 0: return 2;
        }
    }

    /** A string describing the access permission given by a flag set.
     *  This always returns a space-separated list of Java Keywords.
     */
    private static String protectionString(long flags) {
		long flags1 = flags & AccessFlags;
		return (flags1 == 0) ? "package" : TreeInfo.flagNames(flags1);
    }

    /** A customized "cannot override" error message.
     *  @param m      The overriding method.
     *  @param other  The overridden method.
     *  @return       An internationalized string.
     */
    static Object cannotOverride(MethodSymbol m, MethodSymbol other) {
		String key;
		if ((other.owner.flags() & INTERFACE) == 0) 
			key = "cant.override";
		else if ((m.owner.flags() & INTERFACE) == 0) 
			key = "cant.implement";
		else
			key = "clashes.with";
		return JCDiagnostic.fragment(key, m, m.location(), other, other.location());
    }

    /** A customized "override" warning message.
     *  @param m      The overriding method.
     *  @param other  The overridden method.
     *  @return       An internationalized string.
     */
    static Object uncheckedOverrides(MethodSymbol m, MethodSymbol other) {
	String key;
	if ((other.owner.flags() & INTERFACE) == 0) 
	    key = "unchecked.override";
	else if ((m.owner.flags() & INTERFACE) == 0) 
	    key = "unchecked.implement";
	else 
	    key = "unchecked.clash.with";
	return JCDiagnostic.fragment(key, m, m.location(), other, other.location());
    }

    /** A customized "override" warning message.
     *  @param m      The overriding method.
     *  @param other  The overridden method.
     *  @return       An internationalized string.
     */
    static Object varargsOverrides(MethodSymbol m, MethodSymbol other) {
	String key;
	if ((other.owner.flags() & INTERFACE) == 0) 
	    key = "varargs.override";
	else  if ((m.owner.flags() & INTERFACE) == 0) 
	    key = "varargs.implement";
	else
	    key = "varargs.clash.with";
	return JCDiagnostic.fragment(key, m, m.location(), other, other.location());
    }

    /** Check that this method conforms with overridden method 'other'.
     *  where `origin' is the class where checking started.
     *  Complications:
     *  (1) Do not check overriding of synthetic methods
     *      (reason: they might be final).
     *      todo: check whether this is still necessary.
     *  (2) Admit the case where an interface proxy throws fewer exceptions
     *      than the method it implements. Augment the proxy methods with the
     *      undeclared exceptions in this case.
     *  (3) When generics are enabled, admit the case where an interface proxy
     *	    has a result type
     *      extended by the result type of the method it implements.
     *      Change the proxies result type to the smaller type in this case.
     *
     *  @param tree         The tree from which positions
     *			    are extracted for errors.
     *  @param m            The overriding method.
     *  @param other        The overridden method.
     *  @param origin       The class of which the overriding method
     *			    is a member.
     */
    void checkOverride(JCTree tree,
		       MethodSymbol m,
		       MethodSymbol other,
		       ClassSymbol origin) {
		try {//我加上的
		DEBUG.P(this,"checkOverride(4)");
		DEBUG.P("m="+m+"  m.owner="+m.owner);
		DEBUG.P("m.flags()="+Flags.toString(m.flags()));
		DEBUG.P("other="+other+"  other.owner="+other.owner);
		DEBUG.P("other.flags()="+Flags.toString(other.flags()));
		DEBUG.P("origin="+origin);
		DEBUG.P("origin.flags()="+Flags.toString(origin.flags()));
		
		// Don't check overriding of synthetic methods or by bridge methods.
		if ((m.flags() & (SYNTHETIC|BRIDGE)) != 0 || (other.flags() & SYNTHETIC) != 0) {
			return;
		}

		// Error if static method overrides instance method (JLS 8.4.6.2).
		if ((m.flags() & STATIC) != 0 &&
			   (other.flags() & STATIC) == 0) {
			log.error(TreeInfo.diagnosticPositionFor(m, tree), "override.static",
				  cannotOverride(m, other));
			return;
		}

		// Error if instance method overrides static or final
		// method (JLS 8.4.6.1).
		if ((other.flags() & FINAL) != 0 ||
			 (m.flags() & STATIC) == 0 &&
			 (other.flags() & STATIC) != 0) {
			log.error(TreeInfo.diagnosticPositionFor(m, tree), "override.meth",
				  cannotOverride(m, other),
				  TreeInfo.flagNames(other.flags() & (FINAL | STATIC)));
			return;
		}

        if ((m.owner.flags() & ANNOTATION) != 0) {
            // handled in validateAnnotationMethod
            return;
        }

		// Error if overriding method has weaker access (JLS 8.4.6.3).
		if ((origin.flags() & INTERFACE) == 0 &&
			 protection(m.flags()) > protection(other.flags())) {
			log.error(TreeInfo.diagnosticPositionFor(m, tree), "override.weaker.access",
				  cannotOverride(m, other),
				  protectionString(other.flags()));
			return;

		}

		Type mt = types.memberType(origin.type, m);
		Type ot = types.memberType(origin.type, other);
		// Error if overriding result type is different
		// (or, in the case of generics mode, not a subtype) of
		// overridden result type. We have to rename any type parameters
		// before comparing types.
		List<Type> mtvars = mt.getTypeArguments();
		List<Type> otvars = ot.getTypeArguments();
		Type mtres = mt.getReturnType();

		DEBUG.P("mtvars="+mtvars);
		DEBUG.P("otvars="+otvars);
		DEBUG.P("mtres="+mtres);

		Type otres = types.subst(ot.getReturnType(), otvars, mtvars);

		overrideWarner.warned = false;
		boolean resultTypesOK =
			types.returnTypeSubstitutable(mt, ot, otres, overrideWarner);

		DEBUG.P("resultTypesOK="+resultTypesOK);
		DEBUG.P("overrideWarner.warned="+overrideWarner.warned);

		if (!resultTypesOK) {
			if (!source.allowCovariantReturns() &&
			m.owner != origin &&
			m.owner.isSubClass(other.owner, types)) {
			// allow limited interoperability with covariant returns
			} else {
				typeError(TreeInfo.diagnosticPositionFor(m, tree),
					  JCDiagnostic.fragment("override.incompatible.ret",
							 cannotOverride(m, other)),
					  mtres, otres);
				return;
			}
		} else if (overrideWarner.warned) {
			warnUnchecked(TreeInfo.diagnosticPositionFor(m, tree),
				  "prob.found.req",
				  JCDiagnostic.fragment("override.unchecked.ret",
							  uncheckedOverrides(m, other)),
				  mtres, otres);
		}
		
		// Error if overriding method throws an exception not reported
		// by overridden method.
		List<Type> otthrown = types.subst(ot.getThrownTypes(), otvars, mtvars);
		List<Type> unhandled = unHandled(mt.getThrownTypes(), otthrown);
		DEBUG.P("unhandled="+unhandled);
		if (unhandled.nonEmpty()) {
			log.error(TreeInfo.diagnosticPositionFor(m, tree),
				  "override.meth.doesnt.throw",
				  cannotOverride(m, other),
				  unhandled.head);
			return;
		}

		DEBUG.P("m.flags()="+Flags.toString(m.flags()));
		DEBUG.P("other.flags()="+Flags.toString(other.flags()));
		DEBUG.P("(m.flags() ^ other.flags())="+Flags.toString((m.flags() ^ other.flags())));
		DEBUG.P("lint.isEnabled(Lint.LintCategory.OVERRIDES)="+lint.isEnabled(Lint.LintCategory.OVERRIDES));
		// Optional warning if varargs don't agree 
		if ((((m.flags() ^ other.flags()) & Flags.VARARGS) != 0)
			&& lint.isEnabled(Lint.LintCategory.OVERRIDES)) {
			log.warning(TreeInfo.diagnosticPositionFor(m, tree),
				((m.flags() & Flags.VARARGS) != 0)
				? "override.varargs.missing"
				: "override.varargs.extra",
				varargsOverrides(m, other));
		} 

		// Warn if instance method overrides bridge method (compiler spec ??)
		if ((other.flags() & BRIDGE) != 0) {
			log.warning(TreeInfo.diagnosticPositionFor(m, tree), "override.bridge",
				uncheckedOverrides(m, other));
		}

		// Warn if a deprecated method overridden by a non-deprecated one.
		if ((other.flags() & DEPRECATED) != 0 
			&& (m.flags() & DEPRECATED) == 0 
			&& m.outermostClass() != other.outermostClass()
			&& !isDeprecatedOverrideIgnorable(other, origin)) {
			warnDeprecated(TreeInfo.diagnosticPositionFor(m, tree), other);
		}
		
		}finally{//我加上的
		DEBUG.P(0,this,"checkOverride(4)");
		}
    }
    // where
	private boolean isDeprecatedOverrideIgnorable(MethodSymbol m, ClassSymbol origin) {
	    try {//我加上的
			DEBUG.P(this,"isDeprecatedOverrideIgnorable(2)");
			DEBUG.P("m="+m);
			DEBUG.P("origin="+origin);


		// If the method, m, is defined in an interface, then ignore the issue if the method
	    // is only inherited via a supertype and also implemented in the supertype,
	    // because in that case, we will rediscover the issue when examining the method
	    // in the supertype.
	    // If the method, m, is not defined in an interface, then the only time we need to
	    // address the issue is when the method is the supertype implemementation: any other
	    // case, we will have dealt with when examining the supertype classes
	    ClassSymbol mc = m.enclClass();
	    Type st = types.supertype(origin.type);
		DEBUG.P("st="+st+"  st.tag="+TypeTags.toString(st.tag));
	    if (st.tag != CLASS)
		return true;
	    MethodSymbol stimpl = m.implementation((ClassSymbol)st.tsym, types, false);

	    if (mc != null && ((mc.flags() & INTERFACE) != 0)) {
		List<Type> intfs = types.interfaces(origin.type);
		return (intfs.contains(mc.type) ? false : (stimpl != null));
	    }
	    else
		return (stimpl != m);

		}finally{//我加上的
		DEBUG.P(0,this,"isDeprecatedOverrideIgnorable(2)");
		}
	}


    // used to check if there were any unchecked conversions
    Warner overrideWarner = new Warner();

    /** Check that a class does not inherit two concrete methods
     *  with the same signature.
     *  @param pos          Position to be used for error reporting.
     *  @param site         The class type to be checked.
     */
    public void checkCompatibleConcretes(DiagnosticPosition pos, Type site) {
    try {//我加上的
	DEBUG.P(this,"checkCompatibleConcretes(2)");
	DEBUG.P("site="+site);
	
	Type sup = types.supertype(site);
	DEBUG.P("sup="+sup);
	DEBUG.P("sup.tag="+TypeTags.toString(sup.tag));
	if (sup.tag != CLASS) return;

	for (Type t1 = sup;
	     t1.tsym.type.isParameterized();
	     t1 = types.supertype(t1)) {
	    for (Scope.Entry e1 = t1.tsym.members().elems;
		 e1 != null;
		 e1 = e1.sibling) {
		Symbol s1 = e1.sym;
		if (s1.kind != MTH ||
		    (s1.flags() & (STATIC|SYNTHETIC|BRIDGE)) != 0 ||
		    !s1.isInheritedIn(site.tsym, types) ||
		    ((MethodSymbol)s1).implementation(site.tsym,
						      types,
						      true) != s1)
		    continue;
		Type st1 = types.memberType(t1, s1);
		int s1ArgsLength = st1.getParameterTypes().length();
		if (st1 == s1.type) continue;

		for (Type t2 = sup;
		     t2.tag == CLASS;
		     t2 = types.supertype(t2)) {
		    for (Scope.Entry e2 = t1.tsym.members().lookup(s1.name);
			 e2.scope != null;
			 e2 = e2.next()) {
			Symbol s2 = e2.sym;
			if (s2 == s1 ||
			    s2.kind != MTH ||
			    (s2.flags() & (STATIC|SYNTHETIC|BRIDGE)) != 0 ||
			    s2.type.getParameterTypes().length() != s1ArgsLength ||
			    !s2.isInheritedIn(site.tsym, types) ||
			    ((MethodSymbol)s2).implementation(site.tsym,
							      types,
							      true) != s2)
			    continue;
			Type st2 = types.memberType(t2, s2);
			if (types.overrideEquivalent(st1, st2))
			    log.error(pos, "concrete.inheritance.conflict",
				      s1, t1, s2, t2, sup);
		    }
		}
	    }
	}
	
	}finally{//我加上的
	DEBUG.P(0,this,"checkCompatibleConcretes(2)");
	}
	
    }

    /** Check that classes (or interfaces) do not each define an abstract
     *  method with same name and arguments but incompatible return types.
     *  @param pos          Position to be used for error reporting.
     *  @param t1           The first argument type.
     *  @param t2           The second argument type.
     */
        public boolean checkCompatibleAbstracts(DiagnosticPosition pos,
					    Type t1,
					    Type t2) {
	try {//我加上的
	DEBUG.P(this,"checkCompatibleAbstracts(3)");
	DEBUG.P("t1="+t1);
	DEBUG.P("t2="+t2);

        return checkCompatibleAbstracts(pos, t1, t2,
                                        types.makeCompoundType(t1, t2));
    }finally{//我加上的
	DEBUG.P(0,this,"checkCompatibleAbstracts(3)");
	}
    }

	public boolean checkCompatibleAbstracts(DiagnosticPosition pos,
					    Type t1,
					    Type t2,
					    Type site) {
	boolean checkCompatibleAbstracts=false;//我加上的
	try {//我加上的
	DEBUG.P(this,"checkCompatibleAbstracts(4)");
	DEBUG.P("t1="+t1);
	DEBUG.P("t2="+t2);
	DEBUG.P("site="+site);
	
	Symbol sym = firstIncompatibility(t1, t2, site);
	DEBUG.P("");DEBUG.P("sym="+sym);
	if (sym != null) {
	    log.error(pos, "types.incompatible.diff.ret",
		      t1, t2, sym.name +
		      "(" + types.memberType(t2, sym).getParameterTypes() + ")");
	    return false;
	}
	checkCompatibleAbstracts=true;//我加上的
	return true;

	}finally{//我加上的
	DEBUG.P("checkCompatibleAbstracts="+checkCompatibleAbstracts);
	DEBUG.P(0,this,"checkCompatibleAbstracts(4)");
	}
    }

    /** Return the first method which is defined with same args
     *  but different return types in two given interfaces, or null if none
     *  exists.
     *  @param t1     The first type.
     *  @param t2     The second type.
     *  @param site   The most derived type.
     *  @returns symbol from t2 that conflicts with one in t1.
     */
    private Symbol firstIncompatibility(Type t1, Type t2, Type site) {
	try {//我加上的
	DEBUG.P(this,"firstIncompatibility(3)");
	DEBUG.P("t1="+t1);
	DEBUG.P("t2="+t2);
	DEBUG.P("site="+site);

	Map<TypeSymbol,Type> interfaces1 = new HashMap<TypeSymbol,Type>();
	closure(t1, interfaces1);
	Map<TypeSymbol,Type> interfaces2;
	if (t1 == t2)
	    interfaces2 = interfaces1;
	else
	    closure(t2, interfaces1, interfaces2 = new HashMap<TypeSymbol,Type>());
	    
	DEBUG.P("");
	DEBUG.P("site="+site);
	DEBUG.P("interfaces1="+interfaces1);
	DEBUG.P("interfaces2="+interfaces2);
	for (Type t3 : interfaces1.values()) {
	    for (Type t4 : interfaces2.values()) {
		Symbol s = firstDirectIncompatibility(t3, t4, site);
		if (s != null) return s;
	    }
	}
	return null;


    }finally{//我加上的
	DEBUG.P(0,this,"firstIncompatibility(3)");
	}
    }

    /** Compute all the supertypes of t, indexed by type symbol. */
    private void closure(Type t, Map<TypeSymbol,Type> typeMap) {
	try {//我加上的
	DEBUG.P(this,"closure(2)");
	DEBUG.P("t="+t+"  t.tag="+TypeTags.toString(t.tag));
	DEBUG.P("typeMap="+typeMap);

	if (t.tag != CLASS) return;
	if (typeMap.put(t.tsym, t) == null) {
	    closure(types.supertype(t), typeMap);
	    for (Type i : types.interfaces(t))
		closure(i, typeMap);
	}

    }finally{//我加上的
	DEBUG.P(0,this,"closure(2)");
	}
    }

    /** Compute all the supertypes of t, indexed by type symbol (except thise in typesSkip). */
    private void closure(Type t, Map<TypeSymbol,Type> typesSkip, Map<TypeSymbol,Type> typeMap) {
	try {//我加上的
	DEBUG.P(this,"closure(3)");
	DEBUG.P("t="+t+"  t.tag="+TypeTags.toString(t.tag));
	DEBUG.P("typesSkip="+typesSkip);
	DEBUG.P("typeMap="+typeMap);

	if (t.tag != CLASS) return;
	if (typesSkip.get(t.tsym) != null) return;
	if (typeMap.put(t.tsym, t) == null) {
	    closure(types.supertype(t), typesSkip, typeMap);
	    for (Type i : types.interfaces(t))
		closure(i, typesSkip, typeMap);
	}

    }finally{//我加上的
	DEBUG.P(0,this,"closure(3)");
	}
    }

    /** Return the first method in t2 that conflicts with a method from t1. */
    private Symbol firstDirectIncompatibility(Type t1, Type t2, Type site) {
	try {//我加上的
	DEBUG.P(this,"firstDirectIncompatibility(3)");
	DEBUG.P("t1="+t1);
	DEBUG.P("t2="+t2);
	DEBUG.P("site="+site);
	DEBUG.P("t1.tsym.members()="+t1.tsym.members());
	DEBUG.P("t2.tsym.members()="+t2.tsym.members());

	for (Scope.Entry e1 = t1.tsym.members().elems; e1 != null; e1 = e1.sibling) {
	    Symbol s1 = e1.sym;
	    Type st1 = null;
		DEBUG.P("s1.name="+s1.name);
		DEBUG.P("s1.kind="+Kinds.toString(s1.kind));
	    if (s1.kind != MTH || !s1.isInheritedIn(site.tsym, types)) continue;
            Symbol impl = ((MethodSymbol)s1).implementation(site.tsym, types, false);
            //if (impl != null && (impl.flags() & ABSTRACT) == 0) continue;
            if (impl != null && (impl.flags() & ABSTRACT) == 0) {
            	DEBUG.P("");
            	DEBUG.P("***********************");
            	DEBUG.P("site="+site);
            	DEBUG.P("与下面的type中的方法( "+s1.name+" )兼容");
            	DEBUG.P("t1="+t1);
            	DEBUG.P("所以不再与下面的type比较");
				DEBUG.P("t2="+t2);
				DEBUG.P("***********************");
				DEBUG.P("");
            	continue;
            }
		
		DEBUG.P("");DEBUG.P("for.......................");
	    for (Scope.Entry e2 = t2.tsym.members().lookup(s1.name); e2.scope != null; e2 = e2.next()) {
		Symbol s2 = e2.sym;
		DEBUG.P("s2.name="+s2.name);
		DEBUG.P("s2.kind="+Kinds.toString(s2.kind));

		if (s1 == s2) continue;
		if (s2.kind != MTH || !s2.isInheritedIn(site.tsym, types)) continue;
		if (st1 == null) st1 = types.memberType(t1, s1);
		Type st2 = types.memberType(t2, s2);
		if (types.overrideEquivalent(st1, st2)) {
		    List<Type> tvars1 = st1.getTypeArguments();
		    List<Type> tvars2 = st2.getTypeArguments();
		    Type rt1 = st1.getReturnType();
		    Type rt2 = types.subst(st2.getReturnType(), tvars2, tvars1);
		    boolean compat =
			types.isSameType(rt1, rt2) ||
                        rt1.tag >= CLASS && rt2.tag >= CLASS &&
                        (types.covariantReturnType(rt1, rt2, Warner.noWarnings) ||
                         types.covariantReturnType(rt2, rt1, Warner.noWarnings));
		    if (!compat) return s2;
		}
	    }
	}
	return null;
    
	}finally{//我加上的
	DEBUG.P(0,this,"firstDirectIncompatibility(3)");
	}
    }

    /** Check that a given method conforms with any method it overrides.
     *  @param tree         The tree from which positions are extracted
     *			    for errors.
     *  @param m            The overriding method.
     */
    void checkOverride(JCTree tree, MethodSymbol m) {
		try {//我加上的
		DEBUG.P(this,"checkOverride(2)");
		DEBUG.P("MethodSymbol m.name="+m.name);
		
		ClassSymbol origin = (ClassSymbol)m.owner;
		DEBUG.P("origin.name="+origin.name);
		DEBUG.P("origin.flags_field="+Flags.toString(origin.flags_field));
		
		if ((origin.flags() & ENUM) != 0 && names.finalize.equals(m.name))
			if (m.overrides(syms.enumFinalFinalize, origin, types, false)) {
				log.error(tree.pos(), "enum.no.finalize");
				return;
			}
		for (Type t = types.supertype(origin.type); t.tag == CLASS;
			 t = types.supertype(t)) {
			TypeSymbol c = t.tsym;
			Scope.Entry e = c.members().lookup(m.name);
			DEBUG.P("e.scope="+e.scope);
			while (e.scope != null) {
				if (m.overrides(e.sym, origin, types, false))
					checkOverride(tree, m, (MethodSymbol)e.sym, origin);
				e = e.next();
			}
		}
		
		}finally{//我加上的
		DEBUG.P(1,this,"checkOverride(2)");
		}
    }

    /** Check that all abstract members of given class have definitions.
     *  @param pos          Position to be used for error reporting.
     *  @param c            The class.
     */
    void checkAllDefined(DiagnosticPosition pos, ClassSymbol c) {
		DEBUG.P(this,"checkAllDefined(2)");	
		try {
			MethodSymbol undef = firstUndef(c, c);
			if (undef != null) {
					if ((c.flags() & ENUM) != 0 &&
						types.supertype(c.type).tsym == syms.enumSym &&
						(c.flags() & FINAL) == 0) {
						// add the ABSTRACT flag to an enum
						c.flags_field |= ABSTRACT;
					} else {
						MethodSymbol undef1 =
							new MethodSymbol(undef.flags(), undef.name,
											 types.memberType(c.type, undef), undef.owner);
						log.error(pos, "does.not.override.abstract",
								  c, undef1, undef1.location());
					}
				}
		} catch (CompletionFailure ex) {
			completionError(pos, ex);
		}
		
		DEBUG.P(2,this,"checkAllDefined(2)");	
    }
//where
        /** Return first abstract member of class `c' that is not defined
	 *  in `impl', null if there is none.
	 */
	//impl是一个实现类，c是一个假定被impl实现的类(抽象或非抽象类、接口)
	//只要在c中找到第一个没有被impl实现的抽象方法，就马上返回它，否则返回null
	private MethodSymbol firstUndef(ClassSymbol impl, ClassSymbol c) {
		DEBUG.P(this,"firstUndef(2)");	
	    MethodSymbol undef = null;
	    DEBUG.P("ClassSymbol impl="+impl);
	    DEBUG.P("ClassSymbol c   ="+c);
	    DEBUG.P("c.flags()="+Flags.toString(c.flags_field));
	    // Do not bother to search in classes that are not abstract,
	    // since they cannot have abstract members.
	    
	    //c == impl这个条件用于检查非abstract类中, 含有abstract方法的情况
	    if (c == impl || (c.flags() & (ABSTRACT | INTERFACE)) != 0) {
			Scope s = c.members();
			DEBUG.P("Scope s="+s);
			DEBUG.P("");
			DEBUG.P("for........................开始");
			for (Scope.Entry e = s.elems;
			     undef == null && e != null;
			     e = e.sibling) {
			    DEBUG.P("");
				DEBUG.P("e.sym.name="+e.sym.name); 	
			    DEBUG.P("e.sym.kind="+Kinds.toString(e.sym.kind));
			    DEBUG.P("e.sym.flags()="+Flags.toString(e.sym.flags_field));
	
			    if (e.sym.kind == MTH &&
				(e.sym.flags() & (ABSTRACT|IPROXY)) == ABSTRACT) {
				MethodSymbol absmeth = (MethodSymbol)e.sym;
				DEBUG.P("absmeth="+absmeth);
				
				MethodSymbol implmeth = absmeth.implementation(impl, types, true);
				
				DEBUG.P("implmeth="+implmeth);
				/*
				implmeth == absmeth这个条件用于检查非abstract类中,
				含有abstract方法的情况
				例子:
				------------------------------------------------------
				public class Test {
					public abstract void abstractMethod();
				}
				错误提示:
				bin\mysrc\my\test\Test.java:1: Test 不是抽象的，并且未覆盖 Test 中的抽象方法 abstractMethod()
				public class Test {
				       ^
				1 错误
				------------------------------------------------------

				或者是在实现类impl中没有定义absmeth这个方法，
				absmeth只在超类中定义，如:
				------------------------------------------------------
				abstract class ExtendsTestt{
					public abstract void extendsTestAbstractMethod();
				}
				public class Test extends ExtendsTest {}
				------------------------------------------------------
				这时:implmeth = absmeth = extendsTestAbstractMethod()
				*/
				if (implmeth == null || implmeth == absmeth)
				    undef = absmeth;
			    }
			}
			DEBUG.P("for........................结束");
			DEBUG.P("");

			DEBUG.P("undef="+undef);
			DEBUG.P("搜索超类........................开始");
			if (undef == null) {
			    Type st = types.supertype(c.type);
			    
			    DEBUG.P("st.tag="+TypeTags.toString(st.tag));
			    
			    if (st.tag == CLASS)
				undef = firstUndef(impl, (ClassSymbol)st.tsym);
			}
			DEBUG.P("搜索超类........................结束");

			DEBUG.P("");
			DEBUG.P("undef="+undef);
			DEBUG.P("搜索接口........................开始");
			//在for之前可以多加个if (undef == null)，这样当
			//undef!=null时就不用找c.type的interfaces。
			//源代码在这没加，可能是作者考虑到用户写程序时
			//抽象方法没有实现的情况比已实现的情况多，也就是undef!=null
			//这种情况很少出现，大多数情况下还是undef == null
			for (List<Type> l = types.interfaces(c.type);
			     undef == null && l.nonEmpty();
			     l = l.tail) {
			    undef = firstUndef(impl, (ClassSymbol)l.head.tsym);
			}
			DEBUG.P("搜索接口........................结束");
	    }
	    
		DEBUG.P("");
	    DEBUG.P("undef="+undef);
	    DEBUG.P(0,this,"firstUndef(2)");	
	    return undef;
	}

    /** Check for cyclic references. Issue an error if the
     *  symbol of the type referred to has a LOCKED flag set.
     *
     *  @param pos      Position to be used for error reporting.
     *  @param t        The type referred to.
     */
    void checkNonCyclic(DiagnosticPosition pos, Type t) {
    DEBUG.P(this,"checkNonCyclic(2)");	
	checkNonCyclicInternal(pos, t);
	DEBUG.P(1,this,"checkNonCyclic(2)");
    }
    //b10新增
    void checkNonCyclic(DiagnosticPosition pos, TypeVar t) {
    	DEBUG.P(this,"checkNonCyclic(DiagnosticPosition pos, TypeVar t)");	
    	
        checkNonCyclic1(pos, t, new HashSet<TypeVar>());
        
        DEBUG.P(1,this,"checkNonCyclic(DiagnosticPosition pos, TypeVar t)");
    }
    //b10新增
    private void checkNonCyclic1(DiagnosticPosition pos, Type t, Set<TypeVar> seen) {
        DEBUG.P(this,"checkNonCyclic1(3)");
        DEBUG.P("t="+t+"  seen="+seen);
        final TypeVar tv;
        if (seen.contains(t)) {
            tv = (TypeVar)t;
            tv.bound = new ErrorType();
            //循环继承 如:<V extends T,T extends V>
            log.error(pos, "cyclic.inheritance", t);
        } else if (t.tag == TYPEVAR) {
            tv = (TypeVar)t;
            seen.add(tv);
            for (Type b : types.getBounds(tv))
                checkNonCyclic1(pos, b, seen);
        }
        
        DEBUG.P(0,this,"checkNonCyclic1(3)");
    }
    /** Check for cyclic references. Issue an error if the
     *  symbol of the type referred to has a LOCKED flag set.
     *
     *  @param pos      Position to be used for error reporting.
     *  @param t        The type referred to.
     *  @returns        True if the check completed on all attributed classes
     */
    private boolean checkNonCyclicInternal(DiagnosticPosition pos, Type t) {
	boolean complete = true; // was the check complete?
	//- System.err.println("checkNonCyclicInternal("+t+");");//DEBUG
	Symbol c = t.tsym;
	
	try {//我加上的
	DEBUG.P(this,"checkNonCyclicInternal(2)");
    DEBUG.P("Symbol c="+c);
	DEBUG.P("c.flags_field="+Flags.toString(c.flags_field));
	DEBUG.P("c.type.tag="+TypeTags.toString(c.type.tag));
	DEBUG.P("c.type.isErroneous()="+c.type.isErroneous());
	DEBUG.P("c.completer="+c.completer);
	
	//flags_field是一个复合标志位,凡是出现下面的情况(先&再与0进行!=比较)
	//都是用来判断flags_field是否包含所要比较的标志位,包含则为true,否则为false
	//例:如果c.flags_field=public unattributed,那么if ((c.flags_field & ACYCLIC) != 0)=false
	if ((c.flags_field & ACYCLIC) != 0) {
		DEBUG.P(c+" 已确认不存在循环，所以不再检测，直接返回。");
		return true;
	}
	//当同一个Symbol的flags_field在前一次置过LOCKED时,第二次checkNonCyclicInternal时
	//又是同一个Symbol,说明肯定存在循环继承
	if ((c.flags_field & LOCKED) != 0) {
	    noteCyclic(pos, (ClassSymbol)c);
	} else if (!c.type.isErroneous()) {
	    try {
		c.flags_field |= LOCKED;//加锁
		if (c.type.tag == CLASS) {
		    ClassType clazz = (ClassType)c.type;
		    //检查所有实现的接口
		    DEBUG.P("检查 "+clazz+" 的所有接口: "+clazz.interfaces_field);
		    if (clazz.interfaces_field != null)
			for (List<Type> l=clazz.interfaces_field; l.nonEmpty(); l=l.tail)
			    complete &= checkNonCyclicInternal(pos, l.head);
			    
			//检查超类
			DEBUG.P("检查 "+clazz+" 的超类: "+clazz.supertype_field);
		    if (clazz.supertype_field != null) {
			Type st = clazz.supertype_field;
			if (st != null && st.tag == CLASS)
			    complete &= checkNonCyclicInternal(pos, st);
		    }
		    
		    //检查外部类(通常是在Symbol c为一个内部类时，c.owner.kind == TYP)
		    DEBUG.P("检查 "+clazz+" 的owner: "+c.owner.type);
		    DEBUG.P("c.owner.kind="+Kinds.toString(c.owner.kind));
		    if (c.owner.kind == TYP)
			complete &= checkNonCyclicInternal(pos, c.owner.type);
		}
	    } finally {
		c.flags_field &= ~LOCKED;//解锁
	    }
	}
	if (complete)
	//((c.flags_field & UNATTRIBUTED) == 0)当flags_field不包含UNATTRIBUTED时为true
	    complete = ((c.flags_field & UNATTRIBUTED) == 0) && c.completer == null;
	if (complete) c.flags_field |= ACYCLIC;

	return complete;
	
	
	}finally{//我加上的
	DEBUG.P("");
	DEBUG.P("complete="+complete);
	DEBUG.P(c+".flags_field="+Flags.toString(c.flags_field));
	DEBUG.P(0,this,"checkNonCyclicInternal(2)");
	}
    }

    /** Note that we found an inheritance cycle. */
    private void noteCyclic(DiagnosticPosition pos, ClassSymbol c) {
    DEBUG.P(this,"noteCyclic(2)");
    DEBUG.P("ClassSymbol c="+c);
    
	log.error(pos, "cyclic.inheritance", c);
	for (List<Type> l=types.interfaces(c.type); l.nonEmpty(); l=l.tail)
	    l.head = new ErrorType((ClassSymbol)l.head.tsym);
	Type st = types.supertype(c.type);
	if (st.tag == CLASS)
	    ((ClassType)c.type).supertype_field = new ErrorType((ClassSymbol)st.tsym);
	c.type = new ErrorType(c);
	c.flags_field |= ACYCLIC;
	
	DEBUG.P("c.flags_field="+Flags.toString(c.flags_field));
	DEBUG.P(0,this,"noteCyclic(2)");
    }

    /** Check that all methods which implement some
     *  method conform to the method they implement.
     *  @param tree         The class definition whose members are checked.
     */
    void checkImplementations(JCClassDecl tree) {
		DEBUG.P(this,"checkImplementations(1)");
		checkImplementations(tree, tree.sym);
		DEBUG.P(1,this,"checkImplementations(1)");
    }
//where
        /** Check that all methods which implement some
	 *  method in `ic' conform to the method they implement.
	 */
	void checkImplementations(JCClassDecl tree, ClassSymbol ic) {
		DEBUG.P(this,"checkImplementations(2)");
		DEBUG.P("ClassSymbol ic="+ic);
		DEBUG.P("tree.sym="+tree.sym);
	    ClassSymbol origin = tree.sym;
	    for (List<Type> l = types.closure(ic.type); l.nonEmpty(); l = l.tail) {
		ClassSymbol lc = (ClassSymbol)l.head.tsym;
		DEBUG.P("origin="+origin);
		DEBUG.P("lc="+lc);
		if ((allowGenerics || origin != lc) && (lc.flags() & ABSTRACT) != 0) {
		    for (Scope.Entry e=lc.members().elems; e != null; e=e.sibling) {
		    DEBUG.P("e.sym.name="+e.sym.name);	
		    DEBUG.P("e.sym.kind="+com.sun.tools.javac.code.Kinds.toString(e.sym.kind));
			if (e.sym.kind == MTH &&
			    (e.sym.flags() & (STATIC|ABSTRACT)) == ABSTRACT) {
			    MethodSymbol absmeth = (MethodSymbol)e.sym;
			    MethodSymbol implmeth = absmeth.implementation(origin, types, false);
			    DEBUG.P("implmeth="+implmeth);
			    DEBUG.P("absmeth="+absmeth);
			    DEBUG.P("(implmeth != absmeth)="+(implmeth != absmeth));
			    if (implmeth != null && implmeth != absmeth &&
				(implmeth.owner.flags() & INTERFACE) ==
				(origin.flags() & INTERFACE)) {
				// don't check if implmeth is in a class, yet
				// origin is an interface. This case arises only
				// if implmeth is declared in Object. The reason is
				// that interfaces really don't inherit from
				// Object it's just that the compiler represents
				// things that way.
				checkOverride(tree, implmeth, absmeth, origin);
			    }
			}
		    }
		}
	    }
	    DEBUG.P(0,this,"checkImplementations(2)");
	}

    /** Check that all abstract methods implemented by a class are
     *  mutually compatible.
     *  @param pos          Position to be used for error reporting.
     *  @param c            The class whose interfaces are checked.
     */
    void checkCompatibleSupertypes(DiagnosticPosition pos, Type c) {
		try {//我加上的
		DEBUG.P(this,"checkCompatibleSupertypes(2)");
		DEBUG.P("c="+c);
		
		
		List<Type> supertypes = types.interfaces(c);
		Type supertype = types.supertype(c);
		if (supertype.tag == CLASS &&
			(supertype.tsym.flags() & ABSTRACT) != 0)
			supertypes = supertypes.prepend(supertype);
		DEBUG.P("supertypes="+supertypes);
		DEBUG.P("");
		for (List<Type> l = supertypes; l.nonEmpty(); l = l.tail) {
			DEBUG.P("allowGenerics="+allowGenerics);
			DEBUG.P("l.head.getTypeArguments()="+l.head.getTypeArguments());
			
			if (allowGenerics && !l.head.getTypeArguments().isEmpty() &&
			!checkCompatibleAbstracts(pos, l.head, l.head, c))
			return;
			for (List<Type> m = supertypes; m != l; m = m.tail)
			if (!checkCompatibleAbstracts(pos, l.head, m.head, c))
				return;
		}
		checkCompatibleConcretes(pos, c);
		
		}finally{//我加上的
		DEBUG.P(2,this,"checkCompatibleSupertypes(2)");
		}
    }

    /** Check that class c does not implement directly or indirectly
     *  the same parameterized interface with two different argument lists.
     *  @param pos          Position to be used for error reporting.
     *  @param type         The type whose interfaces are checked.
     */
    void checkClassBounds(DiagnosticPosition pos, Type type) {
		DEBUG.P(this,"checkClassBounds(2)");
		DEBUG.P("type="+type);
		checkClassBounds(pos, new HashMap<TypeSymbol,Type>(), type);
		DEBUG.P(0,this,"checkClassBounds(2)");
    }
//where
        /** Enter all interfaces of type `type' into the hash table `seensofar'
	 *  with their class symbol as key and their type as value. Make
	 *  sure no class is entered with two different types.
	 */
	void checkClassBounds(DiagnosticPosition pos,
			      Map<TypeSymbol,Type> seensofar,
			      Type type) {
		try {//我加上的
		DEBUG.P(this,"checkClassBounds(3)");
		DEBUG.P("seensofar="+seensofar);
		DEBUG.P("type="+type);

	    if (type.isErroneous()) return;
	    for (List<Type> l = types.interfaces(type); l.nonEmpty(); l = l.tail) {
			Type it = l.head;
			Type oldit = seensofar.put(it.tsym, it);
			
			DEBUG.P("Type it="+it);
			DEBUG.P("Type oldit="+oldit);
			
			if (oldit != null) {
				/*错误例子:
				bin\mysrc\my\test\Test.java:7: 接口重复
				public class Test<S,T extends ExtendsTest,E extends ExtendsTest & MyInterfaceA>
				extends my.ExtendsTest.MyInnerClassStatic implements InterfaceTest<ExtendsTest,M
				yInterfaceA>, InterfaceTest<ExtendsTest,Test> {
				
				
										   ^
				bin\mysrc\my\test\Test.java:7: 无法使用以下不同的参数继承 my.InterfaceTest：<my.
				ExtendsTest,my.test.MyInterfaceA> 和 <my.ExtendsTest,my.test.Test>
				public class Test<S,T extends ExtendsTest,E extends ExtendsTest & MyInterfaceA>
				extends my.ExtendsTest.MyInnerClassStatic implements InterfaceTest<ExtendsTest,M
				yInterfaceA>, InterfaceTest<ExtendsTest,Test> {
					   ^
				2 错误
				
				打印输出:
				Type it=my.InterfaceTest<my.ExtendsTest,my.test.Test>
				Type oldit=my.InterfaceTest<my.ExtendsTest,my.test.MyInterfaceA>
				oldparams=my.ExtendsTest,my.test.MyInterfaceA
				newparams=my.ExtendsTest,my.test.Test
				*/
				List<Type> oldparams = oldit.allparams();
				List<Type> newparams = it.allparams();
				DEBUG.P("oldparams="+oldparams);
				DEBUG.P("newparams="+newparams);
				if (!types.containsTypeEquivalent(oldparams, newparams))
				log.error(pos, "cant.inherit.diff.arg",
					  it.tsym, Type.toString(oldparams),
					  Type.toString(newparams));
			}
			checkClassBounds(pos, seensofar, it);
	    }
	    Type st = types.supertype(type);
	    DEBUG.P("st="+st);
	    if (st != null) checkClassBounds(pos, seensofar, st);
	    
		}finally{//我加上的
		DEBUG.P(0,this,"checkClassBounds(3)");
		}	
	}

    /** Enter interface into into set.
     *  If it existed already, issue a "repeated interface" error.
     */
    void checkNotRepeated(DiagnosticPosition pos, Type it, Set<Type> its) {
		DEBUG.P(this,"checkNotRepeated(3)");
		DEBUG.P("it="+it);
		DEBUG.P("its="+its);
		/*
		bin\mysrc\my\test\Test.java:8: 接口重复
		public class Test<S extends TestBound & MyInterfaceA, T> extends TestOhter<Integ
		er,String> implements MyInterfaceA,MyInterfaceA,MyInterfaceB {
		
										   ^
		1 错误
		*/
		if (its.contains(it))
			log.error(pos, "repeated.interface");
		else {
			its.add(it);
		}
		DEBUG.P(0,this,"checkNotRepeated(3)");
    }
	
/* *************************************************************************
 * Check annotations
 **************************************************************************/

    /** Annotation types are restricted to primitives, String, an
     *  enum, an annotation, Class, Class<?>, Class<? extends
     *  Anything>, arrays of the preceding.
     */
    void validateAnnotationType(JCTree restype) {
        // restype may be null if an error occurred, so don't bother validating it
        if (restype != null) {
            validateAnnotationType(restype.pos(), restype.type);
        }
    }

    void validateAnnotationType(DiagnosticPosition pos, Type type) {
		if (type.isPrimitive()) return;
		if (types.isSameType(type, syms.stringType)) return;
		if ((type.tsym.flags() & Flags.ENUM) != 0) return;
		if ((type.tsym.flags() & Flags.ANNOTATION) != 0) return;
		if (types.lowerBound(type).tsym == syms.classType.tsym) return;

		//不可以是二维(多维)数组
		if (types.isArray(type) && !types.isArray(types.elemtype(type))) {
			validateAnnotationType(pos, types.elemtype(type));
			return;
		}
		log.error(pos, "invalid.annotation.member.type");
    }

    /**
     * "It is also a compile-time error if any method declared in an
     * annotation type has a signature that is override-equivalent to
     * that of any public or protected method declared in class Object
     * or in the interface annotation.Annotation."
     *
     * @jls3 9.6 Annotation Types
     */
    void validateAnnotationMethod(DiagnosticPosition pos, MethodSymbol m) {
		DEBUG.P(this,"validateAnnotationMethod(2)");
		DEBUG.P("m="+m);
		/* 如:
			@interface IA {
				boolean equals();
				int hashCode();
				String toString();
			}
		*/
        for (Type sup = syms.annotationType; sup.tag == CLASS; sup = types.supertype(sup)) {
            Scope s = sup.tsym.members();
			DEBUG.P("s="+s);
            for (Scope.Entry e = s.lookup(m.name); e.scope != null; e = e.next()) {
                if (e.sym.kind == MTH &&
                    (e.sym.flags() & (PUBLIC | PROTECTED)) != 0 &&
                    types.overrideEquivalent(m.type, e.sym.type))
                    log.error(pos, "intf.annotation.member.clash", e.sym, sup);
            }
        }

		DEBUG.P(0,this,"validateAnnotationMethod(2)");
    }

    /** Check the annotations of a symbol.
     */
    public void validateAnnotations(List<JCAnnotation> annotations, Symbol s) {
		try {//我加上的
		DEBUG.P(this,"validateAnnotations(2)");
		//DEBUG.P("暂时跳过注释，不检测");
		
		DEBUG.P("annotations="+annotations);
		DEBUG.P("s="+s);
		DEBUG.P("skipAnnotations="+skipAnnotations);
		
		
		if (skipAnnotations) return;
		for (JCAnnotation a : annotations)
			validateAnnotation(a, s);
		   
		}finally{//我加上的
		DEBUG.P(2,this,"validateAnnotations(2)");
		}
    }

    /** Check an annotation of a symbol.
     */
    public void validateAnnotation(JCAnnotation a, Symbol s) {
		DEBUG.P(this,"validateAnnotation(2)");
		DEBUG.P("a="+a);
		DEBUG.P("s="+s);
		
		validateAnnotation(a);
		/*
		if (!annotationApplicable(a, s))
			log.error(a.pos(), "annotation.type.not.applicable");
		if (a.annotationType.type.tsym == syms.overrideType.tsym) {
			if (!isOverrider(s))
			log.error(a.pos(), "method.does.not.override.superclass");
		}
		*/
		
		//下面两个log.error()的位置都是a.pos()，所以当两个同时出现时，只报告一个错误
		boolean annotationApplicableFlag=annotationApplicable(a, s);
		DEBUG.P("annotationApplicableFlag="+annotationApplicableFlag);
		if (!annotationApplicableFlag)
			log.error(a.pos(), "annotation.type.not.applicable");

		DEBUG.P("a.annotationType.type.tsym="+a.annotationType.type.tsym);
		DEBUG.P("syms.overrideType.tsym="+syms.overrideType.tsym);
		if (a.annotationType.type.tsym == syms.overrideType.tsym) {
			boolean isOverriderFlag=isOverrider(s);
			DEBUG.P("isOverriderFlag="+isOverriderFlag);
			if (!isOverriderFlag)
				log.error(a.pos(), "method.does.not.override.superclass");
		}
		
		DEBUG.P(1,this,"validateAnnotation(2)");
    }

    /** Is s a method symbol that overrides a method in a superclass? */
    boolean isOverrider(Symbol s) {
		try {//我加上的
		DEBUG.P(this,"isOverrider(Symbol s)");
		DEBUG.P("s="+s+"  s.kind="+Kinds.toString(s.kind)+" s.isStatic()="+s.isStatic());
		
        if (s.kind != MTH || s.isStatic()) //静态方法永远不会覆盖超类中的静态方法
            return false;
        MethodSymbol m = (MethodSymbol)s;
        TypeSymbol owner = (TypeSymbol)m.owner;
        
        DEBUG.P("m="+m);
        DEBUG.P("owner="+owner);
        
        for (Type sup : types.closure(owner.type)) {
            if (sup == owner.type)
                continue; // skip "this"
            Scope scope = sup.tsym.members();
            DEBUG.P("scope="+scope);
            for (Scope.Entry e = scope.lookup(m.name); e.scope != null; e = e.next()) {
                if (!e.sym.isStatic() && m.overrides(e.sym, owner, types, true))
                    return true;
            }
        }
        return false;
        
		}finally{//我加上的
		DEBUG.P(1,this,"isOverrider(Symbol s)");
		}  
    }

    /** Is the annotation applicable to the symbol? */
    boolean annotationApplicable(JCAnnotation a, Symbol s) {
		try {//我加上的
		DEBUG.P(this,"annotationApplicable(2)");
		DEBUG.P("a="+a);
		DEBUG.P("s="+s+"  s.kind="+Kinds.toString(s.kind)+" s.isStatic()="+s.isStatic());
		
		Attribute.Compound atTarget =
			a.annotationType.type.tsym.attribute(syms.annotationTargetType.tsym);
		
		DEBUG.P("atTarget="+atTarget);
		if (atTarget == null) return true;
		Attribute atValue = atTarget.member(names.value);
		DEBUG.P("atValue="+atValue);
		DEBUG.P("(!(atValue instanceof Attribute.Array))="+(!(atValue instanceof Attribute.Array)));
		if (!(atValue instanceof Attribute.Array)) return true; // error recovery
		Attribute.Array arr = (Attribute.Array) atValue;
		for (Attribute app : arr.values) {
			DEBUG.P("(!(app instanceof Attribute.Enum))="+(!(app instanceof Attribute.Enum)));
			if (!(app instanceof Attribute.Enum)) return true; // recovery
			Attribute.Enum e = (Attribute.Enum) app;
			
			DEBUG.P("s.kind="+Kinds.toString(s.kind));
			DEBUG.P("s.owner.kind="+Kinds.toString(s.owner.kind));
			DEBUG.P("s.flags()="+Flags.toString(s.flags()));
			DEBUG.P("e.value.name="+e.value.name);
			if (e.value.name == names.TYPE)
			{ if (s.kind == TYP) return true; }
			else if (e.value.name == names.FIELD)
			{ if (s.kind == VAR && s.owner.kind != MTH) return true; }
			else if (e.value.name == names.METHOD)
			{ if (s.kind == MTH && !s.isConstructor()) return true; }
			else if (e.value.name == names.PARAMETER)
			{	
				if (s.kind == VAR &&
				  s.owner.kind == MTH &&
				  (s.flags() & PARAMETER) != 0)
				return true;
			}
			else if (e.value.name == names.CONSTRUCTOR)
			{ if (s.kind == MTH && s.isConstructor()) return true; }
			else if (e.value.name == names.LOCAL_VARIABLE)
			{ if (s.kind == VAR && s.owner.kind == MTH &&
				  (s.flags() & PARAMETER) == 0)
				return true;
			}
			else if (e.value.name == names.ANNOTATION_TYPE)
			{ if (s.kind == TYP && (s.flags() & ANNOTATION) != 0)
				return true;
			}
			else if (e.value.name == names.PACKAGE)
			{ if (s.kind == PCK) return true; }
			else
			//在Annotate解析Target时发生了错误，导致e.value.name不是以上各项
			return true; // recovery
		}
		return false;
		
		}finally{//我加上的
		DEBUG.P(0,this,"annotationApplicable(2)");
		}
    }

    /** Check an annotation value.
     */
    public void validateAnnotation(JCAnnotation a) {
		try {//我加上的
		DEBUG.P(this,"validateAnnotation(1)");
		DEBUG.P("a="+a);
		DEBUG.P("a.type="+a.type);
		DEBUG.P("a.type.isErroneous()="+a.type.isErroneous());

        if (a.type.isErroneous()) return;

		DEBUG.P("");
		DEBUG.P("a.annotationType.type.tsym="+a.annotationType.type.tsym);
		DEBUG.P("a.annotationType.type.tsym.members()="+a.annotationType.type.tsym.members());
		// collect an inventory of the members
		Set<MethodSymbol> members = new HashSet<MethodSymbol>();
		for (Scope.Entry e = a.annotationType.type.tsym.members().elems;
			 e != null;
			 e = e.sibling)
			if (e.sym.kind == MTH)
					members.add((MethodSymbol) e.sym);
		DEBUG.P("members="+members);

		DEBUG.P("");
		DEBUG.P("a.args="+a.args);
		DEBUG.P("for...............开始");
		// count them off as they're annotated
		for (JCTree arg : a.args) {
			DEBUG.P("arg.tag="+arg.myTreeTag());

			if (arg.tag != JCTree.ASSIGN) continue; // recovery
			JCAssign assign = (JCAssign) arg;
			Symbol m = TreeInfo.symbol(assign.lhs);

			DEBUG.P("m="+m);

			if (m == null || m.type.isErroneous()) continue;
			/*
			检查注释成员值是否有重复，有重复，
			则编译器会报一个关键字为“duplicate.annotation.member.value”的错误。
			
			如下源代码:
			--------------------------------------------------------------------
			package my.error;
			@interface MyAnnotation {
				String value();
			}
			@MyAnnotation(value="testA",value="testB")
			public class duplicate_annotation_member_value  {}
			--------------------------------------------------------------------
			
			编译错误提示信息如下:
			--------------------------------------------------------------------
			bin\mysrc\my\error\duplicate_annotation_member_value.java:5: my.error.MyAnnotation 中的注释成员值 value 重复
			@MyAnnotation(value="testA",value="testB")
											  ^
			1 错误
			--------------------------------------------------------------------
			
			因为members=[value()]，a.args却有两个value，
			所以第二次members.remove(m)时将返回false
			(也就是value()在第一次for循环时已删除，在第二次for循环时已不存在)
			*/
			if (!members.remove(m))
			log.error(arg.pos(), "duplicate.annotation.member.value",
				  m.name, a.type);

			DEBUG.P("assign.rhs.tag="+assign.rhs.myTreeTag());

			if (assign.rhs.tag == ANNOTATION)
			validateAnnotation((JCAnnotation)assign.rhs);
		}
		DEBUG.P("for...............结束");

		DEBUG.P("");
		DEBUG.P("members="+members);

		// all the remaining ones better have default values
		for (MethodSymbol m : members)
			if (m.defaultValue == null && !m.type.isErroneous())
			log.error(a.pos(), "annotation.missing.default.value", 
							  a.type, m.name);

		DEBUG.P("a.annotationType.type.tsym="+a.annotationType.type.tsym);
		DEBUG.P("syms.annotationTargetType.tsym="+syms.annotationTargetType.tsym);
		DEBUG.P("a.args.tail="+a.args.tail);
		// special case: java.lang.annotation.Target must not have
		// repeated values in its value member
		if (a.annotationType.type.tsym != syms.annotationTargetType.tsym ||
			a.args.tail == null) //a.args.tail == null是@Target不加参数的情况
			return;
			
		DEBUG.P("a.args.head.tag="+a.args.head.myTreeTag());
		
			if (a.args.head.tag != JCTree.ASSIGN) return; // error recovery
		JCAssign assign = (JCAssign) a.args.head;
		Symbol m = TreeInfo.symbol(assign.lhs);
		
		DEBUG.P("m.name="+m.name);
		
		if (m.name != names.value) return;
		JCTree rhs = assign.rhs;
		
		DEBUG.P("rhs.tag="+rhs.myTreeTag());
		
		if (rhs.tag != JCTree.NEWARRAY) return;
		JCNewArray na = (JCNewArray) rhs;
		Set<Symbol> targets = new HashSet<Symbol>();
		for (JCTree elem : na.elems) {
			if (!targets.add(TreeInfo.symbol(elem))) {
			log.error(elem.pos(), "repeated.annotation.target");
			}
		}
		
		}finally{//我加上的
		DEBUG.P(1,this,"validateAnnotation(1)");
		}
    }

    void checkDeprecatedAnnotation(DiagnosticPosition pos, Symbol s) {
		/*
		当在javac命令行中启用“-Xlint:dep-ann”选项时，
		如果javadoc文档中有@deprecated，
		但是没有加“@Deprecated ”这个注释标记时，编译器就会发出警告

		注意是:“-Xlint:dep-ann”选项，而不是-Xlint:deprecation
		*/
		DEBUG.P(this,"checkDeprecatedAnnotation(2)");
		if (allowAnnotations &&
			lint.isEnabled(Lint.LintCategory.DEP_ANN) &&
			(s.flags() & DEPRECATED) != 0 &&
			!syms.deprecatedType.isErroneous() &&
			s.attribute(syms.deprecatedType.tsym) == null) {
			log.warning(pos, "missing.deprecated.annotation");
		}
		DEBUG.P(0,this,"checkDeprecatedAnnotation(2)");
    }

/* *************************************************************************
 * Check for recursive annotation elements.
 **************************************************************************/

    /** Check for cycles in the graph of annotation elements.
     */
    void checkNonCyclicElements(JCClassDecl tree) {
    	try {//我加上的
		DEBUG.P(this,"checkNonCyclicElements(JCClassDecl tree)");
		DEBUG.P("tree.sym.flags_field="+Flags.toString(tree.sym.flags_field));
		
        if ((tree.sym.flags_field & ANNOTATION) == 0) return;
        assert (tree.sym.flags_field & LOCKED) == 0;
        try {
            tree.sym.flags_field |= LOCKED;
            for (JCTree def : tree.defs) {
                if (def.tag != JCTree.METHODDEF) continue;
                JCMethodDecl meth = (JCMethodDecl)def;
                checkAnnotationResType(meth.pos(), meth.restype.type);
            }
        } finally {
            tree.sym.flags_field &= ~LOCKED;
            tree.sym.flags_field |= ACYCLIC_ANN;
        }
        
        }finally{//我加上的
		DEBUG.P(1,this,"checkNonCyclicElements(JCClassDecl tree)");
		}
    }

    void checkNonCyclicElementsInternal(DiagnosticPosition pos, TypeSymbol tsym) {
        if ((tsym.flags_field & ACYCLIC_ANN) != 0)
            return;
        if ((tsym.flags_field & LOCKED) != 0) {
            log.error(pos, "cyclic.annotation.element");
            return;
        }
        try {
            tsym.flags_field |= LOCKED;
            for (Scope.Entry e = tsym.members().elems; e != null; e = e.sibling) {
                Symbol s = e.sym;
                if (s.kind != Kinds.MTH)
                    continue;
                checkAnnotationResType(pos, ((MethodSymbol)s).type.getReturnType());
            }
        } finally {
            tsym.flags_field &= ~LOCKED;
            tsym.flags_field |= ACYCLIC_ANN;
        }
    }

    void checkAnnotationResType(DiagnosticPosition pos, Type type) {
        switch (type.tag) {
        case TypeTags.CLASS:
            if ((type.tsym.flags() & ANNOTATION) != 0)
                checkNonCyclicElementsInternal(pos, type.tsym);
            break;
        case TypeTags.ARRAY:
            checkAnnotationResType(pos, types.elemtype(type));
            break;
        default:
            break; // int etc
        }
    }

/* *************************************************************************
 * Check for cycles in the constructor call graph.
 **************************************************************************/

    /** Check for cycles in the graph of constructors calling other
     *  constructors.
     */
    void checkCyclicConstructors(JCClassDecl tree) {
		DEBUG.P(this,"checkCyclicConstructors(JCClassDecl tree)");
		Map<Symbol,Symbol> callMap = new HashMap<Symbol, Symbol>();

		// enter each constructor this-call into the map
		for (List<JCTree> l = tree.defs; l.nonEmpty(); l = l.tail) {
			JCMethodInvocation app = TreeInfo.firstConstructorCall(l.head);
			if (app == null) continue;
			JCMethodDecl meth = (JCMethodDecl) l.head;
			if (TreeInfo.name(app.meth) == names._this) {
				callMap.put(meth.sym, TreeInfo.symbol(app.meth));
			} else {
				meth.sym.flags_field |= ACYCLIC;
			}
		}

		// Check for cycles in the map
		Symbol[] ctors = new Symbol[0];
		ctors = callMap.keySet().toArray(ctors);
		for (Symbol caller : ctors) {
			checkCyclicConstructor(tree, caller, callMap);
		}
		DEBUG.P(1,this,"checkCyclicConstructors(JCClassDecl tree)");
    }

    /** Look in the map to see if the given constructor is part of a
     *  call cycle.
     */
    private void checkCyclicConstructor(JCClassDecl tree, Symbol ctor,
					Map<Symbol,Symbol> callMap) {
		if (ctor != null && (ctor.flags_field & ACYCLIC) == 0) {
			if ((ctor.flags_field & LOCKED) != 0) {
				log.error(TreeInfo.diagnosticPositionFor(ctor, tree),
				  "recursive.ctor.invocation");
			} else {
				ctor.flags_field |= LOCKED;
				checkCyclicConstructor(tree, callMap.remove(ctor), callMap);
				ctor.flags_field &= ~LOCKED;
			}
			ctor.flags_field |= ACYCLIC;
		}
    }

/* *************************************************************************
 * Miscellaneous
 **************************************************************************/

    /**
     * Return the opcode of the operator but emit an error if it is an
     * error.
     * @param pos        position for error reporting.
     * @param operator   an operator
     * @param tag        a tree tag
     * @param left       type of left hand side
     * @param right      type of right hand side
     */
    int checkOperator(DiagnosticPosition pos,
                       OperatorSymbol operator,
                       int tag,
                       Type left,
                       Type right) {
        if (operator.opcode == ByteCodes.error) {
            log.error(pos,
                      "operator.cant.be.applied",
                      treeinfo.operatorName(tag),
                      left + "," + right);
        }
        return operator.opcode;
    }


    /**
     *  Check for division by integer constant zero
     *	@param pos	     Position for error reporting.
     *	@param operator      The operator for the expression
     *	@param operand       The right hand operand for the expression
     */
    void checkDivZero(DiagnosticPosition pos, Symbol operator, Type operand) {
		if (operand.constValue() != null
			&& lint.isEnabled(Lint.LintCategory.DIVZERO)
			&& operand.tag <= LONG
			&& ((Number) (operand.constValue())).longValue() == 0) {
				int opc = ((OperatorSymbol)operator).opcode;
				if (opc == ByteCodes.idiv || opc == ByteCodes.imod 
				|| opc == ByteCodes.ldiv || opc == ByteCodes.lmod) {
					log.warning(pos, "div.zero");
			}
		}
    }

    /**
     * Check for empty statements after if
     */
    void checkEmptyIf(JCIf tree) {
		if (tree.thenpart.tag == JCTree.SKIP && tree.elsepart == null && lint.isEnabled(Lint.LintCategory.EMPTY))
			log.warning(tree.thenpart.pos(), "empty.if");
    }

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

    /** Check that a qualified name is in canonical form (for import decls).
     */
    public void checkCanonical(JCTree tree) {
		DEBUG.P(this,"checkCanonical(1)");
		DEBUG.P("tree="+tree);
		if (!isCanonical(tree))
			log.error(tree.pos(), "import.requires.canonical",
				  TreeInfo.symbol(tree));
		DEBUG.P(0,this,"checkCanonical(1)");
    }
        // where
	private boolean isCanonical(JCTree tree) {
	    while (tree.tag == JCTree.SELECT) {
			JCFieldAccess s = (JCFieldAccess) tree;
			DEBUG.P("s.selected="+s.selected);
			DEBUG.P("s.sym.owner="+s.sym.owner);
			DEBUG.P("TreeInfo.symbol(tree)="+TreeInfo.symbol(tree));
			DEBUG.P("TreeInfo.symbol(s.selected)="+TreeInfo.symbol(s.selected));
			if (s.sym.owner != TreeInfo.symbol(s.selected))
				return false;
			tree = s.selected;
	    }
	    return true;
	}

    private class ConversionWarner extends Warner {
        final String key;
		final Type found;
        final Type expected;
		public ConversionWarner(DiagnosticPosition pos, String key, Type found, Type expected) {
            super(pos);
            this.key = key;
			this.found = found;
			this.expected = expected;
		}

		public void warnUnchecked() {
			boolean warned = this.warned;
			super.warnUnchecked();
			if (warned) return; // suppress redundant diagnostics
			Object problem = JCDiagnostic.fragment(key);
			Check.this.warnUnchecked(pos(), "prob.found.req", problem, found, expected);
		}
    }

    public Warner castWarner(DiagnosticPosition pos, Type found, Type expected) {
		return new ConversionWarner(pos, "unchecked.cast.to.type", found, expected);
    }

    public Warner convertWarner(DiagnosticPosition pos, Type found, Type expected) {
		return new ConversionWarner(pos, "unchecked.assign", found, expected);
    }
}
