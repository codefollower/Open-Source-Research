/*
 * @(#)Parser.java	1.103 07/03/21
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

import java.util.*;

import com.sun.tools.javac.tree.*;
import com.sun.tools.javac.code.*;
import com.sun.tools.javac.util.*;
import com.sun.tools.javac.util.List;
import static com.sun.tools.javac.util.ListBuffer.lb;

import com.sun.tools.javac.tree.JCTree.*;

import static com.sun.tools.javac.parser.Token.*;

/** The parser maps a token sequence into an abstract syntax
 *  tree. It operates by recursive descent, with code derived
 *  systematically from an LL(1) grammar. For efficiency reasons, an
 *  operator precedence scheme is used for parsing binary operation
 *  expressions.
 *
 *  <p><b>This is NOT part of any API supported by Sun Microsystems.  If
 *  you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 */
@Version("@(#)Parser.java	1.103 07/03/21")
public class Parser {
	
    private static my.Debug DEBUG=new my.Debug(my.Debug.Parser);//我加上的
	private void DEBUGPos(JCTree t) {//我加上的
		DEBUG.P("Tree.StartPos="+getStartPos(t));
		DEBUG.P("Tree.EndPos  ="+getEndPos(t));
		DEBUG.P("errorEndPos  ="+errorEndPos);
	}
	
    /** A factory for creating parsers. */
    public static class Factory {
        /** The context key for the parser factory. */
        protected static final Context.Key<Parser.Factory> parserFactoryKey =
            new Context.Key<Parser.Factory>();

        /** Get the Factory instance for this context. */
        public static Factory instance(Context context) {
            Factory instance = context.get(parserFactoryKey);
            if (instance == null)
                instance = new Factory(context);
            return instance;
        }

        final TreeMaker F;
        final Log log;
        final Keywords keywords;
        final Source source;
        final Name.Table names;
        final Options options;

        /** Create a new parser factory. */
        protected Factory(Context context) {
            DEBUG.P(this,"Factory(1)");
                
            context.put(parserFactoryKey, this);
            this.F = TreeMaker.instance(context);
            this.log = Log.instance(context);
            this.names = Name.Table.instance(context);
            this.keywords = Keywords.instance(context);
            this.source = Source.instance(context);
            this.options = Options.instance(context);
            
            DEBUG.P(0,this,"Factory(1)");
        }

        /**
         * Create a new Parser.
         * @param S Lexer for getting tokens while parsing
         * @param keepDocComments true if javadoc comments should be kept
         * @param genEndPos true if end positions should be generated
         */
        public Parser newParser(Lexer S, boolean keepDocComments, boolean genEndPos) {
            try {//我加上的
            DEBUG.P(this,"newParser(3)");
            DEBUG.P("keepDocComments="+keepDocComments);
            DEBUG.P("genEndPos="+genEndPos);
        	
            if (!genEndPos)
                return new Parser(this, S, keepDocComments);
            else
                return new EndPosParser(this, S, keepDocComments);
                
            }finally{//我加上的
            DEBUG.P(0,this,"newParser(3)");
            }
        }
    }
    
    /*为什么会是10呢？因为中缀运算符定义如下
     *  infixop         = "||"
     *                  | "&&"
     *                  | "|"
     *                  | "^"
     *                  | "&"
     *                  | "==" | "!="
     *                  | "<" | ">" | "<=" | ">="
     *                  | "<<" | ">>" | ">>>"
     *                  | "+" | "-"
     *                  | "*" | "/" | "%"
     刚好有10级，当要分析由以上中缀运算符组成的表达式时
     只要定义一个长度为10+1(0号索引可用于哨兵或错误标志)
     的数组来表示一个堆栈空间就足以处理任意长度的中缀运算符表达式。
     
     请参考下面代码中的newOdStack()、newOpStack()、term2Rest()
     */
    /** The number of precedence levels of infix operators.
     */
    private static final int infixPrecedenceLevels = 10;

    /** The scanner used for lexical analysis.
     */
    private Lexer S;

    /** The factory to be used for abstract syntax tree construction.
     */
    protected TreeMaker F;

    /** The log to be used for error diagnostics.
     */
    private Log log;

    /** The keyword table. */
    private Keywords keywords;

    /** The Source language setting. */
    private Source source;

    /** The name table. */
    private Name.Table names;

    /** Construct a parser from a given scanner, tree factory and log.
     */
    protected Parser(Factory fac,
                     Lexer S,
                     boolean keepDocComments) {
        DEBUG.P(this,"Parser(3)");
        this.S = S;
        S.nextToken(); // prime the pump
        this.F = fac.F;
        this.log = fac.log;
        this.names = fac.names;
        this.keywords = fac.keywords;
        this.source = fac.source;
        Options options = fac.options;//这条语句看不出有什么用？
        this.allowGenerics = source.allowGenerics();
        this.allowVarargs = source.allowVarargs();
        this.allowAsserts = source.allowAsserts();
        this.allowEnums = source.allowEnums();
        this.allowForeach = source.allowForeach();
        this.allowStaticImport = source.allowStaticImport();
        this.allowAnnotations = source.allowAnnotations();
        this.keepDocComments = keepDocComments;
        if (keepDocComments) docComments = new HashMap<JCTree,String>();
        this.errorTree = F.Erroneous();
        DEBUG.P(0,this,"Parser(3)");
    }

    /** Switch: Should generics be recognized?
     */
    boolean allowGenerics;

    /** Switch: Should varargs be recognized?
     */
    boolean allowVarargs;

    /** Switch: should we recognize assert statements, or just give a warning?
     */
    boolean allowAsserts;

    /** Switch: should we recognize enums, or just give a warning?
     */
    boolean allowEnums;

    /** Switch: should we recognize foreach?
     */
    boolean allowForeach;

    /** Switch: should we recognize foreach? 
     */
    //应是:Switch: should we recognize static import? 
    boolean allowStaticImport;

    /** Switch: should we recognize annotations?
     */
    boolean allowAnnotations;

    /** Switch: should we keep docComments?
     */
    boolean keepDocComments;

    /** When terms are parsed, the mode determines which is expected:
     *     mode = EXPR        : an expression
     *     mode = TYPE        : a type
     *     mode = NOPARAMS    : no parameters allowed for type
     *     mode = TYPEARG     : type argument
     */
    static final int EXPR = 1;
    static final int TYPE = 2;
    static final int NOPARAMS = 4;
    static final int TYPEARG = 8;

    /** The current mode.
     */
    private int mode = 0;
    
    //下面是我加上的，调试用途
    public static String myMode(int m) {
        StringBuffer buf = new StringBuffer();
        if ((m&EXPR) != 0) buf.append("EXPR ");
        if ((m&TYPE) != 0) buf.append("TYPE ");
        if ((m&NOPARAMS) != 0) buf.append("NOPARAMS ");
        if ((m&TYPEARG) != 0) buf.append("TYPEARG ");
        
        if(buf.length()==0) buf.append(m);
        return buf.toString();
    }

    /** The mode of the term that was parsed last.
     */
    private int lastmode = 0;

/* ---------- error recovery -------------- */

    private JCErroneous errorTree;

    //什么时候该调用这个方法来从错误中恢复呢？当S.pos() <= errorEndPos？？？
    //那什么时候该判断S.pos() <= errorEndPos？当errorEndPos有可能改变吗？
    /** Skip forward until a suitable stop token is found.
     */
    private void skip(boolean stopAtImport, boolean stopAtMemberDecl, boolean stopAtIdentifier, boolean stopAtStatement) {
		try {//我加上的
		DEBUG.P(this,"skip(4)");
		DEBUG.P("stopAtImport    ="+stopAtImport);
		DEBUG.P("stopAtMemberDecl="+stopAtMemberDecl);
		DEBUG.P("stopAtIdentifier="+stopAtIdentifier);
		DEBUG.P("stopAtStatement ="+stopAtStatement);

		while (true) {
			switch (S.token()) {
				case SEMI:
                    S.nextToken();
                    return;
                case PUBLIC:
                case FINAL:
                case ABSTRACT:
                case MONKEYS_AT:
                case EOF:
                case CLASS:
                case INTERFACE:
                case ENUM:
                    return;
                case IMPORT:
                	//如果之前的错误是在分析import语句时发现的,经过若干次
                	//nextToken()后，找到了新的叫IMPORT的token，说明找到了
                	//一条新的import语句，现在就可以正常解析了
                    if (stopAtImport)
                        return;
                    break;
                case LBRACE:
                case RBRACE:
                case PRIVATE:
                case PROTECTED:
                case STATIC:
                case TRANSIENT:
                case NATIVE:
                case VOLATILE:
                case SYNCHRONIZED:
                case STRICTFP:
                case LT:
                case BYTE:
                case SHORT:
                case CHAR:
                case INT:
                case LONG:
                case FLOAT:
                case DOUBLE:
                case BOOLEAN:
                case VOID:
                    if (stopAtMemberDecl)
                        return;
                    break;
                case IDENTIFIER:
					if (stopAtIdentifier)
						return;
					break;
                case CASE:
                case DEFAULT:
                case IF:
                case FOR:
                case WHILE:
                case DO:
                case TRY:
                case SWITCH:
                case RETURN:
                case THROW:
                case BREAK:
                case CONTINUE:
                case ELSE:
                case FINALLY:
                case CATCH:
                    if (stopAtStatement)
                        return;
                    break;
            }
            S.nextToken();
        }
        
		}finally{//我加上的
		DEBUG.P(0,this,"skip(4)");
		}
    }

    private JCErroneous syntaxError(int pos, String key, Object... arg) {
	    try {//我加上的
	    DEBUG.P(this,"syntaxError(3)");
	    DEBUG.P("pos="+pos);
	    DEBUG.P("key="+key);

        return syntaxError(pos, null, key, arg);

		}finally{//我加上的
		DEBUG.P(0,this,"syntaxError(3)");
		}
    }

    private JCErroneous syntaxError(int pos, List<JCTree> errs, String key, Object... arg) {
        try {//我加上的
		DEBUG.P(this,"syntaxError(4)");
	    DEBUG.P("pos="+pos);
	    DEBUG.P("key="+key);
	    DEBUG.P("errs="+errs);

		setErrorEndPos(pos);
        reportSyntaxError(pos, key, arg);
        return toP(F.at(pos).Erroneous(errs));

		}finally{//我加上的
		DEBUG.P(0,this,"syntaxError(4)");
		}
    }

    private int errorPos = Position.NOPOS;
    /**
     * Report a syntax error at given position using the given
     * argument unless one was already reported at the same position.
     */
    private void reportSyntaxError(int pos, String key, Object... arg) {
	    DEBUG.P(this,"reportSyntaxError(3)");
    	DEBUG.P("pos="+pos);
    	DEBUG.P("S.errPos()="+S.errPos());
		DEBUG.P("S.token()="+S.token());

        if (pos > S.errPos() || pos == Position.NOPOS) {
            if (S.token() == EOF)
                log.error(pos, "premature.eof");
            else
                log.error(pos, key, arg);
        }
        S.errPos(pos);

		DEBUG.P("errorPos="+errorPos);
    	DEBUG.P("S.pos()="+S.pos());
		
		//例:Class c=int[][].char;
        if (S.pos() == errorPos)
            S.nextToken(); // guarantee progress
        errorPos = S.pos();

		DEBUG.P(0,this,"reportSyntaxError(3)");
    }


    /** Generate a syntax error at current position unless one was already
     *  reported at the same position.
     */
    private JCErroneous syntaxError(String key) {
        return syntaxError(S.pos(), key); //调用syntaxError(int pos, String key, Object... arg)
    }

    /** Generate a syntax error at current position unless one was
     *  already reported at the same position.
     */
    private JCErroneous syntaxError(String key, String arg) {
        return syntaxError(S.pos(), key, arg);
    }
    // <editor-fold defaultstate="collapsed">//
    /*例子:(对研究accept(1)与skip(4)方法的工作机制有帮助)
    
    有语法错误的源代码:
    package my.test k
	import java.util.ArrayList;
	
	编译错误提示:
    bin\mysrc\my\test\Test3.java:1: 需要 ';'
	package my.test k
	               ^
	1 错误
	
	部分打印输出结果:
	nextToken(11,15)=|test|  tokenName=|IDENTIFIER|  prevEndPos=11
	com.sun.tools.javac.parser.Parser===>ident()
	-------------------------------------------------------------------------
	ident.name=test
	processWhitespace(15,16)=| |
	nextToken(16,17)=|k|  tokenName=|IDENTIFIER|  prevEndPos=15
	com.sun.tools.javac.parser.Parser===>ident()  END
	-------------------------------------------------------------------------
	qualident=my.test
	com.sun.tools.javac.parser.Parser===>qualident()  END
	-------------------------------------------------------------------------
	com.sun.tools.javac.parser.Parser===>accept(1)
	-------------------------------------------------------------------------
	accToken=SEMI
	curToken=IDENTIFIER
	com.sun.tools.javac.parser.Parser===>accept(1)  END
	-------------------------------------------------------------------------
	com.sun.tools.javac.parser.Parser===>skip(4)
	-------------------------------------------------------------------------
	stopAtImport    =true
	stopAtMemberDecl=false
	stopAtIdentifier=false
	stopAtStatement =false
	processTerminator(17,18)=|
	|
	nextToken(18,24)=|import|  tokenName=|IMPORT|  prevEndPos=17
	com.sun.tools.javac.parser.Parser===>skip(4)  END
	-------------------------------------------------------------------------
	*/
	// </editor-fold>
    /** If next input token matches given token, skip it, otherwise report
     *  an error.
     */
    public void accept(Token token) {
    	DEBUG.P(this,"accept(1)");
    	DEBUG.P("accToken="+token);
    	DEBUG.P("curToken="+S.token());
        if (S.token() == token) {
            S.nextToken();
        } else {
            setErrorEndPos(S.pos());
            reportSyntaxError(S.prevEndPos(), "expected", keywords.token2string(token));
        }
        DEBUG.P(0,this,"accept(1)");
    }

    /** Report an illegal start of expression/type error at given position.
     */
    JCExpression illegal(int pos) {
        setErrorEndPos(S.pos());
        if ((mode & EXPR) != 0)
            return syntaxError(pos, "illegal.start.of.expr");
        else
            return syntaxError(pos, "illegal.start.of.type");

    }

    /** Report an illegal start of expression/type error at current position.
     */
    JCExpression illegal() {
        return illegal(S.pos());
    }

    /** Diagnose a modifier flag from the set, if any. */
    void checkNoMods(long mods) {
    	DEBUG.P(this,"checkNoMods(long mods)");
    	DEBUG.P("mods="+Flags.toString(mods).trim());
    	
        if (mods != 0) {
            /*
            只取mods最底非0位,其他位都清0:
            for(int mods=1;mods<6;mods++) {
                System.out.println("十进制: "+mods+" & -"+mods+" = "+(mods & -mods));
                System.out.println("二进制: "+Integer.toBinaryString(mods)+" & "+Integer.toBinaryString(-mods)+" = "+Integer.toBinaryString(mods & -mods));
                System.out.println();
            }
            输出:(互为相反数的两个数都可按“按位取反加1”的原则得到对方)
            十进制: 1 & -1 = 1
            二进制: 1 & 11111111111111111111111111111111 = 1

            十进制: 2 & -2 = 2
            二进制: 10 & 11111111111111111111111111111110 = 10

            十进制: 3 & -3 = 1
            二进制: 11 & 11111111111111111111111111111101 = 1

            十进制: 4 & -4 = 4
            二进制: 100 & 11111111111111111111111111111100 = 100

            十进制: 5 & -5 = 1
            二进制: 101 & 11111111111111111111111111111011 = 1
            */
            long lowestMod = mods & -mods;
            DEBUG.P("lowestMod="+Flags.toString(lowestMod).trim());
            log.error(S.pos(), "mod.not.allowed.here",
                      Flags.toString(lowestMod).trim());
        }
        DEBUG.P(0,this,"checkNoMods(long mods)");
    }

/* ---------- doc comments --------- */

    /** A hashtable to store all documentation comments
     *  indexed by the tree nodes they refer to.
     *  defined only if option flag keepDocComment is set.
     */
    Map<JCTree, String> docComments;

    /** Make an entry into docComments hashtable,
     *  provided flag keepDocComments is set and given doc comment is non-null.
     *  @param tree   The tree to be used as index in the hashtable
     *  @param dc     The doc comment to associate with the tree, or null.
     */
    void attach(JCTree tree, String dc) {
        if (keepDocComments && dc != null) {
//          System.out.println("doc comment = ");System.out.println(dc);//DEBUG
            docComments.put(tree, dc);
        }
    }

/* -------- source positions ------- */

    private int errorEndPos = -1;

    private void setErrorEndPos(int errPos) {
	    DEBUG.P(this,"setErrorEndPos(1)");
	    DEBUG.P("errPos="+errPos);
	    DEBUG.P("errorEndPos="+errorEndPos);

        if (errPos > errorEndPos)
            errorEndPos = errPos;

		DEBUG.P(0,this,"setErrorEndPos(1)");
    }

    protected int getErrorEndPos() {
        return errorEndPos;
    }

    /**
     * Store ending position for a tree.
     * @param tree   The tree.
     * @param endpos The ending position to associate with the tree.
     */
    protected void storeEnd(JCTree tree, int endpos) {}

    /**
     * Store ending position for a tree.  The ending position should
     * be the ending position of the current token.
     * @param t The tree.
     */
    protected <T extends JCTree> T to(T t) { return t; }

    /**
     * Store ending position for a tree.  The ending position should
     * be greater of the ending position of the previous token and errorEndPos.
     * @param t The tree.
     */
    protected <T extends JCTree> T toP(T t) { return t; }

    /** Get the start position for a tree node.  The start position is
     * defined to be the position of the first character of the first
     * token of the node's source text.
     * @param tree  The tree node
     */
    public int getStartPos(JCTree tree) {
        return TreeInfo.getStartPos(tree);
    }

    /**
     * Get the end position for a tree node.  The end position is
     * defined to be the position of the last character of the last
     * token of the node's source text.  Returns Position.NOPOS if end
     * positions are not generated or the position is otherwise not
     * found.
     * @param tree  The tree node
     */
    public int getEndPos(JCTree tree) {
        return Position.NOPOS;
    }



/* ---------- parsing -------------- */

    /**
     * Ident = IDENTIFIER
     */
    Name ident() {
    	try {//我加上的
		DEBUG.P(this,"ident()");
		
        if (S.token() == IDENTIFIER) {
            Name name = S.name();
            DEBUG.P("ident.name="+name);
            S.nextToken();
            return name;
        } else if (S.token() == ASSERT) {
            if (allowAsserts) {
            	/*
            	例:
                F:\Javac\bin\other>javac Test5.java
                Test5.java:4: 从版本 1.4 开始，'assert' 是一个关键字，但不能用作标识符
                （请使用 -source 1.3 或更低版本以便将 'assert' 用作标识符）
                        int assert=0;
                            ^
                1 错误
                */
                log.error(S.pos(), "assert.as.identifier");
                S.nextToken();
                return names.error;//error在com.sun.tools.javac.util.Name.Table中定义
            } else {
            	/*
            	例:
            	F:\Javac\bin\other>javac -source 1.3 Test5.java
                Test5.java:4: 警告：从版本 1.4 开始，'assert' 是一个关键字，但不能用作标识符
                （请使用 -source 1.4 或更高版本以便将 'assert' 用作关键字）
                                int assert=0;
                                    ^
                1 警告
                */
                log.warning(S.pos(), "assert.as.identifier");
                Name name = S.name();
                S.nextToken();
                return name;
            }
        } else if (S.token() == ENUM) {
        	//与ASSERT类似
            if (allowEnums) {
                log.error(S.pos(), "enum.as.identifier");
                S.nextToken();
                return names.error;
            } else {
                log.warning(S.pos(), "enum.as.identifier");
                Name name = S.name();
                S.nextToken();
                return name;
            }
        } else {
            accept(IDENTIFIER);
            return names.error;
        }

		}finally{//我加上的
		DEBUG.P(0,this,"ident()");
		}        
	}

    /**
     * Qualident = Ident { DOT Ident }
     */
    public JCExpression qualident() {
    	DEBUG.P(this,"qualident()");
    	//注意下面是先F.at(S.pos())，然后再调用ident()
        JCExpression t = toP(F.at(S.pos()).Ident(ident()));
		DEBUGPos(t);
        while (S.token() == DOT) {
            int pos = S.pos();
            S.nextToken();
            
            /*
            //用当前pos覆盖TreeMaker里的pos,然后生成一棵JCFieldAccess树
            //所生成的JCFieldAccess实例将TreeMaker里的pos当成自己的pos
            //JCFieldAccess按Ident的逆序层层嵌套
            
            //如当Qualident =java.lang.Byte时表示为:
            JCFieldAccess {
            	Name name = "Byte";
            	JCExpression selected = {
            		JCFieldAccess {
            			Name name="lang";
            			JCExpression selected = {
				            JCIdent {
				            	Name name = "java";
				            }
				        }
				    }
				}
			}
			*/
			//DEBUG.P("pos="+pos);//这里的pos是"."号的开始位置
            t = toP(F.at(pos).Select(t, ident()));
			//DEBUGPos(t);//但是这里输出的开始位置总是第一个ident的开始位置
        }
        
        DEBUG.P("qualident="+t);
		DEBUGPos(t);
        DEBUG.P(0,this,"qualident()");
        return t;
    }

    /**
     * Literal =
     *     INTLITERAL
     *   | LONGLITERAL
     *   | FLOATLITERAL
     *   | DOUBLELITERAL
     *   | CHARLITERAL
     *   | STRINGLITERAL
     *   | TRUE
     *   | FALSE
     *   | NULL
     */

     //为什么没有byte,short呢？因为在Scanner中分析数字或字符不按类型声明来分析的，
     //只是单单从字面值分析，所以没有byte,short这样的字面值(LITERAL)
    JCExpression literal(Name prefix) {
    	DEBUG.P(this,"literal(Name prefix)");
    	DEBUG.P("prefix="+prefix);
    	//prefix是指字面文字(Literal)的前缀,如是否带负号(-)
    	
        int pos = S.pos();
        JCExpression t = errorTree;

        switch (S.token()) {
        case INTLITERAL:
            try {
            	//类全限定名称:com.sun.tools.javac.code.TypeTags
            	//类全限定名称:com.sun.tools.javac.util.Convert
                t = F.at(pos).Literal(
                    TypeTags.INT,
                    Convert.string2int(strval(prefix), S.radix()));
            } catch (NumberFormatException ex) {
            	/*错误例子:
            	bin\mysrc\my\test\Test3.java:29: 过大的整数： 099
		        public final int c=099;
		                           ^
		        */                   
                log.error(S.pos(), "int.number.too.large", strval(prefix));
            }
            break;
        case LONGLITERAL:
            try {
                t = F.at(pos).Literal(
                    TypeTags.LONG,
                    new Long(Convert.string2long(strval(prefix), S.radix())));
            } catch (NumberFormatException ex) {
                log.error(S.pos(), "int.number.too.large", strval(prefix));
            }
            break;
        case FLOATLITERAL: {
            String proper = (S.radix() == 16 ? ("0x"+ S.stringVal()) : S.stringVal());
            Float n;
            try {
				//经过词法分析后proper代表的浮点数格式肯定是正确的，
				//但是词法分析时并不知道浮点字面值是否过小还是过大
				//如果过小，那么Float.valueOf(proper)总是返回0.0f，
				//这与正常的0.0f无法区分，所以在下面通过!isZero(proper)来判断，
				//如果proper("0x"除外)中的每个字符只要有一个不是0或'.'号，
				//则一定是过小的浮点数
				//另外，对于过大的浮点数，Float.valueOf(proper)总是返回Float.POSITIVE_INFINITY
                n = Float.valueOf(proper);
            } catch (NumberFormatException ex) {
                // error already repoted in scanner
                n = Float.NaN;
            }
            if (n.floatValue() == 0.0f && !isZero(proper)) //例:float f1=1.1E-33333f;
                log.error(S.pos(), "fp.number.too.small");
            else if (n.floatValue() == Float.POSITIVE_INFINITY) //例:float f2=1.1E+33333f;
                log.error(S.pos(), "fp.number.too.large");
            else
                t = F.at(pos).Literal(TypeTags.FLOAT, n);
            break;
        }
        case DOUBLELITERAL: {
            String proper = (S.radix() == 16 ? ("0x"+ S.stringVal()) : S.stringVal());
            Double n;
            try {
                n = Double.valueOf(proper); //同上
            } catch (NumberFormatException ex) {
                // error already reported in scanner
                n = Double.NaN;
            }
            if (n.doubleValue() == 0.0d && !isZero(proper))
                log.error(S.pos(), "fp.number.too.small");
            else if (n.doubleValue() == Double.POSITIVE_INFINITY)
                log.error(S.pos(), "fp.number.too.large");
            else
                t = F.at(pos).Literal(TypeTags.DOUBLE, n);
            break;
        }
        case CHARLITERAL:
            t = F.at(pos).Literal(
                TypeTags.CHAR,
                S.stringVal().charAt(0) + 0); //注意这里：字符转成了整数,Literal方法接收的是Integer对象
            break;
        case STRINGLITERAL:
            t = F.at(pos).Literal(
                TypeTags.CLASS,
                S.stringVal());
            break;
        case TRUE: case FALSE:
            t = F.at(pos).Literal(
                TypeTags.BOOLEAN,
                (S.token() == TRUE ? 1 : 0));
            break;
        case NULL:
            t = F.at(pos).Literal(
                TypeTags.BOT,
                null);
            break;
        default:
            assert false;
        }
        if (t == errorTree)
            t = F.at(pos).Erroneous();
        storeEnd(t, S.endPos());
        S.nextToken();
        
        DEBUG.P("return t="+t);
        DEBUG.P(0,this,"literal(Name prefix)");
        return t;
    }
//where
        boolean isZero(String s) {
            char[] cs = s.toCharArray();
            int base = ((Character.toLowerCase(s.charAt(1)) == 'x') ? 16 : 10);
            int i = ((base==16) ? 2 : 0);
            while (i < cs.length && (cs[i] == '0' || cs[i] == '.')) i++;
            return !(i < cs.length && (Character.digit(cs[i], base) > 0));
        }

        String strval(Name prefix) {
        	//字面文字(Literal)在Scanner中被当成
        	//字符串存放在临时缓存字符数组中
            String s = S.stringVal();
            return (prefix.len == 0) ? s : prefix + s;
        }

    /** terms can be either expressions or types.
     */
    public JCExpression expression() {
    	try {//我加上的
		DEBUG.P(this,"expression()");
		
        return term(EXPR);

        }finally{//我加上的
		DEBUG.P(0,this,"expression()");
		}        
    }

    public JCExpression type() {
    	try {//我加上的
		DEBUG.P(this,"type()");

        return term(TYPE);
        
        }finally{//我加上的
		DEBUG.P(0,this,"type()");
		}
    }

    JCExpression term(int newmode) {
    	try {//我加上的
		DEBUG.P(this,"term(int newmode)");
		DEBUG.P("newmode="+myMode(newmode)+"  mode="+myMode(mode));
		
        int prevmode = mode;
        mode = newmode;
        JCExpression t = term();
        lastmode = mode;
        mode = prevmode;
        return t;
        
        }finally{//我加上的
		DEBUG.P(0,this,"term(int newmode)");
		}
    }
    
    /*
    按照操作符优先级从低到高的顺序来看下面的语法中每次出现的一堆操作符
    操作符优先级参考<<core java 卷I) p47
    
    语法中每个非终结符就代表一个函数，函数的调用次序决定了操作符的优先级
    如赋值运算符AssignmentOperator的优先级最底，所以最后才调用termRest函数
    */
    
    /**
     *  Expression = Expression1 [ExpressionRest]
     *  ExpressionRest = [AssignmentOperator Expression1]
     *  AssignmentOperator = "=" | "+=" | "-=" | "*=" | "/=" |
     *                       "&=" | "|=" | "^=" |
     *                       "%=" | "<<=" | ">>=" | ">>>="
     *  Type = Type1
     *  TypeNoParams = TypeNoParams1
     *  StatementExpression = Expression
     *  ConstantExpression = Expression
     */
    JCExpression term() {
    	try {//我加上的
		DEBUG.P(this,"term()");
		
        JCExpression t = term1();   
        /*
        除了"="之外的所有赋值运算符在Token.java中的定义顺序如下:
        PLUSEQ("+="),
	    SUBEQ("-="),
	    STAREQ("*="),
	    SLASHEQ("/="),
	    AMPEQ("&="),
	    BAREQ("|="),
	    CARETEQ("^="),
	    PERCENTEQ("%="),
	    LTLTEQ("<<="),
	    GTGTEQ(">>="),
	    GTGTGTEQ(">>>="),
	    
	    语句PLUSEQ.compareTo(S.token()) <= 0 && S.token().compareTo(GTGTGTEQ) <= 0
	    表示S.token()是上面所列Token之一。
        
        PLUSEQ.compareTo(S.token()) <= 0表示PLUSEQ.ordinal<=S.token().ordinal
        compareTo()方法在java.lang.Enum<E>定义,形如:
        public final int compareTo(E o) {
		Enum other = (Enum)o;
		Enum self = this;
		............
		return self.ordinal - other.ordinal;
	    }
        */
        DEBUG.P("mode="+myMode(mode));
		DEBUG.P("S.token()="+S.token());
        //如果if条件为true说明是一个赋值表达式语句
        if ((mode & EXPR) != 0 &&
            S.token() == EQ || PLUSEQ.compareTo(S.token()) <= 0 && S.token().compareTo(GTGTGTEQ) <= 0)
            return termRest(t);
        else
            return t;
            
        }finally{//我加上的
		DEBUG.P(0,this,"term()");
		}    
    }

    JCExpression termRest(JCExpression t) {
    	try {//我加上的
		DEBUG.P(this,"termRest(JCExpression t)");
		DEBUG.P("t="+t);
		DEBUG.P("S.token()="+S.token());
		
        switch (S.token()) {
        case EQ: {
            int pos = S.pos();
            S.nextToken();
            mode = EXPR;
            /*注意这里是term()，而不是term1()，初看语法:
            Expression = Expression1 [ExpressionRest]
			ExpressionRest = [AssignmentOperator Expression1]
			感觉应是term1()才对，因为java语言允许像a=b=c=d这样的语法,
			所以把ExpressionRest = [AssignmentOperator Expression1]
			看成  ExpressionRest = [AssignmentOperator Expression]
			或者直接用下面一条语法:
			Expression = Expression1 {AssignmentOperator Expression1}
			替换
			Expression = Expression1 [ExpressionRest]
			ExpressionRest = [AssignmentOperator Expression1]
			这两种方式都比原来的好理解
			
			另外在
			Java Language Specification, Third Edition
			18.1. The Grammar of the Java Programming Language
			中的定义如下:
			   Expression:
      		   Expression1 [AssignmentOperator Expression1]]
      		   
      		“]]”有点莫明其妙，不知道是不是多加了个“]”
			*/
            JCExpression t1 = term();
            return toP(F.at(pos).Assign(t, t1));
        }
        case PLUSEQ:
        case SUBEQ:
        case STAREQ:
        case SLASHEQ:
        case PERCENTEQ:
        case AMPEQ:
        case BAREQ:
        case CARETEQ:
        case LTLTEQ:
        case GTGTEQ:
        case GTGTGTEQ:
            int pos = S.pos();
            Token token = S.token();
            S.nextToken();
            mode = EXPR;
            JCExpression t1 = term(); //同上
            return F.at(pos).Assignop(optag(token), t, t1);
        default:
            return t;
        }
        
        }finally{//我加上的
		DEBUG.P(0,this,"termRest(JCExpression t)");
		}  
    }

    /** Expression1   = Expression2 [Expression1Rest]
     *  Type1         = Type2
     *  TypeNoParams1 = TypeNoParams2
     */
    JCExpression term1() {
    	try {//我加上的
		DEBUG.P(this,"term1()");
        JCExpression t = term2();
        DEBUG.P("mode="+myMode(mode));
		DEBUG.P("S.token()="+S.token());
        if ((mode & EXPR) != 0 && S.token() == QUES) {
            mode = EXPR;
            return term1Rest(t);
        } else {
            return t;
        }
        
        }finally{//我加上的
		DEBUG.P(0,this,"term1()");
		}
    }

    /** Expression1Rest = ["?" Expression ":" Expression1]
     */
    JCExpression term1Rest(JCExpression t) {
    	try {//我加上的
		DEBUG.P(this,"term1Rest(JCExpression t)");
		DEBUG.P("t="+t);
		DEBUG.P("S.token()="+S.token());
		
        if (S.token() == QUES) {
            int pos = S.pos();
            DEBUG.P("pos="+pos);
            S.nextToken();
            JCExpression t1 = term();
            accept(COLON);
            
            //对于condition ? trueExpression : falseExpression语句
            //从这里可以看出falseExpression不能含有赋值运算符AssignmentOperator
            //但是trueExpression可以
            JCExpression t2 = term1();
            
            //JCConditional的pos是QUES的pos,而不是t的pos
            return F.at(pos).Conditional(t, t1, t2);
        } else {
            return t;
        }
             
        }finally{//我加上的
		DEBUG.P(0,this,"term1Rest(JCExpression t)");
		}
    }

    /** Expression2   = Expression3 [Expression2Rest]
     *  Type2         = Type3
     *  TypeNoParams2 = TypeNoParams3
     */
    JCExpression term2() {
    	try {//我加上的
		DEBUG.P(this,"term2()");
        JCExpression t = term3();
        
        DEBUG.P("mode="+myMode(mode));
		DEBUG.P("S.token()="+S.token());
		
		//当前运算符的优先级>=“||”运算符的优先级时，才调用term2Rest
        if ((mode & EXPR) != 0 && prec(S.token()) >= TreeInfo.orPrec) {
            mode = EXPR;
            return term2Rest(t, TreeInfo.orPrec);
        } else {
            return t;
        }
        
        
        }finally{//我加上的
		DEBUG.P(0,this,"term2()");
		}        
    }
    
    //instanceof运算符和比较运算符("<" | ">" | "<=" | ">=")的优先级一样
    
    /*  Expression2Rest = {infixop Expression3}
     *                  | Expression3 instanceof Type
     *  infixop         = "||"
     *                  | "&&"
     *                  | "|"
     *                  | "^"
     *                  | "&"
     *                  | "==" | "!="
     *                  | "<" | ">" | "<=" | ">="
     *                  | "<<" | ">>" | ">>>"
     *                  | "+" | "-"
     *                  | "*" | "/" | "%"
     */
    JCExpression term2Rest(JCExpression t, int minprec) {
    	try {//我加上的
		DEBUG.P(this,"term2Rest(JCExpression t, int minprec)");
		DEBUG.P("t="+t);
		DEBUG.P("S.token()="+S.token());
		//DEBUG.P("odStackSupply.size="+odStackSupply.size());
		//DEBUG.P("opStackSupply.size="+opStackSupply.size());
		
		//odStack指向odStackSupply.elems.head
        //odStackSupply.elems往下移
        List<JCExpression[]> savedOd = odStackSupply.elems;
		//DEBUG.P("odStackSupply.elems="+odStackSupply.elems);
		//DEBUG.P("savedOd.size="+savedOd.size());
		//DEBUG.P("savedOd="+savedOd);
        JCExpression[] odStack = newOdStack();


        List<Token[]> savedOp = opStackSupply.elems;
		//DEBUG.P("opStackSupply.elems="+opStackSupply.elems);
		//DEBUG.P("savedOp.size="+savedOp.size());
		//DEBUG.P("savedOp="+savedOp);
        Token[] opStack = newOpStack();

		/*
		DEBUG.P(1);
		DEBUG.P("odStackSupply.elems="+odStackSupply.elems);
		DEBUG.P("savedOd.size="+savedOd.size());
		DEBUG.P("savedOd="+savedOd);
		DEBUG.P("opStackSupply.elems="+opStackSupply.elems);
		DEBUG.P("savedOp.size="+savedOp.size());
		DEBUG.P("savedOp="+savedOp);
		*/

        // optimization, was odStack = new Tree[...]; opStack = new Tree[...];
        int top = 0;
        odStack[0] = t;
        int startPos = S.pos();
        Token topOp = ERROR;
        while (prec(S.token()) >= minprec) {
        	DEBUG.P("topOp="+topOp+" S.token()="+S.token());
            opStack[top] = topOp;
            top++;
            topOp = S.token();
            int pos = S.pos();
            S.nextToken();
            odStack[top] = topOp == INSTANCEOF ? type() : term3();
            //for(int i=0;i<odStack.length;i++) {
			for(int i=0;i<=top;i++) {
            	if(odStack[i]!=null) DEBUG.P("odStack["+i+"]="+odStack[i]);
            }
            for(int i=0;i<=top;i++) {
            	if(opStack[i]!=null) DEBUG.P("opStack["+i+"]="+opStack[i]);
            }
            //只要前一个运算符的优先级>=紧接的运算符的优先级
            //就马上归并。如:1+2+4*5,会先归并1+2，接着是4*5
            //最后是(1+2)+(4*5)
            while (top > 0 && prec(topOp) >= prec(S.token())) {
            	DEBUG.P("pos="+pos);//这里的pos是topOp的pos
            	DEBUG.P("topOp="+topOp+" S.token()="+S.token());
            	//DEBUG.P("odStack[top-1]="+odStack[top-1]);
            	//DEBUG.P("odStack[top]="+odStack[top]);
                odStack[top-1] = makeOp(pos, topOp, odStack[top-1],
                                        odStack[top]);
                top--;
                topOp = opStack[top];
                DEBUG.P("topOp="+topOp+" S.token()="+S.token());
                
                for(int i=0;i<=top;i++) {
	            	if(odStack[i]!=null) DEBUG.P("odStack["+i+"]="+odStack[i]);
	            }
	            for(int i=0;i<=top;i++) {
	            	if(opStack[i]!=null) DEBUG.P("opStack["+i+"]="+opStack[i]);
	            }
            }
        }
        assert top == 0;
        /*
        odStack[0]所代表的Binary表达式的运算符(opcode)的优先级
        总是在所有字面表达式中最小最右边的那个
        
        如a || 1<=2 && 3<=4，则odStack[0].opcode=||
        以下是输出结果:
        ----------------------------
        t=a || 1 <= 2 && 3 <= 4
		t.tag=CONDITIONAL_OR
		t.lhs=a
		t.rhs=1 <= 2 && 3 <= 4
        
        
        再如1+2>0 || a || 1<=2 && 3<=4,则odStack[0].opcode还是等于||
        以下是输出结果:
        ----------------------------
        t=1 + 2 > 0 || a || 1 <= 2 && 3 <= 4
		t.tag=CONDITIONAL_OR
		t.lhs=1 + 2 > 0 || a
		t.rhs=1 <= 2 && 3 <= 4
		*/
        t = odStack[0];
		DEBUG.P(1);
        DEBUG.P("t="+t);
        DEBUG.P("t.tag="+t.getKind());
        if(t instanceof JCBinary) {
			DEBUG.P("t.lhs="+((JCBinary)t).lhs);
			DEBUG.P("t.rhs="+((JCBinary)t).rhs);
        }
        
        if (t.tag == JCTree.PLUS) {
            StringBuffer buf = foldStrings(t);
            DEBUG.P("buf="+buf);
            if (buf != null) {
                t = toP(F.at(startPos).Literal(TypeTags.CLASS, buf.toString()));
            }
        }
        
        //不用再次分配堆栈空间
        odStackSupply.elems = savedOd; // optimization
        opStackSupply.elems = savedOp; // optimization
        return t;
        
        }finally{//我加上的
		DEBUG.P(0,this,"term2Rest(JCExpression t, int minprec)");
		} 
    }
//where
        /** Construct a binary or type test node.
         */
        private JCExpression makeOp(int pos,
                                    Token topOp,
                                    JCExpression od1,
                                    JCExpression od2)
        {
            if (topOp == INSTANCEOF) {
                return F.at(pos).TypeTest(od1, od2);
            } else {
                return F.at(pos).Binary(optag(topOp), od1, od2);
            }
        }
        /** If tree is a concatenation of string literals, replace it
         *  by a single literal representing the concatenated string.
         */
        protected StringBuffer foldStrings(JCTree tree) {
        	try {//我加上的
        	DEBUG.P(this,"foldStrings(JCTree tree");
        	DEBUG.P("tree="+tree);
       		DEBUG.P("tree.tag="+tree.getKind());
       		
            List<String> buf = List.nil();
            /*
            只有表达中的运算符全是加号(+)，而且用加号连接起来
            的每个字面值全都是字符串时，才把每个字面值字符串合并起来。
            例如 "ab"+"cd"+"ef"+"gh":
            List<String> buf的内部结构按如下过程变化:
            1. buf.prepend("gh") = "gh"
            2. buf.prepend("ef") = "ef"==>"gh"
            3. buf.prepend("cd") = "cd"==>"ef"==>"gh"
            
            然后StringBuffer sbuf = new StringBuffer("ab");
            sbuf.append("cd") = "abcd"
            sbuf.append("ef") = "abcdef"
            sbuf.append("gh") = "abcdefgh"
            
            最后返回:"abcdefgh"。
            
            对于"ab"+"cd"+"ef"+1 或 1+"cd"+"ef"+"gh"
                                 或 "ab"+1*2+"cd"+"ef"+"gh"
            都将返回null

			注意:String str="A"+"B"+'c';也返回null，因为'c'是字符，不是字符串
			而str="A"+"B"+"c";就返回ABc
            */
            
            while (true) {
                if (tree.tag == JCTree.LITERAL) { //最左边的字符串
                    JCLiteral lit = (JCLiteral) tree;
                    if (lit.typetag == TypeTags.CLASS) {
                        StringBuffer sbuf =
                            new StringBuffer((String)lit.value);
                        while (buf.nonEmpty()) {
                            sbuf.append(buf.head);
                            buf = buf.tail;
                        }
                        return sbuf;
                    }
                } else if (tree.tag == JCTree.PLUS) {
                    JCBinary op = (JCBinary)tree;
                    DEBUG.P("op.rhs.tag="+op.rhs.getKind());
                    if (op.rhs.tag == JCTree.LITERAL) {
                        JCLiteral lit = (JCLiteral) op.rhs;
                        if (lit.typetag == TypeTags.CLASS) {
                            buf = buf.prepend((String) lit.value);
                            tree = op.lhs;
                            continue;
                        }
                    }
                }
                return null;
            }
	        
	        }finally{//我加上的
			DEBUG.P(0,this,"foldStrings(JCTree tree");
			}
        }

        /** optimization: To save allocating a new operand/operator stack
         *  for every binary operation, we use supplys.
         */
		//odStackSupply.size()与opStackSupply.size() = 表达式中的括号对数+1
		//如表达式:a=a*(b+a)，那么odStackSupply.size() = opStackSupply.size() = 2
        ListBuffer<JCExpression[]> odStackSupply = new ListBuffer<JCExpression[]>();
        ListBuffer<Token[]> opStackSupply = new ListBuffer<Token[]>();

        private JCExpression[] newOdStack() {
			//DEBUG.P(this,"newOdStack()");
			//DEBUG.P("odStackSupply.elems="+odStackSupply.elems);
			//DEBUG.P("odStackSupply.last="+odStackSupply.last);
			//DEBUG.P("if (odStackSupply.elems == odStackSupply.last)="+(odStackSupply.elems == odStackSupply.last));

            if (odStackSupply.elems == odStackSupply.last)
                odStackSupply.append(new JCExpression[infixPrecedenceLevels + 1]);
            JCExpression[] odStack = odStackSupply.elems.head;
            odStackSupply.elems = odStackSupply.elems.tail;
            return odStack;
        }

        private Token[] newOpStack() {
            if (opStackSupply.elems == opStackSupply.last)
                opStackSupply.append(new Token[infixPrecedenceLevels + 1]);
            Token[] opStack = opStackSupply.elems.head;
            opStackSupply.elems = opStackSupply.elems.tail;
            return opStack;
        }
    //下面的Expr是指Expression(参考18.1. The Grammar of the Java Programming Language)
    /** Expression3    = PrefixOp Expression3
     *                 | "(" Expr | TypeNoParams ")" Expression3
     *                 | Primary {Selector} {PostfixOp}
     *  Primary        = "(" Expression ")"
     *                 | Literal
     *                 | [TypeArguments] THIS [Arguments]
     *                 | [TypeArguments] SUPER SuperSuffix
     *                 | NEW [TypeArguments] Creator
     *                 | Ident { "." Ident }
     *                   [ "[" ( "]" BracketsOpt "." CLASS | Expression "]" )
     *                   | Arguments
     *                   | "." ( CLASS | THIS | [TypeArguments] SUPER Arguments | NEW [TypeArguments] InnerCreator )
     *                   ]
     *                 | BasicType BracketsOpt "." CLASS
     *  PrefixOp       = "++" | "--" | "!" | "~" | "+" | "-"
     *  PostfixOp      = "++" | "--"
     *  Type3          = Ident { "." Ident } [TypeArguments] {TypeSelector} BracketsOpt
     *                 | BasicType
     *  TypeNoParams3  = Ident { "." Ident } BracketsOpt
     *  Selector       = "." [TypeArguments] Ident [Arguments]
     *                 | "." THIS
     *                 | "." [TypeArguments] SUPER SuperSuffix
     *                 | "." NEW [TypeArguments] InnerCreator
     *                 | "[" Expression "]"
     *  TypeSelector   = "." Ident [TypeArguments]
     *  SuperSuffix    = Arguments | "." Ident [Arguments]
     */
    protected JCExpression term3() {
    	try {//我加上的
		DEBUG.P(this,"term3()");

        int pos = S.pos();
        JCExpression t;
        List<JCExpression> typeArgs = typeArgumentsOpt(EXPR);

        switch (S.token()) {
        case QUES: //TypeArguments不能像这样 expr=<?>
        	DEBUG.P("case QUES:");
			//如: ClassB<?> c=(ClassB<?>)cb;(见:case LPAREN)
            if ((mode & TYPE) != 0 && (mode & (TYPEARG|NOPARAMS)) == TYPEARG) {
                mode = TYPE;
                return typeArgument();
            } else
                return illegal();
                
                
        /*
        表达式以运算符: ++、--、BANG("!")、TILDE("~")、+、-  开始,
        这几个运算符都是一元运算符，从下面的代码“t = term3()”可以
        看出结合顺序是从右到左的,如:++--myInt 相当于:++(--myInt)
        ++--myInt将生成两棵JCUnary树
        
        但非常值得注意的是不管是++--myInt或是++(--myInt)这样的语法却是
        错误的(错误在Parser阶段没有发现):
        
        bin\mysrc\my\test\Test.java:98: 意外的类型
		需要： 变量
		找到： 值
		                ++(--myInt);
		                   ^
		1 错误
        */
        case PLUSPLUS: case SUBSUB: case BANG: case TILDE: case PLUS: case SUB:
        	DEBUG.P("(case PrefixOp) mode="+myMode(mode));
            if (typeArgs == null && (mode & EXPR) != 0) {
                Token token = S.token();
                S.nextToken();
                mode = EXPR;
                if (token == SUB &&
                    (S.token() == INTLITERAL || S.token() == LONGLITERAL) &&
                    S.radix() == 10) {
                    mode = EXPR;
                    t = literal(names.hyphen);
                } else {
                    t = term3();
                    return F.at(pos).Unary(unoptag(token), t);
                }
            } else return illegal();
            break;
        case LPAREN:
        	DEBUG.P("case LPAREN:");
            if (typeArgs == null && (mode & EXPR) != 0) {
                S.nextToken();
                mode = EXPR | TYPE | NOPARAMS;
                t = term3();
				//如: ClassB<?> c=(ClassB<?>)cb;
                if ((mode & TYPE) != 0 && S.token() == LT) {
                    // Could be a cast to a parameterized type
                    int op = JCTree.LT;
                    int pos1 = S.pos();
                    S.nextToken();
                    mode &= (EXPR | TYPE);
                    mode |= TYPEARG;
                    JCExpression t1 = term3();
                    if ((mode & TYPE) != 0 &&
                        (S.token() == COMMA || S.token() == GT)) {
                        mode = TYPE;
                        ListBuffer<JCExpression> args = new ListBuffer<JCExpression>();
                        args.append(t1);
                        while (S.token() == COMMA) {
                            S.nextToken();
                            args.append(typeArgument());
                        }
                        accept(GT);
                        t = F.at(pos1).TypeApply(t, args.toList());
                        checkGenerics();
                        t = bracketsOpt(toP(t));
                    } else if ((mode & EXPR) != 0) {
                        mode = EXPR;
                        t = F.at(pos1).Binary(op, t, term2Rest(t1, TreeInfo.shiftPrec));
                        t = termRest(term1Rest(term2Rest(t, TreeInfo.orPrec)));
                    } else {
                        accept(GT);
                    }
                } else {
                    t = termRest(term1Rest(term2Rest(t, TreeInfo.orPrec)));
                }
                accept(RPAREN);
                lastmode = mode;
                mode = EXPR;
				DEBUG.P("lastmode="+myMode(lastmode));
                if ((lastmode & EXPR) == 0) {//如：byte b=(byte)++i;
                    JCExpression t1 = term3();
                    return F.at(pos).TypeCast(t, t1);
                } else if ((lastmode & TYPE) != 0) {
                    switch (S.token()) {
                    /*case PLUSPLUS: case SUBSUB: */
                    case BANG: case TILDE:
                    case LPAREN: case THIS: case SUPER:
                    case INTLITERAL: case LONGLITERAL: case FLOATLITERAL:
                    case DOUBLELITERAL: case CHARLITERAL: case STRINGLITERAL:
                    case TRUE: case FALSE: case NULL:
                    case NEW: case IDENTIFIER: case ASSERT: case ENUM:
                    case BYTE: case SHORT: case CHAR: case INT:
                    case LONG: case FLOAT: case DOUBLE: case BOOLEAN: case VOID:
                        JCExpression t1 = term3();
                        return F.at(pos).TypeCast(t, t1);
                    }
                }
            } else return illegal();
            t = toP(F.at(pos).Parens(t));
            break;
        case THIS:
            if ((mode & EXPR) != 0) {
                mode = EXPR;
                t = to(F.at(pos).Ident(names._this));
                S.nextToken();
                if (typeArgs == null)
                    t = argumentsOpt(null, t);
                else
                    t = arguments(typeArgs, t);
                typeArgs = null;
            } else return illegal();
            break;
        case SUPER:
            if ((mode & EXPR) != 0) {
                mode = EXPR;
                t = to(superSuffix(typeArgs, F.at(pos).Ident(names._super)));
                typeArgs = null;
            } else return illegal();
            break;
        case INTLITERAL: case LONGLITERAL: case FLOATLITERAL: case DOUBLELITERAL:
        case CHARLITERAL: case STRINGLITERAL:
        case TRUE: case FALSE: case NULL:
            if (typeArgs == null && (mode & EXPR) != 0) {
                mode = EXPR;
                t = literal(names.empty);
            } else return illegal();
            break;
        case NEW:
            if (typeArgs != null) return illegal();
            if ((mode & EXPR) != 0) {
                mode = EXPR;
                S.nextToken();
                if (S.token() == LT) typeArgs = typeArguments();
                t = creator(pos, typeArgs);
                typeArgs = null;
            } else return illegal();
            break;
        case IDENTIFIER: case ASSERT: case ENUM:
            if (typeArgs != null) return illegal();
            t = toP(F.at(S.pos()).Ident(ident()));
            loop: while (true) {
                pos = S.pos();
                switch (S.token()) {
                case LBRACKET:
                    S.nextToken();
                    if (S.token() == RBRACKET) {
                        S.nextToken();
                        t = bracketsOpt(t);
                        t = toP(F.at(pos).TypeArray(t));
                        t = bracketsSuffix(t);//如:Class c=ParserTest[][].class;
                    } else {
                        if ((mode & EXPR) != 0) {
							//例:{ int a1[]={1,2}, a2; a1[0]=3; a2=a1[1]; }
                            mode = EXPR;
                            JCExpression t1 = term();
                            DEBUG.P("(case IDENTIFIER LBRACKET) t="+t+" t1="+t1);
                            t = to(F.at(pos).Indexed(t, t1));
                        }
                        accept(RBRACKET);
                    }
                    break loop;
                case LPAREN:
                    if ((mode & EXPR) != 0) {
                        mode = EXPR;
						DEBUG.P("(case IDENTIFIER LPAREN) t="+t+" typeArgs="+typeArgs);
						/*例:
						static class MemberClassB {
							static <R> R methodA(R r) { return r; }
						}
						{ MemberClassB.methodA(this); }
						{ MemberClassB.methodA("str"); }
						{ MemberClassB.<ParserTest>methodA(this); }
						{ MemberClassB.<String>methodA("str"); }

						//输出
						t=MemberClassB.methodA typeArgs=null
						t=MemberClassB.methodA typeArgs=null
						t=MemberClassB.methodA typeArgs=ParserTest
						t=MemberClassB.methodA typeArgs=String
						*/
                        t = arguments(typeArgs, t);
                        typeArgs = null;
                    }
                    break loop;
                case DOT:
                    S.nextToken();
                    typeArgs = typeArgumentsOpt(EXPR);
                    if ((mode & EXPR) != 0) {
                        switch (S.token()) {
                        case CLASS:
                            if (typeArgs != null) return illegal();
                            mode = EXPR;
                            t = to(F.at(pos).Select(t, names._class));
                            S.nextToken();
                            break loop;
                        case THIS:
							/*例
							class MemberClassC {
								{ ParserTest.this(); } //有错
								{ ParserTest pt=ParserTest.this; } //正确
							}
							*/
							DEBUG.P("(case IDENTIFIER THIS) t="+t+" typeArgs="+typeArgs);
                            if (typeArgs != null) return illegal();
                            mode = EXPR;
                            t = to(F.at(pos).Select(t, names._this));
                            S.nextToken();
                            break loop;
                        case SUPER:
							DEBUG.P("(case IDENTIFIER SUPER) t="+t+" typeArgs="+typeArgs);
							/*例
							int superField;
							<T> ParserTest(T t){}
							static <T> void methodB(T t){}
							class MemberClassD extends ParserTest {
								MemberClassD() { <String>super("str"); }
								{ int sf=MemberClassD.super.superField; }
								{ MemberClassD.super.<String>methodB("str"); }
							}
							*/
                            mode = EXPR;
                            t = to(F.at(pos).Select(t, names._super));
                            t = superSuffix(typeArgs, t);
                            typeArgs = null;
                            break loop;
                        case NEW:
							/*例子
							class MemberClassE {
								class MemberClassF<T> {
									<T> MemberClassF(T t){}
								}
							}
							{
								MemberClassE me=new MemberClassE();
								MemberClassE.MemberClassF<Long> mf=me.new <String>MemberClassF<Long>("str");
								//类型的格式不正确，缺少某些参数(在Check类中检查)
								//MemberClassE.MemberClassF mf=me.new <String>MemberClassF<Long>("str");
							}
							*/
                            if (typeArgs != null) return illegal();
                            mode = EXPR;
                            int pos1 = S.pos();
                            S.nextToken();
                            if (S.token() == LT) typeArgs = typeArguments();
                            t = innerCreator(pos1, typeArgs, t);
                            typeArgs = null;
                            break loop;
                        }
                    }
                    // typeArgs saved for next loop iteration.
                    t = toP(F.at(pos).Select(t, ident()));
                    break;
                default:
                    break loop;
                }
            }
            if (typeArgs != null) illegal();
            t = typeArgumentsOpt(t);
            break;
        case BYTE: case SHORT: case CHAR: case INT: case LONG: case FLOAT:
        case DOUBLE: case BOOLEAN:
            if (typeArgs != null) illegal();
            t = bracketsSuffix(bracketsOpt(basicType()));
            break;
        case VOID:
            if (typeArgs != null) illegal();
            if ((mode & EXPR) != 0) {
                S.nextToken();
                if (S.token() == DOT) {
                    JCPrimitiveTypeTree ti = toP(F.at(pos).TypeIdent(TypeTags.VOID));
                    t = bracketsSuffix(ti);
                } else {
                    return illegal(pos);
                }
            } else {
                return illegal();
            }
            break;
        default:
            return illegal();
        }
        if (typeArgs != null) illegal();
        while (true) { //对应{Selector}
            int pos1 = S.pos();
            if (S.token() == LBRACKET) {
                S.nextToken();
				DEBUG.P("mode="+myMode(mode));
                if ((mode & TYPE) != 0) {
                    int oldmode = mode;
                    mode = TYPE;
                    if (S.token() == RBRACKET) {
                        S.nextToken();
                        t = bracketsOpt(t);
                        t = toP(F.at(pos1).TypeArray(t));
                        return t;
                    }
                    mode = oldmode;
                }
                if ((mode & EXPR) != 0) {
                    mode = EXPR;
                    JCExpression t1 = term();
					//对于像这样的二维数组
					//int[][] ii2={{1,2},{3,4}};
					//int i2=ii2[1][2]; //主要是这句
					//先在case IDENTIFIER中处理ii2[1]，再转到这里处理[2]
					//(while (true) t=ii2[1] t1=2
					//Indexed t=ii2[1][2]
					DEBUG.P("(while (true) t="+t+" t1="+t1);
                    t = to(F.at(pos1).Indexed(t, t1));
					DEBUG.P("Indexed t="+t);
                }
                accept(RBRACKET);
            } else if (S.token() == DOT) {
                S.nextToken();
                typeArgs = typeArgumentsOpt(EXPR);
                if (S.token() == SUPER && (mode & EXPR) != 0) {
                    mode = EXPR;
                    t = to(F.at(pos1).Select(t, names._super));
                    S.nextToken();
                    t = arguments(typeArgs, t);
                    typeArgs = null;
                } else if (S.token() == NEW && (mode & EXPR) != 0) {
                    if (typeArgs != null) return illegal();
                    mode = EXPR;
                    int pos2 = S.pos();
                    S.nextToken();
                    if (S.token() == LT) typeArgs = typeArguments();
                    t = innerCreator(pos2, typeArgs, t);
                    typeArgs = null;
                } else {
                    t = toP(F.at(pos1).Select(t, ident()));
                    t = argumentsOpt(typeArgs, typeArgumentsOpt(t));
                    typeArgs = null;
                }
            } else {
                break;
            }
        }
		 //对应{PostfixOp}
        while ((S.token() == PLUSPLUS || S.token() == SUBSUB) && (mode & EXPR) != 0) {
			/* 在语法分析阶段:i++--++--是正确的，并且从左到右生成JCUnary
			PostfixOp t=i++
			PostfixOp t=i++--
			PostfixOp t=i++--++
			PostfixOp t=i++--++--
			----------------------------------------------
			test\parser\ParserTest.java:200: 意外的类型
			需要： 变量
			找到： 值
							int i2=i++--++--;
									^
			1 错误
			*/
            mode = EXPR;
            t = to(F.at(S.pos()).Unary(
                  S.token() == PLUSPLUS ? JCTree.POSTINC : JCTree.POSTDEC, t));
			DEBUG.P("PostfixOp t="+t);
            S.nextToken();
        }
        return toP(t);
        
        }finally{//我加上的
		DEBUG.P(0,this,"term3()");
		}
    }

    /** SuperSuffix = Arguments | "." [TypeArguments] Ident [Arguments]
     */
    JCExpression superSuffix(List<JCExpression> typeArgs, JCExpression t) {
    	DEBUG.P(this,"superSuffix(2)");
        S.nextToken();
        if (S.token() == LPAREN || typeArgs != null) {
            t = arguments(typeArgs, t);
        } else {
            int pos = S.pos();
            accept(DOT);
            typeArgs = (S.token() == LT) ? typeArguments() : null;
            t = toP(F.at(pos).Select(t, ident()));
            t = argumentsOpt(typeArgs, t);
        }
        DEBUG.P(0,this,"superSuffix(2)");
        return t;
    }

    /** BasicType = BYTE | SHORT | CHAR | INT | LONG | FLOAT | DOUBLE | BOOLEAN
     */
    JCPrimitiveTypeTree basicType() {
    	DEBUG.P(this,"basicType");
    	DEBUG.P("S.token()="+S.token());
    	
        JCPrimitiveTypeTree t = to(F.at(S.pos()).TypeIdent(typetag(S.token())));
        S.nextToken();
        
        DEBUG.P(0,this,"basicType");
        return t;
    }

    /** ArgumentsOpt = [ Arguments ]
     */
    JCExpression argumentsOpt(List<JCExpression> typeArgs, JCExpression t) {
    	try {//我加上的
		DEBUG.P(this,"argumentsOpt(2)");
		DEBUG.P("mode="+myMode(mode)+" S.token()="+S.token()+" typeArgs="+typeArgs);
		
        if ((mode & EXPR) != 0 && S.token() == LPAREN || typeArgs != null) {
            mode = EXPR;
            return arguments(typeArgs, t);
        } else {
            return t;
        }
        
        }finally{//我加上的
		DEBUG.P(0,this,"argumentsOpt(2)");
		}
    }

    /** Arguments = "(" [Expression { COMMA Expression }] ")"
     */
    List<JCExpression> arguments() {
    	DEBUG.P(this,"arguments()");
		DEBUG.P("S.token()="+S.token());
		
        ListBuffer<JCExpression> args = lb();
        if (S.token() == LPAREN) {
            S.nextToken();
            if (S.token() != RPAREN) {
                args.append(expression());
                while (S.token() == COMMA) {
                    S.nextToken();
                    args.append(expression());
                }
            }
            accept(RPAREN);
        } else {
            syntaxError(S.pos(), "expected", keywords.token2string(LPAREN));
        }
        
        DEBUG.P(0,this,"arguments()");
        return args.toList();
    }

    JCMethodInvocation arguments(List<JCExpression> typeArgs, JCExpression t) {
        int pos = S.pos();
        List<JCExpression> args = arguments();
        return toP(F.at(pos).Apply(typeArgs, t, args));
    }

    /**  TypeArgumentsOpt = [ TypeArguments ]
     */
    JCExpression typeArgumentsOpt(JCExpression t) {
    	try {//我加上的
		DEBUG.P(this,"typeArgumentsOpt(JCExpression t)");
		DEBUG.P("t="+t);
		DEBUG.P("S.token()="+S.token());
		/*这里必须是参数化的类型声明
		class MemberClassH<T> {}
		MemberClassH<?> Mh1;
		MemberClassH<String> Mh2;
		MemberClassH<? extends Number> Mh3;
		*/
		
        if (S.token() == LT &&
            (mode & TYPE) != 0 &&
            (mode & NOPARAMS) == 0) {
            mode = TYPE;
            checkGenerics();
            return typeArguments(t);
        } else {
            return t;
        }

        }finally{//我加上的
		DEBUG.P(0,this,"typeArgumentsOpt(JCExpression t)");
		}       
    }
    
    List<JCExpression> typeArgumentsOpt() {
    	try {//我加上的
		DEBUG.P(this,"typeArgumentsOpt()");
		
        return typeArgumentsOpt(TYPE);
        
        }finally{//我加上的
		DEBUG.P(0,this,"typeArgumentsOpt()");
		}
    }

    List<JCExpression> typeArgumentsOpt(int useMode) {
    	try {//我加上的
        DEBUG.P(this,"typeArgumentsOpt(int useMode)");
        DEBUG.P("useMode="+myMode(useMode));
        DEBUG.P("mode="+myMode(mode));
        DEBUG.P("S.token()="+S.token());

        if (S.token() == LT) {
            checkGenerics();
            if ((mode & useMode) == 0 ||
                (mode & NOPARAMS) != 0) {
                illegal();
            }
            mode = useMode;
            return typeArguments();
        }
        return null;
        
        }finally{//我加上的
        DEBUG.P(0,this,"typeArgumentsOpt(int useMode)");
        }
    }

    /**  TypeArguments  = "<" TypeArgument {"," TypeArgument} ">"
     */
    List<JCExpression> typeArguments() {
    	try {//我加上的
		DEBUG.P(this,"typeArguments()");
		DEBUG.P("S.token()="+S.token()+" mode="+myMode(mode));
		
        ListBuffer<JCExpression> args = lb();
        if (S.token() == LT) {
            S.nextToken();
            //TypeArguments不能像这样 expr=<?>
            
            //只有mode不含EXPR时((mode & EXPR) == 0)，
            //才能在“<>”中放入“？”号
            args.append(((mode & EXPR) == 0) ? typeArgument() : type());
            while (S.token() == COMMA) {
                S.nextToken();
                args.append(((mode & EXPR) == 0) ? typeArgument() : type());
            }
            switch (S.token()) {
            case GTGTGTEQ:
                S.token(GTGTEQ);
                break;
            case GTGTEQ:
                S.token(GTEQ);
                break;
            case GTEQ:
                S.token(EQ);
                break;
            case GTGTGT:
                S.token(GTGT);
                break;
            case GTGT:
                S.token(GT);
                break;
            default:
                accept(GT);
                break;
            }
        } else {
            syntaxError(S.pos(), "expected", keywords.token2string(LT));
        }
        return args.toList();
        
        }finally{//我加上的
		DEBUG.P(0,this,"typeArguments()");
		}
    }

    /** TypeArgument = Type
     *               | "?"
     *               | "?" EXTENDS Type {"&" Type}
     *               | "?" SUPER Type
     */
     
     /*
     在Java Language Specification, Third Edition
	 18.1. The Grammar of the Java Programming Language
	 中的定义如下:
     TypeArgument:
      Type
      ? [( extends | super ) Type]
     所以上面的语法是错误的。
     "?" EXTENDS Type {"&" Type} 应改成 "?" EXTENDS Type
     */
    JCExpression typeArgument() {
    	try {//我加上的
		DEBUG.P(this,"typeArgument()");
		
        if (S.token() != QUES) return type();
		//以下JCWildcard树结点的开始位置pos是从"?"号这个token的开始位置算起的
        int pos = S.pos();
        S.nextToken();
        if (S.token() == EXTENDS) {
            TypeBoundKind t = to(F.at(S.pos()).TypeBoundKind(BoundKind.EXTENDS));
            S.nextToken();
            return F.at(pos).Wildcard(t, type());
        } else if (S.token() == SUPER) {
            TypeBoundKind t = to(F.at(S.pos()).TypeBoundKind(BoundKind.SUPER));
            S.nextToken();
            return F.at(pos).Wildcard(t, type());
        } else if (S.token() == IDENTIFIER) {
			/*例子:
			class MemberClassH<T> {}
			MemberClassH<? mh;
			*/
            //error recovery
            reportSyntaxError(S.prevEndPos(), "expected3",
                    keywords.token2string(GT),
                    keywords.token2string(EXTENDS),
                    keywords.token2string(SUPER));
            TypeBoundKind t = F.at(Position.NOPOS).TypeBoundKind(BoundKind.UNBOUND);
            JCExpression wc = toP(F.at(pos).Wildcard(t, null));
            JCIdent id = toP(F.at(S.pos()).Ident(ident()));
            return F.at(pos).Erroneous(List.<JCTree>of(wc, id));
        } else {
			/*如果是这样的例子:
			class MemberClassH<T> {}
			MemberClassH<? <;

			那么在这个方法里并不报错，照样生成UNBOUND类型的JCWildcard，
			而是将不合法的"<"字符留给调用这个方法的调用者自行处理，
			比如通过typeArguments()调用这个方法时，在typeArguments()里的
			"default:
                accept(GT);"这段代码里就会报告"需要 >"这样的错误提示
			*/
            TypeBoundKind t = F.at(Position.NOPOS).TypeBoundKind(BoundKind.UNBOUND);
            return toP(F.at(pos).Wildcard(t, null));
        }
        
        }finally{//我加上的
		DEBUG.P(0,this,"typeArgument()");
		}
    }

    JCTypeApply typeArguments(JCExpression t) {
    	try {//我加上的
		DEBUG.P(this,"typeArguments(JCExpression t)");
		
        int pos = S.pos();
        List<JCExpression> args = typeArguments();
        return toP(F.at(pos).TypeApply(t, args));
        
        }finally{//我加上的
		DEBUG.P(0,this,"typeArguments(JCExpression t)");
		}
    }
    
    /*
    bracketsOpt和bracketsOptCont这两个方法用来生成一棵JCArrayTypeTree
    如:int a[]将对应一棵elemtype为int的JCArrayTypeTree；
    如:int a[][]将对应一棵elemtype为int型数组的JCArrayTypeTree；
    多维数组通过bracketsOpt和bracketsOptCont这两个方法互相调用实现
    
    int a[][]用JCArrayTypeTree表示为"
    JCArrayTypeTree = {
    	JCExpression elemtype = {
    		JCArrayTypeTree = {
    			JCExpression elemtype = int;
    		}
    	}
    }
    
    int a[][]与int[][] a这两种表示方式都是一样的
    */
    
    /** BracketsOpt = {"[" "]"}
     */
    private JCExpression bracketsOpt(JCExpression t) {
    	try {//我加上的
		DEBUG.P(this,"bracketsOpt(JCExpression t)");
		DEBUG.P("t="+t);
		DEBUG.P("S.token()="+S.token());
		
        if (S.token() == LBRACKET) {
            int pos = S.pos();
            S.nextToken();
            t = bracketsOptCont(t, pos);
            F.at(pos);
        }
        DEBUG.P("t="+t);
        return t;
        
        }finally{//我加上的
		DEBUG.P(0,this,"bracketsOpt(JCExpression t)");
		}    
    }

    private JCArrayTypeTree bracketsOptCont(JCExpression t, int pos) {
        accept(RBRACKET);
        t = bracketsOpt(t);
        return toP(F.at(pos).TypeArray(t));
    }

    /** BracketsSuffixExpr = "." CLASS
     *  BracketsSuffixType =
     */
    JCExpression bracketsSuffix(JCExpression t) {
    	DEBUG.P(this,"bracketsSuffix(JCExpression t)");
		DEBUG.P("t="+t);
		DEBUG.P("mode="+myMode(mode)+" S.token()="+S.token());
		//例:Class c=int[][].class;
        if ((mode & EXPR) != 0 && S.token() == DOT) {
            mode = EXPR;
            int pos = S.pos();
            S.nextToken();
            accept(CLASS);
            if (S.pos() == errorEndPos) {
                // error recovery
                Name name = null;
                if (S.token() == IDENTIFIER) {//例:Class c=int[][].classA;
                    name = S.name();
                    S.nextToken();
                } else {//例:Class c=int[][].char;//可以触发两次错语，但只报一次
                    name = names.error;
                }
				DEBUG.P("name="+name);
                t = F.at(pos).Erroneous(List.<JCTree>of(toP(F.at(pos).Select(t, name))));
            } else {
                t = toP(F.at(pos).Select(t, names._class));
            }
        } else if ((mode & TYPE) != 0) {
            mode = TYPE; //注意这里 如:public int[][] i1={{1,2},{3,4}};
        } else {
			//例:Class c=int[][];
			//例:Class c=int[][].123;
            syntaxError(S.pos(), "dot.class.expected");
        }
        
		DEBUG.P("t="+t);
		DEBUG.P("mode="+myMode(mode)+" S.token()="+S.token());
        DEBUG.P(0,this,"bracketsSuffix(JCExpression t)");
        return t;
    }

    /** Creator = Qualident [TypeArguments] ( ArrayCreatorRest | ClassCreatorRest )
     */
    JCExpression creator(int newpos, List<JCExpression> typeArgs) {
    	try {//我加上的
		DEBUG.P(this,"creator(2)");
		
        switch (S.token()) {
        case BYTE: case SHORT: case CHAR: case INT: case LONG: case FLOAT:
        case DOUBLE: case BOOLEAN:
            if (typeArgs == null)
                return arrayCreatorRest(newpos, basicType());
            break;
        default:
        }
        JCExpression t = qualident();
        int oldmode = mode;
        mode = TYPE;
        if (S.token() == LT) {
            checkGenerics();
            t = typeArguments(t);
        }
        while (S.token() == DOT) {
            int pos = S.pos();
            S.nextToken();
            t = toP(F.at(pos).Select(t, ident()));
            if (S.token() == LT) {
                checkGenerics();
                t = typeArguments(t);
            }
        }
        mode = oldmode;
        DEBUG.P("S.token()="+S.token());
        DEBUG.P("typeArgs="+typeArgs);
        if (S.token() == LBRACKET) {
            JCExpression e = arrayCreatorRest(newpos, t);
            if (typeArgs != null) {
                int pos = newpos;
                if (!typeArgs.isEmpty() && typeArgs.head.pos != Position.NOPOS) {
                    // note: this should always happen but we should
                    // not rely on this as the parser is continuously
                    // modified to improve error recovery.
                    pos = typeArgs.head.pos;
                }
                setErrorEndPos(S.prevEndPos());
				//这个错误key在中文properties文件中没有
				/*例子:
				class MemberClassG<T> {<T> MemberClassG(T t){}}
				{ MemberClassG[] mg=new <Long>MemberClassG<String>[]{};}
				*/
                reportSyntaxError(pos, "cannot.create.array.with.type.arguments");
                return toP(F.at(newpos).Erroneous(typeArgs.prepend(e)));
            }
            return e;
        } else if (S.token() == LPAREN) {
            return classCreatorRest(newpos, null, typeArgs, t);
        } else {
            reportSyntaxError(S.pos(), "expected2",
                               keywords.token2string(LPAREN),
                               keywords.token2string(LBRACKET));
            t = toP(F.at(newpos).NewClass(null, typeArgs, t, List.<JCExpression>nil(), null));
            return toP(F.at(newpos).Erroneous(List.<JCTree>of(t)));
        }
        
    	}finally{//我加上的
		DEBUG.P(0,this,"creator(2)");
		}
    }

    /** InnerCreator = Ident [TypeArguments] ClassCreatorRest
     */
    JCExpression innerCreator(int newpos, List<JCExpression> typeArgs, JCExpression encl) {
        try {//我加上的
		DEBUG.P(this,"innerCreator(3)");
		DEBUG.P("typeArgs="+typeArgs);
		DEBUG.P("encl="+encl);
		
        JCExpression t = toP(F.at(S.pos()).Ident(ident()));
        if (S.token() == LT) {
            checkGenerics();
            t = typeArguments(t);
        }
        return classCreatorRest(newpos, encl, typeArgs, t);
        
        }finally{//我加上的
		DEBUG.P(0,this,"innerCreator(3)");
		}
    }

    /** ArrayCreatorRest = "[" ( "]" BracketsOpt ArrayInitializer
     *                         | Expression "]" {"[" Expression "]"} BracketsOpt )
     */
    JCExpression arrayCreatorRest(int newpos, JCExpression elemtype) {
    	try {//我加上的
        DEBUG.P(this,"arrayCreatorRest(2)");
        DEBUG.P("newpos="+newpos);
        DEBUG.P("elemtype="+elemtype);
        
        accept(LBRACKET);
        if (S.token() == RBRACKET) {
            accept(RBRACKET);
            elemtype = bracketsOpt(elemtype);
            if (S.token() == LBRACE) {
                return arrayInitializer(newpos, elemtype);
            } else {
                //例:int a[]=new int[];
                //src/my/test/ParserTest.java:6: 缺少数组维数
                //int a[]=new int[];
                //                 ^

                return syntaxError(S.pos(), "array.dimension.missing");
            }
        } else {
            //当指定了数组维数后就不能用大括号'{}'对数组进行初始化了
            //以下两例都不符合语法:
            //int a[]=new int[2]{1,2};
            //int b[][]=new int[2][3]{{1,2,3},{4,5,6}};
            
            ListBuffer<JCExpression> dims = new ListBuffer<JCExpression>();
            //例:int a[]=new int[8][4];
            dims.append(expression());
            accept(RBRACKET);
            while (S.token() == LBRACKET) {
                int pos = S.pos();
                S.nextToken();
				//int b[][]=new int[2][];      //无错
				//int c[][][]=new int[2][][3]; //有错
				//第一维数组的大小必须指定，二、三......维之后的可以是[][][]
                if (S.token() == RBRACKET) {
                    elemtype = bracketsOptCont(elemtype, pos);
                } else {
                    dims.append(expression());
                    accept(RBRACKET);
                }
            }
            DEBUG.P("dims.toList()="+dims.toList());
            DEBUG.P("elemtype="+elemtype);
            return toP(F.at(newpos).NewArray(elemtype, dims.toList(), null));
        }
        
        }finally{//我加上的
        DEBUG.P(0,this,"arrayCreatorRest(2)");
        }
    }

    /** ClassCreatorRest = Arguments [ClassBody]
     */
    JCExpression classCreatorRest(int newpos,
                                  JCExpression encl,
                                  List<JCExpression> typeArgs,
                                  JCExpression t)
    {
    	try {//我加上的
		DEBUG.P(this,"classCreatorRest(4)");
		DEBUG.P("encl="+encl);
		DEBUG.P("typeArgs="+typeArgs);
		DEBUG.P("t="+t);
		
        List<JCExpression> args = arguments();
        JCClassDecl body = null;
        if (S.token() == LBRACE) {
            int pos = S.pos();
            List<JCTree> defs = classOrInterfaceBody(names.empty, false);
            JCModifiers mods = F.at(Position.NOPOS).Modifiers(0);
            body = toP(F.at(pos).AnonymousClassDef(mods, defs));
        }
        return toP(F.at(newpos).NewClass(encl, typeArgs, t, args, body));
        
        }finally{//我加上的
		DEBUG.P(0,this,"classCreatorRest(4)");
		}
    }

    /** ArrayInitializer = "{" [VariableInitializer {"," VariableInitializer}] [","] "}"
     */
    JCExpression arrayInitializer(int newpos, JCExpression t) {
    	try {//我加上的
		DEBUG.P(this,"arrayInitializer(2)");
		
        accept(LBRACE);
        ListBuffer<JCExpression> elems = new ListBuffer<JCExpression>();
        if (S.token() == COMMA) {
            S.nextToken();
        } else if (S.token() != RBRACE) {
        	//arrayInitializer()与variableInitializer()两者相互调用
        	//可以实现多维数组(如{{1,2},{3,4}}的初始化
            elems.append(variableInitializer());
            while (S.token() == COMMA) {
                S.nextToken();
                if (S.token() == RBRACE) break;
                elems.append(variableInitializer());
            }
        }
        accept(RBRACE);
        return toP(F.at(newpos).NewArray(t, List.<JCExpression>nil(), elems.toList()));
    	
    	}finally{//我加上的
		DEBUG.P(0,this,"arrayInitializer(2)");
		}  
    }

    /** VariableInitializer = ArrayInitializer | Expression
     */
    public JCExpression variableInitializer() {
    	try {//我加上的
		DEBUG.P(this,"variableInitializer()");
		        
        return S.token() == LBRACE ? arrayInitializer(S.pos(), null) : expression();

		}finally{//我加上的
		DEBUG.P(0,this,"variableInitializer()");
		}    
    }

    /** ParExpression = "(" Expression ")"
     */
    JCExpression parExpression() {
    	DEBUG.P(this,"parExpression()");
        accept(LPAREN);
        JCExpression t = expression();
        accept(RPAREN);
        DEBUG.P(0,this,"parExpression()");
        return t;
    }

    /** Block = "{" BlockStatements "}"
     */
    JCBlock block(int pos, long flags) {
    	DEBUG.P(this,"block(int pos, long flags)");
		DEBUG.P("pos="+pos+" flags="+flags+" modifiers=("+Flags.toString(flags)+")");
		
        accept(LBRACE);
        List<JCStatement> stats = blockStatements();
        
        JCBlock t = F.at(pos).Block(flags, stats);
        while (S.token() == CASE || S.token() == DEFAULT) {
        	/*
        	如下代码:
        	{
				case;
			}
			错误提示:“单个 case”或“单个 default”
			*/
            syntaxError("orphaned", keywords.token2string(S.token()));
            switchBlockStatementGroups();
        }
        // the Block node has a field "endpos" for first char of last token, which is
        // usually but not necessarily the last char of the last token.
        t.endpos = S.pos();
        accept(RBRACE);
        
        DEBUG.P(1,this,"block(int pos, long flags)");
        return toP(t);
    }

    public JCBlock block() {
        return block(S.pos(), 0);
    }

    /** BlockStatements = { BlockStatement }
     *  BlockStatement  = LocalVariableDeclarationStatement
     *                  | ClassOrInterfaceOrEnumDeclaration
     *                  | [Ident ":"] Statement
     *  LocalVariableDeclarationStatement
     *                  = { FINAL | '@' Annotation } Type VariableDeclarators ";"
     */
    @SuppressWarnings("fallthrough")
    List<JCStatement> blockStatements() {
    	try {//我加上的
		DEBUG.P(this,"blockStatements()");
		
//todo: skip to anchor on error(?)
        int lastErrPos = -1;
        ListBuffer<JCStatement> stats = new ListBuffer<JCStatement>();
        while (true) {
            int pos = S.pos();
            DEBUG.P("S.token()="+S.token());
            switch (S.token()) {
            case RBRACE: case CASE: case DEFAULT: case EOF:
                return stats.toList();
            case LBRACE: case IF: case FOR: case WHILE: case DO: case TRY:
            case SWITCH: case SYNCHRONIZED: case RETURN: case THROW: case BREAK:
            case CONTINUE: case SEMI: case ELSE: case FINALLY: case CATCH:
                stats.append(statement());
                break;
            case MONKEYS_AT:
            case FINAL: {
				//枚举类型不能为本地类型(这里与下面的case ENUM: case ASSERT:有BUG)
				//enum MyEnum {}              //有错
				//final enum MyEnum {}        //有错
				//@MyAnnotation enum MyEnum {}//无错
            	DEBUG.P("MONKEYS_AT 或 FINAL开头：");
                String dc = S.docComment();
                JCModifiers mods = modifiersOpt();
                if (S.token() == INTERFACE ||
                    S.token() == CLASS ||
                    allowEnums && S.token() == ENUM) {
                    stats.append(classOrInterfaceOrEnumDeclaration(mods, dc));
                } else {
                    JCExpression t = type();
                    stats.appendList(variableDeclarators(mods, t,
                                                         new ListBuffer<JCStatement>()));
                    // A "LocalVariableDeclarationStatement" subsumes the terminating semicolon
                    storeEnd(stats.elems.last(), S.endPos());
                    accept(SEMI);
                }
                break;
            }
            case ABSTRACT: case STRICTFP: {
                String dc = S.docComment();
                JCModifiers mods = modifiersOpt();
                stats.append(classOrInterfaceOrEnumDeclaration(mods, dc));
                break;
            }
            case INTERFACE:
            case CLASS:
                stats.append(classOrInterfaceOrEnumDeclaration(modifiersOpt(),
                                                               S.docComment()));
                break;
            case ENUM:
            case ASSERT:
                if (allowEnums && S.token() == ENUM) {
                    log.error(S.pos(), "local.enum");//枚举类型不能为本地类型
                    stats.
                        append(classOrInterfaceOrEnumDeclaration(modifiersOpt(),
                                                                 S.docComment()));
                    break;
                } else if (allowAsserts && S.token() == ASSERT) {
                    stats.append(statement());
                    break;
                }
                /* fall through to default */
            default:
            	DEBUG.P("default");
                Name name = S.name(); //只对标签语句有用
                DEBUG.P("name="+name);
                JCExpression t = term(EXPR | TYPE);
                DEBUG.P("S.token()="+S.token());
                DEBUG.P("lastmode="+myMode(lastmode));
                
                if (S.token() == COLON && t.tag == JCTree.IDENT) {//标签语句
                    S.nextToken();
                    JCStatement stat = statement();
                    stats.append(F.at(pos).Labelled(name, stat));
                } else if ((lastmode & TYPE) != 0 &&
                           (S.token() == IDENTIFIER ||
                            S.token() == ASSERT ||
                            S.token() == ENUM)) { //不以MONKEYS_AT 或 FINAL开头的本地变量
                    pos = S.pos();
                    JCModifiers mods = F.at(Position.NOPOS).Modifiers(0);
                    F.at(pos);
                    stats.appendList(variableDeclarators(mods, t,
                                                         new ListBuffer<JCStatement>()));
                    // A "LocalVariableDeclarationStatement" subsumes the terminating semicolon
                    storeEnd(stats.elems.last(), S.endPos());
                    accept(SEMI);
                } else {
			/*
			合法的表达式语句:
			++a，--a，a++，a--，
			a=b，
			a|=b，a^=b，a&=b，
			a<<=b，a>>=b，a>>>=b，a+=b，a-=b，a*=b，a/=b，a%=b，
			a(),new a()
			*/
                    // This Exec is an "ExpressionStatement"; it subsumes the terminating semicolon
                    stats.append(to(F.at(pos).Exec(checkExprStat(t))));
                    accept(SEMI);
                }
            } //switch结束

            // error recovery
            if (S.pos() == lastErrPos)
                return stats.toList();
            if (S.pos() <= errorEndPos) {
                skip(false, true, true, true);
                lastErrPos = S.pos();
            }

            // ensure no dangling /** @deprecated */ active
            S.resetDeprecatedFlag();
        } //while结束
        
        }finally{//我加上的
		DEBUG.P(0,this,"blockStatements()");
		}
    }

    /** Statement =
     *       Block
     *     | IF ParExpression Statement [ELSE Statement]
     *     | FOR "(" ForInitOpt ";" [Expression] ";" ForUpdateOpt ")" Statement
     *     | FOR "(" FormalParameter : Expression ")" Statement
     *     | WHILE ParExpression Statement
     *     | DO Statement WHILE ParExpression ";"
     *     | TRY Block ( Catches | [Catches] FinallyPart )
     *     | SWITCH ParExpression "{" SwitchBlockStatementGroups "}"
     *     | SYNCHRONIZED ParExpression Block
     *     | RETURN [Expression] ";"
     *     | THROW Expression ";"
     *     | BREAK [Ident] ";"
     *     | CONTINUE [Ident] ";"
     *     | ASSERT Expression [ ":" Expression ] ";"
     *     | ";"
     *     | ExpressionStatement
     *     | Ident ":" Statement
     */
    @SuppressWarnings("fallthrough")
    public JCStatement statement() {
    	try {//我加上的
		DEBUG.P(this,"statement()");

        int pos = S.pos();
        switch (S.token()) {
        case LBRACE:
            return block();
        case IF: {
            S.nextToken();
            JCExpression cond = parExpression();
            JCStatement thenpart = statement();
            JCStatement elsepart = null;
            if (S.token() == ELSE) {
                S.nextToken();
                elsepart = statement();
            }
            return F.at(pos).If(cond, thenpart, elsepart);
        }
        case FOR: {
            S.nextToken();
            accept(LPAREN);
            List<JCStatement> inits = S.token() == SEMI ? List.<JCStatement>nil() : forInit();
            DEBUG.P("inits.length()="+inits.length());
            if (inits.length() == 1 &&
                inits.head.tag == JCTree.VARDEF &&
                ((JCVariableDecl) inits.head).init == null &&
                S.token() == COLON) {
                checkForeach();
                JCVariableDecl var = (JCVariableDecl)inits.head;
                accept(COLON);
                JCExpression expr = expression();
                accept(RPAREN);
                JCStatement body = statement();
                return F.at(pos).ForeachLoop(var, expr, body);
            } else {
                accept(SEMI);
                JCExpression cond = S.token() == SEMI ? null : expression();
                accept(SEMI);
                List<JCExpressionStatement> steps = S.token() == RPAREN ? List.<JCExpressionStatement>nil() : forUpdate();
                accept(RPAREN);
                JCStatement body = statement();
                return F.at(pos).ForLoop(inits, cond, steps, body);
            }
        }
        case WHILE: {
            S.nextToken();
            JCExpression cond = parExpression();
            JCStatement body = statement();
            return F.at(pos).WhileLoop(cond, body);
        }
        case DO: {
            S.nextToken();
            JCStatement body = statement();
            accept(WHILE);
            JCExpression cond = parExpression();
            JCDoWhileLoop t = to(F.at(pos).DoLoop(body, cond));
            accept(SEMI);
            return t;
        }
        case TRY: {
            S.nextToken();
            JCBlock body = block();
            ListBuffer<JCCatch> catchers = new ListBuffer<JCCatch>();
            JCBlock finalizer = null;
            if (S.token() == CATCH || S.token() == FINALLY) {
                while (S.token() == CATCH) catchers.append(catchClause());
                if (S.token() == FINALLY) {
                    S.nextToken();
                    finalizer = block();
                }
            } else {
                log.error(pos, "try.without.catch.or.finally");
            }
            return F.at(pos).Try(body, catchers.toList(), finalizer);
        }
        case SWITCH: {
            S.nextToken();
            JCExpression selector = parExpression();
            accept(LBRACE);
            List<JCCase> cases = switchBlockStatementGroups();
            JCSwitch t = to(F.at(pos).Switch(selector, cases));
            accept(RBRACE);
            return t;
        }
        case SYNCHRONIZED: {
            S.nextToken();
            JCExpression lock = parExpression();
            JCBlock body = block();
            return F.at(pos).Synchronized(lock, body);
        }
        case RETURN: {
            S.nextToken();
            JCExpression result = S.token() == SEMI ? null : expression();
            JCReturn t = to(F.at(pos).Return(result));
            accept(SEMI);
            return t;
        }
        case THROW: {
            S.nextToken();
            JCExpression exc = expression();
            JCThrow t = to(F.at(pos).Throw(exc));
            accept(SEMI);
            return t;
        }
        case BREAK: {
            S.nextToken();
            /*
            bin\mysrc\my\test\Test.java:80: 从版本 1.4 开始，'assert' 是一个关键字，但不能用
			作标识符
			（请使用 -source 1.3 或更低版本以便将 'assert' 用作标识符）
			                        break assert;
			                              ^
			1 错误
			*/
            Name label = (S.token() == IDENTIFIER || S.token() == ASSERT || S.token() == ENUM) ? ident() : null;
            JCBreak t = to(F.at(pos).Break(label));
            accept(SEMI);
            return t;
        }
        case CONTINUE: {
            S.nextToken();
            Name label = (S.token() == IDENTIFIER || S.token() == ASSERT || S.token() == ENUM) ? ident() : null;
            JCContinue t =  to(F.at(pos).Continue(label));
            accept(SEMI);
            return t;
        }
        case SEMI:
            S.nextToken();
            return toP(F.at(pos).Skip());
        case ELSE:
            return toP(F.Exec(syntaxError("else.without.if")));
        case FINALLY:
            return toP(F.Exec(syntaxError("finally.without.try")));
        case CATCH:
            return toP(F.Exec(syntaxError("catch.without.try")));
        case ASSERT: {
            if (allowAsserts && S.token() == ASSERT) {
                S.nextToken();
                JCExpression assertion = expression();
                JCExpression message = null;
                if (S.token() == COLON) {
                    S.nextToken();
                    message = expression();
                }
                JCAssert t = to(F.at(pos).Assert(assertion, message));
                accept(SEMI);
                return t;
            }
            /* else fall through to default case */
        }
        case ENUM:
        default:
            Name name = S.name();
            JCExpression expr = expression();
            if (S.token() == COLON && expr.tag == JCTree.IDENT) {
                S.nextToken();
                JCStatement stat = statement();
                return F.at(pos).Labelled(name, stat);
            } else {
                // This Exec is an "ExpressionStatement"; it subsumes the terminating semicolon
                JCExpressionStatement stat = to(F.at(pos).Exec(checkExprStat(expr)));
                accept(SEMI);
                return stat;
            }
        }

        }finally{//我加上的
		DEBUG.P(0,this,"statement()");
		}        
    }

    /** CatchClause     = CATCH "(" FormalParameter ")" Block
     */
    JCCatch catchClause() {
    	DEBUG.P(this,"catchClause()");
        int pos = S.pos();
        accept(CATCH);
        accept(LPAREN);
        JCVariableDecl formal =
            variableDeclaratorId(optFinal(Flags.PARAMETER),
                                 qualident());
        accept(RPAREN);
        JCBlock body = block();
        
        DEBUG.P(0,this,"catchClause()");
        return F.at(pos).Catch(formal, body);
    }

    /** SwitchBlockStatementGroups = { SwitchBlockStatementGroup }
     *  SwitchBlockStatementGroup = SwitchLabel BlockStatements
     *  SwitchLabel = CASE ConstantExpression ":" | DEFAULT ":"
     */
    List<JCCase> switchBlockStatementGroups() {
    	try {//我加上的
		DEBUG.P(this,"switchBlockStatementGroups()");
		
        ListBuffer<JCCase> cases = new ListBuffer<JCCase>();
        while (true) {
            int pos = S.pos();
            switch (S.token()) {
            case CASE: {
                S.nextToken();
                JCExpression pat = expression();
                accept(COLON);
                List<JCStatement> stats = blockStatements();
                JCCase c = F.at(pos).Case(pat, stats);
                if (stats.isEmpty())
                    storeEnd(c, S.prevEndPos());
                cases.append(c);
                break;
            }
            case DEFAULT: {
                S.nextToken();
                accept(COLON);
                List<JCStatement> stats = blockStatements();
                JCCase c = F.at(pos).Case(null, stats);
                if (stats.isEmpty())
                    storeEnd(c, S.prevEndPos());
                cases.append(c);
                break;
            }
            case RBRACE: case EOF:
                return cases.toList();
            default:
                S.nextToken(); // to ensure progress
                syntaxError(pos, "expected3",
                    keywords.token2string(CASE),
                    keywords.token2string(DEFAULT),
                    keywords.token2string(RBRACE));
            }
        }
        
        }finally{//我加上的
		DEBUG.P(0,this,"switchBlockStatementGroups()");
		}
    }

    /** MoreStatementExpressions = { COMMA StatementExpression }
     */
    <T extends ListBuffer<? super JCExpressionStatement>> T moreStatementExpressions(int pos,
                                                                    JCExpression first,
                                                                    T stats) {
        DEBUG.P(this,"moreStatementExpressions(3)");
        
        // This Exec is a "StatementExpression"; it subsumes no terminating token
        stats.append(toP(F.at(pos).Exec(checkExprStat(first))));
        while (S.token() == COMMA) {
            S.nextToken();
            pos = S.pos();
            JCExpression t = expression();
            // This Exec is a "StatementExpression"; it subsumes no terminating token
            stats.append(toP(F.at(pos).Exec(checkExprStat(t))));
        }
        
        DEBUG.P(0,this,"moreStatementExpressions(3)");
        return stats;
    }

    /** ForInit = StatementExpression MoreStatementExpressions
     *           |  { FINAL | '@' Annotation } Type VariableDeclarators
     */
    List<JCStatement> forInit() {
    	try {//我加上的
		DEBUG.P(this,"forInit()");
		
        ListBuffer<JCStatement> stats = lb();
        int pos = S.pos();
        if (S.token() == FINAL || S.token() == MONKEYS_AT) {
            return variableDeclarators(optFinal(0), type(), stats).toList();
        } else {
            JCExpression t = term(EXPR | TYPE);
            if ((lastmode & TYPE) != 0 &&
                (S.token() == IDENTIFIER || S.token() == ASSERT || S.token() == ENUM))
                return variableDeclarators(modifiersOpt(), t, stats).toList();
            else
                return moreStatementExpressions(pos, t, stats).toList();
        }
        
		}finally{//我加上的
		DEBUG.P(0,this,"forInit()");
		}
    }

    /** ForUpdate = StatementExpression MoreStatementExpressions
     */
    List<JCExpressionStatement> forUpdate() {
    	try {//我加上的
		DEBUG.P(this,"forUpdate()");
		
        return moreStatementExpressions(S.pos(),
                                        expression(),
                                        new ListBuffer<JCExpressionStatement>()).toList();
		}finally{//我加上的
		DEBUG.P(0,this,"forUpdate()");
		}
    }

    /** AnnotationsOpt = { '@' Annotation }
     */
    List<JCAnnotation> annotationsOpt() {
    	try {//我加上的
		DEBUG.P(this,"annotationsOpt()");
		DEBUG.P("S.token()="+S.token());
		
        if (S.token() != MONKEYS_AT) return List.nil(); // optimization
        ListBuffer<JCAnnotation> buf = new ListBuffer<JCAnnotation>();
        while (S.token() == MONKEYS_AT) {
            int pos = S.pos();
            S.nextToken();
            buf.append(annotation(pos));
        }
        return buf.toList();
        
		}finally{//我加上的
		DEBUG.P(0,this,"annotationsOpt()");
		}
    }

    /** ModifiersOpt = { Modifier }
     *  Modifier = PUBLIC | PROTECTED | PRIVATE | STATIC | ABSTRACT | FINAL
     *           | NATIVE | SYNCHRONIZED | TRANSIENT | VOLATILE | "@"(单独一个@是不行的)
     *           | "@" Annotation
     */
    JCModifiers modifiersOpt() {
        return modifiersOpt(null);
    }
    JCModifiers modifiersOpt(JCModifiers partial) {
    	DEBUG.P(this,"modifiersOpt(1)");	
    	
    	//flags是各种Modifier通过“位或运算(|)”得到
    	//在com.sun.tools.javac.code.Flags类中用一位(bit)表示一个Modifier
    	//因flags是long类型，所以可表示64个不同的Modifier
    	//如flags=0x01时表示Flags.PUBLIC,当flags=0x03时表示Flags.PUBLIC与Flags.PRIVATE
    	//把flags传到Flags.toString(long flags)方法就可以知道flags代表哪个(哪些)Modifier
        long flags = (partial == null) ? 0 : partial.flags;

        //当Scanner在Javadoc中扫描到有@deprecated时S.deprecatedFlag()返回true
        if (S.deprecatedFlag()) {
            flags |= Flags.DEPRECATED;
            S.resetDeprecatedFlag();
        }
        DEBUG.P("(while前) flags="+flags+" modifiers=("+Flags.toString(flags)+")");
        
        ListBuffer<JCAnnotation> annotations = new ListBuffer<JCAnnotation>();
        if (partial != null) annotations.appendList(partial.annotations);
        int pos = S.pos();
        int lastPos = Position.NOPOS;
    loop:
        while (true) {
            // <editor-fold defaultstate="collapsed">
            long flag;
			/*
			在Flags类中定义了12个Standard Java flags，
			但是下面的switch语句中少了INTERFACE，
			这是因为INTERFACE(还有ENUM)后面不能再有其他修饰符了，
			当S.token()==INTERFACE时，退出while循环，最后再追加INTERFACE修饰符标志
			*/
            switch (S.token()) {
	            case PRIVATE     : flag = Flags.PRIVATE; break;
	            case PROTECTED   : flag = Flags.PROTECTED; break;
	            case PUBLIC      : flag = Flags.PUBLIC; break;
	            case STATIC      : flag = Flags.STATIC; break;
	            case TRANSIENT   : flag = Flags.TRANSIENT; break;
	            case FINAL       : flag = Flags.FINAL; break;
	            case ABSTRACT    : flag = Flags.ABSTRACT; break;
	            case NATIVE      : flag = Flags.NATIVE; break;
	            case VOLATILE    : flag = Flags.VOLATILE; break;
	            case SYNCHRONIZED: flag = Flags.SYNCHRONIZED; break;
	            case STRICTFP    : flag = Flags.STRICTFP; break;
	            case MONKEYS_AT  : flag = Flags.ANNOTATION; break;
	            default: break loop;
            }
            //修饰符重复,错误提示信息在com\sun\tools\javac\resources\compiler.properties定义
            if ((flags & flag) != 0) log.error(S.pos(), "repeated.modifier");
            //报告错误后并没有中断程序的运行，只是在Log中记录下错误发生次数
            //DEBUG.P("Log.nerrors="+log.nerrors);
            
            lastPos = S.pos();
            S.nextToken();
           
            if (flag == Flags.ANNOTATION) {
                checkAnnotations();//检查当前的-source版本是否支持注释
                
                //非“@interface”语法注释识别(@interface用于注释类型的定义)
                //“@interface”语法在com.sun.tools.javac.util.Version类中有这样的例子
                //JDK1.6中有关于Annotations的文档在technotes/guides/language/annotations.html
                if (S.token() != INTERFACE) {
					//lastPos是@的开始位置
                    JCAnnotation ann = annotation(lastPos);
					DEBUG.P("pos="+pos);
					DEBUG.P("ann.pos="+ann.pos);
                    // if first modifier is an annotation, set pos to annotation's.
                    if (flags == 0 && annotations.isEmpty())
                        pos = ann.pos;
                    annotations.append(ann);
                    lastPos = ann.pos;

                    //注意这里,对下面的checkNoMods(mods.flags)有影响
                    flag = 0;
                }
            }
            flags |= flag;
            // </editor-fold>
        }
        switch (S.token()) {
	        case ENUM: flags |= Flags.ENUM; break;
	        case INTERFACE: flags |= Flags.INTERFACE; break;
	        default: break;
        }
        
        DEBUG.P("(while后)  flags="+flags+" modifiers=("+Flags.toString(flags)+")");
        DEBUG.P("JCAnnotation count="+annotations.size());

        /* A modifiers tree with no modifier tokens or annotations
         * has no text position. */
        if (flags == 0 && annotations.isEmpty())
            pos = Position.NOPOS;
            
        JCModifiers mods = F.at(pos).Modifiers(flags, annotations.toList());
        
        if (pos != Position.NOPOS)
            storeEnd(mods, S.prevEndPos());//storeEnd()只是一个空方法,子类EndPosParser已重写
            
        DEBUG.P(1,this,"modifiersOpt(1)");	
        return mods;
    }

    /** Annotation              = "@" Qualident [ "(" AnnotationFieldValues ")" ]
     * @param pos position of "@" token
     */
    JCAnnotation annotation(int pos) {
    	try {//我加上的
        DEBUG.P(this,"annotation(int pos)");
        DEBUG.P("pos="+pos);


        // accept(AT); // AT consumed by caller
        checkAnnotations();
        JCTree ident = qualident();
        List<JCExpression> fieldValues = annotationFieldValuesOpt();
        JCAnnotation ann = F.at(pos).Annotation(ident, fieldValues);
        storeEnd(ann, S.prevEndPos());
        return ann;
        
        
        }finally{//我加上的
        DEBUG.P(0,this,"annotation(int pos)");
        }
    }

    List<JCExpression> annotationFieldValuesOpt() {
        return (S.token() == LPAREN) ? annotationFieldValues() : List.<JCExpression>nil();
    }

    /** AnnotationFieldValues   = "(" [ AnnotationFieldValue { "," AnnotationFieldValue } ] ")" */
    List<JCExpression> annotationFieldValues() {
    	try {//我加上的
		DEBUG.P(this,"annotationFieldValues()");

        accept(LPAREN);
        ListBuffer<JCExpression> buf = new ListBuffer<JCExpression>();
        if (S.token() != RPAREN) {
            buf.append(annotationFieldValue());
            while (S.token() == COMMA) {
                S.nextToken();
                buf.append(annotationFieldValue());
            }
        }
        accept(RPAREN);
        return buf.toList();
        
        }finally{//我加上的
		DEBUG.P(0,this,"annotationFieldValues()");
		}        
    }

    /** AnnotationFieldValue    = AnnotationValue
     *                          | Identifier "=" AnnotationValue
     */
    JCExpression annotationFieldValue() {
    	try {//我加上的
		DEBUG.P(this,"annotationFieldValue()");
		
        if (S.token() == IDENTIFIER) {
            mode = EXPR;
            JCExpression t1 = term1();
            if (t1.tag == JCTree.IDENT && S.token() == EQ) {
                int pos = S.pos();
                accept(EQ);
                return toP(F.at(pos).Assign(t1, annotationValue()));
            } else {
                return t1;
            }
        }
        return annotationValue();
        
        }finally{//我加上的
		DEBUG.P(0,this,"annotationFieldValue()");
		} 
    }

    /* AnnotationValue          = ConditionalExpression
     *                          | Annotation
     *                          | "{" [ AnnotationValue { "," AnnotationValue } ] "}"
     */
    JCExpression annotationValue() {
    	try {//我加上的
		DEBUG.P(this,"annotationValue()");
		
        int pos;
        //JDK1.6中有关于注释字段取值的文档在technotes/guides/language/annotations.html
        switch (S.token()) {
        case MONKEYS_AT:  //注释字段的值是注释的情况
            pos = S.pos();
            S.nextToken();
            return annotation(pos);
        case LBRACE:  //注释字段的值是数组的情况
            pos = S.pos();
            accept(LBRACE);
            ListBuffer<JCExpression> buf = new ListBuffer<JCExpression>();
            if (S.token() != RBRACE) {
                buf.append(annotationValue());
                while (S.token() == COMMA) {
                    S.nextToken();
                    if (S.token() == RPAREN) break;
                    buf.append(annotationValue());
                }
            }
            accept(RBRACE);
            
            //JCNewArray的语法类似如下:
            //new type dimensions initializers 或
            //new type dimensions [ ] initializers
            //看com.sun.source.tree.NewArrayTree
            return toP(F.at(pos).NewArray(null, List.<JCExpression>nil(), buf.toList()));
        default:
            mode = EXPR;
            return term1();
        }
        
        }finally{//我加上的
		DEBUG.P(0,this,"annotationValue()");
		} 
    }

	/*
	<T extends ListBuffer<? super JCVariableDecl>> T vdefs怎样理解?
	意思是:传给“T vdefs”的“type argument”的类型必须是ListBuffer及其子类,
	并且ListBuffer及其子类的“parameterized type”又是JCVariableDecl或其超类。
	
	例子参考forInit()方法中的如下代码片断:
	ListBuffer<JCStatement> stats......
	variableDeclarators(......, stats)

	其中“type argument”指的是stats，它是指向ListBuffer<JCStatement>类实例的引用，
	ListBuffer的“parameterized type”指的是JCStatement，而JCStatement
	又是JCVariableDecl的超类。
	*/

    /** VariableDeclarators = VariableDeclarator { "," VariableDeclarator }
     */
    public <T extends ListBuffer<? super JCVariableDecl>> T variableDeclarators(JCModifiers mods,
                                                                         JCExpression type,
                                                                         T vdefs)
    {
    	try {//我加上的
		DEBUG.P(this,"variableDeclarators(3)");
		
        return variableDeclaratorsRest(S.pos(), mods, type, ident(), false, null, vdefs);

        }finally{//我加上的
		DEBUG.P(0,this,"variableDeclarators(3)");
		}         
    }

    /** VariableDeclaratorsRest = VariableDeclaratorRest { "," VariableDeclarator }
     *  ConstantDeclaratorsRest = ConstantDeclaratorRest { "," ConstantDeclarator }
     *
     *  @param reqInit  Is an initializer always required?
     *  @param dc       The documentation comment for the variable declarations, or null.
     */
    <T extends ListBuffer<? super JCVariableDecl>> T variableDeclaratorsRest(int pos,
                                                                     JCModifiers mods,
                                                                     JCExpression type,
                                                                     Name name,
                                                                     boolean reqInit,
                                                                     String dc,
                                                                     T vdefs) {
    	try {//我加上的
		DEBUG.P(this,"variableDeclaratorsRest(7)");
		
        vdefs.append(variableDeclaratorRest(pos, mods, type, name, reqInit, dc));
        while (S.token() == COMMA) {
            // All but last of multiple declarators subsume a comma
			DEBUG.P("S.endPos()="+S.endPos());
            storeEnd((JCTree)vdefs.elems.last(), S.endPos());
            S.nextToken();
            vdefs.append(variableDeclarator(mods, type, reqInit, dc));
        }
        return vdefs;
        
        }finally{//我加上的
		DEBUG.P(0,this,"variableDeclaratorsRest(7)");
		}          
    }

    /** VariableDeclarator = Ident VariableDeclaratorRest
     *  ConstantDeclarator = Ident ConstantDeclaratorRest
     */
    JCVariableDecl variableDeclarator(JCModifiers mods, JCExpression type, boolean reqInit, String dc) {
        try {//我加上的
		DEBUG.P(this,"variableDeclarator(4)");
		
        return variableDeclaratorRest(S.pos(), mods, type, ident(), reqInit, dc);
       
        }finally{//我加上的
		DEBUG.P(0,this,"variableDeclarator(4)");
		}      
    }

    /** VariableDeclaratorRest = BracketsOpt ["=" VariableInitializer]
     *  ConstantDeclaratorRest = BracketsOpt "=" VariableInitializer
     *
     *  @param reqInit  Is an initializer always required?
     *  @param dc       The documentation comment for the variable declarations, or null.
     */
    JCVariableDecl variableDeclaratorRest(int pos, JCModifiers mods, JCExpression type, Name name,
                                  boolean reqInit, String dc) {
        try {//我加上的
		DEBUG.P(this,"variableDeclaratorRest(6)");
		DEBUG.P("pos="+pos);
		DEBUG.P("mods="+mods);
		DEBUG.P("type="+type);
		DEBUG.P("name="+name);
		//接口中定义的成员变量需要初始化
		//reqInit有时等于isInterface的值
		DEBUG.P("reqInit="+reqInit);
		DEBUG.P("dc="+dc);
		
        type = bracketsOpt(type); //例如:String s1[]
        JCExpression init = null;
        if (S.token() == EQ) {
            S.nextToken();
            init = variableInitializer();
        }
        else if (reqInit) syntaxError(S.pos(), "expected", keywords.token2string(EQ));
        //对于接口中定义的成员变量，如果没有指定修饰符，
        //在Parser阶断也不会自动加上
        //DEBUG.P("mods="+mods);
        JCVariableDecl result =
            toP(F.at(pos).VarDef(mods, name, type, init));
        attach(result, dc);
        return result;

        }finally{//我加上的
		DEBUG.P(0,this,"variableDeclaratorRest(6)");
		}       
    }

    /** VariableDeclaratorId = Ident BracketsOpt
     */
    JCVariableDecl variableDeclaratorId(JCModifiers mods, JCExpression type) {
    	try {//我加上的
        DEBUG.P(this,"variableDeclaratorId(2)");
		
        int pos = S.pos();
        Name name = ident();
        if ((mods.flags & Flags.VARARGS) == 0)
		//mothodName(N[] n[],S s)这种语法也不会报错
		//mothodName(N... n[],S s)这种语法就会报错
		//mothodName(N[8] n[9],S s)这种语法也会报错，
		//因为方法参数中的数组类型参数是不能指定数组大小的
            type = bracketsOpt(type);
        //方法形参没有初始化部分，所以VarDef方法的第4个参数为null
        return toP(F.at(pos).VarDef(mods, name, type, null));

        }finally{//我加上的
        DEBUG.P(0,this,"variableDeclaratorId(2)");
        }  
    }
    //下面这行文法表示得不够准确
    /** CompilationUnit = [ { "@" Annotation } PACKAGE Qualident ";"] {ImportDeclaration} {TypeDeclaration}
     */
    //上面的注释是LL(1)文法的总的全貌, 说明CompilationUnit =空也可以,
    //这如同编译一个没有任何内容的源文件也不会报错一样
    public JCTree.JCCompilationUnit compilationUnit() {
    	DEBUG.P(this,"compilationUnit() 正式开始语法分析......");
    	DEBUG.P("startPos="+S.pos());
    	DEBUG.P("errorPos="+errorPos);
    	DEBUG.P("errorEndPos="+errorEndPos);
        DEBUG.P("startToken="+S.token());
        
        int pos = S.pos();
        JCExpression pid = null;//对应文法中的Qualident
        //当前token对应的javadoc(见DocCommentScanner.processComment(1))
        String dc = S.docComment();
        DEBUG.P("dc="+dc);

		//对应文法中的{ "@" Annotation }，可能是包注释，
		//也可能是第一个声明的类的修饰符
        JCModifiers mods = null;
        
        List<JCAnnotation> packageAnnotations = List.nil();
        
        if (S.token() == MONKEYS_AT)
            mods = modifiersOpt();
        /*
        只有在package-info.java文件中才能有包注释(在没有特别指明的情况下，“注释”指的是Annotation)
        否则会有错误提示：“软件包注释应在文件 package-info.java 中”
        对应compiler.properties中的"pkg.annotations.sb.in.package-info.java"
        错误不在语法分析阶段检查，而是在com.sun.tools.javac.comp.Enter中检查
        */
        if (S.token() == PACKAGE) {
            //如果在“package”前有JavaDoc,且里面有@deprecated，
            //但是后面没有@Annotation或其他modifiers就是合法的。
            if (mods != null) {
            	/*
            	检查是否允许使用修饰符
            	如果package-info.java文件的源码像下面那样:
            	@Deprecated public
                package my.test;

                就会报错:
                bin\mysrc\my\test\package-info.java:2: 此处不允许使用修饰符 public
                package my.test;
                ^
                1 错误				
                */
                checkNoMods(mods.flags);
                packageAnnotations = mods.annotations;
                mods = null;
            }
            S.nextToken();
            pid = qualident();
            accept(SEMI);
        }
        //defs中存放跟import语句与类型(class,interface等)定义相关的JTree
        ListBuffer<JCTree> defs = new ListBuffer<JCTree>();
       	boolean checkForImports = true;
        while (S.token() != EOF) {
            DEBUG.P("S.pos()="+S.pos()+"  errorEndPos="+errorEndPos);
            if (S.pos() <= errorEndPos) {
                // error recovery
                skip(checkForImports, false, false, false);
                if (S.token() == EOF)
                    break;
            }
            
            //软件包注释应在文件 package-info.java 中,而package-info.java是没有import的，
            //非package-info.java文件不能有包注释，所以mods==null(???)
            //(有三个问号的注释表明目前还未完全搞明白)
			//因为第一个类声明之前可能没有import，此时因为是第一次进入while循环
			//checkForImports为true，但是mods可能不为null(如含有public等)
            if (checkForImports && mods == null && S.token() == IMPORT) {
                defs.append(importDeclaration());
            } else {
				//当没有指定package与import语句时，并且在类声明之前加有@，
				//如：@MyAnnotation public ClassA {}，则mods!=null
                JCTree def = typeDeclaration(mods);
                
                //用JCExpressionStatement将JCErroneous“包装”起来
                if (def instanceof JCExpressionStatement)
                    def = ((JCExpressionStatement)def).expr;
                defs.append(def);

				//这里保证了在类声明之后不能有import语句
                if (def instanceof JCClassDecl)
                    checkForImports = false;
				//这个是首先声明的类的修饰符，
				//对于在同一文件中声明的其他类必须设为null，
				//因为typeDeclaration(mods)时会重新modifiersOpt(mods)
                mods = null;
            }
        }
        //F.at(pos)里的pos还是int pos = S.pos();时的pos,一直没变
        JCTree.JCCompilationUnit toplevel = F.at(pos).TopLevel(packageAnnotations, pid, defs.toList());
        attach(toplevel, dc);

		DEBUG.P("defs.elems.isEmpty()="+defs.elems.isEmpty());
        if (defs.elems.isEmpty())
            storeEnd(toplevel, S.prevEndPos());
        if (keepDocComments) toplevel.docComments = docComments;
        
        //运行到这里，语法分析完成，生成了一棵抽象语法树
		//DEBUG.P("toplevel="+toplevel);
		DEBUG.P("toplevel.startPos="+getStartPos(toplevel));
		DEBUG.P("toplevel.endPos  ="+getEndPos(toplevel));
        DEBUG.P(3,this,"compilationUnit()");
        //DEBUG.P("Parser stop",true);
        return toplevel;
    }

    /** ImportDeclaration = IMPORT [ STATIC ] Ident { "." Ident } [ "." "*" ] ";"
     */
    JCTree importDeclaration() {
    	DEBUG.P(this,"importDeclaration()");
        int pos = S.pos();//这个一定是import这个token的开始位置
		DEBUG.P("pos="+pos);
        S.nextToken();
        boolean importStatic = false;
        if (S.token() == STATIC) {
            checkStaticImports();
            importStatic = true;
            S.nextToken();
        }

		//如果是“import my.test;”，那么这里得到的pid的开始位置是my这个token的pos
		//pid的结束位置是my这个token的endpos，
		//对应nextToken(157,159)=|my|中的(157,159)
        JCExpression pid = toP(F.at(S.pos()).Ident(ident()));
        do {
            int pos1 = S.pos();
            accept(DOT);
            if (S.token() == STAR) {
                pid = to(F.at(pos1).Select(pid, names.asterisk));//导入“.*"的情况
                S.nextToken();
                break;
            } else {
				DEBUG.P("pos1="+pos1);
				//如果是“import my.test;”，那么这里得到的pid是一个JCFieldAccess
				//它的开始位置是“.”的pos，结束位置是test这个token的endpos
                pid = toP(F.at(pos1).Select(pid, ident()));
            }
        } while (S.token() == DOT);
        accept(SEMI);
        DEBUG.P(2,this,"importDeclaration()");
		//如果是“import my.test;”，那么这里得到的pid是一个JCImport
		//它的开始位置是“import”的pos，结束位置是";"这个token的endpos
        return toP(F.at(pos).Import(pid, importStatic));
    }

    /** TypeDeclaration = ClassOrInterfaceOrEnumDeclaration
     *                  | ";"
     */
    JCTree typeDeclaration(JCModifiers mods) {
        try {//我加上的
        DEBUG.P(this,"typeDeclaration(1)");
        if(mods!=null) DEBUG.P("mods.flags="+Flags.toString(mods.flags));
        else DEBUG.P("mods=null");
        DEBUG.P("S.token()="+S.token()+"  S.pos()="+S.pos());

        int pos = S.pos();

		//单独的“;"号前面不能有修饰符
        if (mods == null && S.token() == SEMI) {
            S.nextToken();
            return toP(F.at(pos).Skip());
        } else {
            String dc = S.docComment();
			DEBUG.P("dc="+dc);
            return classOrInterfaceOrEnumDeclaration(modifiersOpt(mods), dc);
        }


        }finally{//我加上的
        DEBUG.P(2,this,"typeDeclaration(1)");
        }
    }

    /** ClassOrInterfaceOrEnumDeclaration = ModifiersOpt
     *           (ClassDeclaration | InterfaceDeclaration | EnumDeclaration)
     *  @param mods     Any modifiers starting the class or interface declaration
     *  @param dc       The documentation comment for the class, or null.
     */
    JCStatement classOrInterfaceOrEnumDeclaration(JCModifiers mods, String dc) {
    	try {//我加上的
    	DEBUG.P(this,"classOrInterfaceOrEnumDeclaration(2)");
    	if(mods!=null) DEBUG.P("mods.flags="+Flags.toString(mods.flags));
    	else DEBUG.P("mods=null");
    	DEBUG.P("S.token()="+S.token()+"  dc="+dc);
    	
    	
        if (S.token() == CLASS) {
            return classDeclaration(mods, dc);
        } else if (S.token() == INTERFACE) {
			//这里同时包含接口声明和注释类型声明，
			//因为在modifiersOpt(mods)时，首先遇到@,
			//接着nextToken()后发现是INTERFACE，
			//给flags加上INTERFACE后退出modifiersOpt(mods)
            return interfaceDeclaration(mods, dc);
        } else if (allowEnums) {
            if (S.token() == ENUM) {
                return enumDeclaration(mods, dc);
            } else {
                int pos = S.pos();
                DEBUG.P("pos="+pos);
                List<JCTree> errs;
                if (S.token() == IDENTIFIER) {
                    errs = List.<JCTree>of(mods, toP(F.at(pos).Ident(ident())));
                    DEBUG.P("S.pos()="+S.pos());
                    //虽然在下面的syntaxError()内部也调用了setErrorEndPos()
                    //但因S.pos()>上面的int pos,所以错误结束位置是S.pos().
                    setErrorEndPos(S.pos());
                } else {
                    errs = List.<JCTree>of(mods);
                }
                //用JCExpressionStatement将JCErroneous“包装”起来
                return toP(F.Exec(syntaxError(pos, errs, "expected3",
                                              keywords.token2string(CLASS),
                                              keywords.token2string(INTERFACE),
                                              keywords.token2string(ENUM))));
            }
        } else {
            if (S.token() == ENUM) {
                log.error(S.pos(), "enums.not.supported.in.source", source.name);
                allowEnums = true;
                return enumDeclaration(mods, dc);
            }
            int pos = S.pos();
            List<JCTree> errs;
            if (S.token() == IDENTIFIER) {
                errs = List.<JCTree>of(mods, toP(F.at(pos).Ident(ident())));
                setErrorEndPos(S.pos());
            } else {
                errs = List.<JCTree>of(mods);
            }
            return toP(F.Exec(syntaxError(pos, errs, "expected2",
                                          keywords.token2string(CLASS),
                                          keywords.token2string(INTERFACE))));
        }
        
        
        }finally{//我加上的
        DEBUG.P(0,this,"classOrInterfaceOrEnumDeclaration(2)");
        }
    }

    /** ClassDeclaration = CLASS Ident TypeParametersOpt [EXTENDS Type]
     *                     [IMPLEMENTS TypeList] ClassBody
     *  @param mods    The modifiers starting the class declaration
     *  @param dc       The documentation comment for the class, or null.
     */
    JCClassDecl classDeclaration(JCModifiers mods, String dc) {
    	DEBUG.P(this,"classDeclaration(2)");
    	DEBUG.P("startPos="+S.pos());

        int pos = S.pos(); //对应class这个token的起始位置(pos)
        accept(CLASS);
		//因为类名是一个标识符，
		//所以它在调用Scanner类的nextToken方法时又调用了scanIdent()，
		//通过scanIdent()把类名加进了Name.Table.names这个字节数组中了。
        Name name = ident();

        List<JCTypeParameter> typarams = typeParametersOpt();//泛型<>
        DEBUG.P("typarams="+typarams);
        DEBUG.P("typarams.size="+typarams.size());
        

        JCTree extending = null;
        if (S.token() == EXTENDS) {
            S.nextToken();
            extending = type();
        }
        DEBUG.P("extending="+extending);
        List<JCExpression> implementing = List.nil();
        if (S.token() == IMPLEMENTS) {
            S.nextToken();
            implementing = typeList();
        }
        DEBUG.P("implementing="+implementing);
        List<JCTree> defs = classOrInterfaceBody(name, false);
		DEBUG.P("defs.size="+defs.size());
        JCClassDecl result = toP(F.at(pos).ClassDef(
            mods, name, typarams, extending, implementing, defs));
        attach(result, dc);
        DEBUG.P(2,this,"classDeclaration(2)");
        return result;
    }

    /** InterfaceDeclaration = INTERFACE Ident TypeParametersOpt
     *                         [EXTENDS TypeList] InterfaceBody
     *  @param mods    The modifiers starting the interface declaration
     *  @param dc       The documentation comment for the interface, or null.
     */
    JCClassDecl interfaceDeclaration(JCModifiers mods, String dc) {
    	DEBUG.P(this,"interfaceDeclaration(2)");
    	DEBUG.P("startPos="+S.pos());
        int pos = S.pos();
        accept(INTERFACE);
        Name name = ident();

        List<JCTypeParameter> typarams = typeParametersOpt();

        List<JCExpression> extending = List.nil();
        if (S.token() == EXTENDS) {
            S.nextToken();
            extending = typeList();
        }
        List<JCTree> defs = classOrInterfaceBody(name, true);
		DEBUG.P("defs.size="+defs.size());
        //接口没有implements，注意下面第4,5个参数
        JCClassDecl result = toP(F.at(pos).ClassDef(
            mods, name, typarams, null, extending, defs));
        attach(result, dc);
        DEBUG.P(2,this,"interfaceDeclaration(2)");
        return result;
    }

    /** EnumDeclaration = ENUM Ident [IMPLEMENTS TypeList] EnumBody
     *  @param mods    The modifiers starting the enum declaration
     *  @param dc       The documentation comment for the enum, or null.
     */
    JCClassDecl enumDeclaration(JCModifiers mods, String dc) {
    	DEBUG.P(this,"enumDeclaration(2)");
    	DEBUG.P("startPos="+S.pos());
        int pos = S.pos();
        accept(ENUM);
        Name name = ident();

        List<JCExpression> implementing = List.nil();
        if (S.token() == IMPLEMENTS) {
            S.nextToken();
            implementing = typeList();
        }

        List<JCTree> defs = enumBody(name);
        JCModifiers newMods = //在modifiersOpt()已加Flags.ENUM
            F.at(mods.pos).Modifiers(mods.flags|Flags.ENUM, mods.annotations);
        //枚举类没有TypeParameters也没有EXTENDS TypeList
        JCClassDecl result = toP(F.at(pos).
            ClassDef(newMods, name, List.<JCTypeParameter>nil(),
                null, implementing, defs));
        attach(result, dc);
        DEBUG.P(2,this,"enumDeclaration(2)");
        return result;
    }

    /** EnumBody = "{" { EnumeratorDeclarationList } [","]
     *                  [ ";" {ClassBodyDeclaration} ] "}"
     */
    List<JCTree> enumBody(Name enumName) {
    	DEBUG.P(this,"enumBody(Name enumName)");
        accept(LBRACE);
        ListBuffer<JCTree> defs = new ListBuffer<JCTree>();
        if (S.token() == COMMA) {
            S.nextToken();
        } else if (S.token() != RBRACE && S.token() != SEMI) {
            defs.append(enumeratorDeclaration(enumName));
            while (S.token() == COMMA) {
                S.nextToken();
                if (S.token() == RBRACE || S.token() == SEMI) break;
                defs.append(enumeratorDeclaration(enumName));
            }
            if (S.token() != SEMI && S.token() != RBRACE) {
                defs.append(syntaxError(S.pos(), "expected3",
                                keywords.token2string(COMMA),
                                keywords.token2string(RBRACE),
                                keywords.token2string(SEMI)));
                S.nextToken();
            }
        }
        if (S.token() == SEMI) {
            S.nextToken();
            while (S.token() != RBRACE && S.token() != EOF) {
                defs.appendList(classOrInterfaceBodyDeclaration(enumName,
                                                                false));
                if (S.pos() <= errorEndPos) {
                    // error recovery
                   skip(false, true, true, false);
                }
            }
        }
        accept(RBRACE);
        DEBUG.P(0,this,"enumBody(Name enumName)");
        return defs.toList();
    }
    
    //参考jdk1.6.0docs/technotes/guides/language/enums.html
    /** EnumeratorDeclaration = AnnotationsOpt [TypeArguments] IDENTIFIER [ Arguments ] [ "{" ClassBody "}" ]
     */
    JCTree enumeratorDeclaration(Name enumName) {
    	DEBUG.P(this,"enumeratorDeclaration(Name enumName)");
        String dc = S.docComment();
        int flags = Flags.PUBLIC|Flags.STATIC|Flags.FINAL|Flags.ENUM;
        if (S.deprecatedFlag()) {
            flags |= Flags.DEPRECATED;
            S.resetDeprecatedFlag();
        }
        int pos = S.pos();
        List<JCAnnotation> annotations = annotationsOpt();
        JCModifiers mods = F.at(annotations.isEmpty() ? Position.NOPOS : pos).Modifiers(flags, annotations);
        
        /*在Java Language Specification, Third Edition
		 18.1. The Grammar of the Java Programming Language
		 中有如下定义:
		 EnumConstant:
      	 Annotations Identifier [Arguments] [ClassBody]
      	 所以上面的语法AnnotationsOpt [TypeArguments] IDENTIFIER是错误的
      	 
      	 类似“<?>SUPER("? super ")”这样的枚举常量是错语的(非法的表达式开始)
      	 */
        List<JCExpression> typeArgs = typeArgumentsOpt();//总是返回null
        int identPos = S.pos();
        Name name = ident();
        int createPos = S.pos();
        List<JCExpression> args = (S.token() == LPAREN)
            ? arguments() : List.<JCExpression>nil();
        JCClassDecl body = null;
        if (S.token() == LBRACE) {
        	/*如下代码片断:
        		public static enum MyBoundKind {
			    @Deprecated EXTENDS("? extends ") {
			    	 String toString() {
			    	 	return "extends"; 
			    	 }
			    },
			*/
            JCModifiers mods1 = F.at(Position.NOPOS).Modifiers(Flags.ENUM | Flags.STATIC);
            List<JCTree> defs = classOrInterfaceBody(names.empty, false);
            body = toP(F.at(identPos).AnonymousClassDef(mods1, defs));
        }
        if (args.isEmpty() && body == null)
            createPos = Position.NOPOS;
        JCIdent ident = F.at(Position.NOPOS).Ident(enumName);
        //每个枚举常量就相当于是此枚举类型的一个实例
        JCNewClass create = F.at(createPos).NewClass(null, typeArgs, ident, args, body);
        if (createPos != Position.NOPOS)
            storeEnd(create, S.prevEndPos());
        ident = F.at(Position.NOPOS).Ident(enumName);//注意这里与上面不是同一个JCIdent的实例
        JCTree result = toP(F.at(pos).VarDef(mods, name, ident, create));
        attach(result, dc);
        
        DEBUG.P(0,this,"enumeratorDeclaration(Name enumName)");
        return result;
    }

    /** TypeList = Type {"," Type}
     */
    List<JCExpression> typeList() {
    	try {//我加上的
		DEBUG.P(this,"typeList()");

        ListBuffer<JCExpression> ts = new ListBuffer<JCExpression>();
        ts.append(type());
        while (S.token() == COMMA) {
            S.nextToken();
            ts.append(type());
        }
        return ts.toList();
        
        }finally{//我加上的
		DEBUG.P(0,this,"typeList()");
		}
    }

    /** ClassBody     = "{" {ClassBodyDeclaration} "}"
     *  InterfaceBody = "{" {InterfaceBodyDeclaration} "}"
     */
    List<JCTree> classOrInterfaceBody(Name className, boolean isInterface) {
    	DEBUG.P(this,"classOrInterfaceBody(2)");
    	DEBUG.P("className="+className);
    	DEBUG.P("isInterface="+isInterface);

        accept(LBRACE);
        if (S.pos() <= errorEndPos) {
            // error recovery
            skip(false, true, false, false);
            if (S.token() == LBRACE)
                S.nextToken();
        }
        ListBuffer<JCTree> defs = new ListBuffer<JCTree>();
        while (S.token() != RBRACE && S.token() != EOF) {
            defs.appendList(classOrInterfaceBodyDeclaration(className, isInterface));
            if (S.pos() <= errorEndPos) {
               // error recovery
               skip(false, true, true, false);
           }
        }
        accept(RBRACE);
        DEBUG.P(2,this,"classOrInterfaceBody(2)");
        return defs.toList();
    }

    /** ClassBodyDeclaration =
     *      ";"
     *    | [STATIC] Block
     *    | ModifiersOpt
     *      **********************下面这6项是并列的**********************
     *      ( Type Ident
     *        ( VariableDeclaratorsRest ";" | MethodDeclaratorRest )
     *      | VOID Ident MethodDeclaratorRest
     *      | TypeParameters (Type | VOID) Ident MethodDeclaratorRest
     *      | Ident ConstructorDeclaratorRest
     *      | TypeParameters Ident ConstructorDeclaratorRest
     *      | ClassOrInterfaceOrEnumDeclaration
     *      )
     *      **********************上面这6项是并列的**********************
     *  InterfaceBodyDeclaration =
     *      ";"
     *    | ModifiersOpt Type Ident
     *      ( ConstantDeclaratorsRest | InterfaceMethodDeclaratorRest ";" )
     */
    List<JCTree> classOrInterfaceBodyDeclaration(Name className, boolean isInterface) {
    	try {//我加上的
    	DEBUG.P(this,"classOrInterfaceBodyDeclaration(2)");
 		DEBUG.P("S.token()="+S.token());

        if (S.token() == SEMI) {//这里不把他当成JCSkip，只有与类型声明(最顶层)并排的";"才是JCSkip
            S.nextToken();
            return List.<JCTree>of(F.at(Position.NOPOS).Block(0, List.<JCStatement>nil()));
        } else {
            String dc = S.docComment();
            int pos = S.pos();
            JCModifiers mods = modifiersOpt();
            
            //内部CLASS,INTERFACE,ENUM
            if (S.token() == CLASS ||
                S.token() == INTERFACE ||
				//如果用-source 1.4 -target 1.4编译内部enum类型，错误诊断位置会很乱
                allowEnums && S.token() == ENUM) {
                return List.<JCTree>of(classOrInterfaceOrEnumDeclaration(mods, dc));
				//语句块(包括static语句块(STATIC关键字在modifiersOpt()中已分析过))
            } else if (S.token() == LBRACE && !isInterface &&
                       (mods.flags & Flags.StandardFlags & ~Flags.STATIC) == 0 &&
                       mods.annotations.isEmpty()) {
                       //语句块前不能有注释,只能有static
                return List.<JCTree>of(block(pos, mods.flags));
            } else {
                pos = S.pos();
                //只有Method和Constructor之前才有TypeParameter
                List<JCTypeParameter> typarams = typeParametersOpt();
                DEBUG.P("mods.pos="+mods.pos);
                
                // Hack alert:  if there are type arguments(注：是typeParameters) but no Modifiers, the start
                // position will be lost unless we set the Modifiers position.  There
                // should be an AST node for type parameters (BugId 5005090).
                if (typarams.length() > 0 && mods.pos == Position.NOPOS) {
                    mods.pos = pos;
                }
                Token token = S.token();
                Name name = S.name();//构造方法(Constructor)的名称 或 字段类型名 或 方法的返回值的类型名
                pos = S.pos();
                JCExpression type;//字段的类型 或 方法的返回值的类型
                
                DEBUG.P("S.token()="+S.token());
                DEBUG.P("name="+name);
                
                boolean isVoid = S.token() == VOID;
                if (isVoid) {
                	//typetag为void的JCPrimitiveTypeTree
                    type = to(F.at(pos).TypeIdent(TypeTags.VOID));
                    S.nextToken(); 
                } else {
                    type = type();
                }
                //类的Constructor,如果是类的Constructor的名称，在term3()会生成JCTree.JCIdent
                if (S.token() == LPAREN && !isInterface && type.tag == JCTree.IDENT) {
                	
                	//isInterface这个条件完全可以去掉，因为通过前一个if语句后，
                	//isInterface的值肯定为false
                    if (isInterface || name != className)
                    	//构造方法(Constructor)的名称和类名不一样时
                    	//会报错，只是报错信息是:“方法声明无效；需要返回类型”
                        log.error(pos, "invalid.meth.decl.ret.type.req");
                    return List.of(methodDeclaratorRest(
                        pos, mods, null, names.init, typarams,
                        isInterface, true, dc));
                } else {
                    pos = S.pos();
                    name = ident(); //字段名或方法名，并读取下一个token

                    if (S.token() == LPAREN) { //方法
                        return List.of(methodDeclaratorRest(
                            pos, mods, type, name, typarams,
                            isInterface, isVoid, dc));
                    } else if (!isVoid && typarams.isEmpty()) { //字段名
						//在接口中定义的字段需要显示的初始化(isInterface=true)
                        List<JCTree> defs =
                            variableDeclaratorsRest(pos, mods, type, name, isInterface, dc,
                                                    new ListBuffer<JCTree>()).toList();
                        storeEnd(defs.last(), S.endPos());
                        accept(SEMI);
                        return defs;
                    } else {
                        pos = S.pos();
                        List<JCTree> err = isVoid
                            ? List.<JCTree>of(toP(F.at(pos).MethodDef(mods, name, type, typarams,
                                List.<JCVariableDecl>nil(), List.<JCExpression>nil(), null, null)))
                            : null;
                            
                        /*
                        如:
                        bin\mysrc\my\test\Test.java:32: 需要 '('
						        public <M extends T,S> int myInt='\uuuuu5df2';
						                                        ^
						1 错误
						*/
                        return List.<JCTree>of(syntaxError(S.pos(), err, "expected", keywords.token2string(LPAREN)));
                    }
                }
            }
        }
        
        }finally{//我加上的
		DEBUG.P(2,this,"classOrInterfaceBodyDeclaration(2)");
		}   
    }

    /** MethodDeclaratorRest =
     *      FormalParameters BracketsOpt [Throws TypeList] ( MethodBody | [DEFAULT AnnotationValue] ";")
     *  VoidMethodDeclaratorRest =
     *      FormalParameters [Throws TypeList] ( MethodBody | ";")
     *  InterfaceMethodDeclaratorRest =
     *      FormalParameters BracketsOpt [THROWS TypeList] ";"
     *  VoidInterfaceMethodDeclaratorRest =
     *      FormalParameters [THROWS TypeList] ";"
     *  ConstructorDeclaratorRest =
     *      "(" FormalParameterListOpt ")" [THROWS TypeList] MethodBody
     */
    JCTree methodDeclaratorRest(int pos,
                              JCModifiers mods,
                              JCExpression type,
                              Name name,
                              List<JCTypeParameter> typarams,
                              boolean isInterface, boolean isVoid,
                              String dc) {    
        DEBUG.P(this,"methodDeclaratorRest(6)");
        DEBUG.P("isVoid="+isVoid);          
        List<JCVariableDecl> params = formalParameters();//这是方法的参数
        if(params!=null) DEBUG.P("params.size="+params.size());  
        
        
        /*
        类似下面的语法也可以(返回值是数组的话,[]可以放在右括号')'后面):
	    public int myMethod()[] {
			return new int[0];
		}
		*/
        if (!isVoid) type = bracketsOpt(type);
        
        
         
        List<JCExpression> thrown = List.nil();
        if (S.token() == THROWS) {
            S.nextToken();
            thrown = qualidentList();
        }
        JCBlock body = null;
        JCExpression defaultValue;
        //DEBUG.P("S.token() ="+S.token());
        
		//如果接口中的方法有方法体并不在语法分析时检查
		//interface MemberInterfaceB {
		//	void methodA(){};
		//}
        if (S.token() == LBRACE) {
            body = block();
            defaultValue = null;
        } else {
        	/*
        	注释类型定义中的"default"
        	如jdk1.6.0docs/technotes/guides/language/annotations.html的例子:
        	public @interface RequestForEnhancement {
			    int    id();
			    String synopsis();
			    String engineer() default "[unassigned]"; 
			    String date()    default "[unimplemented]"; 
			}
			*/
            if (S.token() == DEFAULT) {
                accept(DEFAULT);
                defaultValue = annotationValue();
            } else {
                defaultValue = null;
            }
            accept(SEMI);
            if (S.pos() <= errorEndPos) {
                // error recovery
                skip(false, true, false, false);
                if (S.token() == LBRACE) {
                    body = block();
                }
            }
        }
        JCMethodDecl result =
            toP(F.at(pos).MethodDef(mods, name, type, typarams,
                                    params, thrown,
                                    body, defaultValue));
        DEBUG.P(2,this,"methodDeclaratorRest(6)");                            
        attach(result, dc);
        return result;
    }

    /** QualidentList = Qualident {"," Qualident}
     */
    List<JCExpression> qualidentList() {
    	/*这个方法只用于分析throws语句，因为throws语句后面的类名都是
    	java.lang.Throwable及其子类，而泛型类是无法继承Throwable的，
    	所以throws语句后面都是Qualident {"," Qualident}，而implements
    	语句后面可以接多个泛型(或非泛型)类，所以用typeList()文法分析
    	implements语句。


		也就是说，throws语句后头跟的类名不是泛型类，不是泛型类的话，
		也就不用在类名后头加上<...>这样的符号串，没有<...>这样的符号串
		也就意味着类名全是Qualident
    	
    	错误例子:
    	bin\mysrc\my\test\Test.java:29: 泛型类无法继承 java.lang.Throwable
		class MyException1<T> extends Throwable {}
		                              ^
		bin\mysrc\my\test\Test.java:30: 泛型类无法继承 java.lang.Throwable
		class MyException2<T> extends Exception {}
		                              ^
		bin\mysrc\my\test\Test.java:31: 泛型类无法继承 java.lang.Throwable
		class MyException3<T> extends Error {}
		                              ^
		3 错误
    	*/
    	DEBUG.P(this,"qualidentList()");
        ListBuffer<JCExpression> ts = new ListBuffer<JCExpression>();
        ts.append(qualident());
        while (S.token() == COMMA) {
            S.nextToken();
            ts.append(qualident());
        }
        DEBUG.P(0,this,"qualidentList()");
        return ts.toList();
    }

    /** TypeParametersOpt = ["<" TypeParameter {"," TypeParameter} ">"]
     */
    List<JCTypeParameter> typeParametersOpt() {
    	try {//我加上的
    	DEBUG.P(this,"typeParametersOpt()");
    	
        if (S.token() == LT) {
            checkGenerics();
            ListBuffer<JCTypeParameter> typarams = new ListBuffer<JCTypeParameter>();
            S.nextToken();
            typarams.append(typeParameter());
            while (S.token() == COMMA) {
                S.nextToken();
                typarams.append(typeParameter());
            }
            accept(GT);
            return typarams.toList();
        } else {
            return List.nil();
        }
        
        }finally{//我加上的
		DEBUG.P(0,this,"typeParametersOpt()");
		}
    }
    
    /*注意TypeParameter和TypeArgument的差别
     *	TypeArgument = Type
     *               | "?"
     *               | "?" EXTENDS Type
     *               | "?" SUPER Type
    
    对比方法参数的形参与实参来理解TypeParameter和TypeArgument
    */
    
    /** TypeParameter = TypeVariable [TypeParameterBound]
     *  TypeParameterBound = EXTENDS Type {"&" Type}
     *  TypeVariable = Ident
     */
    JCTypeParameter typeParameter() {
    	try {//我加上的
    	DEBUG.P(this,"typeParameter()");
    	
        int pos = S.pos();
        Name name = ident();
        ListBuffer<JCExpression> bounds = new ListBuffer<JCExpression>();
        if (S.token() == EXTENDS) {
            S.nextToken();
            bounds.append(type());
            while (S.token() == AMP) {
                S.nextToken();
                bounds.append(type());
            }
        }
		//如果只是<T>，那么bounds.toList()是一个new List<JCExpression>(null,null)
        return toP(F.at(pos).TypeParameter(name, bounds.toList()));
        
        }finally{//我加上的
		DEBUG.P(0,this,"typeParameter()");
		}
    }

    /** FormalParameters = "(" [ FormalParameterList ] ")"
     *  FormalParameterList = [ FormalParameterListNovarargs , ] LastFormalParameter
     *  FormalParameterListNovarargs = [ FormalParameterListNovarargs , ] FormalParameter
     */
    List<JCVariableDecl> formalParameters() { //指在一个方法的括号中声明的参数
    	try {//我加上的
    	DEBUG.P(this,"formalParameters()");
    	
        ListBuffer<JCVariableDecl> params = new ListBuffer<JCVariableDecl>();
        JCVariableDecl lastParam = null;
        accept(LPAREN);
        DEBUG.P("S.token()="+S.token());
        if (S.token() != RPAREN) {
            params.append(lastParam = formalParameter());
            //Vararrgs参数存在的话，总是方法的括号中声明的参数的最后一个
            while ((lastParam.mods.flags & Flags.VARARGS) == 0 && S.token() == COMMA) {
                S.nextToken();
                params.append(lastParam = formalParameter());
            }
        }
        accept(RPAREN);
        return params.toList();
        
        }finally{//我加上的
		DEBUG.P(0,this,"formalParameters()");
		}
    }

    JCModifiers optFinal(long flags) {
    	try {//我加上的
    	DEBUG.P(this,"optFinal(long flags)");
    	DEBUG.P("flags="+Flags.toString(flags));
    	
        JCModifiers mods = modifiersOpt();
        
        DEBUG.P("mods.flags="+Flags.toString(mods.flags));
        
		//方法括号中的参数只能是final与deprecated(在JAVADOC)中指定
		//ParserTest(/** @deprecated */ final int i){}
		//注意下面两句的编译结果是不一样的
		//ParserTest(final /** @deprecated */ int i){} //有错(不是指语法错误，而是少了deprecated) mods.flags=final parameter
		//ParserTest(/** @deprecated */ final int i){} //无错 mods.flags=final deprecated parameter
		//因为在modifiersOpt()中先看是否有DEPRECATED再进入while循环，
		//当final在先，进入while循环nextToken后忘了分析是否有DEPRECATED了
        checkNoMods(mods.flags & ~(Flags.FINAL | Flags.DEPRECATED));
        mods.flags |= flags;
        
        DEBUG.P("mods.flags="+Flags.toString(mods.flags));
        return mods;
        
        }finally{//我加上的
		DEBUG.P(0,this,"optFinal(long flags)");
		} 
    }

    /** FormalParameter = { FINAL | '@' Annotation } Type VariableDeclaratorId
     *  LastFormalParameter = { FINAL | '@' Annotation } Type '...' Ident | FormalParameter
     */
    JCVariableDecl formalParameter() {
    	try {//我加上的
    	DEBUG.P(this,"formalParameter()");
    	
        JCModifiers mods = optFinal(Flags.PARAMETER);
        JCExpression type = type();
        if (S.token() == ELLIPSIS) { //最后一个形参是varargs的情况
            checkVarargs();
            mods.flags |= Flags.VARARGS;
            type = to(F.at(S.pos()).TypeArray(type));
            S.nextToken();
        }
        return variableDeclaratorId(mods, type);
        
        }finally{//我加上的
		DEBUG.P(0,this,"formalParameter()");
		}        
    }

/* ---------- auxiliary methods -------------- */

    /** Check that given tree is a legal expression statement.
     */
    protected JCExpression checkExprStat(JCExpression t) {
    	/*
    	合法的表达式语句:
    	++a，--a，a++，a--，
    	a=b，
    	a|=b，a^=b，a&=b，
    	a<<=b，a>>=b，a>>>=b，a+=b，a-=b，a*=b，a/=b，a%=b，
    	a(),new a()
    	*/
        switch(t.tag) {
        case JCTree.PREINC: case JCTree.PREDEC:
        case JCTree.POSTINC: case JCTree.POSTDEC:
        case JCTree.ASSIGN:
        case JCTree.BITOR_ASG: case JCTree.BITXOR_ASG: case JCTree.BITAND_ASG:
        case JCTree.SL_ASG: case JCTree.SR_ASG: case JCTree.USR_ASG:
        case JCTree.PLUS_ASG: case JCTree.MINUS_ASG:
        case JCTree.MUL_ASG: case JCTree.DIV_ASG: case JCTree.MOD_ASG:
        case JCTree.APPLY: case JCTree.NEWCLASS:
        case JCTree.ERRONEOUS:
            return t;
        default:
            log.error(t.pos, "not.stmt");
            return F.at(t.pos).Erroneous(List.<JCTree>of(t));
        }
    }

    /** Return precedence of operator represented by token,
     *  -1 if token is not a binary operator. @see TreeInfo.opPrec
     */
    static int prec(Token token) {
        int oc = optag(token);
        return (oc >= 0) ? TreeInfo.opPrec(oc) : -1;
    }

    /** Return operation tag of binary operator represented by token,
     *  -1 if token is not a binary operator.
     */
    static int optag(Token token) {
        switch (token) {
        case BARBAR:
            return JCTree.OR;
        case AMPAMP:
            return JCTree.AND;
        case BAR:
            return JCTree.BITOR;
        case BAREQ:
            return JCTree.BITOR_ASG;
        case CARET:
            return JCTree.BITXOR;//指位异或符(^)
        case CARETEQ:
            return JCTree.BITXOR_ASG;
        case AMP:
            return JCTree.BITAND;
        case AMPEQ:
            return JCTree.BITAND_ASG;
        case EQEQ:
            return JCTree.EQ;
        case BANGEQ:
            return JCTree.NE;
        case LT:
            return JCTree.LT;
        case GT:
            return JCTree.GT;
        case LTEQ:
            return JCTree.LE;
        case GTEQ:
            return JCTree.GE;
        case LTLT:
            return JCTree.SL;
        case LTLTEQ:
            return JCTree.SL_ASG;
        case GTGT:
            return JCTree.SR;
        case GTGTEQ:
            return JCTree.SR_ASG;
        case GTGTGT:
            return JCTree.USR;
        case GTGTGTEQ:
            return JCTree.USR_ASG;
        case PLUS:
            return JCTree.PLUS;
        case PLUSEQ:
            return JCTree.PLUS_ASG;
        case SUB:
            return JCTree.MINUS;
        case SUBEQ:
            return JCTree.MINUS_ASG;
        case STAR:
            return JCTree.MUL;
        case STAREQ:
            return JCTree.MUL_ASG;
        case SLASH:
            return JCTree.DIV;
        case SLASHEQ:
            return JCTree.DIV_ASG;
        case PERCENT:
            return JCTree.MOD;
        case PERCENTEQ:
            return JCTree.MOD_ASG;
        case INSTANCEOF:
            return JCTree.TYPETEST;
        default:
            return -1;
        }
    }

    /** Return operation tag of unary operator represented by token,
     *  -1 if token is not a binary operator.//binary因改成unary
     */
    static int unoptag(Token token) {
        switch (token) {
        case PLUS:
            return JCTree.POS;
        case SUB:
            return JCTree.NEG;
        case BANG: //逻辑反
            return JCTree.NOT;
        case TILDE: //按位取反(注意是一无运算符，如：~34，但20~34是非法的)
            return JCTree.COMPL;
        case PLUSPLUS:
            return JCTree.PREINC;//不管++号是放在前还是放在后，都返回PREINC
        case SUBSUB:
            return JCTree.PREDEC;//不管--号是放在前还是放在后，都返回PREDEC
        default:
            return -1;
        }
    }

    /** Return type tag of basic type represented by token,
     *  -1 if token is not a basic type identifier.
     */
    static int typetag(Token token) {
        switch (token) {
        case BYTE:
            return TypeTags.BYTE;
        case CHAR:
            return TypeTags.CHAR;
        case SHORT:
            return TypeTags.SHORT;
        case INT:
            return TypeTags.INT;
        case LONG:
            return TypeTags.LONG;
        case FLOAT:
            return TypeTags.FLOAT;
        case DOUBLE:
            return TypeTags.DOUBLE;
        case BOOLEAN:
            return TypeTags.BOOLEAN;
        default:
            return -1;
        }
    }

    void checkGenerics() {
        if (!allowGenerics) {
            log.error(S.pos(), "generics.not.supported.in.source", source.name);
            allowGenerics = true;
        }
    }
    void checkVarargs() {
        if (!allowVarargs) {
            log.error(S.pos(), "varargs.not.supported.in.source", source.name);
            allowVarargs = true;
        }
    }
    void checkForeach() {
        if (!allowForeach) {
            log.error(S.pos(), "foreach.not.supported.in.source", source.name);
            allowForeach = true;
        }
    }
    void checkStaticImports() {
        if (!allowStaticImport) {
            log.error(S.pos(), "static.import.not.supported.in.source", source.name);
            allowStaticImport = true;
        }
    }
    void checkAnnotations() {
        if (!allowAnnotations) {
            log.error(S.pos(), "annotations.not.supported.in.source", source.name);
            allowAnnotations = true;
        }
    }
}
