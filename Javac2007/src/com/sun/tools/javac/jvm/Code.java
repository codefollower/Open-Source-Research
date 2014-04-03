/*
 * @(#)Code.java	1.64 07/03/21
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

import com.sun.tools.javac.code.*;
import com.sun.tools.javac.code.Symbol.*;
import com.sun.tools.javac.util.*;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;

import static com.sun.tools.javac.code.TypeTags.*;
import static com.sun.tools.javac.jvm.ByteCodes.*;
import static com.sun.tools.javac.jvm.UninitializedType.*;
import static com.sun.tools.javac.jvm.ClassWriter.StackMapTableFrame;

/** An internal structure that corresponds to the code attribute of
 *  methods in a classfile. The class also provides some utility operations to
 *  generate bytecode instructions.
 *
 *  <p><b>This is NOT part of any API supported by Sun Microsystems.  If
 *  you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 */
@Version("@(#)Code.java	1.64 07/03/21")
public class Code {
	private static my.Debug DEBUG=new my.Debug(my.Debug.Code);//我加上的
	
    public final boolean debugCode;
    public final boolean needStackMap;
    
    public enum StackMapFormat {
		NONE,
		CLDC {
			Name getAttributeName(Name.Table names) {
				return names.StackMap;
			} 
		},
		JSR202 {
			Name getAttributeName(Name.Table names) {
				return names.StackMapTable;
			}
		};
		Name getAttributeName(Name.Table names) {
				return names.empty;
		}
    }

    final Types types;
    final Symtab syms;

/*---------- classfile fields: --------------- */

    /** The maximum stack size.
     */
    public int max_stack = 0;

    /** The maximum number of local variable slots.
     */
    //不同作用域的局部变量可以重用局部变量数组的索引号(nextreg)，
    //但max_locals的值是不会减少的，
    //它用于跟踪某一时刻局部变量数组中的总局部变量个数，
    //如果在新加入一个局部变量后，总个数比上次大，max_locals修改成
    //当前nextreg的值，否则维持原来的值不变。参考:newLocal方法
    public int max_locals = 0;

    /** The code buffer.
     */
    public byte[] code = new byte[64];//存放所要产生的方法的字节吗，数组长度会不断扩大

    /** the current code pointer.
     */
    public int cp = 0;

    /** Check the code against VM spec limits; if
     *  problems report them and return true.
     */
    public boolean checkLimits(DiagnosticPosition pos, Log log) {
		if (cp > ClassFile.MAX_CODE) {
			log.error(pos, "limit.code");
			return true;
		}
		if (max_locals > ClassFile.MAX_LOCALS) {
			log.error(pos, "limit.locals");
			return true;
		}
		if (max_stack > ClassFile.MAX_STACK) {
			log.error(pos, "limit.stack");
			return true;
		}
		return false;
    }

    /** A buffer for expression catch data. Each enter is a vector
     *  of four unsigned shorts.
     */
    ListBuffer<char[]> catchInfo = new ListBuffer<char[]>();

    /** A buffer for line number information. Each entry is a vector
     *  of two unsigned shorts.
     */
    List<char[]> lineInfo = List.nil(); // handled in stack fashion

    /** The CharacterRangeTable
     */
    public CRTable crt;

/*---------- internal fields: --------------- */

    /** Are we generating code with jumps >= 32K?
     */
    public boolean fatcode;

    /** Code generation enabled?
     */
    private boolean alive = true;

    /** The current machine state (registers and stack).
     */
    State state;

    /** Is it forbidden to compactify code, because something is
     *  pointing to current location?
     */
    private boolean fixedPc = false;

    /** The next available register.
     */
    public int nextreg = 0;

    /** A chain for jumps to be resolved before the next opcode is emitted.
     *  We do this lazily to avoid jumps to jumps.
     */
    Chain pendingJumps = null;

    /** The position of the currently statement, if we are at the
     *  start of this statement, NOPOS otherwise.
     *  We need this to emit line numbers lazily, which we need to do
     *  because of jump-to-jump optimization.
     */
    int pendingStatPos = Position.NOPOS;

    /** Set true when a stackMap is needed at the current PC. */
    boolean pendingStackMap = false;
    
    /** The stack map format to be generated. */
    StackMapFormat stackMap;
    
    /** Switch: emit variable debug info.
     */
    boolean varDebugInfo;

    /** Switch: emit line number info.
     */
    boolean lineDebugInfo;
    
    /** Emit line number info if map supplied
     */
    Position.LineMap lineMap;

    /** The constant pool of the current class.
     */
    final Pool pool;

    final MethodSymbol meth;

    /** Construct a code object, given the settings of the fatcode,
     *  debugging info switches and the CharacterRangeTable.
     */
    public Code(MethodSymbol meth,
		boolean fatcode,
		Position.LineMap lineMap,
		boolean varDebugInfo,
		StackMapFormat stackMap,
		boolean debugCode,
		CRTable crt,
		Symtab syms,
		Types types,
		Pool pool) {
		DEBUG.P(this,"Code(10)");

		this.meth = meth;
		this.fatcode = fatcode;
		this.lineMap = lineMap;
		this.lineDebugInfo = lineMap != null;
		this.varDebugInfo = varDebugInfo;
		this.crt = crt;
		this.syms = syms;
		this.types = types;
		this.debugCode = debugCode;
		this.stackMap = stackMap;
		switch (stackMap) {
		case CLDC:
		case JSR202:
			this.needStackMap = true;
			break;
		default:
			this.needStackMap = false;
		}
		state = new State();
		lvar = new LocalVar[20];
		this.pool = pool;
		
		DEBUG.P("meth="+meth);
		DEBUG.P("fatcode="+fatcode);
		DEBUG.P("lineDebugInfo="+lineDebugInfo);
		DEBUG.P("varDebugInfo="+varDebugInfo);
		DEBUG.P("stackMap="+stackMap);
		DEBUG.P("needStackMap="+needStackMap);
		DEBUG.P(0,this,"Code(10)");
    }


/* **************************************************************************
 * Typecodes & related stuff
 ****************************************************************************/

    /** Given a type, return its type code (used implicitly in the
     *  JVM architecture).
     */
    public static int typecode(Type type) {
        switch (type.tag) {
			case BYTE: return BYTEcode;
			case SHORT: return SHORTcode;
			case CHAR: return CHARcode;
			case INT: return INTcode;
			case LONG: return LONGcode;
			case FLOAT: return FLOATcode;
			case DOUBLE: return DOUBLEcode;
			case BOOLEAN: return BYTEcode;//boolean当成byte看待
			case VOID: return VOIDcode;
			case CLASS:
			case ARRAY:
			case METHOD:
			case BOT:
			case TYPEVAR:
			case UNINITIALIZED_THIS:
			case UNINITIALIZED_OBJECT:
				return OBJECTcode;
			default: throw new AssertionError("typecode " + type.tag);
        }
    }

    /** Collapse type code for subtypes of int to INTcode.
     */
    public static int truncate(int tc) {
        switch (tc) {
			case BYTEcode: case SHORTcode: case CHARcode: return INTcode;
			default: return tc;
        }
    }

    /** The width in bytes of objects of the type.
     */
    public static int width(int typecode) {
        switch (typecode) {
			case LONGcode: case DOUBLEcode: return 2;
			case VOIDcode: return 0;
			default: return 1;
        }
    }

    public static int width(Type type) {
		return type == null ? 1 : width(typecode(type));
    }

    /** The total width taken up by a vector of objects.
     */
    public static int width(List<Type> types) {
        int w = 0;
        for (List<Type> l = types; l.nonEmpty(); l = l.tail)
			w = w + width(l.head);
        return w;
    }

    /** Given a type, return its code for allocating arrays of that type.
     */
    //arraycode方法中定义的数字的原由是什么??????我不知道。。。。。。
    //在Gen类的makeNewArray方法中有应用，在<<深入JAVA虚拟机>>第430页也有说明
    public static int arraycode(Type type) {
		switch (type.tag) {
			case BYTE: return 8;
			case BOOLEAN: return 4;
			case SHORT: return 9;
			case CHAR: return 5;
			case INT: return 10;
			case LONG: return 11;
			case FLOAT: return 6;
			case DOUBLE: return 7;
			case CLASS: return 0;
			case ARRAY: return 1;
			default: throw new AssertionError("arraycode " + type);
		}
    }


/* **************************************************************************
 * Emit code
 ****************************************************************************/

    /** The current output code pointer.
     */
    public int curPc() {
		//DEBUG.P(this,"curPc()");

		if (pendingJumps != null) resolvePending();
		if (pendingStatPos != Position.NOPOS) markStatBegin();
		
		fixedPc = true;

		//DEBUG.P("cp="+cp);
		//DEBUG.P(0,this,"curPc()");
		return cp;
    }

    /** Emit a byte of code.
     */
    private  void emit1(int od) {
        if (!alive) return;
		if (cp == code.length) {
			byte[] newcode = new byte[cp * 2];
			//假设code.length=100，对应code数组索引号从0到99，
			//因cp从0开始计数，当code数组索引号从0到99都己有数据时，
			//cp的值也变成了100，所以arraycopy要copy 100个数据到newcode
			System.arraycopy(code, 0, newcode, 0, cp);
			code = newcode;
		}
		code[cp++] = (byte)od;
    }

    /** Emit two bytes of code.
     */
    private void emit2(int od) {
        if (!alive) return;
		/*
		int od是4字节的(也就是一个int占32 bit)，但emit2(int od)完
		成的功能是要在code数组中放入两个字节(2 byte=16 bit)，且这两个
		字节是int od的低16位，code数组是一个字节数组，所以得分两次把这
		两个字节放入code数组，首先按15--8bit位构成一字节，下面代码
		中的“(byte)(od >> 8)”把od向右移动8位，这相当于把原来的15--8bit位
		变成7--0bit位，最后把int数值强制转换成byte时，默认取int数值的低8位，
		这低8位也就是原来的15--8bit位。执行完“(byte)(od >> 8)”后也就把第一个
		高位字节加入了code数组中，但此时od的值没变，接着(byte)od就是取低8位，也
		就是第二个低位字节
		*/
		if (cp + 2 > code.length) {//这里不用>=，因为emit1方法中已有==
			
			emit1(od >> 8);//高位在前(也就是高8位在code数组中的下标比低8位小)
			emit1(od);
		} else {
			code[cp++] = (byte)(od >> 8);
			code[cp++] = (byte)od;
		}
    }

    /** Emit four bytes of code.
     */
    public void emit4(int od) {
        if (!alive) return;
		//参考上面emit2(int od)的注释，只不过这里的唯一差别是四个字节
		if (cp + 4 > code.length) {
			emit1(od >> 24);
			emit1(od >> 16);
			emit1(od >> 8);
			emit1(od);
		} else {
			code[cp++] = (byte)(od >> 24);
			code[cp++] = (byte)(od >> 16);
			code[cp++] = (byte)(od >> 8);
			code[cp++] = (byte)od;
		}
    }

    /** Emit an opcode.
     */
    private void emitop(int op) {
		DEBUG.P(this,"emitop(int op)");
		DEBUG.P("alive="+alive+"  pendingJumps="+pendingJumps);
		
		if (pendingJumps != null) resolvePending();
		if (alive) {
			DEBUG.P("pendingStatPos="+pendingStatPos);
			if (pendingStatPos != Position.NOPOS)
				markStatBegin();

			DEBUG.P("pendingStackMap="+pendingStackMap);
			if (pendingStackMap) {
				pendingStackMap = false;
				emitStackMap(); 
			}

			DEBUG.P("emit@cp=" + cp + " stack=" +
					state.stacksize + ": " +
					mnem(op)+"("+op+")");

			if (debugCode)
				System.err.println("emit@" + cp + " stack=" +
								   state.stacksize + ": " +
								   mnem(op));
			emit1(op);
		}
		
		DEBUG.P(0,this,"emitop(int op)");
    }

    void postop() {
		assert alive || state.stacksize == 0;
    }

    /** Emit a multinewarray instruction.
     */
    public void emitMultianewarray(int ndims, int type, Type arrayType) {
		try {//我加上的
		DEBUG.P(this,"emitMultianewarray(3)");
		DEBUG.P("ndims="+ndims);
		DEBUG.P("type="+type);
		DEBUG.P("arrayType="+arrayType);

		emitop(multianewarray);
        if (!alive) return;
		emit2(type);//无符号16位常量池索引(int type这个参数的命名让人费解，也许是用type表示常量池中存放的数组元素类型)
		emit1(ndims);//数组维数
		state.pop(ndims);//从堆栈弹出ndims个字长，每个字长的值代表数组每一维的宽度
		state.push(arrayType);//将arrayType压入堆栈

		}finally{//我加上的
		DEBUG.P(0,this,"emitMultianewarray(3)");
		}
    }

    /** Emit newarray.
     */
    public void emitNewarray(int elemcode, Type arrayType) {
		try {//我加上的
		DEBUG.P(this,"emitNewarray(2)");
		DEBUG.P("elemcode="+elemcode);
		DEBUG.P("arrayType="+arrayType);

		emitop(newarray);
		if (!alive) return;
		emit1(elemcode);//数组元素类型(对应arraycode方法的返回值)
		state.pop(1); // count 数组元素个数
		state.push(arrayType);

		}finally{//我加上的
		DEBUG.P(0,this,"emitNewarray(2)");
		}
    }

    /** Emit anewarray.
     */
    //分配一个数组元素类型为引用类型的数组
    public void emitAnewarray(int od, Type arrayType) {
		try {//我加上的
		DEBUG.P(this,"emitAnewarray(2)");
		DEBUG.P("od="+od);
		DEBUG.P("arrayType="+arrayType);

        emitop(anewarray);
		if (!alive) return;
		emit2(od);//无符号16位常量池索引
		state.pop(1);
		state.push(arrayType);

		}finally{//我加上的
		DEBUG.P(0,this,"emitAnewarray(2)");
		}
    }

    /** Emit an invokeinterface instruction.
     */
    public void emitInvokeinterface(int meth, Type mtype) {
		try {//我加上的
		DEBUG.P(this,"emitInvokeinterface(int meth, Type mtype)");
		DEBUG.P("meth="+meth+" mtype="+mtype);
		
		int argsize = width(mtype.getParameterTypes());
		emitop(invokeinterface);
        if (!alive) return;
		emit2(meth);//无符号16位常量池索引
		emit1(argsize + 1);//参数(包括this)字长总数
		emit1(0);//0是invokeinterface指令的占位符，固定不变
		state.pop(argsize + 1);//这里加1与上面不同，这里是因为要弹出对象引用而加1
		
		//<<深入JAVA虚拟机>>第404-409页有区别，这里还要push返回值,而书上的堆栈是空的
		state.push(mtype.getReturnType());
		
		}finally{//我加上的
		DEBUG.P(0,this,"emitInvokeinterface(int meth, Type mtype)");
		}
    }

    /** Emit an invokespecial instruction.
     */
    //invokespecial指令格式是“invokespecial 16位常量池索引”
    public void emitInvokespecial(int meth, Type mtype) {
		try {//我加上的
		DEBUG.P(this,"emitInvokespecial(int meth, Type mtype)");
		DEBUG.P("meth="+meth+" mtype="+mtype);
		
		int argsize = width(mtype.getParameterTypes());
		emitop(invokespecial);//对应invokespecial字节码
        if (!alive) return;
		emit2(meth);//对应16位常量池索引字节码
		Symbol sym = (Symbol)pool.pool[meth];
		state.pop(argsize);
		if (sym.isConstructor())
			state.markInitialized((UninitializedType)state.peek());
		state.pop(1);
		state.push(mtype.getReturnType());
		
		}finally{//我加上的
		DEBUG.P(0,this,"emitInvokespecial(int meth, Type mtype)");
		}
    }

    /** Emit an invokestatic instruction.
     */
    public void emitInvokestatic(int meth, Type mtype) {
		try {//我加上的
		DEBUG.P(this,"emitInvokestatic(int meth, Type mtype)");
		DEBUG.P("meth="+meth+" mtype="+mtype);
		
		int argsize = width(mtype.getParameterTypes());
		emitop(invokestatic);
        if (!alive) return;
		emit2(meth);
		state.pop(argsize);
		state.push(mtype.getReturnType());
		
		}finally{//我加上的
		DEBUG.P(0,this,"emitInvokestatic(int meth, Type mtype)");
		}
    }

    /** Emit an invokevirtual instruction.
     */
    public void emitInvokevirtual(int meth, Type mtype) {
		try {//我加上的
		DEBUG.P(this,"emitInvokevirtual(int meth, Type mtype)");
		DEBUG.P("meth="+meth+" mtype="+mtype);
		
		int argsize = width(mtype.getParameterTypes());
		emitop(invokevirtual);
		if (!alive) return;
		emit2(meth);
		state.pop(argsize + 1);
		state.push(mtype.getReturnType());
		
		}finally{//我加上的
		DEBUG.P(0,this,"emitInvokevirtual(int meth, Type mtype)");
		}
    }

    /** Emit an opcode with no operand field.
     */
    public void emitop0(int op) {//加入不带操作数的虚拟机指令
		try {//我加上的
		DEBUG.P(this,"emitop0(int op)");
		//DEBUG.P("op="+op+" mnem="+mnem(op));
		
		emitop(op);
		if (!alive) return;
		switch (op) {
			case aaload: {
				//以下四条语句可以看成是JVM执行aaload指令的过程(以下所有指令都类似)
				
				//从栈中弹出数组索引
				state.pop(1);// index
				//先保存栈顶的type
				Type a = state.stack[state.stacksize-1];
				//从栈中弹出数组引用(type)
				state.pop(1);  
				//由数组引用与数组索引得出此索引位置的value(一般是指向某一type的引用),再压入栈
				state.push(types.erasure(types.elemtype(a))); }
				break;
			case goto_:
				markDead();
				break;
			case nop:
			case ineg:
			case lneg:
			case fneg:
			case dneg:
				break;
			case aconst_null:
				state.push(syms.botType);
				break;
			case iconst_m1:
			case iconst_0:
			case iconst_1:
			case iconst_2:
			case iconst_3:
			case iconst_4:
			case iconst_5:
			case iload_0:
			case iload_1:
			case iload_2:
			case iload_3:
				state.push(syms.intType);
				break;
			case lconst_0:
			case lconst_1:
			case lload_0:
			case lload_1:
			case lload_2:
			case lload_3:
				state.push(syms.longType);
				break;
			case fconst_0:
			case fconst_1:
			case fconst_2:
			case fload_0:
			case fload_1:
			case fload_2:
			case fload_3:
				state.push(syms.floatType);
				break;
			case dconst_0:
			case dconst_1:
			case dload_0:
			case dload_1:
			case dload_2:
			case dload_3:
				state.push(syms.doubleType);
				break;
			case aload_0:
				state.push(lvar[0].sym.type);//从局部变量数组索引0处加载引用类型
				break;
			case aload_1:
				state.push(lvar[1].sym.type);
				break;
			case aload_2:
				state.push(lvar[2].sym.type);
				break;
			case aload_3:
				state.push(lvar[3].sym.type);
				break;
			case iaload:
			case baload:
			case caload:
			case saload:
				state.pop(2);
				state.push(syms.intType);
				break;
			case laload:
				state.pop(2);
				state.push(syms.longType);
				break;
			case faload:
				state.pop(2);
				state.push(syms.floatType);
				break;
			case daload:
				state.pop(2);
				state.push(syms.doubleType);
				break;
			case istore_0:
			case istore_1:
			case istore_2:
			case istore_3:
			case fstore_0:
			case fstore_1:
			case fstore_2:
			case fstore_3:
			case astore_0:
			case astore_1:
			case astore_2:
			case astore_3:
			case pop:
			case lshr:
			case lshl:
			case lushr:
				state.pop(1);
				break;
			case areturn:
			case ireturn:
			case freturn:
				assert state.nlocks == 0;
				state.pop(1);
				markDead();
				break;
			case athrow:
				state.pop(1);
				markDead();
				break;
			case lstore_0:
			case lstore_1:
			case lstore_2:
			case lstore_3:
			case dstore_0:
			case dstore_1:
			case dstore_2:
			case dstore_3:
			case pop2:
				state.pop(2);
				break;
			case lreturn:
			case dreturn:
				assert state.nlocks == 0;
				state.pop(2);
				markDead();
				break;
			case dup:
				state.push(state.stack[state.stacksize-1]);
				break;
			case return_:
				assert state.nlocks == 0;
				markDead();
				break;
			case arraylength:
				state.pop(1);
				state.push(syms.intType);
				break;
			case isub:
			case iadd:
			case imul:
			case idiv:
			case imod:
			case ishl:
			case ishr:
			case iushr:
			case iand:
			case ior:
			case ixor:
				state.pop(1);
				// state.pop(1);
				// state.push(syms.intType);
				break;
			case aastore:
				state.pop(3);
				break;
			case land:
			case lor:
			case lxor:
			case lmod:
			case ldiv:
			case lmul:
			case lsub:
			case ladd:
				state.pop(2);
				break;
			case lcmp:
				state.pop(4);
				state.push(syms.intType);
				break;
			case l2i:
				state.pop(2);
				state.push(syms.intType);
				break;
			case i2l:
				state.pop(1);
				state.push(syms.longType);
				break;
			case i2f:
				state.pop(1);
				state.push(syms.floatType);
				break;
			case i2d:
				state.pop(1);
				state.push(syms.doubleType);
				break;
			case l2f:
				state.pop(2);
				state.push(syms.floatType);
				break;
			case l2d:
				state.pop(2);
				state.push(syms.doubleType);
				break;
			case f2i:
				state.pop(1);
				state.push(syms.intType);
				break;
			case f2l:
				state.pop(1);
				state.push(syms.longType);
				break;
			case f2d:
				state.pop(1);
				state.push(syms.doubleType);
				break;
			case d2i:
				state.pop(2);
				state.push(syms.intType);
				break;
			case d2l:
				state.pop(2);
				state.push(syms.longType);
				break;
			case d2f:
				state.pop(2);
				state.push(syms.floatType);
				break;
			case tableswitch:
			case lookupswitch:
				state.pop(1);
				// the caller is responsible for patching up the state
				break;
			case dup_x1: {
				Type val1 = state.pop1();
				Type val2 = state.pop1();
				state.push(val1);
				state.push(val2);
				state.push(val1);
				break;
			}
			case bastore:
				state.pop(3);
				break;
			case int2byte:
			case int2char:
			case int2short:
				break;
			case fmul:
			case fadd:
			case fsub:
			case fdiv:
			case fmod:
				state.pop(1);
				break;
			case castore:
			case iastore:
			case fastore:
			case sastore:
				state.pop(3);
				break;
			case lastore:
			case dastore:
				state.pop(4);
				break;
			case dup2:
				if (state.stack[state.stacksize-1] != null) {
					Type value1 = state.pop1();
					Type value2 = state.pop1();
					state.push(value2);
					state.push(value1);
					state.push(value2);
					state.push(value1);
				} else {
					Type value = state.pop2();
					state.push(value);
					state.push(value);
				}
				break;
			case dup2_x1:
				if (state.stack[state.stacksize-1] != null) {
					Type value1 = state.pop1();
					Type value2 = state.pop1();
					Type value3 = state.pop1();
					state.push(value2);
					state.push(value1);
					state.push(value3);
					state.push(value2);
					state.push(value1);
				} else {
					Type value1 = state.pop2();
					Type value2 = state.pop1();
					state.push(value1);
					state.push(value2);
					state.push(value1);
				}
				break;
			case dup2_x2:
				if (state.stack[state.stacksize-1] != null) {
					Type value1 = state.pop1();
					Type value2 = state.pop1();
					if (state.stack[state.stacksize-1] != null) {
						// form 1
						Type value3 = state.pop1();
						Type value4 = state.pop1();
						state.push(value2);
						state.push(value1);
						state.push(value4);
						state.push(value3);
						state.push(value2);
						state.push(value1);
					} else {
						// form 3
						Type value3 = state.pop2();
						state.push(value2);
						state.push(value1);
						state.push(value3);
						state.push(value2);
						state.push(value1);
					}
				} else {
					Type value1 = state.pop2();
					if (state.stack[state.stacksize-1] != null) {
						// form 2
						Type value2 = state.pop1();
						Type value3 = state.pop1();
						state.push(value1);
						state.push(value3);
						state.push(value2);
						state.push(value1);
					} else {
						// form 4
						Type value2 = state.pop2();
						state.push(value1);
						state.push(value2);
						state.push(value1);
					}
				}
				break;
			case dup_x2: {
				Type value1 = state.pop1();
				if (state.stack[state.stacksize-1] != null) {
					// form 1
					Type value2 = state.pop1();
					Type value3 = state.pop1();
					state.push(value1);
					state.push(value3);
					state.push(value2);
					state.push(value1);
				} else {
					// form 2
					Type value2 = state.pop2();
					state.push(value1);
					state.push(value2);
					state.push(value1);
				}
			}
				break;
			case fcmpl:
			case fcmpg:
				state.pop(2);
				state.push(syms.intType);
				break;
			case dcmpl:
			case dcmpg:
				state.pop(4);
				state.push(syms.intType);
				break;
			case swap: {
				Type value1 = state.pop1();
				Type value2 = state.pop1();
				state.push(value1);
				state.push(value2);
				break;
			}
			case dadd:
			case dsub:
			case dmul:
			case ddiv:
			case dmod:
				state.pop(2);
				break;
			case ret:
				markDead();
				break;
			case wide:
				// must be handled by the caller.
				return;
			case monitorenter:
			case monitorexit:
				state.pop(1);
				break;

			default:
				throw new AssertionError(mnem(op));
		}
		postop();
		
		}finally{//我加上的
		DEBUG.P(0,this,"emitop0(int op)");
		}
    }

    /** Emit an opcode with a one-byte operand field.
     */
    public void emitop1(int op, int od) {
		try {//我加上的
		DEBUG.P(this,"emitop1(int op, int od)");
		DEBUG.P("op="+op+"  od="+od);

		emitop(op);
		if (!alive) return;
		emit1(od);
		switch (op) {
			case bipush://此时的od是常量(8位)
				state.push(syms.intType);
				break;
			case ldc1://此时的od是常量池索引
				state.push(typeForPool(pool.pool[od]));
				break;
			default:
				throw new AssertionError(mnem(op));
		}
		postop();
		
		}finally{//我加上的
		DEBUG.P(0,this,"emitop1(int op, int od)");
		}
    }

    /** The type of a constant pool entry. */
    private Type typeForPool(Object o) {
		if (o instanceof Integer) return syms.intType;
		if (o instanceof Float) return syms.floatType;
		if (o instanceof String) return syms.stringType;
		if (o instanceof Long) return syms.longType;
		if (o instanceof Double) return syms.doubleType;
		if (o instanceof ClassSymbol) return syms.classType;
		if (o instanceof Type.ArrayType) return syms.classType;
		throw new AssertionError(o);
    }

    /** Emit an opcode with a one-byte operand field;
     *  widen if field does not fit in a byte.
     */
    public void emitop1w(int op, int od) {
		try {//我加上的
		DEBUG.P(this,"emitop1w(int op, int od)");
		DEBUG.P("op="+op+"  od="+od);

		if (od > 0xFF) {//常量池索引号或局部变量数组索引号大于255时，采用宽索引
			emitop(wide);
			emitop(op);
			emit2(od);
		} else {
			emitop(op);
			emit1(od);
		}
		if (!alive) return;
		switch (op) {
			case iload:
				state.push(syms.intType);
				break;
			case lload:
				state.push(syms.longType);
				break;
			case fload:
				state.push(syms.floatType);
				break;
			case dload:
				state.push(syms.doubleType);
				break;
			case aload:
				state.push(lvar[od].sym.type);
				break;
			case lstore:
			case dstore:
				state.pop(2);
				break;
			case istore:
			case fstore:
			case astore:
				state.pop(1);
				break;
			case ret:
				markDead();
				break;
			default:
				throw new AssertionError(mnem(op));
		}
		postop();
		}finally{//我加上的
		DEBUG.P(0,this,"emitop1w(int op, int od)");
		}
    }

    /** Emit an opcode with two one-byte operand fields;
     *  widen if either field does not fit in a byte.
     */
    public void emitop1w(int op, int od1, int od2) {
		try {//我加上的
		DEBUG.P(this,"emitop1w(int op, int od1, int od2)");
		DEBUG.P("op="+op+"  od1="+od1+"  od2="+od2);
		if (od1 > 0xFF || od2 < -128 || od2 > 127) {
			emitop(wide);
			emitop(op);
			emit2(od1);
			emit2(od2);
		} else {
			emitop(op);
			emit1(od1);
			emit1(od2);
		}
		if (!alive) return;
		switch (op) {
			case iinc:
				break;
			default:
				throw new AssertionError(mnem(op));
		}
		}finally{//我加上的
		DEBUG.P(0,this,"emitop1w(int op, int od1, int od2)");
		}
    }

    /** Emit an opcode with a two-byte operand field.
     */
    public void emitop2(int op, int od) {
		try {//我加上的
		DEBUG.P(this,"emitop2(int op, int od)");
		DEBUG.P("op="+op+" mnem="+mnem(op)+" od="+od);
		
		emitop(op);
		if (!alive) return;
		emit2(od);
		switch (op) {
			case getstatic:
				state.push(((Symbol)(pool.pool[od])).erasure(types));
				break;
			case putstatic:
				state.pop(((Symbol)(pool.pool[od])).erasure(types));
				break;
			case new_:
				state.push(uninitializedObject(((Symbol)(pool.pool[od])).erasure(types), cp-3));
				break;
			case sipush:
				state.push(syms.intType);
				break;
			case if_acmp_null:
			case if_acmp_nonnull:
			case ifeq:
			case ifne:
			case iflt:
			case ifge:
			case ifgt:
			case ifle:
				state.pop(1);
				break;
			case if_icmpeq:
			case if_icmpne:
			case if_icmplt:
			case if_icmpge:
			case if_icmpgt:
			case if_icmple:
			case if_acmpeq:
			case if_acmpne:
				state.pop(2);
				break;
			case goto_:
				markDead();
				break;
			case putfield:
				state.pop(((Symbol)(pool.pool[od])).erasure(types));
				state.pop(1); // object ref
				break;
			case getfield:
				state.pop(1); // object ref
				state.push(((Symbol)(pool.pool[od])).erasure(types));
				break;
			case checkcast: {
				state.pop(1); // object ref
				Object o = pool.pool[od];
				Type t = (o instanceof Symbol)
				? ((Symbol)o).erasure(types)
				: types.erasure(((Type)o));
				state.push(t);
				break; }
			case ldc2w:
				state.push(typeForPool(pool.pool[od]));
				break;
			case instanceof_:
				state.pop(1);
				state.push(syms.intType);
				break;
			case ldc2:
				state.push(typeForPool(pool.pool[od]));
				break;
			case jsr:
				break;
			default:
				throw new AssertionError(mnem(op));
		}
		// postop();
		
		}finally{//我加上的
		DEBUG.P(0,this,"emitop2(int op, int od)");
		}
    }

    /** Emit an opcode with a four-byte operand field.
     */
    public void emitop4(int op, int od) {
		try {//我加上的
		DEBUG.P(this,"emitop4(int op, int od)");
		DEBUG.P("op="+op+" mnem="+mnem(op)+" od="+od);
		
		emitop(op);
		if (!alive) return;
		emit4(od);
		switch (op) {
			case goto_w:
				markDead();
				break;
			case jsr_w:
				break;
			default:
				throw new AssertionError(mnem(op));
		}

		// postop();
		}finally{//我加上的
		DEBUG.P(0,this,"emitop4(int op, int od)");
		}
    }

    /** Align code pointer to next `incr' boundary.
     */
    public void align(int incr) {
        if (alive)
            while (cp % incr != 0) emitop0(nop);
    }

    /** Place a byte into code at address pc. Pre: pc + 1 <= cp.
     */
    private void put1(int pc, int op) {
        code[pc] = (byte)op;
    }

    /** Place two bytes into code at address pc. Pre: pc + 2 <= cp.
     */
    private void put2(int pc, int od) {
    	DEBUG.P(this,"put2(int pc, int od)");
		DEBUG.P("pc="+pc+" od="+od);
		
        // pre: pc + 2 <= cp
        put1(pc, od >> 8);
        put1(pc+1, od);
        
        DEBUG.P(0,this,"put2(int pc, int od)");
    }

    /** Place four  bytes into code at address pc. Pre: pc + 4 <= cp.
     */
    public void put4(int pc, int od) {
        // pre: pc + 4 <= cp
        put1(pc  , od >> 24);
        put1(pc+1, od >> 16);
        put1(pc+2, od >> 8);
        put1(pc+3, od);
    }

    /** Return code byte at position pc as an unsigned int.
     */
    private int get1(int pc) {
        return code[pc] & 0xFF;
    }

    /** Return two code bytes at position pc as an unsigned int.
     */
    private int get2(int pc) {
        return (get1(pc) << 8) | get1(pc+1);
    }

    /** Return four code bytes at position pc as an int.
     */
    public int get4(int pc) {
        // pre: pc + 4 <= cp
        return
            (get1(pc) << 24) |
            (get1(pc+1) << 16) |
            (get1(pc+2) << 8) |
            (get1(pc+3));
    }

    /** Is code generation currently enabled?
     */
    public boolean isAlive() {
		return alive || pendingJumps != null;
    }

    /** Switch code generation on/off.
     */
    public void markDead() {
		alive = false;
    }

    /** Declare an entry point; return current code pointer
     */
    public int entryPoint() {
		DEBUG.P(this,"entryPoint()");
		
		int pc = curPc();
		alive = true;
		pendingStackMap = needStackMap;

		DEBUG.P("pc="+pc+" pendingStackMap="+pendingStackMap);
		DEBUG.P(0,this,"entryPoint()");
		return pc;
    }

    /** Declare an entry point with initial state;
     *  return current code pointer
     */
    public int entryPoint(State state) {
		DEBUG.P(this,"entryPoint(1)");
		DEBUG.P("state="+state);
		
		int pc = curPc();
		alive = true;
		this.state = state.dup();
		assert state.stacksize <= max_stack;
		if (debugCode) System.err.println("entry point " + state);
		pendingStackMap = needStackMap;
		
		DEBUG.P("pc="+pc+" pendingStackMap="+pendingStackMap);
		DEBUG.P(0,this,"entryPoint(1)");
		return pc;
    }

    /** Declare an entry point with initial state plus a pushed value;
     *  return current code pointer
     */
    public int entryPoint(State state, Type pushed) {
		DEBUG.P(this,"entryPoint(2)");
		DEBUG.P("state="+state);
		DEBUG.P("pushed="+pushed);
		
		int pc = curPc();
		alive = true;
		this.state = state.dup();
		assert state.stacksize <= max_stack;
		this.state.push(pushed);
		if (debugCode) System.err.println("entry point " + state);
		pendingStackMap = needStackMap;
		
		DEBUG.P("pc="+pc+" pendingStackMap="+pendingStackMap);
		DEBUG.P(0,this,"entryPoint(2)");
		return pc;
    }


/**************************************************************************
 * Stack map generation
 *************************************************************************/

    /** An entry in the stack map. */
    static class StackMapFrame {
		int pc;
		Type[] locals;
		Type[] stack;
    }

    /** A buffer of cldc stack map entries. */
    StackMapFrame[] stackMapBuffer = null;
    
    /** A buffer of compressed StackMapTable entries. */
    StackMapTableFrame[] stackMapTableBuffer = null;
    int stackMapBufferSize = 0;

    /** The last PC at which we generated a stack map. */
    int lastStackMapPC = -1;
    
    /** The last stack map frame in StackMapTable. */
    StackMapFrame lastFrame = null;
    
    /** The stack map frame before the last one. */
    StackMapFrame frameBeforeLast = null;

    /** Emit a stack map entry.  */
    public void emitStackMap() {
		try {//我加上的
		DEBUG.P(this,"emitStackMap()");
		DEBUG.P("needStackMap="+needStackMap);

		int pc = curPc();
		if (!needStackMap) return;
			
		DEBUG.P("pc="+pc);
		DEBUG.P("stackMap="+stackMap);
        switch (stackMap) {
            case CLDC:
                emitCLDCStackMap(pc, getLocalsSize());
                break;
            case JSR202:
                emitStackMapFrame(pc, getLocalsSize());
                break;
            default:
                throw new AssertionError("Should have chosen a stackmap format");
		}
		// DEBUG code follows
		if (debugCode) state.dump(pc);
		
		}finally{//我加上的
		DEBUG.P(0,this,"emitStackMap()");
		}
    }
    
    private int getLocalsSize() {
        int nextLocal = 0;
		for (int i=max_locals-1; i>=0; i--) {
			if (state.defined.isMember(i) && lvar[i] != null) {
				//如果一个居部变量是long或double类型，它占用两个数组项，
				//但这个居部变量在居部变量数组中实际只存放在索引号较低的那一项中，
				//另一项留空，但不能被其他居部变量占用
				nextLocal = i + width(lvar[i].sym.erasure(types));
				break;
			}
		}
        return nextLocal;
    }
    
    /** Emit a CLDC stack map frame. */
    void emitCLDCStackMap(int pc, int localsSize) {
		if (lastStackMapPC == pc) {
			// drop existing stackmap at this offset
			stackMapBuffer[--stackMapBufferSize] = null;
		}
		lastStackMapPC = pc;

		if (stackMapBuffer == null) {
			stackMapBuffer = new StackMapFrame[20];
		} else if (stackMapBuffer.length == stackMapBufferSize) {
			StackMapFrame[] newStackMapBuffer =
			new StackMapFrame[stackMapBufferSize << 1];
			System.arraycopy(stackMapBuffer, 0, newStackMapBuffer,
					 0, stackMapBufferSize);
			stackMapBuffer = newStackMapBuffer;
		}
		StackMapFrame frame =
			stackMapBuffer[stackMapBufferSize++] = new StackMapFrame();
		frame.pc = pc;
			
		frame.locals = new Type[localsSize];
		for (int i=0; i<localsSize; i++) {
			if (state.defined.isMember(i) && lvar[i] != null) {
				Type vtype = lvar[i].sym.type;
				if (!(vtype instanceof UninitializedType))
					vtype = types.erasure(vtype);
				frame.locals[i] = vtype;
			}
		}
		frame.stack = new Type[state.stacksize];
		for (int i=0; i<state.stacksize; i++)
			frame.stack[i] = state.stack[i];
    }
    
    void emitStackMapFrame(int pc, int localsSize) {
        if (lastFrame == null) {
            // first frame
            lastFrame = getInitialFrame();
        } else if (lastFrame.pc == pc) {
			// drop existing stackmap at this offset
			stackMapTableBuffer[--stackMapBufferSize] = null;
            lastFrame = frameBeforeLast;
            frameBeforeLast = null;
		}
        
        StackMapFrame frame = new StackMapFrame();
        frame.pc = pc;

		int localCount = 0;
		Type[] locals = new Type[localsSize];
        for (int i=0; i<localsSize; i++, localCount++) {
            if (state.defined.isMember(i) && lvar[i] != null) {
                Type vtype = lvar[i].sym.type;
				if (!(vtype instanceof UninitializedType))
					vtype = types.erasure(vtype);
				locals[i] = vtype;
				if (width(vtype) > 1) i++;
            }
		}
		frame.locals = new Type[localCount];
		for (int i=0, j=0; i<localsSize; i++, j++) {
            assert(j < localCount);
			frame.locals[j] = locals[i];
            if (width(locals[i]) > 1) i++;
		}

		int stackCount = 0;
		for (int i=0; i<state.stacksize; i++) {
            if (state.stack[i] != null) {
                stackCount++;
			}
		}
		frame.stack = new Type[stackCount];
		stackCount = 0;
		for (int i=0; i<state.stacksize; i++) {
            if (state.stack[i] != null) {
                frame.stack[stackCount++] = state.stack[i];
			}
		}	
            
        if (stackMapTableBuffer == null) {
			stackMapTableBuffer = new StackMapTableFrame[20];
		} else if (stackMapTableBuffer.length == stackMapBufferSize) {
			StackMapTableFrame[] newStackMapTableBuffer =
			new StackMapTableFrame[stackMapBufferSize << 1];
			System.arraycopy(stackMapTableBuffer, 0, newStackMapTableBuffer,
							0, stackMapBufferSize);
			stackMapTableBuffer = newStackMapTableBuffer;
		}
		stackMapTableBuffer[stackMapBufferSize++] = 
                StackMapTableFrame.getInstance(frame, lastFrame.pc, lastFrame.locals, types);
               
        frameBeforeLast = lastFrame;
        lastFrame = frame;
    }
    
    StackMapFrame getInitialFrame() {
        StackMapFrame frame = new StackMapFrame();
        List<Type> arg_types = ((MethodType)meth.externalType(types)).argtypes;
        int len = arg_types.length();
        int count = 0;
        if (!meth.isStatic()) {
            Type thisType = meth.owner.type;
            frame.locals = new Type[len+1];
            if (meth.isConstructor() && thisType != syms.objectType) {
                frame.locals[count++] = UninitializedType.uninitializedThis(thisType);
            } else {
                frame.locals[count++] = types.erasure(thisType);
            }
        } else {
            frame.locals = new Type[len];
        }
        for (Type arg_type : arg_types) {
            frame.locals[count++] = types.erasure(arg_type);
        }
        frame.pc = -1;
        frame.stack = null;
        return frame;
    }
    
    
/**************************************************************************
 * Operations having to do with jumps
 *************************************************************************/

    /** A chain represents a list of unresolved jumps. Jump locations
     *  are sorted in decreasing order.
     */
    public static class Chain {

		/** The position of the jump instruction.
		 */
		public final int pc;

		/** The machine state after the jump instruction.
		 *  Invariant: all elements of a chain list have the same stacksize
		 *  and compatible stack and register contents.
		 */
		Code.State state;

		/** The next jump in the list.
		 */
		public final Chain next;

		/** Construct a chain from its jump position, stacksize, previous
		 *  chain, and machine state.
		 */
		public Chain(int pc, Chain next, Code.State state) {
			DEBUG.P(this,"Chain(3)");
			DEBUG.P("pc="+pc);
			DEBUG.P("next="+next);
			DEBUG.P("state="+state);

			this.pc = pc;
			this.next = next;
			this.state = state;

			DEBUG.P(0,this,"Chain(3)");
		}
		
		//我加上的
		public String toString() {
			return "Chain(pc="+pc+(next!=null?" next="+next:"")+")";
		}
    }

    /** Negate a branch opcode.
     */

	/*取当前条件分支指令相反的分支指令
	(“等于”对应“不等于”、“小于”对应“大于等于”、
	“大于”对应“小于等于”、“null”对应“非空”)
	例1:
	如果当前分支指令是ifeq(如果栈顶的值等于0则跳转),
	那么与ifeq相反的分支指令就是ifne(如果栈顶的值不等于0则跳转)

	例2:
	如果当前分支指令是if_icmplt(如果栈顶往下一项的值小于栈顶的值则跳转),
	那么与if_icmplt相反的分支指令就是if_icmpge
	(如果栈顶往下一项的值大于等于栈顶的值则跳转)
	*/
    public static int negate(int opcode) {
		/*下面的“((opcode + 1) ^ 1) - 1”语句有很强的技巧性，
		如果一条指令码是偶数，那么加1后变成奇数，
		而这个奇数转换成二进制后，最后一个bit肯定是二进制的1，
		若此奇数再与十进制的1进行“异或运算(运算符是:^)”
		相当于是把此奇数-1，此时还原成最初的偶数指令码，
		当最后再-1时，变成了一个更小的奇数，
		这个更小的奇数就是最初的偶数指令码的相反指令。
		例子:
		如果opcode=154，那么指令码是ifne，是一个偶数指令码，
		接着(opcode + 1)=155(变成了奇数)，
		再接着(155 ^ 1)=155-1=154(还原成最初的偶数指令码ifne)
		最后(154-1)=153(就是指令码ifne的相反指令ifeq)；


		如果一条指令码是奇数，那么加1后变成偶数，
		而这个偶数转换成二进制后，最后一个bit肯定是二进制的0，
		若此偶数再与十进制的1进行“异或运算(运算符是:^)”
		相当于是把此偶数+1，当最后再-1时，先前所做的“异或运算”相当于没做，
		这个偶数所对应的指令码就是最初的奇数指令码的相反指令。
		例子:
		如果opcode=157，那么指令码是ifgt，是一个奇数指令码，
		接着(opcode + 1)=158(变成了偶数)，
		再接着(158 ^ 1)=158+1=159
		最后(159-1)=158(就是指令码ifgt的相反指令ifle)；

		总结上面两点说明“((opcode + 1) ^ 1) - 1”语句完成的功能是:
		如果opcode是奇数，那么语句执行结果是一个比opcode大1的偶数；
		如果opcode是偶数，那么语句执行结果是一个比opcode小1的奇数。
		
		从这两个功能特点来看下列JVM中的指令码设计思路
		-----------------------------------------------
		ifeq		= 153,//等于
		ifne		= 154,//不等于
		iflt		= 155,//小于
		ifge		= 156,//大于等于
		ifgt		= 157,//大于
		ifle		= 158,//小于等于
		if_icmpeq	= 159,
		if_icmpne	= 160,
		if_icmplt	= 161,
		if_icmpge	= 162,
		if_icmpgt	= 163,
		if_icmple	= 164,
		if_acmpeq	= 165,
		if_acmpne	= 166,
		goto_		= 167,
		jsr			= 168,
		if_acmp_null    = 198,
		if_acmp_nonnull = 199,
		-----------------------------------------------
		首先:从ifeq到if_acmpne这14条指令码值都是刚好以两条互反分支指令码对排列的，
			 例如:ifeq与ifne是两条互反指令码，

		其次:两条互反指令码对应的指令码值都是以奇数开始，偶数结束，
			 其中的偶数还大于奇数
			 (如两条互反指令码ifeq与ifne: ifeq=153(奇数),ifne=154(偶数),154>153

		上面这两点刚好可以用“((opcode + 1) ^ 1) - 1”语句来完成，

		而if_acmp_null与if_acmp_nonnull指令码值的设计就违反了上面的第二点，
		无法用“((opcode + 1) ^ 1) - 1”语句来完成，
		因为如果opcode=if_acmp_null=198时，
		语句的执行结果是197，不是if_acmp_nonnull=199；

		同样，如果opcode=if_acmp_nonnull=199时，
		语句的执行结果是200，不是if_acmp_null=198。

		所以在negate方法中不得不用两条if语句来判断
		opcode=if_acmp_null或opcode=if_acmp_nonnull时的特殊情况


		意义:从上面的叙述可以看出，指令码值的设计对编译器的实现逻辑有很大影响，
		比如像上面的if_acmp_null与if_acmp_nonnull指令，只要在设计之初多考虑一下，
		把if_acmp_nonnull与if_acmp_nonnull指令码值设为197与198(或199与200)，
		那么negate方法中两条多余的if语句完全可以去掉。
		还好目前的JVM所有指令中互反的条件分支指令码
		只有if_acmp_nonnull与if_acmp_nonnull不符合设计规律，要是将来还有
		其他不符合设计规律的指令加进来，不知还要多加几个if...else if ？？？
		*/
		try {//我加上的
		DEBUG.P(Code.class,"negate(1)");
		DEBUG.P("opcode="+mnem(opcode));
		
		if (opcode == if_acmp_null) return if_acmp_nonnull;
		else if (opcode == if_acmp_nonnull) return if_acmp_null;
		else return ((opcode + 1) ^ 1) - 1;
		
		}finally{//我加上的
		DEBUG.P(0,Code.class,"negate(1)");
		}
    }

    /** Emit a jump instruction.
     *  Return code pointer of instruction to be patched.
     */
    public int emitJump(int opcode) {
		try {//我加上的
		DEBUG.P(this,"emitJump(1)");
		DEBUG.P("opcode="+mnem(opcode));
		DEBUG.P("fatcode="+fatcode);
		
		if (fatcode) {
			if (opcode == goto_ || opcode == jsr) {
				//goto_转换成goto_w，jsr转换成jsr_w，采用4个字节的偏移量
				emitop4(opcode + goto_w - goto_, 0);
			} else {
				emitop2(negate(opcode), 8);
				emitop4(goto_w, 0);
				alive = true;
				pendingStackMap = needStackMap;
			}
			return cp - 5;
		} else {
			emitop2(opcode, 0);//先置0，之后会在resolve(2)方法中回填
			//保存指令位置(因为emitop2(opcode, 0)往code数组中放入3个字节
			//后cp还多加了1，所以cp-3相当于回退到存放指令码的索引位置)
			return cp - 3;
		}
		
		}finally{//我加上的
		DEBUG.P(0,this,"emitJump(1)");
		}
    }

    /** Emit a branch with given opcode; return its chain.
     *  branch differs from jump in that jsr is treated as no-op.
     */
    public Chain branch(int opcode) {
		try {//我加上的
		DEBUG.P(this,"branch(1)");
		DEBUG.P("opcode="+mnem(opcode));
		DEBUG.P("pendingJumps="+pendingJumps);
		DEBUG.P("(opcode != dontgoto)="+(opcode != dontgoto));
		
		Chain result = null;
		if (opcode == goto_) {
			result = pendingJumps;
			pendingJumps = null;
		}
		
		//dontgoto就是jsr指令    
		if (opcode != dontgoto && isAlive()) {
			result = new Chain(emitJump(opcode),
					   result,
					   state.dup());
			fixedPc = fatcode;
			if (opcode == goto_) alive = false;
		}

		DEBUG.P("result="+result);
		DEBUG.P("pendingJumps="+pendingJumps);
		DEBUG.P("fixedPc="+fixedPc);
		DEBUG.P("alive="+alive);

		return result;
		
		}finally{//我加上的
		DEBUG.P(0,this,"branch(1)");
		}
    }

    /** Resolve chain to point to given target.
     */
    public void resolve(Chain chain, int target) {
		DEBUG.P(this,"resolve(2)");
		DEBUG.P("chain="+chain);
		DEBUG.P("target="+target);
		DEBUG.P("cp="+cp);
		DEBUG.P("fatcode="+fatcode);

		boolean changed = false;
		State newState = state;
		for (; chain != null; chain = chain.next) {
			assert state != chain.state;
			assert target > chain.pc || state.stacksize == 0;
				
			if (target >= cp) {
				target = cp;
			} else if (get1(target) == goto_) {
				if (fatcode) target = target + get4(target + 1);
				else target = target + get2(target + 1);
			}

			DEBUG.P("get1(chain.pc)="+mnem(get1(chain.pc)));
			DEBUG.P("chain.pc + 3="+(chain.pc + 3));
			DEBUG.P("target="+target);
			DEBUG.P("cp="+cp);
			DEBUG.P("fixedPc="+fixedPc);
			if (get1(chain.pc) == goto_ &&
					chain.pc + 3 == target && target == cp && !fixedPc) {
					// If goto the next instruction, the jump is not needed: 
					// compact the code.
					cp = cp - 3;
					target = target - 3;
					if (chain.next == null) {
						// This is the only jump to the target. Exit the loop 
						// without setting new state. The code is reachable 
						// from the instruction before goto_.
						alive = true;
						break;
					}
			} else {
				if (fatcode)
					put4(chain.pc + 1, target - chain.pc);
				else if (target - chain.pc < Short.MIN_VALUE ||
							target - chain.pc > Short.MAX_VALUE)
					fatcode = true;
				else
					//注意这里是相对于指令位置的偏移量，不要与用javap工具反编译后的
					//结果相混淆
					put2(chain.pc + 1, target - chain.pc);

				assert !alive ||
					chain.state.stacksize == newState.stacksize &&
					chain.state.nlocks == newState.nlocks;
			}
				
			fixedPc = true;
			if (cp == target) {
				changed = true;
				if (debugCode)
					System.err.println("resolving chain state=" + chain.state);
				if (alive) {
					newState = chain.state.join(newState);
				} else {
					newState = chain.state;
					alive = true;
				}
			}
		}
		assert !changed || state != newState;
		if (state != newState) {
			setDefined(newState.defined);
			state = newState;
			pendingStackMap = needStackMap;
		}

		DEBUG.P(0,this,"resolve(2)");
    }

    /** Resolve chain to point to current code pointer.
     */
    public void resolve(Chain chain) {
		DEBUG.P(this,"resolve(1)");
		DEBUG.P("alive="+alive);
		DEBUG.P("chain="+chain);
		DEBUG.P("pendingJumps前="+pendingJumps);

		assert
			!alive ||
			chain==null ||
			state.stacksize == chain.state.stacksize &&
			state.nlocks == chain.state.nlocks;
		pendingJumps = mergeChains(chain, pendingJumps);

		DEBUG.P("pendingJumps后="+pendingJumps);
		DEBUG.P(0,this,"resolve(1)");
    }

    /** Resolve any pending jumps.
     */
    public void resolvePending() {
		DEBUG.P(this,"resolvePending()");
		DEBUG.P("pendingJumps前="+pendingJumps);
		
		Chain x = pendingJumps;
		pendingJumps = null;
		resolve(x, cp);
		
		DEBUG.P("pendingJumps后="+pendingJumps);
		DEBUG.P(0,this,"resolvePending()");
    }

    /** Merge the jumps in of two chains into one.
     */
    public static Chain mergeChains(Chain chain1, Chain chain2) {
		try {//我加上的
		DEBUG.P(Code.class,"mergeChains(2)");
		DEBUG.P("chain1="+chain1);
		DEBUG.P("chain2="+chain2);

		// recursive merge sort
        if (chain2 == null) return chain1;
        if (chain1 == null) return chain2;
		assert
			chain1.state.stacksize == chain2.state.stacksize &&
			chain1.state.nlocks == chain2.state.nlocks;
	    
	    //按指令码偏移量(pc)从大到小的顺序合并两个链
        if (chain1.pc < chain2.pc)
            return new Chain(
                chain2.pc,
                mergeChains(chain1, chain2.next),
                chain2.state);
        return new Chain(
                chain1.pc,
                mergeChains(chain1.next, chain2),
                chain1.state);

		}finally{//我加上的
		DEBUG.P(0,Code.class,"mergeChains(2)");
		}
    }


/* **************************************************************************
 * Catch clauses
 ****************************************************************************/

    /** Add a catch clause to code.
     */
    public void addCatch(
		char startPc, char endPc, char handlerPc, char catchType) {
		DEBUG.P(this,"addCatch(4)");
		DEBUG.P("startPc="+(int)startPc+" endPc="+(int)endPc);
		DEBUG.P("handlerPc="+(int)handlerPc+" catchType="+(int)catchType);
		
		catchInfo.append(new char[]{startPc, endPc, handlerPc, catchType});
		
		DEBUG.P(0,this,"addCatch(4)");
    }


/* **************************************************************************
 * Line numbers
 ****************************************************************************/

    /** Add a line number entry.
     */
    public void addLineNumber(char startPc, char lineNumber) {
		//DEBUG.P(this,"addLineNumber(2)");
		//DEBUG.P("startPc="+(int)startPc+"  lineNumber="+(int)lineNumber);
		//DEBUG.P("lineDebugInfo="+lineDebugInfo);

		if (lineDebugInfo) {
			if (lineInfo.nonEmpty() && lineInfo.head[0] == startPc)
				lineInfo = lineInfo.tail;
			if (lineInfo.isEmpty() || lineInfo.head[1] != lineNumber)
				lineInfo = lineInfo.prepend(new char[]{startPc, lineNumber});
		}

		//DEBUG.P(0,this,"addLineNumber(2)");
    }

    /** Mark beginning of statement.
     */
    public void statBegin(int pos) {
		//DEBUG.P(this,"statBegin(int pos)");
		//DEBUG.P("pos="+pos);
		
		if (pos != Position.NOPOS) {
			pendingStatPos = pos;
		}
		
		//DEBUG.P("pendingStatPos="+pendingStatPos);
		//DEBUG.P(0,this,"statBegin(int pos)");
    }

    /** Force stat begin eagerly
     */
    public void markStatBegin() {
		//DEBUG.P(this,"markStatBegin()");
		//DEBUG.P("alive="+alive+"  lineDebugInfo="+lineDebugInfo);
		
		if (alive && lineDebugInfo) {
			int line = lineMap.getLineNumber(pendingStatPos);
			char cp1 = (char)cp;
			char line1 = (char)line;
			//DEBUG.P("(cp1 == cp && line1 == line)="+(cp1 == cp && line1 == line));
			if (cp1 == cp && line1 == line)
				addLineNumber(cp1, line1);
		}
		pendingStatPos = Position.NOPOS;
		
		//DEBUG.P(0,this,"markStatBegin()");
    }


/* **************************************************************************
 * Simulated VM machine state
 ****************************************************************************/
    //State类就像一个简单的JVM，字段Type[] stack就相当于JVM中的一个堆栈，
    //在它之上实现pop,push等与堆栈相关的操作，Code类中的emit方法每加入一条
    //JVM指令时，都用State模拟了JVM执行指令的过程
    class State implements Cloneable {
		/** The set of registers containing values. */
		Bits defined;

		/** The (types of the) contents of the machine stack. */
		Type[] stack;

		/** The first stack position currently unused. */
		int stacksize;

		/** The numbers of registers containing locked monitors. */
		int[] locks;
		int nlocks;

		State() {
			defined = new Bits();
			stack = new Type[16];
		}

		State dup() {
			try {
				State state = (State)super.clone();
				state.defined = defined.dup();
				state.stack = stack.clone();
				if (locks != null) state.locks = locks.clone();
				if (debugCode) {
					System.err.println("duping state " + this);
					dump();
				}
				return state;
			} catch (CloneNotSupportedException ex) {
				throw new AssertionError(ex);
			}
		}

		void lock(int register) {
			DEBUG.P(this,"lock(1)");
			DEBUG.P("register="+register);

			if (locks == null) {
				locks = new int[20];
			} else if (locks.length == nlocks) {
				int[] newLocks = new int[locks.length << 1];
				System.arraycopy(locks, 0, newLocks, 0, locks.length);
				locks = newLocks;
			}
			locks[nlocks] = register;
			nlocks++;

			DEBUG.P("nlocks="+nlocks);
			DEBUG.P(0,this,"lock(1)");
		}

		void unlock(int register) {
			DEBUG.P(this,"unlock(1)");
			DEBUG.P("register="+register);

			nlocks--;
			assert locks[nlocks] == register;
			locks[nlocks] = -1;

			DEBUG.P("unlock="+nlocks);
			DEBUG.P(0,this,"unlock(1)");
		}

		void push(Type t) {
			try {//我加上的
			DEBUG.P(this,"push(Type t)");
			DEBUG.P("t="+t);
			DEBUG.P("stack.push前="+toString());
			
			if (debugCode) System.err.println("   pushing " + t);
			switch (t.tag) {
				case TypeTags.VOID:
					return;
				case TypeTags.BYTE:
				case TypeTags.CHAR:
				case TypeTags.SHORT:
				case TypeTags.BOOLEAN:
					t = syms.intType;
					break;
				default:
					break;
			}
			//stacksize+2与width(t)有关
			if (stacksize+2 >= stack.length) {
				Type[] newstack = new Type[2*stack.length];
				System.arraycopy(stack, 0, newstack, 0, stack.length);
				stack = newstack;
			}
			stack[stacksize++] = t;
			switch (width(t)) {
				case 1:
					break;
				case 2:
					stack[stacksize++] = null;
					break;
				default:
					throw new AssertionError(t);
			}
			if (stacksize > max_stack)
				max_stack = stacksize;
			
			}finally{//我加上的
			DEBUG.P("stack.push后="+toString());
			DEBUG.P(0,this,"push(Type t)");
			}
		}

		Type pop1() {
			if (debugCode) System.err.println("   popping " + 1);
			stacksize--;
			Type result = stack[stacksize];
			stack[stacksize] = null;
			assert result != null && width(result) == 1;
			return result;
		}

		Type peek() { //返回栈顶type
			try {//我加上的
			DEBUG.P(this,"peek()");

			return stack[stacksize-1];

			}finally{//我加上的
			DEBUG.P(0,this,"peek()");
			}
		}

		Type pop2() {
			if (debugCode) System.err.println("   popping " + 2);
			stacksize -= 2;
			Type result = stack[stacksize];
			stack[stacksize] = null;
			assert stack[stacksize+1] == null;
			assert result != null && width(result) == 2;
			return result;
		}

		void pop(int n) {
			try {//我加上的
			DEBUG.P(this,"pop(int n)");
			DEBUG.P("n="+n);
			DEBUG.P("stack.pop前="+toString());
			
			if (debugCode) System.err.println("   popping " + n);
			while (n > 0) {
				stack[--stacksize] = null;
				n--;
			}
			
			}finally{//我加上的
			DEBUG.P("stack.pop后="+toString());
			DEBUG.P(0,this,"pop(int n)");
			}
		}

		void pop(Type t) {
			pop(width(t));
		}

		/** Force the top of the stack to be treated as this supertype
		 *  of its current type. */
		//如果栈顶是CLASS或ARRAY类型，把栈顶的类型替换成它的超类型
		void forceStackTop(Type t) {
			DEBUG.P(this,"forceStackTop(Type t)");
			DEBUG.P("t="+t+"  t.tag="+TypeTags.toString(t.tag));
			DEBUG.P("stack前="+toString());

			if (!alive) return;
			switch (t.tag) {
				case CLASS:
				case ARRAY:
					int width = width(t);
					Type old = stack[stacksize-width];
					assert types.isSubtype(types.erasure(old),
										   types.erasure(t));
					stack[stacksize-width] = t;
					break;
				default:
			}

			DEBUG.P("stack后="+toString());
			DEBUG.P(0,this,"forceStackTop(Type t)");
		}

		void markInitialized(UninitializedType old) {
			DEBUG.P(this,"markInitialized(1)");
			DEBUG.P("old="+old+"  old.tag="+TypeTags.toString(old.tag));
			DEBUG.P("stack前="+toString());

			Type newtype = old.initializedType();
			for (int i=0; i<stacksize; i++)
			if (stack[i] == old) stack[i] = newtype;
			for (int i=0; i<lvar.length; i++) {
				LocalVar lv = lvar[i];
				if (lv != null && lv.sym.type == old) {
					VarSymbol sym = lv.sym;
					sym = sym.clone(sym.owner);
					sym.type = newtype;
					LocalVar newlv = lvar[i] = new LocalVar(sym);
					// should the following be initialized to cp?
					newlv.start_pc = lv.start_pc;
				}
			}

			DEBUG.P("stack后="+toString());
			DEBUG.P(0,this,"markInitialized(1)");
		}
		
		//对照当前State与other的堆栈中的每一项的类型，
		//并替换成超类型存放到State的堆栈中
		State join(State other) {
			try {//我加上的
			DEBUG.P(this,"join(1)");
			DEBUG.P("this ="+toString());
			DEBUG.P("other="+other);

			defined = defined.andSet(other.defined);
			assert stacksize == other.stacksize;
			assert nlocks == other.nlocks;
			for (int i=0; i<stacksize; ) {
				Type t = stack[i];
				Type tother = other.stack[i];
				Type result =
							t==tother ? t :
							types.isSubtype(t, tother) ? tother :
							types.isSubtype(tother, t) ? t :
							error();
				int w = width(result);
				stack[i] = result;
				if (w == 2) assert stack[i+1] == null;
				i += w;
			}
			return this;

			}finally{//我加上的
			DEBUG.P(0,this,"join(1)");
			}
		}

		Type error() {
			throw new AssertionError("inconsistent stack types at join point");
		}

		void dump() {
			dump(-1);
		}

		void dump(int pc) {
			System.err.print("stackMap for " + meth.owner + "." + meth);
			if (pc == -1)
				System.out.println();
			else
				System.out.println(" at " + pc);
			System.err.println(" stack (from bottom):");
			for (int i=0; i<stacksize; i++)
				System.err.println("  " + i + ": " + stack[i]);

			int lastLocal = 0;
			for (int i=max_locals-1; i>=0; i--) {
				if (defined.isMember(i)) {
					lastLocal = i;
					break;
				}
			}
			if (lastLocal >= 0)
				System.err.println(" locals:");
			for (int i=0; i<=lastLocal; i++) {
			System.err.print("  " + i + ": ");
				if (defined.isMember(i)) {
					LocalVar var = lvar[i];
					if (var == null) {
					System.err.println("(none)");
					} else if (var.sym == null)
					System.err.println("UNKNOWN!");
					else
					System.err.println("" + var.sym + " of type " +
							   var.sym.erasure(types));
				} else {
					System.err.println("undefined");
				}
			}
			if (nlocks != 0) {
				System.err.print(" locks:");
				for (int i=0; i<nlocks; i++) {
					System.err.print(" " + locks[i]);
				}
				System.err.println();
			}
		}
		
		//toString是我加上的
		public String toString() {
			StringBuffer sb=new StringBuffer("stack(");
			if(stack!=null) {
				sb.append("size=").append(stack.length);
				for(int i=0;i<stack.length;i++)
					sb.append(", ").append(stack[i]);
			}
			sb.append(")");
			return sb.toString();
		}
    }

    static Type jsrReturnValue = new Type(TypeTags.INT, null);


/* **************************************************************************
 * Local variables
 ****************************************************************************/

    /** A live range of a local variable. */
    static class LocalVar {
		final VarSymbol sym;
		final char reg;
		char start_pc = Character.MAX_VALUE;
		char length = Character.MAX_VALUE;
		LocalVar(VarSymbol v) {
			this.sym = v;
			this.reg = (char)v.adr;
		}
		public LocalVar dup() {
			return new LocalVar(sym);
		}
		public String toString() {
			return "" + sym + " in register " + ((int)reg) + " starts at pc=" + ((int)start_pc) + " length=" + ((int)length);
		}
    };

    /** Local variables, indexed by register. */
    LocalVar[] lvar;

    /** Add a new local variable. */
    private void addLocalVar(VarSymbol v) {
		DEBUG.P(this,"addLocalVar(VarSymbol v)");
		DEBUG.P("v="+v+" v.adr="+v.adr+" lvar.length="+lvar.length);
		for(int i=0;i<lvar.length;i++) 
			if(lvar[i]!=null) DEBUG.P("lvar["+i+"]="+lvar[i]);
		DEBUG.P("");
			
		int adr = v.adr;
		if (adr+1 >= lvar.length) {
			int newlength = lvar.length << 1;
			if (newlength <= adr) newlength = adr + 10;
			LocalVar[] new_lvar = new LocalVar[newlength];
			System.arraycopy(lvar, 0, new_lvar, 0, lvar.length);
			lvar = new_lvar;
		}
		assert lvar[adr] == null;
		DEBUG.P("pendingJumps="+pendingJumps);
		if (pendingJumps != null) resolvePending();
		lvar[adr] = new LocalVar(v);

		DEBUG.P("state.defined.excl前="+state.defined);
		state.defined.excl(adr);
		DEBUG.P("state.defined.excl后="+state.defined);
		
		DEBUG.P("");
		DEBUG.P("lvar.length="+lvar.length);
		for(int i=0;i<lvar.length;i++) 
			if(lvar[i]!=null) DEBUG.P("lvar["+i+"]="+lvar[i]);
		DEBUG.P(1,this,"addLocalVar(VarSymbol v)");
    }

    /** Set the current variable defined state. */
    public void setDefined(Bits newDefined) {
		if (alive && newDefined != state.defined) {
			Bits diff = state.defined.dup().xorSet(newDefined);
			for (int adr = diff.nextBit(0);
			 adr >= 0;
			 adr = diff.nextBit(adr+1)) {
			if (adr >= nextreg)
				state.defined.excl(adr);
			else if (state.defined.isMember(adr))
				setUndefined(adr);
			else
				setDefined(adr);
			}
		}
    }

    /** Mark a register as being (possibly) defined. */
    public void setDefined(int adr) {
		DEBUG.P(this,"setDefined(int adr)");
		DEBUG.P("adr="+adr);
		LocalVar v = lvar[adr];
		DEBUG.P("LocalVar v="+v);
		DEBUG.P("cp="+cp);

		DEBUG.P("");
		DEBUG.P("state.defined.excl前="+state.defined);
		
		if (v == null) {
			state.defined.excl(adr);
		} else {
			state.defined.incl(adr);
			if (cp < Character.MAX_VALUE) {
				if (v.start_pc == Character.MAX_VALUE)
					v.start_pc = (char)cp;
			}
		}

		DEBUG.P("state.defined.excl后="+state.defined);
		DEBUG.P("LocalVar v="+v);
		DEBUG.P(1,this,"setDefined(int adr)");
    }

    /** Mark a register as being undefined. */
    public void setUndefined(int adr) {
		state.defined.excl(adr);
		if (adr < lvar.length &&
			lvar[adr] != null &&
			lvar[adr].start_pc != Character.MAX_VALUE) {
			LocalVar v = lvar[adr];
			char length = (char)(curPc() - v.start_pc);
			if (length > 0 && length < Character.MAX_VALUE) {
				lvar[adr] = v.dup();
				v.length = length;
				putVar(v);
			} else {
				v.start_pc = Character.MAX_VALUE;
			}
		}
    }

    /** End the scope of a variable. */
    private void endScope(int adr) {
        // <editor-fold defaultstate="collapsed">
        /*
        class GenInnerclassTest {
			int fieldA=10;
			{
				
				int b,c=0,d;
				fieldA=20;
				d=1;
			}
			
			GenInnerclassTest() {
				fieldA=30;
			}
        }
        
        env.tree.tag=BLOCK visitBlock
        com.sun.tools.javac.jvm.Code===>endScopes(int first)
        -------------------------------------------------------------------------
        first=2 nextreg=5
        com.sun.tools.javac.jvm.Code===>endScope(int adr)
        -------------------------------------------------------------------------
        adr=2
        v=b in register 2 starts at pc=65535 length=65535
        v.start_pc=65535
        cp=26
        state.defined.excl前=(长度=32)00000000000000000000000000011011
        state.defined.excl后=(长度=32)00000000000000000000000000011011
        com.sun.tools.javac.jvm.Code===>endScope(int adr)  END
        -------------------------------------------------------------------------

        com.sun.tools.javac.jvm.Code===>endScope(int adr)
        -------------------------------------------------------------------------
        adr=3
        v=c in register 3 starts at pc=17 length=65535
        v.start_pc=17
        cp=26

        length=9
        v.length=9
        com.sun.tools.javac.jvm.Code===>putVar(LocalVar var)
        -------------------------------------------------------------------------
        var=c in register 3 starts at pc=17 length=9
        state.defined.excl前=(长度=32)00000000000000000000000000011011
        state.defined.excl后=(长度=32)00000000000000000000000000010011
        com.sun.tools.javac.jvm.Code===>endScope(int adr)  END
        -------------------------------------------------------------------------

        com.sun.tools.javac.jvm.Code===>endScope(int adr)
        -------------------------------------------------------------------------
        adr=4
        v=d in register 4 starts at pc=26 length=65535
        v.start_pc=26
        cp=26

        length=0
        v.length=0
        com.sun.tools.javac.jvm.Code===>putVar(LocalVar var)
        -------------------------------------------------------------------------
        var=d in register 4 starts at pc=26 length=0
        state.defined.excl前=(长度=32)00000000000000000000000000010011
        state.defined.excl后=(长度=32)00000000000000000000000000000011
        com.sun.tools.javac.jvm.Code===>endScope(int adr)  END
        -------------------------------------------------------------------------


        重新赋值nextreg=2
        com.sun.tools.javac.jvm.Code===>endScopes(int first)  END
        -------------------------------------------------------------------------
        */
        // </editor-fold>
		DEBUG.P(this,"endScope(int adr)");
		DEBUG.P("adr="+adr);

		LocalVar v = lvar[adr];

		DEBUG.P("v="+v);
		if (v != null) {
			lvar[adr] = null;
			DEBUG.P("v.start_pc="+(int)v.start_pc);
			DEBUG.P("cp="+cp);
			if (v.start_pc != Character.MAX_VALUE) {
				char length = (char)(curPc() - v.start_pc);

				DEBUG.P("");
				DEBUG.P("length="+(int)length);
				if (length < Character.MAX_VALUE) {
					v.length = length;
					DEBUG.P("v.length="+(int)v.length);
					putVar(v);
				}
			}
		}
		DEBUG.P("state.defined.excl前="+state.defined);
		state.defined.excl(adr);
		DEBUG.P("state.defined.excl后="+state.defined);
		DEBUG.P(0,this,"endScope(int adr)");
    }

    /** Put a live variable range into the buffer to be output to the
     *  class file.
     */
    void putVar(LocalVar var) {
        try {
		DEBUG.P(this,"putVar(LocalVar var)");
		DEBUG.P("var="+var);

		if (!varDebugInfo) return;
		if ((var.sym.flags() & Flags.SYNTHETIC) != 0) return;
		if (varBuffer == null)
			varBuffer = new LocalVar[20];
		else if (varBufferSize >= varBuffer.length) {
			LocalVar[] newVarBuffer = new LocalVar[varBufferSize*2];
			System.arraycopy(varBuffer, 0, newVarBuffer, 0, varBuffer.length);
			varBuffer = newVarBuffer;
		}
		varBuffer[varBufferSize++] = var;

		for(int i=0;i<varBuffer.length;i++) 
			if(varBuffer[i]!=null) DEBUG.P("varBuffer["+i+"]="+varBuffer[i]);
		   
        } finally {
		DEBUG.P(0,this,"putVar(LocalVar var)");
        }
    }

    /** Previously live local variables, to be put into the variable table. */
    LocalVar[] varBuffer;
    int varBufferSize;

    /** Create a new local variable address and return it.
     */
    private int newLocal(int typecode) {
		int reg = nextreg;
		int w = width(typecode);//double和long类型的变量在局部变量数组中也占两项
		nextreg = reg + w;
		if (nextreg > max_locals) max_locals = nextreg;
		return reg;
    }

    private int newLocal(Type type) {
		return newLocal(typecode(type));
    }

    public int newLocal(VarSymbol v) {
		DEBUG.P(this,"newLocal(VarSymbol v)");
		DEBUG.P("v="+v+" v.adr="+v.adr+" nextreg="+nextreg+" max_locals="+max_locals);
		
		int reg = v.adr = newLocal(v.erasure(types));
		addLocalVar(v);
		
		DEBUG.P("v="+v+" v.adr="+v.adr+" nextreg="+nextreg+" max_locals="+max_locals);
		DEBUG.P(1,this,"newLocal(VarSymbol v)");
		return reg;
    }

    /** Start a set of fresh registers.
     */
    public void newRegSegment() {
		DEBUG.P(this,"newRegSegment()");
		DEBUG.P("nextreg前="+nextreg);
		
		nextreg = max_locals;
		
		DEBUG.P("nextreg后="+nextreg);
		DEBUG.P(0,this,"newRegSegment()");
    }

    /** End scopes of all variables with registers >= first.
     */
    public void endScopes(int first) {
		DEBUG.P(this,"endScopes(int first)");
		DEBUG.P("first="+first+" nextreg="+nextreg);
		int prevNextReg = nextreg;
		nextreg = first;
		for (int i = nextreg; i < prevNextReg; i++) endScope(i);

		DEBUG.P("");
		DEBUG.P("重新赋值nextreg="+nextreg);
		DEBUG.P(0,this,"endScopes(int first)");
    }

/**************************************************************************
 * static tables
 *************************************************************************/

    public static String mnem(int opcode) {
		return Mneumonics.mnem[opcode];
    }
    private static class Mneumonics {
		//private final static String[] mnem = new String[ByteCodeCount];
		
		//我加上的，扩大数组大小，为了加入其他非标准指令
		private final static String[] mnem = new String[ByteCodeCount+150];
		static {
			//我加上的------------------开始
			mnem[string_add] = "string_add";
			mnem[bool_not] = "bool_not";
			mnem[bool_and] = "bool_and";
			mnem[bool_or] = "bool_or";
			mnem[ishll] = "ishll";
			mnem[lshll] = "lshll";
			mnem[ishrl] = "ishrl";
			mnem[iushrl] = "iushrl";
			mnem[lushrl] = "lushrl";
			//我加上的------------------结束
			
			
			mnem[nop] = "nop";
			mnem[aconst_null] = "aconst_null";
			mnem[iconst_m1] = "iconst_m1";
			mnem[iconst_0] = "iconst_0";
			mnem[iconst_1] = "iconst_1";
			mnem[iconst_2] = "iconst_2";
			mnem[iconst_3] = "iconst_3";
			mnem[iconst_4] = "iconst_4";
			mnem[iconst_5] = "iconst_5";
			mnem[lconst_0] = "lconst_0";
			mnem[lconst_1] = "lconst_1";
			mnem[fconst_0] = "fconst_0";
			mnem[fconst_1] = "fconst_1";
			mnem[fconst_2] = "fconst_2";
			mnem[dconst_0] = "dconst_0";
			mnem[dconst_1] = "dconst_1";
			mnem[bipush] = "bipush";
			mnem[sipush] = "sipush";
			mnem[ldc1] = "ldc1";
			mnem[ldc2] = "ldc2";
			mnem[ldc2w] = "ldc2w";
			mnem[iload] = "iload";
			mnem[lload] = "lload";
			mnem[fload] = "fload";
			mnem[dload] = "dload";
			mnem[aload] = "aload";
			mnem[iload_0] = "iload_0";
			mnem[lload_0] = "lload_0";
			mnem[fload_0] = "fload_0";
			mnem[dload_0] = "dload_0";
			mnem[aload_0] = "aload_0";
			mnem[iload_1] = "iload_1";
			mnem[lload_1] = "lload_1";
			mnem[fload_1] = "fload_1";
			mnem[dload_1] = "dload_1";
			mnem[aload_1] = "aload_1";
			mnem[iload_2] = "iload_2";
			mnem[lload_2] = "lload_2";
			mnem[fload_2] = "fload_2";
			mnem[dload_2] = "dload_2";
			mnem[aload_2] = "aload_2";
			mnem[iload_3] = "iload_3";
			mnem[lload_3] = "lload_3";
			mnem[fload_3] = "fload_3";
			mnem[dload_3] = "dload_3";
			mnem[aload_3] = "aload_3";
			mnem[iaload] = "iaload";
			mnem[laload] = "laload";
			mnem[faload] = "faload";
			mnem[daload] = "daload";
			mnem[aaload] = "aaload";
			mnem[baload] = "baload";
			mnem[caload] = "caload";
			mnem[saload] = "saload";
			mnem[istore] = "istore";
			mnem[lstore] = "lstore";
			mnem[fstore] = "fstore";
			mnem[dstore] = "dstore";
			mnem[astore] = "astore";
			mnem[istore_0] = "istore_0";
			mnem[lstore_0] = "lstore_0";
			mnem[fstore_0] = "fstore_0";
			mnem[dstore_0] = "dstore_0";
			mnem[astore_0] = "astore_0";
			mnem[istore_1] = "istore_1";
			mnem[lstore_1] = "lstore_1";
			mnem[fstore_1] = "fstore_1";
			mnem[dstore_1] = "dstore_1";
			mnem[astore_1] = "astore_1";
			mnem[istore_2] = "istore_2";
			mnem[lstore_2] = "lstore_2";
			mnem[fstore_2] = "fstore_2";
			mnem[dstore_2] = "dstore_2";
			mnem[astore_2] = "astore_2";
			mnem[istore_3] = "istore_3";
			mnem[lstore_3] = "lstore_3";
			mnem[fstore_3] = "fstore_3";
			mnem[dstore_3] = "dstore_3";
			mnem[astore_3] = "astore_3";
			mnem[iastore] = "iastore";
			mnem[lastore] = "lastore";
			mnem[fastore] = "fastore";
			mnem[dastore] = "dastore";
			mnem[aastore] = "aastore";
			mnem[bastore] = "bastore";
			mnem[castore] = "castore";
			mnem[sastore] = "sastore";
			mnem[pop] = "pop";
			mnem[pop2] = "pop2";
			mnem[dup] = "dup";
			mnem[dup_x1] = "dup_x1";
			mnem[dup_x2] = "dup_x2";
			mnem[dup2] = "dup2";
			mnem[dup2_x1] = "dup2_x1";
			mnem[dup2_x2] = "dup2_x2";
			mnem[swap] = "swap";
			mnem[iadd] = "iadd";
			mnem[ladd] = "ladd";
			mnem[fadd] = "fadd";
			mnem[dadd] = "dadd";
			mnem[isub] = "isub";
			mnem[lsub] = "lsub";
			mnem[fsub] = "fsub";
			mnem[dsub] = "dsub";
			mnem[imul] = "imul";
			mnem[lmul] = "lmul";
			mnem[fmul] = "fmul";
			mnem[dmul] = "dmul";
			mnem[idiv] = "idiv";
			mnem[ldiv] = "ldiv";
			mnem[fdiv] = "fdiv";
			mnem[ddiv] = "ddiv";
			mnem[imod] = "imod";
			mnem[lmod] = "lmod";
			mnem[fmod] = "fmod";
			mnem[dmod] = "dmod";
			mnem[ineg] = "ineg";
			mnem[lneg] = "lneg";
			mnem[fneg] = "fneg";
			mnem[dneg] = "dneg";
			mnem[ishl] = "ishl";
			mnem[lshl] = "lshl";
			mnem[ishr] = "ishr";
			mnem[lshr] = "lshr";
			mnem[iushr] = "iushr";
			mnem[lushr] = "lushr";
			mnem[iand] = "iand";
			mnem[land] = "land";
			mnem[ior] = "ior";
			mnem[lor] = "lor";
			mnem[ixor] = "ixor";
			mnem[lxor] = "lxor";
			mnem[iinc] = "iinc";
			mnem[i2l] = "i2l";
			mnem[i2f] = "i2f";
			mnem[i2d] = "i2d";
			mnem[l2i] = "l2i";
			mnem[l2f] = "l2f";
			mnem[l2d] = "l2d";
			mnem[f2i] = "f2i";
			mnem[f2l] = "f2l";
			mnem[f2d] = "f2d";
			mnem[d2i] = "d2i";
			mnem[d2l] = "d2l";
			mnem[d2f] = "d2f";
			mnem[int2byte] = "int2byte";
			mnem[int2char] = "int2char";
			mnem[int2short] = "int2short";
			mnem[lcmp] = "lcmp";
			mnem[fcmpl] = "fcmpl";
			mnem[fcmpg] = "fcmpg";
			mnem[dcmpl] = "dcmpl";
			mnem[dcmpg] = "dcmpg";
			mnem[ifeq] = "ifeq";
			mnem[ifne] = "ifne";
			mnem[iflt] = "iflt";
			mnem[ifge] = "ifge";
			mnem[ifgt] = "ifgt";
			mnem[ifle] = "ifle";
			mnem[if_icmpeq] = "if_icmpeq";
			mnem[if_icmpne] = "if_icmpne";
			mnem[if_icmplt] = "if_icmplt";
			mnem[if_icmpge] = "if_icmpge";
			mnem[if_icmpgt] = "if_icmpgt";
			mnem[if_icmple] = "if_icmple";
			mnem[if_acmpeq] = "if_acmpeq";
			mnem[if_acmpne] = "if_acmpne";
			mnem[goto_] = "goto_";
			mnem[jsr] = "jsr";
			mnem[ret] = "ret";
			mnem[tableswitch] = "tableswitch";
			mnem[lookupswitch] = "lookupswitch";
			mnem[ireturn] = "ireturn";
			mnem[lreturn] = "lreturn";
			mnem[freturn] = "freturn";
			mnem[dreturn] = "dreturn";
			mnem[areturn] = "areturn";
			mnem[return_] = "return_";
			mnem[getstatic] = "getstatic";
			mnem[putstatic] = "putstatic";
			mnem[getfield] = "getfield";
			mnem[putfield] = "putfield";
			mnem[invokevirtual] = "invokevirtual";
			mnem[invokespecial] = "invokespecial";
			mnem[invokestatic] = "invokestatic";
			mnem[invokeinterface] = "invokeinterface";
			// mnem[___unused___] = "___unused___";
			mnem[new_] = "new_";
			mnem[newarray] = "newarray";
			mnem[anewarray] = "anewarray";
			mnem[arraylength] = "arraylength";
			mnem[athrow] = "athrow";
			mnem[checkcast] = "checkcast";
			mnem[instanceof_] = "instanceof_";
			mnem[monitorenter] = "monitorenter";
			mnem[monitorexit] = "monitorexit";
			mnem[wide] = "wide";
			mnem[multianewarray] = "multianewarray";
			mnem[if_acmp_null] = "if_acmp_null";
			mnem[if_acmp_nonnull] = "if_acmp_nonnull";
			mnem[goto_w] = "goto_w";
			mnem[jsr_w] = "jsr_w";
			mnem[breakpoint] = "breakpoint";
		}
    }
}
