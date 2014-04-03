package com.sun.tools.javac.jvm;

import com.sun.tools.javac.util.*;
import com.sun.tools.javac.code.*;

import com.sun.tools.javac.code.Symbol.*;
import com.sun.tools.javac.code.Type.*;
import com.sun.tools.javac.jvm.Code.*;
import com.sun.tools.javac.tree.JCTree;

import static com.sun.tools.javac.code.TypeTags.*;
import static com.sun.tools.javac.jvm.ByteCodes.*;

/** A helper class for code generation. Items are objects
 *  that stand for addressable entities in the bytecode. Each item
 *  supports a fixed protocol for loading the item on the stack, storing
 *  into it, converting it into a jump condition, and several others.
 *  There are many individual forms of items, such as local, static,
 *  indexed, or instance variables, values on the top of stack, the
 *  special values this or super, etc. Individual items are represented as
 *  inner classes in class Items.
 *
 *  <p><b>This is NOT part of any API supported by Sun Microsystems.  If
 *  you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 */
@Version("@(#)Items.java	1.36 07/03/21")
public class Items {
    private static my.Debug DEBUG=new my.Debug(my.Debug.Items);//我加上的

    /** The current constant pool.
     */
    Pool pool;

    /** The current code buffer.
     */
    Code code;

    /** The current symbol table.
     */
    Symtab syms;

    /** Type utilities. */
    Types types;

    /** Items that exist only once (flyweight pattern).
     */
    private final Item voidItem;
    private final Item thisItem;
    private final Item superItem;
    private final Item[] stackItem = new Item[TypeCodeCount]; //TypeCodeCount在ByteCodes中定义

    public Items(Pool pool, Code code, Symtab syms, Types types) {
		this.code = code;
		this.pool = pool;
		this.types = types;
		voidItem = new Item(VOIDcode) {
			public String toString() { return "void"; }
		};
		thisItem = new SelfItem(false);
		superItem = new SelfItem(true);
		for (int i = 0; i < VOIDcode; i++) stackItem[i] = new StackItem(i);
		stackItem[VOIDcode] = voidItem;
		this.syms = syms;
    }

    /** Make a void item
     */
    Item makeVoidItem() {
		return voidItem;
    }
    /** Make an item representing `this'.
     */
    Item makeThisItem() {
		return thisItem;
    }

    /** Make an item representing `super'.
     */
    Item makeSuperItem() {
		return superItem;
    }

    /** Make an item representing a value on stack.
     *  @param type    The value's type.
     */
    Item makeStackItem(Type type) {
		return stackItem[Code.typecode(type)];
    }

    /** Make an item representing an indexed expression.
     *  @param type    The expression's type.
     */
    Item makeIndexedItem(Type type) {
		try {//我加上的
		DEBUG.P(this,"makeIndexedItem(1)");
		DEBUG.P("type="+type);

		return new IndexedItem(type);

		}finally{//我加上的
		DEBUG.P(0,this,"makeIndexedItem(1)");
		}
    }
	
    /** Make an item representing a local variable.
     *  @param v    The represented variable.
     */
    LocalItem makeLocalItem(VarSymbol v) {
		try {//我加上的
		DEBUG.P(this,"makeLocalItem(VarSymbol v)");
		DEBUG.P("v="+v+" v.adr="+v.adr);

		return new LocalItem(v.erasure(types), v.adr);
		
		}finally{//我加上的
		DEBUG.P(0,this,"makeLocalItem(VarSymbol v)");
		}
    }

    /** Make an item representing a local anonymous variable.
     *  @param type  The represented variable's type.
     *  @param reg   The represented variable's register.
     */
	//这个方法没有使用
    private LocalItem makeLocalItem(Type type, int reg) {
		return new LocalItem(type, reg);
    }

    /** Make an item representing a static variable or method.
     *  @param member   The represented symbol.
     */
    Item makeStaticItem(Symbol member) {
		try {//我加上的
		DEBUG.P(this,"makeStaticItem(1)");
		DEBUG.P("member="+member);

		return new StaticItem(member);

		}finally{//我加上的
		DEBUG.P(0,this,"makeStaticItem(1)");
		}
    }

    /** Make an item representing an instance variable or method.
     *  @param member       The represented symbol.
     *  @param nonvirtual   Is the reference not virtual? (true for constructors
     *                      and private members).
     */
    Item makeMemberItem(Symbol member, boolean nonvirtual) {
		try {//我加上的
		DEBUG.P(this,"makeMemberItem(2)");
		DEBUG.P("nonvirtual="+nonvirtual+"  member="+member);

		return new MemberItem(member, nonvirtual);

		}finally{//我加上的
		DEBUG.P(0,this,"makeMemberItem(2)");
		}
    }

    /** Make an item representing a literal.
     *  @param type	The literal's type.
     *  @param value	The literal's value.
     */
    Item makeImmediateItem(Type type, Object value) {
		try {//我加上的
		DEBUG.P(this,"makeImmediateItem(2)");
		DEBUG.P("type="+type+" value="+value);

		return new ImmediateItem(type, value);
		
		}finally{//我加上的
		DEBUG.P(0,this,"makeImmediateItem(2)");
		}
    }

    /** Make an item representing an assignment expression.
     *  @param lhs      The item representing the assignment's left hand side.
     */
    Item makeAssignItem(Item lhs) {
		try {//我加上的
		DEBUG.P(this,"makeAssignItem(1)");
		DEBUG.P("Item lhs="+lhs);

		return new AssignItem(lhs);

		}finally{//我加上的
		DEBUG.P(0,this,"makeAssignItem(1)");
		}
    }

    /** Make an item representing a conditional or unconditional jump.
     *  @param opcode      The jump's opcode.
     *  @param trueJumps   A chain encomassing all jumps that can be taken
     *                     if the condition evaluates to true.
     *  @param falseJumps  A chain encomassing all jumps that can be taken
     *                     if the condition evaluates to false.
     */
    CondItem makeCondItem(int opcode, Chain trueJumps, Chain falseJumps) {
		try {//我加上的
		DEBUG.P(this,"makeCondItem(3)");

		return new CondItem(opcode, trueJumps, falseJumps);
		
		}finally{//我加上的
		DEBUG.P(0,this,"makeCondItem(3)");
		}
    }

    /** Make an item representing a conditional or unconditional jump.
     *  @param opcode      The jump's opcode.
     */
    CondItem makeCondItem(int opcode) {
		try {//我加上的
		DEBUG.P(this,"makeCondItem(1)");

		return makeCondItem(opcode, null, null);

		}finally{//我加上的
		DEBUG.P(0,this,"makeCondItem(1)");
		}
    }
