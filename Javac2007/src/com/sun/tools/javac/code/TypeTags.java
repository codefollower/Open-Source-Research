/*
 * @(#)TypeTags.java	1.24 07/03/21
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

package com.sun.tools.javac.code;

import com.sun.tools.javac.util.Version;

/** An interface for type tag values, which distinguish between different
 *  sorts of types.
 *
 *  <p><b>This is NOT part of any API supported by Sun Microsystems.  If
 *  you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 */
@Version("@(#)TypeTags.java	1.24 07/03/21")
public class TypeTags {

    private TypeTags() {} // uninstantiable

    /** The tag of the basic type `byte'.
     */
    public static final int BYTE = 1;

    /** The tag of the basic type `char'.
     */
    public static final int CHAR = BYTE+1;

    /** The tag of the basic type `short'.
     */
    public static final int SHORT = CHAR+1;

    /** The tag of the basic type `int'.
     */
    public static final int INT = SHORT+1;

    /** The tag of the basic type `long'.
     */
    public static final int LONG = INT+1;

    /** The tag of the basic type `float'.
     */
    public static final int FLOAT = LONG+1;

    /** The tag of the basic type `double'.
     */
    public static final int DOUBLE = FLOAT+1;

    /** The tag of the basic type `boolean'.
     */
    public static final int BOOLEAN = DOUBLE+1;

    /** The tag of the type `void'.
     */
    public static final int VOID = BOOLEAN+1;

    /** The tag of all class and interface types.
     */
    public static final int CLASS = VOID+1;

    /** The tag of all array types.
     */
    public static final int ARRAY = CLASS+1;

    /** The tag of all (monomorphic) method types.
     */
    public static final int METHOD = ARRAY+1;

    /** The tag of all package "types".
     */
    public static final int PACKAGE = METHOD+1;

    /** The tag of all (source-level) type variables.
     */
    public static final int TYPEVAR = PACKAGE+1;

    /** The tag of all type arguments.
     */
    public static final int WILDCARD = TYPEVAR+1;

    /** The tag of all polymorphic (method-) types.
     */
    public static final int FORALL = WILDCARD+1;

    /** The tag of the bottom type <null>.
     */
    public static final int BOT = FORALL+1;

    /** The tag of a missing type.
     */
    public static final int NONE = BOT+1;

    /** The tag of the error type.
     */
    public static final int ERROR = NONE+1;

    /** The tag of an unknown type
     */
    public static final int UNKNOWN = ERROR+1;

    /** The tag of all instantiatable type variables.
     */
    public static final int UNDETVAR = UNKNOWN+1;

    /** The number of type tags.
     */
    public static final int TypeTagCount = UNDETVAR+1;

    /** The maximum tag of a basic type.
     */
    public static final int lastBaseTag = BOOLEAN;

    /** The minimum tag of a partial type
     */
    public static final int firstPartialTag = ERROR;
    
    
    //下面是我加上的，调试用途
    public static String toString(int tag) {
        StringBuffer buf = new StringBuffer();
        if (BYTE == tag) buf.append("BYTE");
        else if (CHAR == tag) buf.append("CHAR");
        else if (SHORT == tag) buf.append("SHORT");
        else if (INT == tag) buf.append("INT");
        else if (LONG == tag) buf.append("LONG");
        else if (FLOAT == tag) buf.append("FLOAT");
        else if (DOUBLE == tag) buf.append("DOUBLE");
        else if (BOOLEAN == tag) buf.append("BOOLEAN");
        else if (VOID == tag) buf.append("VOID");
        else if (CLASS == tag) buf.append("CLASS");
        else if (ARRAY == tag) buf.append("ARRAY");
        else if (METHOD == tag) buf.append("METHOD");
        else if (PACKAGE == tag) buf.append("PACKAGE");
        else if (TYPEVAR == tag) buf.append("TYPEVAR");
        else if (WILDCARD == tag) buf.append("WILDCARD");
        else if (FORALL == tag) buf.append("FORALL");
        else if (BOT == tag) buf.append("BOT");
        else if (NONE == tag) buf.append("NONE");
        else if (ERROR == tag) buf.append("ERROR");
        else if (UNKNOWN == tag) buf.append("UNKNOWN");
        else if (UNDETVAR == tag) buf.append("UNDETVAR");
        //下面两个常量在com.sun.tools.javac.jvm.UninitializedType中定义
        else if (TypeTagCount == tag) buf.append("UNINITIALIZED_THIS");
        else if (TypeTagCount+1 == tag) buf.append("UNINITIALIZED_OBJECT");
        else buf.append(tag);

        return buf.toString();
    }
}
