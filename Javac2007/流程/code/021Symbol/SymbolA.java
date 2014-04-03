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
        DEBUG.P("owner.hasOuterInstance()="+owner.hasOuterInstance());
        
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







































