/*
 * @(#)JavacProcessingEnvironment.java	1.30 07/03/21
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

package com.sun.tools.javac.processing;

import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.*;
import com.sun.tools.javac.code.*;
import com.sun.tools.javac.code.Symbol.*;
import com.sun.tools.javac.comp.*;
import com.sun.tools.javac.jvm.*;
import com.sun.tools.javac.tree.*;
import com.sun.tools.javac.parser.*;
import com.sun.tools.javac.code.Symbol.*;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.model.JavacTypes;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.main.JavaCompiler;
import java.io.StringWriter;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.util.*;

import javax.tools.JavaFileManager;
import javax.tools.StandardJavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.DiagnosticListener;
import static javax.tools.StandardLocation.*;

import java.util.*;
import java.util.regex.*;

import java.net.URLClassLoader;
import java.net.URL;
import java.io.Closeable;
import java.io.File;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.MalformedURLException;

import sun.misc.Service;
import sun.misc.ServiceConfigurationError;

/**
 * Objects of this class hold and manage the state needed to support
 * annotation processing.
 *
 * <p><b>This is NOT part of any API supported by Sun Microsystems.
 * If you write code that depends on this, you do so at your own risk.
 * This code and its internal interfaces are subject to change or
 * deletion without notice.</b>
 */
@Version("@(#)JavacProcessingEnvironment.java	1.30 07/03/21")
public class JavacProcessingEnvironment implements ProcessingEnvironment, Closeable {
    private static my.Debug DEBUG=new my.Debug(my.Debug.JavacProcessingEnvironment);//我加上的
    
    Options options;

    private final boolean printProcessorInfo;
    private final boolean printRounds;
    private final boolean verbose;
    private final boolean lint;
    private final boolean procOnly;
    private final boolean fatalErrors;
    
    //类全限定名称:com.sun.tools.javac.processing.JavacFiler
    private final JavacFiler filer;
    //类全限定名称:com.sun.tools.javac.processing.JavacMessager
    private final JavacMessager messager;
    //类全限定名称:com.sun.tools.javac.model.JavacElements
    private final JavacElements elementUtils;
    //类全限定名称:com.sun.tools.javac.model.JavacTypes
    private final JavacTypes typeUtils;

    /**
     * Holds relevant state history of which processors have been
     * used.
     */
    private DiscoveredProcessors discoveredProcs;

    /**
     * Map of processor-specific options.
     */
    private final Map<String, String> processorOptions;

    /**
     */
    private final Set<String> unmatchedProcessorOptions;

    /**
     * Annotations implicitly processed and claimed by javac.
     */
    private final Set<String> platformAnnotations;//JDK内部已有的注释类型

    /**
     * Set of packages given on command line.
     */
    private Set<PackageSymbol> specifiedPackages = Collections.emptySet();

    /** The log to be used for error reporting.
     */
    Log log;

    /**
     * Source level of the compile.
     */
    Source source;

    private Context context;

   public JavacProcessingEnvironment(Context context, Iterable<? extends Processor> processors) {
        DEBUG.P(this,"JavacProcessingEnvironment(2)");
        options = Options.instance(context);
        this.context = context;
        log = Log.instance(context);
        source = Source.instance(context);
        //输出有关请求处理程序处理哪些注释的信息
        printProcessorInfo = options.get("-XprintProcessorInfo") != null;
        //输出有关注释处理循环的信息
        printRounds = options.get("-XprintRounds") != null;
        verbose = options.get("-verbose") != null;
        //虽然-Xlint:processing选项在OptionName中没有定义
        //但还是可以通过下面的方式之一使得lint=true
        /*
        -Xlint
		-Xlint:all
		-Xlint:processing
		-XD-Xlint:processing
		*/
		lint = options.lint("processing");
		DEBUG.P("lint="+lint);//一般都为false
        procOnly = options.get("-proc:only") != null ||
            options.get("-Xprint") != null;
        fatalErrors = options.get("fatalEnterError") != null;
        platformAnnotations = initPlatformAnnotations();

        // Initialize services before any processors are initialzied
        // in case processors use them.
        filer = new JavacFiler(context);
        messager = new JavacMessager(context, this);
        elementUtils = new JavacElements(context);
        typeUtils = new JavacTypes(context);
        processorOptions = initProcessorOptions(context);
        unmatchedProcessorOptions = initUnmatchedProcessorOptions();
        initProcessorIterator(context, processors);
        DEBUG.P(0,this,"JavacProcessingEnvironment(2)");
    }

    private Set<String> initPlatformAnnotations() {
    	DEBUG.P(this,"initPlatformAnnotations()");
    	
    	//JDK内部已有的注释类型
        Set<String> platformAnnotations = new HashSet<String>();
        platformAnnotations.add("java.lang.Deprecated");
        platformAnnotations.add("java.lang.Override");
        platformAnnotations.add("java.lang.SuppressWarnings");
        platformAnnotations.add("java.lang.annotation.Documented");
        platformAnnotations.add("java.lang.annotation.Inherited");
        platformAnnotations.add("java.lang.annotation.Retention");
        platformAnnotations.add("java.lang.annotation.Target");
        
        DEBUG.P("Set<String> platformAnnotations="+platformAnnotations);
        DEBUG.P(0,this,"initPlatformAnnotations()");
        return Collections.unmodifiableSet(platformAnnotations);
    }

    private void initProcessorIterator(Context context, Iterable<? extends Processor> processors) {
    	DEBUG.P(this,"initProcessorIterator(2)");
        DEBUG.P("processors="+processors);
        DEBUG.P("options.get(\"-Xprint\")="+options.get("-Xprint"));
        
        //类全限定名称:java.util.Iterator
        //类全限定名称:com.sun.tools.javac.util.Paths
        //类全限定名称:com.sun.tools.javac.util.Log
        //类全限定名称:javax.annotation.processing.Processor
        Paths paths = Paths.instance(context);
        Log   log   = Log.instance(context);
        Iterator<? extends Processor> processorIterator;

        if (options.get("-Xprint") != null) {
        	/*
                (2008-02-15新注释:在ubuntu下面运行没有问题)
        	命令行:
        	java -classpath bin\classes com.sun.tools.javac.Main -Aaaa=bbb -Accc -Addd= -Xprint -target 1.7 -moreinfo -implicit:none -implicit:class -g:lines,vars,source -Xprefer:source -d bin\mybin -s bin\mysrc -classpath bin\mysrc;bin\mybin;bin\mybin\myJar.jar;. -Xlint:path -Xbootclasspath/p:bin\mybin -endorseddirs bin\mybin bin\mysrc\my\test\Test3.java
        	
        	未明白的内部编译器错误:
        	编译器 (compiler message file broken: key=compiler.misc.version.resource.missing arguments=1.6.0-beta2, {1}, {2}, {3}, {4}, {5}, {6}, {7}) 中出现异常。 
        	如果在 Bug Parade 中没有找到该错误，请在 Java Developer Connection (http://java.sun.com/webapps/bugreport)  对该错误进行归档。 
        	请在报告中附上您的程序和以下诊断信息。谢谢您的合作。
			java.lang.NoSuchMethodError: javax.lang.model.element.TypeElement.getQualifiedName()Ljavax/lang/model/element/Name;
		        at com.sun.tools.javac.processing.JavacProcessingEnvironment.discoverAndRunProcs(JavacProcessingEnvironment.java:573)
		        at com.sun.tools.javac.processing.JavacProcessingEnvironment.doProcessing(JavacProcessingEnvironment.java:757)
		        at com.sun.tools.javac.main.JavaCompiler.processAnnotations(JavaCompiler.java:1042)
		        at com.sun.tools.javac.main.JavaCompiler.compile(JavaCompiler.java:755)
		        at com.sun.tools.javac.main.Main.compile(Main.java:592)
		        at com.sun.tools.javac.main.Main.compile(Main.java:480)
		        at com.sun.tools.javac.main.Main.compile(Main.java:462)
		        at com.sun.tools.javac.Main.compile(Main.java:103)
		        at com.sun.tools.javac.Main.main(Main.java:85)
		    */
            try {
            	//类全限定名称:com.sun.tools.javac.processing.PrintingProcessor
                Processor processor = PrintingProcessor.class.newInstance();
                processorIterator = List.of(processor).iterator();
            } catch (Throwable t) {
                AssertionError assertError =
                    new AssertionError("Problem instantiating PrintingProcessor.");
                assertError.initCause(t);
                throw assertError;
            }
            DEBUG.P("processorIterator="+processorIterator);
        } else if (processors != null) {
            processorIterator = processors.iterator();
        } else {
            String processorNames = options.get("-processor");
	    	JavaFileManager fileManager = context.get(JavaFileManager.class);
            try {
                // If processorpath is not explicitly set, use the classpath.
                //在javax.tools.StandardLocation定义了ANNOTATION_PROCESSOR_PATH
                //没有指定“-processorpath <路径>”选项时，从
                ClassLoader processorCL = fileManager.hasLocation(ANNOTATION_PROCESSOR_PATH)
                    ? fileManager.getClassLoader(ANNOTATION_PROCESSOR_PATH)
                    : fileManager.getClassLoader(CLASS_PATH);

                /*
                 * If the "-processor" option is used, search the appropriate
                 * path for the named class.  Otherwise, use a service
                 * provider mechanism to create the processor iterator.
                 */
                DEBUG.P("processorNames="+processorNames);
                if (processorNames != null) {
                    processorIterator = new NameProcessIterator(processorNames, processorCL, log);
                } else {
                    //为什么要@SuppressWarnings("unchecked")呢？
                    //因为Service.providers返回的是一个没有范型化的LazyIterator类实例
                    //而it是一个范型化的Iterator<Processor>
                    @SuppressWarnings("unchecked")
                        Iterator<Processor> it =
                            Service.providers(Processor.class, processorCL);
							//Service不包含在javac的源码中,在rt.jar文件中
                    processorIterator = it;
                }
            } catch (SecurityException e) {
                /*
                 * A security exception will occur if we can't create a classloader.
                 * Ignore the exception if, with hindsight, we didn't need it anyway
                 * (i.e. no processor was specified either explicitly, or implicitly,
                 * in service configuration file.) Otherwise, we cannot continue.
                 */

				if (fileManager instanceof JavacFileManager) {
				    StandardJavaFileManager standardFileManager = (JavacFileManager) fileManager;
				    Iterable<? extends File> workingPath = fileManager.hasLocation(ANNOTATION_PROCESSOR_PATH)
					? standardFileManager.getLocation(ANNOTATION_PROCESSOR_PATH)
					: standardFileManager.getLocation(CLASS_PATH);
		
				    if (needClassLoader(processorNames, workingPath) ) {
					log.error("proc.cant.create.loader", e.getLocalizedMessage());
					throw new Abort(e);
				    }
				} else {
				    log.error("proc.cant.create.loader", e.getLocalizedMessage());
				    throw new Abort(e);
				}

                processorIterator =  new LinkedList<Processor>().iterator();
            }
        }
		discoveredProcs = new DiscoveredProcessors(processorIterator);
		DEBUG.P(0,this,"initProcessorIterator(2)");
    }

    private static class NameProcessIterator implements Iterator<Processor> {
	Processor nextProc = null;
        Iterator<String> names;
        ClassLoader processorCL;
        Log log;

        NameProcessIterator(String names, ClassLoader processorCL, Log log) {
            DEBUG.P(this,"NameProcessIterator(3)");
            DEBUG.P("names="+names);
            DEBUG.P("processorCL="+processorCL);

            this.names = Arrays.asList(names.split(",")).iterator();
            this.processorCL = processorCL;
            this.log = log;

            DEBUG.P(0,this,"NameProcessIterator(3)");
        }

        public boolean hasNext() {
            try {//我加上的
            DEBUG.P(this,"hasNext()");
            DEBUG.P("nextProc="+nextProc);
            DEBUG.P("names.hasNext()="+names.hasNext());

            if (nextProc != null)
                return true;
            else {
                if (!names.hasNext())
                    return false;
                else {
                    String processorName = names.next();
                    
                    DEBUG.P("processorName="+processorName);

                    Processor processor;
                    try {
                        try {
                            processor =
                                (Processor) (processorCL.loadClass(processorName).newInstance());
                        } catch (ClassNotFoundException cnfe) {
                            log.error("proc.processor.not.found", processorName);
                            return false;
                        } catch (ClassCastException cce) {
                            log.error("proc.processor.wrong.type", processorName);
                            return false;
                        } catch (Exception e ) {
                            log.error("proc.processor.cant.instantiate", processorName);
                            return false;
                        }
                    } catch(Throwable t) {
                        throw new AnnotationProcessingError(t);
                    }
                    nextProc = processor;
                    return true;
                }

            }

            }finally{//我加上的
            DEBUG.P(0,this,"hasNext()");
            }
        }

        public Processor next() {
            try {//我加上的
            DEBUG.P(this,"next()");

            if (hasNext()) {
                Processor p = nextProc;
                nextProc = null;
                return p;
            } else
                throw new NoSuchElementException();

            }finally{//我加上的
            DEBUG.P(0,this,"next()");
            }
        }

        public void remove () {
            throw new UnsupportedOperationException();
        }
    }

    public boolean atLeastOneProcessor() {
    	try {//我加上的
        DEBUG.P(this,"atLeastOneProcessor()");
		
        return discoveredProcs.iterator().hasNext();
        
        }finally{//我加上的
        DEBUG.P(0,this,"atLeastOneProcessor()");
        }
    }

    private Map<String, String> initProcessorOptions(Context context) {
    	DEBUG.P(this,"initProcessorOptions(1)");
        Options options = Options.instance(context);
        Set<String> keySet = options.keySet();
        Map<String, String> tempOptions = new LinkedHashMap<String, String>();

        for(String key : keySet) {
        	//参考com.sun.tools.javac.main.RecognizedOptions类在getAll()方
        	//法中对选项"-A"的处理
            if (key.startsWith("-A") && key.length() > 2) {
                int sepIndex = key.indexOf('=');
                String candidateKey = null;
                String candidateValue = null;

                if (sepIndex == -1)
                    candidateKey = key.substring(2);
                else if (sepIndex >= 3) {
                    candidateKey = key.substring(2, sepIndex);
                    candidateValue = (sepIndex < key.length()-1)?
                        key.substring(sepIndex+1) : null;
                }
                tempOptions.put(candidateKey, candidateValue);
            }
        }
        /*例子:
        Set<String> keySet=[-Aaaa=bbb, -Accc, -Addd=, -target, -moreinfo, -implicit, -implicit:none, -implicit:class, -g:, -g:lines, -g:vars, -g:source, -Xprefer, -Xprefer:source, -d, -s, -classpath, -Xlint:, -Xlint:path, -Xbootclasspath/p:, -endorseddirs]
		Map<String, String> tempOptions={aaa=bbb, ccc=null, ddd=null}
		*/
        DEBUG.P("Set<String> keySet="+keySet);
        DEBUG.P("Map<String, String> tempOptions="+tempOptions);
        DEBUG.P(0,this,"initProcessorOptions(1)");
        return Collections.unmodifiableMap(tempOptions);
    }

    private Set<String> initUnmatchedProcessorOptions() {
    	DEBUG.P(this,"initUnmatchedProcessorOptions()");
    	
        Set<String> unmatchedProcessorOptions = new HashSet<String>();
        unmatchedProcessorOptions.addAll(processorOptions.keySet());
        
        DEBUG.P("Set<String> unmatchedProcessorOptions="+unmatchedProcessorOptions);
        DEBUG.P(0,this,"initUnmatchedProcessorOptions()");
        return unmatchedProcessorOptions;
    }

    /**
     * State about how a processor has been used by the tool.  If a
     * processor has been used on a prior round, its process method is
     * called on all subsequent rounds, perhaps with an empty set of
     * annotations to process.  The {@code annotatedSupported} method
     * caches the supported annotation information from the first (and
     * only) getSupportedAnnotationTypes call to the processor.
     */
    static class ProcessorState {
        public Processor processor;
        public boolean   contributed;
        private ArrayList<Pattern> supportedAnnotationPatterns;
        private ArrayList<String>  supportedOptionNames;

        ProcessorState(Processor p, Log log, Source source, ProcessingEnvironment env) {
            processor = p;
            contributed = false;

            try {
                processor.init(env);

                checkSourceVersionCompatibility(source, log);

                supportedAnnotationPatterns = new ArrayList<Pattern>();
                for (String importString : processor.getSupportedAnnotationTypes()) {
                    supportedAnnotationPatterns.add(importStringToPattern(importString,
                                                                          processor,
                                                                          log));
                }

                supportedOptionNames = new ArrayList<String>();
                for (String optionName : processor.getSupportedOptions() ) {
                    if (checkOptionName(optionName, log))
                        supportedOptionNames.add(optionName);
                }

            } catch (Throwable t) {
                throw new AnnotationProcessingError(t);
            }
        }

        /**
         * Checks whether or not a processor's source version is
         * compatible with the compilation source version.  The
         * processor's source version needs to be greater than or
         * equal to the source version of the compile.
         */
        private void checkSourceVersionCompatibility(Source source, Log log) {
            SourceVersion procSourceVersion = processor.getSupportedSourceVersion();

            if (procSourceVersion.compareTo(Source.toSourceVersion(source)) < 0 )  {
                log.warning("proc.processor.incompatible.source.version",
                            procSourceVersion,
                            processor.getClass().getName(),
                            source.name);
            }
        }

        private boolean checkOptionName(String optionName, Log log) {
            boolean valid = isValidOptionName(optionName);
            if (!valid)
                log.error("proc.processor.bad.option.name",
                            optionName,
                            processor.getClass().getName());
            return valid;
        }

        public boolean annotationSupported(String annotationName) {
            boolean matche=true;//我加上的
            try {//我加上的
            DEBUG.P(this,"annotationSupported(1)");
            DEBUG.P("annotationName="+annotationName);
            

            for(Pattern p: supportedAnnotationPatterns) {
                if (p.matcher(annotationName).matches())
                    return true;
            }
            
            matche=false;//我加上的
            
            return false;
            
            }finally{//我加上的
            DEBUG.P("matche="+matche);
            DEBUG.P(0,this,"annotationSupported(1)");
            }
        }

        /**
         * Remove options that are matched by this processor.
         */
        public void removeSupportedOptions(Set<String> unmatchedProcessorOptions) {
            DEBUG.P(this,"removeSupportedOptions(1)");
            
            DEBUG.P("unmatchedProcessorOptions1="+unmatchedProcessorOptions);
            DEBUG.P("supportedOptionNames="+supportedOptionNames);
            
            unmatchedProcessorOptions.removeAll(supportedOptionNames);
            
            DEBUG.P("unmatchedProcessorOptions2="+unmatchedProcessorOptions);
            DEBUG.P(0,this,"removeSupportedOptions(1)");
        }
    }

    // TODO: These two classes can probably be rewritten better...
    /**
     * This class holds information about the processors that have
     * been discoverd so far as well as the means to discover more, if
     * necessary.  A single iterator should be used per round of
     * annotation processing.  The iterator first visits already
     * discovered processors then fails over to the service provided
     * mechanism if additional queries are made.
     */
    class DiscoveredProcessors implements Iterable<ProcessorState> {

        class ProcessorStateIterator implements Iterator<ProcessorState> {
            DiscoveredProcessors psi;
            Iterator<ProcessorState> innerIter;
            boolean onProcInterator;

            ProcessorStateIterator(DiscoveredProcessors psi) {
            	DEBUG.P(this,"ProcessorStateIterator(1)");
            	
                this.psi = psi;
                this.innerIter = psi.procStateList.iterator();
                this.onProcInterator = false;
                
                DEBUG.P(0,this,"ProcessorStateIterator(1)");
            }

            public ProcessorState next() {
                try {//我加上的
                DEBUG.P(this,"next()");
                DEBUG.P("onProcInterator="+onProcInterator);
                //DEBUG.P("innerIter.hasNext()="+innerIter.hasNext());
                //DEBUG.P("psi.processorIterator.hasNext()="+psi.processorIterator.hasNext());

                try {
                    if (!onProcInterator) {
                        if (innerIter.hasNext())
                            return innerIter.next();
                        else
                            onProcInterator = true;
                    }

                    if (psi.processorIterator.hasNext()) {
                        ProcessorState ps = new ProcessorState(psi.processorIterator.next(),
                        log, source, JavacProcessingEnvironment.this);
                        psi.procStateList.add(ps);
                        return ps;
                    } else
                        throw new NoSuchElementException();
                } catch (ServiceConfigurationError e) {
                    log.error("proc.bad.config.file", e.getLocalizedMessage());
                    throw new Abort();
                } catch (Throwable e) {
                    // TODO: needed while we use sun.misc.Service;
                    // not needed when we change to use java.util.Service
                    log.error("proc.processor.constructor.error", e.getLocalizedMessage());
                    throw new Abort();
                }

                }finally{//我加上的
                DEBUG.P(0,this,"next()");
                }
            }

            public boolean hasNext() {
            	try {//我加上的
                DEBUG.P(this,"hasNext()");
                DEBUG.P("onProcInterator="+onProcInterator);
                //DEBUG.P("innerIter.hasNext()="+innerIter.hasNext());
                //DEBUG.P("psi.processorIterator.hasNext()="+psi.processorIterator.hasNext());

                try {
                    if (onProcInterator)
                        return psi.processorIterator.hasNext();
                    else
                        return innerIter.hasNext() || psi.processorIterator.hasNext();
                } catch (ServiceConfigurationError e) {
                    log.error("proc.bad.config.file", e.getLocalizedMessage());
                    throw new Abort();
                } catch (Throwable e) {
                    // TODO: needed while we use sun.misc.Service;
                    // not needed when we change to use java.util.Service
                    log.error("proc.processor.constructor.error", e.getLocalizedMessage());
                    throw new Abort();
                }
                
                }finally{//我加上的
                DEBUG.P(0,this,"hasNext()");
                }
            }

            public void remove () {
                throw new UnsupportedOperationException();
            }

            /**
             * Run all remaining processors on the procStateList that
             * have not already run this round with an empty set of
             * annotations.
             */
            public void runContributingProcs(RoundEnvironment re) {
                DEBUG.P(this,"runContributingProcs(1)");
                DEBUG.P("onProcInterator="+onProcInterator);
                
                if (!onProcInterator) {
                    Set<TypeElement> emptyTypeElements = Collections.emptySet();
                    while(innerIter.hasNext()) {
                        ProcessorState ps = innerIter.next();
                        DEBUG.P("ps.contributed="+ps.contributed);
                        if (ps.contributed)
                            callProcessor(ps.processor, emptyTypeElements, re);
                    }
                }
                
                DEBUG.P(0,this,"runContributingProcs(1)");
            }
        }

        Iterator<? extends Processor> processorIterator;
        ArrayList<ProcessorState>  procStateList;

        public ProcessorStateIterator iterator() {
            try {//我加上的
            DEBUG.P(this,"iterator()");

            return new ProcessorStateIterator(this);
            
            }finally{//我加上的
            DEBUG.P(0,this,"iterator");
            }
        }

        DiscoveredProcessors(Iterator<? extends Processor> processorIterator) {
            DEBUG.P(this,"DiscoveredProcessors(1)");
            DEBUG.P("processorIterator="+processorIterator);
            
            this.processorIterator = processorIterator;
            this.procStateList = new ArrayList<ProcessorState>();
            
            DEBUG.P(0,this,"DiscoveredProcessors(1)");
        }
    }

    private void discoverAndRunProcs(Context context,
                                     Set<TypeElement> annotationsPresent,
				     List<ClassSymbol> topLevelClasses,
				     List<PackageSymbol> packageInfoFiles) {
        DEBUG.P(this,"discoverAndRunProcs(4)");
        DEBUG.P("annotationsPresent="+annotationsPresent);
        DEBUG.P("topLevelClasses="+topLevelClasses);
        DEBUG.P("packageInfoFiles="+packageInfoFiles);
                
        // Writer for -XprintRounds and -XprintProcessorInfo data
        PrintWriter xout = context.get(Log.outKey);

        Map<String, TypeElement> unmatchedAnnotations =
            new HashMap<String, TypeElement>(annotationsPresent.size());

        for(TypeElement a  : annotationsPresent) {
        	DEBUG.P("a.getClass().getName()="+a.getClass().getName());
                //(2008-02-16新注释:在ubuntu下没有问题)
        	//有Bug:  java.lang.NoSuchMethodError: javax.lang.model.element.TypeElement.getQualifiedName()Ljavax/lang/model/element/Name;
        	unmatchedAnnotations.put(a.getQualifiedName().toString(),
                                         a);
            
            /*
            //我加上的
        	if(a instanceof Symbol.ClassSymbol) {
        		DEBUG.P("a.getClassName()="+a.getClass());
        		Symbol.ClassSymbol s=(Symbol.ClassSymbol)a;
                unmatchedAnnotations.put(s.getQualifiedName().toString(),
                                         a);
			}*/
        }
        DEBUG.P("unmatchedAnnotations="+unmatchedAnnotations);

        // Give "*" processors a chance to match
        if (unmatchedAnnotations.size() == 0)
            unmatchedAnnotations.put("", null);

        DiscoveredProcessors.ProcessorStateIterator psi = discoveredProcs.iterator();
        // TODO: Create proper argument values; need past round
        // information to fill in this constructor.  Note that the 1
        // st round of processing could be the last round if there
        // were parse errors on the initial source files; however, we
        // are not doing processing in that case.

	Set<Element> rootElements = new LinkedHashSet<Element>();
	rootElements.addAll(topLevelClasses);
	rootElements.addAll(packageInfoFiles);
	rootElements = Collections.unmodifiableSet(rootElements);

        RoundEnvironment renv = new JavacRoundEnvironment(false,
                                                          false,
							  rootElements,
							  JavacProcessingEnvironment.this);

        while(unmatchedAnnotations.size() > 0 && psi.hasNext() ) {
            ProcessorState ps = psi.next();
            Set<String>  matchedNames = new HashSet<String>();
	    Set<TypeElement> typeElements = new LinkedHashSet<TypeElement>();
            for (String unmatchedAnnotationName : unmatchedAnnotations.keySet()) {
                if (ps.annotationSupported(unmatchedAnnotationName) ) {
                    matchedNames.add(unmatchedAnnotationName);
                    TypeElement te = unmatchedAnnotations.get(unmatchedAnnotationName);
                    if (te != null)
                        typeElements.add(te);
                }
            }
            
            DEBUG.P("matchedNames="+matchedNames);
            DEBUG.P("matchedNames.size()="+matchedNames.size());
            DEBUG.P("ps.contributed="+ps.contributed);

            if (matchedNames.size() > 0 || ps.contributed) {
                boolean processingResult = callProcessor(ps.processor, typeElements, renv);
                ps.contributed = true;
                ps.removeSupportedOptions(unmatchedProcessorOptions);
                 
                DEBUG.P("printProcessorInfo="+printProcessorInfo);
                if (printProcessorInfo || verbose) {
		    xout.println(Log.getLocalizedString("x.print.processor.info",
							ps.processor.getClass().getName(),
							matchedNames.toString(),
							processingResult));
                }
                
                DEBUG.P("processingResult="+processingResult);
                DEBUG.P("unmatchedAnnotations.keySet()="+unmatchedAnnotations.keySet());
                DEBUG.P("matchedNames="+matchedNames);
                
                if (processingResult) {
                    unmatchedAnnotations.keySet().removeAll(matchedNames);
                }
                
                DEBUG.P("unmatchedAnnotations="+unmatchedAnnotations);
                DEBUG.P("unmatchedAnnotations.keySet()="+unmatchedAnnotations.keySet());
            }
        }
        unmatchedAnnotations.remove("");
        
        DEBUG.P("lint="+lint);
        DEBUG.P("unmatchedAnnotations.size()="+unmatchedAnnotations.size());

        if (lint && unmatchedAnnotations.size() > 0) {
            // Remove annotations processed by javac
            unmatchedAnnotations.keySet().removeAll(platformAnnotations);
            if (unmatchedAnnotations.size() > 0) {
                log = Log.instance(context);
                log.warning("proc.annotations.without.processors",
                            unmatchedAnnotations.keySet());
            }
        }

        // Run contributing processors that haven't run yet
        psi.runContributingProcs(renv);

        // Debugging
        if (options.get("displayFilerState") != null)
            filer.displayState();
        DEBUG.P(0,this,"discoverAndRunProcs(4)");
    }

    /**
     * Computes the set of annotations on the symbol in question.
     * Leave class public for external testing purposes.
     */
    public static class ComputeAnnotationSet extends
        ElementScanner6<Set<TypeElement>, Set<TypeElement>> {
        final Elements elements;

        public ComputeAnnotationSet(Elements elements) {
            super();
            this.elements = elements;

            DEBUG.P(this,"ComputeAnnotationSet(1)");
            DEBUG.P("elements="+elements);
            DEBUG.P(0,this,"ComputeAnnotationSet(1)");
        }

	@Override
	public Set<TypeElement> visitPackage(PackageElement e, Set<TypeElement> p) {
	    // Don't scan enclosed elements of a package
	    return p;
	}

        @Override
         public Set<TypeElement> scan(Element e, Set<TypeElement> p) {
            try {//我加上的
            DEBUG.P(this,"scan(2)");
            DEBUG.P("e="+e);
            DEBUG.P("p="+p);

            for (AnnotationMirror annotationMirror :
                     elements.getAllAnnotationMirrors(e) ) {
                Element e2 = annotationMirror.getAnnotationType().asElement();
                p.add((TypeElement) e2);
            }
            
            DEBUG.P("p="+p);
                     
            return super.scan(e, p);

            }finally{//我加上的
            DEBUG.P(0,this,"scan(2)");
            }
        }
    }

    private boolean callProcessor(Processor proc,
                                         Set<? extends TypeElement> tes,
                                         RoundEnvironment renv) {
        try {//我加上的
        DEBUG.P(this,"callProcessor(3)");
        DEBUG.P("proc="+proc);
        DEBUG.P("tes="+tes);

        try {
            return proc.process(tes, renv);
        } catch (CompletionFailure ex) {    
            StringWriter out = new StringWriter();
            ex.printStackTrace(new PrintWriter(out));
	    log.error("proc.cant.access", ex.sym, ex.errmsg, out.toString());
            return false;
        } catch (Throwable t) {
            throw new AnnotationProcessingError(t);
        }
        
        }finally{//我加上的
        DEBUG.P(0,this,"callProcessor(3)");
        }
    }


    // TODO: internal catch clauses?; catch and rethrow an annotation
    // processing error
    public JavaCompiler doProcessing(Context context,
                                     List<JCCompilationUnit> roots,
                                     List<ClassSymbol> classSymbols,
                                     Iterable<? extends PackageSymbol> pckSymbols)
    throws IOException {
    	DEBUG.P(this,"doProcessing(4)");
        DEBUG.P("roots.size()="+roots.size());
    	DEBUG.P("classSymbols="+classSymbols);
    	DEBUG.P("pckSymbols="+pckSymbols);
    	
        log = Log.instance(context);
        // Writer for -XprintRounds and -XprintProcessorInfo data
        PrintWriter xout = context.get(Log.outKey);
        TaskListener taskListener = context.get(TaskListener.class);


        AnnotationCollector collector = new AnnotationCollector();

        JavaCompiler compiler = JavaCompiler.instance(context);
        compiler.todo.clear(); // free the compiler's resources

        int round = 0;

        // List<JCAnnotation> annotationsPresentInSource = collector.findAnnotations(roots);
        List<ClassSymbol> topLevelClasses = getTopLevelClasses(roots);

        for (ClassSymbol classSym : classSymbols)
            topLevelClasses = topLevelClasses.prepend(classSym);
        List<PackageSymbol> packageInfoFiles =
	    getPackageInfoFiles(roots);
	    
        DEBUG.P("topLevelClasses ="+topLevelClasses);
    	DEBUG.P("packageInfoFiles="+packageInfoFiles);

        Set<PackageSymbol> specifiedPackages = new LinkedHashSet<PackageSymbol>();
        for (PackageSymbol psym : pckSymbols)
            specifiedPackages.add(psym);
        this.specifiedPackages = Collections.unmodifiableSet(specifiedPackages);
        
        DEBUG.P("this.specifiedPackages="+this.specifiedPackages);
        
        // Use annotation processing to compute the set of annotations present
        Set<TypeElement> annotationsPresent = new LinkedHashSet<TypeElement>();
        ComputeAnnotationSet annotationComputer = new ComputeAnnotationSet(elementUtils);
        for (ClassSymbol classSym : topLevelClasses)
            annotationComputer.scan(classSym, annotationsPresent);
	for (PackageSymbol pkgSym : packageInfoFiles)
	    annotationComputer.scan(pkgSym, annotationsPresent);
        
        DEBUG.P("annotationsPresent="+annotationsPresent);

        Context currentContext = context;

        int roundNumber = 0;
        boolean errorStatus = false;

        runAround:
        while(true) {
            DEBUG.P("roundNumber="+roundNumber);
            DEBUG.P("fatalErrors="+fatalErrors);
            DEBUG.P("compiler.errorCount()="+compiler.errorCount());
            
            if (fatalErrors && compiler.errorCount() != 0) {
                errorStatus = true;
                break runAround;
            }

            this.context = currentContext;
            roundNumber++;
            printRoundInfo(xout, roundNumber, topLevelClasses, annotationsPresent, false);

            if (taskListener != null)
                taskListener.started(new TaskEvent(TaskEvent.Kind.ANNOTATION_PROCESSING_ROUND));

            try {
		discoverAndRunProcs(currentContext, annotationsPresent, topLevelClasses, packageInfoFiles);
            } finally {
                if (taskListener != null)
                    taskListener.finished(new TaskEvent(TaskEvent.Kind.ANNOTATION_PROCESSING_ROUND));
            }

            /*
             * Processors for round n have run to completion.  Prepare
             * for round (n+1) by checked for errors raised by
             * annotation processors and then checking for syntax
             * errors on any generated source files.
             */
            DEBUG.P("messager.errorRaised()="+messager.errorRaised());
            DEBUG.P("moreToDo()="+moreToDo());
            if (messager.errorRaised()) {
                errorStatus = true;
                break runAround;
            } else {
                if (moreToDo()) {
                    // <editor-fold defaultstate="collapsed">
                    
                    // annotationsPresentInSource = List.nil();
                    annotationsPresent = new LinkedHashSet<TypeElement>();
                    topLevelClasses  = List.nil();
		    packageInfoFiles = List.nil();

                    compiler.close();
                    currentContext = contextForNextRound(currentContext, true);

                    JavaFileManager fileManager = currentContext.get(JavaFileManager.class);

                    List<JavaFileObject> fileObjects = List.nil();
                    for (JavaFileObject jfo : filer.getGeneratedSourceFileObjects() ) {
                        fileObjects = fileObjects.prepend(jfo);
                    }


                    compiler = JavaCompiler.instance(currentContext);
                    List<JCCompilationUnit> parsedFiles = compiler.parseFiles(fileObjects);
                    roots = cleanTrees(roots).reverse();


                    for (JCCompilationUnit unit : parsedFiles)
                        roots = roots.prepend(unit);
                    roots = roots.reverse();

                    // Check for errors after parsing
                    if (compiler.parseErrors()) {
                        errorStatus = true;
                        break runAround;
                    } else {
                        ListBuffer<ClassSymbol> classes = enterNewClassFiles(currentContext);
                        compiler.enterTrees(roots);

                        // annotationsPresentInSource =
                        // collector.findAnnotations(parsedFiles);
                        classes.appendList(getTopLevelClasses(parsedFiles));
                        topLevelClasses  = classes.toList();
                        packageInfoFiles = getPackageInfoFiles(parsedFiles);

                        annotationsPresent = new LinkedHashSet<TypeElement>();
                        for (ClassSymbol classSym : topLevelClasses)
                            annotationComputer.scan(classSym, annotationsPresent);
			for (PackageSymbol pkgSym : packageInfoFiles)
			    annotationComputer.scan(pkgSym, annotationsPresent);

                        updateProcessingState(currentContext, false);
                    }
                    // </editor-fold>
                } else
                    break runAround; // No new files
            }
        }
        runLastRound(xout, roundNumber, errorStatus, taskListener);

        compiler.close();
        currentContext = contextForNextRound(currentContext, true);
        compiler = JavaCompiler.instance(currentContext);
        filer.newRound(currentContext, true);
        filer.warnIfUnclosedFiles();
        warnIfUnmatchedOptions();

       /*
        * If an annotation processor raises an error in a round,
        * that round runs to completion and one last round occurs.
        * The last round may also occur because no more source or
        * class files have been generated.  Therefore, if an error
        * was raised on either of the last *two* rounds, the compile
        * should exit with a nonzero exit code.  The current value of
        * errorStatus holds whether or not an error was raised on the
        * second to last round; errorRaised() gives the error status
        * of the last round.
        */
       errorStatus = errorStatus || messager.errorRaised();


        // Free resources
        this.close();

        if (taskListener != null)
            taskListener.finished(new TaskEvent(TaskEvent.Kind.ANNOTATION_PROCESSING));

        if (errorStatus) {
            compiler.log.nerrors += messager.errorCount();
            if (compiler.errorCount() == 0)
                compiler.log.nerrors++;
        } else if (procOnly) {
            compiler.todo.clear();
        } else { // Final compilation
            compiler.close();
            currentContext = contextForNextRound(currentContext, true);
            compiler = JavaCompiler.instance(currentContext);

            if (true) {
                compiler.enterTrees(cleanTrees(roots));
            } else {
                List<JavaFileObject> fileObjects = List.nil();
                for (JCCompilationUnit unit : roots)
                    fileObjects = fileObjects.prepend(unit.getSourceFile());
                roots = null;
                compiler.enterTrees(compiler.parseFiles(fileObjects.reverse()));
            }
        }
        
        DEBUG.P(0,this,"doProcessing(4)");
        return compiler;
    }

    // Call the last round of annotation processing
    private void runLastRound(PrintWriter xout,
                              int roundNumber,
                              boolean errorStatus,
                              TaskListener taskListener) throws IOException {
        DEBUG.P(this,"runLastRound(4)");
        DEBUG.P("roundNumber="+roundNumber);
        DEBUG.P("errorStatus="+errorStatus);
        
        roundNumber++;
        List<ClassSymbol> noTopLevelClasses = List.nil();
        Set<TypeElement> noAnnotations =  Collections.emptySet();
        printRoundInfo(xout, roundNumber, noTopLevelClasses, noAnnotations, true);

        Set<Element> emptyRootElements = Collections.emptySet(); // immutable
        RoundEnvironment renv = new JavacRoundEnvironment(true,
                                                          errorStatus,
                                                          emptyRootElements,
							  JavacProcessingEnvironment.this);
        if (taskListener != null)
            taskListener.started(new TaskEvent(TaskEvent.Kind.ANNOTATION_PROCESSING_ROUND));

        try {
            discoveredProcs.iterator().runContributingProcs(renv);
        } finally {
            if (taskListener != null)
                taskListener.finished(new TaskEvent(TaskEvent.Kind.ANNOTATION_PROCESSING_ROUND));
        }
        
        DEBUG.P(0,this,"runLastRound(4)");
    }

    private void updateProcessingState(Context currentContext, boolean lastRound) {
        filer.newRound(currentContext, lastRound);
        messager.newRound(currentContext);

        elementUtils.setContext(currentContext);
        typeUtils.setContext(currentContext);
    }

    private void warnIfUnmatchedOptions() {
        if (!unmatchedProcessorOptions.isEmpty()) {
            log.warning("proc.unmatched.processor.options", unmatchedProcessorOptions.toString());
        }
    }

    private void printRoundInfo(PrintWriter xout,
                                int roundNumber,
                                List<ClassSymbol> topLevelClasses,
                                Set<TypeElement> annotationsPresent,
                                boolean lastRound) {
        DEBUG.P(this,"printRoundInfo(5)");
        DEBUG.P("printRounds="+printRounds);
        DEBUG.P("verbose="+verbose);
        
        if (printRounds || verbose) {
	    xout.println(Log.getLocalizedString("x.print.rounds",
						roundNumber,
						"{" + topLevelClasses.toString(", ") + "}",
						annotationsPresent,
						lastRound));
        }
        
        DEBUG.P(0,this,"printRoundInfo(5)");
    }

    private ListBuffer<ClassSymbol> enterNewClassFiles(Context currentContext) {
        ClassReader reader = ClassReader.instance(currentContext);
        Name.Table names = Name.Table.instance(currentContext);
        ListBuffer<ClassSymbol> list = new ListBuffer<ClassSymbol>();

        for (Map.Entry<String,JavaFileObject> entry : filer.getGeneratedClasses().entrySet()) {
            Name name = names.fromString(entry.getKey());
            JavaFileObject file = entry.getValue();
            if (file.getKind() != JavaFileObject.Kind.CLASS)
                throw new AssertionError(file);
            ClassSymbol cs = reader.enterClass(name, file);
            list.append(cs);
        }
        return list;
    }

    /**
     * Free resources related to annotation processing.
     */
    public void close() {
        DEBUG.P(this,"close()");
        
        filer.close();
        discoveredProcs = null;
        
        DEBUG.P(0,this,"close()");
    }

    private List<ClassSymbol> getTopLevelClasses(List<? extends JCCompilationUnit> units) {
        List<ClassSymbol> classes = List.nil();
        for (JCCompilationUnit unit : units) {
	    for (JCTree node : unit.defs) {
		if (node.tag == JCTree.CLASSDEF) {
		    classes = classes.prepend(((JCClassDecl) node).sym);
		}
	    }
        }
        return classes.reverse();
    }

    private List<PackageSymbol> getPackageInfoFiles(List<? extends JCCompilationUnit> units) {
        List<PackageSymbol> packages = List.nil();
        for (JCCompilationUnit unit : units) {
	    boolean isPkgInfo = unit.sourcefile.isNameCompatible("package-info",
								 JavaFileObject.Kind.SOURCE);
	    if (isPkgInfo) {
		packages = packages.prepend(unit.packge);
	    }
        }
        return packages.reverse();
    }

    private Context contextForNextRound(Context context, boolean shareNames)
        throws IOException
    {
        Context next = new Context();

        Options options = Options.instance(context);
        assert options != null;
        next.put(Options.optionsKey, options);

        PrintWriter out = context.get(Log.outKey);
        assert out != null;
        next.put(Log.outKey, out);

        if (shareNames) {
            Name.Table names = Name.Table.instance(context);
            assert names != null;
            next.put(Name.Table.namesKey, names);
        }

        DiagnosticListener dl = context.get(DiagnosticListener.class);
        if (dl != null)
            next.put(DiagnosticListener.class, dl);

        TaskListener tl = context.get(TaskListener.class);
        if (tl != null)
            next.put(TaskListener.class, tl);

        JavaFileManager jfm = context.get(JavaFileManager.class);
        assert jfm != null;
        next.put(JavaFileManager.class, jfm);
        if (jfm instanceof JavacFileManager) {
            ((JavacFileManager)jfm).setContext(next);
        }

        Name.Table names = Name.Table.instance(context);
        assert names != null;
        next.put(Name.Table.namesKey, names);

        Keywords keywords = Keywords.instance(context);
        assert(keywords != null);
        next.put(Keywords.keywordsKey, keywords);

        JavaCompiler oldCompiler = JavaCompiler.instance(context);
        JavaCompiler nextCompiler = JavaCompiler.instance(next);
        nextCompiler.initRound(oldCompiler);

        JavacTaskImpl task = context.get(JavacTaskImpl.class);
        if (task != null) {
            next.put(JavacTaskImpl.class, task);
            task.updateContext(next);
        }

        context.clear();
        return next;
    }

    /*
     * Called retroactively to determine if a class loader was required,
     * after we have failed to create one.
     */
    private boolean needClassLoader(String procNames, Iterable<? extends File> workingpath) {
        if (procNames != null)
            return true;

	String procPath;
	URL[] urls = new URL[1]; 
	for(File pathElement : workingpath) {
	    try {
		urls[0] = pathElement.toURI().toURL();
		if (ServiceProxy.hasService(Processor.class, urls))
		    return true;
	    } catch (MalformedURLException ex) {
		throw new AssertionError(ex);
	    }
	    catch (ServiceProxy.ServiceConfigurationError e) {
		log.error("proc.bad.config.file", e.getLocalizedMessage());
		return true;
	    }
	}

        return false;
    }

    private class AnnotationCollector extends TreeScanner {
        List<JCTree> path = List.nil();
        static final boolean verbose = false;
        List<JCAnnotation> annotations = List.nil();

        public List<JCAnnotation> findAnnotations(List<? extends JCTree> nodes) {
            annotations = List.nil();
            scan(nodes);
            List<JCAnnotation> found = annotations;
            annotations = List.nil();
            return found.reverse();
        }

        public void scan(JCTree node) {
            if (node == null)
                return;
            Symbol sym = TreeInfo.symbolFor(node);
            if (sym != null)
                path = path.prepend(node);
            super.scan(node);
            if (sym != null)
                path = path.tail;
        }

        public void visitAnnotation(JCAnnotation node) {
            annotations = annotations.prepend(node);
            if (verbose) {
                StringBuilder sb = new StringBuilder();
                for (JCTree tree : path.reverse()) {
                    System.err.print(sb);
                    System.err.println(TreeInfo.symbolFor(tree));
                    sb.append("  ");
                }
                System.err.print(sb);
                System.err.println(node);
            }
        }
    }

    private static <T extends JCTree> List<T> cleanTrees(List<T> nodes) {
        for (T node : nodes)
            treeCleaner.scan(node);
        return nodes;
    }

    private static TreeScanner treeCleaner = new TreeScanner() {
            public void scan(JCTree node) {
                super.scan(node);
                if (node != null)
                    node.type = null;
            }
            public void visitTopLevel(JCCompilationUnit node) {
                node.packge = null;
                super.visitTopLevel(node);
            }
            public void visitClassDef(JCClassDecl node) {
                node.sym = null;
                super.visitClassDef(node);
            }
            public void visitMethodDef(JCMethodDecl node) {
                node.sym = null;
                super.visitMethodDef(node);
            }
            public void visitVarDef(JCVariableDecl node) {
                node.sym = null;
                super.visitVarDef(node);
            }
            public void visitNewClass(JCNewClass node) {
                node.constructor = null;
                super.visitNewClass(node);
            }
            public void visitAssignop(JCAssignOp node) {
                node.operator = null;
                super.visitAssignop(node);
            }
            public void visitUnary(JCUnary node) {
                node.operator = null;
                super.visitUnary(node);
            }
            public void visitBinary(JCBinary node) {
                node.operator = null;
                super.visitBinary(node);
            }
            public void visitSelect(JCFieldAccess node) {
                node.sym = null;
                super.visitSelect(node);
            }
            public void visitIdent(JCIdent node) {
                node.sym = null;
                super.visitIdent(node);
            }
        };


    private boolean moreToDo() {
        return filer.newFiles();
    }

    /**
     * {@inheritdoc}
     *
     * Command line options suitable for presenting to annotation
     * processors.  "-Afoo=bar" should be "-Afoo" => "bar".
     */
    public Map<String,String> getOptions() {
        return processorOptions;
    }

    public Messager getMessager() {
        return messager;
    }

    public Filer getFiler() {
        return filer;
    }

    public JavacElements getElementUtils() {
        return elementUtils;
    }

    public JavacTypes getTypeUtils() {
        return typeUtils;
    }

    public SourceVersion getSourceVersion() {
        return Source.toSourceVersion(source);
    }

    public Locale getLocale() {
	return Locale.getDefault();
    }

    public Set<Symbol.PackageSymbol> getSpecifiedPackages() {
        return specifiedPackages;
    }

    // Borrowed from DocletInvoker and apt
    // TODO: remove from apt's Main
    /**
     * Utility method for converting a search path string to an array
     * of directory and JAR file URLs.
     *
     * @param path the search path string
     * @return the resulting array of directory and JAR file URLs
     */
    public static URL[] pathToURLs(String path) {
        StringTokenizer st = new StringTokenizer(path, File.pathSeparator);
        URL[] urls = new URL[st.countTokens()];
        int count = 0;
        while (st.hasMoreTokens()) {
            URL url = fileToURL(new File(st.nextToken()));
            if (url != null) {
                urls[count++] = url;
            }
        }
        if (urls.length != count) {
            URL[] tmp = new URL[count];
            System.arraycopy(urls, 0, tmp, 0, count);
            urls = tmp;
        }
        return urls;
    }

    /**
     * Returns the directory or JAR file URL corresponding to the specified
     * local file name.
     *
     * @param file the File object
     * @return the resulting directory or JAR file URL, or null if unknown
     */
    private static URL fileToURL(File file) {
        String name;
        try {
            name = file.getCanonicalPath();
        } catch (IOException e) {
            name = file.getAbsolutePath();
        }
        name = name.replace(File.separatorChar, '/');
        if (!name.startsWith("/")) {
            name = "/" + name;
        }
        // If the file does not exist, then assume that it's a directory
        if (!file.isFile()) {
            name = name + "/";
        }
        try {
            return new URL("file", "", name);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("file");
        }
    }



    private static final Pattern allMatches = Pattern.compile(".*");

    private static final Pattern noMatches  = Pattern.compile("(\\P{all})+");
    /**
     * Convert import-style string to regex matching that string.  If
     * the string is a valid import-style string, return a regex that
     * won't match anything.
     */
    // TODO: remove version in Apt.java
    public static Pattern importStringToPattern(String s, Processor p, Log log) {
        if (s.equals("*")) {
            return allMatches;
        } else {
            String t = s;
            boolean star = false;

            /*
             * Validate string from factory is legal.  If the string
             * has more than one asterisks or the asterisks does not
             * appear as the last character (preceded by a period),
             * the string is not legal.
             */

            boolean valid = true;
            int index = t.indexOf('*');
            if (index != -1) {
                // '*' must be last character...
                if (index == t.length() -1) {
                     // ... and preceeding character must be '.'
                    if ( index-1 >= 0 ) {
                        valid = t.charAt(index-1) == '.';
                        // Strip off ".*$" for identifier checks
                        t = t.substring(0, t.length()-2);
                    }
                } else
                    valid = false;
            }

            // Verify string is off the form (javaId \.)+ or javaId
            if (valid) {
                String[] javaIds = t.split("\\.", t.length()+2);
                for(String javaId: javaIds)
                    valid &= SourceVersion.isIdentifier(javaId);
            }

            if (!valid) {
                log.warning("proc.malformed.supported.string", s, p.getClass().getName());
                return noMatches; // won't match any valid identifier
            }

            String s_prime = s.replaceAll("\\.", "\\\\.");

            if (s_prime.endsWith("*")) {
                s_prime =  s_prime.substring(0, s_prime.length() - 1) + ".+";
            }

            return Pattern.compile(s_prime);
        }
    }

    /**
     * For internal use by Sun Microsystems only.  This method will be
     * removed without warning.
     */
    public Context getContext() {
        return context;
    }

    public String toString() {
        return "javac ProcessingEnvironment version @(#)JavacProcessingEnvironment.java	1.30 07/03/21";
    }

    public static boolean isValidOptionName(String optionName) {
    	//点号(.)在正则表达式中可以表示任何字符(Any character)
    	//为了用它表示普通的点号(.)，得用“\”将它转义，但“\”
    	//又是一个特殊的字符，所以再加一个“\”就变成了“\\.”
    	//参考java.util.regex.Pattern类
        for(String s : optionName.split("\\.", -1)) {
            if (!SourceVersion.isIdentifier(s))
                return false;
        }
        return true;
    }
}

