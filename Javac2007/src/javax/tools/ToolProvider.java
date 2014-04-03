/*
 * @(#)ToolProvider.java	1.14 07/03/21
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

package javax.tools;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.logging.Level;
import static java.util.logging.Level.*;

/**
 * Provides methods for locating tool providers, for example,
 * providers of compilers.  This class complements the
 * functionality of {@link java.util.ServiceLoader}.
 *
 * @author Peter von der Ah&eacute;
 * @since 1.6
 */
public class ToolProvider {
    private static my.Debug DEBUG=new my.Debug(my.Debug.ToolProvider);//我加上的
    /*
    file:/home/zhh/java/jdk1.6.0_04/lib/tools.jar
    urls.length=1
    System.getProperty("sun.tools.ToolProvider")=null
    System.getProperty("sun.tools.ToolProvider")=sun.tools.ToolProvider
    getSystemJavaCompiler()
    findClass()
    trace()
    level=FINE
    reason=java.lang.ClassNotFoundException: com/sun/tools/javac/api/JavacTool
    System.getProperty(propertyName)=sun.tools.ToolProvider
    cls=javax.tools.ToolProvider
    st.length=19
    st=[Ljava.lang.StackTraceElement;@1de007d
    cls=javax.tools.ToolProvider$Lazy
    method=findClass(ToolProvider.java:179)
    logger.getLevel()=null
    logger.getLevel()=FINEST
    trace()
    level=FINE
    reason=file:/home/zhh/java/jdk1.6.0_04/lib/tools.jar
    System.getProperty(propertyName)=sun.tools.ToolProvider
    cls=javax.tools.ToolProvider
    st.length=19
    st=[Ljava.lang.StackTraceElement;@122c9df
    cls=javax.tools.ToolProvider$Lazy
    method=findClass(ToolProvider.java:187)
    logger.getLevel()=FINEST
    Lazy.compilerClass=class com.sun.tools.javac.api.JavacTool
    trace()
    level=WARNING
    reason=java.lang.IllegalAccessError: tried to access class com.sun.tools.javac.api.JavacTool$1 from class com.sun.tools.javac.api.JavacTool
    System.getProperty(propertyName)=sun.tools.ToolProvider
    cls=javax.tools.ToolProvider
    st.length=17
    st=[Ljava.lang.StackTraceElement;@b3b6a6
    cls=javax.tools.ToolProvider
    method=getSystemJavaCompiler(ToolProvider.java:131)
    logger.getLevel()=FINEST
    2008-2-16 17:41:29 javax.tools.ToolProvider getSystemJavaCompiler(ToolProvider.java:131)
    警告: java.lang.IllegalAccessError
    java.lang.IllegalAccessError: tried to access c................
    */

    private ToolProvider() {}
    

    private static final String propertyName = "sun.tools.ToolProvider";
    private static final String loggerName   = "javax.tools";

    /*
     * Define the system property "sun.tools.ToolProvider" to enable
     * debugging:
     *
     *     java ... -Dsun.tools.ToolProvider ...
     */
    static <T> T trace(Level level, Object reason) {
        try {//我加上的
        DEBUG.P(ToolProvider.class,"trace(2)");
        DEBUG.P("level="+level);
        DEBUG.P("reason="+reason);
        
        if (System.getProperty(propertyName) != null)
            DEBUG.P("System.getProperty(propertyName)="+System.getProperty(propertyName));
        else
            System.setProperty(propertyName,"sun.tools.ToolProvider");
          
        // NOTE: do not make this method private as it affects stack traces
        try {
            if (System.getProperty(propertyName) != null) {
                //类全限定名:java.lang.StackTraceElement
                StackTraceElement[] st = Thread.currentThread().getStackTrace();
                String method = "???";
                String cls = ToolProvider.class.getName();
                
                DEBUG.P("cls="+cls);
                DEBUG.P("st.length="+st.length);
                for(int i=0;i<st.length;i++) {
                    DEBUG.P("st["+i+"]="+st[i]);
                }
                
                
                if (st.length > 2) {
                    StackTraceElement frame = st[2];
                    method = String.format((Locale)null, "%s(%s:%s)",
                                           frame.getMethodName(),
                                           frame.getFileName(),
                                           frame.getLineNumber());
                    cls = frame.getClassName();
                    
                    DEBUG.P("cls="+cls);
                }
                
                DEBUG.P("method="+method);
                
                
                Logger logger = Logger.getLogger(loggerName);
                
                //下面的都是我加上的
                DEBUG.P("logger.getLevel()="+logger.getLevel());
                if(logger.getLevel()==null) {
                    //logger.setLevel(Level.ALL);
                    //logger.setLevel(Level.FINEST);
                    logger.setLevel(Level.FINE);
                    DEBUG.P("logger.getLevel()="+logger.getLevel());
                    //logger.fine(String.valueOf(reason));
                }
                
                DEBUG.P("reason.getClass()="+reason.getClass());
                DEBUG.P("reason.getClass().getName()="+reason.getClass().getName());
                
                
                if (reason instanceof Throwable) {
                    logger.logp(level, cls, method,
                                reason.getClass().getName(), (Throwable)reason);
                } else {
                    logger.logp(level, cls, method, String.valueOf(reason));
                }
            }
        } catch (SecurityException ex) {
            System.err.format((Locale)null, "%s: %s; %s%n",
                              ToolProvider.class.getName(),
                              reason,
                              ex.getLocalizedMessage());
        }
        return null;
        
        }finally{//我加上的
        DEBUG.P(0,ToolProvider.class,"trace(2)");
        }
    }

    /**
     * Gets the Java&trade; programming language compiler provided
     * with this platform.
     * @return the compiler provided with this platform or
     * {@code null} if no compiler is provided
     */
    public static JavaCompiler getSystemJavaCompiler() {
        //为了调试，要加-Xbootclasspath/p，如：java -Xbootclasspath/p:src:classes
        //因为javax包在/home/zhh/java/jdk1.6.0_04/jre/lib／rt.jar文件中也有，
        //加-Xbootclasspath/p:src:classes选项是为了优先从src与classes目录中查找。
        
        try {//我加上的
        DEBUG.P(ToolProvider.class,"getSystemJavaCompiler()");
        //DEBUG.P("Lazy.compilerClass="+Lazy.compilerClass);
        
        
        if (Lazy.compilerClass == null)
            return trace(WARNING, "Lazy.compilerClass == null");
        try {
            return Lazy.compilerClass.newInstance();
        } catch (Throwable e) {
            return trace(WARNING, e);
        }
        
        
        }finally{//我加上的
        DEBUG.P(0,ToolProvider.class,"getSystemJavaCompiler()");
        }
    }

    /**
     * Returns the class loader for tools provided with this platform.
     * This does not include user-installed tools.  Use the
     * {@linkplain java.util.ServiceLoader service provider mechanism}
     * for locating user installed tools.
     *
     * @return the class loader for tools provided with this platform
     * or {@code null} if no tools are provided
     */
    public static ClassLoader getSystemToolClassLoader() {
        try {//我加上的
        DEBUG.P(ToolProvider.class,"getSystemToolClassLoader()");
        //DEBUG.P("Lazy.compilerClass="+Lazy.compilerClass);
        
        if (Lazy.compilerClass == null)
            return trace(WARNING, "Lazy.compilerClass == null");
        return Lazy.compilerClass.getClassLoader();
        
        }finally{//我加上的
        DEBUG.P(0,ToolProvider.class,"getSystemToolClassLoader()");
        }
    }

    /**
     * This class will not be initialized until one of the above
     * methods are called.  This ensures that searching for the
     * compiler does not affect platform start up.
     */
    static class Lazy  {
        private static final String defaultJavaCompilerName
            = "com.sun.tools.javac.api.JavacTool";
        private static final String[] defaultToolsLocation
            = { "lib", "tools.jar" };
        static final Class<? extends JavaCompiler> compilerClass;
        static {
            DEBUG.P(Lazy.class,"static()");
            
            Class<? extends JavaCompiler> c = null;
            try {
                c = findClass().asSubclass(JavaCompiler.class);
            } catch (Throwable t) {
                trace(WARNING, t);
            }
            compilerClass = c;
            
            DEBUG.P(0,Lazy.class,"static()");
        }

        private static Class<?> findClass()
            throws MalformedURLException, ClassNotFoundException
        {
            try {//我加上的
            DEBUG.P(Lazy.class,"findClass()");
            
            //先直接从com.sun.tools.javac.api中找
            try {
                return enableAsserts(Class.forName(defaultJavaCompilerName, false, null));
            } catch (ClassNotFoundException e) {
                trace(FINE, e);
            }
            //找不到再从file:/home/zhh/java/jdk1.6.0_04/lib/tools.jar中找
            File file = new File(System.getProperty("java.home"));
            if (file.getName().equalsIgnoreCase("jre"))
                file = file.getParentFile();
            for (String name : defaultToolsLocation)
                file = new File(file, name);
            URL[] urls = {file.toURI().toURL()};
            trace(FINE, urls[0].toString());
            ClassLoader cl = URLClassLoader.newInstance(urls);
            cl.setPackageAssertionStatus("com.sun.tools.javac", true);
            return Class.forName(defaultJavaCompilerName, false, cl);
            
            }finally{//我加上的
            DEBUG.P(0,Lazy.class,"findClass()");
            }
        }

        private static Class<?> enableAsserts(Class<?> cls) {
            try {//我加上的
            DEBUG.P(Lazy.class,"enableAsserts(1)");
            DEBUG.P("cls="+cls);
            
            try {
                ClassLoader loader = cls.getClassLoader();
                if (loader != null)
                    loader.setPackageAssertionStatus("com.sun.tools.javac", true);
                else
                    trace(FINE, "loader == null");
            } catch (SecurityException ex) {
                trace(FINE, ex);
            }
            return cls;
            
            }finally{//我加上的
            DEBUG.P(0,Lazy.class,"enableAsserts(1)");
            }
        }
    }
}
