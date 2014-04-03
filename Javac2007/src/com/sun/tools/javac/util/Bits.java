/*
 * @(#)Bits.java	1.22 07/03/21
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

package com.sun.tools.javac.util;

/** A class for extensible, mutable bit sets.
 *
 *  <p><b>This is NOT part of any API supported by Sun Microsystems.  If
 *  you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 */
@Version("@(#)Bits.java	1.22 07/03/21")
public class Bits {
	private static my.Debug DEBUG=new my.Debug(my.Debug.Bits);//我加上的

    private final static int wordlen = 32;
    private final static int wordshift = 5;
    private final static int wordmask = wordlen - 1;

    private int[] bits;

    /** Construct an initially empty set.
     */
    public Bits() {
        this(new int[1]);
    }

    /** Construct a set consisting initially of given bit vector.
     */
    public Bits(int[] bits) {
        this.bits = bits;
    }

    /** Construct a set consisting initially of given range.
     */
    public Bits(int start, int limit) {
        this();
		inclRange(start, limit);
    }

    private void sizeTo(int len) {
		if (bits.length < len) {
			int[] newbits = new int[len];
			System.arraycopy(bits, 0, newbits, 0, bits.length);
			bits = newbits;
		}
    }

    /** This set = {}.
     */
    public void clear() {
        for (int i = 0; i < bits.length; i++) bits[i] = 0;
    }

    /** Return a copy of this set.
     */
    public Bits dup() {
        int[] newbits = new int[bits.length];
        System.arraycopy(bits, 0, newbits, 0, bits.length);
        return new Bits(newbits);
    }

    /** Include x in this set.
     */
    public void incl(int x) {
		DEBUG.P(this,"incl(int x)");
		//DEBUG.P("x="+x);
		
		assert x >= 0;
		//因为“int[] bits”是整型(int,32位)数组，所以每个数组元素bits[i]可以表示
		//32(按0-31排列)个变量，当变量个数大于32时，扩充数组bits的长度(加1)
		sizeTo((x >>> wordshift) + 1);//相当于x/32+1
		//DEBUG.P("("+x+" >>> "+wordshift+") + 1 = "+((x >>> wordshift) + 1));

		/*
		假如:bits的长度为1,
		bits[0]=00000000000000000000000000000011
		如果x=33,那么((x >>> wordshift) + 1)＝((33/32)+1)=2, bits的长度变为2
		bits[x >>> wordshift]正好对应bits[1],因数组bits的元素是整型(int,32位),
		所以bits[1]在初始化时0,
		也就是bits[1]＝00000000000000000000000000000000(32个0),
		(x & wordmask)=(33 & 31)=(100001 & 11111)=000001=1,
		1<<1=1,
		bits[1] | 1 =00000000000000000000000000000000 | 1
					=00000000000000000000000000000001
		*/
		bits[x >>> wordshift] = bits[x >>> wordshift] |
			(1 << (x & wordmask));
			
		DEBUG.P("x="+x+" bits["+(x >>> wordshift)+"] = "+bits[x >>> wordshift]);
		
		DEBUG.P(0,this,"incl(int x)");
    }
    
    /*
    i<<n表示i*(2的n次方)
    i>>n表示i/(2的n次方)
    i>>>n如果i是非负数，则跟i>>n一样，否则最左边空出的位用0补，
    因此i>>>n的结果一定是非负数
    */


    /** Include [start..limit) in this set.
     */
    public void inclRange(int start, int limit) {
		sizeTo((limit >>> wordshift) + 1);
		for (int x = start; x < limit; x++)
			bits[x >>> wordshift] = bits[x >>> wordshift] |
			(1 << (x & wordmask));
	}

		/** Exclude x from this set.
		 */
	public void excl(int x) {
		DEBUG.P(this,"excl(int x)");
		//DEBUG.P("x="+x);
		
		assert x >= 0;
		sizeTo((x >>> wordshift) + 1);
		bits[x >>> wordshift] = bits[x >>> wordshift] &
			~(1 << (x & wordmask));//清位
			
		DEBUG.P("x="+x+" bits["+(x >>> wordshift)+"] = "+bits[x >>> wordshift]);
		DEBUG.P(0,this,"excl(int x)");
    }

    /** Is x an element of this set?
     */
    public boolean isMember(int x) {
    	//因wordlen = 32，wordshift = 5，
    	//所以bits.length << wordshift等价于bits.length * wordlen
        return
            0 <= x && x < (bits.length << wordshift) &&
            (bits[x >>> wordshift] & (1 << (x & wordmask))) != 0;
    }

    /** this set = this set & xs.
     */
    public Bits andSet(Bits xs) {
		sizeTo(xs.bits.length);
		for (int i = 0; i < xs.bits.length; i++)
			bits[i] = bits[i] & xs.bits[i];
		return this;
    }

    /** this set = this set | xs.
     */
    public Bits orSet(Bits xs) {
		sizeTo(xs.bits.length);
		for (int i = 0; i < xs.bits.length; i++)
			bits[i] = bits[i] | xs.bits[i];
		return this;
    }

    /** this set = this set \ xs.
     */
	//集合差运算
    public Bits diffSet(Bits xs) {
		DEBUG.P(this,"diffSet(Bits xs)");
		DEBUG.P("this前="+this);
		DEBUG.P("xs    ="+xs);
		
		for (int i = 0; i < bits.length; i++) {
			if (i < xs.bits.length) {
				DEBUG.P("");
				DEBUG.P("bits["+i+"]前="+bits[i]);
				DEBUG.P("xs.bits["+i+"]="+xs.bits[i]);
				DEBUG.P("~xs.bits["+i+"]="+(~xs.bits[i]));
				bits[i] = bits[i] & ~xs.bits[i];
				DEBUG.P("bits["+i+"]后="+bits[i]);
			}
		}
		DEBUG.P("");
		DEBUG.P("this后="+this);
		DEBUG.P(0,this,"diffSet(Bits xs)");
		return this;
    }

    /** this set = this set ^ xs.
     */
    public Bits xorSet(Bits xs) {
		sizeTo(xs.bits.length);
		for (int i = 0; i < xs.bits.length; i++)
			bits[i] = bits[i] ^ xs.bits[i];
		return this;
    }

    /** Count trailing zero bits in an int. Algorithm from "Hacker's
     *  Delight" by Henry S. Warren Jr. (figure 5-13)
     */
    private static int trailingZeroBits(int x) {
		assert wordlen == 32;
		if (x == 0) return 32;
		int n = 1;
		if ((x & 0xffff) == 0) { n += 16; x >>>= 16; }
		if ((x & 0x00ff) == 0) { n +=  8; x >>>=  8; }
		if ((x & 0x000f) == 0) { n +=  4; x >>>=  4; }
		if ((x & 0x0003) == 0) { n +=  2; x >>>=  2; }
		return n - (x&1);
    }

    /** Return the index of the least bit position >= x that is set.
     *  If none are set, returns -1.  This provides a nice way to iterate
     *  over the members of a bit set:
     *  <pre>
     *  for (int i = bits.nextBit(0); i>=0; i = bits.nextBit(i+1)) ...
     *  </pre>
     */
    /*
    从x这个位置(索引从0开始，包含x)开始查找bit串，
    返回找到的第1个bit位为1的那个bit位在bit串中的索引。
    
    例:bits=(长度=32)10010011100000011010001101000010
    
    如果x=0(也就是从bit串索引为0的位置开始查找),
    则先从bit串“10010011100000011010001101000010”
    最后一位开始查找，因最后一位是0，不是1，所以再往前找，索引为1的bit位是1，
    此时已找到第1个bit位为1的bit位，停止往前查找。
    所以当x=0时，nextBit()返回索引位置为1；
    
    如果x=9(也就是从bit串索引为9的位置开始查找),
    则先从bit串“10010011100000011010001101000010”
    索引为9开始查找(1101000010)，因索引为9的bit位是1，
    此时已找到第1个bit位为1的bit位，停止往前查找。
    所以当x=9时，nextBit()返回索引位置为9；
    
    如果x=17(也就是从bit串索引为17的位置开始查找),
    则先从bit串“10010011100000011010001101000010”
    索引为17开始查找(011010001101000010)，因索引为17的bit位是0，
    不是1，所以再往前找，一直找到索引为23的bit位时，
    才找到第1个bit位为1的bit位，停止往前查找。
    所以当x=17时，nextBit()返回索引位置为23；
    */
    public int nextBit(int x) {
		/*	
		int windex = x >>> wordshift;
		if (windex >= bits.length) return -1;
		int word = bits[windex] & ~((1 << (x & wordmask))-1);
		while (true) {
			if (word != 0)
			return (windex << wordshift) + trailingZeroBits(word);
			windex++;
			if (windex >= bits.length) return -1;
			word = bits[windex];
		}*/
		
		int nextBitIndex=-1;
		try {//我加上的
		DEBUG.P(this,"nextBit(int x)");
		DEBUG.P("x="+x);
		DEBUG.P("bits="+this);

		int windex = x >>> wordshift;
		if (windex >= bits.length) return -1;
		int word = bits[windex] & ~((1 << (x & wordmask))-1);
		while (true) {
			if (word != 0) {
				nextBitIndex=(windex << wordshift) + trailingZeroBits(word);
				return nextBitIndex;
			}
			windex++;
			if (windex >= bits.length) return -1;
			word = bits[windex];
		}
		
		}finally{//我加上的
		DEBUG.P("nextBitIndex="+nextBitIndex);
		DEBUG.P(0,this,"nextBit(int x)");
		}
    }

    /** a string representation of this set.
     */
    public String toString() {
    	/*
    	char[] digits = new char[bits.length * wordlen];
        for (int i = 0; i < bits.length * wordlen; i++)
            digits[i] = isMember(i) ? '1' : '0';
        return new String(digits);
        */
        int len=bits.length * wordlen;
        char[] digits = new char[len];
        for (int i = 0; i < len; i++)
            //digits[i] = isMember(i) ? '1' : '0';//这是按低位在前的顺序排列
            digits[len-i-1] = isMember(i) ? '1' : '0';//这是按高位在前的顺序排列
        
        return "(长度="+digits.length+")"+new String(digits);//我改动了一下
    }

    /** Test Bits.nextBit(int). */
    public static void main(String[] args) {
		java.util.Random r = new java.util.Random();
		Bits bits = new Bits();
		int dupCount = 0;
		for (int i=0; i<125; i++) {
			int k;
			do {
				k = r.nextInt(250);
			} while (bits.isMember(k));
			System.out.println("adding " + k);
			bits.incl(k);
		}
		int count = 0;
		for (int i = bits.nextBit(0); i >= 0; i = bits.nextBit(i+1)) {
			System.out.println("found " + i);
			count ++;
		}
		if (count != 125) throw new Error();
    }
}
