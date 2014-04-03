/*
 * @(#)Flow.java	1.91 07/03/21
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

//todo: one might eliminate uninits.andSets when monotonic

package com.sun.tools.javac.comp;

import com.sun.tools.javac.code.*;
import com.sun.tools.javac.tree.*;
import com.sun.tools.javac.util.*;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;

import com.sun.tools.javac.code.Symbol.*;
import com.sun.tools.javac.tree.JCTree.*;

import static com.sun.tools.javac.code.Flags.*;
import static com.sun.tools.javac.code.Kinds.*;
import static com.sun.tools.javac.code.TypeTags.*;

/** This pass implements dataflow analysis for Java programs.
 *  Liveness analysis checks that every statement is reachable.
 *  Exception analysis ensures that every checked exception that is
 *  thrown is declared or caught.  Definite assignment analysis
 *  ensures that each variable is assigned when used.  Definite
 *  unassignment analysis ensures that no final variable is assigned
 *  more than once.
 *
 *  <p>The second edition of the JLS has a number of problems in the
 *  specification of these flow analysis problems. This implementation
 *  attempts to address those issues.
 *
 *  <p>First, there is no accommodation for a finally clause that cannot
 *  complete normally. For liveness analysis, an intervening finally
 *  clause can cause a break, continue, or return not to reach its
 *  target.  For exception analysis, an intervening finally clause can
 *  cause any exception to be "caught".  For DA/DU analysis, the finally
 *  clause can prevent a transfer of control from propagating DA/DU
 *  state to the target.  In addition, code in the finally clause can
 *  affect the DA/DU status of variables.
 *
 *  <p>For try statements, we introduce the idea of a variable being
 *  definitely unassigned "everywhere" in a block.  A variable V is
 *  "unassigned everywhere" in a block iff it is unassigned at the
 *  beginning of the block and there is no reachable assignment to V
 *  in the block.  An assignment V=e is reachable iff V is not DA
 *  after e.  Then we can say that V is DU at the beginning of the
 *  catch block iff V is DU everywhere in the try block.  Similarly, V
 *  is DU at the beginning of the finally block iff V is DU everywhere
 *  in the try block and in every catch block.  Specifically, the
 *  following bullet is added to 16.2.2
 *  <pre>
 *	V is <em>unassigned everywhere</em> in a block if it is
 *	unassigned before the block and there is no reachable
 *	assignment to V within the block.
 *  </pre>
 *  <p>In 16.2.15, the third bullet (and all of its sub-bullets) for all
 *  try blocks is changed to
 *  <pre>
 *	V is definitely unassigned before a catch block iff V is
 *	definitely unassigned everywhere in the try block.
 *  </pre>
 *  <p>The last bullet (and all of its sub-bullets) for try blocks that
 *  have a finally block is changed to
 *  <pre>
 *	V is definitely unassigned before the finally block iff
 *	V is definitely unassigned everywhere in the try block
 *	and everywhere in each catch block of the try statement.
 *  </pre>
 *  <p>In addition,
 *  <pre>
 *	V is definitely assigned at the end of a constructor iff
 *	V is definitely assigned after the block that is the body
 *	of the constructor and V is definitely assigned at every
 *	return that can return from the constructor.
 *  </pre>
 *  <p>In addition, each continue statement with the loop as its target
 *  is treated as a jump to the end of the loop body, and "intervening"
 *  finally clauses are treated as follows: V is DA "due to the
 *  continue" iff V is DA before the continue statement or V is DA at
 *  the end of any intervening finally block.  V is DU "due to the
 *  continue" iff any intervening finally cannot complete normally or V
 *  is DU at the end of every intervening finally block.  This "due to
 *  the continue" concept is then used in the spec for the loops.
 *
 *  <p>Similarly, break statements must consider intervening finally
 *  blocks.  For liveness analysis, a break statement for which any
 *  intervening finally cannot complete normally is not considered to
 *  cause the target statement to be able to complete normally. Then
 *  we say V is DA "due to the break" iff V is DA before the break or
 *  V is DA at the end of any intervening finally block.  V is DU "due
 *  to the break" iff any intervening finally cannot complete normally
 *  or V is DU at the break and at the end of every intervening
 *  finally block.  (I suspect this latter condition can be
 *  simplified.)  This "due to the break" is then used in the spec for
 *  all statements that can be "broken".
 *
 *  <p>The return statement is treated similarly.  V is DA "due to a
 *  return statement" iff V is DA before the return statement or V is
 *  DA at the end of any intervening finally block.  Note that we
 *  don't have to worry about the return expression because this
 *  concept is only used for construcrors.
 *
 *  <p>There is no spec in JLS2 for when a variable is definitely
 *  assigned at the end of a constructor, which is needed for final
 *  fields (8.3.1.2).  We implement the rule that V is DA at the end
 *  of the constructor iff it is DA and the end of the body of the
 *  constructor and V is DA "due to" every return of the constructor.
 *
 *  <p>Intervening finally blocks similarly affect exception analysis.	An
 *  intervening finally that cannot complete normally allows us to ignore
 *  an otherwise uncaught exception.
 *
 *  <p>To implement the semantics of intervening finally clauses, all
 *  nonlocal transfers (break, continue, return, throw, method call that
 *  can throw a checked exception, and a constructor invocation that can
 *  thrown a checked exception) are recorded in a queue, and removed
 *  from the queue when we complete processing the target of the
 *  nonlocal transfer.  This allows us to modify the queue in accordance
 *  with the above rules when we encounter a finally clause.  The only
 *  exception to this [no pun intended] is that checked exceptions that
 *  are known to be caught or declared to be caught in the enclosing
 *  method are not recorded in the queue, but instead are recorded in a
 *  global variable "Set<Type> thrown" that records the type of all
 *  exceptions that can be thrown.
 *
 *  <p>Other minor issues the treatment of members of other classes
 *  (always considered DA except that within an anonymous class
 *  constructor, where DA status from the enclosing scope is
 *  preserved), treatment of the case expression (V is DA before the
 *  case expression iff V is DA after the switch expression),
 *  treatment of variables declared in a switch block (the implied
 *  DA/DU status after the switch expression is DU and not DA for
 *  variables defined in a switch block), the treatment of boolean ?:
 *  expressions (The JLS rules only handle b and c non-boolean; the
 *  new rule is that if b and c are boolean valued, then V is
 *  (un)assigned after a?b:c when true/false iff V is (un)assigned
 *  after b when true/false and V is (un)assigned after c when
 *  true/false).
 *
 *  <p>There is the remaining question of what syntactic forms constitute a
 *  reference to a variable.  It is conventional to allow this.x on the
 *  left-hand-side to initialize a final instance field named x, yet
 *  this.x isn't considered a "use" when appearing on a right-hand-side
 *  in most implementations.  Should parentheses affect what is
 *  considered a variable reference?  The simplest rule would be to
 *  allow unqualified forms only, parentheses optional, and phase out
 *  support for assigning to a final field via this.x.
 *
 *  <p><b>This is NOT part of any API supported by Sun Microsystems.  If
 *  you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 */
@Version("@(#)Flow.java	1.91 07/03/21")
public class Flow extends TreeScanner {
  //Flow变量声明部份
    private static my.Debug DEBUG=new my.Debug(my.Debug.Flow);//我加上的
	
    protected static final Context.Key<Flow> flowKey =
	new Context.Key<Flow>();

    private final Name.Table names;
    private final Log log;
    private final Symtab syms;
    private final Types types;
    private final Check chk;
    private       TreeMaker make;
    private       Lint lint;

    public static Flow instance(Context context) {
		Flow instance = context.get(flowKey);
		if (instance == null)
			instance = new Flow(context);
		return instance;
    }

    protected Flow(Context context) {
        DEBUG.P(this,"Flow(1)");

		context.put(flowKey, this);

		names = Name.Table.instance(context);
		log = Log.instance(context);
		syms = Symtab.instance(context);
		types = Types.instance(context);
		chk = Check.instance(context);
		lint = Lint.instance(context);
        DEBUG.P(0,this,"Flow(1)");
    }

	//我加上的，打印未初始化变量名
	//uninits中代表的并不一定是所有未初始化变量，
	//得将uninits与inits进行集合差运算，
	//也就是从uninits中减去inits包含的所有元素
	void myUninitVars(Bits inits,Bits uninits) {
		DEBUG.P(this,"myUninitVars(2)");
		DEBUG.P("uninits  = "+uninits);
		DEBUG.P("inits    = "+inits);
		if(inits!=null && uninits!=null) {
			Bits diffSet = uninits.diffSet(inits);
			DEBUG.P("前减后得 = "+diffSet);
			DEBUG.P("未初始化的变量有:");
			DEBUG.P("----------------------------------");
			for(int i=0;i<vars.length;i++)
				if (vars[i]!=null && diffSet.isMember(vars[i].adr))
					DEBUG.P("vars["+i+"]="+vars[i]+" adr="+vars[i].adr);
		}
		DEBUG.P(0,this,"myUninitVars(2)");
	}

	void myUninitVars(Bits bits) {
		DEBUG.P(this,"myUninitVars(1)");
		DEBUG.P("bits = "+bits);
		if(bits!=null) {
			DEBUG.P("未初始化的变量有:");
			DEBUG.P("----------------------------------");
			for(int i=0;i<vars.length;i++)
				if (vars[i]!=null && bits.isMember(vars[i].adr))
					DEBUG.P("vars["+i+"]="+vars[i]+" adr="+vars[i].adr);
		}
		DEBUG.P(0,this,"myUninitVars(1)");
	}

    /** A flag that indicates whether the last statement could
     *	complete normally.
     */
    private boolean alive;

    /** The set of definitely assigned variables.
     */
    Bits inits;

    /** The set of definitely unassigned variables.
     */
    Bits uninits;

    /** The set of variables that are definitely unassigned everywhere
     *	in current try block. This variable is maintained lazily; it is
     *	updated only when something gets removed from uninits,
     *	typically by being assigned in reachable code.	To obtain the
     *	correct set of variables which are definitely unassigned
     *	anywhere in current try block, intersect uninitsTry and
     *	uninits.
     */
    Bits uninitsTry;

    /** When analyzing a condition, inits and uninits are null.
     *	Instead we have:
     */
    Bits initsWhenTrue;
    Bits initsWhenFalse;
    Bits uninitsWhenTrue;
    Bits uninitsWhenFalse;

    /** A mapping from addresses to variable symbols.
     */
    VarSymbol[] vars;

    /** The current class being defined.
     */
    JCClassDecl classDef;

    /** The first variable sequence number in this class definition.
     */
    int firstadr;

    /** The next available variable sequence number.
     */
    int nextadr;

    /** The list of possibly thrown declarable exceptions.
     */
    List<Type> thrown;

    /** The list of exceptions that are either caught or declared to be
     *	thrown.
     */
    List<Type> caught;

    /** Set when processing a loop body the second time for DU analysis. */
    boolean loopPassTwo = false;
  //
  //

    /*-------------------- Environments ----------------------*/

    /** A pending exit.	 These are the statements return, break, and
     *	continue.  In addition, exception-throwing expressions or
     *	statements are put here when not known to be caught.  This
     *	will typically result in an error unless it is within a
     *	try-finally whose finally block cannot complete normally.
     */
	//在markThrown、recordExit两个方法中调用PendingExit的构造函数
    //static class PendingExit {
	class PendingExit {
		JCTree tree;
		Bits inits;
		Bits uninits;
		Type thrown;

		//由continue、break、return语句产生
		//参数tree是JCContinue、JCBreak、JCReturn
		//inits与uninits是在continue、break、return语句之前变量的赋值情况
		PendingExit(JCTree tree, Bits inits, Bits uninits) {
			DEBUG.P(this,"PendingExit(3)");

			this.tree = tree;
			this.inits = inits.dup();
			this.uninits = uninits.dup();

			DEBUG.P("tree    ="+tree);
			DEBUG.P("inits   ="+inits);
			DEBUG.P("uninits ="+uninits);

			//Flow.this.myUninitVars(inits,uninits);
			myUninitVars(inits,uninits);

			DEBUG.P(0,this,"PendingExit(3)");
		}
		//由throw语句，方法调用语句、new 类()语句产生
		//其中方法调用语句对应的文法声明带有throws，
		//new 类()语句对应的构造函数也带有throws
		//参数tree是JCThrow、JCMethodInvocation、JCNewClass
		PendingExit(JCTree tree, Type thrown) {
			DEBUG.P(this,"PendingExit(2)");

			this.tree = tree;
			this.thrown = thrown;

			DEBUG.P("tree  ="+tree);
			DEBUG.P("thrown="+thrown);

			DEBUG.P(0,this,"PendingExit(2)");
		}
    }

    /** The currently pending exits that go from current inner blocks
     *	to an enclosing block, in source order.
     */
    ListBuffer<PendingExit> pendingExits;

    /*-------------------- Exceptions ----------------------*/

    /** Complain that pending exceptions are not caught.
     */
    void errorUncaught() {
		DEBUG.P(this,"errorUncaught()");
		DEBUG.P("pendingExits.size()="+pendingExits.size());
		for (PendingExit exit = pendingExits.next();
			 exit != null;
			 exit = pendingExits.next()) {
			boolean synthetic = classDef != null &&
			classDef.pos == exit.tree.pos;

			DEBUG.P("synthetic="+synthetic);

			/* //unreported.exception.default.constructor
			test\flow\test.java:92: 默认构造函数中未报告的异常 java.lang.Exception
					class B extends A {}
					^
			1 错误
			------------------------------------------
			class Test {
				class A {
					A() throws Exception {}
				}
				class B extends A {}
			}
			*/

			log.error(exit.tree.pos(),
				  synthetic
				  ? "unreported.exception.default.constructor"
				  : "unreported.exception.need.to.catch.or.throw",
				  exit.thrown);
		}
		
		//因为在for中调用了pendingExits.next()，
		//所以pendingExits.size()最后总是为0
		DEBUG.P("pendingExits.size()="+pendingExits.size());
		DEBUG.P(0,this,"errorUncaught()");
    }

    /** Record that exception is potentially thrown and check that it
     *	is caught.
     */
    void markThrown(JCTree tree, Type exc) {
		DEBUG.P(this,"markThrown(2)");
		DEBUG.P("exc="+exc);
		DEBUG.P("exc.isUnchecked="+chk.isUnchecked(tree.pos(), exc));
		//DEBUG.P("exc.tag="+TypeTags.toString(exc.tag));
		DEBUG.P("caught="+caught);
		DEBUG.P("thrown="+thrown);
		
		//当调用的某一个方法抛出的异常不是
		//java.lang.RuntimeException、java.lang.Error及其子类时，
		//且调用者又没有捕获异常时，
		//将异常加入pendingExits(另请参见Check中的注释)
		if (!chk.isUnchecked(tree.pos(), exc)) {
			DEBUG.P("exc.isHandled="+chk.isHandled(exc, caught));
			if (!chk.isHandled(exc, caught))
				pendingExits.append(new PendingExit(tree, exc));
			thrown = chk.incl(exc, thrown);
		}
		DEBUG.P("thrown="+thrown);
		DEBUG.P(0,this,"markThrown(2)");
    }

    /*-------------- Processing variables ----------------------*/

    /** Do we need to track init/uninit state of this symbol?
     *	I.e. is symbol either a local or a blank final variable?
     */
    boolean trackable(VarSymbol sym) {
    	///*
		return
	    (sym.owner.kind == MTH ||
	     ((sym.flags() & (FINAL | HASINIT | PARAMETER)) == FINAL &&
	      classDef.sym.isEnclosedBy((ClassSymbol)sym.owner)));
	      //*/
	    

		/*
		//我加上的
		DEBUG.P(this,"trackable(VarSymbol sym)");
		DEBUG.P("sym="+sym);
		DEBUG.P("sym.flags()="+Flags.toString(sym.flags()));
		DEBUG.P("((sym.flags() & (FINAL | HASINIT | PARAMETER))="+Flags.toString(((sym.flags() & (FINAL | HASINIT | PARAMETER)))));
		DEBUG.P("sym.owner="+sym.owner);
		DEBUG.P("sym.owner.kind="+Kinds.toString(sym.owner.kind));
		
		//方法中的变量(本地变量)与没有初始化的FINAL成员变量(不含PARAMETER)需要track
		boolean trackable=(sym.owner.kind == MTH ||
			 ((sym.flags() & (FINAL | HASINIT | PARAMETER)) == FINAL &&
			  classDef.sym.isEnclosedBy((ClassSymbol)sym.owner)));
		
		DEBUG.P("trackable="+trackable);
		DEBUG.P(0,this,"trackable(VarSymbol sym)");
		return trackable;
		*/
    }

    /** Initialize new trackable variable by setting its address field
     *	to the next available sequence number and entering it under that
     *	index into the vars array.
     */
	void newVar(VarSymbol sym) {
		DEBUG.P(this,"newVar(VarSymbol sym)");
		DEBUG.P("sym="+sym);
		DEBUG.P("sym.adr="+sym.adr);
		DEBUG.P("nextadr="+nextadr+"   vars.length="+vars.length);
		DEBUG.P("inits  ="+inits);
		DEBUG.P("uninits="+uninits);
		if (nextadr == vars.length) {
			VarSymbol[] newvars = new VarSymbol[nextadr * 2];
			System.arraycopy(vars, 0, newvars, 0, nextadr);
			vars = newvars;
		}
		//注意:uninits的某一bit以及vars[nextadr]有可能被覆盖的情况
		sym.adr = nextadr;
		DEBUG.P("vars["+nextadr+"]前="+vars[nextadr]);
		vars[nextadr] = sym;
		DEBUG.P("vars["+nextadr+"]后="+vars[nextadr]);
		inits.excl(nextadr);
		uninits.incl(nextadr);
		nextadr++;
		DEBUG.P("nextadr="+nextadr);
		DEBUG.P("inits  ="+inits);
		DEBUG.P("uninits="+uninits);
		DEBUG.P(0,this,"newVar(VarSymbol sym)");
    }

    /** Record an initialization of a trackable variable.
     */
    void letInit(DiagnosticPosition pos, VarSymbol sym) {
		DEBUG.P(this,"letInit(2)");
		DEBUG.P("sym="+sym);
		DEBUG.P("sym.adr="+sym.adr);
		DEBUG.P("firstadr="+firstadr);
		DEBUG.P("inits="+inits);
		if (sym.adr >= firstadr && trackable(sym)) {
			if ((sym.flags() & FINAL) != 0) {
				if ((sym.flags() & PARAMETER) != 0) {
					/*例子:
					void myMethod(final int i) {
						i++;
					}
					*/
					log.error(pos, "final.parameter.may.not.be.assigned",
						  sym);
				} else if (!uninits.isMember(sym.adr)) {
					log.error(pos,
						  loopPassTwo
						  ? "var.might.be.assigned.in.loop"
						  : "var.might.already.be.assigned",
						  sym);
				} else if (!inits.isMember(sym.adr)) {
					DEBUG.P("sym.adr="+sym.adr);
					DEBUG.P("uninits   前="+uninits);
					DEBUG.P("uninitsTry前="+uninitsTry);
					// reachable assignment
					uninits.excl(sym.adr);
					uninitsTry.excl(sym.adr);
					
					DEBUG.P("uninits   后="+uninits);
					DEBUG.P("uninitsTry后="+uninitsTry);
				} else {
					//log.rawWarning(pos, "unreachable assignment");//DEBUG
					uninits.excl(sym.adr);
				}
			}
			inits.incl(sym.adr);
		} else if ((sym.flags() & FINAL) != 0) {
			log.error(pos, "var.might.already.be.assigned", sym);
		}
		
		DEBUG.P("inits="+inits);
		DEBUG.P(0,this,"letInit(2)");
    }

    /** If tree is either a simple name or of the form this.name or
     *	C.this.name, and tree represents a trackable variable,
     *	record an initialization of the variable.
     */
    void letInit(JCTree tree) {
		DEBUG.P(this,"letInit(1)");
		tree = TreeInfo.skipParens(tree);
		DEBUG.P("tree.tag="+tree.myTreeTag());
		if (tree.tag == JCTree.IDENT || tree.tag == JCTree.SELECT) {
			Symbol sym = TreeInfo.symbol(tree);
			letInit(tree.pos(), (VarSymbol)sym);
		}
		DEBUG.P(0,this,"letInit(1)");
    }

    /** Check that trackable variable is initialized.
     */
    void checkInit(DiagnosticPosition pos, VarSymbol sym) {
		DEBUG.P(this,"checkInit(2)");
		DEBUG.P("sym="+sym);
		DEBUG.P("sym.adr="+sym.adr);
		DEBUG.P("firstadr="+firstadr);
		DEBUG.P("inits="+inits);
		if ((sym.adr >= firstadr || sym.owner.kind != TYP) &&
			trackable(sym) &&
			!inits.isMember(sym.adr)) {
			DEBUG.P("可能尚未初始化变量:"+sym);
			//如果有多个可能尚未初始化的变量,log.error()只包告一个错误
			log.error(pos, "var.might.not.have.been.initialized",
					  sym);
			inits.incl(sym.adr);
		}
		DEBUG.P("inits="+inits);
		DEBUG.P(0,this,"checkInit(2)");
    }

    /*-------------------- Handling jumps ----------------------*/

    /** Record an outward transfer of control. */

	//碰到continue、break、return语句时调用此方法
    void recordExit(JCTree tree) {
		DEBUG.P(this,"recordExit(1)");
		
		pendingExits.append(new PendingExit(tree, inits, uninits));
		markDead();
		
		DEBUG.P(0,this,"recordExit(1)");
    }

    /** Resolve all breaks of this statement. */
    boolean resolveBreaks(JCTree tree,
			  ListBuffer<PendingExit> oldPendingExits) {
		DEBUG.P(this,"resolveBreaks(2)");
		
		boolean result = false;
		List<PendingExit> exits = pendingExits.toList();
		DEBUG.P("exits.size="+exits.size());
		pendingExits = oldPendingExits;
		for (; exits.nonEmpty(); exits = exits.tail) {
			PendingExit exit = exits.head;
			DEBUG.P("exit.tree.tag="+exit.tree.myTreeTag());
			if (exit.tree.tag == JCTree.BREAK &&
			((JCBreak) exit.tree).target == tree) {

				DEBUG.P("exit.inits  ="+exit.inits);
				DEBUG.P("exit.uninits="+exit.uninits);

				DEBUG.P("inits  前   ="+inits);
				DEBUG.P("uninits前   ="+uninits);

				inits.andSet(exit.inits);
				uninits.andSet(exit.uninits);

				DEBUG.P("inits  后   ="+inits);
				DEBUG.P("uninits后   ="+uninits);
				result = true;
			} else {
				pendingExits.append(exit);
			}
		}
		DEBUG.P("result="+result);
		DEBUG.P(0,this,"resolveBreaks(2)");
		return result;
    }

    /** Resolve all continues of this statement. */
    boolean resolveContinues(JCTree tree) {
		DEBUG.P(this,"resolveContinues(1)");
		
		boolean result = false;
		List<PendingExit> exits = pendingExits.toList();
		pendingExits = new ListBuffer<PendingExit>();
		DEBUG.P("exits.size="+exits.size());
		for (; exits.nonEmpty(); exits = exits.tail) {
			PendingExit exit = exits.head;
			DEBUG.P("exit.tree.tag="+exit.tree.myTreeTag());
			if (exit.tree.tag == JCTree.CONTINUE &&
			((JCContinue) exit.tree).target == tree) {

				DEBUG.P("exit.inits  ="+exit.inits);
				DEBUG.P("exit.uninits="+exit.uninits);

				DEBUG.P("inits  前   ="+inits);
				DEBUG.P("uninits前   ="+uninits);
				
				//在continue语句之前所有变量的赋值情况与continue语句之后
				//所有变量的赋值情况进行位与运算(and)
				inits.andSet(exit.inits);
				uninits.andSet(exit.uninits);

				DEBUG.P("inits  后   ="+inits);
				DEBUG.P("uninits后   ="+uninits);
				result = true;
			} else {
				pendingExits.append(exit);
			}
		}
		DEBUG.P("result="+result);
		DEBUG.P(0,this,"resolveContinues(1)");
		return result;
    }

    /** Record that statement is unreachable.
     */
    void markDead() {
		DEBUG.P(this,"markDead()");
		DEBUG.P("firstadr="+firstadr+"  nextadr="+nextadr);
		DEBUG.P("inits  前="+inits);
		DEBUG.P("uninits前="+uninits);
		
		inits.inclRange(firstadr, nextadr);
		uninits.inclRange(firstadr, nextadr);
		
		DEBUG.P("inits  后="+inits);
		DEBUG.P("uninits后="+uninits);
		
		alive = false;
		DEBUG.P("alive="+alive);
		DEBUG.P(0,this,"markDead()");
    }

    /** Split (duplicate) inits/uninits into WhenTrue/WhenFalse sets
     */
    void split() {
		DEBUG.P(this,"split()");
		
		initsWhenFalse = inits.dup();
		uninitsWhenFalse = uninits.dup();
		initsWhenTrue = inits;
		uninitsWhenTrue = uninits;
		inits = uninits = null;
		
		DEBUG.P(0,this,"split()");
    }

    /** Merge (intersect) inits/uninits from WhenTrue/WhenFalse sets.
     */
    void merge() {
		DEBUG.P(this,"merge()");
		DEBUG.P("inits  前="+inits);
		DEBUG.P("uninits前="+uninits);

		inits = initsWhenFalse.andSet(initsWhenTrue);
		uninits = uninitsWhenFalse.andSet(uninitsWhenTrue);

		DEBUG.P("inits  后="+inits);
		DEBUG.P("uninits后="+uninits);
		DEBUG.P(0,this,"merge()");
    }

/* ************************************************************************
 * Visitor methods for statements and definitions
 *************************************************************************/

    /** Analyze a definition.
     */
    void scanDef(JCTree tree) {
		DEBUG.P(this,"scanDef(1)");
		//DEBUG.P("alive="+alive+" tree="+tree);
		scanStat(tree);
		if (tree != null && tree.tag == JCTree.BLOCK && !alive) {
			//初始化程序必须能够正常完成
			log.error(tree.pos(),
				  "initializer.must.be.able.to.complete.normally");
		}
		DEBUG.P(0,this,"scanDef(1)");
    }

    /** Analyze a statement. Check that statement is reachable.
     */
    void scanStat(JCTree tree) {
		DEBUG.P(this,"scanStat(1)");
		DEBUG.P("alive="+alive+"  (tree != null)="+(tree != null));

		if (!alive && tree != null) {
			/*如下语句:
				if (dd>0) {
					continue;
					;
					ddd++;
				}
			错误提示:
			bin\mysrc\my\test\Test.java:105: 无法访问的语句
									;
									^
			bin\mysrc\my\test\Test.java:106: 无法访问的语句
									ddd++;
									^
			因为编译器在运到“continue”语句时，调用visitContinue(1)-->
			recordExit(1)-->markDead()，在markDead()中把alive设为false
			*/
			log.error(tree.pos(), "unreachable.stmt");
			if (tree.tag != JCTree.SKIP) alive = true;
		}
		scan(tree);

		DEBUG.P(1,this,"scanStat(1)");
    }

    /** Analyze list of statements.
     */
    void scanStats(List<? extends JCStatement> trees) {
		DEBUG.P(this,"scanStats(1)");
		if (trees == null) DEBUG.P("trees is null");
		else DEBUG.P("trees.size="+trees.size());
		
		if (trees != null)
			for (List<? extends JCStatement> l = trees; l.nonEmpty(); l = l.tail)
				scanStat(l.head);
		DEBUG.P(0,this,"scanStats(1)");	
    }

    /** Analyze an expression. Make sure to set (un)inits rather than
     *	(un)initsWhenTrue(WhenFalse) on exit.
     */
    void scanExpr(JCTree tree) {
		DEBUG.P(this,"scanExpr(1)");

		if (tree == null) DEBUG.P("tree is null");
		else DEBUG.P("tree.tag="+tree.myTreeTag());

		if (tree != null) {
			scan(tree);
			DEBUG.P("inits="+inits);
			if (inits == null) merge();
		}
		DEBUG.P(0,this,"scanExpr(1)");
    }

    /** Analyze a list of expressions.
     */
    void scanExprs(List<? extends JCExpression> trees) {
		DEBUG.P(this,"scanExprs(1)");
		if (trees == null) DEBUG.P("trees is null");
		else DEBUG.P("trees.size="+trees.size());
		
		if (trees != null)
			for (List<? extends JCExpression> l = trees; l.nonEmpty(); l = l.tail)
				scanExpr(l.head);
		
		DEBUG.P(0,this,"scanExprs(1)");	
    }

    /** Analyze a condition. Make sure to set (un)initsWhenTrue(WhenFalse)
     *	rather than (un)inits on exit.
     */
    void scanCond(JCTree tree) {
		DEBUG.P(this,"scanCond(1)");
		DEBUG.P("tree.type="+tree.type);
		DEBUG.P("tree.type.isFalse()="+tree.type.isFalse());
		DEBUG.P("tree.type.isTrue()="+tree.type.isTrue());
		DEBUG.P("firstadr="+firstadr+"  nextadr="+nextadr);

		DEBUG.P("");
		DEBUG.P("inits   ="+inits);
		DEBUG.P("uninits ="+uninits);

		//Bits initsPrev = inits.dup();//我加上的
		//Bits uninitsPrev = uninits.dup();//我加上的

		if (tree.type.isFalse()) {//如if(false)，条件表达式的值在编译阶段已知的情况
			if (inits == null) merge();
			initsWhenTrue = inits.dup();
			//因为如果是if(false)，那么then语句部份就不会执行，
			//所以就把initsWhenTrue中从firstadr到nextadr(不包含)的位都置1,
			//这样then语句中涉及的变量都假定它们都己初始化过了
			initsWhenTrue.inclRange(firstadr, nextadr);
			uninitsWhenTrue = uninits.dup();
			//同上
			uninitsWhenTrue.inclRange(firstadr, nextadr);
			initsWhenFalse = inits;
			uninitsWhenFalse = uninits;
		} else if (tree.type.isTrue()) {//如if(true)，条件表达式的值在编译阶段已知的情况
			if (inits == null) merge();
			initsWhenFalse = inits.dup();
			//因为如果是if(true)，那么else语句部份就不会执行，
			//所以就把initsWhenFalse中从firstadr到nextadr(不包含)的位都置1,
			//这样else语句中涉及的变量都假定它们都己初始化过了
			initsWhenFalse.inclRange(firstadr, nextadr);
			uninitsWhenFalse = uninits.dup();
			//同上
			uninitsWhenFalse.inclRange(firstadr, nextadr);
			initsWhenTrue = inits;
			uninitsWhenTrue = uninits;
		} else {//如if(i>0)，条件表达式包含变量且真假值在编译阶段未知的情况
			scan(tree);
			if (inits != null) split();//都要检查
		}
		inits = uninits = null;

		DEBUG.P("");
		//DEBUG.P("inits前         ="+initsPrev+"     inits后="+inits);
		//DEBUG.P("initsWhenFalse  ="+initsWhenFalse);
		//DEBUG.P("initsWhenTrue   ="+initsWhenTrue);
		//DEBUG.P("");
		//DEBUG.P("uninits前       ="+uninitsPrev+"     uninits后="+uninits);

		DEBUG.P("initsWhenFalse   ="+initsWhenFalse);
		DEBUG.P("uninitsWhenFalse ="+uninitsWhenFalse);
		DEBUG.P("");
		DEBUG.P("initsWhenTrue    ="+initsWhenTrue);
		DEBUG.P("uninitsWhenTrue  ="+uninitsWhenTrue);

		//myUninitVars(initsPrev,uninitsPrev);

		myUninitVars(initsWhenFalse.andSet(initsWhenTrue),
			uninitsWhenFalse.andSet(uninitsWhenTrue));
		DEBUG.P(0,this,"scanCond(1)");
    }

    /* ------------ Visitor methods for various sorts of trees -------------*/

    public void visitClassDef(JCClassDecl tree) {
		DEBUG.P(this,"visitClassDef(JCClassDecl tree)");
		if (tree.sym == null) return;
		DEBUG.P("tree.name="+tree.name);
		DEBUG.P("tree.sym="+tree.sym);

		JCClassDecl classDefPrev = classDef;
		List<Type> thrownPrev = thrown;
		List<Type> caughtPrev = caught;

		DEBUG.P("thrownPrev="+thrownPrev);
		DEBUG.P("caughtPrev="+caughtPrev);

		boolean alivePrev = alive;
		int firstadrPrev = firstadr;
		int nextadrPrev = nextadr;
		ListBuffer<PendingExit> pendingExitsPrev = pendingExits;
		Lint lintPrev = lint;

		pendingExits = new ListBuffer<PendingExit>();

		//不是匿名类
		if (tree.name != names.empty) {
			caught = List.nil();
			firstadr = nextadr;
		}
		classDef = tree;
		thrown = List.nil();
		lint = lint.augment(tree.sym.attributes_field);

		try {
			// define all the static fields
			//DEBUG.P("");DEBUG.P("define all the static fields......");
			DEBUG.P("");DEBUG.P("检查没有显示初始化的static final变量......");
			DEBUG.P("-------------------------------------------");
			//DEBUG.P("tree="+tree);
			//如果类中没有定义任何构造函数，
			//那么由编译器生成的默认构造函数 "类名(){super();}" 将放在tree.defs的最前面.
			//参见MemberEnter中的DefaultConstructor
			///*
			for (List<JCTree> l = tree.defs; l.nonEmpty(); l = l.tail) {
				DEBUG.P("l.head.tag="+l.head.myTreeTag());
				DEBUG.P("l="+l);
				DEBUG.P("");
			}
			//*/
			for (List<JCTree> l = tree.defs; l.nonEmpty(); l = l.tail) {
				//DEBUG.P("l.head.tag="+l.head.myTreeTag());
				if (l.head.tag == JCTree.VARDEF) {
					JCVariableDecl def = (JCVariableDecl)l.head;
					//DEBUG.P("def.mods.flags="+Flags.toString(def.mods.flags));
					//DEBUG.P("l.head="+l.head);
					//找出所有标记为static final但没有初始化的字段,并用uninits记录下来
					if ((def.mods.flags & STATIC) != 0) {
						VarSymbol sym = def.sym;
						if (trackable(sym))
							newVar(sym);
					}
				}
				//DEBUG.P("");
			}
			
			DEBUG.P(2);
			DEBUG.P("可能尚未初始化的static final变量有:");
			DEBUG.P("----------------------------------");
			for(int i=0;i<vars.length;i++)
				if (vars[i]!=null) DEBUG.P("vars["+i+"]="+vars[i]);
			DEBUG.P(2);

			// process all the static initializers
			DEBUG.P("");DEBUG.P("process all the static initializers......");
			DEBUG.P("----------------------------------");
			/*
			for (List<JCTree> l = tree.defs; l.nonEmpty(); l = l.tail) {
				if (l.head.tag != JCTree.METHODDEF &&
					(TreeInfo.flags(l.head) & STATIC) != 0) {
					//DEBUG.P("l.head.tag="+l.head.getKind());
					DEBUG.P("l.head.tag="+l.head.myTreeTag());
					DEBUG.P("l.head="+l.head);
					DEBUG.P("");
				}
			}
			*/
			
			//静态初始化块"static {...}"和实例初始化块"{...}"的tag是BLOCK
			for (List<JCTree> l = tree.defs; l.nonEmpty(); l = l.tail) {
				//满足if条件的有:静态变量、静态block、静态成员类
				if (l.head.tag != JCTree.METHODDEF &&
					(TreeInfo.flags(l.head) & STATIC) != 0) {
					//DEBUG.P("l.head.tag="+l.head.getKind());
					DEBUG.P("l.head.tag="+l.head.myTreeTag());
					scanDef(l.head);
					
					/*
					//在静态块中有可能调用抛出异常的静态方法，但是没有捕获
					static {
						final int i4=myStaticMethod();
					}
					
					public static int myStaticMethod() throws Exception{
						return 10;
					}
					
					错误提示:
					bin\mysrc\my\test\Test.java:44: 未报告的异常 java.lang.Exception；必须对其进行捕捉或声明以便抛出
						final int i4=myStaticMethod();
												   ^
					*/
					errorUncaught();
					
					DEBUG.P("");
				}
			}
			
			//注意:执行完上面的代码后，即使static final变量没有初始化还是不能发现错误
			
			DEBUG.P("tree.name="+tree.name);
			// add intersection of all thrown clauses of initial constructors
			// to set of caught exceptions, unless class is anonymous.
			if (tree.name != names.empty) {
				/*
				在所有构造方法中找出第一条语句不是this()调用的所有构造方法
				将这些构造方法抛出的异常构成一个交集
				
				例子:
				Test() {
					this(2);
				}
				Test(int myInt) throws Error, Exception {
					this.myInt=myInt;
				}
				Test(float f) throws Exception {
				}
				
				第一条语句不是this()调用的所有构造方法有:Test(int myInt)与Test(float f)
				抛出的异常构成一个交集:Exception
				*/
				DEBUG.P("caught="+caught);
				boolean firstConstructor = true;
				for (List<JCTree> l = tree.defs; l.nonEmpty(); l = l.tail) {
					boolean isInitialConstructor=TreeInfo.isInitialConstructor(l.head);
					//DEBUG.P("l.head.tag="+l.head.getKind());
					DEBUG.P("l.head.tag="+l.head.myTreeTag());
					DEBUG.P("l.head="+l.head);
					DEBUG.P("isInitialConstructor="+isInitialConstructor);
					
					
					//if (TreeInfo.isInitialConstructor(l.head)) {
					if (isInitialConstructor) {
						List<Type> mthrown =
							((JCMethodDecl) l.head).sym.type.getThrownTypes();
						DEBUG.P("mthrown="+mthrown);
						if (firstConstructor) {
							caught = mthrown;
							firstConstructor = false;
						} else {
							caught = chk.intersect(mthrown, caught);
						}
					}
					DEBUG.P("");
				}
			}
			DEBUG.P("caught="+caught);

			//只有未初始化的final实例字段才trackable
			DEBUG.P("");DEBUG.P("define all the instance fields......");
			DEBUG.P("-------------------------------------------");
			// define all the instance fields
			for (List<JCTree> l = tree.defs; l.nonEmpty(); l = l.tail) {
				if (l.head.tag == JCTree.VARDEF) {
					JCVariableDecl def = (JCVariableDecl)l.head;
					if ((def.mods.flags & STATIC) == 0) {
						VarSymbol sym = def.sym;
						if (trackable(sym))
							newVar(sym);
					}
				}
			}
			
			DEBUG.P("");DEBUG.P("process all the instance initializers......");
			DEBUG.P("-------------------------------------------");
			/*//所有没有siatic的JCTree
			for (List<JCTree> l = tree.defs; l.nonEmpty(); l = l.tail) {
				if (l.head.tag != JCTree.METHODDEF &&
				(TreeInfo.flags(l.head) & STATIC) == 0) {
					DEBUG.P("l.head.tag="+l.head.getKind());
					DEBUG.P("l.head.flags="+Flags.toString(TreeInfo.flags(l.head)));
					DEBUG.P("l.head="+l.head);
					DEBUG.P("");
				}
			}
			*/
			
			// process all the instance initializers
			for (List<JCTree> l = tree.defs; l.nonEmpty(); l = l.tail) {
				if (l.head.tag != JCTree.METHODDEF &&
					(TreeInfo.flags(l.head) & STATIC) == 0) {
					scanDef(l.head);

					/* 例子:
					class Test {
						//未报告的异常 java.lang.Exception；必须对其进行捕捉或声明以便抛出
						int a = m();
						{
							//未报告的异常 java.lang.Exception；必须对其进行捕捉或声明以便抛出
							m();
						}

						int m() throws Exception {return 0;}
					}
					*/
					errorUncaught();
				}
			}

			// in an anonymous class, add the set of thrown exceptions to
			// the throws clause of the synthetic constructor and propagate
			// outwards.
			if (tree.name == names.empty) {
				for (List<JCTree> l = tree.defs; l.nonEmpty(); l = l.tail) {
					if (TreeInfo.isInitialConstructor(l.head)) {
						JCMethodDecl mdef = (JCMethodDecl)l.head;
						mdef.thrown = make.Types(thrown);
						mdef.sym.type.setThrown(thrown);
					}
				}
				thrownPrev = chk.union(thrown, thrownPrev);
			}
			
			DEBUG.P("");DEBUG.P("process all the methods......");
			DEBUG.P("-------------------------------------------");
			// process all the methods
			for (List<JCTree> l = tree.defs; l.nonEmpty(); l = l.tail) {
				if (l.head.tag == JCTree.METHODDEF) {
					scan(l.head);
					errorUncaught();
					
					DEBUG.P("处理结束 方法名:"+((JCMethodDecl)l.head).name);
					DEBUG.P(2);
				}
			}

			thrown = thrownPrev;
		} finally {
			pendingExits = pendingExitsPrev;
			alive = alivePrev;
			nextadr = nextadrPrev;
			firstadr = firstadrPrev;
			//thrown没有保留
			caught = caughtPrev;
			classDef = classDefPrev;
			lint = lintPrev;
			DEBUG.P(0,this,"visitClassDef(JCClassDecl tree)");
		}
    }

    public void visitMethodDef(JCMethodDecl tree) {
		try {//我加上的
		DEBUG.P(this,"visitMethodDef(JCMethodDecl tree)");
		DEBUG.P("tree="+tree);

		if (tree.body == null) return;

		List<Type> caughtPrev = caught;
		List<Type> mthrown = tree.sym.type.getThrownTypes();

		DEBUG.P("caughtPrev="+caughtPrev);
		DEBUG.P("mthrown="+mthrown);

		Bits initsPrev = inits.dup();
		Bits uninitsPrev = uninits.dup();
		int nextadrPrev = nextadr;
		int firstadrPrev = firstadr;
		Lint lintPrev = lint;

		lint = lint.augment(tree.sym.attributes_field);

		assert pendingExits.isEmpty();

		try {
			boolean isInitialConstructor =
			TreeInfo.isInitialConstructor(tree);

			DEBUG.P("isInitialConstructor="+isInitialConstructor);
			DEBUG.P("firstadr="+firstadr);
			DEBUG.P("nextadr="+nextadr);

			if (!isInitialConstructor)
				firstadr = nextadr;

			DEBUG.P("");DEBUG.P("for tree.params......");
			for (List<JCVariableDecl> l = tree.params; l.nonEmpty(); l = l.tail) {
				JCVariableDecl def = l.head;
				DEBUG.P("def="+def);
				scan(def);
				//从下面两条语句看出，
				//只是为了在newVar(VarSymbol sym)给def.sym.adr赋值，并修改nextadr
				inits.incl(def.sym.adr);
				uninits.excl(def.sym.adr);
				
				DEBUG.P("inits  ="+inits);
				DEBUG.P("uninits="+uninits);DEBUG.P("");
			}

			DEBUG.P(2);DEBUG.P("for tree.params......结束");
			DEBUG.P("caught1="+caught);
			
			DEBUG.P("方法:"+tree.name+" isInitialConstructor="+isInitialConstructor);
			DEBUG.P("方法:"+tree.name+" mthrown="+mthrown);
			DEBUG.P("tree.sym.flags()="+Flags.toString(tree.sym.flags()));
			//DEBUG.P("mthrown="+mthrown);

			if (isInitialConstructor)
				caught = chk.union(caught, mthrown);
			//方法或静态初始化块的情形?方法会有BLOCK标记吗？
			else if ((tree.sym.flags() & (BLOCK | STATIC)) != BLOCK)
				caught = mthrown;
			// else we are in an instance initializer block;
			// leave caught unchanged.

			DEBUG.P("caught2="+caught);

			alive = true;
			scanStat(tree.body);
			DEBUG.P("方法体scan结束");
			DEBUG.P("alive="+alive);
			DEBUG.P("ree.sym.type.getReturnType()="+tree.sym.type.getReturnType());
			if (alive && tree.sym.type.getReturnType().tag != VOID)
				log.error(TreeInfo.diagEndPos(tree.body), "missing.ret.stmt");

			/*
			当数据流分析到任意一个第一条语句不是this()调用的构造方法时,
			在分析完此构造方法的方法体时，如果发现final实例字段还有初始
			化，就可以直接报错了，而不管其他构造方法内部是否对它初始化过
			*/
			if (isInitialConstructor) {
				DEBUG.P("firstadr="+firstadr);
				DEBUG.P("nextadr="+nextadr);
				for (int i = firstadr; i < nextadr; i++)
					if (vars[i].owner == classDef.sym)
						checkInit(TreeInfo.diagEndPos(tree.body), vars[i]);
			}


			List<PendingExit> exits = pendingExits.toList();
			pendingExits = new ListBuffer<PendingExit>();
			while (exits.nonEmpty()) {
				PendingExit exit = exits.head;
				exits = exits.tail;

				DEBUG.P("exit.thrown="+exit.thrown);
				/*
				class Test {
					final int a;
					Test(int i) throws NoSuchFieldException, NoSuchMethodException {
						if(i<0) return; //可能尚未初始化变量 a
						if(i>0) throw new NoSuchFieldException();

						a = 10;
					}
				*/
				if (exit.thrown == null) {
					assert exit.tree.tag == JCTree.RETURN;
					if (isInitialConstructor) {
						inits = exit.inits;
						for (int i = firstadr; i < nextadr; i++)
							checkInit(exit.tree.pos(), vars[i]);
					}
				} else {
					// uncaught throws will be reported later
					pendingExits.append(exit);
				}
			}
		} finally {
			inits = initsPrev;
			uninits = uninitsPrev;
			nextadr = nextadrPrev;
			firstadr = firstadrPrev;
			caught = caughtPrev;
			lint = lintPrev;
		}

		}finally{//我加上的
		DEBUG.P(1,this,"visitMethodDef(JCMethodDecl tree)");
		}
    }

    public void visitVarDef(JCVariableDecl tree) {
		DEBUG.P(this,"visitVarDef(1)");
		boolean track = trackable(tree.sym);
		DEBUG.P("track="+track);
		//注意:在JCBlock中定义的变量,tree.sym.owner.kind都为MTH
		DEBUG.P("tree.sym.owner.kind="+Kinds.toString(tree.sym.owner.kind));
		if (track && tree.sym.owner.kind == MTH) newVar(tree.sym);
		DEBUG.P("tree.init="+tree.init);
		
		Bits initsPrev = inits.dup();//我加上的
		Bits uninitsPrev = uninits.dup();//我加上的
		
		if (tree.init != null) {
			Lint lintPrev = lint;
			lint = lint.augment(tree.sym.attributes_field);
			try{
				scanExpr(tree.init);
				if (track) letInit(tree.pos(), tree.sym);
			} finally {
				lint = lintPrev;
			}
		}
		DEBUG.P("inits  前="+initsPrev);
		DEBUG.P("inits  后="+inits);
		//注意下面两个的输出，跟调用letInit的情况有关
		DEBUG.P("uninits前="+uninitsPrev);
		DEBUG.P("uninits后="+uninits);
		DEBUG.P(0,this,"visitVarDef(1)");
    }

	public void visitBlock(JCBlock tree) {
		DEBUG.P(this,"visitBlock(JCBlock tree)");

		//在scan完JCBlock后,nextadr还是还原为原来的nextadr
		//这一点值得注意，因为JCBlock可以看成是一个整体，
		//如果JCBlock中涉及的变量都是正常的，对JCBlock的scan只是过渡性质
		int nextadrPrev = nextadr;
		scanStats(tree.stats);
		
		DEBUG.P("nextadr当前="+nextadr+" nextadr还原后="+nextadrPrev);
		nextadr = nextadrPrev;
		
		DEBUG.P(0,this,"visitBlock(JCBlock tree)");
    }

    public void visitDoLoop(JCDoWhileLoop tree) {
		DEBUG.P(this,"visitDoLoop(1)");
		ListBuffer<PendingExit> prevPendingExits = pendingExits;
		boolean prevLoopPassTwo = loopPassTwo;
        pendingExits = new ListBuffer<PendingExit>();
		do {
			Bits uninitsEntry = uninits.dup();
			myUninitVars(uninitsEntry);

			scanStat(tree.body);
			alive |= resolveContinues(tree);

			DEBUG.P("alive="+alive);

			scanCond(tree.cond);

			DEBUG.P("log.nerrors="+log.nerrors);
			DEBUG.P("loopPassTwo="+loopPassTwo);
			DEBUG.P("uninitsEntry   ="+uninitsEntry);
			DEBUG.P("uninitsWhenTrue="+uninitsWhenTrue);
			DEBUG.P("uninitsEntry.diffSet(uninitsWhenTrue).nextBit(firstadr)="+uninitsEntry.diffSet(uninitsWhenTrue).nextBit(firstadr));

			if (log.nerrors != 0 ||
			loopPassTwo ||
			uninitsEntry.diffSet(uninitsWhenTrue).nextBit(firstadr)==-1)
			break;
			inits = initsWhenTrue;
			uninits = uninitsEntry.andSet(uninitsWhenTrue);

			myUninitVars(inits);
			myUninitVars(uninits);

			loopPassTwo = true;
			alive = true;
		} while (true);
		loopPassTwo = prevLoopPassTwo;
		inits = initsWhenFalse;
		uninits = uninitsWhenFalse;

		myUninitVars(inits);
		myUninitVars(uninits);

		alive = alive && !tree.cond.type.isTrue();

		DEBUG.P("alive="+alive);

		alive |= resolveBreaks(tree, prevPendingExits);

		DEBUG.P("alive="+alive);

		DEBUG.P(0,this,"visitDoLoop(1)");
    }

    public void visitWhileLoop(JCWhileLoop tree) {
		DEBUG.P(this,"visitWhileLoop(1)");
		ListBuffer<PendingExit> prevPendingExits = pendingExits;
		boolean prevLoopPassTwo = loopPassTwo;
		Bits initsCond;
		Bits uninitsCond;
		pendingExits = new ListBuffer<PendingExit>();
		do {
			Bits uninitsEntry = uninits.dup();
			scanCond(tree.cond);
			initsCond = initsWhenFalse;
			uninitsCond = uninitsWhenFalse;
			inits = initsWhenTrue;
			uninits = uninitsWhenTrue;
			//当是while(false)时 无法访问的语句
			alive = !tree.cond.type.isFalse();

			DEBUG.P("tree.cond.type.isFalse()="+tree.cond.type.isFalse());
			DEBUG.P("alive="+alive);

			scanStat(tree.body);
			alive |= resolveContinues(tree);
			if (log.nerrors != 0 ||
			loopPassTwo ||
			uninitsEntry.diffSet(uninits).nextBit(firstadr) == -1)
				break;
			uninits = uninitsEntry.andSet(uninits);
			loopPassTwo = true;
			alive = true;
		} while (true);
		loopPassTwo = prevLoopPassTwo;
		inits = initsCond;
		uninits = uninitsCond;
		
		//true循环中无break语句，那么循环后的其他语句是无法访问的语句
		//如:
		//while(true) {}
		//a++; //无法访问的语句
		alive = resolveBreaks(tree, prevPendingExits) ||
			!tree.cond.type.isTrue();
		DEBUG.P(0,this,"visitWhileLoop(1)");    
    }

    public void visitForLoop(JCForLoop tree) {
		DEBUG.P(this,"visitForLoop(1)");
		ListBuffer<PendingExit> prevPendingExits = pendingExits;
		boolean prevLoopPassTwo = loopPassTwo;
		int nextadrPrev = nextadr;
		scanStats(tree.init);
		Bits initsCond;
		Bits uninitsCond;
		pendingExits = new ListBuffer<PendingExit>();
		do {
			Bits uninitsEntry = uninits.dup();
			if (tree.cond != null) {
				scanCond(tree.cond);
				initsCond = initsWhenFalse;
				uninitsCond = uninitsWhenFalse;
				inits = initsWhenTrue;
				uninits = uninitsWhenTrue;
				alive = !tree.cond.type.isFalse();
			} else {
				initsCond = inits.dup();
				initsCond.inclRange(firstadr, nextadr);
				uninitsCond = uninits.dup();
				uninitsCond.inclRange(firstadr, nextadr);
				alive = true;
			}
			scanStat(tree.body);
			alive |= resolveContinues(tree);
			scan(tree.step);
			if (log.nerrors != 0 ||
				loopPassTwo ||
				uninitsEntry.dup().diffSet(uninits).nextBit(firstadr) == -1)
					break;
			uninits = uninitsEntry.andSet(uninits);
			loopPassTwo = true;
			alive = true;
		} while (true);
		loopPassTwo = prevLoopPassTwo;
		inits = initsCond;
		uninits = uninitsCond;
		/*
		如果for语句的条件表达式(tree.cond)在编译期间就能确定为true值，
		那么紧跟在for语句体后面的语句将无法访问
		例子:
		for(;7<10;) i++;
			ddd++;
		错误提示:
		bin\mysrc\my\test\Test.java:123: 无法访问的语句
					ddd++;
					^
		*/
		alive = resolveBreaks(tree, prevPendingExits) ||
			tree.cond != null && !tree.cond.type.isTrue();
		nextadr = nextadrPrev;
		DEBUG.P(0,this,"visitForLoop(1)");
    }

    public void visitForeachLoop(JCEnhancedForLoop tree) {
		DEBUG.P(this,"visitForeachLoop(1)");
		visitVarDef(tree.var);

		ListBuffer<PendingExit> prevPendingExits = pendingExits;
		boolean prevLoopPassTwo = loopPassTwo;
		int nextadrPrev = nextadr;
		scan(tree.expr);
		
		DEBUG.P("");
		DEBUG.P("inits  ="+inits);
		DEBUG.P("uninits="+uninits);
		Bits initsStart = inits.dup();
		Bits uninitsStart = uninits.dup();

		letInit(tree.pos(), tree.var.sym);
		pendingExits = new ListBuffer<PendingExit>();
		do {
			DEBUG.P("");
			DEBUG.P("inits  ="+inits);
			DEBUG.P("uninits="+uninits);
			Bits uninitsEntry = uninits.dup();
			scanStat(tree.body);
			alive |= resolveContinues(tree);
			
			DEBUG.P("alive="+alive);
			DEBUG.P("log.nerrors="+log.nerrors);
			DEBUG.P("loopPassTwo="+loopPassTwo);
			if (log.nerrors != 0 ||
				loopPassTwo ||
				uninitsEntry.diffSet(uninits).nextBit(firstadr) == -1)
					break;
			uninits = uninitsEntry.andSet(uninits);
			loopPassTwo = true;
			alive = true;
		} while (true);
		loopPassTwo = prevLoopPassTwo;
		inits = initsStart;
		uninits = uninitsStart.andSet(uninits);
		resolveBreaks(tree, prevPendingExits);
		alive = true;
		nextadr = nextadrPrev;
		DEBUG.P(0,this,"visitForeachLoop(1)");
    }

    public void visitLabelled(JCLabeledStatement tree) {
		DEBUG.P(this,"visitLabelled(1)");

		ListBuffer<PendingExit> prevPendingExits = pendingExits;
		pendingExits = new ListBuffer<PendingExit>();
		scanStat(tree.body);
		alive |= resolveBreaks(tree, prevPendingExits);

		DEBUG.P(0,this,"visitLabelled(1)");
    }

    public void visitSwitch(JCSwitch tree) {
		DEBUG.P(this,"visitSwitch(1)");
		ListBuffer<PendingExit> prevPendingExits = pendingExits;
		pendingExits = new ListBuffer<PendingExit>();
		int nextadrPrev = nextadr;
		scanExpr(tree.selector);
		Bits initsSwitch = inits;
		Bits uninitsSwitch = uninits.dup();
		boolean hasDefault = false;
		for (List<JCCase> l = tree.cases; l.nonEmpty(); l = l.tail) {
			alive = true;
			inits = initsSwitch.dup();
			uninits = uninits.andSet(uninitsSwitch);
			JCCase c = l.head;
			if (c.pat == null)
				hasDefault = true;
			else
				scanExpr(c.pat);
			scanStats(c.stats);
			addVars(c.stats, initsSwitch, uninitsSwitch);

			DEBUG.P("loopPassTwo="+loopPassTwo);
			DEBUG.P("alive="+alive);
			DEBUG.P("lint.isEnabled(Lint.LintCategory.FALLTHROUGH)="+lint.isEnabled(Lint.LintCategory.FALLTHROUGH));
			// Warn about fall-through if lint switch fallthrough enabled.
			if (!loopPassTwo &&
			alive &&
			lint.isEnabled(Lint.LintCategory.FALLTHROUGH) &&
			c.stats.nonEmpty() && l.tail.nonEmpty())
				log.warning(l.tail.head.pos(),
					"possible.fall-through.into.case");
		}
		DEBUG.P("hasDefault="+hasDefault);

		if (!hasDefault) {
			inits.andSet(initsSwitch);
			alive = true;
		}
		alive |= resolveBreaks(tree, prevPendingExits);
		nextadr = nextadrPrev;
		DEBUG.P(0,this,"visitSwitch(1)");
    }
    // where
	/** Add any variables defined in stats to inits and uninits. */
	private static void addVars(List<JCStatement> stats, Bits inits,
				    Bits uninits) {
		DEBUG.P(Flow.class,"addVars(3)");		    
	    for (;stats.nonEmpty(); stats = stats.tail) {
			JCTree stat = stats.head;
			if (stat.tag == JCTree.VARDEF) {
				int adr = ((JCVariableDecl) stat).sym.adr;

				DEBUG.P("adr="+adr);

				inits.excl(adr);
				uninits.incl(adr);
			}
	    }
	    DEBUG.P(0,Flow.class,"addVars(3)");	
	}

    public void visitTry(JCTry tree) {
		DEBUG.P(this,"visitTry(1)");
		List<Type> caughtPrev = caught;
		List<Type> thrownPrev = thrown;

		DEBUG.P("caughtPrev="+caughtPrev);
		DEBUG.P("thrownPrev="+thrownPrev);

		thrown = List.nil();
		//如果有多个JCCatch，caught只保存这些JCCatch中处于异常继承树中最高的异常类
		//如有三个JCCatch:
		//catch(RuntimeException e) {}
		//catch(Exception e) {}
		//catch(Error e) {}
		//测:caught=java.lang.Error,java.lang.Exception
		//因为RuntimeException是Exception的子类，所以RuntimeException不用包含在caught中
		for (List<JCCatch> l = tree.catchers; l.nonEmpty(); l = l.tail)
			caught = chk.incl(l.head.param.type, caught);

		DEBUG.P("caught="+caught);

		Bits uninitsTryPrev = uninitsTry;
		ListBuffer<PendingExit> prevPendingExits = pendingExits;
		pendingExits = new ListBuffer<PendingExit>();
		Bits initsTry = inits.dup();
		uninitsTry = uninits.dup();

		DEBUG.P("uninitsTryPrev="+uninitsTryPrev);
		DEBUG.P("initsTry      ="+initsTry);
		DEBUG.P("uninitsTry    ="+uninitsTry);

		scanStat(tree.body);
		List<Type> thrownInTry = thrown;

		DEBUG.P("thrownInTry="+thrownInTry);

		thrown = thrownPrev;
		caught = caughtPrev;

		DEBUG.P("thrown="+thrown);
		DEBUG.P("caught="+caught);

		boolean aliveEnd = alive;
		uninitsTry.andSet(uninits);
		Bits initsEnd = inits;
		Bits uninitsEnd = uninits;
		int nextadrCatch = nextadr;

		List<Type> caughtInTry = List.nil();
		for (List<JCCatch> l = tree.catchers; l.nonEmpty(); l = l.tail) {
			alive = true;
			JCVariableDecl param = l.head.param;
			Type exc = param.type;
			if (chk.subset(exc, caughtInTry)) {
				/*例子:
				catch(Exception e) {}
				catch(NoSuchFieldException e) {}
				 
				错误提示(因为NoSuchFieldException是Exception的子类):
				bin\mysrc\my\test\Test.java:138: 已捕捉到异常 java.lang.NoSuchFieldException
						catch(NoSuchFieldException e) {}
						^
				1 错误
				*/

				log.error(l.head.pos(),
					  "except.already.caught", exc);
			} else if (!chk.isUnchecked(l.head.pos(), exc) &&
				   exc.tsym != syms.throwableType.tsym &&
				   exc.tsym != syms.exceptionType.tsym &&
				   !chk.intersects(exc, thrownInTry)) {
				//如果在try体中没有显示抛出己检查异常(通过throw语句或方法调用或构造函数)，
				//那么在catch中不能捕获己检查异常(Exception和Throwable除外)
				/*例子:
				try {
					i++;
				}
				catch(NoSuchFieldException e) {}
				
				错误提示:
				bin\mysrc\my\test\Test.java:138: 在相应的 try 语句主体中不能抛出异常 java.lang.NoSuchFieldException
						catch(NoSuchFieldException e) {}
						^
				1 错误

				bin\mysrc\my\test\Test.java:138: exception java.lang.NoSuchFieldException is never thrown in body of corresponding try statement
						catch(NoSuchFieldException e) {}
						^
				1 error
				*/
				log.error(l.head.pos(),
					  "except.never.thrown.in.try", exc);
			}
			caughtInTry = chk.incl(exc, caughtInTry);
			inits = initsTry.dup();
			uninits = uninitsTry.dup();
			scan(param);
			inits.incl(param.sym.adr);
			uninits.excl(param.sym.adr);
			scanStat(l.head.body);
			initsEnd.andSet(inits);
			uninitsEnd.andSet(uninits);
			nextadr = nextadrCatch;
			aliveEnd |= alive;
		}
		if (tree.finalizer != null) {
			List<Type> savedThrown = thrown;
			thrown = List.nil();
			inits = initsTry.dup();
			uninits = uninitsTry.dup();
			ListBuffer<PendingExit> exits = pendingExits;
			pendingExits = prevPendingExits;
			alive = true;
			scanStat(tree.finalizer);
			if (!alive) {
				// discard exits and exceptions from try and finally
				thrown = chk.union(thrown, thrownPrev);
				if (!loopPassTwo &&
					lint.isEnabled(Lint.LintCategory.FINALLY)) {
					log.warning(TreeInfo.diagEndPos(tree.finalizer),
						"finally.cannot.complete");
				}
			} else {
				thrown = chk.union(thrown, chk.diff(thrownInTry, caughtInTry));
				thrown = chk.union(thrown, savedThrown);
				uninits.andSet(uninitsEnd);
				// FIX: this doesn't preserve source order of exits in catch
				// versus finally!
				while (exits.nonEmpty()) {
					PendingExit exit = exits.next();
					if (exit.inits != null) {
						exit.inits.orSet(inits);
						exit.uninits.andSet(uninits);
					}
					pendingExits.append(exit);
				}
				inits.orSet(initsEnd);
				alive = aliveEnd;
			}
		} else {
			thrown = chk.union(thrown, chk.diff(thrownInTry, caughtInTry));
			inits = initsEnd;
			uninits = uninitsEnd;
			alive = aliveEnd;
			ListBuffer<PendingExit> exits = pendingExits;
			pendingExits = prevPendingExits;
			while (exits.nonEmpty()) pendingExits.append(exits.next());
		}
		uninitsTry.andSet(uninitsTryPrev).andSet(uninits);
		DEBUG.P(0,this,"visitTry(1)");
    }

    public void visitConditional(JCConditional tree) {
		DEBUG.P(this,"visitConditional(1)");
		scanCond(tree.cond);
		Bits initsBeforeElse = initsWhenFalse;
		Bits uninitsBeforeElse = uninitsWhenFalse;
		inits = initsWhenTrue;
		uninits = uninitsWhenTrue;
		if (tree.truepart.type.tag == BOOLEAN &&
			tree.falsepart.type.tag == BOOLEAN) {
			// if b and c are boolean valued, then
			// v is (un)assigned after a?b:c when true iff
			//    v is (un)assigned after b when true and
			//    v is (un)assigned after c when true
			scanCond(tree.truepart);
			Bits initsAfterThenWhenTrue = initsWhenTrue.dup();
			Bits initsAfterThenWhenFalse = initsWhenFalse.dup();
			Bits uninitsAfterThenWhenTrue = uninitsWhenTrue.dup();
			Bits uninitsAfterThenWhenFalse = uninitsWhenFalse.dup();
			inits = initsBeforeElse;
			uninits = uninitsBeforeElse;
			scanCond(tree.falsepart);
			initsWhenTrue.andSet(initsAfterThenWhenTrue);
			initsWhenFalse.andSet(initsAfterThenWhenFalse);
			uninitsWhenTrue.andSet(uninitsAfterThenWhenTrue);
			uninitsWhenFalse.andSet(uninitsAfterThenWhenFalse);
		} else {
			scanExpr(tree.truepart);
			Bits initsAfterThen = inits.dup();
			Bits uninitsAfterThen = uninits.dup();
			inits = initsBeforeElse;
			uninits = uninitsBeforeElse;
			scanExpr(tree.falsepart);
			inits.andSet(initsAfterThen);
			uninits.andSet(uninitsAfterThen);
		}
		myUninitVars(inits,uninits);
		DEBUG.P(0,this,"visitConditional(1)");
    }
    
    
    /*为什么像下面的语句只报一次错误?
			int iii;
			if(iii>5) iii++;
			else iii--;
			
			bin\mysrc\my\test\Test.java:91: 可能尚未初始化变量 iii
					if(iii>5) iii++;
					   ^
		因为在scanCond(tree.cond)中scan到iii>5时，会转到checkInit(2),
		此时发现inits中没有iii，就报错:可能尚未初始化变量 iii,报完错
		误提示信息后，再把iii加入inits中，
		接着再把inits赋给initsWhenTrue与initsWhenFalse
		
		而	int i=10;
		　　int iii;
			if(i>5) iii++;
			else iii--;
		报了两次错:	
		bin\mysrc\my\test\Test.java:91: 可能尚未初始化变量 iii
					if(i>5) iii++;
							^
		bin\mysrc\my\test\Test.java:92: 可能尚未初始化变量 iii
					else iii--;
						 ^
		是因为:if语句的两个部分(then与else)分别对
		应的是initsWhenTrue与initsWhenFalse，
		而上面的if(iii>5)只单独对应inits，当调用完checkInit(2)后，
		再用inits的当前值赋给initsWhenTrue与initsWhenFalse，而此时
		这两个值都已包含了变量iii。
		
		但是对于if(i>5)来说，在调用完scanCond(tree.cond)后，inits还
		没有包含变量iii，然后就直接赋给initsWhenTrue与initsWhenFalse，
		当调用scanStat(tree.thenpart)与scanStat(tree.elsepart)之前，
		又把initsWhenTrue与initsWhenFalse分别赋给inits，所以在执行
		到checkInit(2)时，inits都没有包含变量iii，从而报两次错误，
		所以这很合理。
	*/
    public void visitIf(JCIf tree) {
		DEBUG.P(this,"visitIf(1)");
		scanCond(tree.cond);
		Bits initsBeforeElse = initsWhenFalse;
		Bits uninitsBeforeElse = uninitsWhenFalse;
		inits = initsWhenTrue;
		uninits = uninitsWhenTrue;
		DEBUG.P("scanStat(tree.thenpart)开始");
		scanStat(tree.thenpart);
		DEBUG.P("scanStat(tree.thenpart)结束");
		if (tree.elsepart != null) {
			DEBUG.P(2);
			DEBUG.P("scanStat(tree.elsepart)开始");
			boolean aliveAfterThen = alive;

			DEBUG.P("aliveAfterThen="+aliveAfterThen);

			alive = true;
			Bits initsAfterThen = inits.dup();
			Bits uninitsAfterThen = uninits.dup();
			inits = initsBeforeElse;
			uninits = uninitsBeforeElse;
			
			scanStat(tree.elsepart);
			inits.andSet(initsAfterThen);
			uninits.andSet(uninitsAfterThen);
			alive = alive | aliveAfterThen;
			DEBUG.P("scanStat(tree.elsepart)结束");
		} else {
			inits.andSet(initsBeforeElse);
			uninits.andSet(uninitsBeforeElse);
			alive = true;
		}
		DEBUG.P("alive="+alive);
		DEBUG.P("inits  ="+inits);
		DEBUG.P("uninits="+uninits);
		myUninitVars(inits,uninits);
		DEBUG.P(0,this,"visitIf(1)");
    }

    public void visitBreak(JCBreak tree) {
		DEBUG.P(this,"visitBreak(1)");
		recordExit(tree);
		DEBUG.P(0,this,"visitBreak(1)");
    }

    public void visitContinue(JCContinue tree) {
		DEBUG.P(this,"visitContinue(1)");
		recordExit(tree);
		DEBUG.P(0,this,"visitContinue(1)");
    }

    public void visitReturn(JCReturn tree) {
		DEBUG.P(this,"visitReturn(1)");
		scanExpr(tree.expr);
		// if not initial constructor, should markDead instead of recordExit
		recordExit(tree);
		DEBUG.P(0,this,"visitReturn(1)");
    }

    public void visitThrow(JCThrow tree) {
		DEBUG.P(this,"visitThrow(1)");
		scanExpr(tree.expr);
		markThrown(tree, tree.expr.type);
		markDead();
		DEBUG.P(0,this,"visitThrow(1)");
    }

    public void visitApply(JCMethodInvocation tree) {
		DEBUG.P(this,"visitApply(1)");
		DEBUG.P("tree.meth="+tree.meth);
		DEBUG.P("tree.args="+tree.args);
		
		scanExpr(tree.meth);
		scanExprs(tree.args);
		for (List<Type> l = tree.meth.type.getThrownTypes(); l.nonEmpty(); l = l.tail)
			markThrown(tree, l.head);
		DEBUG.P(0,this,"visitApply(1)");
    }

    public void visitNewClass(JCNewClass tree) {
		DEBUG.P(this,"visitNewClass(1)");
		DEBUG.P("tree.encl="+tree.encl);
		DEBUG.P("tree.typeargs="+tree.typeargs);
		DEBUG.P("tree.clazz="+tree.clazz);
		DEBUG.P("tree.args="+tree.args);
		DEBUG.P("tree.def="+tree.def);
		DEBUG.P("tree.constructor="+tree.constructor);
		DEBUG.P("tree.varargsElement="+tree.varargsElement);

		scanExpr(tree.encl);
		scanExprs(tree.args);
		   // scan(tree.def);
		for (List<Type> l = tree.constructor.type.getThrownTypes();
			 l.nonEmpty();
			 l = l.tail)
			markThrown(tree, l.head);
		
		scan(tree.def);
		DEBUG.P(0,this,"visitNewClass(1)");
    }

    public void visitNewArray(JCNewArray tree) {
		DEBUG.P(this,"visitNewArray(1)");
		scanExprs(tree.dims);
		scanExprs(tree.elems);
		DEBUG.P(0,this,"visitNewArray(1)");
    }

    public void visitAssert(JCAssert tree) {
		DEBUG.P(this,"visitAssert(1)");
		Bits initsExit = inits.dup();
		Bits uninitsExit = uninits.dup();
		scanCond(tree.cond);
		uninitsExit.andSet(uninitsWhenTrue);
		if (tree.detail != null) {
			inits = initsWhenFalse;
			uninits = uninitsWhenFalse;
			scanExpr(tree.detail);
		}
		inits = initsExit;
		uninits = uninitsExit;
		DEBUG.P(0,this,"visitAssert(1)");
    }

    public void visitAssign(JCAssign tree) {
		DEBUG.P(this,"visitAssign(1)");
		JCTree lhs = TreeInfo.skipParens(tree.lhs);
		if (!(lhs instanceof JCIdent)) scanExpr(lhs);
		scanExpr(tree.rhs);
		letInit(lhs);
		DEBUG.P(0,this,"visitAssign(1)");
    }

    public void visitAssignop(JCAssignOp tree) {
		DEBUG.P(this,"visitAssignop(1)");
		scanExpr(tree.lhs);
		scanExpr(tree.rhs);
		letInit(tree.lhs);
		DEBUG.P(0,this,"visitAssignop(1)");
    }

    public void visitUnary(JCUnary tree) {
		DEBUG.P(this,"visitUnary(1)");
		DEBUG.P("tree.tag="+tree.myTreeTag());
		switch (tree.tag) {
		case JCTree.NOT:
			scanCond(tree.arg);
			Bits t = initsWhenFalse;
			initsWhenFalse = initsWhenTrue;
			initsWhenTrue = t;
			t = uninitsWhenFalse;
			uninitsWhenFalse = uninitsWhenTrue;
			uninitsWhenTrue = t;
			break;
		case JCTree.PREINC: case JCTree.POSTINC:
		case JCTree.PREDEC: case JCTree.POSTDEC:
			scanExpr(tree.arg);
			letInit(tree.arg);
			break;
		default:
			scanExpr(tree.arg);
		}
		DEBUG.P("initsWhenFalse  ="+initsWhenFalse);
		DEBUG.P("uninitsWhenFalse="+uninitsWhenFalse);
		DEBUG.P("initsWhenTrue   ="+initsWhenTrue);
		DEBUG.P("uninitsWhenTrue ="+uninitsWhenTrue);
		DEBUG.P(0,this,"visitUnary(1)");
    }

    public void visitBinary(JCBinary tree) {
		DEBUG.P(this,"visitBinary(1)");
		DEBUG.P("tree.lhs="+tree.lhs);
		DEBUG.P("tree.rhs="+tree.rhs);
		DEBUG.P("tree.tag="+tree.myTreeTag());
		switch (tree.tag) {
		case JCTree.AND:
			scanCond(tree.lhs);
			Bits initsWhenFalseLeft = initsWhenFalse;
			Bits uninitsWhenFalseLeft = uninitsWhenFalse;
			inits = initsWhenTrue;
			uninits = uninitsWhenTrue;
			scanCond(tree.rhs);
			initsWhenFalse.andSet(initsWhenFalseLeft);
			uninitsWhenFalse.andSet(uninitsWhenFalseLeft);
			break;
		case JCTree.OR:
			scanCond(tree.lhs);
			Bits initsWhenTrueLeft = initsWhenTrue;
			Bits uninitsWhenTrueLeft = uninitsWhenTrue;
			inits = initsWhenFalse;
			uninits = uninitsWhenFalse;
			scanCond(tree.rhs);
			initsWhenTrue.andSet(initsWhenTrueLeft);
			uninitsWhenTrue.andSet(uninitsWhenTrueLeft);
			break;
		default:
			scanExpr(tree.lhs);
			scanExpr(tree.rhs);
		}
		DEBUG.P(0,this,"visitBinary(1)");
    }

    public void visitIdent(JCIdent tree) {
		DEBUG.P(this,"visitIdent(1)");
		DEBUG.P("tree.sym.kind="+Kinds.toString(tree.sym.kind));
		
		//这里的JCIdent可能是方法名或者别的东西，所以要判断一下
		if (tree.sym.kind == VAR)
			checkInit(tree.pos(), (VarSymbol)tree.sym);
			
		DEBUG.P(0,this,"visitIdent(1)");    
    }
    
    public void visitTypeCast(JCTypeCast tree) {
		DEBUG.P(this,"visitTypeCast(1)");
        super.visitTypeCast(tree);
		/*例子:
		test\flow\test.java:97: 警告：[转换] 向 int 转换出现冗余
				int i = (int)10;
						^
		1 警告
		*/

		DEBUG.P("tree.expr="+tree.expr);
		DEBUG.P("tree.expr.type="+tree.expr.type);
		DEBUG.P("tree.clazz="+tree.clazz);
		DEBUG.P("tree.clazz.type="+tree.clazz.type);

		if (!tree.type.isErroneous() 
			&& lint.isEnabled(Lint.LintCategory.CAST)
			&& types.isSameType(tree.expr.type, tree.clazz.type)) {
			log.warning(tree.pos(), "redundant.cast", tree.expr.type);
		}
		DEBUG.P(0,this,"visitTypeCast(1)");
    }

    public void visitTopLevel(JCCompilationUnit tree) {
        // Do nothing for TopLevel since each class is visited individually
    }

/**************************************************************************
 * main method
 *************************************************************************/

    /** Perform definite assignment/unassignment analysis on a tree.
     */
    public void analyzeTree(JCTree tree, TreeMaker make) {
		DEBUG.P(5);
		DEBUG.P(this,"analyzeTree(2) 正式开始数据流分析......");
		try {
			this.make = make;
			inits = new Bits();
			uninits = new Bits();
			uninitsTry = new Bits();
			initsWhenTrue = initsWhenFalse =
			uninitsWhenTrue = uninitsWhenFalse = null;
			if (vars == null)
				vars = new VarSymbol[32];
			else
				for (int i=0; i<vars.length; i++)
					vars[i] = null;
			firstadr = 0;
			nextadr = 0;
			pendingExits = new ListBuffer<PendingExit>();
			alive = true;
			this.thrown = this.caught = null;
			this.classDef = null;
			scan(tree);//父类com.sun.tools.javac.tree.TreeScanner的方法
		} finally {
			// note that recursive invocations of this method fail hard
			inits = uninits = uninitsTry = null;
			initsWhenTrue = initsWhenFalse =
			uninitsWhenTrue = uninitsWhenFalse = null;
			if (vars != null)
				for (int i=0; i<vars.length; i++)
					vars[i] = null;
			firstadr = 0;
			nextadr = 0;
			pendingExits = null;
			this.make = null;
			this.thrown = this.caught = null;
			this.classDef = null;
			
			DEBUG.P(5,this,"analyzeTree(2)");
		}
    }
}
