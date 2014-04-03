/*
 * @(#)Options.java	1.16 07/03/21
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

import com.sun.tools.javac.main.OptionName;
import java.util.*;

/** A table of all command-line options.
 *  If an option has an argument, the option name is mapped to the argument.
 *  If a set option has no argument, it is mapped to itself.
 *
 *  <p><b>This is NOT part of any API supported by Sun Microsystems.  If
 *  you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 */
@Version("@(#)Options.java	1.16 07/03/21")
public class Options {
	private static my.Debug DEBUG=new my.Debug(my.Debug.Options);//我加上的

    private static final long serialVersionUID = 0;

    /** The context key for the options. */
    public static final Context.Key<Options> optionsKey =
	new Context.Key<Options>();
    
    private LinkedHashMap<String,String> values;

    /** Get the Options instance for this context. */
    public static Options instance(Context context) {
	Options instance = context.get(optionsKey);
	if (instance == null)
	    instance = new Options(context);
	return instance;
    }

    protected Options(Context context) {
// DEBUGGING -- Use LinkedHashMap for reproducability
	values = new LinkedHashMap<String,String>();
	context.put(optionsKey, this);
    }
    
    //我加上的
    public String toString() {
    	String lineSeparator=System.getProperty("line.separator");
    	StringBuffer sb=new StringBuffer("options.size=").append(size());
    	if(size()>0) {
	    	sb.append(lineSeparator);
	    	sb.append("---------------------------------------------");
	    	sb.append(lineSeparator);
		    for(Map.Entry<String,String> myMapEntry: values.entrySet()) {
	        	sb.append(myMapEntry);
	        	sb.append(lineSeparator);
	        }
		    sb.append("---------------------------------------------");
		}
	    return sb.toString();
    }
    
    public String get(String name) {
	return values.get(name);
    }
    
    public String get(OptionName name) {
	return values.get(name.optionName);
    }
    
    public void put(String name, String value) {
	values.put(name, value);
    }
    
    public void put(OptionName name, String value) {
	values.put(name.optionName, value);
    }
    
    public void putAll(Options options) {
	values.putAll(options.values);
    }
    
    public void remove(String name) {
	values.remove(name);
    }
    
    public Set<String> keySet() {
	return values.keySet();
    }
    
    public int size() {
	return values.size();
    }

    static final String LINT = "-Xlint";

    /** Check for a lint suboption. */
    public boolean lint(String s) {
	DEBUG.P(this,"lint(String s)");
	DEBUG.P("s="+s);
	DEBUG.P("get("+(LINT + ":" + s)+")!=null = "+(get(LINT + ":" + s)!=null));
	DEBUG.P("get("+(LINT)+")!=null =" +(get(LINT)!=null));
	DEBUG.P("get("+(LINT + ":all")+")!=null = "+(get(LINT + ":all")!=null));
	DEBUG.P("get("+(LINT+":-"+s)+")==null = "+(get(LINT+":-"+s)==null));
	
	// return true if either the specific option is enabled, or
	// they are all enabled without the specific one being
	// disabled
	/*
	return
	    get(LINT + ":" + s)!=null ||
	    (get(LINT)!=null || get(LINT + ":all")!=null) &&
	        get(LINT+":-"+s)==null;
    */
    /*
	1:
    假设参数s="unchecked"，如果在javac命令行中指定了"-Xlint:unchecked"选项，
    那么get(LINT + ":" + s)=get("-Xlint:unchecked")="-Xlint:unchecked"!=null
    所以返回true;
    
	2:
    假设参数s="unchecked"，
    如果在javac命令行中只指定了"-Xlint"选项(或"-Xlint:all"选项)，
    那么get(LINT)=get("-Xlint")="-Xlint"!=null
    且get(LINT+":-"+s)=get("-Xlint:-unchecked")==null
    所以返回true;
    
	3:
    假设参数s="unchecked"，
    如果在javac命令行中指定了"-Xlint"选项(或"-Xlint:all"选项)，
    同时还指定了"-Xlint:-unchecked"选项，
    那么get(LINT)=get("-Xlint")="-Xlint"!=null
    但是get(LINT+":-"+s)=get("-Xlint:-unchecked")="-Xlint:-unchecked"!=null
    所以返回false;
    
    也就是说如果在javac命令行中指定了"-Xlint"选项(或"-Xlint:all"选项)，
    同时还指定了"-Xlint:-unchecked”选项，就相当于把“unchecked”从所有
    的lint中去掉，“-unchecked”中的“-”号表示取消紧跟在它后面的选项

	如果"-Xlint:unchecked"与"-Xlint:-unchecked”同时出现，那么也返回true，
	因为它先满足了条件1，所以直接返回true，||运算的第二分支已不执行
    */
    boolean returnFlag=get(LINT + ":" + s)!=null ||
	    (get(LINT)!=null || get(LINT + ":all")!=null) &&
	        get(LINT+":-"+s)==null;
    
    DEBUG.P("");
    DEBUG.P("returnFlag="+returnFlag);
	DEBUG.P(0,this,"lint(String s)");
	return returnFlag;
	}
}
