/*
 * @(#)Main.java	1.28 07/03/21
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

package com.sun.tools.javac;

import java.io.PrintWriter;
import java.lang.reflect.*;

import com.sun.tools.javac.util.Version;

/**
 * The programmatic interface for the Java Programming Language
 * compiler, javac.
 *
 * <p>Except for the two methods
 * {@link #compile(java.lang.String[])}
 * {@link #compile(java.lang.String[],java.io.PrintWriter)},
 * nothing described in this source file is part of any supported
 * API.  If you write code that depends on this, you do so at your own
 * risk.  This code and its internal interfaces are subject to change
 * or deletion without notice.
 */
@Version("@(#)Main.java	1.28 07/03/21")
public class Main {
    private static my.Debug DEBUG=new my.Debug(my.Debug.Main);//我加上的

    static {
	//getClassLoader()在java.lang.Class类中定义
	ClassLoader loader = Main.class.getClassLoader();
	//DEBUG.P("loader="+loader);
	if (loader != null)
		//loader.setDefaultAssertionStatus(false);//我加上的，没用
	    loader.setPackageAssertionStatus("com.sun.tools.javac", false);//没用
    }

    /** Unsupported command line interface.
     * @param args   The command line parameters.
     */
    public static void main(String[] args) throws Exception {
        /*
        如果第一个参数是-Xjdb选项时，可以进行调试，
        但需在classpath中加入%JAVA_HOME%\lib\tools.jar，否则会报错:
        Exception in thread "main" java.lang.ClassNotFoundException: com.sun.tools.example.debug.tty.TTY

        因为此编译器是通过"java com.sun.tools.javac.Main"的方式运行的，
        在JRE下没有tools.jar文件，JDK下才有。
        */
        if (args.length > 0 && args[0].equals("-Xjdb")) {
            String[] newargs = new String[args.length + 2];
            Class<?> c = Class.forName("com.sun.tools.example.debug.tty.TTY");
            Method method = c.getDeclaredMethod ("main", new Class[] {args.getClass()});
            method.setAccessible(true);
            System.arraycopy(args, 1, newargs, 3, args.length - 1);
            newargs[0] = "-connect";
            newargs[1] = "com.sun.jdi.CommandLineLaunch:options=-esa -ea:com.sun.tools...";
            newargs[2] = "com.sun.tools.javac.Main";
            method.invoke(null, new Object[] { newargs });
        } else {
            //System.exit(compile(args));

            //我加上的
            for (int i = 0; i < args.length; i++) {
                if(args[i].equals("-debug:off")) {
                        args[i]="-moreinfo";//-debug:off是我人为加入的命令行选项，编译器会报错
                        DEBUG.OFF();
                        break;
                }
            }

            DEBUG.P(Main.class,"main()");
            //当javac命令的最后一个参数是"*.java"时会把找到的java文件自动分配到args数组中
            for (int i = 0; i < args.length; i++) DEBUG.P("args["+(i)+"]="+args[i]);

            int resultCode=compile(args);

            DEBUG.P("resultCode="+com.sun.tools.javac.main.Main.resultCode(resultCode));
            DEBUG.P(0,Main.class,"main()");

            System.exit(resultCode);
        }
    }

    /** Programmatic interface to the Java Programming Language
     * compiler, javac.
     *
     * @param args The command line arguments that would normally be
     * passed to the javac program as described in the man page.
     * @return an integer equivalent to the exit value from invoking
     * javac, see the man page for details.
     */
    public static int compile(String[] args) {
        try {//我加上的
        DEBUG.P(Main.class,"compile(1)");
    
	com.sun.tools.javac.main.Main compiler =
            new com.sun.tools.javac.main.Main("javac");
	return compiler.compile(args);
	
	}finally{//我加上的
	DEBUG.P(0,Main.class,"compile(1)");
	}
    }

     
 
    /** Programmatic interface to the Java Programming Language
     * compiler, javac.
     *
     * @param args The command line arguments that would normally be
     * passed to the javac program as described in the man page.
     * @param out PrintWriter to which the compiler's diagnostic
     * output is directed.
     * @return an integer equivalent to the exit value from invoking
     * javac, see the man page for details.
     */
    public static int compile(String[] args, PrintWriter out) {
        try {//我加上的
        DEBUG.P(Main.class,"compile(2)");
    
	com.sun.tools.javac.main.Main compiler =
	    new com.sun.tools.javac.main.Main("javac", out);
	return compiler.compile(args);
	
        }finally{//我加上的
	DEBUG.P(0,Main.class,"compile(2)");
	}
    }
}
