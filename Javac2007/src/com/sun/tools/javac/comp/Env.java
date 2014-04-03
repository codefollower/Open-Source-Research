/*
 * @(#)Env.java	1.25 07/03/21
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

package com.sun.tools.javac.comp;

import com.sun.tools.javac.util.*;
import com.sun.tools.javac.tree.*;
import java.util.Iterator;
import java.util.NoSuchElementException;

/** A class for environments, instances of which are passed as
 *  arguments to tree visitors.  Environments refer to important ancestors
 *  of the subtree that's currently visited, such as the enclosing method,
 *  the enclosing class, or the enclosing toplevel node. They also contain
 *  a generic component, represented as a type parameter, to carry further
 *  information specific to individual passes.
 *
 *  <p><b>This is NOT part of any API supported by Sun Microsystems.  If
 *  you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 */
@Version("@(#)Env.java	1.25 07/03/21")
public class Env<A> implements Iterable<Env<A>> { //类全限定名称java.lang.Iterable

    /** The next enclosing environment.
     */
    //新dup的Env的next总是指向当前Env,所有的Env通过next组成一张线性表.
    public Env<A> next;

    /** The environment enclosing the current class.
     */
    public Env<A> outer;

    /** The tree with which this environment is associated.
     */
    public JCTree tree;

    /** The enclosing toplevel tree.
     */
    public JCTree.JCCompilationUnit toplevel;

    /** The next enclosing class definition.
     */
    public JCTree.JCClassDecl enclClass;

    /** The next enclosing method definition.
     */
    public JCTree.JCMethodDecl enclMethod;

    /** A generic field for further information.
     */
    public A info;

    /** Is this an environment for evaluating a base clause?
     */
    //比如与JCTypeParameter相关的Env，baseClause就为true
    //参考com.sun.tools.javac.comp.MemberEnter的baseEnv(2)方法
    public boolean baseClause = false; 
    

    /** Create an outermost environment for a given (toplevel)tree,
     *  with a given info field.
     */
    //这个构选方法只在生成JCCompilationUnit对应的Env时才直接调用，
    //对于其他JCTree子类的Env都是通过dup来间接调用这个构选方法的，
    //这样就确保JCCompilationUnit对应的Env总是在Env线性表的末尾，也就是说
    //它的next和outer都为null，而其他JCTree子类的Env的next不为null,outer取决于dup前Env的outer
    public Env(JCTree tree, A info) {
		this.next = null;
		this.outer = null;
		this.tree = tree;
		this.toplevel = null;
		this.enclClass = null;
		this.enclMethod = null;
		this.info = info;
    }

    /** Duplicate this environment, updating with given tree and info,
     *  and copying all other fields.
     */
    public Env<A> dup(JCTree tree, A info) {
		return dupto(new Env<A>(tree, info));
    }

    /** Duplicate this environment into a given Environment,
     *  using its tree and info, and copying all other fields.
     */
    public Env<A> dupto(Env<A> that) {
		that.next = this;
		that.outer = this.outer;
		that.toplevel = this.toplevel;
		that.enclClass = this.enclClass;
		that.enclMethod = this.enclMethod;
		return that;
    }

    /** Duplicate this environment, updating with given tree,
     *  and copying all other fields.
     */
    public Env<A> dup(JCTree tree) {
		return dup(tree, this.info);
    }

    /** Return closest enclosing environment which points to a tree with given tag.
     */
    public Env<A> enclosing(int tag) {
		Env<A> env1 = this;
		while (env1 != null && env1.tree.tag != tag) env1 = env1.next;
		return env1;
    }
	
	//public String toString() {
    //    return "Env[" + info + (outer == null ? "" : ",outer=" + outer) + "]";
    //}

	//下面的toString是我重写的，对于了解Env的状态非常重要
	static int tabs=0;
    public String toString() {
		tabs++;
        String TK=tree.getKind()+"";//代表TreeKind=tree.getKind()

    	String EC="";//代表enclClass.name
    	if(enclClass!=null) {
			if(com.sun.tools.javac.code.Symtab.MyPredefClass == enclClass.sym)
				EC="预定义";
			else
				EC=enclClass.name+"";
		}
        else EC="null";
        
        String EM="";//代表enclMethod.name
        if(enclMethod!=null) EM=enclMethod.name+"()";
        else EM="null";
        
        String TP="";//代表toplevel.pid
        if(toplevel!=null) TP=toplevel.pid+"";
        else TP="null";

        String tabStr="";
		for(int i=0;i<tabs;i++) tabStr+="    ";
		String ls=System.getProperty("line.separator");
		StringBuffer sb=new StringBuffer();
		sb.append("Env(BC=").append(baseClause);
		sb.append(" TK=").append(TK);
		sb.append(" EC=").append(EC);
		sb.append(" EM=").append(EM);
		sb.append(" TP=").append(TP).append(")");
		sb.append(ls);
		sb.append(tabStr).append("info =").append(info);
		sb.append(ls);
		sb.append(tabStr).append("outer=").append(outer);
		sb.append(ls);
		sb.append(tabStr).append("next =").append(next);
		tabs--;
		return sb.toString();
    }
    
    //注意:这里是用Env.outer字段来遍历的，而不是用Env.next字段
    public Iterator<Env<A>> iterator() {
        return new Iterator<Env<A>>() {
            Env<A> next = Env.this;
            public boolean hasNext() {
                return next.outer != null;
            }
            public Env<A> next() {
                if (hasNext()) {
                    Env<A> current = next;
                    next = current.outer;
                    return current;
                }
                throw new NoSuchElementException();

            }
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
