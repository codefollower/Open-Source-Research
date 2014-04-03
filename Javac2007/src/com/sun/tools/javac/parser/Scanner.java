/*
 * @(#)Scanner.java	1.75 07/03/21
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

package com.sun.tools.javac.parser;

import java.io.*;
import java.nio.*;
import java.nio.ByteBuffer;
import java.nio.charset.*;
import java.nio.channels.*;
import java.util.regex.*;

import com.sun.tools.javac.util.*;

import com.sun.tools.javac.code.Source;

import static com.sun.tools.javac.parser.Token.*;
import static com.sun.tools.javac.util.LayoutCharacters.*;

/** The lexical analyzer maps an input stream consisting of
 *  ASCII characters and Unicode escapes into a token sequence.
 *
 *  <p><b>This is NOT part of any API supported by Sun Microsystems.  If
 *  you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 */
@Version("@(#)Scanner.java	1.75 07/03/21")
public class Scanner implements Lexer {
	
    private static my.Debug DEBUG=new my.Debug(my.Debug.Scanner);//我加上的
	
    //源码中原来是false
    private static boolean scannerDebug = my.Debug.DocCommentScanner;
    //private static boolean scannerDebug = true;
    
    //private static boolean scannerDebug = false;

    /** A factory for creating scanners. */
    public static class Factory {
	/** The context key for the scanner factory. */
	public static final Context.Key<Scanner.Factory> scannerFactoryKey =
	    new Context.Key<Scanner.Factory>();

	/** Get the Factory instance for this context. */
	public static Factory instance(Context context) {
            try {//我加上的
            DEBUG.P(Factory.class,"instance(1)");
            
            //如果JavaCompiler.processAnnotations=true时，
            //则instance是DocCommentScanner.Factory类的实例
	    Factory instance = context.get(scannerFactoryKey);
            DEBUG.P("instance="+instance);
	    if (instance == null)
		instance = new Factory(context);
	    return instance;
            
            }finally{//我加上的
            DEBUG.P(0,Factory.class,"instance(1)");
            }
	}

	final Log log;
	final Name.Table names;
	final Source source;
	final Keywords keywords;

	/** Create a new scanner factory. */
	protected Factory(Context context) {
            DEBUG.P(this,"Factory(1)");
            
	    context.put(scannerFactoryKey, this);
	    this.log = Log.instance(context);
	    this.names = Name.Table.instance(context);
	    this.source = Source.instance(context);
	    this.keywords = Keywords.instance(context);
            
	    DEBUG.P(0,this,"Factory(1)");
	}

        public Scanner newScanner(CharSequence input) {
        	try {//我加上的
        	DEBUG.P(this,"newScanner(1)");
        	//DEBUG.P("input instanceof CharBuffer="+(input instanceof CharBuffer));
        	/*
        	为什么要(input instanceof CharBuffer)呢？
        	因为每个要编译的源文件都被“包装”成一
        	个JavacFileManager.RegularFileObject类的实例 ,
        	RegularFileObject类实现了JavaFileObject接口,JavaFileObject接口的
        	超级接口是FileObject，在FileObject接口中有一个方法(用于读取文件内容):
        	java.lang.CharSequence getCharContent(boolean ignoreEncodingErrors)
                                      throws java.io.IOException
                                      
            而JavacFileManager.RegularFileObject类对应的实现方法为:
            public java.nio.CharBuffer getCharContent(boolean ignoreEncodingErrors)
                                   throws java.io.IOException
                                   
            比较两个方法的返回值，初看可能觉得有点怪，其实这是合法的，
            因为java.nio.CharBuffer类实现了java.lang.CharSequence接口                   
        	*/
            if (input instanceof CharBuffer) {
                return new Scanner(this, (CharBuffer)input);
            } else {
                char[] array = input.toString().toCharArray();
                return newScanner(array, array.length);
            }
            
            }finally{//我加上的
            DEBUG.P(0,this,"newScanner(1)");
            }
        }

        public Scanner newScanner(char[] input, int inputLength) {
            try {//我加上的
            DEBUG.P(this,"newScanner(2)");
            
            return new Scanner(this, input, inputLength);
            
            }finally{//我加上的
            DEBUG.P(0,this,"newScanner(2)");
            }
        }
    }

    /* Output variables; set by nextToken():
     */

    /** The token, set by nextToken().
     */
    private Token token;

    /** Allow hex floating-point literals.
     */
    private boolean allowHexFloats;

    /** The token's position, 0-based offset from beginning of text.
     */
    private int pos;

    /** Character position just after the last character of the token.
     */
    private int endPos;

    /** The last character position of the previous token.
     */
    private int prevEndPos;
    
    /*举例说明:pos，endPos，prevEndPos这三者的区别
    例如要编译的源代码开头如下：
    package my.test;
    
    开启scannerDebug=true后会有如下输出:
    nextToken(0,7)=|package|  	tokenName=PACKAGE|  	prevEndPos=0
    processWhitespace(7,8)=| |
	nextToken(8,10)=|my|   		tokenName=IDENTIFIER|  	prevEndPos=7
	nextToken(10,11)=|.|  		tokenName=DOT|  		prevEndPos=10
	nextToken(11,15)=|test|  	tokenName=IDENTIFIER|  	prevEndPos=11
	nextToken(15,16)=|;|  		tokenName=SEMI|  		prevEndPos=15
	
	其中的(0,7)、(8,10)、(10,11)、(11,15)、(15,16)都是代表(pos,endPos)，
	endPos所代表的位置上的字符并不是当前Token的最后一个字符，而是下一
	个Token的起始字符或者空白、换行、注释文档符等。
	
	另外，prevEndPos总是指向前一个Token的endPos，prevEndPos并不指向
	空白、换行、注释文档的endPos，
	如processWhitespace(7,8)的endPos是8，但是此时prevEndPos=7
	*/


    /** The position where a lexical error occurred;
     */
    private int errPos = Position.NOPOS;

    /** The name of an identifier or token:
     */
    private Name name;

    /** The radix of a numeric literal token.
     */
    private int radix;

    /** Has a @deprecated been encountered in last doc comment?
     *  this needs to be reset by client.
     */
    protected boolean deprecatedFlag = false;

    /** A character buffer for literals.
     */
    private char[] sbuf = new char[128];//字符缓存，会经常变更
    private int sp;

    /** The input buffer, index of next chacter to be read,
     *  index of one past last character in buffer.
     */
    private char[] buf;//存放源方件内容
    private int bp;
    private int buflen;
    private int eofPos;

    /** The current character.
     */
    private char ch;

    /** The buffer index of the last converted unicode character
     */
    private int unicodeConversionBp = -1;

    /** The log to be used for error reporting.
     */
    private final Log log;

    /** The name table. */
    private final Name.Table names;

    /** The keyword table. */
    private final Keywords keywords;

    /** Common code for constructors. */
    private Scanner(Factory fac) {
	this.log = fac.log;
	this.names = fac.names;
	this.keywords = fac.keywords;
	//16进制浮点数只有>=JDK1.5才可以用
	this.allowHexFloats = fac.source.allowHexFloats();
    }

    private static final boolean hexFloatsWork = hexFloatsWork();
    private static boolean hexFloatsWork() {
        try {
            Float.valueOf("0x1.0p1");
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    /** Create a scanner from the input buffer.  buffer must implement
     *  array() and compact(), and remaining() must be less than limit().
     */
    protected Scanner(Factory fac, CharBuffer buffer) {
	this(fac, JavacFileManager.toArray(buffer), buffer.limit());
    }

    /**
     * Create a scanner from the input array.  This method might
     * modify the array.  To avoid copying the input array, ensure
     * that {@code inputLength < input.length} or
     * {@code input[input.length -1]} is a white space character.
     * 
     * @param fac the factory which created this Scanner
     * @param input the input, might be modified
     * @param inputLength the size of the input.
     * Must be positive and less than or equal to input.length.
     */
    protected Scanner(Factory fac, char[] input, int inputLength) {
        this(fac);

        DEBUG.P(this,"Scanner(3) 源文件内容预览......");
		//2009-3-14修改，下在的解释不完全正确，请看B50版中的注释：
		//在com.sun.tools.javac.file.JavacFileManager===>toArray(1)


        //input字符数组中存放的内容与源文件不完全一样，比源文件多了10个
        //null字符(10进制等于0),是在源文件后面加上的,
        //inputLength是源文件内容的长度,input.length一般等于inputLength+10
    	//DEBUG.P(new String(input)+"");
    	//DEBUG.P("---------------------------");
    	//DEBUG.P("buffer.limit="+inputLength);
    	//DEBUG.P("input.length="+input.length);
        
        DEBUG.P("源文件长度="+inputLength);
    	DEBUG.P("调整后长度="+input.length);
    	
        eofPos = inputLength;
        //这种情况只是为了方便在buf最后放入EOI而进行的特殊处理
	//如果char[] input不是由CharBuffer buffer转换而来的就会有inputLength == input.length
        if (inputLength == input.length) {
                        //查看java.lang.Character的isWhitespace()方法,null字符会返回false
            if (input.length > 0 && Character.isWhitespace(input[input.length - 1])) {
                inputLength--;
            } else {
                char[] newInput = new char[inputLength + 1];
                System.arraycopy(input, 0, newInput, 0, input.length);
                input = newInput;
            }
        }
        buf = input;
        buflen = inputLength;
        buf[buflen] = EOI;//EOI在com.sun.tools.javac.util.LayoutCharacters定义
        bp = -1;
        scanChar();

        DEBUG.P("scan first char="+ch);
	DEBUG.P("bp="+bp);
	DEBUG.P("endPos="+endPos);
	DEBUG.P("errPos="+errPos);
	DEBUG.P("pos="+pos);
	DEBUG.P("prevEndPos="+prevEndPos);
	DEBUG.P("sp="+sp);
	DEBUG.P("unicodeConversionBp="+unicodeConversionBp); 
        DEBUG.P(0,this,"Scanner(3)");
    }

    /** Report an error at the given position using the provided arguments.
     */
    private void lexError(int pos, String key, Object... args) {
	DEBUG.P(this,"lexError(3)");
    	DEBUG.P("key="+key);
        DEBUG.P("pos="+pos+" bp="+bp);

        log.error(pos, key, args);
        token = ERROR;
        errPos = pos;

        DEBUG.P(0,this,"lexError(3)");
    }

    /** Report an error at the current token position using the provided
     *  arguments.
     */
    private void lexError(String key, Object... args) {
        DEBUG.P(this,"lexError(2)");
    	DEBUG.P("key="+key);
        DEBUG.P("pos="+pos+" bp="+bp);
        if(args!=null && args.length>0) DEBUG.P("args[0]="+args[0]);
        
	lexError(pos, key, args);
        
        DEBUG.P(0,this,"lexError(2)");
    }

    /** Convert an ASCII digit from its base (8, 10, or 16)
     *  to its value.
     */
    private int digit(int base) {//如16进制的A会转换成10
	char c = ch;
	int result = Character.digit(c, base);
	if (result >= 0 && c > 0x7f) {
		//如:int aaa= 12\u06604; //非法的非 ASCII 数字
		//见Character类的isDigit方法
	    lexError(pos+1, "illegal.nonascii.digit");
	    ch = "0123456789abcdef".charAt(result);
	}
	return result;
    }

    /** Convert unicode escape; bp points to initial '\' character
     *  (Spec 3.3).
     */
    private void convertUnicode() {
	//ch所代表的字符就是buf[bp]
        try {//我加上的
	DEBUG.P(this,"convertUnicode()");
        DEBUG.P("ch="+ch+" bp="+bp+" unicodeConversionBp="+unicodeConversionBp);
        //注意，因为'\\'也可以用'\\u005C'表示(也就是斜线的Unicode码是005C)，
	//所以像'\\u005Cu0012'就不代表\\u0012了，这时先解析'\\u005C'，使得
	//ch='\\'且unicodeConversionBp = bp
	if (ch == '\\' && unicodeConversionBp != bp) {
	    bp++; ch = buf[bp];
	   	/*
	    (注:注释里的\\u必须有两个\，如果只有一个\，则unicode字符处理规则与在其它地方一样
	    unicode字符只能是以\\u开头,不能以\\U(大写的U)开头
	    */
	    if (ch == 'u') {//在\后面可以接不只一个u
		do {
		    bp++; ch = buf[bp];
		} while (ch == 'u');
		//每一个unicode占4个16进制字符，
		//因为退了while时已读了一个，所以只加3
		int limit = bp + 3;
		if (limit < buflen) {
		    int d = digit(16);
		    int code = d;
		    while (bp < limit && d >= 0) {
			bp++; ch = buf[bp];
			d = digit(16);
			DEBUG.P("d1="+d);
			code = (code << 4) + d;
			//从高位到低位依次计算10进制值,
			//因为一个16进制字符用4个二进制字符表示，所以每次左移4位，
			//相当于10进制值每次乘以16
			/*
			举例:
			unicod码:   \uA971
			10进制码:   10*16*16*16 + 9*16*16 + 7*16 + 1
			            =(10*16 + 9)*16*16 + 7*16 + 1
			            =((10*16 + 9)*16 + 7)*16 + 1
			            =((10<<4 + 9)<<4 + 7)<<4 + 1
			            
			正好对应公式:(code << 4) + d;
			*/
		    }
		    DEBUG.P("d2="+d);
		    if (d >= 0) {
			ch = (char)code;
			unicodeConversionBp = bp;
			return;
		    }
		}
			//非法的 Unicode 转义,如下面的g是非法的
			//public int myInt='\\uuuuugfff';
            //                         ^  
		lexError(bp, "illegal.unicode.esc");
	    } else {
	    //如果'\'字符后面不是'u'，说明不是Unicode，往后退一位
		bp--;
		ch = '\\';
	    }
	}
        
        }finally{//我加上的
        DEBUG.P(0,this,"convertUnicode()");
        } 
    }

    /** Read next character.
     */
    private void scanChar() {
        //try {//我加上的
	//DEBUG.P(this,"scanChar()");
        
	ch = buf[++bp];
	if (ch == '\\') {
	    convertUnicode();
	}
        
        //}finally{//我加上的
        //DEBUG.P(0,this,"scanChar()");
        //}
    }

    /** Read next character in comment, skipping over double '\' characters.
     */
    private void scanCommentChar() {
	//DEBUG.P(this,"scanCommentChar()");
	scanChar();
	//DEBUG.P("ch="+ch);
	//DEBUG.P("bp="+bp);
	//DEBUG.P("unicodeConversionBp="+unicodeConversionBp);
	if (ch == '\\') {
	    if (buf[bp+1] == '\\' && unicodeConversionBp != bp) {
		bp++;
	    } else {
		convertUnicode();
	    }
	}
	//DEBUG.P(0,this,"scanCommentChar()");
    }

    /** Append a character to sbuf.
     */
    private void putChar(char ch) {
	if (sp == sbuf.length) {
	    char[] newsbuf = new char[sbuf.length * 2];
	    System.arraycopy(sbuf, 0, newsbuf, 0, sbuf.length);
	    sbuf = newsbuf;
	}
	sbuf[sp++] = ch;
    }

    /** For debugging purposes: print character.
     */
    private void dch() {
        System.err.print(ch); System.out.flush();
    }

    /** Read next character in character or string literal and copy into sbuf.
     */
    private void scanLitChar() {
        if (ch == '\\') {
	    if (buf[bp+1] == '\\' && unicodeConversionBp != bp) {
		bp++;
		putChar('\\');
		scanChar();
	    } else {
		scanChar();
		switch (ch) {
		case '0': case '1': case '2': case '3':
		case '4': case '5': case '6': case '7':
		    char leadch = ch;
		    int oct = digit(8);
		    scanChar();
		    if ('0' <= ch && ch <= '7') {
			oct = oct * 8 + digit(8);
			scanChar();
			//用\表示8进制的字符时，当8进制的字符占3位时，为何第一位leadch <= '3' ????
			//(2007-06-15 10:37解决这问题)
			//见JLS3 3.10.6. Escape Sequences for Character and String Literals
			//用\表示8进制的字符时，只能表示\u0000 到 \u00ff的字符
			//\377刚好对应\u00ff
			if (leadch <= '3' && '0' <= ch && ch <= '7') {
			    oct = oct * 8 + digit(8);
			    scanChar();
			}
		    }
		    putChar((char)oct);
		    break;
		case 'b':
		    putChar('\b'); scanChar(); break;
		case 't':
		    putChar('\t'); scanChar(); break;
		case 'n':
		    putChar('\n'); scanChar(); break;
		case 'f':
		    putChar('\f'); scanChar(); break;
		case 'r':
		    putChar('\r'); scanChar(); break;
		case '\'':
		    putChar('\''); scanChar(); break;
		case '\"':
		    putChar('\"'); scanChar(); break;
		case '\\':
		    putChar('\\'); scanChar(); break;
		default:
                    //非法转义字符 例如：char c='\w';
 		    lexError(bp, "illegal.esc.char");
		}
	    }
	} else if (bp != buflen) {
            putChar(ch); scanChar();
        }
    }

    /** Read fractional part of hexadecimal floating point number.
     */
    private void scanHexExponentAndSuffix() {
    	//16进制浮点指数部分(注:p(或P)后面是指数,不能省略,如果是float类型则f(或F)也是必须的)
        if (ch == 'p' || ch == 'P') {
            // <editor-fold defaultstate="collapsed">
            putChar(ch);
            scanChar();
            
            if (ch == '+' || ch == '-') {
                putChar(ch);
                scanChar();
            }

            if ('0' <= ch && ch <= '9') {
                do {
                    putChar(ch);
                    scanChar();
                } while ('0' <= ch && ch <= '9');

                if (!allowHexFloats) {
                    //例子:0x.1p-1f，当指定选项:-source 1.4时
                    //报错:在 -source 5 之前，不支持十六进制浮点字面值
                    lexError("unsupported.fp.lit");
                    allowHexFloats = true;
                }
                else if (!hexFloatsWork)
                    //该 VM 不支持十六进制浮点字面值
                    lexError("unsupported.cross.fp.lit");
            } else
                //如:0x.1p-wf，字符w不是数字0-9，编译器报错:浮点字面值不规则
                //但如果是:0x.1p-2wf，虽然字符w不是数字0-9，但不在这里报错
                //这里只检查+-号后面的字符是否是数字
                lexError("malformed.fp.lit");
            // </editor-fold>
        } else {
            //如:0x.1-1f，少了字符p(或P)，编译器报错:浮点字面值不规则
            lexError("malformed.fp.lit");
        }
        if (ch == 'f' || ch == 'F') {
            putChar(ch);
            scanChar();
            token = FLOATLITERAL;
        } else {
            /*
            如果浮点数后没有指定后缀f(或F)，那么都把它当成是双精度的，
            这时如果把它赋值给一个float类型的字段，编译器在其他地方(Check类中)会检查，
            如：public float myFloat2=0x.1p-2;

            错误提示如下:

            bin\mysrc\my\test\ScannerTest.java:9: 可能损失精度
            找到： double
            需要： float
                            public float myFloat2=0x.1p-2;
                                                                      ^
            1 错误
            */
            if (ch == 'd' || ch == 'D') {
                putChar(ch);
                scanChar();
            }
            token = DOUBLELITERAL;
        }
    }

    /** Read fractional part of floating point number.
     */
    private void scanFraction() {
        while (digit(10) >= 0) {
            putChar(ch);
            scanChar();
        }
        
        int sp1 = sp;
        if (ch == 'e' || ch == 'E') {
            putChar(ch);
            scanChar();
            if (ch == '+' || ch == '-') {
                putChar(ch);
                scanChar();
            }
	   		
            if ('0' <= ch && ch <= '9') {
                do {
                    putChar(ch);
                    scanChar();
                } while ('0' <= ch && ch <= '9');
                return;
            }
            //如:1.2E+w，字符w不是数字0-9，编译器报错:浮点字面值不规则
            lexError("malformed.fp.lit");
            sp = sp1;
        }
    }

    /** Read fractional part and 'd' or 'f' suffix of floating point number.
     */
    private void scanFractionAndSuffix() {
	this.radix = 10;
	scanFraction();
	if (ch == 'f' || ch == 'F') {
	    putChar(ch);
	    scanChar();
            token = FLOATLITERAL;
	} else {
	    if (ch == 'd' || ch == 'D') {
		putChar(ch);
		scanChar();
	    }
	    token = DOUBLELITERAL;
	}
    }

    /** Read fractional part and 'd' or 'f' suffix of floating point number.
     */
    private void scanHexFractionAndSuffix(boolean seendigit) {
	this.radix = 16;
	assert ch == '.';
	putChar(ch);
	scanChar();
        //DEBUG.P("ch="+ch+" digit(16)="+digit(16));
        while (digit(16) >= 0) {
	    seendigit = true;
	    putChar(ch);
            scanChar();
        }
	if (!seendigit)
	    lexError("invalid.hex.number");//十六进制数字必须包含至少一位十六进制数,错例如:0x.p-1f;
	else
	    scanHexExponentAndSuffix();
    }

    /** Read a number.
     *  @param radix  The radix of the number; one of 8, 10, 16.
     */
     //在词法分析阶段并不检查数字是否合法，而是在Parser的literal方法中检查
     //例如用8进制表示的数int i=078;
    private void scanNumber(int radix) {
	this.radix = radix;
	// for octal, allow base-10 digit in case it's a float literal
	int digitRadix = (radix <= 10) ? 10 : 16;
	boolean seendigit = false;
	while (digit(digitRadix) >= 0) {
	    seendigit = true;
	    putChar(ch);
	    scanChar();
	}
	if (radix == 16 && ch == '.') {
	    scanHexFractionAndSuffix(seendigit);
	} else if (seendigit && radix == 16 && (ch == 'p' || ch == 'P')) {
            //如:0x1p-1f的情况
	    scanHexExponentAndSuffix();
	} else if (radix <= 10 && ch == '.') {
            //浮点数可以像这样float f=00001.2f;(可以有多个0开头)
	    putChar(ch);
	    scanChar();
	    scanFractionAndSuffix();
	} else if (radix <= 10 &&
		   (ch == 'e' || ch == 'E' ||
		    ch == 'f' || ch == 'F' ||
		    ch == 'd' || ch == 'D')) {
		//如: 2e2f、2f、2d
	    scanFractionAndSuffix();
	} else {
	    if (ch == 'l' || ch == 'L') {
		scanChar();
		token = LONGLITERAL;
	    } else {
		token = INTLITERAL;
	    }
	}
    }

    /** Read an identifier.
     */
    private void scanIdent() {
	    DEBUG.P("scanIdent()=>ch="+ch);
	boolean isJavaIdentifierPart;
	char high;
	do {
            //每次都用if判断一下比每次都调用putChar(ch)效率更高
	    if (sp == sbuf.length) putChar(ch); else sbuf[sp++] = ch;
	    // optimization, was: putChar(ch);

	    scanChar();
	    DEBUG.P("ch="+ch);
	    switch (ch) {
	    case 'A': case 'B': case 'C': case 'D': case 'E':
	    case 'F': case 'G': case 'H': case 'I': case 'J':
	    case 'K': case 'L': case 'M': case 'N': case 'O':
	    case 'P': case 'Q': case 'R': case 'S': case 'T':
	    case 'U': case 'V': case 'W': case 'X': case 'Y':
	    case 'Z':
	    case 'a': case 'b': case 'c': case 'd': case 'e':
	    case 'f': case 'g': case 'h': case 'i': case 'j':
	    case 'k': case 'l': case 'm': case 'n': case 'o':
	    case 'p': case 'q': case 'r': case 's': case 't':
	    case 'u': case 'v': case 'w': case 'x': case 'y':
	    case 'z':
	    case '$': case '_':
	    case '0': case '1': case '2': case '3': case '4':
	    case '5': case '6': case '7': case '8': case '9':
            case '\u0000': case '\u0001': case '\u0002': case '\u0003':
            case '\u0004': case '\u0005': case '\u0006': case '\u0007':
            case '\u0008': case '\u000E': case '\u000F': case '\u0010':
            case '\u0011': case '\u0012': case '\u0013': case '\u0014':
            case '\u0015': case '\u0016': case '\u0017':
            case '\u0018': case '\u0019': case '\u001B':
            case '\u007F':
		break;
            case '\u001A': // EOI is also a legal identifier part
	    //当源文件只有一行“int a\u001A”时
	    /*
		scanIdent()=>ch=a
		com.sun.tools.javac.parser.DocCommentScanner===>convertUnicode()
		-------------------------------------------------------------------------
		ch=\ bp=5 unicodeConversionBp=-1
		d1=0
		d1=1
		d1=10
		d2=10
		com.sun.tools.javac.parser.DocCommentScanner===>convertUnicode()  END
		-------------------------------------------------------------------------

		ch=
		bp >= buflen=false
		ch=
		bp >= buflen=true
		name=a
		token=IDENTIFIER
		nextToken(4,11)=|a\u001A|  tokenName=|IDENTIFIER|  prevEndPos=3
		nextToken(11,11)=||  tokenName=|EOF|  prevEndPos=11
	*/
	    DEBUG.P("bp >= buflen="+(bp >= buflen));
                if (bp >= buflen) {
                    name = names.fromChars(sbuf, 0, sp);
                    token = keywords.key(name);
		    DEBUG.P("name="+name);
                    DEBUG.P("token="+token);
                    return;
                }
                break;
	    default:
                if (ch < '\u0080') {
                    // all ASCII range chars already handled, above
                    isJavaIdentifierPart = false;
                } else {//处理例如中文变量的情况，中文变量不一定是HighSurrogate
		    high = scanSurrogates();
                    if (high != 0) {
	                if (sp == sbuf.length) {
                            putChar(high);
                        } else {
                            sbuf[sp++] = high;
                        }
                        isJavaIdentifierPart = Character.isJavaIdentifierPart(
                            Character.toCodePoint(high, ch));
                    } else {
                        isJavaIdentifierPart = Character.isJavaIdentifierPart(ch);
                    }
                }
                //如果isJavaIdentifierPart为false，代表标识符识别结束
		if (!isJavaIdentifierPart) {
                    //标识符识别后会存入name表中
		    name = names.fromChars(sbuf, 0, sp);
		    token = keywords.key(name);
                    DEBUG.P("name="+name);
                    DEBUG.P("token="+token);
		    return;
		}
	    }
	} while (true);
    }

    /** Are surrogates supported?
     */
    final static boolean surrogatesSupported = surrogatesSupported();
    private static boolean surrogatesSupported() {
        try {
            Character.isHighSurrogate('a');
            return true;
        } catch (NoSuchMethodError ex) {
            return false;
        }
    }

    /** Scan surrogate pairs.  If 'ch' is a high surrogate and
     *  the next character is a low surrogate, then put the low
     *  surrogate in 'ch', and return the high surrogate.
     *  otherwise, just return 0.
     */
    //上面的注释不全，完整如下
    //如果当前的ch不是HighSurrogate，返回0，
    //如果当前的ch是HighSurrogate，接着判断下一个字符是否是LowSurrogate，
    //是的话就返回high，不是的话将ch设为high，最后返回0
    private char scanSurrogates() {
        try {//我加上的
        DEBUG.P(this,"scanSurrogates()");
        DEBUG.P("surrogatesSupported="+surrogatesSupported);
    	DEBUG.P("ch="+ch+"(0x"+Integer.toHexString(ch).toUpperCase()+")");
        DEBUG.P("Character.isHighSurrogate(ch)="+Character.isHighSurrogate(ch));
        DEBUG.P("Character.isJavaIdentifierStart(ch)="+Character.isJavaIdentifierStart(ch));
        DEBUG.P("Character.isJavaIdentifierPart(ch)="+Character.isJavaIdentifierPart(ch));
        
        if (surrogatesSupported && Character.isHighSurrogate(ch)) {
            char high = ch;

            scanChar();
            
            DEBUG.P("next ch="+ch+"(0x"+Integer.toHexString(ch).toUpperCase()+")");
            DEBUG.P("Character.isLowSurrogate(ch)="+Character.isLowSurrogate(ch));

            if (Character.isLowSurrogate(ch)) {
                return high;
            }

            ch = high;
        }

        return 0;
        
        } finally {
        DEBUG.P(0,this,"scanSurrogates()");
        }
    }

    /** Return true if ch can be part of an operator.
     */
    private boolean isSpecial(char ch) {
        switch (ch) {
        case '!': case '%': case '&': case '*': case '?':
        case '+': case '-': case ':': case '<': case '=':
        case '>': case '^': case '|': case '~':
	case '@':
            return true;
        default:
            return false;
        }
    }

    /** Read longest possible sequence of special characters and convert
     *  to token.
     */
    private void scanOperator() {
	while (true) {
	    putChar(ch);
	    Name newname = names.fromChars(sbuf, 0, sp);
	    
	    //DEBUG.P("newname="+newname);
	    //如果一个字符能作为一个完整的操作符的一部分，尽可能的把它加到操作符中，
	    //如果最近加入的字符使得原来的操作符变成了一个标识符了，那么往后退一格
            //如:假设先前读到的操作符为“!="，接着读进字符“*”变成了“!=*"，成了一
            //个标识符(IDENTIFIER)了，这时就得往后退一格，还原成“!="
            if (keywords.key(newname) == IDENTIFIER) {
                sp--;
                break;
	    }
	    
            name = newname;
            token = keywords.key(newname);
	    scanChar();
	    if (!isSpecial(ch)) break;
	}
    }

    /**
     * Scan a documention comment; determine if a deprecated tag is present.
     * Called once the initial /, * have been skipped, positioned at the second *
     * (which is treated as the beginning of the first line).
     * Stops positioned at the closing '/'.
     */
    @SuppressWarnings("fallthrough")
    private void scanDocComment() {
        try {//我加上的
	DEBUG.P(this,"scanDocComment()");
	DEBUG.P("ch="+ch+" bp="+bp+" buflen="+buflen+" buf["+bp+"]="+buf[bp]);
        
	boolean deprecatedPrefix = false;

	forEachLine:
	while (bp < buflen) {

	    // Skip optional WhiteSpace at beginning of line
	    while (bp < buflen && (ch == ' ' || ch == '\t' || ch == FF)) {
		scanCommentChar();
	    }

	    // Skip optional consecutive Stars
	    while (bp < buflen && ch == '*') {
		scanCommentChar();
		if (ch == '/') {
		    return;
		}
	    }
	
	    // Skip optional WhiteSpace after Stars
	    while (bp < buflen && (ch == ' ' || ch == '\t' || ch == FF)) {
		scanCommentChar();
	    }

	    deprecatedPrefix = false;
	    // At beginning of line in the JavaDoc sense.
	    if (bp < buflen && ch == '@' && !deprecatedFlag) {
		scanCommentChar();
		if (bp < buflen && ch == 'd') {
		    scanCommentChar();
		    if (bp < buflen && ch == 'e') {
			scanCommentChar();
			if (bp < buflen && ch == 'p') {
			    scanCommentChar();
			    if (bp < buflen && ch == 'r') {
				scanCommentChar();
				if (bp < buflen && ch == 'e') {
				    scanCommentChar();
				    if (bp < buflen && ch == 'c') {
					scanCommentChar();
					if (bp < buflen && ch == 'a') {
					    scanCommentChar();
					    if (bp < buflen && ch == 't') {
						scanCommentChar();
						if (bp < buflen && ch == 'e') {
						    scanCommentChar();
						    if (bp < buflen && ch == 'd') {
							deprecatedPrefix = true;
							scanCommentChar();
						    }}}}}}}}}}}
            
            //DEBUG.P("deprecatedPrefix="+deprecatedPrefix);
            //DEBUG.P("ch="+ch+" bp="+bp+" buflen="+buflen+" buf["+bp+"]="+buf[bp]);
            
	    if (deprecatedPrefix && bp < buflen) {
		if (Character.isWhitespace(ch)) {
		    deprecatedFlag = true;
		} else if (ch == '*') {
		    scanCommentChar();
		    if (ch == '/') {
			deprecatedFlag = true;
			return;
		    }
		}
	    }
            
            //DEBUG.P("ch="+ch+" bp="+bp+" buflen="+buflen+" buf["+bp+"]="+buf[bp]);

	    // Skip rest of line
	    while (bp < buflen) {
		switch (ch) {
		case '*':
		    scanCommentChar();
		    if (ch == '/') {
			return;
		    }
		    break;
		case CR: // (Spec 3.4)
		    scanCommentChar();
		    if (ch != LF) {
                        continue forEachLine;
		    }
                    //因为这里没有break语句，上面又可能执行continue，
                    //从而导至下面的case LF后面的语句执行不了，所以编译器会警告
                    //警告：[fallthrough] 可能无法实现 case
                    //fallthrough 中文意思是:失败,落空
                    
		    /* fall through to LF case */
		case LF: // (Spec 3.4)
		    scanCommentChar();
		    continue forEachLine;
		default:
		    scanCommentChar();
		}
	    } // rest of line
	} // forEachLine
	return;//多余的语句
        
        }finally{//我加上的
        DEBUG.P("deprecatedFlag="+deprecatedFlag);
        DEBUG.P(0,this,"scanDocComment()");
        } 
    }

    /** The value of a literal token, recorded as a string.
     *  For integers, leading 0x and 'l' suffixes are suppressed.
     */
    public String stringVal() {
	return new String(sbuf, 0, sp);
    }

    /** Read token.
     */
    public void nextToken() {

	try {
	    prevEndPos = endPos;
	    sp = 0;
	
	    while (true) {//     
	    //处理完processWhiteSpace()与processLineTerminator()两个
	    //方法后，继续往下扫描字符
		pos = bp;
		switch (ch) {
                //如果匹配一个case语句后没有break，那么跟在它后面的case不管是否匹配都执行
                //那个case后面的语句，直到有break为止。例如：当ch=' '(空格)时，依次执行
                //DEBUG.P("空格"),DEBUG.P("Tab"),DEBUG.P("换页")及后面的do循环
		case ' ': //DEBUG.P("空格");// (Spec 3.6)
                //(注意:在用NetBeans书写java源程序时，按下Tab键时会默认输入4个空格)
		case '\t': //DEBUG.P("Tab");// (Spec 3.6)
		case FF: //DEBUG.P("换页");// (Spec 3.6)   //form feed是指换页
                    // <editor-fold defaultstate="collapsed">
		    do {
			scanChar();
		    } while (ch == ' ' || ch == '\t' || ch == FF);
		    endPos = bp;
                    //当紧接着的是Unicode字符时，打印出来的信息就不正确啦。
                    //因为要表示一个Unicode字符最少要6位，bp也要最少加6，
                    //从而pos到endPos(也就是bp)之间的字符包含了表示Unicode字符的位串
                    //例如代码：int \uD800\uDC00;
                    //输出:processWhitespace(1952,1958)=| \\uD80|
                    //(注：输出中只有一个\，我多加了一个\，是用来转义，见convertUnicode())
		    //当unicodeConversionBp == bp时说明当前的ch已是Unicode字符啦
		    if(unicodeConversionBp == bp) {//我加上的
			    endPos = bp+1;
			    DEBUG.P("ch="+ch);
		    }
		    processWhiteSpace();
		    break;
		case LF: // (Spec 3.4)   //换行,有的系统生成的文件可能没有回车符
                    //DEBUG.P("换行");
		    scanChar();
		    endPos = bp;
		    processLineTerminator();
		    break;
		case CR: // (Spec 3.4)   //回车,回车符后面跟换行符
                    //DEBUG.P("回车");
		    scanChar();
		    if (ch == LF) {
                        //DEBUG.P("换行");
			scanChar();
		    }
		    endPos = bp;
		    processLineTerminator();
		    break;
                    // </editor-fold>
		//符合java标识符(或保留字)的首字母的情况之一
		case 'A': case 'B': case 'C': case 'D': case 'E':
		case 'F': case 'G': case 'H': case 'I': case 'J':
		case 'K': case 'L': case 'M': case 'N': case 'O':
		case 'P': case 'Q': case 'R': case 'S': case 'T':
		case 'U': case 'V': case 'W': case 'X': case 'Y':
		case 'Z':
		case 'a': case 'b': case 'c': case 'd': case 'e':
		case 'f': case 'g': case 'h': case 'i': case 'j':
		case 'k': case 'l': case 'm': case 'n': case 'o':
		case 'p': case 'q': case 'r': case 's': case 't':
		case 'u': case 'v': case 'w': case 'x': case 'y':
		case 'z':
		case '$': case '_':
		    scanIdent();
		    return;
		case '0': //16或8进制数的情况
                    // <editor-fold defaultstate="collapsed">
		    scanChar();
		    if (ch == 'x' || ch == 'X') {
			scanChar();
			if (ch == '.') {
                            //参数为false表示在小数点之前没有数字
			    scanHexFractionAndSuffix(false);
			} else if (digit(16) < 0) {
                            //如: 0x、0xw 报错:十六进制数字必须包含至少一位十六进制数
			    lexError("invalid.hex.number");
			} else {
			    scanNumber(16);
			}
		    } else {
			putChar('0');
			scanNumber(8);
		    }
		    return;
                    // </editor-fold>
		case '1': case '2': case '3': case '4':
		case '5': case '6': case '7': case '8': case '9':
		    scanNumber(10);
		    return;
		case '.':
                    // <editor-fold defaultstate="collapsed">
		    scanChar();
		    if ('0' <= ch && ch <= '9') { //例如:float f=.0f;
			putChar('.');
			scanFractionAndSuffix();
		    } else if (ch == '.') {  //检测是否是省略符号(...)
			putChar('.'); putChar('.');
			scanChar();
			if (ch == '.') {
			    scanChar();
			    putChar('.');
			    token = ELLIPSIS;
			} else {  //否则认为是浮点错误
			    lexError("malformed.fp.lit");
			}
		    } else {
			token = DOT;
		    }
		    return;
                    // </editor-fold>
		case ',':
		    scanChar(); token = COMMA; return;
		case ';':
		    scanChar(); token = SEMI; return;
		case '(':
		    scanChar(); token = LPAREN; return;
		case ')':
		    scanChar(); token = RPAREN; return;
		case '[':
		    scanChar(); token = LBRACKET; return;
		case ']':
		    scanChar(); token = RBRACKET; return;
		case '{':
		    scanChar(); token = LBRACE; return;
		case '}':
		    scanChar(); token = RBRACE; return;
		case '/':
                    // <editor-fold defaultstate="collapsed">
		    scanChar();
		    if (ch == '/') {
                        do {
                            scanCommentChar();
                        } while (ch != CR && ch != LF && bp < buflen);
                        
                        DEBUG.P("bp="+bp+" buflen="+buflen+" buf["+bp+"]="+buf[bp]);
                        //如果行注释是最后一行，则不再处理
                        if (bp < buflen) {
                            endPos = bp;
                            processComment(CommentStyle.LINE);
                        }
                        break;
		    } else if (ch == '*') {
                        scanChar();
                        CommentStyle style;

                        if (ch == '*') {
                            style = CommentStyle.JAVADOC;
                            scanDocComment();
                        } else {
                            style = CommentStyle.BLOCK;

                            while (bp < buflen) {
                                if (ch == '*') {
                                    scanChar();
                                    if (ch == '/') break;
                                } else {
                                    scanCommentChar();
                                }
                            }
                        }

                        if (ch == '/') {
                            scanChar();
                            endPos = bp;
                            processComment(style);
                            break;
                        } else {
                            //未结束的注释
                            lexError("unclosed.comment");
                            return;
                        }
		    } else if (ch == '=') {
			name = names.slashequals;
			token = SLASHEQ;
			scanChar();
		    } else {
			name = names.slash;
			token = SLASH;
		    }
		    return;
                    // </editor-fold>
		case '\'':  //字符与字符串都不能跨行
                    // <editor-fold defaultstate="collapsed">
		    scanChar();
		    if (ch == '\'') {
                        //例如:char c='';
			lexError("empty.char.lit");  //空字符字面值
		    } else {
			if (ch == CR || ch == LF)
			    lexError(pos, "illegal.line.end.in.char.lit");//字符字面值的行结尾不合法
			scanLitChar();
			if (ch == '\'') {
			    scanChar();
			    token = CHARLITERAL;
			} else {
				//如“ '8p ”，未结束的字符字面值
			    lexError(pos, "unclosed.char.lit");
			}
		    }
		    return;
                    // </editor-fold>
		case '\"':
                    // <editor-fold defaultstate="collapsed">
		    scanChar();
		    while (ch != '\"' && ch != CR && ch != LF && bp < buflen)
			scanLitChar();
		    if (ch == '\"') {
			token = STRINGLITERAL;
			scanChar();
		    } else {
			lexError(pos, "unclosed.str.lit");
		    }
		    return;
                    // </editor-fold>
		default:
                    // <editor-fold defaultstate="collapsed">
		    if (isSpecial(ch)) { //可以作为操作符的某一部分的字符
			scanOperator();
		    } else {
		    	//这里处理其它字符,如中文变量之类的
		    	//与scanIdent()有相同的部分
		    	//注意这里是Start，而scanIdent()是Part
                        boolean isJavaIdentifierStart;
                        if (ch < '\u0080') {
                            // all ASCII range chars already handled, above
                            isJavaIdentifierStart = false;
                        } else {
                            char high = scanSurrogates();
                            DEBUG.P("high="+high+"(0x"+Integer.toHexString(high).toUpperCase()+") ch="+ch+"(0x"+Integer.toHexString(ch).toUpperCase()+")");
                            if (high != 0) {
                                if (sp == sbuf.length) {
                                    putChar(high);
                                } else {
                                    sbuf[sp++] = high;
                                }

                                isJavaIdentifierStart = Character.isJavaIdentifierStart(
                                Character.toCodePoint(high, ch));
                            } else {
                                // <editor-fold defaultstate="collapsed">
                                /*
                                如果isHighSurrogate(ch)=true或isLowSurrogate(ch)=true
                                那么isJavaIdentifierStart(ch)=false 且 isJavaIdentifierPart(ch)=false
                                下面是测试代码:
                                public static void isHighSurrogate() {
                                    char ch='\uD800';//ch >= '\uD800' && ch <= '\uDBFF'
                                    while(ch <= '\uDBFF') {
                                        //System.out.println("ch="+ch+"(0x"+Integer.toHexString(ch).toUpperCase()+") "+Character.isJavaIdentifierStart(ch));

                                        if(Character.isJavaIdentifierStart(ch))
                                            System.out.println("ch="+ch+"(0x"+Integer.toHexString(ch).toUpperCase()+") isJavaIdentifierStart");

                                        if(Character.isJavaIdentifierPart(ch))
                                            System.out.println("ch="+ch+"(0x"+Integer.toHexString(ch).toUpperCase()+") isJavaIdentifierPart");
                                        ch++;
                                    }
                                }

                                public static void isLowSurrogate() {
                                    char ch='\uDC00';//ch >= '\uDC00' && ch <= '\uDFFF'
                                    while(ch <= '\uDFFF') {
                                        if(Character.isJavaIdentifierStart(ch))
                                            System.out.println("ch="+ch+"(0x"+Integer.toHexString(ch).toUpperCase()+") isJavaIdentifierStart");

                                        if(Character.isJavaIdentifierPart(ch))
                                            System.out.println("ch="+ch+"(0x"+Integer.toHexString(ch).toUpperCase()+") isJavaIdentifierPart");
                                        ch++;
                                    }
                                }
                                */
                                // </editor-fold>
                                isJavaIdentifierStart = Character.isJavaIdentifierStart(ch);
                            }
                        }

                        if (isJavaIdentifierStart) {
                            scanIdent();
		        } else if (bp == buflen || ch == EOI && bp+1 == buflen) { // JLS 3.5
                            token = EOF;
                            pos = bp = eofPos;
		        } else {
                            //如: public char \u007fmyField12
                            //报错:非法字符： \127
                            lexError("illegal.char", String.valueOf((int)ch));
                            scanChar();
		        }
		    }
		    return;
                    // </editor-fold>
		}//switch
	    }//while
	} finally {
	    endPos = bp;
	    /*
	    if (scannerDebug)
		System.out.println("nextToken(" + pos
				   + "," + endPos + ")=|" +
				   new String(getRawCharacters(pos, endPos))
				   + "|");
            */

            //我多加了tokenName=...(方便查看调试结果)
        if (scannerDebug)
            System.out.println("nextToken(" + pos
                               + "," + endPos + ")=|" +
                               new String(getRawCharacters(pos, endPos))
                               + "|  tokenName=|"+token+ "|  prevEndPos="+prevEndPos);
	}
    }

    /** Return the current token, set by nextToken().
     */
    public Token token() {
        return token;
    }

    /** Sets the current token.
     */
    public void token(Token token) {
        this.token = token;
    }

    /** Return the current token's position: a 0-based
     *  offset from beginning of the raw input stream
     *  (before unicode translation)
     */
    public int pos() {
        return pos;
    }

    /** Return the last character position of the current token.
     */
    public int endPos() {
        return endPos;
    }

    /** Return the last character position of the previous token.
     */
    public int prevEndPos() {
        return prevEndPos;
    }

    /** Return the position where a lexical error occurred;
     */
    public int errPos() {
        return errPos;
    }

    /** Set the position where a lexical error occurred;
     */
    public void errPos(int pos) {
        errPos = pos;
    }

    /** Return the name of an identifier or token for the current token.
     */
    public Name name() {
        return name;
    }

    /** Return the radix of a numeric literal token.
     */
    public int radix() {
        return radix;
    }

    /** Has a @deprecated been encountered in last doc comment?
     *  This needs to be reset by client with resetDeprecatedFlag.
     */
    public boolean deprecatedFlag() {
        return deprecatedFlag;
    }

    public void resetDeprecatedFlag() {
        deprecatedFlag = false;
    }

    /**
     * Returns the documentation string of the current token.
     */
    public String docComment() {
        return null;
    }

    /**
     * Returns a copy of the input buffer, up to its inputLength.
     * Unicode escape sequences are not translated.
     */
    public char[] getRawCharacters() {
        //此方法暂时没什么用处,只为了实现Lexer接口而加的
        //这种说法是错误的 2007-06-18 10:09已改正 此方法在DocCommentScanner中有应用
        char[] chars = new char[buflen];
        System.arraycopy(buf, 0, chars, 0, buflen);
        return chars;
    }

    /**
     * Returns a copy of a character array subset of the input buffer.
     * The returned array begins at the <code>beginIndex</code> and
     * extends to the character at index <code>endIndex - 1</code>.
     * Thus the length of the substring is <code>endIndex-beginIndex</code>.
     * This behavior is like 
     * <code>String.substring(beginIndex, endIndex)</code>.
     * Unicode escape sequences are not translated.
     *
     * @param beginIndex the beginning index, inclusive.
     * @param endIndex the ending index, exclusive.
     * @throws IndexOutOfBounds if either offset is outside of the
     *         array bounds
     */
    public char[] getRawCharacters(int beginIndex, int endIndex) {
    	//length不是关键字,可以当变量名
    	//endIndex就是endPos,buf[endPos]的字符不会输出，会作为下次扫描的起点
        int length = endIndex - beginIndex;
        char[] chars = new char[length];
        System.arraycopy(buf, beginIndex, chars, 0, length);
        return chars;
    }

    public enum CommentStyle {
        LINE,
        BLOCK,
        JAVADOC,
    }

    /**
     * Called when a complete comment has been scanned. pos and endPos 
     * will mark the comment boundary.
     */
    protected void processComment(CommentStyle style) {
	if (scannerDebug)
	    System.out.println("processComment(" + pos
			       + "," + endPos + "," + style + ")=|"
                               + new String(getRawCharacters(pos, endPos))
			       + "|");
    }

    /**
     * Called when a complete whitespace run has been scanned. pos and endPos 
     * will mark the whitespace boundary.
     */
    protected void processWhiteSpace() {
	if (scannerDebug)
	    System.out.println("processWhitespace(" + pos
			       + "," + endPos + ")=|" +
			       new String(getRawCharacters(pos, endPos))
			       + "|");
    }

    /**
     * Called when a line terminator has been processed.
     */
    protected void processLineTerminator() {
	if (scannerDebug)
	    System.out.println("processTerminator(" + pos
			       + "," + endPos + ")=|" +
			       new String(getRawCharacters(pos, endPos))
			       + "|");
    }

    /** Build a map for translating between line numbers and
     * positions in the input.
     *
     * @return a LineMap */
    public Position.LineMap getLineMap() {
	return Position.makeLineMap(buf, buflen, false);
    }

}

