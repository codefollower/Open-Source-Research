/*
 * @(#)Symbol.java	1.99 07/03/21
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Callable;
import javax.lang.model.element.*;
import javax.lang.model.type.ReferenceType;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;

import com.sun.tools.javac.util.*;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.code.Type.*;
import com.sun.tools.javac.comp.Attr;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.jvm.*;
import com.sun.tools.javac.model.*;
import com.sun.tools.javac.tree.JCTree;

import static com.sun.tools.javac.code.Flags.*;
import static com.sun.tools.javac.code.Kinds.*;
import static com.sun.tools.javac.code.TypeTags.*;

/** Root class for Java symbols. It contains subclasses
 *  for specific sorts of symbols, such as variables, methods and operators,
 *  types, packages. Each subclass is represented as a static inner class
 *  inside Symbol.
 *
 *  <p><b>This is NOT part of any API supported by Sun Microsystems.  If
 *  you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 */
@Version("@(#)Symbol.java	1.99 07/03/21")
public abstract class Symbol implements Element {
	private static my.Debug DEBUG=new my.Debug(my.Debug.Symbol);//我加上的
	
    // public Throwable debug = new Throwable();

    /** The kind of this symbol.
     *  @see Kinds
     */
    public int kind;

    /** The flags of this symbol.
     */
    public long flags_field;

    /** An accessor method for the flags of this symbol.
     *  Flags of class symbols should be accessed through the accessor
     *  method to make sure that the class symbol is loaded.
     */
    public long flags() { return flags_field; }
    
    //我加上的，调试用途
    public String myFlags() {
    	return Flags.toString(flags_field);
    }
    //我加上的，调试用途
    public String myKind() {
    	return Kinds.toString(kind);
    }

    /** The attributes of this symbol.
     */
    public List<Attribute.Compound> attributes_field;

    /** An accessor method for the attributes of this symbol.
     *  Attributes of class symbols should be accessed through the accessor
     *  method to make sure that the class symbol is loaded.
     */
    public List<Attribute.Compound> getAnnotationMirrors() {
    	try {//我加上的
		DEBUG.P(this,"getAnnotationMirrors()");
		
        assert attributes_field != null;
        return attributes_field;
        
        }finally{//我加上的
        DEBUG.P("attributes_field="+attributes_field);
		DEBUG.P(0,this,"getAnnotationMirrors()");
		}
    }

    /** Fetch a particular annotation from a symbol. */
    public Attribute.Compound attribute(Symbol anno) {
    	try {//我加上的
		DEBUG.P(this,"attribute(Symbol anno)");
		DEBUG.P("this="+toString());
		DEBUG.P("anno="+anno);
		
        for (Attribute.Compound a : getAnnotationMirrors())
            if (a.type.tsym == anno) return a;
        return null;
        
        }finally{//我加上的
		DEBUG.P(0,this,"attribute(Symbol anno)");
		}
    }

    /** The name of this symbol in Utf8 representation.
     */
    public Name name;

    /** The type of this symbol.
     */
    public Type type;

    /** The owner of this symbol.
     */
    public Symbol owner;

    /** The completer of this symbol.
     */
    public Completer completer;

    /** A cache for the type erasure of this symbol.
     */
    public Type erasure_field;

    /** Construct a symbol with given kind, flags, name, type and owner.
     */
    public Symbol(int kind, long flags, Name name, Type type, Symbol owner) {
        this.kind = kind;
        this.flags_field = flags;
        this.type = type;
        this.owner = owner;
        this.completer = null;
        this.erasure_field = null;
        this.attributes_field = List.nil();
        this.name = name;
    }

    /** Clone this symbol with new owner.
     *  Legal only for fields and methods.
     */
    //子类VarSymbol，MethodSymbol覆盖了此方法
    public Symbol clone(Symbol newOwner) {
        throw new AssertionError();
    }

    /** The Java source which this symbol represents.
     *  A description of this symbol; overrides Object.
     */
    public String toString() {
        return name.toString();
    }

    /** A Java source description of the location of this symbol; used for
     *  error reporting.  Use of this method may result in the loss of the
     *  symbol's description.
     */
    public String location() {
    if (owner.name == null || (owner.name.len == 0 && owner.kind != PCK)) {
	    return "";
	}
	return owner.toString();
    }

    public String location(Type site, Types types) {
        if (owner.name == null || owner.name.len == 0) {
            return location();
        }
        if (owner.type.tag == CLASS) {
            Type ownertype = types.asOuterSuper(site, owner);
            if (ownertype != null) return ownertype.toString();
        }
        return owner.toString();
    }

    /** The symbol's erased type.
     */
    public Type erasure(Types types) {
    //try {//我加上的
	//DEBUG.P(this,"erasure(Types types)");
	//DEBUG.P("erasure_field="+erasure_field);

        if (erasure_field == null)
            erasure_field = types.erasure(type);
        return erasure_field;
        
    //}finally{//我加上的
	//DEBUG.P(0,this,"erasure(Types types)");
	//}
    }

    /** The external type of a symbol. This is the symbol's erased type
     *  except for constructors of inner classes which get the enclosing
     *  instance class added as first argument.
     */
    
    /*例子:
    public class Test{
		class MyInnerClass{
			MyInnerClass(){ this("str",123); }
			MyInnerClass(String str,int i){}
		}
	}
	输出:
	com.sun.tools.javac.code.Symbol$MethodSymbol===>externalType(Types types)
	-------------------------------------------------------------------------
	type=Method(java.lang.String,int)void
	t=Method(java.lang.String,int)void
	name=<init>
	owner.hasOuterInstance()=true
	com.sun.tools.javac.code.Types===>erasure(Type t)
	-------------------------------------------------------------------------
	t=my.test.Test  t.tag=(CLASS)10  lastBaseTag=8
	com.sun.tools.javac.code.Types$16===>visitClassType(2)
	-------------------------------------------------------------------------
	com.sun.tools.javac.code.Symbol$ClassSymbol===>erasure(Types types)
	-------------------------------------------------------------------------
	erasure_field=my.test.Test
	com.sun.tools.javac.code.Symbol$ClassSymbol===>erasure(Types types)  END
	-------------------------------------------------------------------------
	
	com.sun.tools.javac.code.Types$16===>visitClassType(2)  END
	-------------------------------------------------------------------------
	
	t=my.test.Test  erasureType=my.test.Test
	com.sun.tools.javac.code.Types===>erasure(Type t)  END
	-------------------------------------------------------------------------
	
	outerThisType=my.test.Test
	com.sun.tools.javac.code.Symbol$MethodSymbol===>externalType(Types types)  END
	-------------------------------------------------------------------------
    */
    
    public Type externalType(Types types) {
		try {//我加上的
		DEBUG.P(this,"externalType(Types types)");
		DEBUG.P("type="+type);
		
        Type t = erasure(types);
        
        DEBUG.P("t="+t);
        DEBUG.P("name="+name);
        if(name == name.table.init) DEBUG.P("owner.hasOuterInstance()="+owner.hasOuterInstance());
        
        if (name == name.table.init && owner.hasOuterInstance()) {
            Type outerThisType = types.erasure(owner.type.getEnclosingType());
            
            DEBUG.P("outerThisType="+outerThisType);
            
            //getParameterTypes()指的是方法括号里各个普通参数的类型
            //而不是泛型变量
            return new MethodType(t.getParameterTypes().prepend(outerThisType),
                                  t.getReturnType(),
                                  t.getThrownTypes(),
                                  t.tsym);
        } else {
            return t;
        }
        
		}finally{//我加上的
		DEBUG.P(0,this,"externalType(Types types)");
		}
    }

    public boolean isStatic() {
        return
            (flags() & STATIC) != 0 ||
            (owner.flags() & INTERFACE) != 0 && kind != MTH;
            //第二个条件:owner是接口且当前symbol的kind不是方法时返回true
            //也就是接口中声明的变量默认是STATIC的，但接口中声明的方法却不是
    }

    public boolean isInterface() {
        return (flags() & INTERFACE) != 0;
    }

    /** Is this symbol declared (directly or indirectly) local
     *  to a method or variable initializer?
     *  Also includes fields of inner classes which are in
     *  turn local to a method or variable initializer.
     */
    public boolean isLocal() {
    	//1.若当前symbol的owner.kind是VAR或MTH,则返回true
    	//2.若当前symbol的owner.kind是TYP且owner.isLocal()为true,则返回true
        return
            (owner.kind & (VAR | MTH)) != 0 ||
            (owner.kind == TYP && owner.isLocal());
    }

    /** Is this symbol a constructor?
     */
    public boolean isConstructor() {
        return name == name.table.init;
    }

    /** The fully qualified name of this symbol.
     *  This is the same as the symbol's name except for class symbols,
     *  which are handled separately.
     */
    public Name getQualifiedName() {
        return name;
    }

    /** The fully qualified name of this symbol after converting to flat
     *  representation. This is the same as the symbol's name except for
     *  class symbols, which are handled separately.
     */
    public Name flatName() {
        return getQualifiedName();
    }

    /** If this is a class or package, its members, otherwise null.
     */
    public Scope members() {
        return null;
    }

    /** A class is an inner class if it it has an enclosing instance class.
     */
    public boolean isInner() {
        return type.getEnclosingType().tag == CLASS;
    }

    /** An inner class has an outer instance if it is not an interface
     *  it has an enclosing instance class which might be referenced from the class.
     *  Nested classes can see instance members of their enclosing class.
     *  Their constructors carry an additional this$n parameter, inserted
     *  implicitly by the compiler.
     *
     *  @see #isInner
     */
    public boolean hasOuterInstance() {
        return
            type.getEnclosingType().tag == CLASS && (flags() & (INTERFACE | NOOUTERTHIS)) == 0;
    }
    
    //对于类my.test.ClassA.ClassB.ClassC
    //enclClass()＝my.test.ClassA.ClassB.ClassC
    //outermostClass()＝my.test.ClassA
    //packge()=my.test
    
    /** The closest enclosing class of this symbol's declaration.
     */
    public ClassSymbol enclClass() {
    	//如果当前symbol本身就是一个ClassSymbol,则它的enclosing class就是它本身
    	//如果当前symbol本身是一个PackageSymbol,则它的enclosing class为null
    	//其他情况，enclosing class是源代码中第一层包围它的那个ClassSymbol
        Symbol c = this;
        while (c != null &&
               ((c.kind & TYP) == 0 || c.type.tag != CLASS)) {
            c = c.owner;
        }
        
        //DEBUG.P("enclClass() this="+this+" return="+c);
        //if(c!=null && c.kind==TYP) c.owner.enclClass();
        
        return (ClassSymbol)c;
    }

    /** The outermost class which indirectly owns this symbol.
     */
    public ClassSymbol outermostClass() {
        Symbol sym = this;
        Symbol prev = null;
        while (sym.kind != PCK) {
            prev = sym;
            sym = sym.owner;
        }
        return (ClassSymbol) prev;
    }
    
    //按symbol.owner的层次来说packge()比outermostClass()高一个层次

    /** The package which indirectly owns this symbol.
     */
    public PackageSymbol packge() {
        Symbol sym = this;
        while (sym.kind != PCK) {
            sym = sym.owner;
        }
        return (PackageSymbol) sym;
    }

    /** Is this symbol a subclass of `base'? Only defined for ClassSymbols.
     */
    public boolean isSubClass(Symbol base, Types types) {
        throw new AssertionError("isSubClass " + this);
    }

    /** Fully check membership: hierarchy, protection, and hiding.
     *  Does not exclude methods not inherited due to overriding.
     */
    public boolean isMemberOf(TypeSymbol clazz, Types types) {
    	try {//我加上的
		DEBUG.P(this,"isMemberOf(2)");
		DEBUG.P("this.name="+this.name);
		DEBUG.P("owner.name="+owner.name);
		DEBUG.P("clazz.name="+clazz.name);
		DEBUG.P("(owner == clazz)="+(owner == clazz));

    	//当owner == clazz时，说明当前symbol是clazz的成员，直接返回true
    	//当clazz.isSubClass(owner, types)返回true时，可知clazz是owner
    	//的子类,但必须再用isInheritedIn(clazz, types)来判断当
    	//前symbol(owner的成员,如字段,方法等)是否能被子类clazz继承下来。
        /*return
            owner == clazz ||
            clazz.isSubClass(owner, types) &&
            isInheritedIn(clazz, types) &&
            !hiddenIn((ClassSymbol)clazz, types);*/

		boolean isMemberOf=
			owner == clazz ||
            clazz.isSubClass(owner, types) &&
            isInheritedIn(clazz, types) &&
            !hiddenIn((ClassSymbol)clazz, types);
        
		DEBUG.P("");
		DEBUG.P("isMemberOf="+isMemberOf);	
		return isMemberOf;
        }finally{//我加上的
		DEBUG.P(0,this,"isMemberOf(2)");
		}
    }

    /** Is this symbol the same as or enclosed by the given class? */
    public boolean isEnclosedBy(ClassSymbol clazz) {
    	//如果clazz与当前smybol相同，或与当前smybol的(直接的或间接的)owner相同，则返回true
		/*
		for (Symbol sym = this; sym.kind != PCK; sym = sym.owner)
            if (sym == clazz) return true;
        return false;
		*/
		
		//我加上的
		DEBUG.P(this,"isEnclosedBy(ClassSymbol clazz)");
		DEBUG.P("clazz="+clazz);
        boolean result=false;
		for (Symbol sym = this; sym.kind != PCK; sym = sym.owner) {
			DEBUG.P("sym="+sym);
            if (sym == clazz) {
				result=true;
				break;
			}
		}
		DEBUG.P("result="+result);
		DEBUG.P(0,this,"isEnclosedBy(ClassSymbol clazz)");
		return result;
    }

    /** Check for hiding.  Note that this doesn't handle multiple
     *  (interface) inheritance. */

	//如果子类跟超类有相同的字段、内部类、相同参数的STATIC方法，
	//那么超类对应的字段、内部类、相同参数的STATIC方法在子类中不可见，
	//也就是说在子类中被隐藏掉了(注意:超类中的非STATIC方法不能被隐藏)
    private boolean hiddenIn(ClassSymbol clazz, Types types) {
		boolean hiddenIn=false;
		try {//我加上的
		DEBUG.P(this,"hiddenIn(2)");
		DEBUG.P("this.name ="+this.name);
		DEBUG.P("owner.name="+owner.name);
		DEBUG.P("clazz.name="+clazz.name);
		DEBUG.P("this.kind="+Kinds.toString(kind));
		DEBUG.P("this.flags_field="+Flags.toString(flags_field));
		
    	//超类的非STATIC方法不能被子类hidden，直接返回false
        if (kind == MTH && (flags() & STATIC) == 0) return false;
        
        while (true) {
            if (owner == clazz) return false;
            Scope.Entry e = clazz.members().lookup(name);
            while (e.scope != null) {
                if (e.sym == this) return false;
                
                //子类与超类的成员如果有相同kind与name的成员，
                //那么子类不会继承超类同kind与name的成员

				/*当编译“import static my.test.ClassF.*;”时

				package my.test;
				public class ClassD {
					public static class Class1{} //hiddenIn=true 因为ClassE中有同样的Class1
					public static class Class2{} //hiddenIn=false
				}

				package my.test;
				public class ClassE extends ClassD {
					public static class Class1{} //hiddenIn=false
				}

				package my.test;
				public class ClassF extends ClassE {}
				*/
                if (e.sym.kind == kind &&
                    (kind != MTH || //STATIC方法
                     (e.sym.flags() & STATIC) != 0 &&
                     types.isSubSignature(e.sym.type, type))) {
					hiddenIn=true;
                    return true;
					}
                e = e.next();
            }
            Type superType = types.supertype(clazz.type);
            if (superType.tag != TypeTags.CLASS) return false;
            clazz = (ClassSymbol)superType.tsym;
        }

		}finally{//我加上的
		DEBUG.P("");
		DEBUG.P("this.name ="+this.name);
		DEBUG.P("owner.name="+owner.name);
		DEBUG.P("clazz.name="+clazz.name);
		DEBUG.P("hiddenIn="+hiddenIn);	
		DEBUG.P(0,this,"hiddenIn(2)");
		}
    }

    /** Is this symbol inherited into a given class?
     *  PRE: If symbol's owner is a interface,
     *       it is already assumed that the interface is a superinterface
     *       of given class.
     *  @param clazz  The class for which we want to establish membership.
     *                This must be a subclass of the member's owner.
     */
    //参考上面的isMemberOf，在此以假定clazz是symbol's owner的子类
    //此方法的功能是判断当前symbol能否被clazz继承
    public boolean isInheritedIn(Symbol clazz, Types types) {
		boolean isInheritedIn=true;//我加上的
    	try {//我加上的
		DEBUG.P(this,"isInheritedIn(2)");
		//DEBUG.P("this.name="+this.name+" clazz="+clazz);
		//DEBUG.P("flags_field="+Flags.toString(flags_field));
		//DEBUG.P("flags_field & AccessFlags="+Flags.toString(flags_field & AccessFlags));

        switch ((int)(flags_field & Flags.AccessFlags)) {
        default: // error recovery
        case PUBLIC:
            return true;
        case PRIVATE:
            return isInheritedIn=this.owner == clazz;
        case PROTECTED:
            // we model interfaces as extending Object
            return isInheritedIn=(clazz.flags() & INTERFACE) == 0;
            //受保护的成员，只有非INTERFACE的Symbol子类才能继承
            //注意:这里只是按程序逻辑来理解，实际并不存在一个类的子类是一个接口的情况
            
        case 0:
        //访问标志缺省的成员，只有同包的非INTERFACE的Symbol子类才能继承
            PackageSymbol thisPackage = this.packge();
            //DEBUG.P("");DEBUG.P("case 0");
            //DEBUG.P("thisPackage="+thisPackage);
			for (Symbol sup = clazz;
                 sup != null && sup != this.owner;
                 sup = types.supertype(sup.type).tsym) {
                //DEBUG.P("sup != null && sup != this.owner="+(sup != null && sup != this.owner));
            	//DEBUG.P("sup.type="+sup.type);
            	//DEBUG.P("sup.type.isErroneous()="+sup.type.isErroneous());
                if (sup.type.isErroneous())
                    return true; // error recovery
                if ((sup.flags() & COMPOUND) != 0)
                    continue;
                //DEBUG.P("(sup.packge() != thisPackage)="+(sup.packge() != thisPackage));
				/*
				//clazz所在的直到this.owner为根的继承树(含clazz)上的所有类所在的包必须都是thisPackage
				//只要有一个不是thisPackage都返回false

				例子:
				clazz代表ClassC，this代表Class1，
				通过"import static my.test.ClassC.*;"语句转到此方法

				package my.test;
				public class ClassA {
					static class Class1{}
				}

				package my;
				public class ClassB extends my.test.ClassA {}

				package my.test;
				public class ClassC extends my.ClassB {}
				*/
                if (sup.packge() != thisPackage)
                    return isInheritedIn=false;
            }
            return isInheritedIn=(clazz.flags() & INTERFACE) == 0;
        }
        
        }finally{//我加上的
		DEBUG.P("this.name="+this.name+"(" +Flags.toString(flags_field)+") this.owner="+this.owner);
		DEBUG.P("clazz="+clazz);
		DEBUG.P("isInheritedIn="+isInheritedIn);
		DEBUG.P(0,this,"isInheritedIn(2)");
		}
    }

    /** The (variable or method) symbol seen as a member of given
     *  class type`site' (this might change the symbol's type).
     *  This is used exclusively for producing diagnostics.
     */
    public Symbol asMemberOf(Type site, Types types) {
        throw new AssertionError();
    }

    /** Does this method symbol override `other' symbol, when both are seen as
     *  members of class `origin'?  It is assumed that _other is a member
     *  of origin.
     *
     *  It is assumed that both symbols have the same name.  The static
     *  modifier is ignored for this test.
     *
     *  See JLS 8.4.6.1 (without transitivity) and 8.4.6.4
     */
    public boolean overrides(Symbol _other, TypeSymbol origin, Types types, boolean checkResult) {
        return false;
    }

    /** Complete the elaboration of this symbol's definition.
     */
    public void complete() throws CompletionFailure {
    	DEBUG.P(this,"complete()");
    	DEBUG.P("name="+name+"   completer="+completer);
        if (completer != null) {
            Completer c = completer;
            completer = null;
            //DEBUG.P("c.getClass().getName()="+c.getClass().getName(),true);
            //输出如:com.sun.tools.javac.jvm.ClassReader
            //另外也请注意com.sun.tools.javac.comp.MemberEnter
            c.complete(this);
        }
        DEBUG.P(0,this,"complete()");
    }

    /** True if the symbol represents an entity that exists.
     */
    //只有子类PackageSymbol覆盖了此方法，其他子类没有覆盖。
    //在com.sun.tools.javac.comp.Resolve类中对此方法有大量运用，一般都返回true
    public boolean exists() {
        return true;
    }

    public Type asType() {
        return type;
    }

    public Symbol getEnclosingElement() {
        return owner;
    }

    public ElementKind getKind() {
        return ElementKind.OTHER;       // most unkind
    }

    public Set<Modifier> getModifiers() {
        return Flags.asModifierSet(flags());
    }

    public Name getSimpleName() {
        return name;
    }

    /**
     * @deprecated this method should never be used by javac internally.
     */
    @Deprecated
    public <A extends java.lang.annotation.Annotation> A getAnnotation(Class<A> annoType) {
        return JavacElements.getAnnotation(this, annoType);
    }

    // TODO: getEnclosedElements should return a javac List, fix in FilteredMemberList
    public java.util.List<Symbol> getEnclosedElements() {
        return List.nil();
    }

    public List<TypeSymbol> getTypeParameters() {
        ListBuffer<TypeSymbol> l = ListBuffer.lb();
        for (Type t : type.getTypeArguments()) {
            l.append(t.tsym);
        }
        return l.toList();
    }

    public static class DelegatedSymbol extends Symbol {
        protected Symbol other;
        public DelegatedSymbol(Symbol other) {
            super(other.kind, other.flags_field, other.name, other.type, other.owner);
            this.other = other;
        }
        public String toString() { return other.toString(); }
        public String location() { return other.location(); }
        public String location(Type site, Types types) { return other.location(site, types); }
        public Type erasure(Types types) { return other.erasure(types); }
        public Type externalType(Types types) { return other.externalType(types); }
        public boolean isLocal() { return other.isLocal(); }
        public boolean isConstructor() { return other.isConstructor(); }
        public Name getQualifiedName() { return other.getQualifiedName(); }
        public Name flatName() { return other.flatName(); }
        public Scope members() { return other.members(); }
        public boolean isInner() { return other.isInner(); }
        public boolean hasOuterInstance() { return other.hasOuterInstance(); }
        public ClassSymbol enclClass() { return other.enclClass(); }
        public ClassSymbol outermostClass() { return other.outermostClass(); }
        public PackageSymbol packge() { return other.packge(); }
        public boolean isSubClass(Symbol base, Types types) { return other.isSubClass(base, types); }
        public boolean isMemberOf(TypeSymbol clazz, Types types) { return other.isMemberOf(clazz, types); }
        public boolean isEnclosedBy(ClassSymbol clazz) { return other.isEnclosedBy(clazz); }
        public boolean isInheritedIn(Symbol clazz, Types types) { return other.isInheritedIn(clazz, types); }
        public Symbol asMemberOf(Type site, Types types) { return other.asMemberOf(site, types); }
        public void complete() throws CompletionFailure { other.complete(); }

        public <R, P> R accept(ElementVisitor<R, P> v, P p) {
            return other.accept(v, p);
        }
    }

    /** A class for type symbols. Type variables are represented by instances
     *  of this class, classes and packages by instances of subclasses.
     */
    public static class TypeSymbol
            extends Symbol implements TypeParameterElement {
        // Implements TypeParameterElement because type parameters don't
        // have their own TypeSymbol subclass.
        // TODO: type parameters should have their own TypeSymbol subclass

        public TypeSymbol(long flags, Name name, Type type, Symbol owner) {
            super(TYP, flags, name, type, owner);
        }

        /** form a fully qualified name from a name and an owner
         */
        static public Name formFullName(Name name, Symbol owner) {
            if (owner == null) return name;
            if (((owner.kind != ERR)) &&
                ((owner.kind & (VAR | MTH)) != 0
                 || (owner.kind == TYP && owner.type.tag == TYPEVAR)
                 )) return name;
            Name prefix = owner.getQualifiedName();
            if (prefix == null || prefix == prefix.table.empty)
                return name;
            else return prefix.append('.', name);
        }

        /** form a fully qualified name from a name and an owner, after
         *  converting to flat representation
         */
        static public Name formFlatName(Name name, Symbol owner) {
            if (owner == null ||
                (owner.kind & (VAR | MTH)) != 0
                || (owner.kind == TYP && owner.type.tag == TYPEVAR)
                ) return name;
            char sep = owner.kind == TYP ? '$' : '.';
            Name prefix = owner.flatName();
            if (prefix == null || prefix == prefix.table.empty)
                return name;
            else return prefix.append(sep, name);
        }

        /**
         * A total ordering between type symbols that refines the
         * class inheritance graph.
         *
         * Typevariables always precede other kinds of symbols.
         */
        public final boolean precedes(TypeSymbol that, Types types) {
			try {//我加上的
			DEBUG.P(this,"precedes(2)");
			DEBUG.P("that="+that);
			DEBUG.P("(this == that)="+(this == that));

            if (this == that)
                return false;

			DEBUG.P("this.type="+this.type+"  this.type.tag="+TypeTags.toString(this.type.tag));
			DEBUG.P("that.type="+that.type+"  that.type.tag="+TypeTags.toString(that.type.tag));

            if (this.type.tag == that.type.tag) {
                if (this.type.tag == CLASS) {
                    return
                        types.rank(that.type) < types.rank(this.type) ||
                        types.rank(that.type) == types.rank(this.type) &&
                        that.getQualifiedName().compareTo(this.getQualifiedName()) < 0;
                } else if (this.type.tag == TYPEVAR) {
                    return types.isSubtype(this.type, that.type);
                }
            }
            return this.type.tag == TYPEVAR;

			}finally{//我加上的
			DEBUG.P(0,this,"precedes(2)");
			}
        }

        // For type params; overridden in subclasses.
        public ElementKind getKind() {
            return ElementKind.TYPE_PARAMETER;
        }

        public java.util.List<Symbol> getEnclosedElements() {
            List<Symbol> list = List.nil();
            for (Scope.Entry e = members().elems; e != null; e = e.sibling) {
                if (e.sym != null && (e.sym.flags() & SYNTHETIC) == 0 && e.sym.owner == this)
                    list = list.prepend(e.sym);
            }
            return list;
        }

        // For type params.
        // Perhaps not needed if getEnclosingElement can be spec'ed
        // to do the same thing.
        // TODO: getGenericElement() might not be needed
        public Symbol getGenericElement() {
            return owner;
        }

        public <R, P> R accept(ElementVisitor<R, P> v, P p) {
            assert type.tag == TYPEVAR; // else override will be invoked
            return v.visitTypeParameter(this, p);
        }

        public List<Type> getBounds() {
            TypeVar t = (TypeVar)type;
            Type bound = t.getUpperBound();
            if (!bound.isCompound())
                return List.of(bound);
            ClassType ct = (ClassType)bound;
            if (!ct.tsym.erasure_field.isInterface()) {
                return ct.interfaces_field.prepend(ct.supertype_field);
            } else {
                // No superclass was given in bounds.
                // In this case, supertype is Object, erasure is first interface.
                return ct.interfaces_field;
            }
        }
    }

    /** A class for package symbols
     */
    public static class PackageSymbol extends TypeSymbol
        implements PackageElement {

        public Scope members_field;
        public Name fullname;
        
        //对应package-info.java的情况
        public ClassSymbol package_info; // see bug 6443073

        public PackageSymbol(Name name, Type type, Symbol owner) {
        	//这里的0代表flags_field,因为是一个PackageSymbol,包是没有修饰符(modifier)的,
        	//所以用0表示(注:在类Flags中没有定义值为0的flag)
        	//DEBUG.P("flag=0 modifier=("+Flags.toString(0)+")");
        	
            super(0, name, type, owner);
            //当调用TypeSymbol的构造方法时,kind默认取值为TYP,所以得在这里修正为PCK
            this.kind = PCK;
            this.members_field = null;
            this.fullname = formFullName(name, owner);//在TypeSymbol中定义
        }

        public PackageSymbol(Name name, Symbol owner) {
            this(name, null, owner);
            this.type = new PackageType(this);
        }

        public String toString() {
            return fullname.toString();
        }

        public Name getQualifiedName() {
            return fullname;
        }

		public boolean isUnnamed() {
		    return name.isEmpty() && owner != null;
		}

        public Scope members() {
            if (completer != null) complete();
            return members_field;
        }

        public long flags() {
            if (completer != null) complete();
            return flags_field;
        }

        public List<Attribute.Compound> getAnnotationMirrors() {
            if (completer != null) complete();
            assert attributes_field != null;
            return attributes_field;
        }

        /** A package "exists" if a type or package that exists has
         *  been seen within it.
         */
        public boolean exists() {
            return (flags_field & EXISTS) != 0;
        }

        public ElementKind getKind() {
            return ElementKind.PACKAGE;
        }

        public Symbol getEnclosingElement() {
            return null;
        }

        public <R, P> R accept(ElementVisitor<R, P> v, P p) {
            return v.visitPackage(this, p);
        }
    }

    /** A class for class symbols
     */
    public static class ClassSymbol extends TypeSymbol implements TypeElement {

        /** a scope for all class members; variables, methods and inner classes
         *  type parameters are not part of this scope
         */
        public Scope members_field;

        /** the fully qualified name of the class, i.e. pck.outer.inner.
         *  null for anonymous classes
         */
        public Name fullname;

        /** the fully qualified name of the class after converting to flat
         *  representation, i.e. pck.outer$inner,
         *  set externally for local and anonymous classes
         */
        public Name flatname;

        /** the sourcefile where the class came from
         */
        public JavaFileObject sourcefile;

        /** the classfile from where to load this class
         *  this will have extension .class or .java
         */
        public JavaFileObject classfile;

        /** the constant pool of the class
         */
        public Pool pool;
        
        //ClassSymbol的kind是TYP
        public ClassSymbol(long flags, Name name, Type type, Symbol owner) {
            super(flags, name, type, owner);
            this.members_field = null;
            this.fullname = formFullName(name, owner);
            this.flatname = formFlatName(name, owner);
            this.sourcefile = null;
            this.classfile = null;
            this.pool = null;
        }

        public ClassSymbol(long flags, Name name, Symbol owner) {
            this(
                flags,
                name,
                new ClassType(Type.noType, null, null),
                owner);
            this.type.tsym = this;
        }

        /** The Java source which this symbol represents.
         */
        public String toString() {
            return className();
        }

        public long flags() {
            if (completer != null) complete();
            return flags_field;
        }

        public Scope members() {
            if (completer != null) complete();
            return members_field;
        }

        public List<Attribute.Compound> getAnnotationMirrors() {
        	try {//我加上的
			DEBUG.P(this,"getAnnotationMirrors()");
		
            if (completer != null) complete();
            assert attributes_field != null;
            return attributes_field;
            
            }finally{//我加上的
	        DEBUG.P("attributes_field="+attributes_field);
			DEBUG.P(0,this,"getAnnotationMirrors()");
			}
        }

        public Type erasure(Types types) {
        	try {//我加上的
			DEBUG.P(this,"erasure(Types types)");
			DEBUG.P("erasure_field="+erasure_field);
			
            if (erasure_field == null)
                erasure_field = new ClassType(types.erasure(type.getEnclosingType()),
                                              List.<Type>nil(), this);
            return erasure_field;
            
            }finally{//我加上的
            //DEBUG.P("erasure_field="+erasure_field);
			DEBUG.P(0,this,"erasure(Types types)");
			}
        }

        public String className() {
            if (name.len == 0)
                return
                    Log.getLocalizedString("anonymous.class", flatname);
            else
                return fullname.toString();
        }

        public Name getQualifiedName() {
            return fullname;
        }

        public Name flatName() {
            return flatname;
        }
        
        //判断当前ClassSymbol是否是Symbol base的子类
        public boolean isSubClass(Symbol base, Types types) {
			/*
			if (this == base) {
                return true;
            } else if ((base.flags() & INTERFACE) != 0) {
                for (Type t = type; t.tag == CLASS; t = types.supertype(t))
                    for (List<Type> is = types.interfaces(t);
                         is.nonEmpty();
                         is = is.tail)
                        if (is.head.tsym.isSubClass(base, types)) return true;
            } else {
                for (Type t = type; t.tag == CLASS; t = types.supertype(t))
                    if (t.tsym == base) return true;
            }
            return false;
			*/

        	//当this == base时表示指向同一个ClassSymbol，返回true
        	//否则，当base是接口时，查看当前ClassSymbol实现的所有接口是否是base的子接口
        	//否则，当base不是接口时，查看当前ClassSymbol的所有超类是否等于base
        	//否则，反回false
			boolean isSubClass=false;
			try {//我加上的
			DEBUG.P(this,"isSubClass(2)");
			DEBUG.P("this="+toString());
			DEBUG.P("this.flags_field="+Flags.toString(this.flags_field));
			DEBUG.P("base="+base);
			DEBUG.P("base.flags_field="+Flags.toString(base.flags_field));

            if (this == base) {
				isSubClass=true;
                return true;
            } else if ((base.flags() & INTERFACE) != 0) {
                for (Type t = type; t.tag == CLASS; t = types.supertype(t))
                    for (List<Type> is = types.interfaces(t);
                         is.nonEmpty();
                         is = is.tail)
                        if (is.head.tsym.isSubClass(base, types)) {
							 isSubClass=true;
							 return true;
						 }
            } else {
                for (Type t = type; t.tag == CLASS; t = types.supertype(t))
                    //为什么这里不像上面那样用isSubClass(base, types)判断呢?
                    //因为上面base是接口，这里base是超类
                    if (t.tsym == base) {
						isSubClass=true;
						return true;
					}
            }
            return false;

			}finally{//我加上的
			DEBUG.P("this="+toString());
			DEBUG.P("base="+base);
			DEBUG.P("isSubClass="+isSubClass);
			DEBUG.P(0,this,"isSubClass(2)");
			}
        }

        /** Complete the elaboration of this symbol's definition.
         */
        public void complete() throws CompletionFailure {
            try {
                super.complete();
            } catch (CompletionFailure ex) {
                // quiet error recovery
                flags_field |= (PUBLIC|STATIC);
                this.type = new ErrorType(this);
                throw ex;
            }
        }

        public List<Type> getInterfaces() {
			try {//我加上的
			DEBUG.P(this,"getInterfaces()");

            complete();
            if (type instanceof ClassType) {
                ClassType t = (ClassType)type;
                if (t.interfaces_field == null) // FIXME: shouldn't be null
                    t.interfaces_field = List.nil();
                return t.interfaces_field;
            } else {
                return List.nil();
            }

			}finally{//我加上的
			DEBUG.P(0,this,"getInterfaces()");
			}
        }
        
        //特别注意:Symbol中的type字段和JCTree中的type字段是不想同的两个Type对象
        public Type getSuperclass() {
			try {//我加上的
			DEBUG.P(this,"getSuperclass()");

            complete();
            DEBUG.P("type.getClass().getName()="+type.getClass().getName());
            if (type instanceof ClassType) {
                ClassType t = (ClassType)type;
                if (t.supertype_field == null) // FIXME: shouldn't be null
                    t.supertype_field = Type.noType;
				// An interface has no superclass; its supertype is Object.
				return t.isInterface()
					? Type.noType
					: t.supertype_field;
            } else {
                return Type.noType;
            }

			}finally{//我加上的
			DEBUG.P(0,this,"getSuperclass()");
			}
        }
        
        //从这里看出ClassSymbol对应java源代码中
        //的注释类型定义、接口、枚举、普通类的定义
        public ElementKind getKind() {
            long flags = flags();
            if ((flags & ANNOTATION) != 0)
                return ElementKind.ANNOTATION_TYPE;
            else if ((flags & INTERFACE) != 0)
                return ElementKind.INTERFACE;
            else if ((flags & ENUM) != 0)
                return ElementKind.ENUM;
            else
                return ElementKind.CLASS;
        }

        public NestingKind getNestingKind() {
            complete();
            if (owner.kind == PCK)
                return NestingKind.TOP_LEVEL;
            else if (name.isEmpty())
                return NestingKind.ANONYMOUS;
            else if (owner.kind == MTH)
                return NestingKind.LOCAL;
            else
                return NestingKind.MEMBER;
        }

        /**
         * @deprecated this method should never be used by javac internally.
         */
        @Override @Deprecated
        public <A extends java.lang.annotation.Annotation> A getAnnotation(Class<A> annoType) {
            return JavacElements.getAnnotation(this, annoType);
        }

        public <R, P> R accept(ElementVisitor<R, P> v, P p) {
            return v.visitType(this, p);
        }
    }


    /** A class for variable symbols
     */
    public static class VarSymbol extends Symbol implements VariableElement {

        /** The variable's declaration position.
         */
        public int pos = Position.NOPOS;

        /** The variable's address. Used for different purposes during
         *  flow analysis, translation and code generation.
         *  Flow analysis:
         *    If this is a blank final or local variable, its sequence number.
         *  Translation:
         *    If this is a private field, its access number.
         *  Code generation:
         *    If this is a local variable, its logical slot number.
         */
        public int adr = -1;

        /** Construct a variable symbol, given its flags, name, type and owner.
         */
        public VarSymbol(long flags, Name name, Type type, Symbol owner) {
            super(VAR, flags, name, type, owner);
        }

        /** Clone this symbol with new owner.
         */
        public VarSymbol clone(Symbol newOwner) {
            VarSymbol v = new VarSymbol(flags_field, name, type, newOwner);
            v.pos = pos;
            v.adr = adr;
            v.data = data;
//          System.out.println("clone " + v + " in " + newOwner);//DEBUG
            return v;
        }

        public String toString() {
            return name.toString();
        }

        public Symbol asMemberOf(Type site, Types types) {
            return new VarSymbol(flags_field, name, types.memberType(site, this), owner);
        }

        public ElementKind getKind() {
            long flags = flags();
            if ((flags & PARAMETER) != 0) {
                if (isExceptionParameter())
                    return ElementKind.EXCEPTION_PARAMETER;
                else
                    return ElementKind.PARAMETER;
            } else if ((flags & ENUM) != 0) {
                return ElementKind.ENUM_CONSTANT;
            } else if (owner.kind == TYP || owner.kind == ERR) {
                return ElementKind.FIELD;
            } else {
                return ElementKind.LOCAL_VARIABLE;
            }
        }

        public <R, P> R accept(ElementVisitor<R, P> v, P p) {
            return v.visitVariable(this, p);
        }

        public Object getConstantValue() { // Mirror API
        	//com.sun.tools.javac.util.Constants
            return Constants.decode(getConstValue(), type);
        }

        public void setLazyConstValue(final Env<AttrContext> env,
                                      final Log log,
                                      final Attr attr,
                                      final JCTree.JCExpression initializer)
        {
            setData(new Callable<Object>() {
                public Object call() {
                    JavaFileObject source = log.useSource(env.toplevel.sourcefile);
                    try {
                        // In order to catch self-references, we set
                        // the variable's declaration position to
                        // maximal possible value, effectively marking
                        // the variable as undefined.
                        int pos = VarSymbol.this.pos;
                        VarSymbol.this.pos = Position.MAXPOS;
                        Type itype = attr.attribExpr(initializer, env, type);
                        VarSymbol.this.pos = pos;
                        if (itype.constValue() != null)
                            return attr.coerce(itype, type).constValue();
                        else
                            return null;
                    } finally {
                        log.useSource(source);
                    }
                }
            });
        }

        /**
         * The variable's constant value, if this is a constant.
         * Before the constant value is evaluated, it points to an
         * initalizer environment.  If this is not a constant, it can
         * be used for other stuff.
         */
        private Object data;

        public boolean isExceptionParameter() {
            return data == ElementKind.EXCEPTION_PARAMETER;
        }

        public Object getConstValue() {
            // TODO: Consider if getConstValue and getConstantValue can be collapsed
            if (data == ElementKind.EXCEPTION_PARAMETER) {
                return null;
            } else if (data instanceof Callable<?>) {
                // In this case, this is final a variable, with an as
                // yet unevaluated initializer.
                
                //是指java.util.concurrent.Callable<V>
                //javax.tools.JavaCompiler.CompilationTask是它的子接口
                Callable<?> eval = (Callable<?>)data;
                data = null; // to make sure we don't evaluate this twice.
                try {
                    data = eval.call();
                } catch (Exception ex) {
                    throw new AssertionError(ex);
                }
            }
            return data;
        }

        public void setData(Object data) {
            assert !(data instanceof Env<?>) : this;
            this.data = data;
        }
    }

    /** A class for method symbols.
     */
    public static class MethodSymbol extends Symbol implements ExecutableElement {

        /** The code of the method. */
        public Code code = null;

        /** The parameters of the method. */
        public List<VarSymbol> params = null;

        /** The names of the parameters */
        public List<Name> savedParameterNames;

        /** For an attribute field accessor, its default value if any.
         *  The value is null if none appeared in the method
         *  declaration.
         */
        public Attribute defaultValue = null;

        /** Construct a method symbol, given its flags, name, type and owner.
         */
        public MethodSymbol(long flags, Name name, Type type, Symbol owner) {
            super(MTH, flags, name, type, owner);
            assert owner.type.tag != TYPEVAR : owner + "." + name;
        }

        /** Clone this symbol with new owner.
         */
        public MethodSymbol clone(Symbol newOwner) {
            MethodSymbol m = new MethodSymbol(flags_field, name, type, newOwner);
            m.code = code;
            return m;
        }

        /** The Java source which this symbol represents.
         */
		
        public String toString() {
            if ((flags() & BLOCK) != 0) {
                return owner.name.toString();
            } else {
                String s = (name == name.table.init)
                    ? owner.name.toString()
                    : name.toString();
                if (type != null) {
                    if (type.tag == FORALL)
                        s = "<" + ((ForAll)type).getTypeArguments() + ">" + s;
                    s += "(" + type.argtypes((flags() & VARARGS) != 0) + ")";
                }
                return s;
            }
        }

        /** find a symbol that this (proxy method) symbol implements.
         *  @param    c       The class whose members are searched for
         *                    implementations
         */
        public Symbol implemented(TypeSymbol c, Types types) {
            Symbol impl = null;
            for (List<Type> is = types.interfaces(c.type);
                 impl == null && is.nonEmpty();
                 is = is.tail) {
                TypeSymbol i = is.head.tsym;
                for (Scope.Entry e = i.members().lookup(name);
                     impl == null && e.scope != null;
                     e = e.next()) {
                    if (this.overrides(e.sym, (TypeSymbol)owner, types, true) &&
                        // FIXME: I suspect the following requires a
                        // subst() for a parametric return type.
                        types.isSameType(type.getReturnType(),
                                         types.memberType(owner.type, e.sym).getReturnType())) {
                        impl = e.sym;
                    }
                    if (impl == null)
                        impl = implemented(i, types);
                }
            }
            return impl;
        }

        /** Will the erasure of this method be considered by the VM to
         *  override the erasure of the other when seen from class `origin'?
         */
        public boolean binaryOverrides(Symbol _other, TypeSymbol origin, Types types) {
            if (isConstructor() || _other.kind != MTH) return false;

            if (this == _other) return true;
            MethodSymbol other = (MethodSymbol)_other;

            // check for a direct implementation
            if (other.isOverridableIn((TypeSymbol)owner) &&
                types.asSuper(owner.type, other.owner) != null &&
                types.isSameType(erasure(types), other.erasure(types)))
                return true;

            // check for an inherited implementation
            return
                (flags() & ABSTRACT) == 0 &&
                other.isOverridableIn(origin) &&
                this.isMemberOf(origin, types) &&
                types.isSameType(erasure(types), other.erasure(types));
        }

        /** The implementation of this (abstract) symbol in class origin,
         *  from the VM's point of view, null if method does not have an
         *  implementation in class.
         *  @param origin   The class of which the implementation is a member.
         */
        public MethodSymbol binaryImplementation(ClassSymbol origin, Types types) {
            for (TypeSymbol c = origin; c != null; c = types.supertype(c.type).tsym) {
                for (Scope.Entry e = c.members().lookup(name);
                     e.scope != null;
                     e = e.next()) {
                    if (e.sym.kind == MTH &&
                        ((MethodSymbol)e.sym).binaryOverrides(this, origin, types))
                        return (MethodSymbol)e.sym;
                }
            }
            return null;
        }

        /** Does this symbol override `other' symbol, when both are seen as
         *  members of class `origin'?  It is assumed that _other is a member
         *  of origin.
         *
         *  It is assumed that both symbols have the same name.  The static
         *  modifier is ignored for this test.
         *
         *  See JLS 8.4.6.1 (without transitivity) and 8.4.6.4
         */
        //检查当前Symbol是否覆盖了Symbol _other
        //当前Symbol有可能是原始实现类(origin)或者超类中的方法
        public boolean overrides(Symbol _other, TypeSymbol origin, Types types, boolean checkResult) {
			boolean overrides=true;//我加上的
        	try {//我加上的
			DEBUG.P(this,"overrides(4)");
			DEBUG.P("this  ="+toString());
			DEBUG.P("_other="+_other);
			DEBUG.P("this.owner  ="+this.owner);
			DEBUG.P("_other.owner="+_other.owner);
			DEBUG.P("_other.kind ="+Kinds.toString(_other.kind));
			DEBUG.P("isConstructor()="+isConstructor());
            if (isConstructor() || _other.kind != MTH) return overrides=false;
            
            DEBUG.P("");
			DEBUG.P("TypeSymbol origin="+origin);
			DEBUG.P("boolean checkResult="+checkResult);
            DEBUG.P("(this == _other)="+(this == _other));
            if (this == _other) return true;
            MethodSymbol other = (MethodSymbol)_other;

            // check for a direct implementation
            
            /*在判断当前方法能否覆盖other方法前，先调用isOverridableIn
			判别other方法的修饰符(PRIVATE,PUBLIC,PROTECTED或没有)
			是否能在当前方法的owner中覆盖other，比如说，如果other
			方法的修饰符是PRIVATE，那么在owner中不能覆盖他。

			如果isOverridableIn返回true了，还必须确认other方法的owner
			是当前当前方法的owner的超类
			*/
            if (other.isOverridableIn((TypeSymbol)owner) &&
                types.asSuper(owner.type, other.owner) != null) {
                Type mt = types.memberType(owner.type, this);
                Type ot = types.memberType(owner.type, other);
                if (types.isSubSignature(mt, ot)) {
                    if (!checkResult) //检查方法返回类型
                        return true;
                    if (types.returnTypeSubstitutable(mt, ot))
                        return true;
                }
            }
			DEBUG.P("");
			DEBUG.P("this  ="+toString());
			DEBUG.P("_other="+_other);
			DEBUG.P("this.owner  ="+this.owner);
			DEBUG.P("_other.owner="+_other.owner);
			DEBUG.P("");
			DEBUG.P("this.flags() ="+Flags.toString(this.flags()));
			DEBUG.P("other.flags()="+Flags.toString(other.flags()));

            // check for an inherited implementation
            if ((flags() & ABSTRACT) != 0 ||
                (other.flags() & ABSTRACT) == 0 ||
                !other.isOverridableIn(origin) ||
                !this.isMemberOf(origin, types))
                return overrides=false;

            // assert types.asSuper(origin.type, other.owner) != null;
            Type mt = types.memberType(origin.type, this);
            Type ot = types.memberType(origin.type, other);
            return overrides=
                types.isSubSignature(mt, ot) &&
                (!checkResult || types.resultSubtype(mt, ot, Warner.noWarnings));
            }finally{//我加上的
			DEBUG.P("overrides="+overrides);
			DEBUG.P(1,this,"overrides(4)");
			}
        }
        
        //根据方法前的修饰符(PRIVATE,PUBLIC,PROTECTED或没有)
        //来决定实现类是否能覆盖此方法
        private boolean isOverridableIn(TypeSymbol origin) {
			/*
            // JLS3 8.4.6.1
            switch ((int)(flags_field & Flags.AccessFlags)) {
            case Flags.PRIVATE:
                return false;
            case Flags.PUBLIC:
                return true;
            case Flags.PROTECTED:
                return (origin.flags() & INTERFACE) == 0;
            case 0:
                // for package private: can only override in the same
                // package
                return
                    this.packge() == origin.packge() &&
                    (origin.flags() & INTERFACE) == 0;
            default:
                return false;
            }
			*/

			boolean isOverridableIn=false;
			DEBUG.P(this,"isOverridableIn(TypeSymbol origin)");
			DEBUG.P("flags_field="+Flags.toString(flags_field));
			DEBUG.P("flags_field & AccessFlags="+Flags.toString(flags_field & AccessFlags));

			DEBUG.P("  this="+toString()+"    this.owner="+this.owner);
			DEBUG.P("this.packge()="+this.packge());
			DEBUG.P("origin.packge()="+origin.packge());
			DEBUG.P("origin="+origin);
			DEBUG.P("origin.flags_field="+Flags.toString(origin.flags_field));

			switch ((int)(flags_field & Flags.AccessFlags)) {
            case Flags.PRIVATE:
                isOverridableIn= false;break;
            case Flags.PUBLIC:
                isOverridableIn= true;break;
            case Flags.PROTECTED:
                isOverridableIn= (origin.flags() & INTERFACE) == 0;break;
            case 0:
                // for package private: can only override in the same
                // package
                isOverridableIn=
                    this.packge() == origin.packge() &&
                    (origin.flags() & INTERFACE) == 0;break;
            default:
                isOverridableIn= false;
            }

			DEBUG.P("");
			DEBUG.P("isOverridableIn="+isOverridableIn);
			DEBUG.P(0,this,"isOverridableIn(TypeSymbol origin)");
			return isOverridableIn;
        }

        /** The implementation of this (abstract) symbol in class origin;
         *  null if none exists. Synthetic methods are not considered
         *  as possible implementations.
         */
        public MethodSymbol implementation(TypeSymbol origin, Types types, boolean checkResult) {
        	//当前的MethodSymbol代表一个抽象方法，检查origin类及超类中是否实现了该方法
        	try {//我加上的
			DEBUG.P(this,"implementation(3)");
			DEBUG.P("TypeSymbol origin="+origin);
			DEBUG.P("boolean checkResult="+checkResult);
			
            for (Type t = origin.type; t.tag == CLASS; t = types.supertype(t)) {
                TypeSymbol c = t.tsym;
                DEBUG.P("第一层for:");
                DEBUG.P("TypeSymbol c="+c);
                DEBUG.P("c.members()="+c.members());
                DEBUG.P("lookup(name)="+name);
                DEBUG.P("t.tag="+TypeTags.toString(t.tag));
                for (Scope.Entry e = c.members().lookup(name);
                     e.scope != null;
                     e = e.next()) {
                    DEBUG.P("第二层for:");
                    DEBUG.P("e.sym="+e.sym);
                    DEBUG.P("e.scope="+e.scope);
                    DEBUG.P("e.sym.kind="+Kinds.toString(e.sym.kind));
                    if (e.sym.kind == MTH) {
                        MethodSymbol m = (MethodSymbol) e.sym;
                        
						//m有可能是原始实现类(origin)或者超类中的方法，this是被实现的抽象方法
                        boolean overrides=m.overrides(this, origin, types, checkResult);
						//如果非abstract类中含有abstract方法，m和this是指向这个非abstract类中
						//的同一个方法，在调用overrides方法时，
						//有一条“if (this == _other) return true;”的语句，
						//也就是说，直接就认为他们相互覆盖。
                        DEBUG.P("overrides="+overrides);
                        if(overrides) {
                        	if((m.flags() & SYNTHETIC) == 0) {
                        		DEBUG.P(m+".flags() 没有SYNTHETIC");
                        		return m;
                        	}
                        }
                        /*		
                        if (m.overrides(this, origin, types, checkResult) &&
                            (m.flags() & SYNTHETIC) == 0)
                            return m;
                            */
                    }
                }
            }
            DEBUG.P("结束第一层for");
            DEBUG.P("origin.type="+origin.type);
            // if origin is derived from a raw type, we might have missed
            // an implementation because we do not know enough about instantiations.
            // in this case continue with the supertype as origin.
            if (types.isDerivedRaw(origin.type))
                return implementation(types.supertype(origin.type).tsym, types, checkResult);
            else
                return null;
                
            }finally{//我加上的
			DEBUG.P(0,this,"implementation(3)");
			}
        }

        public List<VarSymbol> params() {
            owner.complete();
            if (params == null) {
                List<Name> names = savedParameterNames;
                savedParameterNames = null;
                if (names == null) {
                    names = List.nil();
                    int i = 0;
                    for (Type t : type.getParameterTypes())
                        names = names.prepend(name.table.fromString("arg" + i++));
                    names = names.reverse();
                }
                ListBuffer<VarSymbol> buf = new ListBuffer<VarSymbol>();
                for (Type t : type.getParameterTypes()) {
                    buf.append(new VarSymbol(PARAMETER, names.head, t, this));
                    names = names.tail;
                }
                params = buf.toList();
            }
            return params;
        }

        public Symbol asMemberOf(Type site, Types types) {
            return new MethodSymbol(flags_field, name, types.memberType(site, this), owner);
        }

        public ElementKind getKind() {
            if (name == name.table.init)
                return ElementKind.CONSTRUCTOR;
            else if (name == name.table.clinit)
                return ElementKind.STATIC_INIT;
            else
                return ElementKind.METHOD;
        }

        public Attribute getDefaultValue() {
            return defaultValue;
        }

        public List<VarSymbol> getParameters() {
            return params();
        }

        public boolean isVarArgs() {
            return (flags() & VARARGS) != 0;
        }

        public <R, P> R accept(ElementVisitor<R, P> v, P p) {
            return v.visitExecutable(this, p);
        }

        public Type getReturnType() {
            return asType().getReturnType();
        }

        public List<Type> getThrownTypes() {
            return asType().getThrownTypes();
        }
    }

    /** A class for predefined operators.
     */
    public static class OperatorSymbol extends MethodSymbol {

        public int opcode;

        public OperatorSymbol(Name name, Type type, int opcode, Symbol owner) {
            super(PUBLIC | STATIC, name, type, owner);
            this.opcode = opcode;
        }
    }

    /** Symbol completer interface.
     */
    public static interface Completer {
        void complete(Symbol sym) throws CompletionFailure;
    }

    public static class CompletionFailure extends RuntimeException {
        private static final long serialVersionUID = 0;
        public Symbol sym;

        /** A localized string describing the failure.
         */
        public String errmsg;

        public CompletionFailure(Symbol sym, String errmsg) {
            this.sym = sym;
            this.errmsg = errmsg;
//          this.printStackTrace();//DEBUG
        }

        public String getMessage() {
            return errmsg;
        }

        @Override
        public CompletionFailure initCause(Throwable cause) {
            super.initCause(cause);
            return this;
        }

    }
}
