/*
 * @(#)Keywords.java	1.19 07/03/21
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

import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Version;

import static com.sun.tools.javac.parser.Token.*;

/**
 * Map from Name to Token and Token to String.
 * 
 * <p><b>This is NOT part of any API supported by Sun Microsystems.
 * If you write code that depends on this, you do so at your own risk.
 * This code and its internal interfaces are subject to change or
 * deletion without notice.</b>
 */
@Version("@(#)Keywords.java	1.19 07/03/21")
public class Keywords {
	private static my.Debug DEBUG=new my.Debug(my.Debug.Keywords);//我加上的
	
    public static final Context.Key<Keywords> keywordsKey =
	new Context.Key<Keywords>();

    public static Keywords instance(Context context) {
	Keywords instance = context.get(keywordsKey);
	if (instance == null)
	    instance = new Keywords(context);
	return instance;
    }

    private final Log log;
    private final Name.Table names;

    protected Keywords(Context context) {
        DEBUG.P(this,"Keywords(1)");
	context.put(keywordsKey, this);
	log = Log.instance(context);
	names = Name.Table.instance(context);

	for (Token t : Token.values()) {
            /*
            Token是enum类型,values()对所有enum类型都通用，并不是Token中定义的方法,
            EOF,
            ERROR,
            IDENTIFIER,
            ABSTRACT("abstract"),
            对应的输出是:
            Token t=EOF name=null ordina=0
            Token t=ERROR name=null ordina=1
            Token t=IDENTIFIER name=null ordina=2
            Token t=ABSTRACT name=abstract ordina=3
            */
            //DEBUG.P("ordina="+t.ordinal()+"\t\tToken t="+t+"\t\tname="+t.name);
		
	    if (t.name != null)
		enterKeyword(t.name, t);
	    else
		tokenName[t.ordinal()] = null;
		
            DEBUG.P("tokenName["+t.ordinal()+"]="+tokenName[t.ordinal()]);
	}
	
	DEBUG.P("maxKey="+maxKey);
	
	key = new Token[maxKey+1];
	
	DEBUG.P("key.length="+key.length);
	
	for (int i = 0; i <= maxKey; i++) key[i] = IDENTIFIER;
	for (Token t : Token.values()) {
	    if (t.name != null)
		key[tokenName[t.ordinal()].index] = t;
	}
	
	for (int i = 0; i <= maxKey; i++) DEBUG.P("key["+i+"]="+key[i]);
	DEBUG.P(0,this,"Keywords(1)");
    }
    
    //检查Scanner当前得到的token是否是关键字
    //判断思路很简单,因关键字在没开始词法分析前已加入符号表，
    //并用maxKey记下所有关键字中在符号表字节数组的最大索引index
    //如果当前token的index>maxKey则当前的token肯定是IDENTIFIER
    //否则直接返回关键字key[name.index]
    
    /*
    Token枚举类的所有name!=null的枚举常量都被加进Name.Table类
    的names字段(一个字节数组)，并且都对应一个index，maxKey记录了其中的最大index，
    “Token[] key”的长度等于maxKey+1,在给“Token[] key”初始化时，
    如果“Token[] key”的下标刚好等于Token枚举类的所有name!=null的枚举常量在
    Name.Table类的names字段的index，那么“Token[] key”对应下标的元素值就是
    这个枚举常量，否则为IDENTIFIER
    */
    
    //这里的设计原则是:牺牲点内存空间，加快编译器的编译速度
    public Token key(Name name) {
	return (name.index > maxKey) ? IDENTIFIER : key[name.index];
    }

    /**
     * Keyword array. Maps name indices to Token.
     */
    private final Token[] key;//如何缩减数组的长度,见Name.Table类的构造方法

    /**	 The number of the last entered keyword.
     */
    private int maxKey = 0;

    /** The names of all tokens.
     */
    //这个字段是可有可无的，如果不需要这个字段，只需把enterKeyword方法的代码
    //移到构造方法中即可
    private Name[] tokenName = new Name[Token.values().length];

    public String token2string(Token token) {
	switch (token) {
	case IDENTIFIER:
	    return log.getLocalizedString("token.identifier");
	case CHARLITERAL:
	    return log.getLocalizedString("token.character");
	case STRINGLITERAL:
	    return log.getLocalizedString("token.string");
	case INTLITERAL:
	    return log.getLocalizedString("token.integer");
	case LONGLITERAL:
	    return log.getLocalizedString("token.long-integer");
	case FLOATLITERAL:
	    return log.getLocalizedString("token.float");
	case DOUBLELITERAL:
	    return log.getLocalizedString("token.double");
	case ERROR:
	    return log.getLocalizedString("token.bad-symbol");
	case EOF:
	    return log.getLocalizedString("token.end-of-input");
	case DOT: case COMMA: case SEMI: case LPAREN: case RPAREN:
	case LBRACKET: case RBRACKET: case LBRACE: case RBRACE:
	    return "'" + token.name + "'";
	default:
	    return token.name;
	}
    }

    private void enterKeyword(String s, Token token) {
	Name n = names.fromString(s);
	tokenName[token.ordinal()] = n;
	if (n.index > maxKey) maxKey = n.index;
    }
}
