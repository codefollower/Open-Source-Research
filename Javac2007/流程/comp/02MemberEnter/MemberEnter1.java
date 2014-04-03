package com.sun.tools.javac.comp;

import java.util.*;
import java.util.Set;
import javax.tools.JavaFileObject;

import com.sun.tools.javac.code.*;
import com.sun.tools.javac.jvm.*;
import com.sun.tools.javac.tree.*;
import com.sun.tools.javac.util.*;
import com.sun.tools.javac.util.List;

import com.sun.tools.javac.code.Type.*;
import com.sun.tools.javac.code.Symbol.*;
import com.sun.tools.javac.tree.JCTree.*;

import static com.sun.tools.javac.code.Flags.*;
import static com.sun.tools.javac.code.Kinds.*;
import static com.sun.tools.javac.code.TypeTags.*;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;

/** This is the second phase of Enter, in which classes are completed
 *  by entering their members into the class scope using
 *  MemberEnter.complete().  See Enter for an overview.
 *
 *  <p><b>This is NOT part of any API supported by Sun Microsystems.  If
 *  you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 */
@Version("@(#)MemberEnter.java	1.67 07/03/21")
public class MemberEnter extends JCTree.Visitor implements Completer {
	private static my.Debug DEBUG=new my.Debug(my.Debug.MemberEnter);//我加上的
	
    protected static final Context.Key<MemberEnter> memberEnterKey =
        new Context.Key<MemberEnter>();

    /** A switch to determine whether we check for package/class conflicts
     */
    final static boolean checkClash = true;

    private final Name.Table names;
    private final Enter enter;
    private final Log log;
    private final Check chk;
    private final Attr attr;
    private final Symtab syms;
    private final TreeMaker make;
    private final ClassReader reader;
    private final Todo todo;
    private final Annotate annotate;
    private final Types types;
    private final Target target;

    private final boolean skipAnnotations;

    public static MemberEnter instance(Context context) {
        MemberEnter instance = context.get(memberEnterKey);
        if (instance == null)
            instance = new MemberEnter(context);
        return instance;
    }

    protected MemberEnter(Context context) {
    	DEBUG.P(this,"MemberEnter(1)");
    	
        context.put(memberEnterKey, this);
        names = Name.Table.instance(context);
        enter = Enter.instance(context);
        log = Log.instance(context);
        chk = Check.instance(context);
        attr = Attr.instance(context);
        syms = Symtab.instance(context);
        make = TreeMaker.instance(context);
        reader = ClassReader.instance(context);
        todo = Todo.instance(context);
        annotate = Annotate.instance(context);
        types = Types.instance(context);
        target = Target.instance(context);
        skipAnnotations =
            Options.instance(context).get("skipAnnotations") != null;
            
        DEBUG.P(0,this,"MemberEnter(1)");
    }

    /** A queue for classes whose members still need to be entered into the
     *  symbol table.
     */
    ListBuffer<Env<AttrContext>> halfcompleted = new ListBuffer<Env<AttrContext>>();

    /** Set to true only when the first of a set of classes is
     *  processed from the halfcompleted queue.
     */
    boolean isFirst = true;

    /** A flag to disable completion from time to time during member
     *  enter, as we only need to look up types.  This avoids
     *  unnecessarily deep recursion.
     */
    boolean completionEnabled = true;
