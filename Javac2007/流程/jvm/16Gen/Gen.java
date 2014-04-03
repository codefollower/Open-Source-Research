package com.sun.tools.javac.jvm;
import java.util.*;

import com.sun.tools.javac.util.*;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.code.*;
import com.sun.tools.javac.comp.*;
import com.sun.tools.javac.tree.*;

import com.sun.tools.javac.code.Symbol.*;
import com.sun.tools.javac.code.Type.*;
import com.sun.tools.javac.jvm.Code.*;
import com.sun.tools.javac.jvm.Items.*;
import com.sun.tools.javac.tree.JCTree.*;

import static com.sun.tools.javac.code.Flags.*;
import static com.sun.tools.javac.code.Kinds.*;
import static com.sun.tools.javac.code.TypeTags.*;
import static com.sun.tools.javac.jvm.ByteCodes.*;
import static com.sun.tools.javac.jvm.CRTFlags.*;

/** This pass maps flat Java (i.e. without inner classes) to bytecodes.
 *
 *  <p><b>This is NOT part of any API supported by Sun Microsystems.  If
 *  you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 */
@Version("@(#)Gen.java	1.148 07/03/21")
public class Gen extends JCTree.Visitor {
    private static my.Debug DEBUG=new my.Debug(my.Debug.Gen);//我加上的
	
    protected static final Context.Key<Gen> genKey =
	new Context.Key<Gen>();

    private final Log log;
    private final Symtab syms;
    private final Check chk;
    private final Resolve rs;
    private final TreeMaker make;
    private final Name.Table names;
    private final Target target;
    private final Type stringBufferType;
    private final Map<Type,Symbol> stringBufferAppend;
    private Name accessDollar;
    private final Types types;

    /** Switch: GJ mode?
     */
    private final boolean allowGenerics;

    /** Set when Miranda method stubs are to be generated. */
    private final boolean generateIproxies;

    /** Format of stackmap tables to be generated. */
    private final Code.StackMapFormat stackMap;
    
    /** A type that serves as the expected type for all method expressions.
     */
    private final Type methodType;

    public static Gen instance(Context context) {
		Gen instance = context.get(genKey);
		if (instance == null)
			instance = new Gen(context);
		return instance;
    }

    protected Gen(Context context) {
		DEBUG.P(this,"Gen(1)");
		context.put(genKey, this);

		names = Name.Table.instance(context);
		log = Log.instance(context);
		syms = Symtab.instance(context);
		chk = Check.instance(context);
		rs = Resolve.instance(context);
		make = TreeMaker.instance(context);
		target = Target.instance(context);
		types = Types.instance(context);
		methodType = new MethodType(null, null, null, syms.methodClass);
		allowGenerics = Source.instance(context).allowGenerics();
		stringBufferType = target.useStringBuilder()
			? syms.stringBuilderType
			: syms.stringBufferType;
		stringBufferAppend = new HashMap<Type,Symbol>();
		accessDollar = names.
			fromString("access" + target.syntheticNameChar());

		Options options = Options.instance(context);
		lineDebugInfo =
			options.get("-g:") == null ||
			options.get("-g:lines") != null;
		varDebugInfo =
			options.get("-g:") == null
			? options.get("-g") != null
			: options.get("-g:vars") != null;
		genCrt = options.get("-Xjcov") != null;
		debugCode = options.get("debugcode") != null;

		generateIproxies =
			target.requiresIproxy() ||
			options.get("miranda") != null;

		if (target.generateStackMapTable()) {
			// ignore cldc because we cannot have both stackmap formats
			this.stackMap = StackMapFormat.JSR202;
		} else {	    
			if (target.generateCLDCStackmap()) {
				this.stackMap = StackMapFormat.CLDC;
			} else {
				this.stackMap = StackMapFormat.NONE;
			}
		}
		
		// by default, avoid jsr's for simple finalizers
		int setjsrlimit = 50;
		String jsrlimitString = options.get("jsrlimit");
		if (jsrlimitString != null) {
			try {
				setjsrlimit = Integer.parseInt(jsrlimitString);
			} catch (NumberFormatException ex) {
				// ignore ill-formed numbers for jsrlimit
			}
		}
		this.jsrlimit = setjsrlimit;

		this.useJsrLocally = false; // reset in visitTry
		DEBUG.P(0,this,"Gen(1)");
    }

    /** Switches
     */
    private final boolean lineDebugInfo;
    private final boolean varDebugInfo;
    private final boolean genCrt;
    private final boolean debugCode;

    /** Default limit of (approximate) size of finalizer to inline.
     *  Zero means always use jsr.  100 or greater means never use
     *  jsr.
     */
    private final int jsrlimit;
    
    /** True if jsr is used.
     */
    private boolean useJsrLocally;
    
    /* Constant pool, reset by genClass.
     */
    private Pool pool = new Pool();

    /** Code buffer, set by genMethod.
     */
    private Code code;

    /** Items structure, set by genMethod.
     */
    private Items items;

    /** Environment for symbol lookup, set by genClass
     */
    private Env<AttrContext> attrEnv;

    /** The top level tree.
     */
    private JCCompilationUnit toplevel;

    /** The number of code-gen errors in this class.
     */
    private int nerrs = 0;

    /** A hash table mapping syntax trees to their ending source positions.
     */
    private Map<JCTree, Integer> endPositions;