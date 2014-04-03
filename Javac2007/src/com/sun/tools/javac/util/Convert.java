/*
 * @(#)Convert.java	1.25 07/03/21
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

/** Utility class for static conversion methods between numbers
 *  and strings in various formats.
 *
 *  <p><b>This is NOT part of any API supported by Sun Microsystems.  If
 *  you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 */
@Version("@(#)Convert.java	1.25 07/03/21")
public class Convert {
	
    private static my.Debug DEBUG=new my.Debug(my.Debug.Convert);//我加上的
    // <editor-fold defaultstate="collapsed">
    /*注意:
      java语言的“整数字面值(integer literal)”可以用10进制、8进制或者16进制
    表示，如果“整数字面值”没有后缀L(或l)字符，就代表是int类型，否则就是
    long类型。另外，java语言没有无符号数，所以int、long类型代表的整数都是有
    符号数，int类型用32位二进制表示一个有符号整数，最高位是符号位，符号位
    是0，表示是正整数(或0)；符号位是1，表示是负整数(或0)。
      
      “整数字面值”不带后缀L(或l)的情形：
      当用10进制表示一个“整数字面值”时，前缀符"+"、"-"就代表了正、负整数；
    当用8进制或者16进制表示一个“整数字面值”时，得看总的二进制位是否达到了32
    位(一个8进制位对应3个二进制位，一个16进制位对应4个二进制位)，如果二进制位
    总数<32，那么这个“整数字面值”就是正整数(或0)；如果二进制位总数=32，那么
    看最高位，是1就代表负整数(或0)，是0就代表正整数(或0)；如果二进制位总数>32
    去掉前面所有的0后，二进制位总数还是>32，则认为此“整数字面值”超出了int类
    型所能表示的范围。
      值得注意的是:用8进制或者16进制表示的“整数字面值”采用的是补码形式，如果
    “整数字面值”代表的是正整数，那么此正整数的绝对值就是“整数字面值”本身；如果
    “整数字面值”代表的是负整数，那么此负整数的绝对值就是把“整数字面值”的所有位
    都按位取反，然后加1。
    
    例子: 
   ----------------------------------------------------------------------------
    16进制
  “整数字面值”               二进制值			                    10进制值	
   0x64                                           0110 0100(8 位)   100
   0xFFFFFF9C       1111 1111 1111 1111 1111 1111 1001 1100(32位)  -100
   0x00000000       0000 0000 0000 0000 0000 0000 0000 0000(32位)   0
   0x00000001       0000 0000 0000 0000 0000 0000 0000 0001(32位)   1 (最小正整数)
   0x7FFFFFFF       0111 1111 1111 1111 1111 1111 1111 1111(32位)   2147483647 (最大正整数)
   0x80000000       1000 0000 0000 0000 0000 0000 0000 0000(32位)  -2147483648 (最小负整数)
   0xFFFFFFFF       1111 1111 1111 1111 1111 1111 1111 1111(32位)  -1 (最大负整数)
   0x0FFFFFFFF 0000 1111 1111 1111 1111 1111 1111 1111 1111(36位)  -1 (最大负整数)
   0x10FFFFFFF    1 0000 1111 1111 1111 1111 1111 1111 1111(33位)   不合法(过大的整数)
   
     
   
   0xFFFFFF9C       1111 1111 1111 1111 1111 1111 1001 1100(32位)  -100
   所有位取反 :     0000 0000 0000 0000 0000 0000 0110 0011(32位)  
   0x00000001       0000 0000 0000 0000 0000 0000 0000 0001(32位)   1
   加1得绝对值:     0000 0000 0000 0000 0000 0000 0110 0100(32位)   100
   ----------------------------------------------------------------------------      
     
   “整数字面值”带有后缀L(或l)的情形与上面类似，只要把32改成64，把int改成
    long就行了。
    另请参考<<Java Language Specification, Third Edition>> 3.10. Literals 

    */
    // </editor-fold>
    /** Convert string to integer.
     */
    public static int string2int(String s, int radix)
        throws NumberFormatException {
        try {//我加上的
		DEBUG.P(Convert.class,"string2int(String s, int radix)");
		DEBUG.P("radix="+radix+" s="+s);
		//注意:像在“int n = 0”这样的语句中,字面值0是单独
        //出现的,此时把它看成是8进制的0,也就是基数(radix)会是8,而不是10
        
        if (radix == 10) { //10进制
            return Integer.parseInt(s, radix);
        } else {
        	//将8进制或者16进制字面值(Literal)转换成10进制的算法请参考
        	//com.sun.tools.javac.parser.Scanner类convertUnicode()方法中的注释
            char[] cs = s.toCharArray();
            /*
            //8进制或者16进制字面值最大为037777777777或0xffffffff,
            //Integer.MAX_VALUE可以表示为017777777777或0x7fffffff,
            //当radix为8或16时limit的值为003777777777或0x0fffffff,
            //其中003777777777=017777777777>>2(也就是Integer.MAX_VALUE / (8/2))
            //0x0fffffff=0x7fffffff>>3(也就是Integer.MAX_VALUE / (16/2))
            
            对于公式n * radix + d，当radix=8时，用limit=003777777777替换n,用7替换d:
            n * radix + d = 003777777777 * 8 + 7 = 003777777777 << 3 + 7 = 037777777777
            
            如果n>limit，令n=(003777777777 + 1)，代入公式:
            n * radix + d = (003777777777 + 1) * 8 + d
            因d>=0(d不可能小于0,小于0时是非法字符)
                        ==> (003777777777 * 8 + 8 + d) > (003777777777 * 8 + 7 )
                        ==> (003777777777 * 8 + 8 + d) > 037777777777
                        ==> 错误提示(过大的整数)
                        
            当radix=16时与上类似        
            
            如当“整数字面值”=0x180000000时,n>limit
            */
            int limit = Integer.MAX_VALUE / (radix/2);
            int n = 0;
            for (int i = 0; i < cs.length; i++) {
                int d = Character.digit(cs[i], radix);
                DEBUG.P("cs["+i+"]="+cs[i]+"  d="+d+"  limit="+limit+"  n1="+n+"  n2="+(n * radix + d)+"  Integer.MAX_VALUE - d="+(Integer.MAX_VALUE - d));
                if (n < 0 ||
                    n > limit ||
                    n * radix > Integer.MAX_VALUE - d)
                    throw new NumberFormatException();
                    /*注意:
                    n * radix > Integer.MAX_VALUE - d
                    这一条件也是很关键的，当8进制或者16进制字面值(Literal)里的
                    字符不合法时(比如8进制的字面值里出现了'9'这个字符)，在执行
                    完d = Character.digit(cs[i], radix)这条语句后，d的值就变成
                    负值，Integer.MAX_VALUE - d也会跟着变成负值，从而
                    导致n * radix > Integer.MAX_VALUE - d的结果为true，并抛出异常
                    */
                n = n * radix + d;
            }
            return n;
        }
        
        }finally{//我加上的
		DEBUG.P(0,Convert.class,"string2int(String s, int radix)");
		}
    }

    /** Convert string to long integer.
     */
    public static long string2long(String s, int radix)
        throws NumberFormatException {
        if (radix == 10) {
            return Long.parseLong(s, radix);
        } else {
            char[] cs = s.toCharArray();
            long limit = Long.MAX_VALUE / (radix/2);
            long n = 0;
            for (int i = 0; i < cs.length; i++) {
                int d = Character.digit(cs[i], radix);
                if (n < 0 ||
                    n > limit ||
                    n * radix > Long.MAX_VALUE - d)
                    throw new NumberFormatException();
                n = n * radix + d;
            }
            return n;
        }
    }

/* Conversion routines between names, strings, and byte arrays in Utf8 format
 */

    /** Convert `len' bytes from utf8 to characters.
     *  Parameters are as in System.arraycopy
     *  Return first index in `dst' past the last copied char.
     *  @param src        The array holding the bytes to convert.
     *  @param sindex     The start index from which bytes are converted.
     *  @param dst        The array holding the converted characters..
     *  @param dindex     The start index from which converted characters
     *                    are written.
     *  @param len        The maximum number of bytes to convert.
     */
    public static int utf2chars(byte[] src, int sindex,
                                char[] dst, int dindex,
                                int len) {
        int i = sindex;
        int j = dindex;
        int limit = sindex + len;
        while (i < limit) {
            int b = src[i++] & 0xFF;
            if (b >= 0xE0) {
                b = (b & 0x0F) << 12;
                b = b | (src[i++] & 0x3F) << 6;
                b = b | (src[i++] & 0x3F);
            } else if (b >= 0xC0) {
                b = (b & 0x1F) << 6;
                b = b | (src[i++] & 0x3F);
            }
            dst[j++] = (char)b;
        }
        return j;
    }

    /** Return bytes in Utf8 representation as an array of characters.
     *  @param src        The array holding the bytes.
     *  @param sindex     The start index from which bytes are converted.
     *  @param len        The maximum number of bytes to convert.
     */
    public static char[] utf2chars(byte[] src, int sindex, int len) {
        char[] dst = new char[len];
        int len1 = utf2chars(src, sindex, dst, 0, len);
        char[] result = new char[len1];
        System.arraycopy(dst, 0, result, 0, len1);
        return result;
    }

    /** Return all bytes of a given array in Utf8 representation
     *  as an array of characters.
     *  @param src        The array holding the bytes.
     */
    public static char[] utf2chars(byte[] src) {
        return utf2chars(src, 0, src.length);
    }

    /** Return bytes in Utf8 representation as a string.
     *  @param src        The array holding the bytes.
     *  @param sindex     The start index from which bytes are converted.
     *  @param len        The maximum number of bytes to convert.
     */
    public static String utf2string(byte[] src, int sindex, int len) {
        char dst[] = new char[len];
        int len1 = utf2chars(src, sindex, dst, 0, len);
        return new String(dst, 0, len1);
    }

    /** Return all bytes of a given array in Utf8 representation
     *  as a string.
     *  @param src        The array holding the bytes.
     */
    public static String utf2string(byte[] src) {
        return utf2string(src, 0, src.length);
    }

    /** Copy characters in source array to bytes in target array,
     *  converting them to Utf8 representation.
     *  The target array must be large enough to hold the result.
     *  returns first index in `dst' past the last copied byte.
     *  @param src        The array holding the characters to convert.
     *  @param sindex     The start index from which characters are converted.
     *  @param dst        The array holding the converted characters..
     *  @param dindex     The start index from which converted bytes
     *                    are written.
     *  @param len        The maximum number of characters to convert.
     */
    public static int chars2utf(char[] src, int sindex,
                                byte[] dst, int dindex,
                                int len) {
        int j = dindex;
        int limit = sindex + len;
        for (int i = sindex; i < limit; i++) {
            char ch = src[i];
            //在<<深入java虚拟机>>P130页有说到
            if (1 <= ch && ch <= 0x7F) {
                dst[j++] = (byte)ch;
            } else if (ch <= 0x7FF) {
                dst[j++] = (byte)(0xC0 | (ch >> 6));
                dst[j++] = (byte)(0x80 | (ch & 0x3F));
            } else {
                dst[j++] = (byte)(0xE0 | (ch >> 12));
                dst[j++] = (byte)(0x80 | ((ch >> 6) & 0x3F));
                dst[j++] = (byte)(0x80 | (ch & 0x3F));
            }
        }
        return j;
    }

    /** Return characters as an array of bytes in Utf8 representation.
     *  @param src        The array holding the characters.
     *  @param sindex     The start index from which characters are converted.
     *  @param len        The maximum number of characters to convert.
     */
    public static byte[] chars2utf(char[] src, int sindex, int len) {
        byte[] dst = new byte[len * 3];
        int len1 = chars2utf(src, sindex, dst, 0, len);
        byte[] result = new byte[len1];
        System.arraycopy(dst, 0, result, 0, len1);
        return result;
    }

    /** Return all characters in given array as an array of bytes
     *  in Utf8 representation.
     *  @param src        The array holding the characters.
     */
    public static byte[] chars2utf(char[] src) {
        return chars2utf(src, 0, src.length);
    }

    /** Return string as an array of bytes in in Utf8 representation.
     */
    public static byte[] string2utf(String s) {
        return chars2utf(s.toCharArray());
    }

    /**
     * Escapes each character in a string that has an escape sequence or
     * is non-printable ASCII.  Leaves non-ASCII characters alone.
     */
    public static String quote(String s) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            buf.append(quote(s.charAt(i)));
        }
        return buf.toString();
    }

    /**
     * Escapes a character if it has an escape sequence or is
     * non-printable ASCII.  Leaves non-ASCII characters alone.
     */
    public static String quote(char ch) {
        switch (ch) {
        case '\b':  return "\\b";
        case '\f':  return "\\f";
        case '\n':  return "\\n";
        case '\r':  return "\\r";
        case '\t':  return "\\t";
        case '\'':  return "\\'";
        case '\"':  return "\\\"";
        case '\\':  return "\\\\";
        default:
            return (ch > 127 || isPrintableAscii(ch))
                ? String.valueOf(ch)
                : String.format("\\%03o", (int) ch);
        }
    }

    /**
     * Is a character printable ASCII?
     */
    private static boolean isPrintableAscii(char ch) {
        return ch >= ' ' && ch <= '~';
    }

    /** Escape all unicode characters in string.
     */
    public static String escapeUnicode(String s) {
        int len = s.length();
        int i = 0;
        while (i < len) {
            char ch = s.charAt(i);
            if (ch > 255) {
                StringBuffer buf = new StringBuffer();
                buf.append(s.substring(0, i));
                while (i < len) {
                    ch = s.charAt(i);
                    if (ch > 255) {
                        buf.append("\\u");
                        buf.append(Character.forDigit((ch >> 12) % 16, 16));
                        buf.append(Character.forDigit((ch >>  8) % 16, 16));
                        buf.append(Character.forDigit((ch >>  4) % 16, 16));
                        buf.append(Character.forDigit((ch      ) % 16, 16));
                    } else {
                        buf.append(ch);
                    }
                    i++;
                }
                s = buf.toString();
            } else {
                i++;
            }
        }
        return s;
    }

/* Conversion routines for qualified name splitting
 */
    /** Return the last part of a class name.
     */
    public static Name shortName(Name classname) {
        return classname.subName(
            classname.lastIndexOf((byte)'.') + 1, classname.len);
    }

    public static String shortName(String classname) {
        return classname.substring(classname.lastIndexOf('.') + 1);
    }

    /** Return the package name of a class name, excluding the trailing '.',
     *  "" if not existent.
     */
    public static Name packagePart(Name classname) {
        return classname.subName(0, classname.lastIndexOf((byte)'.'));
    }

    public static String packagePart(String classname) {
        int lastDot = classname.lastIndexOf('.');
        return (lastDot < 0 ? "" : classname.substring(0, lastDot));
    }
    
    //如“name1$name2$name3”按“＄”符号分开为:name1 name2 name3
    public static List<Name> enclosingCandidates(Name name) {
        List<Name> names = List.nil();
        int index;
        while ((index = name.lastIndexOf((byte)'$')) > 0) {
            name = name.subName(0, index);
            names = names.prepend(name);
        }
        return names;
    }
}
