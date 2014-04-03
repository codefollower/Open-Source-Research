/*
 * @(#)MemberEnter.java	1.67 07/03/21
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

    /* ---------- Processing import clauses ----------------
     */

    /** Import all classes of a class or package on demand.
     *  @param pos           Position to be used for error reporting.
     *  @param tsym          The class or package the members of which are imported.
     *  @param toScope   The (import) scope in which imported classes
     *               are entered.
     */
    //tsym可能是一个包也可能是一个类，
    //如果是一个包，就把这个包中的所有类导入env.toplevel.starImportScope
    //如果是一个类，就把在这个类中定义的所有成员类导入env.toplevel.starImportScope
    private void importAll(int pos,
                           final TypeSymbol tsym,
                           Env<AttrContext> env) {
        DEBUG.P(this,"importAll(3)");
        DEBUG.P("tsym="+tsym+" tsym.kind="+Kinds.toString(tsym.kind));
        
        //当tsym.kind == PCK时说明tsym是PackageSymbol的实例引用，当执行
        //tsym.members()时会调用ClassReader类的complete()导入tsym所表示的包中的所有类
        // Check that packages imported from exist (JLS ???).
        if (tsym.kind == PCK && tsym.members().elems == null && !tsym.exists()) {
        	//EXISTS标志在com.sun.tools.javac.jvm.ClassReader.includeClassFile(2)里设置
        	
            // If we can't find java.lang, exit immediately.
            if (((PackageSymbol)tsym).fullname.equals(names.java_lang)) {
                JCDiagnostic msg = JCDiagnostic.fragment("fatal.err.no.java.lang");
                //类全限定名称:com.sun.tools.javac.util.FatalError
                throw new FatalError(msg);
            } else {
				//例:import test2.*;(假设test2不存在)
                log.error(pos, "doesnt.exist", tsym);
            }
        }
        final Scope fromScope = tsym.members();
        //java.lang包中的所有类在默认情况下不用import
        final Scope toScope = env.toplevel.starImportScope;
        
        DEBUG.P("fromScope="+fromScope);
        DEBUG.P("toScope(for前)="+toScope);

        for (Scope.Entry e = fromScope.elems; e != null; e = e.sibling) {
        	//调用Symbol.ClassSymbol.getKind()会触发complete()
        	//所以调试时最好别用
        	//DEBUG.P("Entry e.sym="+e.sym+" (kind="+e.sym.getKind()+")");
        	//DEBUG.P("e.sym="+e.sym);
        	//DEBUG.P("toScope.nelems="+toScope.nelems);
            if (e.sym.kind == TYP && !toScope.includes(e.sym))
                toScope.enter(e.sym, fromScope);//注意这里,是ImportEntry
            else //if (e.sym.kind == TYP && toScope.includes(e.sym))
            	DEBUG.P("e.sym="+e.sym+"  已存在");
            //DEBUG.P("toScope.nelems="+toScope.nelems);
        }
        
        DEBUG.P("toScope(for后)="+toScope);
        DEBUG.P(1,this,"importAll(3)");    
    }

    /** Import all static members of a class or package on demand.
     *  @param pos           Position to be used for error reporting.
     *  @param tsym          The class or package the members of which are imported.
     *  @param toScope   The (import) scope in which imported classes
     *               are entered.
     */
    private void importStaticAll(int pos,
                                 final TypeSymbol tsym,
                                 Env<AttrContext> env) {
        try {//我加上的                         	
        DEBUG.P(this,"importStaticAll(3)");
        DEBUG.P("tsym="+tsym+" tsym.kind="+Kinds.toString(tsym.kind));   
        DEBUG.P("env="+env);
                              	
        final JavaFileObject sourcefile = env.toplevel.sourcefile;
        final Scope toScope = env.toplevel.starImportScope;
        final PackageSymbol packge = env.toplevel.packge;
        final TypeSymbol origin = tsym;
        
        DEBUG.P("starImportScope前="+env.toplevel.starImportScope);

        // enter imported types immediately
        new Object() {
            Set<Symbol> processed = new HashSet<Symbol>();
            void importFrom(TypeSymbol tsym) {
            	try {//我加上的                         	
                DEBUG.P(this,"importFrom(1)");
                if (tsym != null) DEBUG.P("tsym.name="+tsym.name+" tsym.kind="+Kinds.toString(tsym.kind));
                else DEBUG.P("tsym=null");
				//如果processed.add(tsym)返回true，就代表tsym之前没在Set中
                if (tsym == null || !processed.add(tsym))
                    return;

                // also import inherited names
                importFrom(types.supertype(tsym.type).tsym);
                for (Type t : types.interfaces(tsym.type))
                    importFrom(t.tsym);

                final Scope fromScope = tsym.members();
                DEBUG.P("fromScope="+fromScope);
                for (Scope.Entry e = fromScope.elems; e != null; e = e.sibling) {
                    Symbol sym = e.sym;
                    
                    DEBUG.P("sym.name="+sym.name);
                    DEBUG.P("sym.kind="+Kinds.toString(sym.kind));
                    DEBUG.P("sym.completer="+sym.completer);
                    DEBUG.P("sym.flags_field="+Flags.toString(sym.flags_field));
                    
					/*
                    if (sym.kind == TYP &&
                        (sym.flags() & STATIC) != 0 &&
                        staticImportAccessible(sym, packge) &&
                        sym.isMemberOf(origin, types) &&
                        !toScope.includes(sym))
                        
                        //fromScope.owner可以是origin.members().owner
                        //或者是origin.members().owner的所有超类与所有实现的接口
                        toScope.enter(sym, fromScope, origin.members());
					*/

					///*
					boolean flag1,flag2,flag3,flag4,flag5;
					flag1=flag2=flag3=flag4=flag5=false;
					flag1=sym.kind == TYP;
					if(flag1) flag2=(sym.flags() & STATIC) != 0;
					if(flag1 && flag2) flag3=staticImportAccessible(sym, packge);
					if(flag1 && flag2 && flag3) flag4=sym.isMemberOf(origin, types);
					if(flag1 && flag2 && flag3 && flag4) flag5=!toScope.includes(sym);

					if(flag1 && flag2 && flag3 && flag4 && flag5)
						toScope.enter(sym, fromScope, origin.members());

					DEBUG.P("kind == TYP     ="+flag1);
					DEBUG.P("flags == STATIC ="+flag2);
					DEBUG.P("accessible      ="+flag3);
					DEBUG.P("isMemberOf      ="+flag4);
					DEBUG.P("not includes    ="+flag5);
					//*/

                    DEBUG.P("");
                }
                
                }finally{//我加上的
                DEBUG.P(0,this,"importFrom(1)");
                }
            }
        }.importFrom(tsym);
        
        DEBUG.P("starImportScope后="+env.toplevel.starImportScope);
        
        // enter non-types before annotations that might use them
        annotate.earlier(new Annotate.Annotator() {
            Set<Symbol> processed = new HashSet<Symbol>();

            public String toString() {
                return "import static " + tsym + ".*" + " in " + sourcefile;
            }
            void importFrom(TypeSymbol tsym) {
                if (tsym == null || !processed.add(tsym))
                    return;

                // also import inherited names
                importFrom(types.supertype(tsym.type).tsym);
                for (Type t : types.interfaces(tsym.type))
                    importFrom(t.tsym);

                final Scope fromScope = tsym.members();
                DEBUG.P("toScope前="+toScope);
                for (Scope.Entry e = fromScope.elems; e != null; e = e.sibling) {
                    Symbol sym = e.sym;
                    if (sym.isStatic() && sym.kind != TYP &&
                        staticImportAccessible(sym, packge) &&
                        !toScope.includes(sym) &&
                        sym.isMemberOf(origin, types)) {
                        toScope.enter(sym, fromScope, origin.members());
                    }
                }
				DEBUG.P("toScope后="+toScope);
            }
            public void enterAnnotation() {
				DEBUG.P(this,"enterAnnotation()");
				DEBUG.P("tsym="+tsym);

                importFrom(tsym);
				DEBUG.P(0,this,"enterAnnotation()");
            }
        });
        
        }finally{//我加上的
		DEBUG.P(0,this,"importStaticAll(3)");
		}
    }

    // is the sym accessible everywhere in packge?
    boolean staticImportAccessible(Symbol sym, PackageSymbol packge) {
		/*
        int flags = (int)(sym.flags() & AccessFlags);
        switch (flags) {
        default:
        case PUBLIC:
            return true;
        case PRIVATE:
            return false;
        case 0:
        case PROTECTED:
            return sym.packge() == packge;
        }*/

		boolean result=false;
    	try {//我加上的                         	
        DEBUG.P(this,"staticImportAccessible(2)");
        DEBUG.P("sym="+sym);   
        DEBUG.P("sym.packge()="+sym.packge()+" packge="+packge);
        DEBUG.P("sym.flags()="+Flags.toString(sym.flags()));
        DEBUG.P("(sym.flags() & AccessFlags)="+Flags.toString((sym.flags() & AccessFlags)));

        int flags = (int)(sym.flags() & AccessFlags);
        switch (flags) {
        default:
        case PUBLIC:
			result=true;
            return true;
        case PRIVATE:
            return false;
        case 0:
        case PROTECTED:
			result=sym.packge() == packge;
            return sym.packge() == packge;
        }

        }finally{//我加上的
		DEBUG.P("result="+result);
		DEBUG.P(0,this,"staticImportAccessible(2)");
		}
    }

    /** Import statics types of a given name.  Non-types are handled in Attr.
     *  @param pos           Position to be used for error reporting.
     *  @param tsym          The class from which the name is imported.
     *  @param name          The (simple) name being imported.
     *  @param env           The environment containing the named import
     *                  scope to add to.
     */
    private void importNamedStatic(final DiagnosticPosition pos,
                                   final TypeSymbol tsym,
                                   final Name name,
                                   final Env<AttrContext> env) {
        try {//我加上的                         	
        DEBUG.P(this,"importNamedStatic(4)");
        DEBUG.P("name="+name+" tsym="+tsym+" tsym.kind="+Kinds.toString(tsym.kind));   
        DEBUG.P("env="+env);
        
        if (tsym.kind != TYP) {
            /*例如:
            src/my/test/EnterTest.java:18: 仅从类和接口静态导入
            import static my.MyProcessor;
            ^
            */

            log.error(pos, "static.imp.only.classes.and.interfaces");
            return;
        }

        final Scope toScope = env.toplevel.namedImportScope;
        final PackageSymbol packge = env.toplevel.packge;
        final TypeSymbol origin = tsym;
        
        DEBUG.P("namedImportScope前="+env.toplevel.namedImportScope);
        
        // enter imported types immediately
        new Object() {
            Set<Symbol> processed = new HashSet<Symbol>();
            void importFrom(TypeSymbol tsym) {
                try {//我加上的                         	
                DEBUG.P(this,"importFrom(1)");
                if (tsym != null) DEBUG.P("tsym.name="+tsym.name+" tsym.kind="+Kinds.toString(tsym.kind));
                else DEBUG.P("tsym=null");
                
                if (tsym == null || !processed.add(tsym))
                    return;

                // also import inherited names
                importFrom(types.supertype(tsym.type).tsym);
                for (Type t : types.interfaces(tsym.type))
                    importFrom(t.tsym);

				DEBUG.P("tsym.members()="+tsym.members());
                for (Scope.Entry e = tsym.members().lookup(name);
                     e.scope != null;
                     e = e.next()) {
                    Symbol sym = e.sym;
                    
                    DEBUG.P("sym.name="+sym.name);
                    DEBUG.P("sym.kind="+Kinds.toString(sym.kind));
                    DEBUG.P("sym.completer="+sym.completer);
                    DEBUG.P("sym.flags_field="+Flags.toString(sym.flags_field));
                    
                    if (sym.isStatic() &&
                        sym.kind == TYP &&
                        staticImportAccessible(sym, packge) &&
                        sym.isMemberOf(origin, types) &&
                        chk.checkUniqueStaticImport(pos, sym, toScope))
                        toScope.enter(sym, sym.owner.members(), origin.members());
                }
                
                }finally{//我加上的
                DEBUG.P(0,this,"importFrom(1)");
                }
            }
        }.importFrom(tsym);
        
        DEBUG.P("namedImportScope后="+env.toplevel.namedImportScope);

        // enter non-types before annotations that might use them
        annotate.earlier(new Annotate.Annotator() {
            Set<Symbol> processed = new HashSet<Symbol>();
            boolean found = false;

            public String toString() {
                return "import static " + tsym + "." + name;
            }
            void importFrom(TypeSymbol tsym) {
				try {//我加上的                         	
                DEBUG.P(this,"importFrom(1)");
                if (tsym != null) DEBUG.P("tsym.name="+tsym.name+" tsym.kind="+Kinds.toString(tsym.kind));
                else DEBUG.P("tsym=null");

                if (tsym == null || !processed.add(tsym))
                    return;

                // also import inherited names
                importFrom(types.supertype(tsym.type).tsym);
                for (Type t : types.interfaces(tsym.type))
                    importFrom(t.tsym);

                for (Scope.Entry e = tsym.members().lookup(name);
                     e.scope != null;
                     e = e.next()) {
                    Symbol sym = e.sym;
                    if (sym.isStatic() &&
                        staticImportAccessible(sym, packge) &&
                        sym.isMemberOf(origin, types)) {
                        found = true;
                        if (sym.kind == MTH ||
                            sym.kind != TYP && chk.checkUniqueStaticImport(pos, sym, toScope))
                            toScope.enter(sym, sym.owner.members(), origin.members());
                    }
                }

				}finally{//我加上的
                DEBUG.P(0,this,"importFrom(1)");
                }
            }
            public void enterAnnotation() {
				DEBUG.P(this,"enterAnnotation()");
                JavaFileObject prev = log.useSource(env.toplevel.sourcefile);
                try {
                    importFrom(tsym);
					//如果导入的不是一个静态类(或者其他情况)则报错
					DEBUG.P("found="+found);
                    if (!found) {
                        log.error(pos, "cant.resolve.location",
                                  JCDiagnostic.fragment("kindname.static"),
                                  name, "", "", Resolve.typeKindName(tsym.type),
                                  tsym.type);
                    }
                } finally {
                    log.useSource(prev);
					DEBUG.P(0,this,"enterAnnotation()");
                }
            }
        });
        
        }finally{//我加上的
		DEBUG.P(0,this,"importNamedStatic(4)");
		}
    }
    /** Import given class.
     *  @param pos           Position to be used for error reporting.
     *  @param tsym          The class to be imported.
     *  @param env           The environment containing the named import
     *                  scope to add to.
     */
    private void importNamed(DiagnosticPosition pos, Symbol tsym, Env<AttrContext> env) {
    	DEBUG.P(this,"importNamed(3)");
        DEBUG.P("tsym="+tsym);
        DEBUG.P("env="+env);
        DEBUG.P("tsym.kind="+Kinds.toString(tsym.kind));
        DEBUG.P("env.toplevel.namedImportScope前="+env.toplevel.namedImportScope);

		//像这样，导入两个一样的类不会报错，会重复加入namedImportScope
		//import test.memberEnter.UniqueImport;
		//import test.memberEnter.UniqueImport;
        
        if (tsym.kind == TYP &&
            chk.checkUniqueImport(pos, tsym, env.toplevel.namedImportScope))
            env.toplevel.namedImportScope.enter(tsym, tsym.owner.members());
        
        DEBUG.P("env.toplevel.namedImportScope后="+env.toplevel.namedImportScope);
        DEBUG.P(0,this,"importNamed(3)");
    }

    /** Construct method type from method signature.
     *  @param typarams    The method's type parameters.
     *  @param params      The method's value parameters.
     *  @param res             The method's result type,
     *                 null if it is a constructor.
     *  @param thrown      The method's thrown exceptions.
     *  @param env             The method's (local) environment.
     */
    Type signature(List<JCTypeParameter> typarams,
                   List<JCVariableDecl> params,
                   JCTree res,
                   List<JCExpression> thrown,
                   Env<AttrContext> env) {
        try {//我加上的
        DEBUG.P(this,"signature(5)");
        DEBUG.P("typarams="+typarams);
		DEBUG.P("params="+params); 
		DEBUG.P("res="+res); 
		DEBUG.P("thrown="+thrown); 
		DEBUG.P("env="+env); 
		        	
        // Enter and attribute type parameters.
        List<Type> tvars = enter.classEnter(typarams, env);
        attr.attribTypeVariables(typarams, env);

        // Enter and attribute value parameters.
        ListBuffer<Type> argbuf = new ListBuffer<Type>();
        for (List<JCVariableDecl> l = params; l.nonEmpty(); l = l.tail) {
            memberEnter(l.head, env);
            argbuf.append(l.head.vartype.type);
        }

        // Attribute result type, if one is given.
        Type restype = res == null ? syms.voidType : attr.attribType(res, env);

        // Attribute thrown exceptions.
        ListBuffer<Type> thrownbuf = new ListBuffer<Type>();
        for (List<JCExpression> l = thrown; l.nonEmpty(); l = l.tail) {
            Type exc = attr.attribType(l.head, env);
			DEBUG.P("exc="+exc);
			DEBUG.P("exc.tag="+TypeTags.toString(exc.tag));
            if (exc.tag != TYPEVAR)
                exc = chk.checkClassType(l.head.pos(), exc);//也就是说throws语句后面必须是类名
            thrownbuf.append(exc);
        }
        
        //注意MethodType并不包含Type Parameter
        Type mtype = new MethodType(argbuf.toList(),
                                    restype,
                                    thrownbuf.toList(),
                                    syms.methodClass);
        return tvars.isEmpty() ? mtype : new ForAll(tvars, mtype);
        
        }finally{//我加上的
        DEBUG.P(0,this,"signature(5)");
        }
    }

/* ********************************************************************
 * Visitor methods for member enter
 *********************************************************************/

    /** Visitor argument: the current environment
     */
    protected Env<AttrContext> env;

    /** Enter field and method definitions and process import
     *  clauses, catching any completion failure exceptions.
     */
    protected void memberEnter(JCTree tree, Env<AttrContext> env) {
    	DEBUG.P(this,"memberEnter(2)");
    	DEBUG.P("tree.tag="+tree.myTreeTag());
    	DEBUG.P("先前Env="+this.env);
		DEBUG.P("当前Env="+env);

        Env<AttrContext> prevEnv = this.env;       
        try {
            this.env = env;
            tree.accept(this);
        }  catch (CompletionFailure ex) {
            chk.completionError(tree.pos(), ex);
        } finally {
            this.env = prevEnv;
            DEBUG.P(1,this,"memberEnter(2)");
        }
    }

    /** Enter members from a list of trees.
     */
    void memberEnter(List<? extends JCTree> trees, Env<AttrContext> env) {
    	DEBUG.P(this,"memberEnter(List<? extends JCTree> trees, Env<AttrContext> env)");
        DEBUG.P("trees.size="+trees.size());
        for (List<? extends JCTree> l = trees; l.nonEmpty(); l = l.tail) {
            memberEnter(l.head, env);
        }
        DEBUG.P(0,this,"memberEnter(List<? extends JCTree> trees, Env<AttrContext> env)");
    }

    /** Enter members for a class.
     */
    void finishClass(JCClassDecl tree, Env<AttrContext> env) {
    	DEBUG.P(this,"finishClass(2)");
    	DEBUG.P("tree="+tree);
    	DEBUG.P("env="+env);
    	
        if ((tree.mods.flags & Flags.ENUM) != 0 &&
            (types.supertype(tree.sym.type).tsym.flags() & Flags.ENUM) == 0) {
            addEnumMembers(tree, env);
        }
        memberEnter(tree.defs, env);
        
        DEBUG.P(0,this,"finishClass(2)");
    }

    /** Add the implicit members for an enum type
     *  to the symbol table.
     */
    private void addEnumMembers(JCClassDecl tree, Env<AttrContext> env) {
    	try {//我加上的
    	DEBUG.P(this,"addEnumMembers(2)");

        JCExpression valuesType = make.Type(new ArrayType(tree.sym.type, syms.arrayClass));

        // public static T[] values() { return ???; }
        JCMethodDecl values = make.
            MethodDef(make.Modifiers(Flags.PUBLIC|Flags.STATIC),
                      names.values,
                      valuesType,
                      List.<JCTypeParameter>nil(),
                      List.<JCVariableDecl>nil(),
                      List.<JCExpression>nil(), // thrown
                      null, //make.Block(0, Tree.emptyList.prepend(make.Return(make.Ident(names._null)))),
                      null);
        memberEnter(values, env);

        // public static T valueOf(String name) { return ???; }
        JCMethodDecl valueOf = make.
            MethodDef(make.Modifiers(Flags.PUBLIC|Flags.STATIC),
                      names.valueOf,
                      make.Type(tree.sym.type),
                      List.<JCTypeParameter>nil(),
                      List.of(make.VarDef(make.Modifiers(Flags.PARAMETER),
                                            names.fromString("name"),
                                            make.Type(syms.stringType), null)),
                      List.<JCExpression>nil(), // thrown
                      null, //make.Block(0, Tree.emptyList.prepend(make.Return(make.Ident(names._null)))),
                      null);
        memberEnter(valueOf, env);

        // the remaining members are for bootstrapping only
        if (!target.compilerBootstrap(tree.sym)) return;

        // public final int ordinal() { return ???; }
        JCMethodDecl ordinal = make.at(tree.pos).
            MethodDef(make.Modifiers(Flags.PUBLIC|Flags.FINAL),
                      names.ordinal,
                      make.Type(syms.intType),
                      List.<JCTypeParameter>nil(),
                      List.<JCVariableDecl>nil(),
                      List.<JCExpression>nil(),
                      null,
                      null);
        memberEnter(ordinal, env);

        // public final String name() { return ???; }
        JCMethodDecl name = make.
            MethodDef(make.Modifiers(Flags.PUBLIC|Flags.FINAL),
                      names._name,
                      make.Type(syms.stringType),
                      List.<JCTypeParameter>nil(),
                      List.<JCVariableDecl>nil(),
                      List.<JCExpression>nil(),
                      null,
                      null);
        memberEnter(name, env);

        // public int compareTo(E other) { return ???; }
        MethodSymbol compareTo = new
            MethodSymbol(Flags.PUBLIC,
                         names.compareTo,
                         new MethodType(List.of(tree.sym.type),
                                        syms.intType,
                                        List.<Type>nil(),
                                        syms.methodClass),
                         tree.sym);
        memberEnter(make.MethodDef(compareTo, null), env);
        
        }finally{//我加上的
    	DEBUG.P("tree="+tree);
		DEBUG.P(0,this,"addEnumMembers(2)");
		}
    }

    public void visitTopLevel(JCCompilationUnit tree) {
    	try {
            // <editor-fold defaultstate="collapsed">
    	DEBUG.P(this,"visitTopLevel(1)");
    	DEBUG.P("tree.namedImportScope="+tree.namedImportScope);
    	DEBUG.P("tree.starImportScope="+tree.starImportScope);
    	DEBUG.P("env.info.scope="+env.info.scope);
    	DEBUG.P("tree.packge.name="+tree.packge.name);
    	DEBUG.P("tree.packge.fullname="+tree.packge.fullname);
    	DEBUG.P("tree.packge.owner.name="+tree.packge.owner.name);
    	DEBUG.P("tree.packge.owner.fullname="+tree.packge.owner.getQualifiedName());
    	
		DEBUG.P(2);
    	DEBUG.P("tree.starImportScope.elems="+tree.starImportScope.elems);
    	//当tree.starImportScope.nelems=0时tree.starImportScope.elems==null
        if (tree.starImportScope.elems != null) {
        	/*
        	当在同一文件内定义了多个类时就会出现这种情况
        	如下代码所示:
        	
        	package my.test;
			public class Test {}
        	class MyTheSamePackageClass {}
        	
        	*/
        	DEBUG.P("starImportScope 已处理");
        	
            // we must have already processed this toplevel
            return;
        }

		DEBUG.P("checkClash="+checkClash);
		DEBUG.P("tree.pid="+tree.pid);

        // check that no class exists with same fully qualified name as
        // toplevel package
        if (checkClash && tree.pid != null) {
            Symbol p = tree.packge;
			while (p.owner != syms.rootPackage) {
                p.owner.complete(); // enter all class members of p            
                /*
                比如:如果包名是my.test,然后在my目录下有个test.java文件
                那么就会出现错误提示:
                package my.test clashes with class of same name
                package my.test;
                ^
                原因:
                如果类路径是: F:\javac\bin\mybin；
                test.java文件位置: F:\javac\bin\mybin\my\test.java；
                p是: my.test
                p.owner就是: my
                那么加载的包名是: my
                test.java文件内容不用管，什么都可以；

                当调用到com.sun.tools.javac.util.JavacFileManager===>inferBinaryName(2)时
                它按包名my截断F:\javac\bin\mybin\my\test.java得到my\test.java
                将目录分隔符替换成".",去掉扩展名，得到一个完全类名"my.test"，
                如果这里的包名也是"my.test"就会产生冲突

				但是，如果在F:\javac\bin\mybin目录下有个类文件my.java是不会冲突的，
				因为当p变为“my"时，p.owner变成了syms.rootPackage，while循环结束了。
				如果把循环条件改成(p.owner != null)，就可以检测出my.java与包名my冲突
                */
                if (syms.classes.get(p.getQualifiedName()) != null) {
                    log.error(tree.pos,
                              "pkg.clashes.with.class.of.same.name",
                              p);
                }
                p = p.owner;
                
                DEBUG.P("p.name="+p.name);
                DEBUG.P("p.fullname="+p.getQualifiedName());
                DEBUG.P("p.owner.name="+p.owner.name);
                DEBUG.P("p.owner.fullname="+p.owner.getQualifiedName());
            }
        }
        // </editor-fold>

        // process package annotations
		//编译package-info.java时能测试tree.packageAnnotations!=null
        annotateLater(tree.packageAnnotations, env, tree.packge);
        
        // Import-on-demand java.lang.
        importAll(tree.pos, reader.enterPackage(names.java_lang), env);

		DEBUG.P("tree.namedImportScope="+tree.namedImportScope);
    	DEBUG.P("tree.starImportScope="+tree.starImportScope);
    	DEBUG.P("env.info.scope="+env.info.scope);
		DEBUG.P("env="+env);

        // Process all import clauses.
        memberEnter(tree.defs, env);

		//DEBUG.P("stop",true);
        
    	}finally{
    	DEBUG.P(0,this,"visitTopLevel(1)");
    	}
    }

    // process the non-static imports and the static imports of types.
    public void visitImport(JCImport tree) {
        // <editor-fold defaultstate="collapsed">
        
    	DEBUG.P(this,"visitImport(1)");
    	DEBUG.P("tree.qualid="+tree.qualid);
        DEBUG.P("tree.staticImport="+tree.staticImport);
    	
        JCTree imp = tree.qualid;
        Name name = TreeInfo.name(imp);//取最后一个Ident(如java.util.* 则返回*; 如java.util.Map 则返回Map)
        TypeSymbol p;
        
        // Create a local environment pointing to this tree to disable
        // effects of other imports in Resolve.findGlobalType
        Env<AttrContext> localEnv = env.dup(tree);//outer为null
        //localEnv = env.dup(tree)相当于先把env复制一分，再用当前tree替换原来的tree,
        //新的env(localEnv)的next指向原来的env

        // Attribute qualifying package or class.
        JCFieldAccess s = (JCFieldAccess) imp;
        
        
        /*
        因为所有的导入(import)语句都是用一棵JCFieldAccess树
        表示的(参见Parser.importDeclaration())，
        JCFieldAccess树也含有JCIdent(最后一个selector)，
        在MemberEnter阶段的visitImport(1)方法中会设
        置JCFieldAccess与JCIdent的Symbol sym字段
        */
        //在没有attribTree()前sym都是null
        DEBUG.P(2);DEBUG.P("************attribTree()前************");
        for(JCTree myJCTree=s;;) {
            DEBUG.P("");
            if(myJCTree.tag==JCTree.SELECT) {
                JCFieldAccess myJCFieldAccess=(JCFieldAccess)myJCTree;
                DEBUG.P("JCFieldAccess.name="+myJCFieldAccess.name);
                DEBUG.P("JCFieldAccess.sym="+myJCFieldAccess.sym);
                myJCTree=myJCFieldAccess.selected;
            } else if(myJCTree.tag==JCTree.IDENT) {
                JCIdent myJCIdent=(JCIdent)myJCTree;
                DEBUG.P("JCIdent.name="+myJCIdent.name);
                DEBUG.P("JCIdent.sym="+myJCIdent.sym);
                break;
            } else break;
        }
        DEBUG.P("************attribTree()前************");DEBUG.P(2);

        
        //attribTree()调用有点繁琐，得耐心看
        p = attr.
            attribTree(s.selected,
                       localEnv,
                       tree.staticImport ? TYP : (TYP | PCK),
                       Type.noType).tsym;
        
        
        
        //在attribTree()后只有第一个JCFieldAccess的sym是null
        DEBUG.P(2);DEBUG.P("************attribTree()后************");
        for(JCTree myJCTree=s;;) {
            DEBUG.P("");
            if(myJCTree.tag==JCTree.SELECT) {
                JCFieldAccess myJCFieldAccess=(JCFieldAccess)myJCTree;
                DEBUG.P("JCFieldAccess.name="+myJCFieldAccess.name);
                DEBUG.P("JCFieldAccess.sym="+myJCFieldAccess.sym);
                myJCTree=myJCFieldAccess.selected;
            } else if(myJCTree.tag==JCTree.IDENT) {
                JCIdent myJCIdent=(JCIdent)myJCTree;
                DEBUG.P("JCIdent.name="+myJCIdent.name);
                DEBUG.P("JCIdent.sym="+myJCIdent.sym);
                break;
            } else break;
        }
        DEBUG.P("************attribTree()后************");DEBUG.P(2);  
        
		// </editor-fold>
        
        DEBUG.P("p="+p);
        DEBUG.P("name="+name);    
        //DEBUG.P("visitImport stop",true);          
        if (name == names.asterisk) {
            // Import on demand.
            chk.checkCanonical(s.selected);
            if (tree.staticImport)
                importStaticAll(tree.pos, p, env);
            else
                importAll(tree.pos, p, env);
        } else {
            // Named type import.
            if (tree.staticImport) {
                importNamedStatic(tree.pos(), p, name, localEnv);
                chk.checkCanonical(s.selected);
            } else {
                TypeSymbol c = attribImportType(imp, localEnv).tsym;
                DEBUG.P("TypeSymbol c="+c); 
                chk.checkCanonical(imp);
                importNamed(tree.pos(), c, env);
            }
        }
        
        DEBUG.P(0,this,"visitImport(1)");
    }

    public void visitMethodDef(JCMethodDecl tree) {
    	DEBUG.P(this,"visitMethodDef(1)");
    	DEBUG.P("tree.name="+tree.name); 
        Scope enclScope = enter.enterScope(env);
        DEBUG.P("enclScope前="+enclScope); 
        MethodSymbol m = new MethodSymbol(0, tree.name, null, enclScope.owner);
		DEBUG.P("tree.mods.flags="+Flags.toString(tree.mods.flags));
        DEBUG.P("m.flags_field前="+Flags.toString(m.flags_field));
        m.flags_field = chk.checkFlags(tree.pos(), tree.mods.flags, m, tree);
        tree.sym = m;
        DEBUG.P("m.flags_field后="+Flags.toString(m.flags_field));
        Env<AttrContext> localEnv = methodEnv(tree, env);
        
        //DEBUG.P("localEnv="+localEnv); 
        
        // Compute the method type
        m.type = signature(tree.typarams, tree.params,
                           tree.restype, tree.thrown,
                           localEnv);
        
        DEBUG.P("m.type.tag="+TypeTags.toString(m.type.tag));
                          
        // Set m.params
        ListBuffer<VarSymbol> params = new ListBuffer<VarSymbol>();
        JCVariableDecl lastParam = null;
        for (List<JCVariableDecl> l = tree.params; l.nonEmpty(); l = l.tail) {
            JCVariableDecl param = lastParam = l.head;
            assert param.sym != null;
            params.append(param.sym);
        }
        m.params = params.toList();

        // mark the method varargs, if necessary
        if (lastParam != null && (lastParam.mods.flags & Flags.VARARGS) != 0)
            m.flags_field |= Flags.VARARGS;

        localEnv.info.scope.leave();
        DEBUG.P("localEnv="+localEnv); 
        if (chk.checkUnique(tree.pos(), m, enclScope)) {
            enclScope.enter(m);
        }
        annotateLater(tree.mods.annotations, localEnv, m);
		DEBUG.P("tree.defaultValue="+tree.defaultValue); 
        if (tree.defaultValue != null)
            annotateDefaultValueLater(tree.defaultValue, localEnv, m);
        
        DEBUG.P("enclScope后="+enclScope); 
       	DEBUG.P(0,this,"visitMethodDef(1)");     
    }

    /** Create a fresh environment for method bodies.
     *  @param tree     The method definition.
     *  @param env      The environment current outside of the method definition.
     */
    Env<AttrContext> methodEnv(JCMethodDecl tree, Env<AttrContext> env) {
    	DEBUG.P(this,"methodEnv(2)");
    	DEBUG.P("env="+env);
    	
    	//dupUnshared()后会有两个scope,新scope的next指向原来的scope,
    	//新scope的Entry[] table由原来的scope的table复制而来，但新scope的
    	//elems 开始时为 null，所以不会显示原来的scope的table。
    	//如下面的(nelems=0 owner=<init>()):
    	//localEnv=Env(TK=METHOD EC=Test)[AttrContext[Scope[(nelems=0 owner=<init>()) | (nelems=6 owner=Test)super, this, E, T, V, S]],outer=Env(TK=COMPILATION_UNIT EC=)[AttrContext[Scope[(nelems=3 owner=test)MyInnerClass, MyInnerClassStaticPublic, Test]]]]
        Env<AttrContext> localEnv =
            env.dup(tree, env.info.dup(env.info.scope.dupUnshared()));
        localEnv.enclMethod = tree;
        localEnv.info.scope.owner = tree.sym;
        if ((tree.mods.flags & STATIC) != 0) localEnv.info.staticLevel++;
        DEBUG.P("localEnv="+localEnv);
        DEBUG.P(0,this,"methodEnv(2)");
        return localEnv;
    }

    public void visitVarDef(JCVariableDecl tree) {
    	DEBUG.P(this,"visitVarDef(1)");

        DEBUG.P("tree="+tree); 
        DEBUG.P("tree.mods.flags="+Flags.toString(tree.mods.flags));
		DEBUG.P("env.info.scope.owner="+env.info.scope.owner);
		DEBUG.P("env.info.scope.owner.flags_field="+Flags.toString(env.info.scope.owner.flags_field));
        
        Env<AttrContext> localEnv = env;
        
        DEBUG.P("localEnv1="+localEnv); 
        
        if ((tree.mods.flags & STATIC) != 0 ||
            (env.info.scope.owner.flags() & INTERFACE) != 0) {
            localEnv = env.dup(tree, env.info.dup());
            localEnv.info.staticLevel++;
        }
        DEBUG.P("localEnv2="+localEnv); 
        
        attr.attribType(tree.vartype, localEnv);
        Scope enclScope = enter.enterScope(env);
        
        DEBUG.P("enclScope前="+enclScope); 
        
        VarSymbol v =
            new VarSymbol(0, tree.name, tree.vartype.type, enclScope.owner);
        v.flags_field = chk.checkFlags(tree.pos(), tree.mods.flags, v, tree);
        tree.sym = v;
        
        DEBUG.P("v.flags_field="+Flags.toString(v.flags_field));
        
        if (tree.init != null) {
            v.flags_field |= HASINIT;
            if ((v.flags_field & FINAL) != 0 && tree.init.tag != JCTree.NEWCLASS)
                v.setLazyConstValue(initEnv(tree, env), log, attr, tree.init);
        }
        if (chk.checkUnique(tree.pos(), v, enclScope)) {
            chk.checkTransparentVar(tree.pos(), v, enclScope);
            enclScope.enter(v);
        }
        annotateLater(tree.mods.annotations, localEnv, v);
        v.pos = tree.pos;
        
        DEBUG.P("enclScope后="+enclScope); 
        DEBUG.P(0,this,"visitVarDef(1)");
    }

    /** Create a fresh environment for a variable's initializer.
     *  If the variable is a field, the owner of the environment's scope
     *  is be the variable itself, otherwise the owner is the method
     *  enclosing the variable definition.
     *
     *  @param tree     The variable definition.
     *  @param env      The environment current outside of the variable definition.
     */
    Env<AttrContext> initEnv(JCVariableDecl tree, Env<AttrContext> env) {
		DEBUG.P(this,"initEnv(2)");
        DEBUG.P("tree="+tree);
		DEBUG.P("env="+env); 
        
        Env<AttrContext> localEnv = env.dupto(new AttrContextEnv(tree, env.info.dup()));

		DEBUG.P("tree.sym.owner.kind="+Kinds.toString(tree.sym.owner.kind));

        if (tree.sym.owner.kind == TYP) {
            localEnv.info.scope = new Scope.DelegatedScope(env.info.scope);
            localEnv.info.scope.owner = tree.sym;
        }
        if ((tree.mods.flags & STATIC) != 0 ||
            (env.enclClass.sym.flags() & INTERFACE) != 0)
            localEnv.info.staticLevel++;

		DEBUG.P("localEnv="+localEnv);
		DEBUG.P(1,this,"initEnv(2)");
        return localEnv;
    }

    /** Default member enter visitor method: do nothing
     */
    public void visitTree(JCTree tree) {
    	DEBUG.P(this,"visitTree(1)");
    	DEBUG.P(0,this,"visitTree(1)");
    }
    
    
    public void visitErroneous(JCErroneous tree) {
    	DEBUG.P(this,"visitErroneous(1)");
        memberEnter(tree.errs, env);
        DEBUG.P(0,this,"visitErroneous(1)");
    }
    
    public Env<AttrContext> getMethodEnv(JCMethodDecl tree, Env<AttrContext> env) {
        Env<AttrContext> mEnv = methodEnv(tree, env);
		mEnv.info.lint = mEnv.info.lint.augment(tree.sym.attributes_field, tree.sym.flags());
        for (List<JCTypeParameter> l = tree.typarams; l.nonEmpty(); l = l.tail)
            mEnv.info.scope.enterIfAbsent(l.head.type.tsym);
        for (List<JCVariableDecl> l = tree.params; l.nonEmpty(); l = l.tail)
            mEnv.info.scope.enterIfAbsent(l.head.sym);
        return mEnv;
    }
    
    public Env<AttrContext> getInitEnv(JCVariableDecl tree, Env<AttrContext> env) {
        Env<AttrContext> iEnv = initEnv(tree, env);
        return iEnv;
    }

/* ********************************************************************
 * Type completion
 *********************************************************************/

    Type attribImportType(JCTree tree, Env<AttrContext> env) {
        assert completionEnabled;
        try {
            DEBUG.P(this,"attribImportType(JCTree tree, Env<AttrContext> env)");
            DEBUG.P("tree="+tree);
            DEBUG.P("env="+env);
            // To prevent deep recursion, suppress completion of some
            // types.
            completionEnabled = false;
            //由import my.StaticImportTest.MyInnerClass;构成的JCFieldAccess树
            //JCFieldAccess树里每一个selector的sym在attribType后都不为null
            return attr.attribType(tree, env);
        } finally {
            DEBUG.P(0,this,"attribImportType(JCTree tree, Env<AttrContext> env)");
            completionEnabled = true;
        }
    }

/* ********************************************************************
 * Annotation processing
 *********************************************************************/

    /** Queue annotations for later processing. */
    void annotateLater(final List<JCAnnotation> annotations,
                       final Env<AttrContext> localEnv,
                       final Symbol s) {
        try {
        DEBUG.P(this,"annotateLater(3)");
        DEBUG.P("List<JCAnnotation> annotations.size()="+annotations.size());
        DEBUG.P("annotations="+annotations);
        DEBUG.P("annotations.isEmpty()="+annotations.isEmpty());
        DEBUG.P("localEnv="+localEnv);
        DEBUG.P("s="+s);
        
        
        if (annotations.isEmpty()) return;
        DEBUG.P("sym.kind="+Kinds.toString(s.kind));
        
        if (s.kind != PCK) s.attributes_field = null; // mark it incomplete for now
        DEBUG.P("s.attributes_field="+s.attributes_field);
        annotate.later(new Annotate.Annotator() {
                public String toString() {
                    return "annotate " + annotations + " onto " + s + " in " + s.owner;
                }
                public void enterAnnotation() {
					DEBUG.P(this,"enterAnnotation()");
                    assert s.kind == PCK || s.attributes_field == null;
                    JavaFileObject prev = log.useSource(localEnv.toplevel.sourcefile);
                    try {
						//同时编译
						//test/memberEnter/package-info.java
						//test/memberEnter/subdir/package-info.java
						//两文件的内容都为:
						//@PackageAnnotations
						//package test.memberEnter;

						//就可测试下面的错误:
						//test\memberEnter\subdir\package-info.java:1: 软件包 test.memberEnter 已被注释


                        if (s.attributes_field != null &&
                            s.attributes_field.nonEmpty() &&
                            annotations.nonEmpty())
                            log.error(annotations.head.pos,
                                      "already.annotated",
                                      Resolve.kindName(s), s);
                        enterAnnotations(annotations, localEnv, s);
                    } finally {
                        log.useSource(prev);
						DEBUG.P(0,this,"enterAnnotation()");
                    }
                }
            });
            
            
        } finally {
        DEBUG.P(0,this,"annotateLater(3)");
        } 
    }

    /**
     * Check if a list of annotations contains a reference to
     * java.lang.Deprecated.
     **/
    private boolean hasDeprecatedAnnotation(List<JCAnnotation> annotations) {
        for (List<JCAnnotation> al = annotations; al.nonEmpty(); al = al.tail) {
            JCAnnotation a = al.head;
			//因为MemberEnter阶段是紧跟在Parser阶段之后的，而在Parser阶段如果
			//@Deprecated带有参数(如:@Deprecated("str"))是正确的，在这里使用了
			//a.args.isEmpty()是为了提前检测一下是否正确使用了@Deprecated，以便
			//为当前ClassSymbol的flags_field加上DEPRECATED(见complete)
            if (a.annotationType.type == syms.deprecatedType && a.args.isEmpty())
                return true;
        }
        return false;
    }


    /** Enter a set of annotations. */
    private void enterAnnotations(List<JCAnnotation> annotations,
                          Env<AttrContext> env,
                          Symbol s) {
        DEBUG.P(this,"enterAnnotations(3)");   
        DEBUG.P("annotations="+annotations);
        DEBUG.P("env="+env);
        DEBUG.P("s="+s);
        DEBUG.P("skipAnnotations="+skipAnnotations);
                       
        ListBuffer<Attribute.Compound> buf =
            new ListBuffer<Attribute.Compound>();
        Set<TypeSymbol> annotated = new HashSet<TypeSymbol>();
        if (!skipAnnotations)
        for (List<JCAnnotation> al = annotations; al.nonEmpty(); al = al.tail) {
            JCAnnotation a = al.head;
            DEBUG.P("a="+a);
            Attribute.Compound c = annotate.enterAnnotation(a,
                                                            syms.annotationType,
                                                            env);
            DEBUG.P("c="+c);
        
            if (c == null) continue;
            buf.append(c);
            // Note: @Deprecated has no effect on local variables and parameters
            if (!c.type.isErroneous()
                && s.owner.kind != MTH
                && types.isSameType(c.type, syms.deprecatedType))
                s.flags_field |= Flags.DEPRECATED;
            if (!annotated.add(a.type.tsym))
                log.error(a.pos, "duplicate.annotation");
        }
        s.attributes_field = buf.toList();
        
        DEBUG.P(0,this,"enterAnnotations(3)");  
    }

    /** Queue processing of an attribute default value. */
    void annotateDefaultValueLater(final JCExpression defaultValue,
                                   final Env<AttrContext> localEnv,
                                   final MethodSymbol m) {
		DEBUG.P(this,"annotateDefaultValueLater(3)");   
        DEBUG.P("defaultValue="+defaultValue);
        DEBUG.P("localEnv="+localEnv);
        DEBUG.P("m="+m);
        annotate.later(new Annotate.Annotator() {
                public String toString() {
                    return "annotate " + m.owner + "." +
                        m + " default " + defaultValue;
                }
                public void enterAnnotation() {
                    JavaFileObject prev = log.useSource(localEnv.toplevel.sourcefile);
                    try {
                        enterDefaultValue(defaultValue, localEnv, m);
                    } finally {
                        log.useSource(prev);
                    }
                }
            });
		DEBUG.P(0,this,"annotateDefaultValueLater(3)");   
    }

    /** Enter a default value for an attribute method. */
    private void enterDefaultValue(final JCExpression defaultValue,
                                   final Env<AttrContext> localEnv,
                                   final MethodSymbol m) {
        m.defaultValue = annotate.enterAttributeValue(m.type.getReturnType(),
                                                      defaultValue,
                                                      localEnv);
    }

/* ********************************************************************
 * Source completer
 *********************************************************************/

    /** Complete entering a class.
     *  @param sym         The symbol of the class to be completed.
     */
    public void complete(Symbol sym) throws CompletionFailure {
        // <editor-fold defaultstate="collapsed">
    	try {
    	DEBUG.P(this,"complete(Symbol sym)");
    	DEBUG.P("sym="+sym+"  sym.kind="+Kinds.toString(sym.kind)+" isFirst="+isFirst+"  completionEnabled="+completionEnabled);

        // Suppress some (recursive) MemberEnter invocations
        if (!completionEnabled) {
            // Re-install same completer for next time around and return.
            assert (sym.flags() & Flags.COMPOUND) == 0;
            sym.completer = this;
            return;
        }

        ClassSymbol c = (ClassSymbol)sym;
        ClassType ct = (ClassType)c.type;
        Env<AttrContext> env = enter.typeEnvs.get(c);
        JCClassDecl tree = (JCClassDecl)env.tree;
        boolean wasFirst = isFirst;
        isFirst = false;
        
        DEBUG.P("c.owner.kind="+Kinds.toString(c.owner.kind));
        DEBUG.P("env="+env);
		// </editor-fold>

        JavaFileObject prev = log.useSource(env.toplevel.sourcefile);
        try {
            // <editor-fold defaultstate="collapsed">
            // Save class environment for later member enter (2) processing.
            halfcompleted.append(env);

            // If this is a toplevel-class, make sure any preceding import
            // clauses have been seen.
            if (c.owner.kind == PCK) {
                memberEnter(env.toplevel, env.enclosing(JCTree.TOPLEVEL));
                todo.append(env);
            }


        	DEBUG.P(2);
        	DEBUG.P("***JCTree.TOPLEVEL MemberEnter完***");
        	DEBUG.P("--------------------------------------");
        	DEBUG.P("env.toplevel.packge               ="+env.toplevel.packge);
        	DEBUG.P("env.toplevel.packge.members_field ="+env.toplevel.packge.members_field);
        	DEBUG.P("toplevel.env.info.scope           ="+env.enclosing(JCTree.TOPLEVEL).info.scope);
        	DEBUG.P("env.toplevel.namedImportScope     ="+env.toplevel.namedImportScope);
        	DEBUG.P("env.toplevel.starImportScope      ="+env.toplevel.starImportScope);
        	DEBUG.P(2);
       	


            // Mark class as not yet attributed.
            c.flags_field |= UNATTRIBUTED;
            
            DEBUG.P("c="+c);
            DEBUG.P("c.flags_field="+Flags.toString(c.flags_field));
            DEBUG.P("c.owner.kind="+Kinds.toString(c.owner.kind));
        	
        
            if (c.owner.kind == TYP)
                c.owner.complete();
                
            

            // create an environment for evaluating the base clauses
            Env<AttrContext> baseEnv = baseEnv(tree, env);
            
            

            //DEBUG.P("env="+env);
            //DEBUG.P("baseEnv="+env);
            DEBUG.P("tree.extending="+tree.extending);
            DEBUG.P("ct.supertype_field前="+ct.supertype_field);
			DEBUG.P("tree.mods.flags="+Flags.toString(tree.mods.flags));
            // Determine supertype.
            Type supertype =
                (tree.extending != null)
                ? attr.attribBase(tree.extending, baseEnv, true, false, true)
                : ((tree.mods.flags & Flags.ENUM) != 0 && !target.compilerBootstrap(c))
                ? attr.attribBase(enumBase(tree.pos, c), baseEnv,
                                  true, false, false)//枚举类型不能带extends，所以不用检查继承，所以最后一个参数是false
                : (c.fullname == names.java_lang_Object)
                ? Type.noType
                : syms.objectType;
            ct.supertype_field = supertype;
            //DEBUG.P("ct.supertype_field后="+ct.supertype_field);
            DEBUG.P("ct.supertype_field.tag="+TypeTags.toString(ct.supertype_field.tag));
            DEBUG.P("tree.mods.flags="+Flags.toString(tree.mods.flags));
            
            DEBUG.P("");
            DEBUG.P("ct.interfaces_field前="+ct.interfaces_field);
            DEBUG.P("tree.implementing="+tree.implementing);
            
            // </editor-fold>
            
            // <editor-fold defaultstate="collapsed">
            // Determine interfaces.
            ListBuffer<Type> interfaces = new ListBuffer<Type>();
            Set<Type> interfaceSet = new HashSet<Type>();
            List<JCExpression> interfaceTrees = tree.implementing;
			DEBUG.P("((tree.mods.flags & Flags.ENUM) != 0 && target.compilerBootstrap(c))="+((tree.mods.flags & Flags.ENUM) != 0 && target.compilerBootstrap(c)));
			/*加-target jsr14选项编译下面的类就可以使if为true
			package com.sun.tools;

			enum CompilerBootstrapEnumTest {
				A,
				B,
				C;
			}
			*/
            //枚举类型默认实现了java.lang.Comparable与java.io.Serializable接口
            if ((tree.mods.flags & Flags.ENUM) != 0 && target.compilerBootstrap(c)) {
                // add interface Comparable<T>
                interfaceTrees =
                    interfaceTrees.prepend(make.Type(new ClassType(syms.comparableType.getEnclosingType(),
                                                                   List.of(c.type),
                                                                   syms.comparableType.tsym)));
                // add interface Serializable
                interfaceTrees =
                    interfaceTrees.prepend(make.Type(syms.serializableType));
            }
            for (JCExpression iface : interfaceTrees) {
                Type i = attr.attribBase(iface, baseEnv, false, true, true);
				DEBUG.P("i="+i);
				DEBUG.P("i.tag="+TypeTags.toString(i.tag));
                if (i.tag == CLASS) {
                    interfaces.append(i);
                    chk.checkNotRepeated(iface.pos(), types.erasure(i), interfaceSet);
                }
            }
            if ((c.flags_field & ANNOTATION) != 0)
                ct.interfaces_field = List.of(syms.annotationType);
            else
                ct.interfaces_field = interfaces.toList();
            DEBUG.P("");
            DEBUG.P("ct.interfaces_field后="+ct.interfaces_field);
            DEBUG.P("c.fullname="+c.fullname);    
            //java.lang.Object没有超类，也不实现任何接口
            if (c.fullname == names.java_lang_Object) {
                if (tree.extending != null) {
                    chk.checkNonCyclic(tree.extending.pos(),
                                       supertype);
                    ct.supertype_field = Type.noType;
                }
                else if (tree.implementing.nonEmpty()) {
                    chk.checkNonCyclic(tree.implementing.head.pos(),
                                       ct.interfaces_field.head);
                    ct.interfaces_field = List.nil();
                }
            }
            // </editor-fold>
            
            // <editor-fold defaultstate="collapsed">
            // Annotations.
            // In general, we cannot fully process annotations yet,  but we
            // can attribute the annotation types and then check to see if the
            // @Deprecated annotation is present.
            attr.attribAnnotationTypes(tree.mods.annotations, baseEnv);
            if (hasDeprecatedAnnotation(tree.mods.annotations))
                c.flags_field |= DEPRECATED;
                
            DEBUG.P("c.flags_field="+Flags.toString(c.flags_field));
            
            annotateLater(tree.mods.annotations, baseEnv, c);
            
            DEBUG.P(3);
            //DEBUG.P("baseEnv="+baseEnv);
            //DEBUG.P("tree.typarams="+tree.typarams);
            
            
            attr.attribTypeVariables(tree.typarams, baseEnv);
            
            DEBUG.P("c.type="+c.type);
            DEBUG.P("c.type.tag="+TypeTags.toString(c.type.tag));
            
            //对同一个type在很多地方都进行了相同的循环检测，
			//直到type.tsym.flags_field含有ACYCLIC标志为止，
			//这一点是否有改进的空间？
            chk.checkNonCyclic(tree.pos(), c.type);
            
            DEBUG.P("c="+c);
            DEBUG.P("c.name="+c.name);
            DEBUG.P("c.flags()="+Flags.toString(c.flags()));
            // Add default constructor if needed.
            if ((c.flags() & INTERFACE) == 0 &&
                !TreeInfo.hasConstructors(tree.defs)) {
                List<Type> argtypes = List.nil();
                List<Type> typarams = List.nil();
                List<Type> thrown = List.nil();
                long ctorFlags = 0;
                boolean based = false;
                DEBUG.P("c.name.len="+c.name.len);
                if (c.name.len == 0) {
                    JCNewClass nc = (JCNewClass)env.next.tree;
                    DEBUG.P("nc.constructor="+nc.constructor);
                    if (nc.constructor != null) {
                        Type superConstrType = types.memberType(c.type,
                                                                nc.constructor);
                        argtypes = superConstrType.getParameterTypes();
                        typarams = superConstrType.getTypeArguments();
                        ctorFlags = nc.constructor.flags() & VARARGS;
                        if (nc.encl != null) {
                            argtypes = argtypes.prepend(nc.encl.type);
                            based = true;
                        }
                        thrown = superConstrType.getThrownTypes();
                    }
                }
                JCTree constrDef = DefaultConstructor(make.at(tree.pos), c,
                                                    typarams, argtypes, thrown,
                                                    ctorFlags, based);
                tree.defs = tree.defs.prepend(constrDef);
            }
			// </editor-fold>
            
            // <editor-fold defaultstate="collapsed">
            // If this is a class, enter symbols for this and super into
            // current scope.
            DEBUG.P("c.flags_field="+Flags.toString(c.flags_field));
            if ((c.flags_field & INTERFACE) == 0) {
                VarSymbol thisSym =
                    new VarSymbol(FINAL | HASINIT, names._this, c.type, c);
                thisSym.pos = Position.FIRSTPOS;
                
                DEBUG.P("thisSym="+thisSym);
                DEBUG.P("env.info.scope="+env.info.scope);
                
                env.info.scope.enter(thisSym);
                DEBUG.P("env.info.scope="+env.info.scope);
                DEBUG.P("ct.supertype_field="+ct.supertype_field);
				DEBUG.P("ct.supertype_field.tag="+TypeTags.toString(ct.supertype_field.tag));
                
                if (ct.supertype_field.tag == CLASS) {
                    VarSymbol superSym =
                        new VarSymbol(FINAL | HASINIT, names._super,
                                      ct.supertype_field, c);
                    superSym.pos = Position.FIRSTPOS;
                    
                    DEBUG.P("superSym="+superSym);
                	DEBUG.P("env.info.scope="+env.info.scope);
                
                    env.info.scope.enter(superSym);
                    
                    DEBUG.P("env.info.scope="+env.info.scope);
                }
            }
            
            DEBUG.P("checkClash="+checkClash);
            DEBUG.P("c.owner.kind="+Kinds.toString(c.owner.kind));
            DEBUG.P("c.owner="+c.owner);
            DEBUG.P("c.fullname="+c.fullname);

            // check that no package exists with same fully qualified name,
            // but admit classes in the unnamed package which have the same
            // name as a top-level package.
            //在执行reader.packageExists(c.fullname))时，也会把一类名当成一个包名加
            //到Map<Name, PackageSymbol> packages
            //注:成员类不用检测
            DEBUG.P("syms.packages.size="+syms.packages.size()+" keySet="+syms.packages.keySet());

			//同时编译test/memberEnter/EnumTest.java
			//test/memberEnter/Clash/ClassA.java可测试出错误
			DEBUG.P("reader.packageExists(c.fullname)="+reader.packageExists(c.fullname));
            if (checkClash &&
                c.owner.kind == PCK && c.owner != syms.unnamedPackage &&
                reader.packageExists(c.fullname))
                {
                    log.error(tree.pos, "clash.with.pkg.of.same.name", c);
                }
            DEBUG.P("syms.packages.size="+syms.packages.size()+" keySet="+syms.packages.keySet());
		// </editor-fold>
        } catch (CompletionFailure ex) {
            chk.completionError(tree.pos(), ex);
        } finally {
            log.useSource(prev);
        }
        
        // <editor-fold defaultstate="collapsed">
        DEBUG.P("wasFirst="+wasFirst);
        DEBUG.P("halfcompleted.nonEmpty()="+halfcompleted.nonEmpty());
        if(halfcompleted.nonEmpty()) 
        DEBUG.P("halfcompleted.size="+halfcompleted.size());
        
        // Enter all member fields and methods of a set of half completed
        // classes in a second phase.
        if (wasFirst) {
        	//注:成员类在MemberEnter阶段不用finish
            try {
                while (halfcompleted.nonEmpty()) {
                    finish(halfcompleted.next());
                }
            } finally {
                isFirst = true;
            }

            // commit pending annotations
            annotate.flush();
        }
        
    	} finally {
    	//DEBUG.P("sym.members_field="+((ClassSymbol)sym).members_field);
    	DEBUG.P(3,this,"complete(Symbol sym)");
    	}
        
        // </editor-fold>
    }

    private Env<AttrContext> baseEnv(JCClassDecl tree, Env<AttrContext> env) {
    	DEBUG.P(this,"baseEnv(2)");
    	DEBUG.P("env="+env);
    	DEBUG.P("tree.sym="+tree.sym);
    	DEBUG.P("env.enclClass.sym="+env.enclClass.sym);
        Scope typaramScope = new Scope(tree.sym);
        if (tree.typarams != null)
            for (List<JCTypeParameter> typarams = tree.typarams;
                 typarams.nonEmpty();
                 typarams = typarams.tail) {
                 	DEBUG.P("typarams.head.type.tsym=     "+typarams.head.type.tsym);
                typaramScope.enter(typarams.head.type.tsym);
            }

		/*
		//比如类型变量不能extends成员类
		class MemberEnterTest<T,V extends MemberClassB> { //找不到符号
			public class MemberClassB{}
		}
		*/
        Env<AttrContext> outer = env.outer; // the base clause can't see members of this class
        Env<AttrContext> localEnv = outer.dup(tree, outer.info.dup(typaramScope));
        localEnv.baseClause = true;
        localEnv.outer = outer;
        localEnv.info.isSelfCall = false;
        //localEnv与env是并列的，但是enclClass换了，
        //localEnv.enclClass=env.outer.enclClass
        DEBUG.P("localEnv="+localEnv);
        DEBUG.P("localEnv.enclClass.sym="+localEnv.enclClass.sym);
        DEBUG.P(0,this,"baseEnv(2)");
        return localEnv;
    }

    /** Enter member fields and methods of a class
     *  @param env        the environment current for the class block.
     */
    private void finish(Env<AttrContext> env) {
    	DEBUG.P(this,"finish(Env<AttrContext> env)");
    	DEBUG.P("env="+env);
    	
        JavaFileObject prev = log.useSource(env.toplevel.sourcefile);
        try {
            JCClassDecl tree = (JCClassDecl)env.tree;
            finishClass(tree, env);
        } finally {
            log.useSource(prev);
            
            DEBUG.P(0,this,"finish(Env<AttrContext> env)");
        }
    }

    /** Generate a base clause for an enum type.
     *  @param pos              The position for trees and diagnostics, if any
     *  @param c                The class symbol of the enum
     */
    private JCExpression enumBase(int pos, ClassSymbol c) {
    	DEBUG.P(this,"enumBase(2)");
        JCExpression result = make.at(pos).
            TypeApply(make.QualIdent(syms.enumSym),
                      List.<JCExpression>of(make.Type(c.type)));
        DEBUG.P("result="+result);
		//result=.java.lang.Enum<.test.memberEnter.EnumTest>
		//为什么最前面是"."号呢？因为在enterPackage时，java包、test包的owner
		//都是rootPackage，调用make.QualIdent时递归到rootPackage时才结束
        DEBUG.P(0,this,"enumBase(2)");
        return result;
    }

/* ***************************************************************************
 * tree building
 ****************************************************************************/

    /** Generate default constructor for given class. For classes different
     *  from java.lang.Object, this is:
     *
     *    c(argtype_0 x_0, ..., argtype_n x_n) throws thrown {
     *      super(x_0, ..., x_n)
     *    }
     *
     *  or, if based == true:
     *
     *    c(argtype_0 x_0, ..., argtype_n x_n) throws thrown {
     *      x_0.super(x_1, ..., x_n)
     *    }
     *
     *  @param make     The tree factory.
     *  @param c        The class owning the default constructor.
     *  @param argtypes The parameter types of the constructor.
     *  @param thrown   The thrown exceptions of the constructor.
     *  @param based    Is first parameter a this$n?
     */
    JCTree DefaultConstructor(TreeMaker make,
                            ClassSymbol c,
                            List<Type> typarams,
                            List<Type> argtypes,
                            List<Type> thrown,
                            long flags,
                            boolean based) {
        DEBUG.P(this,"DefaultConstructor(7)");                    	
        List<JCVariableDecl> params = make.Params(argtypes, syms.noSymbol);
        DEBUG.P("params="+params);
        DEBUG.P("flags="+Flags.toString(flags));
        List<JCStatement> stats = List.nil();
        if (c.type != syms.objectType)
            stats = stats.prepend(SuperCall(make, typarams, params, based));
        
        DEBUG.P("stats="+stats);
        
        if ((c.flags() & ENUM) != 0 &&
            (types.supertype(c.type).tsym == syms.enumSym ||
             target.compilerBootstrap(c))) {
            // constructors of true enums are private
            flags = (flags & ~AccessFlags) | PRIVATE | GENERATEDCONSTR;
        } else
            flags |= (c.flags() & AccessFlags) | GENERATEDCONSTR;
        if (c.name.len == 0) flags |= ANONCONSTR;
        JCTree result = make.MethodDef(
            make.Modifiers(flags),
            names.init,
            null,
            make.TypeParams(typarams),
            params,
            make.Types(thrown),
            make.Block(0, stats),
            null);
        
        DEBUG.P("flags="+Flags.toString(flags));
        DEBUG.P("result="+result);
        DEBUG.P(0,this,"DefaultConstructor(7)");
        return result;
    }

    /** Generate call to superclass constructor. This is:
     *
     *    super(id_0, ..., id_n)
     *
     * or, if based == true
     *
     *    id_0.super(id_1,...,id_n)
     *
     *  where id_0, ..., id_n are the names of the given parameters.
     *
     *  @param make    The tree factory
     *  @param params  The parameters that need to be passed to super
     *  @param typarams  The type parameters that need to be passed to super
     *  @param based   Is first parameter a this$n?
     */
    JCExpressionStatement SuperCall(TreeMaker make,
                   List<Type> typarams,
                   List<JCVariableDecl> params,
                   boolean based) {
        
        try {//我加上的
		DEBUG.P(this,"SuperCall(3)");
		DEBUG.P("typarams="+typarams);
		DEBUG.P("params="+params);
		DEBUG.P("based="+based);

        
        JCExpression meth;
        if (based) {
            meth = make.Select(make.Ident(params.head), names._super);
            params = params.tail;
        } else {
            meth = make.Ident(names._super);
        }
        
        DEBUG.P("meth="+meth);
        
        List<JCExpression> typeargs = typarams.nonEmpty() ? make.Types(typarams) : null;
        return make.Exec(make.Apply(typeargs, meth, make.Idents(params)));
        
        
        }finally{//我加上的
		DEBUG.P(0,this,"SuperCall(3)");
		}
    }
}
