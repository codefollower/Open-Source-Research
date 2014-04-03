/*
 * @(#)JavaCompiler.java	1.112 07/03/21
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

package com.sun.tools.javac.main;

import java.io.*;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.DiagnosticListener;

import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;

import com.sun.tools.javac.util.*;
import com.sun.tools.javac.code.*;
import com.sun.tools.javac.tree.*;
import com.sun.tools.javac.parser.*;
import com.sun.tools.javac.comp.*;
import com.sun.tools.javac.jvm.*;

import com.sun.tools.javac.code.Symbol.*;
import com.sun.tools.javac.tree.JCTree.*;

import com.sun.tools.javac.processing.*;
import javax.annotation.processing.Processor;

import static javax.tools.StandardLocation.CLASS_OUTPUT;
import static com.sun.tools.javac.util.ListBuffer.lb;

// TEMP, until we have a more efficient way to save doc comment info
import com.sun.tools.javac.parser.DocCommentScanner;

import javax.lang.model.SourceVersion;

/** This class could be the main entry point for GJC when GJC is used as a
 *  component in a larger software system. It provides operations to
 *  construct a new compiler, and to run a new compiler on a set of source
 *  files.
 *
 *  <p><b>This is NOT part of any API supported by Sun Microsystems.  If
 *  you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 */
@Version("@(#)JavaCompiler.java	1.112 07/03/21")
public class JavaCompiler implements ClassReader.SourceCompleter {
	private static my.Debug DEBUG=new my.Debug(my.Debug.JavaCompiler);//我加上的
	
    /** The context key for the compiler. */
    protected static final Context.Key<JavaCompiler> compilerKey =
        new Context.Key<JavaCompiler>();

    /** Get the JavaCompiler instance for this context. */
    public static JavaCompiler instance(Context context) {
        JavaCompiler instance = context.get(compilerKey);
        if (instance == null)
            instance = new JavaCompiler(context);
        return instance;
    }

    /** The current version number as a string.
     */
    public static String version() {
        return version("release");  // mm.nn.oo[-milestone]
    }

    /** The current full version number as a string.
     */
    public static String fullVersion() {
        return version("full"); // mm.mm.oo[-milestone]-build
    }
    
    //com\sun\tools\javac\resources\version.properties文件不存在
    private static final String versionRBName = "com.sun.tools.javac.resources.version";
    private static ResourceBundle versionRB;
    
    //因为com\sun\tools\javac\resources\version.properties文件不存在
    //所以返回值可能都类似这样:"compiler message file broken: key=..."
    //详情请看com.sun.tools.javac.util.Messages类
    private static String version(String key) {
        if (versionRB == null) {
            try {
                versionRB = ResourceBundle.getBundle(versionRBName);
            } catch (MissingResourceException e) {
                return Log.getLocalizedString("version.resource.missing", System.getProperty("java.version"));
            }
        }
        try {
            return versionRB.getString(key);
        }
        catch (MissingResourceException e) {
            return Log.getLocalizedString("version.unknown", System.getProperty("java.version"));
        }
    }
    
    //对照下面的compile2()方法的代码一起看,就会明白下面几个枚举常量的作用
    private static enum CompilePolicy {
        /*
         * Just attribute the parse trees
         */
        ATTR_ONLY,

        /*
         * Just attribute and do flow analysis on the parse trees.
         * This should catch most user errors.
         */
        CHECK_ONLY,

        /*
         * Attribute everything, then do flow analysis for everything,
         * then desugar everything, and only then generate output.
         * Means nothing is generated if there are any errors in any classes.
         */
        SIMPLE,

        /*
         * After attributing everything and doing flow analysis,
         * group the work by compilation unit.
         * Then, process the work for each compilation unit together.
         * Means nothing is generated for a compilation unit if the are any errors
         * in the compilation unit  (or in any preceding compilation unit.)
         */
        BY_FILE,

        /*
         * Completely process each entry on the todo list in turn.
         * -- this is the same for 1.5.
         * Means output might be generated for some classes in a compilation unit
         * and not others.
         */
        BY_TODO;

        static CompilePolicy decode(String option) {
            if (option == null)
                return DEFAULT_COMPILE_POLICY;
            else if (option.equals("attr"))
                return ATTR_ONLY;
            else if (option.equals("check"))
                return CHECK_ONLY;
            else if (option.equals("simple"))
                return SIMPLE;
            else if (option.equals("byfile"))
                return BY_FILE;
            else if (option.equals("bytodo"))
                return BY_TODO;
            else
                return DEFAULT_COMPILE_POLICY;
        }
    }

    private static CompilePolicy DEFAULT_COMPILE_POLICY = CompilePolicy.BY_TODO;
    
    //参考1.7新增标准选项:IMPLICIT("-implicit:{none,class}"),
    //指定是否为隐式引用文件生成类文件
    private static enum ImplicitSourcePolicy {
        /** Don't generate or process implicitly read source files. */
        NONE,
        /** Generate classes for implicitly read source files. */
        CLASS,
        /** Like CLASS, but generate warnings if annotation processing occurs */
        UNSET;
        
        static ImplicitSourcePolicy decode(String option) {
            if (option == null)
                return UNSET;
            else if (option.equals("none"))
                return NONE;
            else if (option.equals("class"))
                return CLASS;
            else
                return UNSET;
        }
    }
    
    /** The log to be used for error reporting.
     */
    public Log log;//类全限定名称:com.sun.tools.javac.util.Log

    /** The tree factory module.
     */
    protected TreeMaker make;//类全限定名称:com.sun.tools.javac.tree.TreeMaker

    /** The class reader.
     */
    protected ClassReader reader;//类全限定名称:com.sun.tools.javac.jvm.ClassReader

    /** The class writer.
     */
    protected ClassWriter writer;//类全限定名称:com.sun.tools.javac.jvm.ClassWriter

    /** The module for the symbol table entry phases.
     */
    protected Enter enter;//类全限定名称:com.sun.tools.javac.comp.Enter

    /** The symbol table.
     */
    protected Symtab syms;//类全限定名称:com.sun.tools.javac.code.Symtab

    /** The language version.
     */
    protected Source source;//类全限定名称:com.sun.tools.javac.code.Source

    /** The module for code generation.
     */
    protected Gen gen;//类全限定名称:com.sun.tools.javac.jvm.Gen

    /** The name table.
     */
    protected Name.Table names;//类全限定名称:com.sun.tools.javac.util.Name.Table

    /** The attributor.
     */
    protected Attr attr;//类全限定名称:com.sun.tools.javac.comp.Attr

    /** The attributor.
     */
    protected Check chk;//类全限定名称:com.sun.tools.javac.comp.Check

    /** The flow analyzer.
     */
    protected Flow flow;//类全限定名称:com.sun.tools.javac.comp.Flow

    /** The type eraser.
     */
    TransTypes transTypes;//类全限定名称:com.sun.tools.javac.comp.TransTypes

    /** The syntactic sugar desweetener.
     */
    Lower lower;//类全限定名称:com.sun.tools.javac.comp.Lower

    /** The annotation annotator.
     */
    protected Annotate annotate;//类全限定名称:com.sun.tools.javac.comp.Annotate

    /** Force a completion failure on this name
     */
    protected final Name completionFailureName;//类全限定名称:com.sun.tools.javac.util.Name

    /** Type utilities.
     */
    protected Types types;//类全限定名称:com.sun.tools.javac.code.Types

    /** Access to file objects.
     */
    protected JavaFileManager fileManager;//类全限定名称:javax.tools.JavaFileManager

    /** Factory for parsers.
     */
    protected Parser.Factory parserFactory;//类全限定名称:com.sun.tools.javac.parser.Parser.Factory

    /** Optional listener for progress events
     */
    protected TaskListener taskListener;//类全限定名称:com.sun.source.util.TaskListener

    /**
     * Annotation processing may require and provide a new instance
     * of the compiler to be used for the analyze and generate phases.
     */
    protected JavaCompiler delegateCompiler;
    
    /**
     * Flag set if any annotation processing occurred.
     **/
    protected boolean annotationProcessingOccurred;
    
    /**
     * Flag set if any implicit source files read.
     **/
    protected boolean implicitSourceFilesRead;

    protected Context context;//类全限定名称:com.sun.tools.javac.util.Context

    /** Construct a new compiler using a shared context.
     */
    public JavaCompiler(final Context context) {
    	DEBUG.P(this,"JavaCompiler(1)");
        this.context = context;
        context.put(compilerKey, this);

        // if fileManager not already set, register the JavacFileManager to be used
        if (context.get(JavaFileManager.class) == null)
            JavacFileManager.preRegister(context);

        names = Name.Table.instance(context);
        log = Log.instance(context);
        reader = ClassReader.instance(context);
        make = TreeMaker.instance(context);
        writer = ClassWriter.instance(context);
        enter = Enter.instance(context);
        todo = Todo.instance(context);//类全限定名称:com.sun.tools.javac.comp.Todo

        fileManager = context.get(JavaFileManager.class);
        parserFactory = Parser.Factory.instance(context);

        try {
            // catch completion problems with predefineds
            syms = Symtab.instance(context);//这一步值得注意
        } catch (CompletionFailure ex) { //类全限定名称:com.sun.tools.javac.code.Symbol.CompletionFailure
            // inlined Check.completionError as it is not initialized yet
            log.error("cant.access", ex.sym, ex.errmsg);
            if (ex instanceof ClassReader.BadClassFile)
                throw new Abort();
        }
        source = Source.instance(context);
        attr = Attr.instance(context);
        chk = Check.instance(context);
        gen = Gen.instance(context);
        flow = Flow.instance(context);
        transTypes = TransTypes.instance(context);
        lower = Lower.instance(context);
        annotate = Annotate.instance(context);
        types = Types.instance(context);
        taskListener = context.get(TaskListener.class);

        reader.sourceCompleter = this;

        Options options = Options.instance(context);
        DEBUG.P("options="+options);
        
        //下面的选项有些在com.sun.tools.javac.main.OptionName类中是没有的
        verbose       = options.get("-verbose")       != null;
        sourceOutput  = options.get("-printsource")   != null; // used to be -s
        stubOutput    = options.get("-stubs")         != null;
        relax         = options.get("-relax")         != null;
        printFlat     = options.get("-printflat")     != null;
        attrParseOnly = options.get("-attrparseonly") != null;
        encoding      = options.get("-encoding");
        lineDebugInfo = options.get("-g:")            == null ||
                        options.get("-g:lines")       != null;
                        
        genEndPos     = options.get("-Xjcov")         != null ||
        				//类全限定名称:javax.tools.DiagnosticListener
        				//见Log类的Log(4)方法
                        context.get(DiagnosticListener.class) != null;
                      
        devVerbose    = options.get("dev") != null;  
        processPcks   = options.get("process.packages") != null;

        verboseCompilePolicy = options.get("verboseCompilePolicy") != null;
        
        DEBUG.P("genEndPos="+genEndPos);  
        DEBUG.P("devVerbose="+devVerbose);  
        DEBUG.P("processPcks="+processPcks);  
        DEBUG.P("verboseCompilePolicy="+verboseCompilePolicy);  
        DEBUG.P("attrParseOnly="+attrParseOnly); 

        if (attrParseOnly)
            compilePolicy = CompilePolicy.ATTR_ONLY;
        else
            compilePolicy = CompilePolicy.decode(options.get("compilePolicy"));
        
        implicitSourcePolicy = ImplicitSourcePolicy.decode(options.get("-implicit"));

        completionFailureName =
            (options.get("failcomplete") != null)
            ? names.fromString(options.get("failcomplete"))
            : null;
            
        DEBUG.P(0,this,"JavaCompiler(1)");
    }

    /* Switches:
     */

    /** Verbose output.
     */
    public boolean verbose;

    /** Emit plain Java source files rather than class files.
     */
    public boolean sourceOutput;

    /** Emit stub source files rather than class files.
     */
    public boolean stubOutput;

    /** Generate attributed parse tree only.
     */
    public boolean attrParseOnly;

    /** Switch: relax some constraints for producing the jsr14 prototype.
     */
    boolean relax;

    /** Debug switch: Emit Java sources after inner class flattening.
     */
    public boolean printFlat;

    /** The encoding to be used for source input.
     */
    public String encoding;

    /** Generate code with the LineNumberTable attribute for debugging
     */
    public boolean lineDebugInfo;

    /** Switch: should we store the ending positions?
     */
    public boolean genEndPos;

    /** Switch: should we debug ignored exceptions
     */
    protected boolean devVerbose;

    /** Switch: should we (annotation) process packages as well
     */
    protected boolean processPcks;

    /** Switch: is annotation processing requested explitly via
     * CompilationTask.setProcessors?
     */
    protected boolean explicitAnnotationProcessingRequested = false;

    /**
     * The policy for the order in which to perform the compilation
     */
    protected CompilePolicy compilePolicy;
    
    /**
     * The policy for what to do with implicitly read source files
     */
    protected ImplicitSourcePolicy implicitSourcePolicy;

    /**
     * Report activity related to compilePolicy
     */
    public boolean verboseCompilePolicy;

    /** A queue of all as yet unattributed classes.
     */
    public Todo todo;

    private Set<Env<AttrContext>> deferredSugar = new HashSet<Env<AttrContext>>();

    /** The set of currently compiled inputfiles, needed to ensure
     *  we don't accidentally overwrite an input file when -s is set.
     *  initialized by `compile'.
     */
    protected Set<JavaFileObject> inputFiles = new HashSet<JavaFileObject>();

    /** The number of errors reported so far.
     */
    public int errorCount() {
        if (delegateCompiler != null && delegateCompiler != this)
            return delegateCompiler.errorCount();
        else
            return log.nerrors;
    }
    
    //在编译的每个阶段里都有可能找到错误，如果某一阶段找到了错误导致
    //接下来的阶段任务无法进行，就会先调用stopIfError()方法，如果错误
    //数为0，就继续下一阶段的任务，否则编译不正常结束。
    protected final <T> List<T> stopIfError(ListBuffer<T> listBuffer) {
        if (errorCount() == 0)
            return listBuffer.toList();
        else
            return List.nil();
    }

    protected final <T> List<T> stopIfError(List<T> list) {
        if (errorCount() == 0)
            return list;
        else
            return List.nil();
    }

    /** The number of warnings reported so far.
     */
    public int warningCount() {
        if (delegateCompiler != null && delegateCompiler != this)
            return delegateCompiler.warningCount();
        else
            return log.nwarnings;
    }
    
    /** Whether or not any parse errors have occurred.
     */
    public boolean parseErrors() {
	return parseErrors;
    }

    protected Scanner.Factory getScannerFactory() {
    	try {//我加上的
    	DEBUG.P(this,"getScannerFactory()");
    	
        return Scanner.Factory.instance(context);
        
        }finally{//我加上的
		DEBUG.P(0,this,"getScannerFactory()");
		}
    }

    /** Try to open input stream with given name.
     *  Report an error if this fails.
     *  @param filename   The file name of the input stream to be opened.
     */
    //类全限定名称:java.lang.CharSequence
    public CharSequence readSource(JavaFileObject filename) {
    	try {//我加上的
    	DEBUG.P(this,"readSource(1)");
        DEBUG.P("filename="+filename);
    	
        try {
            inputFiles.add(filename);
            //在这里实际已开始读取源文件的内容了
            //参考com.sun.tools.javac.main.Main类compile()方法中的注释
            return filename.getCharContent(false);
        } catch (IOException e) {
            log.error("error.reading.file", filename, e.getLocalizedMessage());
            return null;
        }
        
        }finally{//我加上的
        DEBUG.P(0,this,"readSource(1)");
        }
    }

    /** Parse contents of input stream.
     *  @param filename     The name of the file from which input stream comes.
     *  @param input        The input stream to be parsed.
     */
    protected JCCompilationUnit parse(JavaFileObject filename, CharSequence content) {
        DEBUG.P(this,"parse(2)");
        
        long msec = now();
        
        //生成一棵空JCCompilationUnit树，
        //JCCompilationUnit是最顶层的抽象语法树(abstract syntax tree)
        //参考com.sun.tools.javac.tree.JCTree类与com.sun.tools.javac.tree.TreeMaker类
        JCCompilationUnit tree = make.TopLevel(List.<JCTree.JCAnnotation>nil(),
                                      null, List.<JCTree>nil());
        if (content != null) {
            if (verbose) {
                printVerbose("parsing.started", filename);
            }
            
            //taskListener在这为空,因为这个版本的Javac还没有任
            //何类实现com.sun.source.util.TaskListener接口
        	DEBUG.P("taskListener="+taskListener);
            if (taskListener != null) {
                TaskEvent e = new TaskEvent(TaskEvent.Kind.PARSE, filename);
                taskListener.started(e);
            }
            
	    	int initialErrorCount = log.nerrors;
	    	
	    	//建立一个词法分析类Scanner的实例,并指向第一个字符
            Scanner scanner = getScannerFactory().newScanner(content);
            
            //建立一个语法分析类Parser的实例,并指向第一个token
            Parser parser = parserFactory.newParser(scanner, keepComments(), genEndPos);
            
            //java语言的语法符合LL(1)文法,所以采用的是递归下降分析算法,
            //对于二元运算表达式采用运算符优先级算法
            //Parser通过nextToken()来驱动Scanner
            tree = parser.compilationUnit();
            
	    	parseErrors |= (log.nerrors > initialErrorCount);
            if (lineDebugInfo) {
                tree.lineMap = scanner.getLineMap();
            }
            if (verbose) {
                printVerbose("parsing.done", Long.toString(elapsed(msec)));
            }
        }

        tree.sourcefile = filename;

        if (content != null && taskListener != null) {
            TaskEvent e = new TaskEvent(TaskEvent.Kind.PARSE, tree);
            taskListener.finished(e);
        }
        
        DEBUG.P(0,this,"parse(2)");
        return tree;
    }
    // where
        public boolean keepComments = false;
        protected boolean keepComments() {
            return keepComments || sourceOutput || stubOutput;
        }


    /** Parse contents of file.
     *  @param filename     The name of the file to be parsed.
     */
    @Deprecated
    public JCTree.JCCompilationUnit parse(String filename) throws IOException {
	JavacFileManager fm = (JavacFileManager)fileManager;
        return parse(fm.getJavaFileObjectsFromStrings(List.of(filename)).iterator().next());
    }

    /** Parse contents of file.
     *  @param filename     The name of the file to be parsed.
     */
    public JCTree.JCCompilationUnit parse(JavaFileObject filename) {
    	DEBUG.P(this,"parse(1)");
    	
    	//将log内部的引用文件切换到当前待处理的文件filename
        JavaFileObject prev = log.useSource(filename);
        try {
            JCTree.JCCompilationUnit t = parse(filename, readSource(filename));
            if (t.endPositions != null)
                log.setEndPosTable(filename, t.endPositions);
            return t;
        } finally {
            log.useSource(prev);//将log内部的引用文件切换到原来的文件
            DEBUG.P(0,this,"parse(1)");
        }
    }

    /** Resolve an identifier.
     * @param name      The identifier to resolve
     */
    public Symbol resolveIdent(String name) {
    	try {//我加上的
        DEBUG.P(this,"resolveIdent(1)");
        DEBUG.P("name="+name);

        if (name.equals(""))
            return syms.errSymbol;
        JavaFileObject prev = log.useSource(null);
        try {
            JCExpression tree = null;
            for (String s : name.split("\\.", -1)) {
                if (!SourceVersion.isIdentifier(s)) // TODO: check for keywords
                    return syms.errSymbol;
                tree = (tree == null) ? make.Ident(names.fromString(s))
                                      : make.Select(tree, names.fromString(s));
            }
            DEBUG.P("tree="+tree);
            JCCompilationUnit toplevel =
                make.TopLevel(List.<JCTree.JCAnnotation>nil(), null, List.<JCTree>nil());
            toplevel.packge = syms.unnamedPackage;
            return attr.attribIdent(tree, toplevel);
        } finally {
            log.useSource(prev);
        }
        
        }finally{//我加上的
        DEBUG.P(0,this,"resolveIdent(1)");
        }
    }

    /** Emit plain Java source for a class.
     *  @param env    The attribution environment of the outermost class
     *                containing this class.
     *  @param cdef   The class definition to be printed.
     */
    JavaFileObject printSource(Env<AttrContext> env, JCClassDecl cdef) throws IOException {
        JavaFileObject outFile
            = fileManager.getJavaFileForOutput(CLASS_OUTPUT,
					       cdef.sym.flatname.toString(),
					       JavaFileObject.Kind.SOURCE,
					       null);
        if (inputFiles.contains(outFile)) {
            log.error(cdef.pos(), "source.cant.overwrite.input.file", outFile);
            return null;
        } else {
            BufferedWriter out = new BufferedWriter(outFile.openWriter());
            try {
                new Pretty(out, true).printUnit(env.toplevel, cdef);
                if (verbose)
                    printVerbose("wrote.file", outFile);
            } finally {
                out.close();
            }
            return outFile;
        }
    }

    /** Generate code and emit a class file for a given class
     *  @param env    The attribution environment of the outermost class
     *                containing this class.
     *  @param cdef   The class definition from which code is generated.
     */
    JavaFileObject genCode(Env<AttrContext> env, JCClassDecl cdef) throws IOException {
        try {//我加上的
        DEBUG.P(this,"genCode(2)"); 
	    DEBUG.P("env="+env);
        DEBUG.P("cdef.sym="+cdef.sym);
        
        try {
            if (gen.genClass(env, cdef)) 
                return writer.writeClass(cdef.sym);
        } catch (ClassWriter.PoolOverflow ex) {
            log.error(cdef.pos(), "limit.pool");
        } catch (ClassWriter.StringOverflow ex) {
            log.error(cdef.pos(), "limit.string.overflow",
                      ex.value.substring(0, 20));
        } catch (CompletionFailure ex) {
            chk.completionError(cdef.pos(), ex);
        }
        return null;
        
        }finally{//我加上的
		DEBUG.P(1,this,"genCode(2)"); 
		}
    }

    /** Complete compiling a source file that has been accessed
     *  by the class file reader.
     *  @param c          The class the source file of which needs to be compiled.
     *  @param filename   The name of the source file.
     *  @param f          An input stream that reads the source file.
     */
    public void complete(ClassSymbol c) throws CompletionFailure {
    	try {//我加上的
        DEBUG.P(this,"complete(1)"); 
	    DEBUG.P("completionFailureName="+completionFailureName);
        DEBUG.P("c.fullname="+c.fullname);
        DEBUG.P("c.classfile="+c.classfile);
        
//      System.err.println("completing " + c);//DEBUG
        if (completionFailureName == c.fullname) {
            throw new CompletionFailure(c, "user-selected completion failure by class name");
        }
        JCCompilationUnit tree;
        JavaFileObject filename = c.classfile;
        JavaFileObject prev = log.useSource(filename);

        try {
            tree = parse(filename, filename.getCharContent(false));
        } catch (IOException e) {
            log.error("error.reading.file", filename, e);
            tree = make.TopLevel(List.<JCTree.JCAnnotation>nil(), null, List.<JCTree>nil());
        } finally {
            log.useSource(prev);
        }

        if (taskListener != null) {
            TaskEvent e = new TaskEvent(TaskEvent.Kind.ENTER, tree);
            taskListener.started(e);
        }

        enter.complete(List.of(tree), c);

        if (taskListener != null) {
            TaskEvent e = new TaskEvent(TaskEvent.Kind.ENTER, tree);
            taskListener.finished(e);
        }

        if (enter.getEnv(c) == null) {
            boolean isPkgInfo =
                tree.sourcefile.isNameCompatible("package-info",
                                                 JavaFileObject.Kind.SOURCE);
            if (isPkgInfo) {
                if (enter.getEnv(tree.packge) == null) {
                    String msg
                        = log.getLocalizedString("file.does.not.contain.package",
                                                 c.location());
                    throw new ClassReader.BadClassFile(c, filename, msg);
                }
            } else {
                throw new
                    ClassReader.BadClassFile(c, filename, log.
                                             getLocalizedString("file.doesnt.contain.class",
                                                                c.fullname));
            }
        }
        
        implicitSourceFilesRead = true;
        
        }finally{//我加上的
		DEBUG.P(1,this,"complete(1)"); 
		}
    }

    /** Track when the JavaCompiler has been used to compile something. */
    private boolean hasBeenUsed = false;
    private long start_msec = 0; //编译开始时间(单位:毫秒)
    public long elapsed_msec = 0;//编译结束时间(单位:毫秒)

    /** Track whether any errors occurred while parsing source text. */
    private boolean parseErrors = false;

    public void compile(List<JavaFileObject> sourceFileObject)
        throws Throwable {
        compile(sourceFileObject, List.<String>nil(), null);
    }

    /**
     * Main method: compile a list of files, return all compiled classes
     *
     * @param sourceFileObjects file objects to be compiled
     * @param classnames class names to process for annotations
     * @param processors user provided annotation processors to bypass
     * discovery, {@code null} means that no processors were provided
     */
    public void compile(List<JavaFileObject> sourceFileObjects,
                        List<String> classnames,
			Iterable<? extends Processor> processors)
        throws IOException // TODO: temp, from JavacProcessingEnvironment
    {
    	try {//我加上的
    	DEBUG.P(3);DEBUG.P(this,"compile(3) 一系列编译任务的起点......");
    	DEBUG.P("sourceFileObjects="+sourceFileObjects);
    	DEBUG.P("classnames="+classnames);
    	DEBUG.P("processors="+processors);
    	
    	//通过com.sun.tools.javac.api.JavacTaskImpl类的call()方法
    	//调用com.sun.tools.javac.main.Main类compile(4)方法间接
    	//调用到这里时，processors不为null；
    	//如果通过com.sun.tools.javac.main.Main类的compile(2)方法
    	//调用com.sun.tools.javac.main.Main类compile(4)方法间接
    	//调用到这里时，processors为null；
    	
        if (processors != null && processors.iterator().hasNext())
            explicitAnnotationProcessingRequested = true;
        // as a JavaCompiler can only be used once, throw an exception if
        // it has been used before.
        if (hasBeenUsed)
	    throw new AssertionError("attempt to reuse JavaCompiler");
        hasBeenUsed = true;

        start_msec = now();//记录开始编译时间
        try {
            initProcessAnnotations(processors);

            // These method calls must be chained to avoid memory leaks
            delegateCompiler = processAnnotations(enterTrees(stopIfError(parseFiles(sourceFileObjects))),
                                                  classnames);
            /*运行完上面后，已完成的编译任务有:
            1.词法分析(Scanner)
            2.语法分析(Parser)
            3.Enter与MemberEnter
            4.注释处理(JavacProcessingEnvironment)
            */

            delegateCompiler.compile2();
            /*运行完compile2()后，已完成的编译任务有:
            1.属性分析(Attr)
            2.数据流分析(Flow)
            3.Desugar
            4.生成字节码(Gen,ClassWriter)
            */
            
            /*
            上面所述内容只是对编译任务的一个粗略划分,具体细节还得
            分析到每一阶段时才能明了，另外对于错误处理是无处不在的，
            每一阶段都有特定的错误要查找。
            
            核心的内部数据结构在下面几个类中定义:
            com.sun.tools.javac.util.Name
            com.sun.tools.javac.tree.JCTree
            com.sun.tools.javac.code.Symbol
            com.sun.tools.javac.code.Type
            com.sun.tools.javac.code.Scope
            com.sun.tools.javac.jvm.Items
            com.sun.tools.javac.jvm.Code
            */
	    delegateCompiler.close();
	    elapsed_msec = delegateCompiler.elapsed_msec;
        } catch (Abort ex) { //类全限定名称:com.sun.tools.javac.util.Abort
            if (devVerbose)
                ex.printStackTrace();
        } 
        
        }finally{//我加上的
        DEBUG.P(0,this,"compile(3)");
    	}
    }

    /**
     * The phases following annotation processing: attribution,
     * desugar, and finally code generation.
     */
    private void compile2() {
    	DEBUG.P(this,"compile2() (字节码从这开始生成)");
    	DEBUG.P("compilePolicy="+compilePolicy);
    	if(todo.nonEmpty()) {
    		DEBUG.P("todo env lists:");
    		DEBUG.P("---------------------------------------------------");
    		for(Env<AttrContext> e:todo) DEBUG.P(""+e);
    	}
    	else DEBUG.P("todo=null");
    	DEBUG.P("");
    	
    	
        try {
            switch (compilePolicy) {
            case ATTR_ONLY:
                attribute(todo);
                break;

            case CHECK_ONLY:
                flow(attribute(todo));
                break;

            case SIMPLE:
                generate(desugar(flow(attribute(todo))));
                break;

            case BY_FILE:
                for (List<Env<AttrContext>> list : groupByFile(flow(attribute(todo))).values())
                    generate(desugar(list));
                break;

            case BY_TODO:
                while (todo.nonEmpty())
                    generate(desugar(flow(attribute(todo.next()))));
                break;

            default:
                assert false: "unknown compile policy";
            }
        } catch (Abort ex) {
            if (devVerbose)
                ex.printStackTrace();
        }

        if (verbose) {
	    elapsed_msec = elapsed(start_msec);;
            printVerbose("total", Long.toString(elapsed_msec));
		}

        reportDeferredDiagnostics();

        if (!log.hasDiagnosticListener()) {
            printCount("error", errorCount());
            printCount("warn", warningCount());
        }
        
        DEBUG.P(0,this,"compile2()");
    }

    private List<JCClassDecl> rootClasses;

    /**
     * Parses a list of files.
     */
   public List<JCCompilationUnit> parseFiles(List<JavaFileObject> fileObjects) throws IOException {
       try {//我加上的
       DEBUG.P(this,"parseFiles(1) (语法分析......)");
       
       if (errorCount() > 0)
       	   return List.nil();

        //parse all files
        ListBuffer<JCCompilationUnit> trees = lb();
        //lb()生成一个元素类型为JCCompilationUnit的空ListBuffer
        //在com.sun.tools.javac.util.ListBuffer类中定义;
        for (JavaFileObject fileObject : fileObjects)
            trees.append(parse(fileObject));
        return trees.toList();
        
        }finally{//我加上的
        DEBUG.P(2,this,"parseFiles(1)");
    	}
    }

    /**
     * Enter the symbols found in a list of parse trees.
     * As a side-effect, this puts elements on the "todo" list.
     * Also stores a list of all top level classes in rootClasses.
     */
    public List<JCCompilationUnit> enterTrees(List<JCCompilationUnit> roots) {
        DEBUG.P(this,"enterTrees(1)");
        
        //enter symbols for all files
        if (taskListener != null) {
            for (JCCompilationUnit unit: roots) {
                TaskEvent e = new TaskEvent(TaskEvent.Kind.ENTER, unit);
                taskListener.started(e);
            }
        }
        
        enter.main(roots);
        
        if (taskListener != null) {
            for (JCCompilationUnit unit: roots) {
                TaskEvent e = new TaskEvent(TaskEvent.Kind.ENTER, unit);
                taskListener.finished(e);
            }
        }

        //If generating source, remember the classes declared in
        //the original compilation units listed on the command line.
        DEBUG.P("sourceOutput="+sourceOutput+" stubOutput="+stubOutput);
        if (sourceOutput || stubOutput) {
            ListBuffer<JCClassDecl> cdefs = lb();
            for (JCCompilationUnit unit : roots) {
                for (List<JCTree> defs = unit.defs;
                     defs.nonEmpty();
                     defs = defs.tail) {
                    if (defs.head instanceof JCClassDecl)
                        cdefs.append((JCClassDecl)defs.head);
                }
            }
            rootClasses = cdefs.toList();
        }
        
        DEBUG.P(2,this,"enterTrees(1)");
        //DEBUG.P("enterTrees(1) stop",true);
        return roots;
    }

    /**
     * Set to true to enable skeleton annotation processing code.
     * Currently, we assume this variable will be replaced more
     * advanced logic to figure out if annotation processing is
     * needed.
     */
    boolean processAnnotations = false;

    /**
     * Object to handle annotation processing.
     */
    JavacProcessingEnvironment procEnvImpl = null;

    /**
     * Check if we should process annotations.
     * If so, and if no scanner is yet registered, then set up the DocCommentScanner
     * to catch doc comments, and set keepComments so the parser records them in
     * the compilation unit.
     *
     * @param processors user provided annotation processors to bypass
     * discovery, {@code null} means that no processors were provided
     */
    public void initProcessAnnotations(Iterable<? extends Processor> processors) {
    	DEBUG.P(this,"initProcessAnnotations(1)");
        // Process annotations if processing is not disabled and there
        // is at least one Processor available.
        Options options = Options.instance(context);
        DEBUG.P("options.get(\"-proc:none\")="+options.get("-proc:none"));
        DEBUG.P("JavacProcessingEnvironment procEnvImpl="+procEnvImpl);
        if (options.get("-proc:none") != null) {
            processAnnotations = false;
        } else if (procEnvImpl == null) {
        	/*
        	当在javac命令行中加了"-proc:none"选项时，
        	就表示不执行注释处理和/或编译，processAnnotations为false。
        	
        	
        	不加"-proc:none"选项时，会生成一个JavacProcessingEnvironment类
        	的实例，在生成实例的过程中，查看是否在命令行中指定了如下选项：
        	
        	-processor <class1>[,<class2>,<class3>...]要运行的注释处理程序的名称；绕过默认的搜索进程
        	-processorpath <路径>        指定查找注释处理程序的位置
        	
        	如果选项“-processor”没指定，就采用默认的注释处理程序
        	(注:默认的注释处理程序由sun.misc.Service类提供，
        	    sun.misc.Service类并不包含在javac1.7的源代码中，
        	    而是在rt.jar文件中，并没有开源)
        	    
        	如果选项“-processorpath”没指定，就以-classpath为准。
        	然后在上面指定的路径中搜索注释处理程序的名称，找到至少
        	一个注释处理程序的话就设processAnnotations为ture，否则
        	设processAnnotations为false。以后当调用processAnnotations()方法
        	时会根据processAnnotations的取值决定是否对源代码中的所有注释
        	进行处理和/或编译。
        	
        	最后还得注意一个细节:
        	在生成一个JavacProcessingEnvironment类的实例时，如果没有加-Xprint选
                项并且processors=null,已间接的调
        	用了com.sun.tools.javac.util.Paths类的lazy()方法，在此方法
        	中会把PLATFORM_CLASS_PATH,CLASS_PATH,SOURCE_PATH这三种路径
        	的值计算出来。
        	*/
            procEnvImpl = new JavacProcessingEnvironment(context, processors);
            processAnnotations = procEnvImpl.atLeastOneProcessor();
            
            DEBUG.P("processAnnotations="+processAnnotations);
            if (processAnnotations) {
                if (context.get(Scanner.Factory.scannerFactoryKey) == null)
                    DocCommentScanner.Factory.preRegister(context);
                    
                options.put("save-parameter-names", "save-parameter-names");
                reader.saveParameterNames = true;
                keepComments = true;

                if (taskListener != null)
                    taskListener.started(new TaskEvent(TaskEvent.Kind.ANNOTATION_PROCESSING));
            } else { // free resources
                procEnvImpl.close();
            }
        }
        DEBUG.P(0,this,"initProcessAnnotations(1)");
    }

    // TODO: called by JavacTaskImpl
    public JavaCompiler processAnnotations(List<JCCompilationUnit> roots) throws IOException {
    	try {//我加上的
        DEBUG.P(this,"processAnnotations(1)");
		
        return processAnnotations(roots, List.<String>nil());
        
        }finally{//我加上的
        DEBUG.P(0,this,"processAnnotations(1)");
        }
    }

    /**
     * Process any anotations found in the specifed compilation units.
     * @param roots a list of compilation units
     * @return an instance of the compiler in which to complete the compilation
     */
    public JavaCompiler processAnnotations(List<JCCompilationUnit> roots,
                                           List<String> classnames)
        throws IOException  { // TODO: see TEMP note in JavacProcessingEnvironment
        try {//我加上的
        DEBUG.P(this,"processAnnotations(2)");
        DEBUG.P("errorCount()="+errorCount());
        DEBUG.P("processAnnotations="+processAnnotations);
        DEBUG.P("classnames="+classnames);
		
        if (errorCount() != 0) {
            // Errors were encountered.  If todo is empty, then the
            // encountered errors were parse errors.  Otherwise, the
            // errors were found during the enter phase which should
            // be ignored when processing annotations.

            if (todo.isEmpty())
                return this;
        }

        // ASSERT: processAnnotations and procEnvImpl should have been set up by
        // by initProcessAnnotations

        // NOTE: The !classnames.isEmpty() checks should be refactored to Main.

        if (!processAnnotations) {
	    // If there are no annotation processors present, and
	    // annotation processing is to occur with compilation,
	    // emit a warning.
	    Options options = Options.instance(context);
	    if (options.get("-proc:only") != null) {
	    //警告：在未请求编译的情况下进行注释处理，但未找到处理程序。
		log.warning("proc.proc-only.requested.no.procs");
		todo.clear();
	    }
            // If not processing annotations, classnames must be empty
            if (!classnames.isEmpty()) {
                log.error("proc.no.explicit.annotation.processing.requested",
                          classnames);
            }
            return this; // continue regular compilation
        }
        
        try {
            DEBUG.P("classnames.isEmpty()="+classnames.isEmpty());
            
            List<ClassSymbol> classSymbols = List.nil();
            List<PackageSymbol> pckSymbols = List.nil();
            if (!classnames.isEmpty()) {
                 // Check for explicit request for annotation
                 // processing
                if (!explicitAnnotationProcessingRequested()) {
                    log.error("proc.no.explicit.annotation.processing.requested",
                              classnames);
                    return this; // TODO: Will this halt compilation?
                } else {
                    boolean errors = false;
                    for (String nameStr : classnames) {
                        Symbol sym = resolveIdent(nameStr);
                        DEBUG.P("sym="+sym);
                        if(sym!=null) {
                            DEBUG.P("sym.kind="+Kinds.toString(sym.kind));
                            DEBUG.P("processPcks="+processPcks);
                        }
                        //加“-XDprocess.packages”选项时processPcks=true
                        if (sym == null || (sym.kind == Kinds.PCK && !processPcks)) {
                            log.error("proc.cant.find.class", nameStr);
                            errors = true;
                            continue;
                        }
                        try {
                            if (sym.kind == Kinds.PCK)
                                sym.complete();

                            DEBUG.P("sym.exists()="+sym.exists());
                            if (sym.exists()) {
                                Name name = names.fromString(nameStr);
                                if (sym.kind == Kinds.PCK)
                                    pckSymbols = pckSymbols.prepend((PackageSymbol)sym);
                                else
                                    classSymbols = classSymbols.prepend((ClassSymbol)sym);
                                continue;
                            }
                            assert sym.kind == Kinds.PCK;
                            log.warning("proc.package.does.not.exist", nameStr);
                            pckSymbols = pckSymbols.prepend((PackageSymbol)sym);
                        } catch (CompletionFailure e) {
                            log.error("proc.cant.find.class", nameStr);
                            errors = true;
                            continue;
                        }
                    }
                    if (errors)
                        return this;
                }
            }
            JavaCompiler c = procEnvImpl.doProcessing(context, roots, classSymbols, pckSymbols);
            if (c != this) 
                annotationProcessingOccurred = c.annotationProcessingOccurred = true;
            return c;
        } catch (CompletionFailure ex) {
	    log.error("cant.access", ex.sym, ex.errmsg);
            return this;
            
        }
        
        }finally{//我加上的
        DEBUG.P("annotationProcessingOccurred="+annotationProcessingOccurred);
        DEBUG.P(3,this,"processAnnotations(2)");
        }
    }

    boolean explicitAnnotationProcessingRequested() {
        Options options = Options.instance(context);
        return
            explicitAnnotationProcessingRequested ||
            options.get("-processor") != null ||
            options.get("-processorpath") != null ||
            options.get("-proc:only") != null ||
            options.get("-Xprint") != null;
    }

    /**
     * Attribute a list of parse trees, such as found on the "todo" list.
     * Note that attributing classes may cause additional files to be
     * parsed and entered via the SourceCompleter.
     * Attribution of the entries in the list does not stop if any errors occur.
     * @returns a list of environments for attributd classes.
     */
    public List<Env<AttrContext>> attribute(ListBuffer<Env<AttrContext>> envs) {
        ListBuffer<Env<AttrContext>> results = lb();
        while (envs.nonEmpty())
            results.append(attribute(envs.next()));
        return results.toList();
    }

    /**
     * Attribute a parse tree.
     * @returns the attributed parse tree
     */
    public Env<AttrContext> attribute(Env<AttrContext> env) {
    	DEBUG.P(this,"attribute(Env<AttrContext> env)");
    	DEBUG.P("attribute(前) env="+env);
    	//verboseCompilePolicy=true; verbose=true;//我加上的，调试用途
    	
    	
        if (verboseCompilePolicy)
            log.printLines(log.noticeWriter, "[attribute " + env.enclClass.sym + "]");
        if (verbose)
            printVerbose("checking.attribution", env.enclClass.sym);

        if (taskListener != null) {
            TaskEvent e = new TaskEvent(TaskEvent.Kind.ANALYZE, env.toplevel, env.enclClass.sym);
            taskListener.started(e);
        }

        JavaFileObject prev = log.useSource(
                                  env.enclClass.sym.sourcefile != null ?
                                  env.enclClass.sym.sourcefile :
                                  env.toplevel.sourcefile);
        try {
            attr.attribClass(env.tree.pos(), env.enclClass.sym);
        }
        finally {
            log.useSource(prev);
        }

        //运行到这里，还没开始字节码翻译
        //DEBUG.P("JCTree.JCCompilationUnit toplevel(属性分析后):"+env.toplevel);
        DEBUG.P("attribute(后) env="+env);
        DEBUG.P(3,this,"attribute(Env<AttrContext> env)");
        return env;
    }

    /**
     * Perform dataflow checks on attributed parse trees.
     * These include checks for definite assignment and unreachable statements.
     * If any errors occur, an empty list will be returned.
     * @returns the list of attributed parse trees
     */
    public List<Env<AttrContext>> flow(List<Env<AttrContext>> envs) {
        ListBuffer<Env<AttrContext>> results = lb();
        for (List<Env<AttrContext>> l = envs; l.nonEmpty(); l = l.tail) {
            flow(l.head, results);
        }
        return stopIfError(results);
    }

    /**
     * Perform dataflow checks on an attributed parse tree.
     */
    public List<Env<AttrContext>> flow(Env<AttrContext> env) {
    	try {//我加上的
		DEBUG.P(this,"flow(1)");

        ListBuffer<Env<AttrContext>> results = lb();
        flow(env, results);
        return stopIfError(results);
        
        }finally{//我加上的
		DEBUG.P(0,this,"flow(1)");
		}
    }

    /**
     * Perform dataflow checks on an attributed parse tree.
     */
    protected void flow(Env<AttrContext> env, ListBuffer<Env<AttrContext>> results) {
        try {
        	DEBUG.P(this,"flow(2)");
			DEBUG.P("env="+env);
			
            if (errorCount() > 0)
                return;

            if (relax || deferredSugar.contains(env)) {
                results.append(env);
                return;
            }

            if (verboseCompilePolicy)
                log.printLines(log.noticeWriter, "[flow " + env.enclClass.sym + "]");
            JavaFileObject prev = log.useSource(
                                                env.enclClass.sym.sourcefile != null ?
                                                env.enclClass.sym.sourcefile :
                                                env.toplevel.sourcefile);
            try {
                make.at(Position.FIRSTPOS);
                TreeMaker localMake = make.forToplevel(env.toplevel);
                flow.analyzeTree(env.tree, localMake);

                if (errorCount() > 0)
                    return;

                results.append(env);
            }
            finally {
                log.useSource(prev);
            }
        }
        finally {
            if (taskListener != null) {
                TaskEvent e = new TaskEvent(TaskEvent.Kind.ANALYZE, env.toplevel, env.enclClass.sym);
                taskListener.finished(e);
            }
            
            DEBUG.P(0,this,"flow(2)");
        }
    }

    /**
     * Prepare attributed parse trees, in conjunction with their attribution contexts,
     * for source or code generation.
     * If any errors occur, an empty list will be returned.
     * @returns a list containing the classes to be generated
     */
    public List<Pair<Env<AttrContext>, JCClassDecl>> desugar(List<Env<AttrContext>> envs) {
        try {//我加上的
		DEBUG.P(this,"desugar(1)");

        ListBuffer<Pair<Env<AttrContext>, JCClassDecl>> results = lb();
        for (List<Env<AttrContext>> l = envs; l.nonEmpty(); l = l.tail)
            desugar(l.head, results);
        return stopIfError(results);
        
        }finally{//我加上的
		DEBUG.P(1,this,"desugar(1)");
		}
    }

    /**
     * Prepare attributed parse trees, in conjunction with their attribution contexts,
     * for source or code generation. If the file was not listed on the command line,
     * the current implicitSourcePolicy is taken into account.
     * The preparation stops as soon as an error is found.
     */
    protected void desugar(Env<AttrContext> env, ListBuffer<Pair<Env<AttrContext>, JCClassDecl>> results) {
        try {//我加上的
		DEBUG.P(this,"desugar(2)");
		DEBUG.P("env="+env);
		DEBUG.P("errorCount()="+errorCount());
		DEBUG.P("implicitSourcePolicy="+implicitSourcePolicy);
		
        if (errorCount() > 0)
            return;
        
        if (implicitSourcePolicy == ImplicitSourcePolicy.NONE
                && !inputFiles.contains(env.toplevel.sourcefile)) {
            return;
        }
        
        boolean myBoolean=desugarLater(env);//我加上的
        DEBUG.P("myBoolean="+myBoolean);//我加上的
        if (myBoolean) {//我加上的
        //if (desugarLater(env)) {
            if (verboseCompilePolicy)
                log.printLines(log.noticeWriter, "[defer " + env.enclClass.sym + "]");
            todo.append(env);
            return;
        }
        DEBUG.P("deferredSugar1="+deferredSugar);
        deferredSugar.remove(env);
        DEBUG.P("deferredSugar2="+deferredSugar);

        if (verboseCompilePolicy)
            log.printLines(log.noticeWriter, "[desugar " + env.enclClass.sym + "]");

        JavaFileObject prev = log.useSource(env.enclClass.sym.sourcefile != null ?
                                  env.enclClass.sym.sourcefile :
                                  env.toplevel.sourcefile);
        try {
            //save tree prior to rewriting
            JCTree untranslated = env.tree;

            make.at(Position.FIRSTPOS);
            TreeMaker localMake = make.forToplevel(env.toplevel);
            DEBUG.P("stubOutput="+stubOutput);
            DEBUG.P("sourceOutput="+sourceOutput);
            DEBUG.P("printFlat="+printFlat);
            DEBUG.P("env.tree instanceof JCCompilationUnit="+(env.tree instanceof JCCompilationUnit));

            if (env.tree instanceof JCCompilationUnit) {
                if (!(stubOutput || sourceOutput || printFlat)) {
                    List<JCTree> pdef = lower.translateTopLevelClass(env, env.tree, localMake);
                    if (pdef.head != null) {
                        assert pdef.tail.isEmpty();
                        results.append(new Pair<Env<AttrContext>, JCClassDecl>(env, (JCClassDecl)pdef.head));
                    }
                }
                return;
            }

            if (stubOutput) {
                //emit stub Java source file, only for compilation
                //units enumerated explicitly on the command line
                JCClassDecl cdef = (JCClassDecl)env.tree;
                if (untranslated instanceof JCClassDecl &&
                    rootClasses.contains((JCClassDecl)untranslated) &&
                    ((cdef.mods.flags & (Flags.PROTECTED|Flags.PUBLIC)) != 0 ||
                     cdef.sym.packge().getQualifiedName() == names.java_lang)) {
                    results.append(new Pair<Env<AttrContext>, JCClassDecl>(env, removeMethodBodies(cdef)));
                }
                return;
            }

            env.tree = transTypes.translateTopLevelClass(env.tree, localMake);

            if (errorCount() != 0)
                return;

            if (sourceOutput) {
                //emit standard Java source file, only for compilation
                //units enumerated explicitly on the command line
                JCClassDecl cdef = (JCClassDecl)env.tree;
                if (untranslated instanceof JCClassDecl &&
                    rootClasses.contains((JCClassDecl)untranslated)) {
                    results.append(new Pair<Env<AttrContext>, JCClassDecl>(env, cdef));
                }
                return;
            }

            //translate out inner classes
            List<JCTree> cdefs = lower.translateTopLevelClass(env, env.tree, localMake);

            if (errorCount() != 0)
                return;

            //generate code for each class
            for (List<JCTree> l = cdefs; l.nonEmpty(); l = l.tail) {
                JCClassDecl cdef = (JCClassDecl)l.head;
                results.append(new Pair<Env<AttrContext>, JCClassDecl>(env, cdef));
            }
        }
        finally {
            log.useSource(prev);
        }
        
        }finally{//我加上的
		DEBUG.P(1,this,"desugar(2)");
		}
    }

    /**
     * Determine if a class needs to be desugared later.  As erasure
     * (TransTypes) destroys information needed in flow analysis, we
     * need to ensure that supertypes are translated before derived
     * types are translated.
     */
    public boolean desugarLater(final Env<AttrContext> env) {
    	try {//我加上的
		DEBUG.P(this,"desugarLater(1)");
		DEBUG.P("env="+env);
		DEBUG.P("compilePolicy="+compilePolicy);
		
        if (compilePolicy == CompilePolicy.BY_FILE)
            return false;
        if (!devVerbose && deferredSugar.contains(env))
            // guarantee that compiler terminates
            return false;
        class ScanNested extends TreeScanner {
            Set<Symbol> externalSupers = new HashSet<Symbol>();
            public void visitClassDef(JCClassDecl node) {
            	DEBUG.P(this,"visitClassDef(1)");
            	
                Type st = types.supertype(node.sym.type);
                DEBUG.P("st.tag="+TypeTags.toString(st.tag));
                if (st.tag == TypeTags.CLASS) {
                    ClassSymbol c = st.tsym.outermostClass();
                    DEBUG.P("c="+c);
                    Env<AttrContext> stEnv = enter.getEnv(c);
                    DEBUG.P("stEnv="+stEnv);
                    if (stEnv != null && env != stEnv)
                        externalSupers.add(st.tsym);
                }
                super.visitClassDef(node);
                
                DEBUG.P(0,this,"visitClassDef(1)");
            }
        }
        ScanNested scanner = new ScanNested();
        scanner.scan(env.tree);
        if (scanner.externalSupers.isEmpty())
            return false;
        if (!deferredSugar.add(env) && devVerbose) {
            throw new AssertionError(env.enclClass.sym + " was deferred, " +
                                     "second time has these external super types " +
                                     scanner.externalSupers);
        }
        return true;
        
        }finally{//我加上的
		DEBUG.P(1,this,"desugarLater(1)");
		}
    }

    /** Generates the source or class file for a list of classes.
     * The decision to generate a source file or a class file is
     * based upon the compiler's options.
     * Generation stops if an error occurs while writing files.
     */
    public void generate(List<Pair<Env<AttrContext>, JCClassDecl>> list) {
        DEBUG.P(this,"generate(1)");
		
        generate(list, null);
        
        DEBUG.P(1,this,"generate(1)");
    }
    
    public void generate(List<Pair<Env<AttrContext>, JCClassDecl>> list, ListBuffer<JavaFileObject> results) {
        try {//我加上的
        DEBUG.P(this,"generate(2)");
        
        boolean usePrintSource = (stubOutput || sourceOutput || printFlat);
        
        DEBUG.P("usePrintSource="+usePrintSource);
        DEBUG.P("list.size()="+list.size());

        for (List<Pair<Env<AttrContext>, JCClassDecl>> l = list; l.nonEmpty(); l = l.tail) {
            Pair<Env<AttrContext>, JCClassDecl> x = l.head;
            Env<AttrContext> env = x.fst;
            JCClassDecl cdef = x.snd;
            
            DEBUG.P("env="+env);
            DEBUG.P("cdef.sym="+cdef.sym);

            if (verboseCompilePolicy) {
                log.printLines(log.noticeWriter, "[generate "
                               + (usePrintSource ? " source" : "code")
                               + " " + env.enclClass.sym + "]");
            }

            if (taskListener != null) {
                TaskEvent e = new TaskEvent(TaskEvent.Kind.GENERATE, env.toplevel, cdef.sym);
                taskListener.started(e);
            }

            JavaFileObject prev = log.useSource(env.enclClass.sym.sourcefile != null ?
                                      env.enclClass.sym.sourcefile :
                                      env.toplevel.sourcefile);
            try {
                JavaFileObject file;
                if (usePrintSource)
                    file = printSource(env, cdef);
                else
                    file = genCode(env, cdef);
                if (results != null && file != null)
                    results.append(file);
            } catch (IOException ex) {
                log.error(cdef.pos(), "class.cant.write",
                          cdef.sym, ex.getMessage());
                return;
            } finally {
                log.useSource(prev);
            }

            if (taskListener != null) {
                TaskEvent e = new TaskEvent(TaskEvent.Kind.GENERATE, env.toplevel, cdef.sym);
                taskListener.finished(e);
            }
        }
        
        }finally{//我加上的
        DEBUG.P(1,this,"generate(2)");
    	}
    }

        // where
        Map<JCCompilationUnit, List<Env<AttrContext>>> groupByFile(List<Env<AttrContext>> list) {
            // use a LinkedHashMap to preserve the order of the original list as much as possible
            Map<JCCompilationUnit, List<Env<AttrContext>>> map = new LinkedHashMap<JCCompilationUnit, List<Env<AttrContext>>>();
            Set<JCCompilationUnit> fixupSet = new HashSet<JCTree.JCCompilationUnit>();
            for (List<Env<AttrContext>> l = list; l.nonEmpty(); l = l.tail) {
                Env<AttrContext> env = l.head;
                List<Env<AttrContext>> sublist = map.get(env.toplevel);
                if (sublist == null)
                    sublist = List.of(env);
                else {
                    // this builds the list for the file in reverse order, so make a note
                    // to reverse the list before returning.
                    sublist = sublist.prepend(env);
                    fixupSet.add(env.toplevel);
                }
                map.put(env.toplevel, sublist);
            }
            // fixup any lists that need reversing back to the correct order
            for (JCTree.JCCompilationUnit tree: fixupSet)
                map.put(tree, map.get(tree).reverse());
            return map;
        }

        JCClassDecl removeMethodBodies(JCClassDecl cdef) {
            final boolean isInterface = (cdef.mods.flags & Flags.INTERFACE) != 0;
            class MethodBodyRemover extends TreeTranslator {
                public void visitMethodDef(JCMethodDecl tree) {
                    tree.mods.flags &= ~Flags.SYNCHRONIZED;
                    for (JCVariableDecl vd : tree.params)
                        vd.mods.flags &= ~Flags.FINAL;
                    tree.body = null;
                    super.visitMethodDef(tree);
                }
                public void visitVarDef(JCVariableDecl tree) {
                    if (tree.init != null && tree.init.type.constValue() == null)
                        tree.init = null;
                    super.visitVarDef(tree);
                }
                public void visitClassDef(JCClassDecl tree) {
                    ListBuffer<JCTree> newdefs = lb();
                    for (List<JCTree> it = tree.defs; it.tail != null; it = it.tail) {
                        JCTree t = it.head;
                        switch (t.tag) {
                        case JCTree.CLASSDEF:
                            if (isInterface ||
                                (((JCClassDecl) t).mods.flags & (Flags.PROTECTED|Flags.PUBLIC)) != 0 ||
                                (((JCClassDecl) t).mods.flags & (Flags.PRIVATE)) == 0 && ((JCClassDecl) t).sym.packge().getQualifiedName() == names.java_lang)
                                newdefs.append(t);
                            break;
                        case JCTree.METHODDEF:
                            if (isInterface ||
                                (((JCMethodDecl) t).mods.flags & (Flags.PROTECTED|Flags.PUBLIC)) != 0 ||
                                ((JCMethodDecl) t).sym.name == names.init ||
                                (((JCMethodDecl) t).mods.flags & (Flags.PRIVATE)) == 0 && ((JCMethodDecl) t).sym.packge().getQualifiedName() == names.java_lang)
                                newdefs.append(t);
                            break;
                        case JCTree.VARDEF:
                            if (isInterface || (((JCVariableDecl) t).mods.flags & (Flags.PROTECTED|Flags.PUBLIC)) != 0 ||
                                (((JCVariableDecl) t).mods.flags & (Flags.PRIVATE)) == 0 && ((JCVariableDecl) t).sym.packge().getQualifiedName() == names.java_lang)
                                newdefs.append(t);
                            break;
                        default:
                            break;
                        }
                    }
                    tree.defs = newdefs.toList();
                    super.visitClassDef(tree);
                }
            }
            MethodBodyRemover r = new MethodBodyRemover();
            return r.translate(cdef);
        }
        
    public void reportDeferredDiagnostics() {
        if (annotationProcessingOccurred 
                && implicitSourceFilesRead 
                && implicitSourcePolicy == ImplicitSourcePolicy.UNSET) {
            if (explicitAnnotationProcessingRequested())
                log.warning("proc.use.implicit");
            else
                log.warning("proc.use.proc.or.implicit");
        }
        chk.reportDeferredDiagnostics();
    }

    /** Close the compiler, flushing the logs
     */
    public void close() {
        close(true);
    }

    private void close(boolean disposeNames) {
        rootClasses = null;
        reader = null;
        make = null;
        writer = null;
        enter = null;
	if (todo != null)
	    todo.clear();
        todo = null;
        parserFactory = null;
        syms = null;
        source = null;
        attr = null;
        chk = null;
        gen = null;
        flow = null;
        transTypes = null;
        lower = null;
        annotate = null;
        types = null;

        log.flush();
        try {
            fileManager.flush();
        } catch (IOException e) {
            throw new Abort(e);
        } finally {
            if (names != null && disposeNames)
                names.dispose();
            names = null;
        }
    }

    /** Output for "-verbose" option.
     *  @param key The key to look up the correct internationalized string.
     *  @param arg An argument for substitution into the output string.
     */
    protected void printVerbose(String key, Object arg) {
        Log.printLines(log.noticeWriter, log.getLocalizedString("verbose." + key, arg));
    }

    /** Print numbers of errors and warnings.
     */
    protected void printCount(String kind, int count) {
        if (count != 0) {
            String text;
            if (count == 1)
                text = log.getLocalizedString("count." + kind, String.valueOf(count));
            else
                text = log.getLocalizedString("count." + kind + ".plural", String.valueOf(count));
            Log.printLines(log.errWriter, text);
            log.errWriter.flush();
        }
    }

    private static long now() {
	return System.currentTimeMillis();
    }

    private static long elapsed(long then) {
	return now() - then;
    }

    public void initRound(JavaCompiler prev) {
	keepComments = prev.keepComments;
	start_msec = prev.start_msec;
	hasBeenUsed = true;
    }

    public static void enableLogging() {
        Logger logger = Logger.getLogger(com.sun.tools.javac.Main.class.getPackage().getName());
        logger.setLevel(Level.ALL);
        for (Handler h : logger.getParent().getHandlers()) {
            h.setLevel(Level.ALL);
       }

    }
}
