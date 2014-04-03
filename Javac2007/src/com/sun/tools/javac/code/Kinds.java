/*
 * @(#)Kinds.java	1.21 07/03/21
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

/** Internal symbol kinds, which distinguish between elements of
 *  different subclasses of Symbol. Symbol kinds are organized so they can be
 *  or'ed to sets.
 *
 *  <p><b>This is NOT part of any API supported by Sun Microsystems.  If
 *  you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 */
@Version("@(#)Kinds.java	1.21 07/03/21")
public class Kinds {

    private Kinds() {} // uninstantiable

    /** The empty set of kinds.
     */
    public final static int NIL = 0;

    /** The kind of package symbols.
     */
    public final static int PCK = 1 << 0;

    /** The kind of type symbols (classes, interfaces and type variables).
     */
    public final static int TYP = 1 << 1;

    /** The kind of variable symbols.
     */
    public final static int VAR = 1 << 2;

    /** The kind of values (variables or non-variable expressions), includes VAR.
     */
    public final static int VAL = (1 << 3) | VAR;

    /** The kind of methods.
     */
    public final static int MTH = 1 << 4;

    /** The error kind, which includes all other kinds.
     */
    public final static int ERR = (1 << 5) - 1;

    /** The set of all kinds.
     */
    public final static int AllKinds = ERR;

    /** Kinds for erroneous symbols that complement the above
     */
    public static final int ERRONEOUS = 1 << 6;
    public static final int AMBIGUOUS    = ERRONEOUS+1; // ambiguous reference
    public static final int HIDDEN       = ERRONEOUS+2; // hidden method or field
    public static final int STATICERR    = ERRONEOUS+3; // nonstatic member from static context
    public static final int ABSENT_VAR   = ERRONEOUS+4; // missing variable
    public static final int WRONG_MTHS   = ERRONEOUS+5; // methods with wrong arguments
    public static final int WRONG_MTH    = ERRONEOUS+6; // one method with wrong arguments
    public static final int ABSENT_MTH   = ERRONEOUS+7; // missing method
    public static final int ABSENT_TYP   = ERRONEOUS+8; // missing type
    
    
    //下面是我加上的，调试用途
    public static String toString(int kinds) {
        StringBuffer buf = new StringBuffer();
        
        if (kinds == NIL) buf.append("NIL");
        else if (kinds == ERR) buf.append("ERR");
        else if (kinds == ERRONEOUS) buf.append("ERRONEOUS");
        else if (kinds == AMBIGUOUS) buf.append("AMBIGUOUS");
        else if (kinds == HIDDEN) buf.append("HIDDEN");
        else if (kinds == STATICERR) buf.append("STATICERR");
        else if (kinds == ABSENT_VAR) buf.append("ABSENT_VAR");
        else if (kinds == WRONG_MTHS) buf.append("WRONG_MTHS");
        else if (kinds == WRONG_MTH) buf.append("WRONG_MTH");
        else if (kinds == ABSENT_MTH) buf.append("ABSENT_MTH");
        else if (kinds == ABSENT_TYP) buf.append("ABSENT_TYP");
        else {
            //if ((kinds&PCK) != 0) buf.append("PCK ");
            if ((kinds&PCK) == PCK) buf.append("PCK ");
            if ((kinds&TYP) != 0) buf.append("TYP ");
            
            //if ((kinds&VAL) != 0 && (kinds&VAL) != 0) buf.append("VAL ");
            if ((kinds&VAL) == VAL) buf.append("VAL ");
			else if ((kinds&VAR) != 0) buf.append("VAR ");

            if ((kinds&MTH) != 0) buf.append("MTH ");
        }
        
        /*
        if (kinds == NIL) buf.append("NIL");
        //if ((kinds&PCK) != 0) buf.append("PCK ");
        if ((kinds&PCK) == PCK) buf.append("PCK ");
        if ((kinds&TYP) != 0) buf.append("TYP ");
        if ((kinds&VAR) != 0) buf.append("VAR ");
        //if ((kinds&VAL) != 0 && (kinds&VAL) != 0) buf.append("VAL ");
        if ((kinds&VAL) == VAL) buf.append("VAL ");
        if ((kinds&MTH) != 0) buf.append("MTH ");
        
        if (((kinds&(1 << 5))-1) == ERR) buf.append("ERR ");
        
        if ((kinds&ERRONEOUS) != 0){
        if ((kinds&(ERRONEOUS|1)) == AMBIGUOUS) buf.append("AMBIGUOUS ");
        else if ((kinds&(ERRONEOUS|2)) == HIDDEN) buf.append("HIDDEN ");
        else if ((kinds&(ERRONEOUS|3)) == STATICERR) buf.append("STATICERR ");
        else if ((kinds&(ERRONEOUS|4)) == ABSENT_VAR) buf.append("ABSENT_VAR ");
        else if ((kinds&(ERRONEOUS|5)) == WRONG_MTHS) buf.append("WRONG_MTHS ");
        else if ((kinds&(ERRONEOUS|6)) == WRONG_MTH) buf.append("WRONG_MTH ");
        else if ((kinds&(ERRONEOUS|7)) == ABSENT_MTH) buf.append("ABSENT_MTH ");
        else if ((kinds&(ERRONEOUS|8)) == ABSENT_TYP) buf.append("ABSENT_TYP ");
        else buf.append("ERRONEOUS ");
    	}*/
        
        if(buf.length()==0) buf.append(kinds);
        return buf.toString();
    }
}
