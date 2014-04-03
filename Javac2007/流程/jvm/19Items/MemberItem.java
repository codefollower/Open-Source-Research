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