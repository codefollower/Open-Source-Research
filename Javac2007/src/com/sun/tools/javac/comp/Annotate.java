/*
 * @(#)Annotate.java	1.39 07/03/21
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

import com.sun.tools.javac.util.*;
import com.sun.tools.javac.code.*;
import com.sun.tools.javac.code.Symbol.*;
import com.sun.tools.javac.tree.*;
import com.sun.tools.javac.tree.JCTree.*;

/** Enter annotations on symbols.  Annotations accumulate in a queue,
 *  which is processed at the top level of any set of recursive calls
 *  requesting it be processed.
 *
 *  <p><b>This is NOT part of any API supported by Sun Microsystems.  If
 *  you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 */
@Version("@(#)Annotate.java	1.39 07/03/21")
public class Annotate {
	private static my.Debug DEBUG=new my.Debug(my.Debug.Annotate);//我加上的
	
    protected static final Context.Key<Annotate> annotateKey =
	new Context.Key<Annotate>();

    public static Annotate instance(Context context) {
		Annotate instance = context.get(annotateKey);
		if (instance == null)
			instance = new Annotate(context);
		return instance;
    }

    final Attr attr;
    final TreeMaker make;
    final Log log;
    final Symtab syms;
    final Name.Table names;
    final Resolve rs;
    final Types types;
    final ConstFold cfolder;
    final Check chk;

    protected Annotate(Context context) {
		DEBUG.P(this,"Annotate(1)");
		
		context.put(annotateKey, this);
		attr = Attr.instance(context);
		make = TreeMaker.instance(context);
		log = Log.instance(context);
		syms = Symtab.instance(context);
		names = Name.Table.instance(context);
		rs = Resolve.instance(context);
		types = Types.instance(context);
		cfolder = ConstFold.instance(context);
		chk = Check.instance(context);
		
		DEBUG.P(0,this,"Annotate(1)");
    }

/* ********************************************************************
 * Queue maintenance
 *********************************************************************/

    private int enterCount = 0;

    ListBuffer<Annotator> q = new ListBuffer<Annotator>();

    public void later(Annotator a) {
		q.append(a);
    }

    public void earlier(Annotator a) {
		q.prepend(a);
    }

    /** Called when the Enter phase starts. */
    public void enterStart() {
        enterCount++;
    }

    /** Called after the Enter phase completes. */
    public void enterDone() {
		DEBUG.P(this,"enterDone()");
		DEBUG.P("enterCount="+enterCount);
    
        enterCount--;
		flush();
		
		DEBUG.P(0,this,"enterDone()");
    }

    public void flush() {
		try {//我加上的
		DEBUG.P(this,"flush()");
		DEBUG.P("enterCount="+enterCount);
		DEBUG.P("q.nonEmpty()="+q.nonEmpty());

		if (enterCount != 0) return;
		
		enterCount++;
		try {
			//ListBuffer<Annotator> myq = q;
			//while (myq.nonEmpty())
			//	DEBUG.P("myq.next().toString()="+myq.next().toString());
			//while (q.nonEmpty())
			//	q.next().enterAnnotation();

			DEBUG.P("q.size()="+q.size());
			while (q.nonEmpty()) {
				Annotator aa = q.next();
				DEBUG.P("aa="+aa);
				aa.enterAnnotation();
			}
		} finally {
			enterCount--;
		}
		
		}finally{//我加上的
		DEBUG.P(0,this,"flush()");
		}
    }

    /** A client that has annotations to add registers an annotator,
     *  the method it will use to add the annotation.  There are no
     *  parameters; any needed data should be captured by the
     *  Annotator.
     */
    public interface Annotator {
		void enterAnnotation();
		String toString();
    }


/* ********************************************************************
 * Compute an attribute from its annotation.
 *********************************************************************/

    /** Process a single compound annotation, returning its
     *  Attribute. Used from MemberEnter for attaching the attributes
     *  to the annotated symbol.
     */
    Attribute.Compound enterAnnotation(JCAnnotation a,
                                       Type expected,
				       Env<AttrContext> env) {
        try {//我加上的
		DEBUG.P(this,"enterAnnotation(3)");
		DEBUG.P("a="+a);
		DEBUG.P("a.annotationType.type="+a.annotationType.type);
		DEBUG.P("expected="+expected);
        DEBUG.P("env="+env);
		
		// The annotation might have had its type attributed (but not checked)
		// by attr.attribAnnotationTypes during MemberEnter, in which case we do not
		// need to do it again.
		Type at = (a.annotationType.type != null ? a.annotationType.type
			  : attr.attribType(a.annotationType, env));
		a.type = chk.checkType(a.annotationType.pos(), at, expected);
		DEBUG.P("a.type="+a.type);
		DEBUG.P("a.type.isErroneous()="+a.type.isErroneous());
		/*例:不兼容的类型
		@AnnotationB
		class AnnotateTest{}
		class AnnotationB{}
		*/
		if (a.type.isErroneous())
			return new Attribute.Compound(a.type, List.<Pair<MethodSymbol,Attribute>>nil());
		
		/*例:test.memberEnter.AnnotationC 不是注释类型
		@AnnotationC
		class AnnotateTest{}
		class AnnotationC implements java.lang.annotation.Annotation{}
		*/
		if ((a.type.tsym.flags() & Flags.ANNOTATION) == 0) {
			log.error(a.annotationType.pos(), 
						  "not.annotation.type", a.type.toString());
			return new Attribute.Compound(a.type, List.<Pair<MethodSymbol,Attribute>>nil());
		}
		List<JCExpression> args = a.args;
		DEBUG.P("args="+args);
		DEBUG.P("args.length()="+args.length());
		if (args.length() == 1 && args.head.tag != JCTree.ASSIGN) {
			// special case: elided "value=" assumed
			args.head = make.at(args.head.pos).
			Assign(make.Ident(names.value), args.head);
		}
		ListBuffer<Pair<MethodSymbol,Attribute>> buf =
			new ListBuffer<Pair<MethodSymbol,Attribute>>();
		for (List<JCExpression> tl = args; tl.nonEmpty(); tl = tl.tail) {
			JCExpression t = tl.head;
			if (t.tag != JCTree.ASSIGN) {
				log.error(t.pos(), "annotation.value.must.be.name.value");
				continue;
			}
			JCAssign assign = (JCAssign)t;
			if (assign.lhs.tag != JCTree.IDENT) {
				log.error(t.pos(), "annotation.value.must.be.name.value");
				continue;
			}
			JCIdent left = (JCIdent)assign.lhs;
			Symbol method = rs.resolveQualifiedMethod(left.pos(),
								  env,
								  a.type,
								  left.name,
								  List.<Type>nil(),
								  null);
			left.sym = method;
			left.type = method.type;
			DEBUG.P("method.owner="+method.owner);
			/*如:
			test\memberEnter\AnnotateTest.java:15: test.memberEnter.AnnotationA 中没有注释成
			员 toString
			@AnnotationA(toString=10,f2=2)
						 ^


			@AnnotationA(toString=10,f2=2)
			class AnnotateTest{}

			@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
			@interface AnnotationA{
				int f1();
				int f2();
			}
			*/
			if (method.owner != a.type.tsym)
				log.error(left.pos(), "no.annotation.member", left.name, a.type);
			Type result = method.type.getReturnType();
			DEBUG.P("result="+result);
			Attribute value = enterAttributeValue(result, assign.rhs, env);
			if (!method.type.isErroneous())
				buf.append(new Pair<MethodSymbol,Attribute>
					((MethodSymbol)method, value));
		}
		return new Attribute.Compound(a.type, buf.toList());
        
        }finally{//我加上的
		DEBUG.P(0,this,"enterAnnotation(3)");
		}
    }
	/*例:
	@interface AnnotationA{
		int f1();
	}
	@AnnotationA(f1=10)
	class AnnotateTest{}

	expected就是int f1()的返回类型int,
	tree就是@AnnotationA(f1=10)中的10
	*/
    Attribute enterAttributeValue(Type expected,
				  JCExpression tree,
				  Env<AttrContext> env) {
        
        try {//我加上的
		DEBUG.P(this,"enterAttributeValue(3)");
		DEBUG.P("expected="+expected);
        DEBUG.P("tree="+tree);
        DEBUG.P("env="+env);
		DEBUG.P("expected.isPrimitive()="+expected.isPrimitive());
        
		if (expected.isPrimitive() || types.isSameType(expected, syms.stringType)) {
			Type result = attr.attribExpr(tree, env, expected);

			DEBUG.P("result.isErroneous()="+result.isErroneous());
			if (result.isErroneous())
				return new Attribute.Error(expected);

			DEBUG.P("result.constValue()="+result.constValue());
			if (result.constValue() == null) {
				log.error(tree.pos(), "attribute.value.must.be.constant");
				return new Attribute.Error(expected);
			}
			result = cfolder.coerce(result, expected);
			DEBUG.P("result="+result);
			return new Attribute.Constant(expected, result.constValue());
		}
		if (expected.tsym == syms.classType.tsym) {
			Type result = attr.attribExpr(tree, env, expected);
			if (result.isErroneous())
				return new Attribute.Error(expected);
			if (TreeInfo.name(tree) != names._class) {
				log.error(tree.pos(), "annotation.value.must.be.class.literal");
				return new Attribute.Error(expected);
			}
			return new Attribute.Class(types,
						   (((JCFieldAccess) tree).selected).type);
		}
		if ((expected.tsym.flags() & Flags.ANNOTATION) != 0) {
			if (tree.tag != JCTree.ANNOTATION) {
				log.error(tree.pos(), "annotation.value.must.be.annotation");
				expected = syms.errorType;
			}
			return enterAnnotation((JCAnnotation)tree, expected, env);
		}
		if (expected.tag == TypeTags.ARRAY) { // should really be isArray()
			if (tree.tag != JCTree.NEWARRAY) {
				tree = make.at(tree.pos).
				NewArray(null, List.<JCExpression>nil(), List.of(tree));
			}
			JCNewArray na = (JCNewArray)tree;

			//例:@AnnotationA(f1=1,f2=2,f4=new int[]{1,2})
			if (na.elemtype != null) {
				log.error(na.elemtype.pos(), "new.not.allowed.in.annotation");
				return new Attribute.Error(expected);
			}
			ListBuffer<Attribute> buf = new ListBuffer<Attribute>();
			for (List<JCExpression> l = na.elems; l.nonEmpty(); l=l.tail) {
				buf.append(enterAttributeValue(types.elemtype(expected),
							l.head,
							env));
			}
			return new Attribute.
			Array(expected, buf.toArray(new Attribute[buf.length()]));
		}
		if (expected.tag == TypeTags.CLASS &&
				(expected.tsym.flags() & Flags.ENUM) != 0) {
			attr.attribExpr(tree, env, expected);
			Symbol sym = TreeInfo.symbol(tree);

			DEBUG.P("sym="+sym);
			DEBUG.P("TreeInfo.nonstaticSelect(tree)="+TreeInfo.nonstaticSelect(tree));
			if(sym != null) {
				DEBUG.P("sym.kind="+Kinds.toString(sym.kind));
				DEBUG.P("sym.flags()="+Flags.toString(sym.flags()));
			}

			/*当f6=90时，可测试sym == null
				@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
				@interface AnnotationA{
					int f1();
					int f2();
					//Class f3() default null;
					Class f3() default AnnotationA.class;

					int[] f4() default {0,1,2};

					//AnnotationB f5() default null;

					//EnumTest f6() default A;
					EnumTest f6() default EnumTest.A;
				}
				enum EnumTest{
					A,B,C;
				}
			*/
			//EnumTest f6() default this.A;测试TreeInfo.nonstaticSelect(tree)=true
			if (sym == null ||
			TreeInfo.nonstaticSelect(tree) ||
			sym.kind != Kinds.VAR ||
			(sym.flags() & Flags.ENUM) == 0) {
				log.error(tree.pos(), "enum.annotation.must.be.enum.constant");
				return new Attribute.Error(expected);
			}
			VarSymbol enumerator = (VarSymbol) sym;
			return new Attribute.Enum(expected, enumerator);
		}

		/*
		test\memberEnter\AnnotateTest.java:35: 注释值不是允许的类型
        AnnotationB f5() default null;
                                 ^
		test\memberEnter\AnnotateTest.java:35: 注释成员的类型无效
        AnnotationB f5() default null;
        ^
		例:
		@interface AnnotationA{
			int f1();
			int f2();
			//Class f3() default null;
			Class f3() default AnnotationA.class;

			int[] f4() default {0,1,2};

			AnnotationB f5() default null;
		}

		class AnnotationB{
		}
		*/
		if (!expected.isErroneous())
			log.error(tree.pos(), "annotation.value.not.allowable.type");
		return new Attribute.Error(attr.attribExpr(tree, env, expected));
        
        }finally{//我加上的
		DEBUG.P(0,this,"enterAttributeValue(3)");
		}
    }
}
