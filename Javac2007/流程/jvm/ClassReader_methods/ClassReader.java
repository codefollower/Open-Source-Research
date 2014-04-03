package com.sun.tools.javac.jvm;

import java.io.*;
import java.net.URI;
import java.nio.CharBuffer;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.lang.model.SourceVersion;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileManager;
import javax.tools.StandardJavaFileManager;

import com.sun.tools.javac.comp.Annotate;
import com.sun.tools.javac.code.*;
import com.sun.tools.javac.code.Type.*;
import com.sun.tools.javac.code.Symbol.*;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.util.*;
import com.sun.tools.javac.util.List;

import static com.sun.tools.javac.code.Flags.*;
import static com.sun.tools.javac.code.Kinds.*;
import static com.sun.tools.javac.code.TypeTags.*;
import com.sun.tools.javac.jvm.ClassFile.NameAndType;
import javax.tools.JavaFileManager.Location;
import static javax.tools.StandardLocation.*;

/** This class provides operations to read a classfile into an internal
 *  representation. The internal representation is anchored in a
 *  ClassSymbol which contains in its scope symbol representations
 *  for all other definitions in the classfile. Top-level Classes themselves
 *  appear as members of the scopes of PackageSymbols.
 *
 *  <p><b>This is NOT part of any API supported by Sun Microsystems.  If
 *  you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 */
@Version("@(#)ClassReader.java	1.138 07/03/21")
public class ClassReader extends ClassFile implements Completer {
	private static my.Debug DEBUG=new my.Debug(my.Debug.ClassReader);//我加上的
	
    /** The context key for the class reader. */
    protected static final Context.Key<ClassReader> classReaderKey =
        new Context.Key<ClassReader>();

    Annotate annotate;

    /** Switch: verbose output.
     */
    boolean verbose;

    /** Switch: check class file for correct minor version, unrecognized
     *  attributes.
     */
    boolean checkClassFile;

    /** Switch: read constant pool and code sections. This switch is initially
     *  set to false but can be turned on from outside.
     */
    public boolean readAllOfClassFile = false;

    /** Switch: read GJ signature information.
     */
    boolean allowGenerics;

    /** Switch: read varargs attribute.
     */
    boolean allowVarargs;

    /** Switch: allow annotations.
     */
    boolean allowAnnotations;

    /** Switch: preserve parameter names from the variable table.
     */
    public boolean saveParameterNames;

    /**
     * Switch: cache completion failures unless -XDdev is used
     */
    private boolean cacheCompletionFailure;
    
    /**
     * Switch: prefer source files instead of newer when both source 
     * and class are available
     **/
    public boolean preferSource;

    /** The log to use for verbose output
     */
    final Log log;

    /** The symbol table. */
    Symtab syms;

    Types types;

    /** The name table. */
    final Name.Table names;

    /** Force a completion failure on this name
     */
    final Name completionFailureName;

    /** Access to files
     */
    private final JavaFileManager fileManager;

    /** Can be reassigned from outside:
     *  the completer to be used for ".java" files. If this remains unassigned
     *  ".java" files will not be loaded.
     */
    public SourceCompleter sourceCompleter = null;//在JavaCompiler(final Context context)赋值

    /** A hashtable containing the encountered top-level and member classes,
     *  indexed by flat names. The table does not contain local classes.
     */
    private Map<Name,ClassSymbol> classes;

    /** A hashtable containing the encountered packages.
     */
    private Map<Name, PackageSymbol> packages;

    /** The current scope where type variables are entered.
     */
    protected Scope typevars;

    /** The path name of the class file currently being read.
     */
    protected JavaFileObject currentClassFile = null;

    /** The class or method currently being read.
     */
    protected Symbol currentOwner = null;

    /** The buffer containing the currently read class file.
     */
    byte[] buf = new byte[0x0fff0];

    /** The current input pointer.
     */
    int bp;

    /** The objects of the constant pool.
     */
    Object[] poolObj;

    /** For every constant pool entry, an index into buf where the
     *  defining section of the entry is found.
     */
    int[] poolIdx;

    /** Get the ClassReader instance for this invocation. */
    public static ClassReader instance(Context context) {
        ClassReader instance = context.get(classReaderKey);
        if (instance == null)
            instance = new ClassReader(context, true);
        return instance;
    }

    /** Construct a new class reader, optionally treated as the
     *  definitive classreader for this invocation.
     */
    protected ClassReader(Context context, boolean definitive) {
    	DEBUG.P(this,"ClassReader(2)");
        if (definitive) context.put(classReaderKey, this);

        names = Name.Table.instance(context);
        syms = Symtab.instance(context);
        types = Types.instance(context);
        fileManager = context.get(JavaFileManager.class);
        if (fileManager == null)
            throw new AssertionError("FileManager initialization error");

        init(syms, definitive);
        log = Log.instance(context);

        Options options = Options.instance(context);
        annotate = Annotate.instance(context);
        verbose        = options.get("-verbose")        != null;
        checkClassFile = options.get("-checkclassfile") != null;
        Source source = Source.instance(context);
        allowGenerics    = source.allowGenerics();
        allowVarargs     = source.allowVarargs();
        allowAnnotations = source.allowAnnotations();
        saveParameterNames = options.get("save-parameter-names") != null;
        cacheCompletionFailure = options.get("dev") == null;
        preferSource = "source".equals(options.get("-Xprefer"));

        completionFailureName =
            (options.get("failcomplete") != null)
            ? names.fromString(options.get("failcomplete"))
            : null;

        typevars = new Scope(syms.noSymbol);
        
        DEBUG.P(1,this,"ClassReader(2)");
    }