package com.sun.tools.javac.code;

import javax.lang.model.element.Element;
import javax.lang.model.type.*;
import com.sun.tools.javac.util.*;
import com.sun.tools.javac.code.Symbol.*;
import javax.lang.model.element.Element;//重复了

import javax.lang.model.type.*;//重复了

import static com.sun.tools.javac.code.Flags.*;
import static com.sun.tools.javac.code.Kinds.*;
import static com.sun.tools.javac.code.BoundKind.*;
import static com.sun.tools.javac.code.TypeTags.*;

/** This class represents Java types. The class itself defines the behavior of
 *  the following types:
 *  <pre>
 *  base types (tags: BYTE, CHAR, SHORT, INT, LONG, FLOAT, DOUBLE, BOOLEAN),
 *  type `void' (tag: VOID),
 *  the bottom type (tag: BOT),
 *  the missing type (tag: NONE).
 *  </pre>
 *  <p>The behavior of the following types is defined in subclasses, which are
 *  all static inner classes of this class:
 *  <pre>
 *  class types (tag: CLASS, class: ClassType),
 *  array types (tag: ARRAY, class: ArrayType),
 *  method types (tag: METHOD, class: MethodType),
 *  package types (tag: PACKAGE, class: PackageType),
 *  type variables (tag: TYPEVAR, class: TypeVar),
 *  type arguments (tag: WILDCARD, class: WildcardType),
 *  polymorphic types (tag: FORALL, class: ForAll),
 *  the error type (tag: ERROR, class: ErrorType).
 *  </pre>
 *
 *  <p><b>This is NOT part of any API supported by Sun Microsystems.  If
 *  you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 *
 *  @see TypeTags
 */
@Version("@(#)Type.java	1.104 07/03/21")
public class Type implements PrimitiveType {
	private static my.Debug DEBUG=new my.Debug(my.Debug.Type);//我加上的

    /** Constant type: no type at all. */
    public static final JCNoType noType = new JCNoType(NONE);//是指com.sun.tools.javac.code.TypeTags.NONE

    /** If this switch is turned on, the names of type variables
     *  and anonymous classes are printed with hashcodes appended.
     */
    //在com.sun.tools.javac.main.RecognizedOptions类中根据“moreInfo”选项设置
    public static boolean moreInfo = false;

    /** The tag of this type.
     *
     *  @see TypeTags
     */
    //注意两个类名:
    //com.sun.tools.javac.code.TypeTags与
    //javax.lang.model.type.TypeKind
    //这两个类大体上是对应的，
    //前者主要用在javac源码内部，而后者是java API的一部份。
    //tag字段的值是取自com.sun.tools.javac.code.TypeTags类，
    //但是getKind()方法的返回值取自javax.lang.model.type.TypeKind类。
    public int tag;

    /** The defining class / interface / package / type variable
     */
    public TypeSymbol tsym;

    /**
     * The constant value of this type, null if this type does not
     * have a constant value attribute. Only primitive types and
     * strings (ClassType) can have a constant value attribute.
     * @return the constant value attribute of this type
     */
    public Object constValue() {
        return null;
    }

    public <R,S> R accept(Type.Visitor<R,S> v, S s) { return v.visitType(this, s); }

    /** Define a type given its tag and type symbol
     */
    public Type(int tag, TypeSymbol tsym) {
        this.tag = tag;
        this.tsym = tsym;
    }

    /** An abstract class for mappings from types to types
     */
    public static abstract class Mapping {
        private String name;
        public Mapping(String name) {
            this.name = name;
        }
        public abstract Type apply(Type t);//在类Types中有很多匿名内部类实现该方法
        public String toString() {
            return name;
        }
    }

    /** map a type function over all immediate descendants of this type
     */
    public Type map(Mapping f) {
        return this;
    }

    /** map a type function over a list of types
     */
    public static List<Type> map(List<Type> ts, Mapping f) {
    	
    	if (ts.nonEmpty()) {
            List<Type> tail1 = map(ts.tail, f);//递归调用自己
            Type t = f.apply(ts.head);
            if (tail1 != ts.tail || t != ts.head)
                return tail1.prepend(t);
        }
        return ts;
        
        
        /*输出例子:
        class com.sun.tools.javac.code.Type===>map(2)
		-------------------------------------------------------------------------
		ts1=long,int  ts1.size()=2  Mapping f=erasure
		class com.sun.tools.javac.code.Type===>map(2)
		-------------------------------------------------------------------------
		ts1=int  ts1.size()=1  Mapping f=erasure
		tail1=  tail1.size()=0
		(tail1 != ts.tail)=false
		(t != ts.head)=false
		ts2=int  ts2.size()=1
		class com.sun.tools.javac.code.Type===>map(2)  END
		-------------------------------------------------------------------------
		tail1=int  tail1.size()=1
		(tail1 != ts.tail)=false
		(t != ts.head)=false
		ts2=long,int  ts2.size()=2
		class com.sun.tools.javac.code.Type===>map(2)  END
		-------------------------------------------------------------------------
		
		
		class com.sun.tools.javac.code.Type===>map(2)
		-------------------------------------------------------------------------
		ts1=java.lang.InterruptedException  ts1.size()=1  Mapping f=erasure
		tail1=  tail1.size()=0
		(tail1 != ts.tail)=false
		(t != ts.head)=true
		ts2=java.lang.InterruptedException  ts2.size()=1
		class com.sun.tools.javac.code.Type===>map(2)  END
		-------------------------------------------------------------------------
		class com.sun.tools.javac.code.Type===>map(2)
		-------------------------------------------------------------------------
		ts1=long  ts1.size()=1  Mapping f=erasure
		tail1=  tail1.size()=0
		(tail1 != ts.tail)=false
		(t != ts.head)=false
		ts2=long  ts2.size()=1
		class com.sun.tools.javac.code.Type===>map(2)  END
		-------------------------------------------------------------------------
		*/
		
		/*
        List<Type> ts2=ts;
        try {//我加上的
	        if (ts.nonEmpty()) {
				DEBUG.P(Type.class,"map(2)");
				DEBUG.P("ts1="+ts+"  ts1.size()="+ts.size()+"  Mapping f="+f);
			}

	        if (ts.nonEmpty()) {
	            List<Type> tail1 = map(ts.tail, f);//递归调用自己
	            DEBUG.P("tail1="+tail1+"  tail1.size()="+tail1.size());
	            Type t = f.apply(ts.head);
	            DEBUG.P("(tail1 != ts.tail)="+(tail1 != ts.tail));
	            DEBUG.P("(t != ts.head)="+(t != ts.head));
	            if (tail1 != ts.tail || t != ts.head) {
	            	ts2=tail1.prepend(t);
	            	return ts2;
	                //return tail1.prepend(t);
	            }
	        }
	        return ts;

		}finally{//我加上的
			if (ts.nonEmpty()) {
				DEBUG.P("ts2="+ts+"  ts2.size()="+ts.size());
				DEBUG.P(0,Type.class,"map(2)");
			}
		}
		*/
    }

    /** Define a constant type, of the same kind as this type
     *  and with given constant value
     */
    public Type constType(Object constValue) {
        final Object value = constValue;
        assert tag <= BOOLEAN;//必须是基本类型
        return new Type(tag, tsym) {
                @Override
                public Object constValue() {
                    return value;
                }
                @Override
                public Type baseType() {
                    return tsym.type;
                }
            };
    }

    /**
     * If this is a constant type, return its underlying type.
     * Otherwise, return the type itself.
     */
    public Type baseType() {
        return this;
    }

    /** Return the base types of a list of types.
     */
    public static List<Type> baseTypes(List<Type> ts) {
    	try {//我加上的
		DEBUG.P(Type.class,"baseTypes(List<Type> ts)");
		DEBUG.P("ts1="+ts);
		
		
        if (ts.nonEmpty()) {
            Type t = ts.head.baseType();
            List<Type> baseTypes = baseTypes(ts.tail);
            if (t != ts.head || baseTypes != ts.tail)
                return baseTypes.prepend(t);
        }
        return ts;
        

		}finally{//我加上的
		DEBUG.P("ts2="+ts);
		DEBUG.P(0,Type.class,"baseTypes(List<Type> ts)");
		}

    }

    /** The Java source which this type represents.
     */
    public String toString() {
        String s = (tsym == null || tsym.name == null)
            ? "<none>"
            : tsym.name.toString();
        //指定了“-moreinfo”选项且又是一个TypeVar时,加上hashCode
        //例如:S30426707,T12122157,E28145575
        if (moreInfo && tag == TYPEVAR) s = s + hashCode();
        return s;
    }

    /**
     * The Java source which this type list represents.  A List is
     * represented as a comma-spearated listing of the elements in
     * that list.
     */
    public static String toString(List<Type> ts) {
        if (ts.isEmpty()) {
            return "";
        } else {
            StringBuffer buf = new StringBuffer();
            buf.append(ts.head.toString());
            for (List<Type> l = ts.tail; l.nonEmpty(); l = l.tail)
                buf.append(",").append(l.head.toString());
            return buf.toString();
        }
    }

    /**
     * The constant value of this type, converted to String
     */
    public String stringValue() {
        assert constValue() != null;
        if (tag == BOOLEAN)
            return ((Integer) constValue()).intValue() == 0 ? "false" : "true";
        else if (tag == CHAR)
            return String.valueOf((char) ((Integer) constValue()).intValue());
        else
            return constValue().toString();
    }

    /**
     * This method is analogous to isSameType, but weaker, since we
     * never complete classes. Where isSameType would complete a
     * class, equals assumes that the two types are different.
     */
    public boolean equals(Object t) {
        return super.equals(t);
    }

    public int hashCode() {
        return super.hashCode();//实际上是Object.hashCode()，这是一个本地方法
    }

    /** Is this a constant type whose value is false?
     */
    public boolean isFalse() {
        return
            tag == BOOLEAN &&
            constValue() != null &&
            ((Integer)constValue()).intValue() == 0;
    }

    /** Is this a constant type whose value is true?
     */
    public boolean isTrue() {
        return
            tag == BOOLEAN &&
            constValue() != null &&
            ((Integer)constValue()).intValue() != 0;
    }
    
    //返回方法参数的类型字符串
    public String argtypes(boolean varargs) {
    	//注意getParameterTypes()返回的是方法参数的类型，而不是TypeParameter。
    	//在javac的类型系统的所有源代码中，
    	//关于方法参数的类型与TypeParameter(用于泛型定义)的源代码有点混乱。
        List<Type> args = getParameterTypes();
        if (!varargs) return args.toString();
        StringBuffer buf = new StringBuffer();
        while (args.tail.nonEmpty()) {
            buf.append(args.head);
            args = args.tail;
            buf.append(',');
        }
        if (args.head.tag == ARRAY) {//可变长度的数组变量一定是方法参数的最后一个
            buf.append(((ArrayType)args.head).elemtype);
            buf.append("...");
        } else {
            buf.append(args.head);
        }
        return buf.toString();
    }

    /** Access methods.
     */
    //返回的是所有TypeParameter的type(type的tag一般是TYPEVAR)
    public List<Type>        getTypeArguments()  { return List.nil(); }
    
    public Type              getEnclosingType() { return null; }
    
    //方法参数的类型
    public List<Type>        getParameterTypes() { return List.nil(); }
    
    public Type              getReturnType()     { return null; }
    public List<Type>        getThrownTypes()    { return List.nil(); }
    public Type              getUpperBound()     { return null; }

    public void setThrown(List<Type> ts) {
        throw new AssertionError();
    }

    /** Navigation methods, these will work for classes, type variables,
     *  foralls, but will return null for arrays and methods.
     */

   /** Return all parameters of this type and all its outer types in order
    *  outer (first) to inner (last).
    */
    //返回的是所有(outer及inner的)TypeParameter的type(type的tag一般是TYPEVAR)
    public List<Type> allparams() { return List.nil(); }

    /** Does this type contain "error" elements?
     */
    public boolean isErroneous() {
        return false;
    }

    public static boolean isErroneous(List<Type> ts) {
        for (List<Type> l = ts; l.nonEmpty(); l = l.tail)
            if (l.head.isErroneous()) return true;
        return false;
    }

    /** Is this type parameterized?
     *  A class type is parameterized if it has some parameters.
     *  An array type is parameterized if its element type is parameterized.
     *  All other types are not parameterized.
     */
    //适用于两种类型ClassType与ArrayType
    public boolean isParameterized() {
        return false;
    }

    /** Is this type a raw type?
     *  A class type is a raw type if it misses some of its parameters.
     *  An array type is a raw type if its element type is raw.
     *  All other types are not raw.
     *  Type validation will ensure that the only raw types
     *  in a program are types that miss all their type variables.
     */
    //没有type variables的Type
    public boolean isRaw() {
        return false;
    }
    
    //type variables有多个bound的情况,如<T extends ClassA & InterfaceA>
    public boolean isCompound() {
        return tsym.completer == null
            // Compound types can't have a completer.  Calling
            // flags() will complete the symbol causing the
            // compiler to load classes unnecessarily.  This led
            // to regression 6180021.
            && (tsym.flags() & COMPOUND) != 0;
    }

    public boolean isInterface() {
        return (tsym.flags() & INTERFACE) != 0;
    }

    public boolean isPrimitive() {
        return tag < VOID; //tag比VOID小的都是基本类型
    }

    /**
     * Does this type contain occurrences of type t?
     */
    public boolean contains(Type t) {
        return t == this;
    }

    public static boolean contains(List<Type> ts, Type t) {
        for (List<Type> l = ts;
             l.tail != null /*inlined: l.nonEmpty()*/;
             l = l.tail)
            if (l.head.contains(t)) return true;
        return false;
    }

    /** Does this type contain an occurrence of some type in `elems'?
     */
    public boolean containsSome(List<Type> ts) {
        for (List<Type> l = ts; l.nonEmpty(); l = l.tail)
            if (this.contains(ts.head)) return true;
        return false;
    }

    public boolean isSuperBound() { return false; }
    public boolean isExtendsBound() { return false; }
    public boolean isUnbound() { return false; }
    
    //只有子类WildcardType覆盖了这个方法
    public Type withTypeVar(Type t) { return this; }
    
    //在com.sun.tools.javac.comp.Check$Validator===>visitTypeApply(1)中有应用
    public static List<Type> removeBounds(List<Type> ts) {
    	DEBUG.P(Type.class,"removeBounds(1)");
    	DEBUG.P("ts="+ts);
    	
        ListBuffer<Type> result = new ListBuffer<Type>();
        for(;ts.nonEmpty(); ts = ts.tail) {
            result.append(ts.head.removeBounds());
        }
        
        DEBUG.P("result="+result.toList());
        DEBUG.P(1,Type.class,"removeBounds(1)");
        return result.toList();
    }
    
    //只有子类WildcardType覆盖了这个方法
    public Type removeBounds() {
    	try {//我加上的
		DEBUG.P(this,"removeBounds()");
		DEBUG.P("this="+toString());
		
		return this;
		
		}finally{//我加上的
		DEBUG.P(0,this,"removeBounds()");
		}
    }

    /** The underlying method type of this type.
     */
    public MethodType asMethodType() { throw new AssertionError(); }

    /** Complete loading all classes in this type.
     */
    public void complete() {}

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

	//只有MethodType覆盖了这个方法且返回的是null
    public TypeSymbol asElement() {
        return tsym;
    }

    public TypeKind getKind() {
        switch (tag) {
        case BYTE:      return TypeKind.BYTE;
        case CHAR:      return TypeKind.CHAR;
        case SHORT:     return TypeKind.SHORT;
        case INT:       return TypeKind.INT;
        case LONG:      return TypeKind.LONG;
        case FLOAT:     return TypeKind.FLOAT;
        case DOUBLE:    return TypeKind.DOUBLE;
        case BOOLEAN:   return TypeKind.BOOLEAN;
        case VOID:      return TypeKind.VOID;
        case BOT:       return TypeKind.NULL;//这个比较特殊
        case NONE:      return TypeKind.NONE;
        default:        return TypeKind.OTHER;
        }
    }

    public <R, P> R accept(TypeVisitor<R, P> v, P p) {
        if (isPrimitive())
            return v.visitPrimitive(this, p);
        else
            throw new AssertionError();
    }