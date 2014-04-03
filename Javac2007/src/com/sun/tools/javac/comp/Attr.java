/*
 * @(#)Attr.java	1.224 07/03/21
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
import javax.lang.model.element.ElementKind;
import javax.tools.JavaFileObject;

import com.sun.tools.javac.code.*;
import com.sun.tools.javac.jvm.*;
import com.sun.tools.javac.tree.*;
import com.sun.tools.javac.util.*;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;
import com.sun.tools.javac.util.List;

import com.sun.tools.javac.jvm.Target;
import com.sun.tools.javac.code.Symbol.*;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.code.Type.*;

import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.TreeVisitor;
import com.sun.source.util.SimpleTreeVisitor;

import static com.sun.tools.javac.code.Flags.*;
import static com.sun.tools.javac.code.Kinds.*;
import static com.sun.tools.javac.code.TypeTags.*;

/** This is the main context-dependent analysis phase in GJC. It
 *  encompasses name resolution, type checking and constant folding as
 *  subtasks. Some subtasks involve auxiliary classes.
 *  @see Check
 *  @see Resolve
 *  @see ConstFold
 *  @see Infer
 *
 *  <p><b>This is NOT part of any API supported by Sun Microsystems.  If
 *  you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 */
@Version("@(#)Attr.java	1.224 07/03/21")
//只有visitCase, visitCatch, visitLetExpr, visitModifiers, visitTopLevel, visitTypeBoundKind没有覆盖
public class Attr extends JCTree.Visitor {
    private static my.Debug DEBUG=new my.Debug(my.Debug.Attr);//我加上的
	
    protected static final Context.Key<Attr> attrKey =
        new Context.Key<Attr>();

    final Name.Table names;
    final Log log;
    final Symtab syms;
    final Resolve rs;
    final Check chk;
    final MemberEnter memberEnter;
    final TreeMaker make;
    final ConstFold cfolder;
    final Enter enter;
    final Target target;
    final Types types;
    final Annotate annotate;

    public static Attr instance(Context context) {
        Attr instance = context.get(attrKey);
        if (instance == null)
            instance = new Attr(context);
        return instance;
    }

    protected Attr(Context context) {
    	DEBUG.P(this,"Attr(1)");
    	
        context.put(attrKey, this);

        names = Name.Table.instance(context);
        log = Log.instance(context);
        syms = Symtab.instance(context);
        rs = Resolve.instance(context);
        chk = Check.instance(context);
        memberEnter = MemberEnter.instance(context);
        make = TreeMaker.instance(context);
        enter = Enter.instance(context);
        cfolder = ConstFold.instance(context);
        target = Target.instance(context);
        types = Types.instance(context);
        annotate = Annotate.instance(context);

        Options options = Options.instance(context);

        Source source = Source.instance(context);
        allowGenerics = source.allowGenerics();
        allowVarargs = source.allowVarargs();
        allowEnums = source.allowEnums();
        allowBoxing = source.allowBoxing();
        allowCovariantReturns = source.allowCovariantReturns();
        allowAnonOuterThis = source.allowAnonOuterThis();
        relax = (options.get("-retrofit") != null ||
                 options.get("-relax") != null);
        useBeforeDeclarationWarning = options.get("useBeforeDeclarationWarning") != null;
        
        DEBUG.P(0,this,"Attr(1)");
    }

    /** Switch: relax some constraints for retrofit mode.
     */
    boolean relax;

    /** Switch: support generics?
     */
    boolean allowGenerics;

    /** Switch: allow variable-arity methods.
     */
    boolean allowVarargs;

    /** Switch: support enums?
     */
    boolean allowEnums;

    /** Switch: support boxing and unboxing?
     */
    boolean allowBoxing;

    /** Switch: support covariant result types?
     */
    boolean allowCovariantReturns;

    /** Switch: allow references to surrounding object from anonymous
     * objects during constructor call?
     */
    boolean allowAnonOuterThis;

    /**
     * Switch: warn about use of variable before declaration?
     * RFE: 6425594
     */
    boolean useBeforeDeclarationWarning;

    /** Check kind and type of given tree against protokind and prototype.
     *  If check succeeds, store type in tree and return it.
     *  If check fails, store errType in tree and return it.
     *  No checks are performed if the prototype is a method type.
     *  Its not necessary in this case since we know that kind and type
     *  are correct.
     *
     *  @param tree     The tree whose kind and type is checked
     *  @param owntype  The computed type of the tree
     *  @param ownkind  The computed kind of the tree
     *  @param pkind    The expected kind (or: protokind) of the tree
     *  @param pt       The expected type (or: prototype) of the tree
     */
    Type check(JCTree tree, Type owntype, int ownkind, int pkind, Type pt) {
    	DEBUG.P(this,"check(5)");
    	DEBUG.P("tree.type="+tree.type);
    	DEBUG.P("ownkind="+Kinds.toString(ownkind));
    	DEBUG.P("owntype.tag="+TypeTags.toString(owntype.tag));
    	DEBUG.P("pkind="+Kinds.toString(pkind));
    	DEBUG.P("pt.tag="+TypeTags.toString(pt.tag));

        if (owntype.tag != ERROR && pt.tag != METHOD && pt.tag != FORALL) {
        	//如果ownkind所代表的Kinds在pkind中没有，则报错
        	/*比如：如果ownkind是VAR,而pkind是PCK与TYP
        	bin\mysrc\my\test\Test.java:3: 意外的类型
			需要： 类、软件包
			找到： 变量
			*/
			//ownkind只能代表单个kind，而pkind可以是多个kind的复合
			//从下面的kindName与kindNames也可能看出来
            if ((ownkind & ~pkind) == 0) {
                owntype = chk.checkType(tree.pos(), owntype, pt);
            } else {
                log.error(tree.pos(), "unexpected.type",
                          Resolve.kindNames(pkind),
                          Resolve.kindName(ownkind));
                owntype = syms.errType;
            }
        }
        tree.type = owntype;
        DEBUG.P(0,this,"check(5)");
        return owntype;
    }

    /** Is given blank final variable assignable, i.e. in a scope where it
     *  may be assigned to even though it is final?
     *  @param v      The blank final variable.
     *  @param env    The current environment.
     */
    boolean isAssignableAsBlankFinal(VarSymbol v, Env<AttrContext> env) {
		try {//我加上的
        DEBUG.P(this,"isAssignableAsBlankFinal(2)");
		

        Symbol owner = env.info.scope.owner;

		DEBUG.P("v="+v);
		DEBUG.P("v.flags()="+Flags.toString(v.flags()));
		DEBUG.P("v.owner="+v.owner);
		DEBUG.P("owner="+owner);
		DEBUG.P("owner.flags()="+Flags.toString(owner.flags()));
           // owner refers to the innermost variable, method or
           // initializer block declaration at this point.
        return
            v.owner == owner
            ||
            ((owner.name == names.init ||    // i.e. we are in a constructor
              owner.kind == VAR ||           // i.e. we are in a variable initializer
              (owner.flags() & BLOCK) != 0)  // i.e. we are in an initializer block
             &&
             v.owner == owner.owner
             &&
             ((v.flags() & STATIC) != 0) == Resolve.isStatic(env));

		}finally{//我加上的
            DEBUG.P(0,this,"isAssignableAsBlankFinal(2)");
        }
    }

    /** Check that variable can be assigned to.
     *  @param pos    The current source code position.
     *  @param v      The assigned varaible
     *  @param base   If the variable is referred to in a Select, the part
     *                to the left of the `.', null otherwise.
     *  @param env    The current environment.
     */
    void checkAssignable(DiagnosticPosition pos, VarSymbol v, JCTree base, Env<AttrContext> env) {
		DEBUG.P(this,"checkAssignable(4)");
		DEBUG.P("v="+v);
		DEBUG.P("v.flags()="+Flags.toString(v.flags()));
		DEBUG.P("base="+base);

		if(base != null) {
			DEBUG.P("base.tag="+base.myTreeTag());
			DEBUG.P("TreeInfo.name(base)="+TreeInfo.name(base));
		}

        if ((v.flags() & FINAL) != 0 &&
            ((v.flags() & HASINIT) != 0
             ||
             !((base == null ||
               (base.tag == JCTree.IDENT && TreeInfo.name(base) == names._this)) &&
               isAssignableAsBlankFinal(v, env)))) {
            log.error(pos, "cant.assign.val.to.final.var", v);
        }

		DEBUG.P(0,this,"checkAssignable(4)");
    }

    /** Does tree represent a static reference to an identifier?
     *  It is assumed that tree is either a SELECT or an IDENT.
     *  We have to weed out selects from non-type names here.
     *  @param tree    The candidate tree.
     */
    boolean isStaticReference(JCTree tree) {
        if (tree.tag == JCTree.SELECT) {
            Symbol lsym = TreeInfo.symbol(((JCFieldAccess) tree).selected);
            if (lsym == null || lsym.kind != TYP) {
                return false;
            }
        }
        return true;
    }

    /** Is this symbol a type?
     */
    static boolean isType(Symbol sym) {
        return sym != null && sym.kind == TYP;
    }

    /** The current `this' symbol.
     *  @param env    The current environment.
     */
    Symbol thisSym(DiagnosticPosition pos, Env<AttrContext> env) {
		try {//我加上的
            DEBUG.P(this,"thisSym(2)");
        return rs.resolveSelf(pos, env, env.enclClass.sym, names._this);

		}finally{//我加上的
            DEBUG.P(0,this,"thisSym(2)");
        }
    }

    /** Attribute a parsed identifier.
     * @param tree Parsed identifier name
     * @param topLevel The toplevel to use
     */
    public Symbol attribIdent(JCTree tree, JCCompilationUnit topLevel) {
        Env<AttrContext> localEnv = enter.topLevelEnv(topLevel);
        localEnv.enclClass = make.ClassDef(make.Modifiers(0),
                                           syms.errSymbol.name,
                                           null, null, null, null);
        localEnv.enclClass.sym = syms.errSymbol;
        return tree.accept(identAttributer, localEnv);
    }
    // where
        private TreeVisitor<Symbol,Env<AttrContext>> identAttributer = new IdentAttributer();
        private class IdentAttributer extends SimpleTreeVisitor<Symbol,Env<AttrContext>> {
            @Override
            public Symbol visitMemberSelect(MemberSelectTree node, Env<AttrContext> env) {
                Symbol site = visit(node.getExpression(), env);
                if (site.kind == ERR)
                    return site;
                Name name = (Name)node.getIdentifier();
                if (site.kind == PCK) {
                    env.toplevel.packge = (PackageSymbol)site;
                    return rs.findIdentInPackage(env, (TypeSymbol)site, name, TYP | PCK);
                } else {
                    env.enclClass.sym = (ClassSymbol)site;
                    return rs.findMemberType(env, site.asType(), name, (TypeSymbol)site);
                }
            }
    
            @Override
            public Symbol visitIdentifier(IdentifierTree node, Env<AttrContext> env) {
                return rs.findIdent(env, (Name)node.getName(), TYP | PCK);
            }
        }

    public Type coerce(Type etype, Type ttype) {
        return cfolder.coerce(etype, ttype);
    }

    public Type attribType(JCTree node, TypeSymbol sym) {
        Env<AttrContext> env = enter.typeEnvs.get(sym);
        Env<AttrContext> localEnv = env.dup(node, env.info.dup());
        return attribTree(node, localEnv, Kinds.TYP, Type.noType);
    }  

    public Env<AttrContext> attribExprToTree(JCTree expr, Env<AttrContext> env, JCTree tree) {
        breakTree = tree;
        JavaFileObject prev = log.useSource(null);
        try {
            attribExpr(expr, env);
        } catch (BreakAttr b) {
            return b.env;
        } finally {
            breakTree = null;
            log.useSource(prev);
        }
        return env;
    }    

    public Env<AttrContext> attribStatToTree(JCTree stmt, Env<AttrContext> env, JCTree tree) {
        breakTree = tree;
        JavaFileObject prev = log.useSource(null);
        try {
            attribStat(stmt, env);
        } catch (BreakAttr b) {
            return b.env;
        } finally {
            breakTree = null;
            log.useSource(prev);
        }
        return env;
    }
    
    private JCTree breakTree = null;
    
    private static class BreakAttr extends RuntimeException {
        static final long serialVersionUID = -6924771130405446405L;
        private Env<AttrContext> env;
        private BreakAttr(Env<AttrContext> env) {
            this.env = env;
        }
    }
   

/* ************************************************************************
 * Visitor methods
 *************************************************************************/
//
    /** Visitor argument: the current environment.
     */
    Env<AttrContext> env;

    /** Visitor argument: the currently expected proto-kind.
     */
    int pkind;

    /** Visitor argument: the currently expected proto-type.
     */
    Type pt;

    /** Visitor result: the computed type.
     */
    Type result;

    /** Visitor method: attribute a tree, catching any completion failure
     *  exceptions. Return the tree's type.
     *
     *  @param tree    The tree to be visited.
     *  @param env     The environment visitor argument.
     *  @param pkind   The protokind visitor argument.
     *  @param pt      The prototype visitor argument.
     */
    Type attribTree(JCTree tree, Env<AttrContext> env, int pkind, Type pt) {
    	DEBUG.P(this,"attribTree(4)");
    	DEBUG.P("tree="+tree);
    	DEBUG.P("tree.tag="+tree.myTreeTag());
    	//DEBUG.P("env="+env);
    	DEBUG.P("pkind="+Kinds.toString(pkind));
    	DEBUG.P("pt="+pt);
    	DEBUG.P("pt.tag="+TypeTags.toString(pt.tag));
    	
        Env<AttrContext> prevEnv = this.env;
        int prevPkind = this.pkind;
        Type prevPt = this.pt;
        try {
            this.env = env;
            this.pkind = pkind;
            this.pt = pt;
            tree.accept(this);
            if (tree == breakTree) //当breakTree==tree==null时
                throw new BreakAttr(env);//是java.lang.RuntimeException的子类
            return result;
        } catch (CompletionFailure ex) {
            tree.type = syms.errType;
            return chk.completionError(tree.pos(), ex);
        } finally {
            this.env = prevEnv;
            this.pkind = prevPkind;
            this.pt = prevPt;

				
				DEBUG.P("pkind="+Kinds.toString(pkind));
				DEBUG.P("tree     ="+tree);
    			DEBUG.P("tree.tag ="+tree.myTreeTag());
			if(tree.type!=null)  {
				DEBUG.P("tree.type          ="+tree.type);
				DEBUG.P("tree.type.tsym     ="+tree.type.tsym);
				if(tree.type.tsym!=null)
					DEBUG.P("tree.type.tsym.type="+tree.type.tsym.type);
			} else 
				DEBUG.P("tree.type=null");
            
            DEBUG.P(0,this,"attribTree(4)");
        }
    }

    /** Derived visitor method: attribute an expression tree.
     */
    public Type attribExpr(JCTree tree, Env<AttrContext> env, Type pt) {
    	try {//我加上的
		DEBUG.P(this,"attribExpr(3)");
		
        return attribTree(tree, env, VAL, pt.tag != ERROR ? pt : Type.noType);
		
		}finally{//我加上的
		DEBUG.P(0,this,"attribExpr(3)");
		}
    }

    /** Derived visitor method: attribute an expression tree with
     *  no constraints on the computed type.
     */
    Type attribExpr(JCTree tree, Env<AttrContext> env) {
    	try {//我加上的
		DEBUG.P(this,"attribExpr(2)");
		
        return attribTree(tree, env, VAL, Type.noType);
		
		}finally{//我加上的
		DEBUG.P(0,this,"attribExpr(2)");
		}
        
    }

    /** Derived visitor method: attribute a type tree.
     */
    Type attribType(JCTree tree, Env<AttrContext> env) {
    	DEBUG.P(this,"attribType(2)");
        Type result = attribTree(tree, env, TYP, Type.noType);
        
        //DEBUG.P("result="+result);
        //DEBUG.P("result.tag="+TypeTags.toString(result.tag));
        DEBUG.P(0,this,"attribType(2)");
        return result;
    }

    /** Derived visitor method: attribute a statement or definition tree.
     */
    public Type attribStat(JCTree tree, Env<AttrContext> env) {
    	try {//我加上的
		DEBUG.P(this,"attribStat(2)");
		
        return attribTree(tree, env, NIL, Type.noType);
        
        }finally{//我加上的
		DEBUG.P(1,this,"attribStat(2)");
		}
    }

    /** Attribute a list of expressions, returning a list of types.
     */
    List<Type> attribExprs(List<JCExpression> trees, Env<AttrContext> env, Type pt) {
		DEBUG.P(this,"attribExprs(3)");
        ListBuffer<Type> ts = new ListBuffer<Type>();
        for (List<JCExpression> l = trees; l.nonEmpty(); l = l.tail)
            ts.append(attribExpr(l.head, env, pt));

		DEBUG.P(0,this,"attribExprs(3)");
        return ts.toList();
    }

    /** Attribute a list of statements, returning nothing.
     */
    <T extends JCTree> void attribStats(List<T> trees, Env<AttrContext> env) {
    	DEBUG.P(this,"attribStats(2)");
        for (List<T> l = trees; l.nonEmpty(); l = l.tail)
            attribStat(l.head, env);
        DEBUG.P(0,this,"attribStats(2)");
    }

    /** Attribute the arguments in a method call, returning a list of types.
     */
    List<Type> attribArgs(List<JCExpression> trees, Env<AttrContext> env) {
    	try {//我加上的
		DEBUG.P(this,"attribArgs(2)");
		DEBUG.P("trees="+trees);
		//DEBUG.P("env="+env);
		
        ListBuffer<Type> argtypes = new ListBuffer<Type>();
        for (List<JCExpression> l = trees; l.nonEmpty(); l = l.tail)
            argtypes.append(chk.checkNonVoid(
                l.head.pos(), types.upperBound(attribTree(l.head, env, VAL, Infer.anyPoly))));
        return argtypes.toList();
        
        }finally{//我加上的
		DEBUG.P(0,this,"attribArgs(2)");
		}
    }

    /** Attribute a type argument list, returning a list of types.
     */
    List<Type> attribTypes(List<JCExpression> trees, Env<AttrContext> env) {
    	DEBUG.P(this,"attribTypes(2)");
    	DEBUG.P("trees="+trees);
		//DEBUG.P("env="+env);
        ListBuffer<Type> argtypes = new ListBuffer<Type>();
        for (List<JCExpression> l = trees; l.nonEmpty(); l = l.tail)
            argtypes.append(chk.checkRefType(l.head.pos(), attribType(l.head, env)));
        
        DEBUG.P(0,this,"attribTypes(2)");
        return argtypes.toList();
    }
//

    /**
     * Attribute type variables (of generic classes or methods).
     * Compound types are attributed later in attribBounds.
     * @param typarams the type variables to enter
     * @param env      the current environment
     */
    //b10新增
    void attribTypeVariables(List<JCTypeParameter> typarams, Env<AttrContext> env) {
    	DEBUG.P(this,"attribTypeVariables(2)");
    	DEBUG.P("typarams="+typarams);
    	DEBUG.P("env="+env);
    	
    	/*注意:
		像class Test<S,P extends V, V extends InterfaceTest,T extends ExtendsTest,E extends ExtendsTest&InterfaceTest>
		这样的定义是合法的，
		虽然V在P之后，但P 先extends V也不会报错，
		因为所有的类型变量(这里是S, P, V, T, E)，在
		com.sun.tools.javac.comp.Enter===>visitTypeParameter(JCTypeParameter tree)
		方法中事先已加入与Test对应的Env里，如下所示为上面两个DEBUG.P()的结果:
		typarams=S,P extends V,V extends InterfaceTest,T extends ExtendsTest,E extends ExtendsTest & InterfaceTest
		env=Env(TK=CLASS EC=)[AttrContext[Scope[(nelems=5 owner=Test)E, T, V, P, S]],outer=Env(TK=COMPILATION_UNIT EC=)[AttrContext[Scope[(nelems=3 owner=test)Test, ExtendsTest, InterfaceTest]]]]
		
		当要生成类型变量P的bound时，因为JCTypeParameter.bounds=V，然后
		在env中查找，发现V在env的Scope存在，所以是可以超前引用V的，
		这主要是因为类型变量的解析和类型变量的bound的解析是分先后两个
		阶段进行的，但是把“P extends V”改成“P extends V2”，就会
		报“找不到符号”这个错误，因为V2不在env中，其他地方也找不到。
		*/
    	
        for (JCTypeParameter tvar : typarams) {
            TypeVar a = (TypeVar)tvar.type;
            DEBUG.P("a.tsym.name="+a.tsym.name);
            DEBUG.P("a.bound="+a.bound);
            DEBUG.P("tvar="+tvar);
    		DEBUG.P("tvar.bounds="+tvar.bounds);
            if (!tvar.bounds.isEmpty()) {
                List<Type> bounds = List.of(attribType(tvar.bounds.head, env));
                for (JCExpression bound : tvar.bounds.tail)
                    bounds = bounds.prepend(attribType(bound, env));
                DEBUG.P("bounds="+bounds);
                DEBUG.P("bounds.reverse()="+bounds.reverse());
                types.setBounds(a, bounds.reverse());
            } else {
                // if no bounds are given, assume a single bound of
                // java.lang.Object.
                types.setBounds(a, List.of(syms.objectType));
            }
            DEBUG.P("a.bound="+a.bound);DEBUG.P("");
        }
        for (JCTypeParameter tvar : typarams)
            chk.checkNonCyclic(tvar.pos(), (TypeVar)tvar.type);
        attribStats(typarams, env);
        
        DEBUG.P(0,this,"attribTypeVariables(2)");
    }
    
	//对形如:E extends ExtendsTest&InterfaceTest这样的类型变量进行attribClass
    void attribBounds(List<JCTypeParameter> typarams) {
    	DEBUG.P(this,"attribBounds(1)");
    	DEBUG.P("typarams="+typarams);
        for (JCTypeParameter typaram : typarams) {
            Type bound = typaram.type.getUpperBound();
            DEBUG.P("");
            DEBUG.P("typaram="+typaram);
            DEBUG.P("bound="+bound);
            if (bound != null) DEBUG.P("bound.tsym.className="+bound.tsym.getClass().getName());

            if (bound != null && bound.tsym instanceof ClassSymbol) {
                ClassSymbol c = (ClassSymbol)bound.tsym;
                DEBUG.P("bound.tsym.flags_field="+Flags.toString(c.flags_field));
                if ((c.flags_field & COMPOUND) != 0) {
                    assert (c.flags_field & UNATTRIBUTED) != 0 : c;
                    attribClass(typaram.pos(), c);
                }
            }
        }
        DEBUG.P(1,this,"attribBounds(1)");
    }

    /**
     * Attribute the type references in a list of annotations.
     */
    void attribAnnotationTypes(List<JCAnnotation> annotations,
                               Env<AttrContext> env) {
        DEBUG.P(this,"attribAnnotationTypes(2)");  
        DEBUG.P("env="+env);
        DEBUG.P("annotations="+annotations);                     	
        for (List<JCAnnotation> al = annotations; al.nonEmpty(); al = al.tail) {
            JCAnnotation a = al.head;
            attribType(a.annotationType, env);
        }
        DEBUG.P(0,this,"attribAnnotationTypes(2)");  
    }
	// <editor-fold defaultstate="collapsed">
    /** Attribute type reference in an `extends' or `implements' clause.
     *
     *  @param tree              The tree making up the type reference.
     *  @param env               The environment current at the reference.
     *  @param classExpected     true if only a class is expected here.
     *  @param interfaceExpected true if only an interface is expected here.
     */
     /*
    Type attribBase(JCTree tree,
                    Env<AttrContext> env,
                    boolean classExpected,
                    boolean interfaceExpected,
                    boolean checkExtensible) {
        try {//我加上的
		DEBUG.P(this,"attribBase(5)");
		DEBUG.P("tree="+tree);
		DEBUG.P("tree.getKind()="+tree.getKind());
		DEBUG.P("env="+env);
		DEBUG.P("classExpected="+classExpected);
		DEBUG.P("interfaceExpected="+interfaceExpected);
		DEBUG.P("checkExtensible="+checkExtensible);     
		
		   
        Type t = attribType(tree, env);

        DEBUG.P("t.tag="+TypeTags.toString(t.tag));

        if (t.tag == TYPEVAR && !classExpected && !interfaceExpected) {
            // check that type variable is already visible
            if (t.getUpperBound() == null) {
                log.error(tree.pos(), "illegal.forward.ref");
                return syms.errType;
            }
        } else {
            t = chk.checkClassType(tree.pos(), t, checkExtensible|!allowGenerics);
        }
        if (interfaceExpected && (t.tsym.flags() & INTERFACE) == 0) {
            log.error(tree.pos(), "intf.expected.here");
            // return errType is necessary since otherwise there might
            // be undetected cycles which cause attribution to loop
            return syms.errType;
        } else if (checkExtensible &&
                   classExpected &&
                   (t.tsym.flags() & INTERFACE) != 0) {
            log.error(tree.pos(), "no.intf.expected.here");
            return syms.errType;
        }
        if (checkExtensible &&
            ((t.tsym.flags() & FINAL) != 0)) {
            log.error(tree.pos(),
                      "cant.inherit.from.final", t.tsym);
        }
        chk.checkNonCyclic(tree.pos(), t);
        return t;
        
        }finally{//我加上的
		DEBUG.P(0,this,"attribBase(5)");
		}
    }*/
// </editor-fold>
    /** Attribute type reference in an `extends' or `implements' clause.
     *
     *  @param tree              The tree making up the type reference.
     *  @param env               The environment current at the reference.
     *  @param classExpected     true if only a class is expected here.
     *  @param interfaceExpected true if only an interface is expected here.
     */
    //b10
    Type attribBase(JCTree tree,
                    Env<AttrContext> env,
                    boolean classExpected,
                    boolean interfaceExpected,
                    boolean checkExtensible) {
        try {//我加上的
        DEBUG.P(this,"attribBase(5)");
        DEBUG.P("tree="+tree);
        DEBUG.P("tree.tag="+tree.myTreeTag());
        DEBUG.P("env="+env);
        DEBUG.P("classExpected="+classExpected);
        DEBUG.P("interfaceExpected="+interfaceExpected);
        DEBUG.P("checkExtensible="+checkExtensible);  
		
        Type t = attribType(tree, env);
        
        DEBUG.P("t.tag="+TypeTags.toString(t.tag));
        
        return checkBase(t, tree, env, classExpected, interfaceExpected, checkExtensible);
        
        }finally{//我加上的
        DEBUG.P(0,this,"attribBase(5)");
        }
    }
    //b10
    Type checkBase(Type t,
                   JCTree tree,
                   Env<AttrContext> env,
                   boolean classExpected,
                   boolean interfaceExpected,
                   boolean checkExtensible) {
        try {//我加上的
        DEBUG.P(this,"checkBase(6)");
        DEBUG.P("t.tag="+TypeTags.toString(t.tag));
        DEBUG.P("tree="+tree);
        DEBUG.P("env="+env);
        DEBUG.P("classExpected="+classExpected);
        DEBUG.P("interfaceExpected="+interfaceExpected);
        DEBUG.P("checkExtensible="+checkExtensible);  
                 
        if (t.tag == TYPEVAR && !classExpected && !interfaceExpected) {
            DEBUG.P("t.getUpperBound()="+t.getUpperBound());
            // check that type variable is already visible
            if (t.getUpperBound() == null) {
                log.error(tree.pos(), "illegal.forward.ref");
                return syms.errType;
            }
        } else {
            t = chk.checkClassType(tree.pos(), t, checkExtensible|!allowGenerics);
        }
        if (interfaceExpected && (t.tsym.flags() & INTERFACE) == 0) {
            /*错误例子:
            bin\mysrc\my\test\Test.java:8: 此处需要接口
                    public class Test<S,T extends ExtendsTest,E extends ExtendsTest & MyInterfaceA>
                    extends my.ExtendsTest.MyInnerClassStatic implements ExtendsTest {
                                                                         ^
            */
            log.error(tree.pos(), "intf.expected.here");
            // return errType is necessary since otherwise there might
            // be undetected cycles which cause attribution to loop
            return syms.errType;
        } else if (checkExtensible &&
                   classExpected &&
                   (t.tsym.flags() & INTERFACE) != 0) {
            /*src/my/test/EnterTest.java:24: 此处不需要接口
            public class EnterTest<T,S> extends EnterTestInterfaceA implements EnterTestInterfaceA,EnterTestInterfaceB {
                                                ^
            */
            log.error(tree.pos(), "no.intf.expected.here");
            return syms.errType;
        }
        if (checkExtensible &&
            ((t.tsym.flags() & FINAL) != 0)) {
            /*
            src/my/test/EnterTest.java:27: 无法从最终 my.test.EnterTestFinalSupertype 进行继承
            public class EnterTest<T,S> extends EnterTestFinalSupertype {    
                                                ^
            */
            log.error(tree.pos(),
                      "cant.inherit.from.final", t.tsym);
        }
        chk.checkNonCyclic(tree.pos(), t);
        return t;
        
        
        }finally{//我加上的
        DEBUG.P(0,this,"checkBase(6)");
        }
    }

    public void visitClassDef(JCClassDecl tree) {
    	DEBUG.P(this,"visitClassDef(1)");
    	DEBUG.P("tree.sym="+tree.sym);
    	DEBUG.P("env.info.scope.owner.kind="+Kinds.toString(env.info.scope.owner.kind));
    	
        // Local classes have not been entered yet, so we need to do it now:
        if ((env.info.scope.owner.kind & (VAR | MTH)) != 0)
            enter.classEnter(tree, env);

        ClassSymbol c = tree.sym;
        DEBUG.P("enter.classEnter 结束  c="+c);
        if (c == null) {
            // exit in case something drastic went wrong during enter.
            result = null;
        } else {
            // make sure class has been completed:
            c.complete();

            // If this class appears as an anonymous class
            // in a superclass constructor call where
            // no explicit outer instance is given,
            // disable implicit outer instance from being passed.
            // (This would be an illegal access to "this before super").
            DEBUG.P("env.info.isSelfCall="+env.info.isSelfCall);
            DEBUG.P("env.tree.tag="+env.tree.myTreeTag());
			if(env.tree.tag == JCTree.NEWCLASS)
				DEBUG.P("env.tree.encl="+((JCNewClass) env.tree).encl);
            if (env.info.isSelfCall &&
                env.tree.tag == JCTree.NEWCLASS &&
                ((JCNewClass) env.tree).encl == null)
            {
                c.flags_field |= NOOUTERTHIS;
            }
            
            DEBUG.P("c.flags_field="+Flags.toString(c.flags_field));
            attribClass(tree.pos(), c);
            result = tree.type = c.type;
        }
        
        DEBUG.P(0,this,"visitClassDef(1)");
    }

    public void visitMethodDef(JCMethodDecl tree) {
    	DEBUG.P(this,"visitMethodDef(JCMethodDecl tree)");
    	DEBUG.P("tree.sym="+tree.sym);
        MethodSymbol m = tree.sym;

		DEBUG.P("env.info.lint="+env.info.lint);
        Lint lint = env.info.lint.augment(m.attributes_field, m.flags());
		DEBUG.P("lint="+lint);

        Lint prevLint = chk.setLint(lint);
        try {
            chk.checkDeprecatedAnnotation(tree.pos(), m);
            
            //COMPOUND类型会对应一个ClassSymbol
            //在attribBounds必须对这个ClassSymbol进行attribClass
            attribBounds(tree.typarams);

            // If we override any other methods, check that we do so properly.
            // JLS ???
            chk.checkOverride(tree, m);

            // Create a new environment with local scope
            // for attributing the method.
            Env<AttrContext> localEnv = memberEnter.methodEnv(tree, env);

            localEnv.info.lint = lint;

            // Enter all type parameters into the local method scope.
            for (List<JCTypeParameter> l = tree.typarams; l.nonEmpty(); l = l.tail)
                localEnv.info.scope.enterIfAbsent(l.head.type.tsym);

            ClassSymbol owner = env.enclClass.sym;
            if ((owner.flags() & ANNOTATION) != 0 &&
                tree.params.nonEmpty())
                log.error(tree.params.head.pos(),
                          "intf.annotation.members.cant.have.params");

            // Attribute all value parameters.
            for (List<JCVariableDecl> l = tree.params; l.nonEmpty(); l = l.tail) {
                attribStat(l.head, localEnv);
            }

            // Check that type parameters are well-formed.
            chk.validateTypeParams(tree.typarams);
            if ((owner.flags() & ANNOTATION) != 0 &&
                tree.typarams.nonEmpty())
                log.error(tree.typarams.head.pos(),
                          "intf.annotation.members.cant.have.type.params");

            // Check that result type is well-formed.
            chk.validate(tree.restype);
            if ((owner.flags() & ANNOTATION) != 0)
                chk.validateAnnotationType(tree.restype);

            if ((owner.flags() & ANNOTATION) != 0)
                chk.validateAnnotationMethod(tree.pos(), m);

            // Check that all exceptions mentioned in the throws clause extend
            // java.lang.Throwable.
            if ((owner.flags() & ANNOTATION) != 0 && tree.thrown.nonEmpty())
                log.error(tree.thrown.head.pos(),
                          "throws.not.allowed.in.intf.annotation");
            for (List<JCExpression> l = tree.thrown; l.nonEmpty(); l = l.tail)
                chk.checkType(l.head.pos(), l.head.type, syms.throwableType);

            if (tree.body == null) {
                // Empty bodies are only allowed for
                // abstract, native, or interface methods, or for methods
                // in a retrofit signature class.
                if ((owner.flags() & INTERFACE) == 0 &&
                    (tree.mods.flags & (ABSTRACT | NATIVE)) == 0 &&
                    !relax)
                    log.error(tree.pos(), "missing.meth.body.or.decl.abstract");
                if (tree.defaultValue != null) {
                    if ((owner.flags() & ANNOTATION) == 0)
                        log.error(tree.pos(),
                                  "default.allowed.in.intf.annotation.member");
                }
            } else if ((owner.flags() & INTERFACE) != 0) {
                log.error(tree.body.pos(), "intf.meth.cant.have.body");
            } else if ((tree.mods.flags & ABSTRACT) != 0) {
                log.error(tree.pos(), "abstract.meth.cant.have.body");
            } else if ((tree.mods.flags & NATIVE) != 0) {
                log.error(tree.pos(), "native.meth.cant.have.body");
            } else {
                // Add an implicit super() call unless an explicit call to
                // super(...) or this(...) is given
                // or we are compiling class java.lang.Object.
                if (tree.name == names.init && owner.type != syms.objectType) {
                    JCBlock body = tree.body;
                    if (body.stats.isEmpty() ||
                        !TreeInfo.isSelfCall(body.stats.head)) {
                        body.stats = body.stats.
                            prepend(memberEnter.SuperCall(make.at(body.pos),
                                                          List.<Type>nil(),
                                                          List.<JCVariableDecl>nil(),
                                                          false));
                    } else if ((env.enclClass.sym.flags() & ENUM) != 0 &&
                               (tree.mods.flags & GENERATEDCONSTR) == 0 &&
                               TreeInfo.isSuperCall(body.stats.head)) {
						/*如:
						enum EA {
							;
							EA() { super(); }
						}
						*/
                        // enum constructors are not allowed to call super
                        // directly, so make sure there aren't any super calls
                        // in enum constructors, except in the compiler
                        // generated one.
                        log.error(tree.body.stats.head.pos(),
                                  "call.to.super.not.allowed.in.enum.ctor",
                                  env.enclClass.sym);
                    }
                }

                // Attribute method body.
                attribStat(tree.body, localEnv);
            }
            localEnv.info.scope.leave();
            result = tree.type = m.type;
            chk.validateAnnotations(tree.mods.annotations, m);
        }
        finally {
            chk.setLint(prevLint);
            DEBUG.P(1,this,"visitMethodDef(JCMethodDecl tree)");
        }
    }

    public void visitVarDef(JCVariableDecl tree) {
    	DEBUG.P(this,"visitVarDef(1)");
    	DEBUG.P("tree="+tree);
		DEBUG.P("env.info.scope前="+env.info.scope);
    	DEBUG.P("env.info.scope.owner="+env.info.scope.owner);
		DEBUG.P("env.info.scope.owner.kind="+Kinds.toString(env.info.scope.owner.kind));

    	
        // Local variables have not been entered yet, so we need to do it now:
        if (env.info.scope.owner.kind == MTH) {
        	DEBUG.P("tree.sym="+tree.sym);
			/*方法的参数和方法体中的局部变量在两个作用域(Scope)中，如:
			class Aclass<T> {
				void m(int a) {
					int b;
				}
			}
			env.info.scope=Scope[(entries=1 nelems=1 owner=m())b | (entries=1 nelems=1 owner=m())a | (entries=3 nelems=3 owner=Aclass)super, this, T]
			*/
            if (tree.sym != null) { //方法参数已在MemberEnter.signature(5)中加入
                // parameters have already been entered
                env.info.scope.enter(tree.sym);
            } else {
                memberEnter.memberEnter(tree, env);
                annotate.flush();
            }
        }
		DEBUG.P("env.info.scope后="+env.info.scope);
        
        DEBUG.P("chk.validate 前");

        // Check that the variable's declared type is well-formed.
        chk.validate(tree.vartype);

        VarSymbol v = tree.sym;
        Lint lint = env.info.lint.augment(v.attributes_field, v.flags());
        Lint prevLint = chk.setLint(lint);

        try {
            chk.checkDeprecatedAnnotation(tree.pos(), v);
            
            DEBUG.P("tree.init="+tree.init);
            if (tree.init != null) {
                if ((v.flags_field & FINAL) != 0 && tree.init.tag != JCTree.NEWCLASS) {
                    // In this case, `v' is final.  Ensure that it's initializer is
                    // evaluated.
                    v.getConstValue(); // ensure initializer is evaluated
                } else {
                    // Attribute initializer in a new environment
                    // with the declared variable as owner.
                    // Check that initializer conforms to variable's declared type.
                    Env<AttrContext> initEnv = memberEnter.initEnv(tree, env);
                    initEnv.info.lint = lint;
                    // In order to catch self-references, we set the variable's
                    // declaration position to maximal possible value, effectively
                    // marking the variable as undefined.
                    v.pos = Position.MAXPOS;
                    attribExpr(tree.init, initEnv, v.type);
                    v.pos = tree.pos;
                }
            }
            result = tree.type = v.type;
            chk.validateAnnotations(tree.mods.annotations, v);
        }
        finally {
            chk.setLint(prevLint);
        }
        
        DEBUG.P(0,this,"visitVarDef(1)");
    }

    public void visitSkip(JCSkip tree) {
        result = null;
    }

    public void visitBlock(JCBlock tree) {
		DEBUG.P(this,"visitBlock(1)");
		DEBUG.P("env.info.scope.owner="+env.info.scope.owner);
		DEBUG.P("env.info.scope.owner.kind="+Kinds.toString(env.info.scope.owner.kind));

        if (env.info.scope.owner.kind == TYP) { //实例、static初始块
            // Block is a static or instance initializer;
            // let the owner of the environment be a freshly
            // created BLOCK-method.
            Env<AttrContext> localEnv =
                env.dup(tree, env.info.dup(env.info.scope.dupUnshared()));
            localEnv.info.scope.owner =
                new MethodSymbol(tree.flags | BLOCK, names.empty, null,
                                 env.info.scope.owner);
            if ((tree.flags & STATIC) != 0) localEnv.info.staticLevel++;

			DEBUG.P("localEnv="+localEnv);
            attribStats(tree.stats, localEnv);
        } else {
            // Create a new local environment with a local scope.
            Env<AttrContext> localEnv =
                env.dup(tree, env.info.dup(env.info.scope.dup()));

			DEBUG.P("localEnv="+localEnv);
            attribStats(tree.stats, localEnv);
            localEnv.info.scope.leave();
        }
        result = null;
        DEBUG.P(0,this,"visitBlock(1)");
    }

	//JCBlock、JCDoWhileLoop、JCWhileLoop、JCForLoop、JCEnhancedForLoop对应的type都为null
	//因为它们都是语句，语句没有类型
    public void visitDoLoop(JCDoWhileLoop tree) {
        attribStat(tree.body, env.dup(tree));
        attribExpr(tree.cond, env, syms.booleanType);
        result = null;
    }

    public void visitWhileLoop(JCWhileLoop tree) {
        attribExpr(tree.cond, env, syms.booleanType);
        attribStat(tree.body, env.dup(tree));
        result = null;
    }

	/*
		class Aclass {
			{
				int i=10;
				for(int i=10,j=20;;);
			}
			void m(int i) {
				for(int i=10,j=20;;);
			}
		}

		test\attr\AttrTests.java:8: 已在  中定义 i
			for(int i=10,j=20;;);
					^
		test\attr\AttrTests.java:11: 已在 m(int) 中定义 i
						for(int i=10,j=20;;);
								^
		2 错误
	*/
    public void visitForLoop(JCForLoop tree) {
		DEBUG.P(this,"visitForLoop(1)");
        Env<AttrContext> loopEnv =
            env.dup(env.tree, env.info.dup(env.info.scope.dup()));

		DEBUG.P("loopEnv="+loopEnv);
        attribStats(tree.init, loopEnv);
        if (tree.cond != null) attribExpr(tree.cond, loopEnv, syms.booleanType);
        loopEnv.tree = tree; // before, we were not in loop!
        attribStats(tree.step, loopEnv);
        attribStat(tree.body, loopEnv);
        loopEnv.info.scope.leave();
        result = null;
		DEBUG.P("tree.type = "+tree.type);
		DEBUG.P(0,this,"visitForLoop(1)");
    }

    public void visitForeachLoop(JCEnhancedForLoop tree) {
    	DEBUG.P(this,"visitForeachLoop(1)");
        Env<AttrContext> loopEnv =
            env.dup(env.tree, env.info.dup(env.info.scope.dup()));
        attribStat(tree.var, loopEnv);
        Type exprType = types.upperBound(attribExpr(tree.expr, loopEnv));
        chk.checkNonVoid(tree.pos(), exprType);
        Type elemtype = types.elemtype(exprType); // perhaps expr is an array?
		DEBUG.P("elemtype = "+elemtype);
		//如果types.elemtype(exprType)返回值不为null，要么是ERROR，要么是数组元素类型
        if (elemtype == null) {
            // or perhaps expr implements Iterable<T>?
            Type base = types.asSuper(exprType, syms.iterableType.tsym);
			DEBUG.P("base = "+base);
            if (base == null) {
                log.error(tree.expr.pos(), "foreach.not.applicable.to.type");
                elemtype = syms.errType;
            } else {
                List<Type> iterableParams = base.allparams();
				DEBUG.P("iterableParams = "+iterableParams);
                elemtype = iterableParams.isEmpty()
                    ? syms.objectType
                    : types.upperBound(iterableParams.head);
            }
        }
		DEBUG.P("elemtype = "+elemtype);
        chk.checkType(tree.expr.pos(), elemtype, tree.var.sym.type);
        loopEnv.tree = tree; // before, we were not in loop!
        attribStat(tree.body, loopEnv);
        loopEnv.info.scope.leave();
        result = null;
        DEBUG.P(0,this,"visitForeachLoop(1)");
    }

    public void visitLabelled(JCLabeledStatement tree) {
    	DEBUG.P(this,"visitLabelled(1)");
    	
        // Check that label is not used in an enclosing statement
        Env<AttrContext> env1 = env;
        while (env1 != null && env1.tree.tag != JCTree.CLASSDEF) {
			//例:labelA:while(true) labelA: break;
			//从内往外看，第二个labelA出错:标签 labelA 已使用
            if (env1.tree.tag == JCTree.LABELLED &&
                ((JCLabeledStatement) env1.tree).label == tree.label) {
                log.error(tree.pos(), "label.already.in.use",
                          tree.label);
                break;
            }
            env1 = env1.next;
        }

        attribStat(tree.body, env.dup(tree));
        result = null;
        
        DEBUG.P(0,this,"visitLabelled(1)");
    }

    public void visitSwitch(JCSwitch tree) {
    	DEBUG.P(this,"visitSwitch(1)");
        Type seltype = attribExpr(tree.selector, env);

        Env<AttrContext> switchEnv =
            env.dup(tree, env.info.dup(env.info.scope.dup()));

        boolean enumSwitch =
            allowEnums &&
            (seltype.tsym.flags() & Flags.ENUM) != 0;
        if (!enumSwitch)
            seltype = chk.checkType(tree.selector.pos(), seltype, syms.intType);

        // Attribute all cases and
        // check that there are no duplicate case labels or default clauses.
        Set<Object> labels = new HashSet<Object>(); // The set of case labels.
        boolean hasDefault = false;      // Is there a default label?
        for (List<JCCase> l = tree.cases; l.nonEmpty(); l = l.tail) {
            JCCase c = l.head;
            Env<AttrContext> caseEnv =
                switchEnv.dup(c, env.info.dup(switchEnv.info.scope.dup()));
            if (c.pat != null) {
                if (enumSwitch) {
                    Symbol sym = enumConstant(c.pat, seltype);
                    if (sym == null) {
                        log.error(c.pat.pos(), "enum.const.req");
                    } else if (!labels.add(sym)) {
                        log.error(c.pos(), "duplicate.case.label");
                    }
                } else {
                    Type pattype = attribExpr(c.pat, switchEnv, seltype);
                    if (pattype.tag != ERROR) {
                        if (pattype.constValue() == null) {
                            log.error(c.pat.pos(), "const.expr.req");
                        } else if (labels.contains(pattype.constValue())) {
                            log.error(c.pos(), "duplicate.case.label");
                        } else {
                            labels.add(pattype.constValue());
                        }
                    }
                }
            } else if (hasDefault) {
                log.error(c.pos(), "duplicate.default.label");
            } else {
                hasDefault = true;
            }
            attribStats(c.stats, caseEnv);
            caseEnv.info.scope.leave();
            addVars(c.stats, switchEnv.info.scope);
        }

        switchEnv.info.scope.leave();
        result = null;
        
        DEBUG.P(0,this,"visitSwitch(1)");
    }
    // where
        /** Add any variables defined in stats to the switch scope. */
        private static void addVars(List<JCStatement> stats, Scope switchScope) {
            for (;stats.nonEmpty(); stats = stats.tail) {
                JCTree stat = stats.head;
                if (stat.tag == JCTree.VARDEF)
                    switchScope.enter(((JCVariableDecl) stat).sym);
            }
        }
    // where
    /** Return the selected enumeration constant symbol, or null. */
    private Symbol enumConstant(JCTree tree, Type enumType) {
		//switch语句的selector如果是枚举类型，
		//那么对应的case中不能在枚举常量前加枚举类型名
        if (tree.tag != JCTree.IDENT) {
            log.error(tree.pos(), "enum.label.must.be.unqualified.enum");
            return syms.errSymbol;
        }
        JCIdent ident = (JCIdent)tree;
        Name name = ident.name;
        for (Scope.Entry e = enumType.tsym.members().lookup(name);
             e.scope != null; e = e.next()) {
            if (e.sym.kind == VAR) {
                Symbol s = ident.sym = e.sym;
                ((VarSymbol)s).getConstValue(); // ensure initializer is evaluated
                ident.type = s.type;
                return ((s.flags_field & Flags.ENUM) == 0)
                    ? null : s;
            }
        }
        return null;
    }

    public void visitSynchronized(JCSynchronized tree) {
    	DEBUG.P(this,"visitSynchronized(1)");
        chk.checkRefType(tree.pos(), attribExpr(tree.lock, env));
        attribStat(tree.body, env);
        result = null;
        DEBUG.P(0,this,"visitSynchronized(1)");
    }

    public void visitTry(JCTry tree) {
    	DEBUG.P(this,"visitTry(1)");
        // Attribute body
        attribStat(tree.body, env.dup(tree, env.info.dup()));

        // Attribute catch clauses
        for (List<JCCatch> l = tree.catchers; l.nonEmpty(); l = l.tail) {
            JCCatch c = l.head;
            Env<AttrContext> catchEnv =
                env.dup(c, env.info.dup(env.info.scope.dup()));
            Type ctype = attribStat(c.param, catchEnv);
            if (c.param.type.tsym.kind == Kinds.VAR) {
                c.param.sym.setData(ElementKind.EXCEPTION_PARAMETER);
            }
            chk.checkType(c.param.vartype.pos(),
                          chk.checkClassType(c.param.vartype.pos(), ctype),
                          syms.throwableType);
            attribStat(c.body, catchEnv);
            catchEnv.info.scope.leave();
        }

        // Attribute finalizer
		//注意这里的env没有用JCTry tree
        if (tree.finalizer != null) attribStat(tree.finalizer, env);
        result = null;
        
        DEBUG.P(0,this,"visitTry(1)");
    }

    public void visitConditional(JCConditional tree) {
		DEBUG.P(this,"visitConditional(1)");

        attribExpr(tree.cond, env, syms.booleanType);
        attribExpr(tree.truepart, env);
        attribExpr(tree.falsepart, env);
        result = check(tree,
                       capture(condType(tree.pos(), tree.cond.type,
                                        tree.truepart.type, tree.falsepart.type)),
                       VAL, pkind, pt);

		DEBUG.P(0,this,"visitConditional(1)");
    }
    //where
        /** Compute the type of a conditional expression, after
         *  checking that it exists. See Spec 15.25.
         *
         *  @param pos      The source position to be used for
         *                  error diagnostics.
         *  @param condtype The type of the expression's condition.
         *  @param thentype The type of the expression's then-part.
         *  @param elsetype The type of the expression's else-part.
         */
        private Type condType(DiagnosticPosition pos,
                              Type condtype,
                              Type thentype,
                              Type elsetype) {
			try {//我加上的
			DEBUG.P(this,"condType(4)");

            Type ctype = condType1(pos, condtype, thentype, elsetype);

            // If condition and both arms are numeric constants,
            // evaluate at compile-time.
            return ((condtype.constValue() != null) &&
                    (thentype.constValue() != null) &&
                    (elsetype.constValue() != null))
                ? cfolder.coerce(condtype.isTrue()?thentype:elsetype, ctype)
                : ctype;

			}finally{//我加上的
			DEBUG.P(0,this,"condType(4)");
			}
        }
        /** Compute the type of a conditional expression, after
         *  checking that it exists.  Does not take into
         *  account the special case where condition and both arms
         *  are constants.
         *
         *  @param pos      The source position to be used for error
         *                  diagnostics.
         *  @param condtype The type of the expression's condition.
         *  @param thentype The type of the expression's then-part.
         *  @param elsetype The type of the expression's else-part.
         */
        private Type condType1(DiagnosticPosition pos, Type condtype,
                               Type thentype, Type elsetype) {
			try {//我加上的
			DEBUG.P(this,"condType1(4)");
			DEBUG.P("condtype="+condtype);
			DEBUG.P("thentype="+thentype);
			DEBUG.P("elsetype="+elsetype);

            // If same type, that is the result
            if (types.isSameType(thentype, elsetype))
                return thentype.baseType();

            Type thenUnboxed = (!allowBoxing || thentype.isPrimitive())
                ? thentype : types.unboxedType(thentype);
            Type elseUnboxed = (!allowBoxing || elsetype.isPrimitive())
                ? elsetype : types.unboxedType(elsetype);

			DEBUG.P("thenUnboxed="+thenUnboxed);
			DEBUG.P("elseUnboxed="+elseUnboxed);

            // Otherwise, if both arms can be converted to a numeric
            // type, return the least numeric type that fits both arms
            // (i.e. return larger of the two, or return int if one
            // arm is short, the other is char).
            if (thenUnboxed.isPrimitive() && elseUnboxed.isPrimitive()) {
                // If one arm has an integer subrange type (i.e., byte,
                // short, or char), and the other is an integer constant
                // that fits into the subrange, return the subrange type.
                if (thenUnboxed.tag < INT && elseUnboxed.tag == INT &&
                    types.isAssignable(elseUnboxed, thenUnboxed))
                    return thenUnboxed.baseType();
                if (elseUnboxed.tag < INT && thenUnboxed.tag == INT &&
                    types.isAssignable(thenUnboxed, elseUnboxed))
                    return elseUnboxed.baseType();

                for (int i = BYTE; i < VOID; i++) {
                    Type candidate = syms.typeOfTag[i];
                    if (types.isSubtype(thenUnboxed, candidate) &&
                        types.isSubtype(elseUnboxed, candidate))
                        return candidate;
                }
            }

            // Those were all the cases that could result in a primitive
            if (allowBoxing) {
                if (thentype.isPrimitive())
                    thentype = types.boxedClass(thentype).type;
                if (elsetype.isPrimitive())
                    elsetype = types.boxedClass(elsetype).type;
            }

            if (types.isSubtype(thentype, elsetype))
                return elsetype.baseType();
            if (types.isSubtype(elsetype, thentype))
                return thentype.baseType();

            if (!allowBoxing || thentype.tag == VOID || elsetype.tag == VOID) {
                log.error(pos, "neither.conditional.subtype",
                          thentype, elsetype);
                return thentype.baseType();
            }

            // both are known to be reference types.  The result is
            // lub(thentype,elsetype). This cannot fail, as it will
            // always be possible to infer "Object" if nothing better.
            return types.lub(thentype.baseType(), elsetype.baseType());

			}finally{//我加上的
			DEBUG.P(0,this,"condType1(2)");
			}
        }

    public void visitIf(JCIf tree) {
    	DEBUG.P(this,"visitIf(1)");
        attribExpr(tree.cond, env, syms.booleanType);
        attribStat(tree.thenpart, env);
        if (tree.elsepart != null)
            attribStat(tree.elsepart, env);
        chk.checkEmptyIf(tree);
        result = null;
        DEBUG.P(0,this,"visitIf(1)");
    }

    public void visitExec(JCExpressionStatement tree) {
    	DEBUG.P(this,"visitExec(1)");
        attribExpr(tree.expr, env);
        result = null;
        DEBUG.P(0,this,"visitExec(1)");
    }

    public void visitBreak(JCBreak tree) {
        tree.target = findJumpTarget(tree.pos(), tree.tag, tree.label, env);
        result = null;
    }

    public void visitContinue(JCContinue tree) {
        tree.target = findJumpTarget(tree.pos(), tree.tag, tree.label, env);
        result = null;
    }
    //where
        /** Return the target of a break or continue statement, if it exists,
         *  report an error if not.
         *  Note: The target of a labelled break or continue is the
         *  (non-labelled) statement tree referred to by the label,
         *  not the tree representing the labelled statement itself.
         *
         *  @param pos     The position to be used for error diagnostics
         *  @param tag     The tag of the jump statement. This is either
         *                 Tree.BREAK or Tree.CONTINUE.
         *  @param label   The label of the jump statement, or null if no
         *                 label is given.
         *  @param env     The environment current at the jump statement.
         */
        private JCTree findJumpTarget(DiagnosticPosition pos,
                                    int tag,
                                    Name label,
                                    Env<AttrContext> env) {
            // Search environments outwards from the point of jump.
            Env<AttrContext> env1 = env;
            LOOP:
            while (env1 != null) {
                switch (env1.tree.tag) {
                case JCTree.LABELLED:
                    JCLabeledStatement labelled = (JCLabeledStatement)env1.tree;
                    if (label == labelled.label) {
                        // If jump is a continue, check that target is a loop.
                        if (tag == JCTree.CONTINUE) {
                            if (labelled.body.tag != JCTree.DOLOOP &&
                                labelled.body.tag != JCTree.WHILELOOP &&
                                labelled.body.tag != JCTree.FORLOOP &&
                                labelled.body.tag != JCTree.FOREACHLOOP)
                                log.error(pos, "not.loop.label", label);
                            // Found labelled statement target, now go inwards
                            // to next non-labelled tree.
                            return TreeInfo.referencedStatement(labelled);
                        } else {
                            return labelled;
                        }
                    }
                    break;
                case JCTree.DOLOOP:
                case JCTree.WHILELOOP:
                case JCTree.FORLOOP:
                case JCTree.FOREACHLOOP:
                    if (label == null) return env1.tree;
                    break;
                case JCTree.SWITCH:
                    if (label == null && tag == JCTree.BREAK) return env1.tree;
                    break;
                case JCTree.METHODDEF:
                case JCTree.CLASSDEF:
                    break LOOP;
                default:
                }
                env1 = env1.next;
            }
            if (label != null)
                log.error(pos, "undef.label", label);
            else if (tag == JCTree.CONTINUE)
                log.error(pos, "cont.outside.loop");
            else
                log.error(pos, "break.outside.switch.loop");
            return null;
        }

    public void visitReturn(JCReturn tree) {
    	DEBUG.P(this,"visitReturn(1)");
        // Check that there is an enclosing method which is
        // nested within than the enclosing class.

		//env.enclMethod=null，如:实例化块{ return; }
		if(env.enclMethod!=null) {
			DEBUG.P("env.enclMethod.sym.owner="+env.enclMethod.sym.owner);
			DEBUG.P("env.enclClass.sym="+env.enclClass.sym);
		} else DEBUG.P("env.enclMethod="+null);

        if (env.enclMethod == null ||
            env.enclMethod.sym.owner != env.enclClass.sym) { //???什么时候满足这个条件
            log.error(tree.pos(), "ret.outside.meth");

        } else {
            // Attribute return expression, if it exists, and check that
            // it conforms to result type of enclosing method.
            Symbol m = env.enclMethod.sym;
            if (m.type.getReturnType().tag == VOID) {
                if (tree.expr != null)
                    log.error(tree.expr.pos(),
                              "cant.ret.val.from.meth.decl.void");
            } else if (tree.expr == null) {
                log.error(tree.pos(), "missing.ret.val");
            } else {
                attribExpr(tree.expr, env, m.type.getReturnType());
            }
        }
        result = null;
        
        DEBUG.P(0,this,"visitReturn(1)");
    }

    public void visitThrow(JCThrow tree) {
        attribExpr(tree.expr, env, syms.throwableType);
        result = null;
    }

    public void visitAssert(JCAssert tree) {
    	DEBUG.P(this,"visitAssert(1)");
        attribExpr(tree.cond, env, syms.booleanType);
        if (tree.detail != null) {
            chk.checkNonVoid(tree.detail.pos(), attribExpr(tree.detail, env));
        }
        result = null;
        DEBUG.P(0,this,"visitAssert(1)");
    }

    /** Visitor method for method invocations.
     *  NOTE: The method part of an application will have in its type field
     *        the return type of the method, not the method's type itself!
     */
    public void visitApply(JCMethodInvocation tree) {
    	DEBUG.P(this,"visitApply(1)");
		DEBUG.P("tree="+tree);
		DEBUG.P("tree.meth="+tree.meth);
		DEBUG.P("tree.typeargs="+tree.typeargs);
		DEBUG.P("tree.args="+tree.args);
		DEBUG.P("tree.varargsElement="+tree.varargsElement);

        // The local environment of a method application is
        // a new environment nested in the current one.
        Env<AttrContext> localEnv = env.dup(tree, env.info.dup());

		DEBUG.P("localEnv="+localEnv);

        // The types of the actual method arguments.
        List<Type> argtypes;

        // The types of the actual method type arguments.
        List<Type> typeargtypes = null;

        Name methName = TreeInfo.name(tree.meth);

		DEBUG.P("methName="+methName);

        boolean isConstructorCall =
            methName == names._this || methName == names._super;

		DEBUG.P("isConstructorCall="+isConstructorCall);

        if (isConstructorCall) {
            // We are seeing a ...this(...) or ...super(...) call.
            // Check that this is the first statement in a constructor.
            if (checkFirstConstructorStat(tree, env)) {
				//注意:是传入env，而不是localEnv

                // Record the fact
                // that this is a constructor call (using isSelfCall).
                localEnv.info.isSelfCall = true;

                // Attribute arguments, yielding list of argument types.
				DEBUG.P("tree.args="+tree.args);
				DEBUG.P("tree.typeargs="+tree.typeargs);
                argtypes = attribArgs(tree.args, localEnv);
                typeargtypes = attribTypes(tree.typeargs, localEnv);

                // Variable `site' points to the class in which the called
                // constructor is defined.
                Type site = env.enclClass.sym.type;
                DEBUG.P("site="+site);
                DEBUG.P("methName="+methName);
                if (methName == names._super) {
                    if (site == syms.objectType) {
                        log.error(tree.meth.pos(), "no.superclass", site);
                        site = syms.errType;
                    } else {
                        site = types.supertype(site);
                    }
                }
                
                DEBUG.P("site="+site);
                DEBUG.P("site.tag="+TypeTags.toString(site.tag));

                if (site.tag == CLASS) {
                	DEBUG.P("site.getEnclosingType().tag="+TypeTags.toString(site.getEnclosingType().tag));
                    if (site.getEnclosingType().tag == CLASS) {
                        // we are calling a nested class

                        if (tree.meth.tag == JCTree.SELECT) {
                            JCTree qualifier = ((JCFieldAccess) tree.meth).selected;

                            // We are seeing a prefixed call, of the form
                            //     <expr>.super(...).
                            // Check that the prefix expression conforms
                            // to the outer instance type of the class.
                            chk.checkRefType(qualifier.pos(),
                                             attribExpr(qualifier, localEnv,
                                                        site.getEnclosingType()));
                        } else if (methName == names._super) {
                            // qualifier omitted; check for existence
                            // of an appropriate implicit qualifier.
                            rs.resolveImplicitThis(tree.meth.pos(),
                                                   localEnv, site);
                        }
                    } else if (tree.meth.tag == JCTree.SELECT) {
						//例:class ClassA { ClassA() { ClassA.super(); } } 
                        log.error(tree.meth.pos(), "illegal.qual.not.icls",
                                  site.tsym);
                    }

                    // if we're calling a java.lang.Enum constructor,
                    // prefix the implicit String and int parameters
                    if (site.tsym == syms.enumSym && allowEnums)
                        argtypes = argtypes.prepend(syms.intType).prepend(syms.stringType);

                    // Resolve the called constructor under the assumption
                    // that we are referring to a superclass instance of the
                    // current instance (JLS ???).
                    boolean selectSuperPrev = localEnv.info.selectSuper;
                    localEnv.info.selectSuper = true;
                    localEnv.info.varArgs = false;
                    Symbol sym = rs.resolveConstructor(
                        tree.meth.pos(), localEnv, site, argtypes, typeargtypes);
                    localEnv.info.selectSuper = selectSuperPrev;

                    // Set method symbol to resolved constructor...
                    TreeInfo.setSymbol(tree.meth, sym);

                    // ...and check that it is legal in the current context.
                    // (this will also set the tree's type)
                    Type mpt = newMethTemplate(argtypes, typeargtypes);
                    checkId(tree.meth, site, sym, localEnv, MTH,
                            mpt, tree.varargsElement != null);
                }
                // Otherwise, `site' is an error type and we do nothing
            }
            result = tree.type = syms.voidType;
        } else {
            // Otherwise, we are seeing a regular method call.
            // Attribute the arguments, yielding list of argument types, ...
            argtypes = attribArgs(tree.args, localEnv);
            typeargtypes = attribTypes(tree.typeargs, localEnv);

            // ... and attribute the method using as a prototype a methodtype
            // whose formal argument types is exactly the list of actual
            // arguments (this will also set the method symbol).
            Type mpt = newMethTemplate(argtypes, typeargtypes);
            localEnv.info.varArgs = false;
            Type mtype = attribExpr(tree.meth, localEnv, mpt);
            if (localEnv.info.varArgs)
                assert mtype.isErroneous() || tree.varargsElement != null;

            // Compute the result type.
            Type restype = mtype.getReturnType();
            assert restype.tag != WILDCARD : mtype;

            // as a special case, array.clone() has a result that is
            // the same as static type of the array being cloned
            if (tree.meth.tag == JCTree.SELECT &&
                allowCovariantReturns &&
                methName == names.clone &&
                types.isArray(((JCFieldAccess) tree.meth).selected.type))
                restype = ((JCFieldAccess) tree.meth).selected.type;

            // as a special case, x.getClass() has type Class<? extends |X|>
            if (allowGenerics &&
                methName == names.getClass && tree.args.isEmpty()) {
                Type qualifier = (tree.meth.tag == JCTree.SELECT)
                    ? ((JCFieldAccess) tree.meth).selected.type
                    : env.enclClass.sym.type;
                restype = new
                    ClassType(restype.getEnclosingType(),
                              List.<Type>of(new WildcardType(types.erasure(qualifier),
                                                               BoundKind.EXTENDS,
                                                               syms.boundClass)),
                              restype.tsym);
            }

            // Check that value of resulting type is admissible in the
            // current context.  Also, capture the return type
            result = check(tree, capture(restype), VAL, pkind, pt);
        }
        chk.validate(tree.typeargs);
        DEBUG.P(0,this,"visitApply(1)");
    }
    //where
        /** Check that given application node appears as first statement
         *  in a constructor call.
         *  @param tree   The application node
         *  @param env    The environment current at the application.
         */
		//调用这个方法的前提是存在this(...)或super(...)调用，
		//因为可能在方法或构造函数中任何位置调用this(...)或super(...)，
		//所以必须检查只有在构造函数中第一条语句才能调用this(...)或super(...)
		//下面的enclMethod表示调用this(...)或super(...)的方法或构造函数
		//JCMethodInvocation tree表示this(...)或super(...)
        boolean checkFirstConstructorStat(JCMethodInvocation tree, Env<AttrContext> env) {
            try {//我加上的
			DEBUG.P(this,"checkFirstConstructorStat(2)");
			DEBUG.P("tree="+tree);
			DEBUG.P("env="+env);
			
            JCMethodDecl enclMethod = env.enclMethod;

			if(enclMethod != null) DEBUG.P("enclMethod.name="+enclMethod.name);
            else DEBUG.P("enclMethod=null");

			//如果在实例初始化语句块或静态语句块(如{this();} static {this();})
			//此时enclMethod为null，所以下面加了enclMethod != null条件
			if (enclMethod != null && enclMethod.name == names.init) {
                JCBlock body = enclMethod.body;
				//第一条语句是JCMethodInvocation tree(即:this(...)或super(...))
                if (body.stats.head.tag == JCTree.EXEC &&
                    ((JCExpressionStatement) body.stats.head).expr == tree)
                    return true;
            }
            log.error(tree.pos(),"call.must.be.first.stmt.in.ctor",
                      TreeInfo.name(tree.meth));
            return false;
            
            }finally{//我加上的
			DEBUG.P(0,this,"checkFirstConstructorStat(2)");
			}
        }

        /** Obtain a method type with given argument types.
         */
        Type newMethTemplate(List<Type> argtypes, List<Type> typeargtypes) {
			DEBUG.P(this,"newMethTemplate(2)");
			DEBUG.P("argtypes="+argtypes);
			DEBUG.P("typeargtypes="+typeargtypes);

            MethodType mt = new MethodType(argtypes, null, null, syms.methodClass);
            
			
			//typeargtypes不会为null,因为attribTypes(2)不会返回null
			//return (typeargtypes == null) ? mt : (Type)new ForAll(typeargtypes, mt);
			Type newMeth = (typeargtypes == null) ? mt : (Type)new ForAll(typeargtypes, mt);
			DEBUG.P("newMeth="+newMeth);
			DEBUG.P(0,this,"newMethTemplate(2)");
			return newMeth;
        }

    public void visitNewClass(JCNewClass tree) {
    	DEBUG.P(this,"visitNewClass(1)");
		DEBUG.P("tree="+tree);

        Type owntype = syms.errType;

        // The local environment of a class creation is
        // a new environment nested in the current one.
        Env<AttrContext> localEnv = env.dup(tree, env.info.dup());
        
        DEBUG.P("localEnv="+localEnv);
        
        // The anonymous inner class definition of the new expression,
        // if one is defined by it.
        JCClassDecl cdef = tree.def;

        // If enclosing class is given, attribute it, and
        // complete class name to be fully qualified
        JCExpression clazz = tree.clazz; // Class field following new
        DEBUG.P("clazz="+clazz);
        DEBUG.P("clazz.tag="+clazz.myTreeTag());
        JCExpression clazzid =          // Identifier in class field
            (clazz.tag == JCTree.TYPEAPPLY)
            ? ((JCTypeApply) clazz).clazz
            : clazz;

        JCExpression clazzid1 = clazzid; // The same in fully qualified form
        
        DEBUG.P("clazzid="+clazzid);
        DEBUG.P("tree.encl="+tree.encl);
        
		//如果(tree.encl != null)，那么就不能用 “<expr>.new 完全限定类名”这样的语法
		//如ClassA.new test.ClassB();这样的语法是错误的
        if (tree.encl != null) {
            // We are seeing a qualified new, of the form
            //    <expr>.new C <...> (...) ...
            // In this case, we let clazz stand for the name of the
            // allocated class C prefixed with the type of the qualifier
            // expression, so that we can
            // resolve it with standard techniques later. I.e., if
            // <expr> has type T, then <expr>.new C <...> (...)
            // yields a clazz T.C.
            Type encltype = chk.checkRefType(tree.encl.pos(),
                                             attribExpr(tree.encl, env));
            clazzid1 = make.at(clazz.pos).Select(make.Type(encltype),
                                                 ((JCIdent) clazzid).name);
            if (clazz.tag == JCTree.TYPEAPPLY)
                clazz = make.at(tree.pos).
                    TypeApply(clazzid1,
                              ((JCTypeApply) clazz).arguments);
            else
                clazz = clazzid1;
		//          System.out.println(clazz + " generated.");//DEBUG
        }

        // Attribute clazz expression and store
        // symbol + type back into the attributed tree.
        Type clazztype = chk.checkClassType(
            tree.clazz.pos(), attribType(clazz, env), true);
        chk.validate(clazz);
        if (tree.encl != null) {
            // We have to work in this case to store
            // symbol + type back into the attributed tree.
            tree.clazz.type = clazztype;
            TreeInfo.setSymbol(clazzid, TreeInfo.symbol(clazzid1));
            clazzid.type = ((JCIdent) clazzid).sym.type;
            if (!clazztype.isErroneous()) {
                if (cdef != null && clazztype.tsym.isInterface()) {
					/* 如
					class VisitSelectTest<T> {
						interface InterfaceA {}
						InterfaceA ia = new VisitSelectTest().new InterfaceA(){};
					}
					*/
                    log.error(tree.encl.pos(), "anon.class.impl.intf.no.qual.for.new");
                } else if (clazztype.tsym.isStatic()) {
					/* 如
					class VisitSelectTest<T> {
						static class ClassA {}
						ClassA ca = new VisitSelectTest().new ClassA(){};
					}
					*/
                    log.error(tree.encl.pos(), "qualified.new.of.static.class", clazztype.tsym);
                }
            }
        } else if (!clazztype.tsym.isInterface() &&
                   clazztype.getEnclosingType().tag == CLASS) {
            // Check for the existence of an apropos outer instance
            rs.resolveImplicitThis(tree.pos(), env, clazztype);
        }

        // Attribute constructor arguments.
        List<Type> argtypes = attribArgs(tree.args, localEnv);
        List<Type> typeargtypes = attribTypes(tree.typeargs, localEnv);

        // If we have made no mistakes in the class type...
        if (clazztype.tag == CLASS) {
            // Enums may not be instantiated except implicitly
            if (allowEnums &&
                (clazztype.tsym.flags_field&Flags.ENUM) != 0 &&
                (env.tree.tag != JCTree.VARDEF ||
                 (((JCVariableDecl) env.tree).mods.flags&Flags.ENUM) == 0 ||
                 ((JCVariableDecl) env.tree).init != tree)) {
                log.error(tree.pos(), "enum.cant.be.instantiated");
				/*如:((JCVariableDecl) env.tree).init != tree)??????不知举什么例子)
					enum EnumA {}
					int ea = new EnumA();
					EnumA eb = new EnumA();

					class ClassA {
						void m1(EnumA e) {}
						void m2() {
							m1(new EnumA());
						}
					}
				*/
				if(env.tree.tag == JCTree.VARDEF) {
					DEBUG.P("((JCVariableDecl) env.tree).init="+((JCVariableDecl) env.tree).init);
					DEBUG.P("tree="+tree);
					DEBUG.P("(((JCVariableDecl) env.tree).init != tree)="+(((JCVariableDecl) env.tree).init != tree));
				}
			}
            // Check that class is not abstract
			//如:
			//abstract class ClassA {}
			//ClassA ca = new ClassA();
            if (cdef == null &&
                (clazztype.tsym.flags() & (ABSTRACT | INTERFACE)) != 0) {
                log.error(tree.pos(), "abstract.cant.be.instantiated",
                          clazztype.tsym);
            } else if (cdef != null && clazztype.tsym.isInterface()) {
                // Check that no constructor arguments are given to
                // anonymous classes implementing an interface
				//如:
				//interface InterfaceB {}
				//InterfaceB ib = new InterfaceB(10,20){};
                if (!argtypes.isEmpty())
                    log.error(tree.args.head.pos(), "anon.class.impl.intf.no.args");

				//如:
				//interface InterfaceB<T> {}
				//InterfaceB<String> ib = new <String>InterfaceB(){};
                if (!typeargtypes.isEmpty())
                    log.error(tree.typeargs.head.pos(), "anon.class.impl.intf.no.typeargs");

                // Error recovery: pretend no arguments were supplied.
                argtypes = List.nil();
                typeargtypes = List.nil();
            }

            // Resolve the called constructor under the assumption
            // that we are referring to a superclass instance of the
            // current instance (JLS ???).
            else {
                localEnv.info.selectSuper = cdef != null;
                localEnv.info.varArgs = false;
                tree.constructor = rs.resolveConstructor(
                    tree.pos(), localEnv, clazztype, argtypes, typeargtypes);
                Type ctorType = checkMethod(clazztype,
                                            tree.constructor,
                                            localEnv,
                                            tree.args,
                                            argtypes,
                                            typeargtypes,
                                            localEnv.info.varArgs);
                if (localEnv.info.varArgs)
                    assert ctorType.isErroneous() || tree.varargsElement != null;
            }

            if (cdef != null) {
                // We are seeing an anonymous class instance creation.
                // In this case, the class instance creation
                // expression
                //
                //    E.new <typeargs1>C<typargs2>(args) { ... }
                //
                // is represented internally as
                //
                //    E . new <typeargs1>C<typargs2>(args) ( class <empty-name> { ... } )  .
                //
                // This expression is then *transformed* as follows:
                //
                // (1) add a STATIC flag to the class definition
                //     if the current environment is static
                // (2) add an extends or implements clause
                // (3) add a constructor.
                //
                // For instance, if C is a class, and ET is the type of E,
                // the expression
                //
                //    E.new <typeargs1>C<typargs2>(args) { ... }
                //
                // is translated to (where X is a fresh name and typarams is the
                // parameter list of the super constructor):
                //
                //   new <typeargs1>X(<*nullchk*>E, args) where
                //     X extends C<typargs2> {
                //       <typarams> X(ET e, args) {
                //         e.<typeargs1>super(args)
                //       }
                //       ...
                //     }
                if (Resolve.isStatic(env)) cdef.mods.flags |= STATIC;

                if (clazztype.tsym.isInterface()) {
                    cdef.implementing = List.of(clazz);
                } else {
                    cdef.extending = clazz;
                }

                attribStat(cdef, localEnv);

                // If an outer instance is given,
                // prefix it to the constructor arguments
                // and delete it from the new expression
                if (tree.encl != null && !clazztype.tsym.isInterface()) {
                    tree.args = tree.args.prepend(makeNullCheck(tree.encl));
                    argtypes = argtypes.prepend(tree.encl.type);
                    tree.encl = null;
                }

                // Reassign clazztype and recompute constructor.
                clazztype = cdef.sym.type;
                Symbol sym = rs.resolveConstructor(
                    tree.pos(), localEnv, clazztype, argtypes,
                    typeargtypes, true, tree.varargsElement != null);
                assert sym.kind < AMBIGUOUS || tree.constructor.type.isErroneous();
                tree.constructor = sym;
            }

            if (tree.constructor != null && tree.constructor.kind == MTH)
                owntype = clazztype;
        }
        result = check(tree, owntype, VAL, pkind, pt);
        chk.validate(tree.typeargs);
        
        DEBUG.P(1,this,"visitNewClass(1)");
    }

    /** Make an attributed null check tree.
     */
    public JCExpression makeNullCheck(JCExpression arg) {
        // optimization: X.this is never null; skip null check
        Name name = TreeInfo.name(arg);
        if (name == names._this || name == names._super) return arg;

        int optag = JCTree.NULLCHK;
        JCUnary tree = make.at(arg.pos).Unary(optag, arg);
        tree.operator = syms.nullcheck;
        tree.type = arg.type;
        return tree;
    }

    public void visitNewArray(JCNewArray tree) {
        Type owntype = syms.errType;
        Type elemtype;
        if (tree.elemtype != null) {
            elemtype = attribType(tree.elemtype, env);
            chk.validate(tree.elemtype);
            owntype = elemtype;
            for (List<JCExpression> l = tree.dims; l.nonEmpty(); l = l.tail) {
                attribExpr(l.head, env, syms.intType);
                owntype = new ArrayType(owntype, syms.arrayClass);
            }
        } else {
            // we are seeing an untyped aggregate { ... }
            // this is allowed only if the prototype is an array
            if (pt.tag == ARRAY) {
                elemtype = types.elemtype(pt);
            } else {
                if (pt.tag != ERROR) {
                    log.error(tree.pos(), "illegal.initializer.for.type",
                              pt);
                }
                elemtype = syms.errType;
            }
        }
        if (tree.elems != null) {
            attribExprs(tree.elems, env, elemtype);
            owntype = new ArrayType(elemtype, syms.arrayClass);
        }
        if (!types.isReifiable(elemtype))
            log.error(tree.pos(), "generic.array.creation");
        result = check(tree, owntype, VAL, pkind, pt);
    }

    public void visitParens(JCParens tree) {
        Type owntype = attribTree(tree.expr, env, pkind, pt);
        result = check(tree, owntype, pkind, pkind, pt);
        Symbol sym = TreeInfo.symbol(tree);
        if (sym != null && (sym.kind&(TYP|PCK)) != 0)
            log.error(tree.pos(), "illegal.start.of.type");
    }

    public void visitAssign(JCAssign tree) {
        Type owntype = attribTree(tree.lhs, env.dup(tree), VAR, Type.noType);
        Type capturedType = capture(owntype);
        attribExpr(tree.rhs, env, owntype);
        result = check(tree, capturedType, VAL, pkind, pt);
    }

    public void visitAssignop(JCAssignOp tree) {
        // Attribute arguments.
        Type owntype = attribTree(tree.lhs, env, VAR, Type.noType);
        Type operand = attribExpr(tree.rhs, env);
        // Find operator.
        Symbol operator = tree.operator = rs.resolveBinaryOperator(
            tree.pos(), tree.tag - JCTree.ASGOffset, env,
            owntype, operand);

        if (operator.kind == MTH) {
            chk.checkOperator(tree.pos(),
                              (OperatorSymbol)operator,
                              tree.tag - JCTree.ASGOffset,
                              owntype,
                              operand);
            if (types.isSameType(operator.type.getReturnType(), syms.stringType)) {
                // String assignment; make sure the lhs is a string
                chk.checkType(tree.lhs.pos(),
                              owntype,
                              syms.stringType);
            } else {
                chk.checkDivZero(tree.rhs.pos(), operator, operand);
                chk.checkCastable(tree.rhs.pos(),
                                  operator.type.getReturnType(),
                                  owntype);
            }
        }
        result = check(tree, owntype, VAL, pkind, pt);
    }

    public void visitUnary(JCUnary tree) {
        // Attribute arguments.
        Type argtype = (JCTree.PREINC <= tree.tag && tree.tag <= JCTree.POSTDEC)
            ? attribTree(tree.arg, env, VAR, Type.noType)
            : chk.checkNonVoid(tree.arg.pos(), attribExpr(tree.arg, env));

        // Find operator.
        Symbol operator = tree.operator =
            rs.resolveUnaryOperator(tree.pos(), tree.tag, env, argtype);

        Type owntype = syms.errType;
        if (operator.kind == MTH) {
            owntype = (JCTree.PREINC <= tree.tag && tree.tag <= JCTree.POSTDEC)
                ? tree.arg.type
                : operator.type.getReturnType();
            int opc = ((OperatorSymbol)operator).opcode;

            // If the argument is constant, fold it.
            if (argtype.constValue() != null) {
                Type ctype = cfolder.fold1(opc, argtype);
                if (ctype != null) {
                    owntype = cfolder.coerce(ctype, owntype);

                    // Remove constant types from arguments to
                    // conserve space. The parser will fold concatenations
                    // of string literals; the code here also
                    // gets rid of intermediate results when some of the
                    // operands are constant identifiers.
                    if (tree.arg.type.tsym == syms.stringType.tsym) {
                        tree.arg.type = syms.stringType;
                    }
                }
            }
        }
        result = check(tree, owntype, VAL, pkind, pt);
    }

    public void visitBinary(JCBinary tree) {
    	DEBUG.P(this,"visitBinary(1)");
        // Attribute arguments.
        Type left = chk.checkNonVoid(tree.lhs.pos(), attribExpr(tree.lhs, env));
        Type right = chk.checkNonVoid(tree.lhs.pos(), attribExpr(tree.rhs, env));

        // Find operator.
        Symbol operator = tree.operator =
            rs.resolveBinaryOperator(tree.pos(), tree.tag, env, left, right);

        Type owntype = syms.errType;
        if (operator.kind == MTH) {
            owntype = operator.type.getReturnType();
            int opc = chk.checkOperator(tree.lhs.pos(),
                                        (OperatorSymbol)operator,
                                        tree.tag,
                                        left,
                                        right);

            // If both arguments are constants, fold them.
            if (left.constValue() != null && right.constValue() != null) {
                Type ctype = cfolder.fold2(opc, left, right);
                if (ctype != null) {
                    owntype = cfolder.coerce(ctype, owntype);

                    // Remove constant types from arguments to
                    // conserve space. The parser will fold concatenations
                    // of string literals; the code here also
                    // gets rid of intermediate results when some of the
                    // operands are constant identifiers.
                    if (tree.lhs.type.tsym == syms.stringType.tsym) {
                        tree.lhs.type = syms.stringType;
                    }
                    if (tree.rhs.type.tsym == syms.stringType.tsym) {
                        tree.rhs.type = syms.stringType;
                    }
                }
            }

            // Check that argument types of a reference ==, != are
            // castable to each other, (JLS???).
            if ((opc == ByteCodes.if_acmpeq || opc == ByteCodes.if_acmpne)) {
                if (!types.isCastable(left, right, new Warner(tree.pos()))) {
                    log.error(tree.pos(), "incomparable.types", left, right);
                }
            }

            chk.checkDivZero(tree.rhs.pos(), operator, right);
        }
        result = check(tree, owntype, VAL, pkind, pt);
        DEBUG.P(0,this,"visitBinary(1)");
    }

    public void visitTypeCast(JCTypeCast tree) {
        Type clazztype = attribType(tree.clazz, env);
        Type exprtype = attribExpr(tree.expr, env, Infer.anyPoly);
        Type owntype = chk.checkCastable(tree.expr.pos(), exprtype, clazztype);
        if (exprtype.constValue() != null)
            owntype = cfolder.coerce(exprtype, owntype);
        result = check(tree, capture(owntype), VAL, pkind, pt);
    }

    public void visitTypeTest(JCInstanceOf tree) {
        Type exprtype = chk.checkNullOrRefType(
            tree.expr.pos(), attribExpr(tree.expr, env));
        Type clazztype = chk.checkReifiableReferenceType(
            tree.clazz.pos(), attribType(tree.clazz, env));
        chk.checkCastable(tree.expr.pos(), exprtype, clazztype);
        result = check(tree, syms.booleanType, VAL, pkind, pt);
    }

    public void visitIndexed(JCArrayAccess tree) {
        Type owntype = syms.errType;
        Type atype = attribExpr(tree.indexed, env);
        attribExpr(tree.index, env, syms.intType);
        if (types.isArray(atype))
            owntype = types.elemtype(atype);
        else if (atype.tag != ERROR)
            log.error(tree.pos(), "array.req.but.found", atype);
        if ((pkind & VAR) == 0) owntype = capture(owntype);
        result = check(tree, owntype, VAR, pkind, pt);
    }

	//在Attr阶段前JCIdent.sym是null的，在调用visitIdent()就有适当的值了
    public void visitIdent(JCIdent tree) {
    	DEBUG.P(this,"visitIdent(1)");
        Symbol sym;
        boolean varArgs = false;
        
        DEBUG.P("pt.tag="+TypeTags.toString(pt.tag));
        DEBUG.P("tree.sym="+tree.sym);
		if (tree.sym != null)
			DEBUG.P("tree.sym.kind="+Kinds.toString(tree.sym.kind));

        // Find symbol
        if (pt.tag == METHOD || pt.tag == FORALL) {
            // If we are looking for a method, the prototype `pt' will be a
            // method type with the type of the call's arguments as parameters.
            env.info.varArgs = false;
            sym = rs.resolveMethod(tree.pos(), env, tree.name, pt.getParameterTypes(), pt.getTypeArguments());
            varArgs = env.info.varArgs;
        } else if (tree.sym != null && tree.sym.kind != VAR) {
            sym = tree.sym;
        } else {
            sym = rs.resolveIdent(tree.pos(), env, tree.name, pkind);
        }
        tree.sym = sym;
        DEBUG.P("tree.sym="+tree.sym);
        DEBUG.P("tree.sym.kind="+Kinds.toString(tree.sym.kind));

        // (1) Also find the environment current for the class where
        //     sym is defined (`symEnv').
        // Only for pre-tiger versions (1.4 and earlier):
        // (2) Also determine whether we access symbol out of an anonymous
        //     class in a this or super call.  This is illegal for instance
        //     members since such classes don't carry a this$n link.
        //     (`noOuterThisPath').
        Env<AttrContext> symEnv = env;

        DEBUG.P("symEnv="+symEnv);
        DEBUG.P("env.enclClass.sym.owner.kind="+Kinds.toString(env.enclClass.sym.owner.kind));
		DEBUG.P("sym="+sym);
		DEBUG.P("sym.kind="+Kinds.toString(sym.kind));
		DEBUG.P("sym.owner="+sym.owner);
		if(sym.owner!=null)
			DEBUG.P("sym.owner.kind="+Kinds.toString(sym.owner.kind));
        DEBUG.P(1);
        boolean noOuterThisPath = false;
        if (env.enclClass.sym.owner.kind != PCK && // we are in an inner class
            (sym.kind & (VAR | MTH | TYP)) != 0 &&
            sym.owner.kind == TYP &&
            tree.name != names._this && tree.name != names._super) {
			
			if(symEnv.outer != null) {
				DEBUG.P("sym="+sym);
				DEBUG.P("symEnv.enclClass.sym="+symEnv.enclClass.sym);
				DEBUG.P("symEnv.enclClass.sym.flags()="+Flags.toString(symEnv.enclClass.sym.flags()));
				DEBUG.P("sym.isMemberOf(symEnv.enclClass.sym)="+sym.isMemberOf(symEnv.enclClass.sym, types));
			}

            // Find environment in which identifier is defined.
            while (symEnv.outer != null &&
                   !sym.isMemberOf(symEnv.enclClass.sym, types)) {
                if ((symEnv.enclClass.sym.flags() & NOOUTERTHIS) != 0)
                    noOuterThisPath = !allowAnonOuterThis;
                symEnv = symEnv.outer;
            }
        }

		DEBUG.P(1);
		DEBUG.P("symEnv="+symEnv);

        // If symbol is a variable, ...
        if (sym.kind == VAR) {
            VarSymbol v = (VarSymbol)sym;

            // ..., evaluate its initializer, if it has one, and check for
            // illegal forward reference.
            checkInit(tree, env, v, false);

            // If symbol is a local variable accessed from an embedded
            // inner class check that it is final.

			DEBUG.P("v="+v);
			DEBUG.P("v.owner="+v.owner);
			DEBUG.P("env.info.scope.owner="+env.info.scope.owner);

			//在方法中定义的本地类或匿名本地类内部引用到方法的变量，变量必须是FINAL

            if (v.owner.kind == MTH &&
                v.owner != env.info.scope.owner &&
                (v.flags_field & FINAL) == 0) {
                log.error(tree.pos(),
                          "local.var.accessed.from.icls.needs.final",
                          v);
            }

            // If we are expecting a variable (as opposed to a value), check
            // that the variable is assignable in the current environment.
            DEBUG.P("pkind="+Kinds.toString(pkind));
			DEBUG.P("if (pkind == VAR)="+(pkind == VAR));
			if (pkind == VAR)
                checkAssignable(tree.pos(), v, null, env);
        }
        
        DEBUG.P("symEnv.info.isSelfCall="+symEnv.info.isSelfCall);
        DEBUG.P("noOuterThisPath="+noOuterThisPath);
        // In a constructor body,
        // if symbol is a field or instance method, check that it is
        // not accessed before the supertype constructor is called.
        if ((symEnv.info.isSelfCall || noOuterThisPath) &&
            (sym.kind & (VAR | MTH)) != 0 &&
            sym.owner.kind == TYP &&
            (sym.flags() & STATIC) == 0) {

			DEBUG.P("sym="+sym);
			DEBUG.P("sym.kind="+Kinds.toString(sym.kind));

            chk.earlyRefError(tree.pos(), sym.kind == VAR ? sym : thisSym(tree.pos(), env));
        }
		Env<AttrContext> env1 = env;
		DEBUG.P("env1="+env1);
		DEBUG.P("sym.owner="+sym.owner);
		DEBUG.P("env1.enclClass.sym="+env1.enclClass.sym);
		if (sym.kind != ERR && sym.owner != null && sym.owner != env1.enclClass.sym) {
		    // If the found symbol is inaccessible, then it is
		    // accessed through an enclosing instance.  Locate this
		    // enclosing instance:
		    DEBUG.P("env1.outer="+env1.outer);
		    while (env1.outer != null && !rs.isAccessible(env, env1.enclClass.sym.type, sym))
			env1 = env1.outer;
		}
		DEBUG.P("env1="+env1);
		DEBUG.P("env1.enclClass.sym="+env1.enclClass.sym);
        result = checkId(tree, env1.enclClass.sym.type, sym, env, pkind, pt, varArgs);
        
        DEBUG.P(0,this,"visitIdent(1)");
    }

    public void visitSelect(JCFieldAccess tree) {
		/*************************************************************
		pkind表示当前期待tree.type.tsym是pkind指定的类型
		例如pkind=PCK，就表示tree.type.tsym代表的是一个包(如:my.test)
		**************************************************************/
        // <editor-fold defaultstate="collapsed">
		try {
    	DEBUG.P(this,"visitSelect(1)");
    	DEBUG.P("tree.name="+tree.name);
		DEBUG.P("tree="+tree);
    	/*对于像Qualident = Ident { DOT Ident }这样的语法，
    	如果最后一个Ident是“this”、“super”、“class”，那么前
    	一个Ident的符号类型(symbol kind)只能是TYP，也就是说只有
    	类型名后面才能跟“this”、“super”、“class”；
    	
    	如果最后一个Ident符号类型是PCK，那么前一个Ident的符号类型
    	也是PCK，因为包名前面只能是包名；
    	
    	如果最后一个Ident符号类型是TYP，那么前一个Ident的符号类型
    	可以是TYP或PCK，因为类型名可以是内部类，这时前一个Ident
    	的符号类型就是TYP，否则只能是PCK；
    	
    	如果最后一个Ident符号类型是VAL或MTH，也就是当它是
    	变量或非变量表达式(variables or non-variable expressions)
    	或者是方法名的时候，那么前一个Ident的符号类型
    	可以是VAL或TYP。
    	*/
    	
        // Determine the expected kind of the qualifier expression.
        int skind = 0;
        if (tree.name == names._this || tree.name == names._super ||
            tree.name == names._class)
        {
            skind = TYP;
        } else {
            if ((pkind & PCK) != 0) skind = skind | PCK;
            if ((pkind & TYP) != 0) skind = skind | TYP | PCK;
			//注意:如果pkind=VAR，那么(pkind & (VAL | MTH)) != 0)是不等于0的
			//因为(VAR & VAL)!=0;
			//DEBUG.P("(VAR & VAL)="+(VAR & VAL));
            if ((pkind & (VAL | MTH)) != 0) skind = skind | VAL | TYP;
        }

        // Attribute the qualifier expression, and determine its symbol (if any).
        Type site = attribTree(tree.selected, env, skind, Infer.anyPoly);//Infer.anyPoly是一个Type(NONE, null)与JCNoType(NONE)类拟
        
        DEBUG.P("site.tag="+TypeTags.toString(site.tag));
        
        DEBUG.P("pkind="+Kinds.toString(pkind));
        DEBUG.P("skind="+Kinds.toString(skind));
        if ((pkind & (PCK | TYP)) == 0)
            site = capture(site); // Capture field access

        // don't allow T.class T[].class, etc
        if (skind == TYP) {
            Type elt = site;
            while (elt.tag == ARRAY)
                elt = ((ArrayType)elt).elemtype;
            if (elt.tag == TYPEVAR) {
                log.error(tree.pos(), "type.var.cant.be.deref");
                result = syms.errType;

				//我加上的，见if (tree.selected.type.tag == FORALL)的注释
				tree.type = syms.errType;
                return;
            }
        }
        
        // </editor-fold>
        
        // <editor-fold defaultstate="collapsed">

        // If qualifier symbol is a type or `super', assert `selectSuper'
        // for the selection. This is relevant for determining whether
        // protected symbols are accessible.
		DEBUG.P("tree.selected="+tree.selected);
        Symbol sitesym = TreeInfo.symbol(tree.selected);
        boolean selectSuperPrev = env.info.selectSuper;
        
        DEBUG.P("sitesym="+sitesym);
		if(sitesym==site.tsym)
			DEBUG.P("sitesym==site.tsym");
		else
			DEBUG.P("sitesym!=site.tsym");
        DEBUG.P("selectSuperPrev="+selectSuperPrev);
        
        env.info.selectSuper =
            sitesym != null &&
            sitesym.name == names._super;

        // If selected expression is polymorphic, strip
        // type parameters and remember in env.info.tvars, so that
        // they can be added later (in Attr.checkId and Infer.instantiateMethod).

		DEBUG.P("env.info.selectSuper="+env.info.selectSuper);
		try {
		DEBUG.P("tree="+tree);
		DEBUG.P("tree.selected="+tree.selected);
		DEBUG.P("tree.selected.type="+tree.selected.type);
		DEBUG.P("tree.selected.type.tag="+TypeTags.toString(tree.selected.type.tag));

		/*
		这里有NullPointerException
		当编译T t=T.super.toString();时，
		上面的skind = TYP，报告错误"无法从类型变量中进行选择"后返回，
		但是没有对(T.super)JCFieldAccess tree.type赋值，
		导致tree.selected.type = null;
		*/
        if (tree.selected.type.tag == FORALL) {
            ForAll pstype = (ForAll)tree.selected.type;
            env.info.tvars = pstype.tvars;
            site = tree.selected.type = pstype.qtype;
        }

		} catch (RuntimeException e) {
			System.err.println("出错了:"+e);
			e.printStackTrace();
			throw e;
		}
        
        // </editor-fold>
        
        // <editor-fold defaultstate="collapsed">
        // Determine the symbol represented by the selection.
        env.info.varArgs = false;
        Symbol sym = selectSym(tree, site, env, pt, pkind);
        
		DEBUG.P("tree="+tree);
		DEBUG.P("sym="+sym);
		DEBUG.P("sym.type="+sym.type);
		DEBUG.P("sym.kind="+Kinds.toString(sym.kind));
        DEBUG.P("sym.exists()="+sym.exists());
        DEBUG.P("isType(sym)="+isType(sym));
        DEBUG.P("pkind="+Kinds.toString(pkind));
        
        if (sym.exists() && !isType(sym) && (pkind & (PCK | TYP)) != 0) {
            site = capture(site);
            sym = selectSym(tree, site, env, pt, pkind);
        }
        boolean varArgs = env.info.varArgs;
        tree.sym = sym;
        
        DEBUG.P("env.info.varArgs="+env.info.varArgs);
        DEBUG.P("tree.sym="+tree.sym);
        DEBUG.P("site.tag="+TypeTags.toString(site.tag));
        
        if (site.tag == TYPEVAR && !isType(sym) && sym.kind != ERR)
            site = capture(site.getUpperBound());

        // If that symbol is a variable, ...
        if (sym.kind == VAR) {
            VarSymbol v = (VarSymbol)sym;

            // ..., evaluate its initializer, if it has one, and check for
            // illegal forward reference.
            checkInit(tree, env, v, true);

            // If we are expecting a variable (as opposed to a value), check
            // that the variable is assignable in the current environment.
            if (pkind == VAR)
                checkAssignable(tree.pos(), v, tree.selected, env);
        }
        
        // </editor-fold>
        
        // <editor-fold defaultstate="collapsed">
        
        DEBUG.P("isType(sym)="+isType(sym));
        DEBUG.P("sitesym="+sitesym);
        if(sitesym!=null) DEBUG.P("sitesym.kind="+Kinds.toString(sitesym.kind));
        
        // Disallow selecting a type from an expression
        if (isType(sym) && (sitesym==null || (sitesym.kind&(TYP|PCK)) == 0)) {
            tree.type = check(tree.selected, pt,
                              sitesym == null ? VAL : sitesym.kind, TYP|PCK, pt);
        }
        
        DEBUG.P("isType(sitesym)="+isType(sitesym));
        
        if (isType(sitesym)) {
        	DEBUG.P("sym.name="+sym.name);
            if (sym.name == names._this) {
                // If `C' is the currently compiled class, check that
                // C.this' does not appear in a call to a super(...)
                if (env.info.isSelfCall &&
                    site.tsym == env.enclClass.sym) {
                    chk.earlyRefError(tree.pos(), sym);
                }
            } else {
                // Check if type-qualified fields or methods are static (JLS)
				/*
					test\attr\VisitSelectTest.java:15: 无法从类型变量中进行选择
					public class VisitSelectTest<T extends B> extends A<T.b> {
																		 ^
					test\attr\VisitSelectTest.java:15: 无法从类型变量中进行选择
					public class VisitSelectTest<T extends B> extends A<T.b> {
																		 ^
					test\attr\VisitSelectTest.java:19: 无法从静态上下文中引用非静态 变量 b
							B b=T.b;
								 ^
					test\attr\VisitSelectTest.java:20: 无法从静态上下文中引用非静态 方法 b()
							B b2=T.b();
								  ^
					4 错误
					class A<T>{}
					class B {
						//int i;
						B b;
						B b(){ return new B(); }
						class b{}
					}
					public class VisitSelectTest<T extends B> extends A<T.b> {
						//A<T.i> al;

						//A<T.b> al;
						B b=T.b;
						B b2=T.b();
					}
				*/
                if ((sym.flags() & STATIC) == 0 &&
                    sym.name != names._super &&
                    (sym.kind == VAR || sym.kind == MTH)) {
                    rs.access(rs.new StaticError(sym),
                              tree.pos(), site, sym.name, true);
                }
            }
        }
        
        // </editor-fold>
        
        // <editor-fold defaultstate="collapsed">
        
        DEBUG.P("env.info.selectSuper="+env.info.selectSuper);
        DEBUG.P("sym.flags_field="+Flags.toString(sym.flags_field));
        // If we are selecting an instance member via a `super', ...
        if (env.info.selectSuper && (sym.flags() & STATIC) == 0) {
			/*
				class ClassA<T>{
					void m(){};
				}
				public class VisitSelectTest extends ClassA {
					void m() {super.m();} //site.isRaw()=true
				}


				abstract class ClassA{
					abstract void m();
				}
				public class VisitSelectTest extends ClassA {
					void m() {super.m();} //无法直接访问 test.attr.ClassA 中的抽象 方法
				}
			*/
            // Check that super-qualified symbols are not abstract (JLS)
            rs.checkNonAbstract(tree.pos(), sym);

			DEBUG.P("site="+site);
			DEBUG.P("site.isRaw()="+site.isRaw());
            if (site.isRaw()) {
                // Determine argument types for site.
                Type site1 = types.asSuper(env.enclClass.sym.type, site.tsym);
                
				DEBUG.P("(site1 == site)="+(site1 == site));
				if (site1 != null) site = site1;
            }
        }

        env.info.selectSuper = selectSuperPrev;
        result = checkId(tree, site, sym, env, pkind, pt, varArgs);
        env.info.tvars = List.nil();
        
        
		}finally{//我加上的
        DEBUG.P(0,this,"visitSelect(1)");
        }
        // </editor-fold>
    }
    //where
        /** Determine symbol referenced by a Select expression,
         *
         *  @param tree   The select tree.
         *  @param site   The type of the selected expression,
         *  @param env    The current environment.
         *  @param pt     The current prototype.
         *  @param pkind  The expected kind(s) of the Select expression.
         */
        private Symbol selectSym(JCFieldAccess tree,
                                 Type site,
                                 Env<AttrContext> env,
                                 Type pt,
                                 int pkind) {
            try {//我加上的
            DEBUG.P(this,"selectSym(5)");
            DEBUG.P("tree="+tree);
            DEBUG.P("site="+site); 
            DEBUG.P("site.tag="+TypeTags.toString(site.tag));   
            DEBUG.P("env="+env);
            DEBUG.P("pt="+pt); 
            DEBUG.P("pt.tag="+TypeTags.toString(pt.tag));
            DEBUG.P("pkind="+Kinds.toString(pkind));
			
            DiagnosticPosition pos = tree.pos();
            Name name = tree.name;

			DEBUG.P("name="+name);

            switch (site.tag) {
            case PACKAGE:
                return rs.access(
                    rs.findIdentInPackage(env, site.tsym, name, pkind),
                    pos, site, name, true);
            case ARRAY:
            case CLASS:
                if (pt.tag == METHOD || pt.tag == FORALL) {
                    return rs.resolveQualifiedMethod(
                        pos, env, site, name, pt.getParameterTypes(), pt.getTypeArguments());
				//此处不处理像c.super()或c.this()(语法错误)这样的情形
				//而是在visitApply(1)中处理
                } else if (name == names._this || name == names._super) {
                    return rs.resolveSelf(pos, env, site.tsym, name);
                } else if (name == names._class) {
                    // In this case, we have already made sure in
                    // visitSelect that qualifier expression is a type.
                    Type t = syms.classType;
                    List<Type> typeargs = allowGenerics
                        ? List.of(types.erasure(site))
                        : List.<Type>nil();
                    t = new ClassType(t.getEnclosingType(), typeargs, t.tsym);
                    return new VarSymbol(
                        STATIC | PUBLIC | FINAL, names._class, t, site.tsym);
                } else {
                    // We are seeing a plain identifier as selector.
                    Symbol sym = rs.findIdentInType(env, site, name, pkind);
                    if ((pkind & ERRONEOUS) == 0)
                        sym = rs.access(sym, pos, site, name, true);
                    return sym;
                }
            case WILDCARD:
                throw new AssertionError(tree);
            case TYPEVAR:
                // Normally, site.getUpperBound() shouldn't be null.
                // It should only happen during memberEnter/attribBase
                // when determining the super type which *must* be
                // done before attributing the type variables.  In
                // other words, we are seeing this illegal program:
                // class B<T> extends A<T.foo> {}
				/*
					test\attr\VisitSelectTest.java:15: 无法从类型变量中进行选择
					public class VisitSelectTest<T extends B> extends A<T.b> {
																		 ^
					test\attr\VisitSelectTest.java:15: 无法从类型变量中进行选择
					public class VisitSelectTest<T extends B> extends A<T.b> {
																		 ^
					test\attr\VisitSelectTest.java:19: 无法从静态上下文中引用非静态 变量 b
							B b=T.b;
								 ^
					test\attr\VisitSelectTest.java:20: 无法从静态上下文中引用非静态 方法 b()
							B b2=T.b();
								  ^
					4 错误
					class A<T>{}
					class B {
						//int i;
						B b;
						B b(){ return new B(); }
						class b{}
					}
					public class VisitSelectTest<T extends B> extends A<T.b> {
						//A<T.i> al;

						//A<T.b> al;
						B b=T.b;
						B b2=T.b();
					}
				*/
				DEBUG.P("site.getUpperBound()="+site.getUpperBound()); 
                Symbol sym = (site.getUpperBound() != null)
                    ? selectSym(tree, capture(site.getUpperBound()), env, pt, pkind)
                    : null;
				DEBUG.P("sym="+sym); 
				DEBUG.P("selectSym(5) isType(sym)="+isType(sym)); 
				if(sym!=null)DEBUG.P("sym.kind="+Kinds.toString(sym.kind));
				/*
				class A{
					C c;
					static class C{
						static int i;
					}
				}
				class D<T extends A> {
					Class<?> c = T.class;
					int i = T.C.i; //isType(sym)=true
					A.C c = T.c; //无法从静态上下文中引用非静态 变量 c
				}
				*/
                if (sym == null || isType(sym)) {
                    log.error(pos, "type.var.cant.be.deref");
                    return syms.errSymbol;
                } else {
                    return sym;
                }
            case ERROR:
                // preserve identifier names through errors
                return new ErrorType(name, site.tsym).tsym;
            default:
                // The qualifier expression is of a primitive type -- only
                // .class is allowed for these.
                if (name == names._class) {
                    // In this case, we have already made sure in Select that
                    // qualifier expression is a type.
                    Type t = syms.classType;
                    Type arg = types.boxedClass(site).type;
                    t = new ClassType(t.getEnclosingType(), List.of(arg), t.tsym);
                    return new VarSymbol(
                        STATIC | PUBLIC | FINAL, names._class, t, site.tsym);
                } else {
					/*
						test\attr\VisitSelectTest.java:8: 无法取消引用 int
										int c = t.t;
												 ^
						1 错误
						void m(int t){
							int c = t.t;
						}
					*/
                    log.error(pos, "cant.deref", site);
                    return syms.errSymbol;
                }
            }
            
            }finally{//我加上的
            DEBUG.P(0,this,"selectSym(5)");
            }
        }

        /** Determine type of identifier or select expression and check that
         *  (1) the referenced symbol is not deprecated
         *  (2) the symbol's type is safe (@see checkSafe)
         *  (3) if symbol is a variable, check that its type and kind are
         *      compatible with the prototype and protokind.
         *  (4) if symbol is an instance field of a raw type,
         *      which is being assigned to, issue an unchecked warning if its
         *      type changes under erasure.
         *  (5) if symbol is an instance method of a raw type, issue an
         *      unchecked warning if its argument types change under erasure.
         *  If checks succeed:
         *    If symbol is a constant, return its constant type
         *    else if symbol is a method, return its result type
         *    otherwise return its type.
         *  Otherwise return errType.
         *
         *  @param tree       The syntax tree representing the identifier
         *  @param site       If this is a select, the type of the selected
         *                    expression, otherwise the type of the current class.
         *  @param sym        The symbol representing the identifier.
         *  @param env        The current environment.
         *  @param pkind      The set of expected kinds.
         *  @param pt         The expected type.
         */
        Type checkId(JCTree tree,
                     Type site,
                     Symbol sym,
                     Env<AttrContext> env,
                     int pkind,
                     Type pt,
                     boolean useVarargs) {
            try {//我加上的
            DEBUG.P(this,"checkId(7)");
            DEBUG.P("env="+env);
            DEBUG.P("sym="+sym);
            DEBUG.P("sym.kind="+Kinds.toString(sym.kind));
			
			
            if (pt.isErroneous()) return syms.errType;
            Type owntype; // The computed type of this identifier occurrence.
            switch (sym.kind) {
            case TYP:
                // <editor-fold defaultstate="collapsed">
                // For types, the computed type equals the symbol's type,
                // except for two situations:
                owntype = sym.type;
                DEBUG.P("owntype.tag="+TypeTags.toString(owntype.tag));
                if (owntype.tag == CLASS) {
                    Type ownOuter = owntype.getEnclosingType();
                    DEBUG.P("ownOuter="+ownOuter);
                    DEBUG.P("ownOuter.tag="+TypeTags.toString(ownOuter.tag));
					DEBUG.P("site="+site);
                    DEBUG.P("site != ownOuter="+(site != ownOuter));
                    DEBUG.P("owntype.tsym.type.getTypeArguments()="+owntype.tsym.type.getTypeArguments());

                    // (a) If the symbol's type is parameterized, erase it
                    // because no type parameters were given.
                    // We recover generic outer type later in visitTypeApply.
                    if (owntype.tsym.type.getTypeArguments().nonEmpty()) {
                        owntype = types.erasure(owntype);
                        DEBUG.P("owntype="+owntype);
                        DEBUG.P("owntype.tag="+TypeTags.toString(owntype.tag));
                    }

                    // (b) If the symbol's type is an inner class, then
                    // we have to interpret its outer type as a superclass
                    // of the site type. Example:
                    //
                    // class Tree<A> { class Visitor { ... } }
                    // class PointTree extends Tree<Point> { ... }
                    // ...PointTree.Visitor...
                    //
                    // Then the type of the last expression above is
                    // Tree<Point>.Visitor.
                    else if (ownOuter.tag == CLASS && site != ownOuter) {
                        Type normOuter = site;
						DEBUG.P("site="+site); 
						DEBUG.P("ownOuter="+ownOuter);
						DEBUG.P("ownOuter.tsym="+ownOuter.tsym);
                        DEBUG.P("normOuter.tag="+TypeTags.toString(normOuter.tag));
                        if (normOuter.tag == CLASS)
                            normOuter = types.asEnclosingSuper(site, ownOuter.tsym);
                       
                        DEBUG.P("normOuter="+normOuter);    
                        if (normOuter == null) // perhaps from an import
                            normOuter = types.erasure(ownOuter);

                        DEBUG.P("normOuter="+normOuter);
						DEBUG.P("ownOuter ="+ownOuter);
                        DEBUG.P("normOuter != ownOuter="+(normOuter != ownOuter));
                        if (normOuter != ownOuter)
                            owntype = new ClassType(
                                normOuter, List.<Type>nil(), owntype.tsym);
                        DEBUG.P("owntype="+owntype);
                        DEBUG.P("owntype.tag="+TypeTags.toString(owntype.tag));
                    }
                }
                break;
                // </editor-fold>
            case VAR:
                // <editor-fold defaultstate="collapsed">
                VarSymbol v = (VarSymbol)sym;
                // Test (4): if symbol is an instance field of a raw type,
                // which is being assigned to, issue an unchecked warning if
                // its type changes under erasure.
                if (allowGenerics &&
                    pkind == VAR &&
                    v.owner.kind == TYP &&
                    (v.flags() & STATIC) == 0 &&
                    (site.tag == CLASS || site.tag == TYPEVAR)) {
                    Type s = types.asOuterSuper(site, v.owner);
                    if (s != null &&
                        s.isRaw() &&
                        !types.isSameType(v.type, v.erasure(types))) {
                        chk.warnUnchecked(tree.pos(),
                                          "unchecked.assign.to.var",
                                          v, s);
                    }
                }
                // The computed type of a variable is the type of the
                // variable symbol, taken as a member of the site type.
                owntype = (sym.owner.kind == TYP &&
                           sym.name != names._this && sym.name != names._super)
                    ? types.memberType(site, sym)
                    : sym.type;

                if (env.info.tvars.nonEmpty()) {
                    Type owntype1 = new ForAll(env.info.tvars, owntype);
                    for (List<Type> l = env.info.tvars; l.nonEmpty(); l = l.tail)
                        if (!owntype.contains(l.head)) {
                            log.error(tree.pos(), "undetermined.type", owntype1);
                            owntype1 = syms.errType;
                        }
                    owntype = owntype1;
                }

                // If the variable is a constant, record constant value in
                // computed type.
                if (v.getConstValue() != null && isStaticReference(tree))
                    owntype = owntype.constType(v.getConstValue());

                if (pkind == VAL) {
                    owntype = capture(owntype); // capture "names as expressions"
                }
                break;
                // </editor-fold>
            case MTH: {
                JCMethodInvocation app = (JCMethodInvocation)env.tree;
                owntype = checkMethod(site, sym, env, app.args,
                                      pt.getParameterTypes(), pt.getTypeArguments(),
                                      env.info.varArgs);
                break;
            }
            case PCK: case ERR:
                owntype = sym.type;
                DEBUG.P("owntype.tag="+TypeTags.toString(owntype.tag));
                break;
            default:
                throw new AssertionError("unexpected kind: " + sym.kind +
                                         " in tree " + tree);
            }
            DEBUG.P("sym.flags_field="+Flags.toString(sym.flags_field));

            // Test (1): emit a `deprecation' warning if symbol is deprecated.
            // (for constructors, the error was given when the constructor was
            // resolved)
            if (sym.name != names.init &&
                (sym.flags() & DEPRECATED) != 0 &&
                (env.info.scope.owner.flags() & DEPRECATED) == 0 &&
                sym.outermostClass() != env.info.scope.owner.outermostClass())
                chk.warnDeprecated(tree.pos(), sym);

            if ((sym.flags() & PROPRIETARY) != 0)
                log.strictWarning(tree.pos(), "sun.proprietary", sym);
                
            // Test (3): if symbol is a variable, check that its type and
            // kind are compatible with the prototype and protokind.
            return check(tree, owntype, sym.kind, pkind, pt);
            
            
            }finally{//我加上的
            DEBUG.P(0,this,"checkId(7)");
            }
        }

        /** Check that variable is initialized and evaluate the variable's
         *  initializer, if not yet done. Also check that variable is not
         *  referenced before it is defined.
         *  @param tree    The tree making up the variable reference.
         *  @param env     The current environment.
         *  @param v       The variable's symbol.
         */
        private void checkInit(JCTree tree,
                               Env<AttrContext> env,
                               VarSymbol v,
                               boolean onlyWarning) {
			DEBUG.P(this,"checkInit(4)");
			DEBUG.P("tree="+tree);
			DEBUG.P("v="+v);
			DEBUG.P("onlyWarning="+onlyWarning);
			DEBUG.P("v.pos="+v.pos);
			DEBUG.P("tree.pos="+tree.pos);
			DEBUG.P("v.owner.kind="+Kinds.toString(v.owner.kind));
			//          System.err.println(v + " " + ((v.flags() & STATIC) != 0) + " " +
			//                             tree.pos + " " + v.pos + " " +
			//                             Resolve.isStatic(env));//DEBUG

            // A forward reference is diagnosed if the declaration position
            // of the variable is greater than the current tree position
            // and the tree and variable definition occur in the same class
            // definition.  Note that writes don't count as references.
            // This check applies only to class and instance
            // variables.  Local variables follow different scope rules,
            // and are subject to definite assignment checking.
            if (v.pos > tree.pos &&
                v.owner.kind == TYP &&
                canOwnInitializer(env.info.scope.owner) &&
                v.owner == env.info.scope.owner.enclClass() &&
                ((v.flags() & STATIC) != 0) == Resolve.isStatic(env) &&
                (env.tree.tag != JCTree.ASSIGN ||
                 TreeInfo.skipParens(((JCAssign) env.tree).lhs) != tree)) {

                if (!onlyWarning || isNonStaticEnumField(v)) {
                    log.error(tree.pos(), "illegal.forward.ref");
                } else if (useBeforeDeclarationWarning) {
                    log.warning(tree.pos(), "forward.ref", v);
                }
            }

            v.getConstValue(); // ensure initializer is evaluated

            checkEnumInitializer(tree, env, v);

			DEBUG.P(0,this,"checkInit(4)");
        }

        /**
         * Check for illegal references to static members of enum.  In
         * an enum type, constructors and initializers may not
         * reference its static members unless they are constant.
         *
         * @param tree    The tree making up the variable reference.
         * @param env     The current environment.
         * @param v       The variable's symbol.
         * @see JLS 3rd Ed. (8.9 Enums)
         */
        private void checkEnumInitializer(JCTree tree, Env<AttrContext> env, VarSymbol v) {
            // JLS 3rd Ed.:
            //
            // "It is a compile-time error to reference a static field
            // of an enum type that is not a compile-time constant
            // (15.28) from constructors, instance initializer blocks,
            // or instance variable initializer expressions of that
            // type. It is a compile-time error for the constructors,
            // instance initializer blocks, or instance variable
            // initializer expressions of an enum constant e to refer
            // to itself or to an enum constant of the same type that
            // is declared to the right of e."
            if (isNonStaticEnumField(v)) {
                ClassSymbol enclClass = env.info.scope.owner.enclClass();

                if (enclClass == null || enclClass.owner == null)
                    return;

                // See if the enclosing class is the enum (or a
                // subclass thereof) declaring v.  If not, this
                // reference is OK.
                if (v.owner != enclClass && !types.isSubtype(enclClass.type, v.owner.type))
                    return;

                // If the reference isn't from an initializer, then
                // the reference is OK.
                if (!Resolve.isInitializer(env))
                    return;

                log.error(tree.pos(), "illegal.enum.static.ref");
            }
        }

        private boolean isNonStaticEnumField(VarSymbol v) {
            return Flags.isEnum(v.owner) && Flags.isStatic(v) && !Flags.isConstant(v);
        }

        /** Can the given symbol be the owner of code which forms part
         *  if class initialization? This is the case if the symbol is
         *  a type or field, or if the symbol is the synthetic method.
         *  owning a block.
         */
        private boolean canOwnInitializer(Symbol sym) {
            return
                (sym.kind & (VAR | TYP)) != 0 ||
                (sym.kind == MTH && (sym.flags() & BLOCK) != 0);
        }

    Warner noteWarner = new Warner();

    /**
     * Check that method arguments conform to its instantation.
     **/
    public Type checkMethod(Type site,
                            Symbol sym,
                            Env<AttrContext> env,
                            final List<JCExpression> argtrees,
                            List<Type> argtypes,
                            List<Type> typeargtypes,
                            boolean useVarargs) {
		DEBUG.P(this,"checkMethod(7)");
		DEBUG.P("site="+site);
		DEBUG.P("sym="+sym);
		DEBUG.P("argtrees="+argtrees);
		DEBUG.P("argtypes="+argtypes);
		DEBUG.P("typeargtypes="+typeargtypes);
		DEBUG.P("useVarargs="+useVarargs);

        // Test (5): if symbol is an instance method of a raw type, issue
        // an unchecked warning if its argument types change under erasure.
        if (allowGenerics &&
            (sym.flags() & STATIC) == 0 &&
            (site.tag == CLASS || site.tag == TYPEVAR)) {
			/*如:
				class VisitNewClassTest<T> {
					VisitNewClassTest vct = new VisitNewClassTest(this);
					VisitNewClassTest(VisitNewClassTest<T> t){}
				}
			*/
            Type s = types.asOuterSuper(site, sym.owner);
            if (s != null && s.isRaw() &&
                !types.isSameTypes(sym.type.getParameterTypes(),
                                   sym.erasure(types).getParameterTypes())) {
                chk.warnUnchecked(env.tree.pos(),
                                  "unchecked.call.mbr.of.raw.type",
                                  sym, s);
            }
        }

        // Compute the identifier's instantiated type.
        // For methods, we need to compute the instance type by
        // Resolve.instantiate from the symbol's type as well as
        // any type arguments and value arguments.
        noteWarner.warned = false;
        Type owntype = rs.instantiate(env,
                                      site,
                                      sym,
                                      argtypes,
                                      typeargtypes,
                                      true,
                                      useVarargs,
                                      noteWarner);
        boolean warned = noteWarner.warned;

        // If this fails, something went wrong; we should not have
        // found the identifier in the first place.
        if (owntype == null) {
            if (!pt.isErroneous())
                log.error(env.tree.pos(),
                          "internal.error.cant.instantiate",
                          sym, site,
                          Type.toString(pt.getParameterTypes()));
            owntype = syms.errType;
        } else {
            // System.out.println("call   : " + env.tree);
            // System.out.println("method : " + owntype);
            // System.out.println("actuals: " + argtypes);
            List<Type> formals = owntype.getParameterTypes();
            Type last = useVarargs ? formals.last() : null;
            if (sym.name==names.init &&
                sym.owner == syms.enumSym)
                formals = formals.tail.tail;
            List<JCExpression> args = argtrees;
            while (formals.head != last) {
                JCTree arg = args.head;
                Warner warn = chk.convertWarner(arg.pos(), arg.type, formals.head);
                assertConvertible(arg, arg.type, formals.head, warn);
                warned |= warn.warned;
                args = args.tail;
                formals = formals.tail;
            }
            if (useVarargs) {
                Type varArg = types.elemtype(last);
                while (args.tail != null) {
                    JCTree arg = args.head;
                    Warner warn = chk.convertWarner(arg.pos(), arg.type, varArg);
                    assertConvertible(arg, arg.type, varArg, warn);
                    warned |= warn.warned;
                    args = args.tail;
                }
            } else if ((sym.flags() & VARARGS) != 0 && allowVarargs) {
                // non-varargs call to varargs method
                Type varParam = owntype.getParameterTypes().last();
                Type lastArg = argtypes.last();
                if (types.isSubtypeUnchecked(lastArg, types.elemtype(varParam)) &&
                    !types.isSameType(types.erasure(varParam), types.erasure(lastArg)))
                    log.warning(argtrees.last().pos(), "inexact.non-varargs.call",
                                types.elemtype(varParam),
                                varParam);
            }

            if (warned && sym.type.tag == FORALL) {
                String typeargs = "";
                if (typeargtypes != null && typeargtypes.nonEmpty()) {
                    typeargs = "<" + Type.toString(typeargtypes) + ">";
                }
                chk.warnUnchecked(env.tree.pos(),
                                  "unchecked.meth.invocation.applied",
                                  sym,
                                  sym.location(),
                                  typeargs,
                                  Type.toString(argtypes));
                owntype = new MethodType(owntype.getParameterTypes(),
                                         types.erasure(owntype.getReturnType()),
                                         owntype.getThrownTypes(),
                                         syms.methodClass);
            }
            if (useVarargs) {
                JCTree tree = env.tree;
                Type argtype = owntype.getParameterTypes().last();
                if (!types.isReifiable(argtype))
                    chk.warnUnchecked(env.tree.pos(),
                                      "unchecked.generic.array.creation",
                                      argtype);
                Type elemtype = types.elemtype(argtype);
                switch (tree.tag) {
                case JCTree.APPLY:
                    ((JCMethodInvocation) tree).varargsElement = elemtype;
                    break;
                case JCTree.NEWCLASS:
                    ((JCNewClass) tree).varargsElement = elemtype;
                    break;
                default:
                    throw new AssertionError(""+tree);
                }
            }
        }

		DEBUG.P("owntype="+owntype);
		DEBUG.P(0,this,"checkMethod(7)");
        return owntype;
    }

    private void assertConvertible(JCTree tree, Type actual, Type formal, Warner warn) {
        if (types.isConvertible(actual, formal, warn))
            return;

        if (formal.isCompound()
            && types.isSubtype(actual, types.supertype(formal))
            && types.isSubtypeUnchecked(actual, types.interfaces(formal), warn))
            return;

        if (false) {
            // TODO: make assertConvertible work
            chk.typeError(tree.pos(), JCDiagnostic.fragment("incompatible.types"), actual, formal);
            throw new AssertionError("Tree: " + tree
                                     + " actual:" + actual
                                     + " formal: " + formal);
        }
    }

    public void visitLiteral(JCLiteral tree) {
		DEBUG.P(this,"visitLiteral(1)");
		DEBUG.P("tree="+tree);

        result = check(
            tree, litType(tree.typetag).constType(tree.value), VAL, pkind, pt);

		if(result!=null) {
			DEBUG.P("result="+result);
			DEBUG.P("result.constValue()="+result.constValue());
		}
		DEBUG.P(0,this,"visitLiteral(1)");
    }
    //where
    /** Return the type of a literal with given type tag.
     */
    Type litType(int tag) {
        return (tag == TypeTags.CLASS) ? syms.stringType : syms.typeOfTag[tag];
    }

	//原始类型名，如int long等等，
    public void visitTypeIdent(JCPrimitiveTypeTree tree) {
    	DEBUG.P(this,"visitTypeIdent(JCPrimitiveTypeTree tree)");
		DEBUG.P("tree="+tree);
		
        result = check(tree, syms.typeOfTag[tree.typetag], TYP, pkind, pt);
        
        DEBUG.P(0,this,"visitTypeIdent(JCPrimitiveTypeTree tree)");
    }

	//如:int[]
    public void visitTypeArray(JCArrayTypeTree tree) {
    	DEBUG.P(this,"visitTypeArray(JCArrayTypeTree tree)");
		DEBUG.P("tree="+tree);
		
        Type etype = attribType(tree.elemtype, env);
        Type type = new ArrayType(etype, syms.arrayClass);
        result = check(tree, type, TYP, pkind, pt);
        
        DEBUG.P(0,this,"visitTypeArray(JCArrayTypeTree tree)");
    }

    /** Visitor method for parameterized types.
     *  Bound checking is left until later, since types are attributed
     *  before supertype structure is completely known
     */
	//如:List<String>
    public void visitTypeApply(JCTypeApply tree) {
		DEBUG.P(this,"visitTypeApply(1)");
		DEBUG.P("tree="+tree);
		DEBUG.P("tree.clazz="+tree.clazz);
		DEBUG.P("tree.arguments="+tree.arguments);
		
        Type owntype = syms.errType;

        // Attribute functor part of application and make sure it's a class.
        Type clazztype = chk.checkClassType(tree.clazz.pos(), attribType(tree.clazz, env));
        
        // Attribute type parameters
        List<Type> actuals = attribTypes(tree.arguments, env);
        
        DEBUG.P("");
        DEBUG.P("actuals="+actuals);
        DEBUG.P("clazztype="+clazztype);
        DEBUG.P("clazztype.tag="+TypeTags.toString(clazztype.tag));
        if (clazztype.tag == CLASS) {
            List<Type> formals = clazztype.tsym.type.getTypeArguments();
            DEBUG.P("formals="+formals);
            
            DEBUG.P("actuals.length()="+actuals.length());
            DEBUG.P("formals.length()="+formals.length());
            if (actuals.length() == formals.length()) {
                List<Type> a = actuals;
                List<Type> f = formals;
                while (a.nonEmpty()) {
					/*如:
					class Aclass<T> {
						Aclass<?> a;
					}
					com.sun.tools.javac.code.Type$WildcardType===>withTypeVar(Type t)
					-------------------------------------------------------------------------
					bound=null
					t    =T {bound=Object}

					泛型类形参：T {bound=Object}
					泛型类实参：?
					com.sun.tools.javac.code.Type$WildcardType===>withTypeVar(Type t)  END
					-------------------------------------------------------------------------
					*/
                    a.head = a.head.withTypeVar(f.head);//只对WildcardType有用
                    a = a.tail;
                    f = f.tail;
                }
                // Compute the proper generic outer
                Type clazzOuter = clazztype.getEnclosingType();
                DEBUG.P("");
                DEBUG.P("clazzOuter="+clazzOuter);
        		DEBUG.P("clazzOuter.tag="+TypeTags.toString(clazzOuter.tag));
                if (clazzOuter.tag == CLASS) {
                	DEBUG.P("tree.clazz="+tree.clazz);
        			DEBUG.P("tree.clazz.tag="+tree.clazz.myTreeTag());
        			DEBUG.P("env="+env);
                    Type site;
                    if (tree.clazz.tag == JCTree.IDENT) {
                        site = env.enclClass.sym.type;
                    } else if (tree.clazz.tag == JCTree.SELECT) {
                        site = ((JCFieldAccess) tree.clazz).selected.type;
                    } else throw new AssertionError(""+tree);
                    
                    DEBUG.P("site="+site);
        			DEBUG.P("site.tag="+TypeTags.toString(site.tag));
        			DEBUG.P("(clazzOuter.tag == CLASS && site != clazzOuter)="+(clazzOuter.tag == CLASS && site != clazzOuter));
					/*例:
						import test.attr.Aclass.*;
						class Aclass<T> {
							class Bclass<V>{
								//site=test.attr.Aclass<T {bound=Object}>.Bclass<V {bound=Object}>
								//site.tag=CLASS
								(clazzOuter.tag == CLASS && site != clazzOuter)=true
								//site=test.attr.Aclass<T {bound=Object}>
								//clazzOuter=test.attr.Aclass
								//clazzOuter=test.attr.Aclass<T {bound=Object}>
								Bclass<Aclass3> b1;
							}
						}

						class Aclass2<T> {
							//类型的格式不正确，给出了普通类型的类型参数
							//因为import中导入的Aclass不带参数，
							//相当于“Aclass.Bclass<Aclass3> b2;”这样的格式是错误的
							//site = types.asOuterSuper(site, clazzOuter.tsym)=null
							//最后clazzOuter=test.attr.Aclass
							Bclass<Aclass3> b2;

							//第一个clazzOuter=test.attr.Aclass，
							//但是site=test.attr.Aclass<test.attr.Aclass3>
							//所以(clazzOuter.tag == CLASS && site != clazzOuter)=true
							//接着site = types.asOuterSuper(site, clazzOuter.tsym);
							//返回site=test.attr.Aclass<test.attr.Aclass3>
							//最后clazzOuter=test.attr.Aclass<test.attr.Aclass3>
							Aclass<Aclass3>.Bclass<Aclass3> b3;
						}
						class Aclass3{}
					*/
                    if (clazzOuter.tag == CLASS && site != clazzOuter) {
                        if (site.tag == CLASS)
                            site = types.asOuterSuper(site, clazzOuter.tsym);

						DEBUG.P("site="+site);
						DEBUG.P("clazzOuter="+clazzOuter);
                        if (site == null)
                            site = types.erasure(clazzOuter);
                        clazzOuter = site;
                    }
                }
				DEBUG.P("clazzOuter="+clazzOuter);
                owntype = new ClassType(clazzOuter, actuals, clazztype.tsym);
            } else {
                if (formals.length() != 0) {
                	/*例子:
                	class ExtendsTest<T,S,B>  {}
                	public class MyTestInnerClass
					<Z extends ExtendsTest<?,? super ExtendsTest>> 
					
					错误提示(中文):
					bin\mysrc\my\test\Test.java:8: 类型变量数目错误；需要 3
			        MyTestInnerClass<Z extends ExtendsTest<?,? super ExtendsTest>>
			                                              ^
			        错误提示(英文):
			        bin\mysrc\my\test\Test.java:8: wrong number of type arguments; required 3
			        MyTestInnerClass<Z extends ExtendsTest<?,? super ExtendsTest>>
			                                              ^
			        注:中文错误提示翻译不准确,“type arguments”不能翻译成“类型变量”，
			        “类型变量”是特指泛型类定义中的“类型变量”，如Test<T>，“T”就是
			        一个“类型变量”，而“type arguments”是指参数化后的泛型类的参数，
			        如Test<String>，String就是一个“type argument”，所以准确一点的
			        翻译应该是“类型参数数目错误”。
			        */                                     
					
                    log.error(tree.pos(), "wrong.number.type.args",
                              Integer.toString(formals.length()));
                } else {
                	/*例子:
                	class ExtendsTest{}
                	public class MyTestInnerClass
					<Z extends ExtendsTest<?,? super ExtendsTest>> 
					
					错误提示(中文):
					bin\mysrc\my\test\Test.java:8: 类型 my.test.ExtendsTest 不带有参数
			        MyTestInnerClass<Z extends ExtendsTest<?,? super ExtendsTest>>
			                                              ^
			        错误提示(英文):
			        bin\mysrc\my\test\Test.java:8: type my.test.ExtendsTest does not take parameters
			        MyTestInnerClass<Z extends ExtendsTest<?,? super ExtendsTest>>
			                                              ^
			        */                                  
                    log.error(tree.pos(), "type.doesnt.take.params", clazztype.tsym);
                }
                owntype = syms.errType;
            }
        }
        result = check(tree, owntype, TYP, pkind, pt);
        
        DEBUG.P("tree.type="+tree.type);
        DEBUG.P("tree.type.tsym.type="+tree.type.tsym.type);
		DEBUG.P(0,this,"visitTypeApply(1)");
    }
    
    //b10
    public void visitTypeParameter(JCTypeParameter tree) {
    	try {//我加上的
		DEBUG.P(this,"visitTypeParameter(1)");
		DEBUG.P("tree="+tree);
		DEBUG.P("tree.type.tag="+TypeTags.toString(tree.type.tag));
		DEBUG.P("tree.bounds="+tree.bounds);
		
        TypeVar a = (TypeVar)tree.type;
        Set<Type> boundSet = new HashSet<Type>();

		DEBUG.P("a.bound.isErroneous()="+a.bound.isErroneous());

        if (a.bound.isErroneous())
            return;
        List<Type> bs = types.getBounds(a);
        if (tree.bounds.nonEmpty()) {
            // accept class or interface or typevar as first bound.
            Type b = checkBase(bs.head, tree.bounds.head, env, false, false, false);
            boundSet.add(types.erasure(b));
            DEBUG.P("b.tag="+TypeTags.toString(b.tag));
            if (b.tag == TYPEVAR) {
            	/*错误例子:
					bin\mysrc\my\test\Test.java:8: 类型变量后面不能带有其他限制范围
					public class Test<S,T extends ExtendsTest,E extends S & MyInterfaceA> extends my
					.ExtendsTest.MyInnerClassStatic {
																			^
				*/
                // if first bound was a typevar, do not accept further bounds.
                if (tree.bounds.tail.nonEmpty()) {
                    log.error(tree.bounds.tail.head.pos(),
                              "type.var.may.not.be.followed.by.other.bounds");
                    tree.bounds = List.of(tree.bounds.head);
                }
            } else {
                // if first bound was a class or interface, accept only interfaces
                // as further bounds.
                for (JCExpression bound : tree.bounds.tail) {
                    bs = bs.tail;
                    Type i = checkBase(bs.head, bound, env, false, true, false);
                    if (i.tag == CLASS)
                        chk.checkNotRepeated(bound.pos(), types.erasure(i), boundSet);
                }
            }
        }
        bs = types.getBounds(a);

		//对于TF extends TA&InterfaceA,TG extends SuperClassA & InterfaceA & TB
		//这样的复合类型不管对不对，这里都生成一个JCClassDecl
        
        DEBUG.P("bs="+bs);
        DEBUG.P("bs.length()="+bs.length());
        // in case of multiple bounds ...
        if (bs.length() > 1) {
            // ... the variable's bound is a class type flagged COMPOUND
            // (see comment for TypeVar.bound).
            // In this case, generate a class tree that represents the
            // bound class, ...
            JCTree extending;
            List<JCExpression> implementing;
            if ((bs.head.tsym.flags() & INTERFACE) == 0) {
                extending = tree.bounds.head;
                implementing = tree.bounds.tail;
            } else {
                extending = null;
                implementing = tree.bounds;
            }
            
            DEBUG.P("tree.name="+tree.name);
            DEBUG.P("a.getUpperBound()="+a.getUpperBound());
            
            JCClassDecl cd = make.at(tree.pos).ClassDef(
                make.Modifiers(PUBLIC | ABSTRACT),
                tree.name, List.<JCTypeParameter>nil(),
                extending, implementing, List.<JCTree>nil());

            ClassSymbol c = (ClassSymbol)a.getUpperBound().tsym;
            assert (c.flags() & COMPOUND) != 0;
            cd.sym = c;
            c.sourcefile = env.toplevel.sourcefile;

            // ... and attribute the bound class
            c.flags_field |= UNATTRIBUTED;
            Env<AttrContext> cenv = enter.classEnv(cd, env);
            enter.typeEnvs.put(c, cenv);
            
            DEBUG.P("cenv="+cenv);
            DEBUG.P("c="+c);
            /*
            DEBUG.P("");
	        DEBUG.P("Env总数: "+enter.typeEnvs.size());
	        DEBUG.P("--------------------------");
	        for(Map.Entry<TypeSymbol,Env<AttrContext>> myMapEntry:enter.typeEnvs.entrySet())
	        	DEBUG.P(""+myMapEntry);
	        DEBUG.P("");
	        */	
        }
        
        }finally{//我加上的
		DEBUG.P(0,this,"visitTypeParameter(1)");
		}
    }


    public void visitWildcard(JCWildcard tree) {
    	DEBUG.P(this,"visitWildcard(1)");
    	DEBUG.P("tree="+tree);
    	DEBUG.P("tree.kind="+tree.kind);
    	DEBUG.P("tree.inner="+tree.inner);
    	
        //- System.err.println("visitWildcard("+tree+");");//DEBUG
        Type type = (tree.kind.kind == BoundKind.UNBOUND)
            ? syms.objectType
            : attribType(tree.inner, env);
        result = check(tree, new WildcardType(chk.checkRefType(tree.pos(), type),
                                              tree.kind.kind,
                                              syms.boundClass),
                       TYP, pkind, pt);
                       
       DEBUG.P(0,this,"visitWildcard(1)");                
    }

    public void visitAnnotation(JCAnnotation tree) {
        log.error(tree.pos(), "annotation.not.valid.for.type", pt);
        result = tree.type = syms.errType;
    }

    public void visitErroneous(JCErroneous tree) {
        if (tree.errs != null)
            for (JCTree err : tree.errs)
                attribTree(err, env, ERR, pt);
        result = tree.type = syms.errType;
    }

    /** Default visitor method for all other trees.
     */
    public void visitTree(JCTree tree) {
        throw new AssertionError();
    }

    /** Main method: attribute class definition associated with given class symbol.
     *  reporting completion failures at the given position.
     *  @param pos The source position at which completion errors are to be
     *             reported.
     *  @param c   The class symbol whose definition will be attributed.
     */
    public void attribClass(DiagnosticPosition pos, ClassSymbol c) {
    	DEBUG.P(5);DEBUG.P(this,"attribClass(2)");
        try {
            annotate.flush();
            attribClass(c);
        } catch (CompletionFailure ex) {
            chk.completionError(pos, ex);
        }
        DEBUG.P(2,this,"attribClass(2)");
    }

    /** Attribute class definition associated with given class symbol.
     *  @param c   The class symbol whose definition will be attributed.
     */
    void attribClass(ClassSymbol c) throws CompletionFailure {
    	try {//我加上的
    	DEBUG.P(this,"attribClass(1)");
    	DEBUG.P("ClassSymbol c="+c);
    	DEBUG.P("c.type="+c.type);
    	DEBUG.P("c.type.tag="+TypeTags.toString(c.type.tag));
    	DEBUG.P("c.type.supertype="+((ClassType)c.type).supertype_field);
        
        //编译package-info.java时有错java.lang.NullPointerException
    	//DEBUG.P("c.type.supertype.tag="+TypeTags.toString((((ClassType)c.type).supertype_field).tag));
    	
    	
    	
        if (c.type.tag == ERROR) return;

        // Check for cycles in the inheritance graph, which can arise from
        // ill-formed class files.
        chk.checkNonCyclic(null, c.type);
        //检查类(或接口)是否自己继承(或实现)自己，是否彼此之间互相继承(或实现)
        //如Test4 extends Test4(自己继承自己)
        //如Test4 extends Test5且Test5 extends Test4(彼此之间互相继承)
        //如:public class Test4 extends Test4
        //报错:cyclic inheritance involving my.test.Test4


        Type st = types.supertype(c.type);
        DEBUG.P("c.flags_field="+Flags.toString(c.flags_field));
        DEBUG.P("c.supertype="+st);
        DEBUG.P("c.supertype.tag="+TypeTags.toString(st.tag));
        DEBUG.P("c.owner="+c.owner);
        DEBUG.P("c.owner.kind="+Kinds.toString(c.owner.kind));
        if(c.owner.type!=null) DEBUG.P("c.owner.type.tag="+TypeTags.toString(c.owner.type.tag));
        
        
        //c.flags_field不包含Flags.COMPOUND时执行
        if ((c.flags_field & Flags.COMPOUND) == 0) {
        	DEBUG.P("c.flags_field不包含Flags.COMPOUND");
            // First, attribute superclass.
            if (st.tag == CLASS)
                attribClass((ClassSymbol)st.tsym);

            // Next attribute owner, if it is a class.
            if (c.owner.kind == TYP && c.owner.type.tag == CLASS)
                attribClass((ClassSymbol)c.owner);
        }
        
        DEBUG.P("完成对："+c+" 的superclass及owner的attribute");
        DEBUG.P("c.flags_field="+Flags.toString(c.flags_field));
        // The previous operations might have attributed the current class
        // if there was a cycle. So we test first whether the class is still
        // UNATTRIBUTED.
        if ((c.flags_field & UNATTRIBUTED) != 0) {
        	//这条语句很有用，因为如果对c这个类进行attribClass后，
        	//在c.flags_field中就没有UNATTRIBUTED这个标志了，当其他
        	//类的超类是c时，在调用Check.checkNonCyclic方法检测循环时，
        	//就可以把ACYCLIC标志加进c.flags_field中。
            c.flags_field &= ~UNATTRIBUTED;

            // Get environment current at the point of class definition.
            Env<AttrContext> env = enter.typeEnvs.get(c);
            
            DEBUG.P("c.flags_field="+Flags.toString(c.flags_field));
            DEBUG.P("env="+env);

            // The info.lint field in the envs stored in enter.typeEnvs is deliberately uninitialized,
            // because the annotations were not available at the time the env was created. Therefore,
            // we look up the environment chain for the first enclosing environment for which the
            // lint value is set. Typically, this is the parent env, but might be further if there
            // are any envs created as a result of TypeParameter nodes.
            Env<AttrContext> lintEnv = env;
            while (lintEnv.info.lint == null)
                lintEnv = lintEnv.next;
                
            DEBUG.P("lintEnv="+lintEnv);
            // Having found the enclosing lint value, we can initialize the lint value for this class
            env.info.lint = lintEnv.info.lint.augment(c.attributes_field, c.flags());
            
            DEBUG.P("env.info.lint="+env.info.lint);

            Lint prevLint = chk.setLint(env.info.lint);
            JavaFileObject prev = log.useSource(c.sourcefile);

            try {
            	
            	DEBUG.P("");
            	DEBUG.P("st.tsym="+st.tsym);
            	if (st.tsym != null) 
            		DEBUG.P("st.tsym.flags_field="+Flags.toString(st.tsym.flags_field));
            	DEBUG.P("c.flags_field="+Flags.toString(c.flags_field));
            
                // java.lang.Enum may not be subclassed by a non-enum
                if (st.tsym == syms.enumSym &&
                    ((c.flags_field & (Flags.ENUM|Flags.COMPOUND)) == 0))
                    /*例子:
                    F:\javac\bin\mysrc\my\test\TestOhter.java:2: 类无法直接继承 java.lang.Enum
					public class TestOhter<TestOhterS,TestOhterT> extends Enum {
					       ^
					1 错误
					*/
                    log.error(env.tree.pos(), "enum.no.subclassing");

                // Enums may not be extended by source-level classes
                //注:如果((c.flags_field & Flags.ENUM) == 0)为true，那么
                //target.compilerBootstrap(c)总是为fasle的，也就是
                //!target.compilerBootstrap(c)总是为true，这条件是多余的判断
                if (st.tsym != null &&
                    ((st.tsym.flags_field & Flags.ENUM) != 0) &&
                    ((c.flags_field & Flags.ENUM) == 0) &&
                    !target.compilerBootstrap(c)) {
                    
                    /*例子:
                    源代码:
                    package my.test.myenum;
					public class EnumTest extends MyEnum {}
					enum MyEnum {}
					
					错误提示:
					bin\mysrc\my\test\myenum\EnumTest.java:3: 无法从最终 my.test.myenum.MyEnum 进行继承
					public class EnumTest extends MyEnum {}
					                              ^
					bin\mysrc\my\test\myenum\EnumTest.java:3: 枚举类型不可继承
					public class EnumTest extends MyEnum {}
					       ^
					2 错误
					*/
                    log.error(env.tree.pos(), "enum.types.not.extensible");
                }
                DEBUG.P(2);
                attribClassBody(env, c);

                chk.checkDeprecatedAnnotation(env.tree.pos(), c);
            } finally {
                log.useSource(prev);
                chk.setLint(prevLint);
                
            }
        }
        
        
        }finally{//我加上的
        DEBUG.P("结束对此类的属性分性: "+c);
        DEBUG.P(1,this,"attribClass(1)");
    	}
    }

    public void visitImport(JCImport tree) {
        // nothing to do
    }

    /** Finish the attribution of a class. */
    private void attribClassBody(Env<AttrContext> env, ClassSymbol c) {
    	DEBUG.P(this,"attribClassBody(2)");
    	DEBUG.P("ClassSymbol c="+c);
        DEBUG.P("env="+env);
    	
        JCClassDecl tree = (JCClassDecl)env.tree;
        assert c == tree.sym;

        // Validate annotations
        chk.validateAnnotations(tree.mods.annotations, c);

        // Validate type parameters, supertype and interfaces.
        attribBounds(tree.typarams);//对COMPOUND型的上限绑定进行attribClass
        /*
        主要是检查同一泛型类的参数化类型之间的差别
        如泛型类定义  :interface Test<T extends Number>
        参数化类型t :Test<Number>
        参数化类型s :Test<? super Float>
        
        当定义新的泛型类：Test2<S extends Test<Number>&Test<? super Float>>
        时，在validateTypeParams中能检查出“无法使用以下不同的参数继承”错误
        */
        chk.validateTypeParams(tree.typarams);
        chk.validate(tree.extending);
        chk.validate(tree.implementing);
        
        DEBUG.P(2);DEBUG.P("结束:Validate annotations, type parameters, supertype and interfaces : "+c);DEBUG.P(2);
        
        DEBUG.P("relax="+relax);
        DEBUG.P("c.flags()="+Flags.toString(c.flags()));
        // If this is a non-abstract class, check that it has no abstract
        // methods or unimplemented methods of an implemented interface.
        if ((c.flags() & (ABSTRACT | INTERFACE)) == 0) {
            if (!relax)
                chk.checkAllDefined(tree.pos(), c);
        }

        if ((c.flags() & ANNOTATION) != 0) {
            if (tree.implementing.nonEmpty())
                log.error(tree.implementing.head.pos(),
                          "cant.extend.intf.annotation");
            if (tree.typarams.nonEmpty())
                log.error(tree.typarams.head.pos(),
                          "intf.annotation.cant.have.type.params");
        } else {
            // Check that all extended classes and interfaces
            // are compatible (i.e. no two define methods with same arguments
            // yet different return types).  (JLS 8.4.6.3)
            chk.checkCompatibleSupertypes(tree.pos(), c.type);
        }

        // Check that class does not import the same parameterized interface
        // with two different argument lists.
        chk.checkClassBounds(tree.pos(), c.type);

        tree.type = c.type;

        boolean assertsEnabled = false;
        assert assertsEnabled = true;
        
        DEBUG.P("env.info.scope="+env.info.scope);
        if (assertsEnabled) {
            for (List<JCTypeParameter> l = tree.typarams;
                 l.nonEmpty(); l = l.tail)
                assert env.info.scope.lookup(l.head.name).scope != null;
        }
        
        DEBUG.P("c.type="+c.type);
        DEBUG.P("c.type.allparams()="+c.type.allparams());
        /*错误例子:
        bin\mysrc\my\test\Test.java:7: 泛型类无法继承 java.lang.Throwable
		public class Test<S,T extends ExtendsTest,E extends ExtendsTest & MyInterfaceA>
		extends Exception {
		
		        ^
		1 错误
		*/
        // Check that a generic class doesn't extend Throwable
        if (!c.type.allparams().isEmpty() && types.isSubtype(c.type, syms.throwableType))
            log.error(tree.extending.pos(), "generic.throwable");

        // Check that all methods which implement some
        // method conform to the method they implement.
        chk.checkImplementations(tree);

        for (List<JCTree> l = tree.defs; l.nonEmpty(); l = l.tail) {
            // Attribute declaration
            attribStat(l.head, env);
            // Check that declarations in inner classes are not static (JLS 8.1.2)
            // Make an exception for static constants.
            if (c.owner.kind != PCK &&
                ((c.flags() & STATIC) == 0 || c.name == names.empty) &&
                (TreeInfo.flags(l.head) & (STATIC | INTERFACE)) != 0) {
                Symbol sym = null;
                if (l.head.tag == JCTree.VARDEF) sym = ((JCVariableDecl) l.head).sym;
                if (sym == null ||
                    sym.kind != VAR ||
                    ((VarSymbol) sym).getConstValue() == null)
                    log.error(l.head.pos(), "icls.cant.have.static.decl");
            }
        }

        // Check for cycles among non-initial constructors.
        chk.checkCyclicConstructors(tree);

        // Check for cycles among annotation elements.
        chk.checkNonCyclicElements(tree);

        // Check for proper use of serialVersionUID
        if (env.info.lint.isEnabled(Lint.LintCategory.SERIAL) &&
            isSerializable(c) &&
            (c.flags() & Flags.ENUM) == 0 &&
            (c.flags() & ABSTRACT) == 0) {
            checkSerialVersionUID(tree, c);
        }
        
        DEBUG.P(0,this,"attribClassBody(2)");
    }
        // where
        /** check if a class is a subtype of Serializable, if that is available. */
        
        //注:任何java.lang.Throwable的子类都是可序列化的，因为
        //java.lang.Throwable实现了java.io.Serializable接中。
        private boolean isSerializable(ClassSymbol c) {
            try {
                syms.serializableType.complete();
            }
            catch (CompletionFailure e) {
                return false;
            }
            return types.isSubtype(c.type, syms.serializableType);
        }

        /** Check that an appropriate serialVersionUID member is defined. */
        private void checkSerialVersionUID(JCClassDecl tree, ClassSymbol c) {
			//如果一个类直接或间接实现了java.io.Serializable接中，在这个类中需要
			//定义一个“static final long serialVersionUID”字段，且这个字段还必需
			//显示赋值，否则，只要其中一点不符合，编译器就给出警告。

            // check for presence of serialVersionUID
            Scope.Entry e = c.members().lookup(names.serialVersionUID);
            while (e.scope != null && e.sym.kind != VAR) e = e.next();
            if (e.scope == null) {
                log.warning(tree.pos(), "missing.SVUID", c);
                return;
            }

            // check that it is static final
            VarSymbol svuid = (VarSymbol)e.sym;
            if ((svuid.flags() & (STATIC | FINAL)) !=
                (STATIC | FINAL))
                log.warning(TreeInfo.diagnosticPositionFor(svuid, tree), "improper.SVUID", c);

            // check that it is long
            else if (svuid.type.tag != TypeTags.LONG)
                log.warning(TreeInfo.diagnosticPositionFor(svuid, tree), "long.SVUID", c);

            // check constant
            else if (svuid.getConstValue() == null)
                log.warning(TreeInfo.diagnosticPositionFor(svuid, tree), "constant.SVUID", c);
        }

    private Type capture(Type type) {
		DEBUG.P(this,"capture(1)");

        //return types.capture(type);

		Type typeCapture = types.capture(type);
		
    	DEBUG.P("type       ="+type);
		DEBUG.P("typeCapture="+typeCapture);
		DEBUG.P(0,this,"capture(1)");

		return typeCapture;
    }
}
