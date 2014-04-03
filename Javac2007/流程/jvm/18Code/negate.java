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