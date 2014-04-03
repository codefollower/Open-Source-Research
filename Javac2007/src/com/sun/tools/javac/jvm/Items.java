/*
 * @(#)Items.java	1.36 07/03/21
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

    /** The base class of all items, which implements default behavior.
     */
    abstract class Item {
        /** The type code of values represented by this item.
		 */
		int typecode;
		
		Item(int typecode) {
			this.typecode = typecode;
		}

		/** Generate code to load this item onto stack.
		 */
		Item load() {
			throw new AssertionError();
		}

		/** Generate code to store top of stack into this item.
		 */
		void store() {
			throw new AssertionError("store unsupported: " + this);
		}

		/** Generate code to invoke method represented by this item.
		 */
		Item invoke() {
			throw new AssertionError(this);
		}

		/** Generate code to use this item twice.
		 */
		void duplicate() {
			DEBUG.P(this,"duplicate()");
			DEBUG.P("Item.duplicate() do nothing");
			DEBUG.P(0,this,"duplicate()");
		}

		/** Generate code to avoid having to use this item.
		 */
		void drop() {
			DEBUG.P(this,"drop()");
			DEBUG.P("Item.drop() do nothing");
			DEBUG.P(0,this,"drop()");
		}

		/** Generate code to stash a copy of top of stack - of typecode toscode -
		 *  under this item.
		 */
		void stash(int toscode) {
			stackItem[toscode].duplicate();
		}

		/** Generate code to turn item into a testable condition.
		 */
		//将此item压入堆栈(stack),返回一个表示ifne(如果栈顶不等于0则跳转)的CondItem
		//只有子类CondItem与ImmediateItem覆盖了这个方法。
		CondItem mkCond() {
			try {//我加上的
			DEBUG.P(this,"mkCond()");
			
			load();
			return makeCondItem(ifne); //ifne在ByteCodes定义

			}finally{//我加上的
			DEBUG.P(0,this,"mkCond()");
			}
		}
		
		/** Generate code to coerce item to given type code.
		 *  @param targetcode    The type code to coerce to.
		 */
		Item coerce(int targetcode) {
			try {//我加上的
			DEBUG.P(this,"coerce(int targetcode)");
			DEBUG.P("Item.coerce(int targetcode)");
			DEBUG.P("typecode="+typecode+" targetcode="+targetcode);
			
			if (typecode == targetcode)
				return this;
			else {
				load();

				int typecode1 = Code.truncate(typecode);
				int targetcode1 = Code.truncate(targetcode);
				if (typecode1 != targetcode1) {
					int offset = targetcode1 > typecode1 ? targetcode1 - 1
					: targetcode1;
					// <editor-fold defaultstate="collapsed">
					/*对应下面的指令之一:
					i2l		= 133,
					i2f		= 134,
					i2d		= 135,
					l2i		= 136,
					l2f		= 137,
					l2d		= 138,
					f2i		= 139,
					f2l		= 140,
					f2d		= 141,
					d2i		= 142,
					d2l		= 143,
					d2f		= 144,
					*/
					/*
					注意上面的指令是以3条为一组的,且与下面的type code相对应
					int INTcode 	= 0,
					LONGcode 	= 1,
					FLOATcode 	= 2,
					DOUBLEcode 	= 3,
					
					举例:将long转成float(也就是l2f = 137这条指令所具有的功能)
					对应程序变量值为:
					typecode=LONGcode=1,
					targetcode=FLOATcode=2
					首先判断得出typecode与targetcode不相等,
					且int typecode1 = Code.truncate(typecode) =LONGcode=1;
					  int targetcode1 = Code.truncate(targetcode)=FLOATcode=2;
					
					因为targetcode1>typecode1 
					所以int offset=targetcode1 - 1=2-1=LONGcode=1;
					
					最后：i2l + typecode1 * 3 + offset = 133 + 1 * 3 + 1=137=l2f
					
					理解关键点是:
					INTcode,LONGcode,FLOATcode,DOUBLEcode的值按1递增，
					且这四种基本类型之间的相互转换都有3条指令，
					指令码(值)也按INT,LONG,FLOAT,DOUBLE的顺序来定，
					这样就很有规律了。
					*/
					// </editor-fold>
					code.emitop0(i2l + typecode1 * 3 + offset);
				}
				/*
				当targetcode是BYTEcode、SHORTcode、CHARcode时,
				targetcode1经过Code.truncate(targetcode)后变为INTcode,
				if (targetcode != targetcode1)就为true
				*/
				if (targetcode != targetcode1) {
					/*对应下面的指令之一:
					int2byte	= 145,
					int2char	= 146,
					int2short	= 147,
					*/
					code.emitop0(int2byte + targetcode - BYTEcode);
				}
				return stackItem[targetcode];
			}
			
			}finally{//我加上的
			DEBUG.P(0,this,"coerce(int targetcode)");
			}
		}

		/** Generate code to coerce item to given type.
		 *  @param targettype    The type to coerce to.
		 */
		Item coerce(Type targettype) {
			return coerce(Code.typecode(targettype));
		}

		/** Return the width of this item on stack as a number of words.
		 */
		int width() {
			return 0;
		}

		public abstract String toString();
    }

    /** An item representing a value on stack.
     */
    class StackItem extends Item {

		StackItem(int typecode) {
			super(typecode);
		}

		Item load() {
			return this;
		}

		void duplicate() {
			DEBUG.P(this,"duplicate()");
			code.emitop0(width() == 2 ? dup2 : dup);
			DEBUG.P(0,this,"duplicate()");
		}

		void drop() {
			DEBUG.P(this,"drop()");
			code.emitop0(width() == 2 ? pop2 : pop);
			DEBUG.P(0,this,"drop()");
		}

		void stash(int toscode) {
			/*对应下面的指令之一(参考<<深入java虚拟机>>P375--P377:
			dup_x1		= 90,//复制1个，弹出2(2=1+1)个
			dup_x2		= 91,//复制1个，弹出3(3=1+2)个

			dup2_x1		= 93,//复制2个，弹出3(3=2+1)个
			dup2_x2		= 94,//复制2个，弹出4(4=2+2)个
			
			//(记忆方式:
			//加号左边的数1表示dup，2表示dup2，
			//加号右边的数就是指令名称x字母旁边的数字)
			*/
			code.emitop0(//toscode不会是VOIDcode
			(width() == 2 ? dup_x2 : dup_x1) + 3 * (Code.width(toscode) - 1));
		}

		int width() {
			//LONGcode与DOUBLEcode占两个字长,VOIDcode不占字长，其他为1个字长。
			//注意字长是相对于堆栈而言的，与java的基本类型所占的bit位长度无关。
			//如果把一个堆栈看成是一个元素类型为Object的数组的话，一个字长就是
			//这个数组中的一个元素。
			return Code.width(typecode);
		}

		public String toString() {
			return "stack(" + typecodeNames[typecode] + ")";
		}
    }

    /** An item representing an indexed expression.
     */
    class IndexedItem extends Item {

		IndexedItem(Type type) {
			super(Code.typecode(type));
		}

		Item load() {
			/*对应下面的指令之一
			iaload		= 46,
			laload		= 47,
			faload		= 48,
			daload		= 49,
			aaload		= 50,
			baload		= 51,
			caload		= 52,
			saload		= 53,
			*/
			code.emitop0(iaload + typecode);
			return stackItem[typecode];
		}

		void store() {
			/*对应下面的指令之一
			iastore		= 79,
			lastore		= 80,
			fastore		= 81,
			dastore		= 82,
			aastore		= 83,
			bastore		= 84,
			castore		= 85,
			sastore		= 86,
			*/
			code.emitop0(iastore + typecode);
		}

		void duplicate() {
			code.emitop0(dup2);
		}

		void drop() {
			code.emitop0(pop2);
		}

		void stash(int toscode) {
			code.emitop0(dup_x2 + 3 * (Code.width(toscode) - 1));
		}

		int width() {
			return 2;
		}

		public String toString() {
			return "indexed(" + ByteCodes.typecodeNames[typecode] + ")";
		}
    }

    /** An item representing `this' or `super'.
     */
    class SelfItem extends Item {

        /** Flag which determines whether this item represents `this' or `super'.
		 */
		boolean isSuper;

		SelfItem(boolean isSuper) {
			super(OBJECTcode);
			this.isSuper = isSuper;
		}

		Item load() {
			DEBUG.P(this,"load()");
			code.emitop0(aload_0);
			DEBUG.P(0,this,"load()");
			return stackItem[typecode];
		}

		public String toString() {
			return isSuper ? "super" : "this";
		}
    }

    /** An item representing a local variable.
     */
    class LocalItem extends Item {

		/** The variable's register.
		 */
		int reg;

		/** The variable's type.
		 */
		Type type;

		LocalItem(Type type, int reg) {
			super(Code.typecode(type));
			assert reg >= 0;
			this.type = type;
			this.reg = reg;
		}

		Item load() {
			try {//我加上的
			DEBUG.P(this,"load()");
			DEBUG.P("reg="+reg+" typecode="+ByteCodes.typecodeNames[typecode]);

			//reg是局部变量的位置，JVM指令中有直接取局部变量位置0到3的指令
			if (reg <= 3)//对应指令iload_0到aload_3之一(每一种类型有四条指令，所以乘以4)
				code.emitop0(iload_0 + Code.truncate(typecode) * 4 + reg);
			else
				code.emitop1w(iload + Code.truncate(typecode), reg);
			return stackItem[typecode];

			}finally{//我加上的
			DEBUG.P(0,this,"load()");
			}
		}

		void store() {
			DEBUG.P(this,"store()");
			DEBUG.P("reg="+reg+" typecode="+ByteCodes.typecodeNames[typecode]);
			if (reg <= 3)//对应指令istore_0到astore_3之一
				code.emitop0(istore_0 + Code.truncate(typecode) * 4 + reg);
			else
				code.emitop1w(istore + Code.truncate(typecode), reg);
			code.setDefined(reg);
			DEBUG.P(0,this,"store()");
		}

		void incr(int x) {
			DEBUG.P(this,"incr(int x)");
			DEBUG.P("x="+x+" typecode="+ByteCodes.typecodeNames[typecode]);

			//typecode与x同为INTcode时，直接iinc
			if (typecode == INTcode && x >= -32768 && x <= 32767) {
				//把常量值x加到索引为reg的局部变量，这个局部变量是int类型
				code.emitop1w(iinc, reg, x);
			} else {
				//把LocalItem压入堆栈，把ImmediateItem(常数x)压入堆栈，
				//相加或相减后，结果类型转换成LocalItem，最后保存到LocalItem
				
				load();//把LocalItem压入堆栈
				if (x >= 0) {
					makeImmediateItem(syms.intType, x).load();//把ImmediateItem(常数x)压入堆栈
					code.emitop0(iadd);//相加
				} else {
					makeImmediateItem(syms.intType, -x).load();//把ImmediateItem(常数-x)压入堆栈
					code.emitop0(isub);//相减
				}		
				makeStackItem(syms.intType).coerce(typecode);//结果类型转换成LocalItem
				store();//保存到LocalItem
			}

			DEBUG.P(0,this,"incr(int x)");
		}

		public String toString() {
			return "localItem(type=" + type + "; reg=" + reg + ")";
		}
    }

    /** An item representing a static variable or method.
     */
    class StaticItem extends Item {
		/** The represented symbol.
		 */
		Symbol member;

		StaticItem(Symbol member) {
			super(Code.typecode(member.erasure(types)));
			this.member = member;
		}

		Item load() {
			//pool.put(member)的返回值为int类型
			code.emitop2(getstatic, pool.put(member));
			return stackItem[typecode];
		}

		void store() {
			code.emitop2(putstatic, pool.put(member));
		}

		Item invoke() {
			try {//我加上的
			DEBUG.P(this,"invoke()");
			
			MethodType mtype = (MethodType)member.erasure(types);
			int argsize = Code.width(mtype.argtypes);//没有用处
			int rescode = Code.typecode(mtype.restype);
			int sdiff = Code.width(rescode) - argsize;//没有用处
			code.emitInvokestatic(pool.put(member), mtype);
			return stackItem[rescode];

			}finally{//我加上的
			DEBUG.P(0,this,"invoke()");
			}
		}

		public String toString() {
			return "static(" + member + ")";
		}
    }

    /** An item representing an instance variable or method.
     */
    class MemberItem extends Item {
		/** The represented symbol.
		 */
		Symbol member;

		/** Flag that determines whether or not access is virtual.
		 */
		boolean nonvirtual;

		MemberItem(Symbol member, boolean nonvirtual) {
			super(Code.typecode(member.erasure(types)));
			this.member = member;
			this.nonvirtual = nonvirtual;
		}

		Item load() {
			code.emitop2(getfield, pool.put(member));
			return stackItem[typecode];
		}

		void store() {
				DEBUG.P(this,"store()");
			DEBUG.P("member="+member);
			code.emitop2(putfield, pool.put(member));
				DEBUG.P(0,this,"store()");
		}
		
		//四条Invoke指令的区别看<<深入java虚拟机>>P404-P409
		//因static字段与方法用StaticItem类表示，所以不在invoke()方法处理范围之内
		Item invoke() {
			DEBUG.P(this,"invoke()");
			DEBUG.P("nonvirtual="+nonvirtual);
			DEBUG.P("member="+member);
			DEBUG.P("member.owner.flags()="+Flags.toString(member.owner.flags()));
			DEBUG.P("");
			DEBUG.P("member.type="+member.type);
			/*
			如果member是一个内部成员类的构造方法，那么在调用externalType方法
			后得到一个新的MethodType，这个MethodType的第一个参数的类型是这个
			内部成员类的owner
			如下源代码:
			---------------------------
			package my.test;
			public class Test {
				class MyInnerClass{
					MyInnerClass(){
						this("str");
					}
					MyInnerClass(String str){}
				}
			}
			---------------------------
			编译器在编译到“this("str");”这条语句时，会执行到这里的invoke()方法
			下面是调试输出结果(样例):

			com.sun.tools.javac.jvm.Items$MemberItem===>invoke()
			-------------------------------------------------------------------------
			nonvirtual=true
			member=MyInnerClass(java.lang.String)
			member.owner.flags()=0

			member.type=Method(java.lang.String)void		//注意这里只有一个参数
			mtype=Method(my.test.Test,java.lang.String)void //注意这里已有两个参数
			com.sun.tools.javac.jvm.Code===>emitInvokespecial(int meth, Type mtype)
			-------------------------------------------------------------------------
			meth=2 mtype=Method(my.test.Test,java.lang.String)void
			com.sun.tools.javac.jvm.Code===>emitop(int op)
			-------------------------------------------------------------------------
			emit@5 stack=3: invokespecial(183)
			com.sun.tools.javac.jvm.Code===>emitop(int op)  END
			-------------------------------------------------------------------------

			com.sun.tools.javac.jvm.Code===>emitInvokespecial(int meth, Type mtype)  END
			-------------------------------------------------------------------------

			com.sun.tools.javac.jvm.Items$MemberItem===>invoke()  END
			-------------------------------------------------------------------------
			*/
			MethodType mtype = (MethodType)member.externalType(types);
			DEBUG.P("mtype="+mtype);

			int rescode = Code.typecode(mtype.restype);
			if ((member.owner.flags() & Flags.INTERFACE) != 0) {
				code.emitInvokeinterface(pool.put(member), mtype);
			} else if (nonvirtual) {
				code.emitInvokespecial(pool.put(member), mtype);
			} else {
				code.emitInvokevirtual(pool.put(member), mtype);
			}
			DEBUG.P(0,this,"invoke()");
			return stackItem[rescode];
		}

		void duplicate() {
			stackItem[OBJECTcode].duplicate();
		}

		void drop() {
			stackItem[OBJECTcode].drop();
		}

		void stash(int toscode) {
			stackItem[OBJECTcode].stash(toscode);
		}

		int width() {
			return 1;
		}

		public String toString() {
			return "member(" + member + (nonvirtual ? " nonvirtual)" : ")");
		}
    }

    /** An item representing a literal.
     */
    class ImmediateItem extends Item {

		/** The literal's value.
		 */
		Object value;

		ImmediateItem(Type type, Object value) {
			super(Code.typecode(type));
			this.value = value;
		}

		private void ldc() {
			int idx = pool.put(value);
			if (typecode == LONGcode || typecode == DOUBLEcode) {
				//将常量池中的long或double类型的项压入堆栈(16位索引)
				code.emitop2(ldc2w, idx);
			} else if (idx <= 255) {
				code.emitop1(ldc1, idx);//将常量池中的项压入堆栈(8位索引)
			} else {
				code.emitop2(ldc2, idx);//将常量池中的项压入堆栈(16位索引)
			}
		}

		Item load() {
			DEBUG.P(this,"load()");
			DEBUG.P("typecode="+ByteCodes.typecodeNames[typecode]);
			switch (typecode) {
				case INTcode: case BYTEcode: case SHORTcode: case CHARcode:
					int ival = ((Number)value).intValue();
					if (-1 <= ival && ival <= 5)
						code.emitop0(iconst_0 + ival);
					else if (Byte.MIN_VALUE <= ival && ival <= Byte.MAX_VALUE)
						code.emitop1(bipush, ival);
					else if (Short.MIN_VALUE <= ival && ival <= Short.MAX_VALUE)
						code.emitop2(sipush, ival);
					else
						ldc();
					break;
				case LONGcode:
					long lval = ((Number)value).longValue();
					if (lval == 0 || lval == 1)
						code.emitop0(lconst_0 + (int)lval);
					else
						ldc();
					break;
				case FLOATcode:
					float fval = ((Number)value).floatValue();
					if (isPosZero(fval) || fval == 1.0 || fval == 2.0)
						code.emitop0(fconst_0 + (int)fval);
					else {
						ldc();
					}
					break;
				case DOUBLEcode:
					double dval = ((Number)value).doubleValue();
					if (isPosZero(dval) || dval == 1.0)
						code.emitop0(dconst_0 + (int)dval);
					else
						ldc();
					break;
				case OBJECTcode:
					ldc();
					break;
				default:
					assert false;
			}
			DEBUG.P(0,this,"load()");
			return stackItem[typecode];
		}
			//where
			/** Return true iff float number is positive 0.
			 */
			/*注意:
			(0.0f==-0.0f)=true
			(1.0f/0.0f)=Infinity
			(1.0f/-0.0f)=-Infinity
			(0.0d==-0.0d)=true
			(1.0d/0.0d)=Infinity
			(1.0d/-0.0d)=-Infinity
			下面两个方法是判断x是否是正的浮点数0
			*/
			private boolean isPosZero(float x) {
				return x == 0.0f && 1.0f / x > 0.0f;
			}
			/** Return true iff double number is positive 0.
			 */
			private boolean isPosZero(double x) {
				return x == 0.0d && 1.0d / x > 0.0d;
			}

		CondItem mkCond() {
			try {//我加上的
			DEBUG.P(this,"mkCond()");
			
			int ival = ((Number)value).intValue();

			DEBUG.P("ival="+ival);

			return makeCondItem(ival != 0 ? goto_ : dontgoto);

			}finally{//我加上的
			DEBUG.P(0,this,"mkCond()");
			}
		}

		Item coerce(int targetcode) {
			try {//我加上的
			DEBUG.P(this,"coerce(int targetcode)");
			DEBUG.P("typecode  ="+ByteCodes.typecodeNames[typecode]);
			DEBUG.P("targetcode="+ByteCodes.typecodeNames[targetcode]);

			if (typecode == targetcode) {
				return this;
			} else {
			switch (targetcode) {
				case INTcode:
					if (Code.truncate(typecode) == INTcode)
						return this;
					else
						return new ImmediateItem(
										syms.intType,
										((Number)value).intValue());
				case LONGcode:
					return new ImmediateItem(
					syms.longType,
								((Number)value).longValue());
				case FLOATcode:
					return new ImmediateItem(
					syms.floatType,
								((Number)value).floatValue());
				case DOUBLEcode:
					return new ImmediateItem(
					syms.doubleType,
					((Number)value).doubleValue());
				case BYTEcode:
					return new ImmediateItem(
					syms.byteType,
								(int)(byte)((Number)value).intValue());
				case CHARcode:
					return new ImmediateItem(
					syms.charType,
								(int)(char)((Number)value).intValue());
				case SHORTcode:
					return new ImmediateItem(
					syms.shortType,
								(int)(short)((Number)value).intValue());
				default:
					return super.coerce(targetcode);
				}
			}

			}finally{//我加上的
			DEBUG.P(0,this,"coerce(int targetcode)");
			}
		}

		public String toString() {
			return "immediate(" + value + ")";
		}
    }

    /** An item representing an assignment expressions.
     */
    class AssignItem extends Item {

		/** The item representing the assignment's left hand side.
		 */
		Item lhs;

		AssignItem(Item lhs) {
			super(lhs.typecode);
			this.lhs = lhs;
		}

		Item load() {
			lhs.stash(typecode);
			lhs.store();
			return stackItem[typecode];
		}

		void duplicate() {
			load().duplicate();
		}

		void drop() {
			DEBUG.P(this,"drop()");
			lhs.store();//先出栈再存放到Item lhs
			DEBUG.P(0,this,"drop()");
		}

		void stash(int toscode) {
			assert false;
		}

		int width() {
			return lhs.width() + Code.width(typecode);
		}

		public String toString() {
			return "assign(lhs = " + lhs + ")";
		}
    }

    /** An item representing a conditional or unconditional jump.
     */
    class CondItem extends Item {

		/** A chain encomassing all jumps that can be taken
		 *  if the condition evaluates to true.
		 */
		Chain trueJumps;

		/** A chain encomassing all jumps that can be taken
		 *  if the condition evaluates to false.
		 */
		Chain falseJumps;

		/** The jump's opcode.
		 */
		int opcode;

		/*
		 *  An abstract syntax tree of this item. It is needed
		 *  for branch entries in 'CharacterRangeTable' attribute.
		 */
		JCTree tree;

		CondItem(int opcode, Chain truejumps, Chain falsejumps) {
			super(BYTEcode);

			DEBUG.P(this,"CondItem(3)");
			DEBUG.P("opcode="+Code.mnem(opcode));
			DEBUG.P("truejumps ="+truejumps);
			DEBUG.P("falsejumps="+falsejumps);

			this.opcode = opcode;
			this.trueJumps = truejumps;
			this.falseJumps = falsejumps;

			DEBUG.P(0,this,"CondItem(3)");
		}

		Item load() {
			try {//我加上的
			DEBUG.P(this,"load()");

			Chain trueChain = null;
			Chain falseChain = jumpFalse();

			DEBUG.P("isFalse()="+isFalse());
			if (!isFalse()) {
				code.resolve(trueJumps);
				code.emitop0(iconst_1);
				trueChain = code.branch(goto_);
			}

			DEBUG.P("falseChain="+falseChain);
			if (falseChain != null) {
				code.resolve(falseChain);
				code.emitop0(iconst_0);
			}
			code.resolve(trueChain);
			return stackItem[typecode];

			}finally{//我加上的
			DEBUG.P(0,this,"load()");
			}
		}

		void duplicate() {
			load().duplicate();
		}

		void drop() {
			load().drop();
		}

		void stash(int toscode) {
			assert false;
		}

		CondItem mkCond() {
			return this;
		}

		Chain jumpTrue() {
			try {//我加上的
			DEBUG.P(this,"jumpTrue()");
			DEBUG.P("tree="+tree);

			if (tree == null) return code.mergeChains(trueJumps, code.branch(opcode));
			// we should proceed further in -Xjcov mode only
			int startpc = code.curPc();
			Chain c = code.mergeChains(trueJumps, code.branch(opcode));
			code.crt.put(tree, CRTable.CRT_BRANCH_TRUE, startpc, code.curPc());
			return c;

			}finally{//我加上的
			DEBUG.P(0,this,"jumpTrue()");
			}
		}

		Chain jumpFalse() {
			try {//我加上的
			DEBUG.P(this,"jumpFalse()");
			DEBUG.P("tree="+tree);

			if (tree == null) return code.mergeChains(falseJumps, code.branch(code.negate(opcode)));
			// we should proceed further in -Xjcov mode only
			int startpc = code.curPc();
			Chain c = code.mergeChains(falseJumps, code.branch(code.negate(opcode)));
			code.crt.put(tree, CRTable.CRT_BRANCH_FALSE, startpc, code.curPc());
			return c;

			}finally{//我加上的
			DEBUG.P(0,this,"jumpFalse()");
			}
		}

		CondItem negate() {
			try {//我加上的
			DEBUG.P(this,"negate()");

			CondItem c = new CondItem(code.negate(opcode), falseJumps, trueJumps);
			c.tree = tree;
			return c;

			}finally{//我加上的
			DEBUG.P(0,this,"negate()");
			}
		}

		int width() {
			// a CondItem doesn't have a size on the stack per se.
			throw new AssertionError();
		}

		boolean isTrue() {
			return falseJumps == null && opcode == goto_;
		}

		boolean isFalse() {
			return trueJumps == null && opcode == dontgoto;
		}

		public String toString() {
			//return "cond(" + Code.mnem(opcode) + ")";

			//我加上的
			return "CondItem(" + Code.mnem(opcode) + "[trueJumps="+trueJumps+", falseJumps="+falseJumps+", tree="+tree+"])";
		}
    }
}
