/*
 * @(#)Context.java	1.23 07/03/21
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

package com.sun.tools.javac.util;

import com.sun.tools.javac.Main;
import java.util.*;

/**
 * Support for an abstract context, modelled loosely after ThreadLocal
 * but using a user-provided context instead of the current thread.
 *
 * <p>Within the compiler, a single Context is used for each
 * invocation of the compiler.  The context is then used to ensure a
 * single copy of each compiler phase exists per compiler invocation.
 *
 * <p>The context can be used to assist in extending the compiler by
 * extending its components.  To do that, the extended component must
 * be registered before the base component.  We break initialization
 * cycles by (1) registering a factory for the component rather than
 * the component itself, and (2) a convention for a pattern of usage
 * in which each base component registers itself by calling an
 * instance method that is overridden in extended components.  A base
 * phase supporting extension would look something like this:
 *
 * <p><pre>
 * public class Phase {
 *     protected static final Context.Key<Phase> phaseKey =
 *	   new Context.Key<Phase>();
 *
 *     public static Phase instance(Context context) {
 *	   Phase instance = context.get(phaseKey);
 *	   if (instance == null)
 *	       // the phase has not been overridden
 *	       instance = new Phase(context);
 *	   return instance;
 *     }
 *
 *     protected Phase(Context context) {
 *	   context.put(phaseKey, this);
 *	   // other intitialization follows...
 *     }
 * }
 * </pre>
 *
 * <p>In the compiler, we simply use Phase.instance(context) to get
 * the reference to the phase.  But in extensions of the compiler, we
 * must register extensions of the phases to replace the base phase,
 * and this must be done before any reference to the phase is accessed
 * using Phase.instance().  An extended phase might be declared thus:
 *
 * <p><pre>
 * public class NewPhase extends Phase {
 *     protected NewPhase(Context context) {
 *	   super(context);
 *     }
 *     public static void preRegister(final Context context) {
 *         context.put(phaseKey, new Context.Factory<Phase>() {
 *	       public Phase make() {
 *		   return new NewPhase(context);
 *	       }
 *         });
 *     }
 * }
 * </pre>
 *
 * <p>And is registered early in the extended compiler like this
 *
 * <p><pre>
 *     NewPhase.preRegister(context);
 * </pre>
 *
 *  <p><b>This is NOT part of any API supported by Sun Microsystems.  If
 *  you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 */
@Version("@(#)Context.java	1.23 07/03/21")
public class Context {
	private static my.Debug DEBUG=new my.Debug(my.Debug.Context);//我加上的
	
    /** The client creates an instance of this class for each key.
     */
    public static class Key<T> {
    	/*都是错误的用法
    	//T t=new T();
    	//我加上的
    	public String toString() {
    		return T.class+"";
    		//return "Key<"+t.getClass().getName()+">";
    	}
    	*/
	// note: we inherit identity equality from Object.
    }

    /**
     * The client can register a factory for lazy creation of the
     * instance.
     */
    public static interface Factory<T> {
	T make();
    };
    
    //我加上的
    public String toString() {
    	String lineSeparator=System.getProperty("line.separator");
    	StringBuffer sb=new StringBuffer();
    	//sb.append(lineSeparator);
    	/*
    	if(ht==null) sb.append("ht=null");
    	else {
	    	sb.append("ht.[size=").append(ht.size());	
		    for(Map.Entry<Key,Object> myMapEntry: ht.entrySet())
		        	sb.append(", ").append(myMapEntry);
		    sb.append("]");
		}
		sb.append(System.getProperty("line.separator"));
		
		if(kt==null) sb.append("kt=null");
    	else {
	    	sb.append("kt.[size=").append(kt.size());	
		    for(Map.Entry<Class<?>, Key<?>> myMapEntry: kt.entrySet())
		        	sb.append(", ").append(myMapEntry);
		    sb.append("]");
		}
		*/
		
		if(ht==null) sb.append("Map<Key,Object> ht=null");
    	else {
	    	sb.append("Map<Key,Object> ht.size=").append(ht.size());	
	    	if(ht.size()>0) {
	    	sb.append(lineSeparator);
	    	sb.append("---------------------------------------------");
	    	sb.append(lineSeparator);
		    for(Map.Entry<Key,Object> myMapEntry: ht.entrySet()) {
		    	sb.append("Key   =").append(myMapEntry.getKey());
		    	sb.append(lineSeparator);
                        
                        Object o=myMapEntry.getValue();
                        if(o!=null) {
                            sb.append("Object=").append(o.getClass().getName());
                            if(o instanceof Factory)
                                sb.append(" [instanceof Factory]");
                        }
		    	else sb.append("Object=").append(o);
                                
		    	//if(myMapEntry.getValue()!=null)
		    	//sb.append("Object=").append(myMapEntry.getValue().getClass().getName());
		    	//else sb.append("Object=").append(myMapEntry.getValue());
		    	
		    	sb.append(lineSeparator);
		    	sb.append(lineSeparator);
		    }
		    sb.append("---------------------------------------------");
			}
		}
		sb.append(lineSeparator);
		sb.append(lineSeparator);
		if(kt==null) sb.append("Map<Class<?>, Key<?>> kt=null");
    	else {
	    	sb.append("Map<Class<?>, Key<?>> kt.size=").append(kt.size());	
	    	if(kt.size()>0) {
	    	sb.append(lineSeparator);
	    	sb.append("---------------------------------------------");
	    	sb.append(lineSeparator);
		    for(Map.Entry<Class<?>, Key<?>> myMapEntry: kt.entrySet()) {
		    	sb.append("Class<?>=").append(myMapEntry.getKey());
		    	sb.append(lineSeparator);
		    	sb.append("Key<?>  =").append(myMapEntry.getValue());
		    	sb.append(lineSeparator);
		    	sb.append(lineSeparator);
		    }
		    sb.append("---------------------------------------------");
			}
		}
	    return sb.toString();
    }

    /**
     * The underlying map storing the data.
     * We maintain the invariant that this table contains only
     * mappings of the form
     * Key<T> -> T or Key<T> -> Factory<T> */
    private Map<Key,Object> ht = new HashMap<Key,Object>();

    /** Set the factory for the key in this context. */
    public <T> void put(Key<T> key, Factory<T> fac) {
    DEBUG.P(this,"put(Key<T> key, Factory<T> fac)");
	//DEBUG.P("context前="+toString())
	if(fac!=null)
		DEBUG.P("fac="+fac.getClass().getName());
	else DEBUG.P("fac="+fac);
		    	
	
	checkState(ht);
	Object old = ht.put(key, fac);
	if (old != null)
	    throw new AssertionError("duplicate context value");
	
	DEBUG.P("context后="+toString());
	DEBUG.P(0,this,"put(Key<T> key, Factory<T> fac)");
    }

    /** Set the value for the key in this context. */
    public <T> void put(Key<T> key, T data) {
    DEBUG.P(this,"put(Key<T> key, T data)");
    if(data!=null)
		DEBUG.P("data="+data.getClass().getName());
	else DEBUG.P("data="+data);
	//DEBUG.P("context前="+toString());
	
    /*例如:
    Context context = new Context();
    Context.Key<Context.Factory> factoryKey =new Context.Key<Context.Factory>();
	context.put(factoryKey,new Context.Factory<String>(){public String make() {return "";}});
    
    出现如下类似异常:
    Exception in thread "main" java.lang.AssertionError: T extends Context.Factory
    
    因为Context.Key<T>的参数化类型不允许是Context.Key<Context.Factory>
    */    
	if (data instanceof Factory)
	    throw new AssertionError("T extends Context.Factory");
	checkState(ht);
	Object old = ht.put(key, data);
	if (old != null && !(old instanceof Factory) && old != data && data != null)
	    throw new AssertionError("duplicate context value");
	
	DEBUG.P("context后="+toString());
	DEBUG.P(0,this,"put(Key<T> key, T data)");
    }

    /** Get the value for the key in this context. */
    public <T> T get(Key<T> key) {
        try {
        DEBUG.P(this,"get(Key<T> key)");
        //if(key!=null) DEBUG.P("key="+key.getClass().getName());
	//else DEBUG.P("key="+key);
        DEBUG.P("key="+key);
        
	checkState(ht);
	Object o = ht.get(key);
	
        if(o!=null) DEBUG.P("o="+o.getClass().getName());
	else DEBUG.P("o="+o);
        
        DEBUG.P("(o instanceof Factory)="+(o instanceof Factory));
        
	if (o instanceof Factory) {
	    Factory fac = (Factory)o;
	    o = fac.make();
	    if (o instanceof Factory)
		throw new AssertionError("T extends Context.Factory");
            //也就是说在调用make()时已把make()返回的结果放入ht(例子见:JavacFileManager.preRegister())
	    assert ht.get(key) == o;
	}

	/* The following cast can't fail unless there was
	 * cheating elsewhere, because of the invariant on ht.
	 * Since we found a key of type Key<T>, the value must
	 * be of type T.
	 */
	 return Context.<T>uncheckedCast(o);
         
         } finally {
         DEBUG.P(0,this,"get(Key<T> key)"); 
         }
	/*
	注意这里的“<T>”与 “private static <T> T uncheckedCast(Object o)”中
	的“<T>”的差别，前者表示的是get(Key<T> key)方法中的“T”的实际类型，
	假设最开始:
	-------------------------------------
	Context context = new Context();
	Context.Key<Number> numberKey = new Context.Key<Number>();
	Number number=new Number();
	context.put(numberKey,number);
	
	number=context.get(numberKey);
	------------------------------------
	这时把“numberKey”传进“get(Key<T> key)”方法，
	因为参数“numberKey”是“Context.Key<Number>”类型，
	所以get(Key<T> key)中的“T”的实际类型是“Number”。
	
	但是“get(Key<T> key)”方法里的“Object o = ht.get(key)”，
	变量“o”是Object类型的，必需调用“Context.<T>uncheckedCast(o)”将
	变量“o”引用的Object实例转换成“Number”类型，
	这样“number=context.get(numberKey)”得到的结果才正确。
	“Context.<T>uncheckedCast(o)”相当于把“Object o”转换成“<T>”(这里就是Number)
	
	像“Context.<T>uncheckedCast(o)”这种语法确实很古怪，
	这主要是由于“private static <T> T uncheckedCast(Object o)”方法的定义
	造成的，这个方法是一个静态泛型方法，而且只有返回类型与泛型变量“<T>”相关，
	方法参数只有( Object o )，并且不与泛型变量“<T>”相关，
	当要调用uncheckedCast方法时，可以用下面的格式调用它:
	Context.<返回值类型>uncheckedCast(参数Object o)
	
	返回值类型可以是泛型变量
	(泛型变量的实际类型在编译期间只能推断它的上限绑定，
	具体是什么类型只能在运行期间确定)
	
	
	假设最开始:
	Object o=new String("str");
	那么可以这样调用它:
	String str=Context.<String>uncheckedCast(o);(等价于: String str=(String)o)
	
	把“return Context.<T>uncheckedCast(o);”与uncheckedCast方法的调用格式对照
	“<T>”对应“<返回值类型>”，“(o)”对应“(参数Object o)”
	
	另外“uncheckedCast”方法的定义如下:
	--------------------------------------
	@SuppressWarnings("unchecked")
    private static <T> T uncheckedCast(Object o) {
        return (T)o;
    }
    --------------------------------------
    注释“@SuppressWarnings("unchecked")”间接指出了uncheckedCast方法会在
    运行时有可能产生转换异常(java.lang.ClassCastException)，
    因为(T)的类型是未确定的，
    比如(T)的类型可能是“Number”，当参数(Object o)实际是String的实例引用时，
    这时“(T)o”就等价于“(Number)String”，显然是不对的。
	*/
    }

    public Context() {}

    private Map<Class<?>, Key<?>> kt = new HashMap<Class<?>, Key<?>>();
    
    private <T> Key<T> key(Class<T> clss) {
        DEBUG.P(this,"key(Class<T> clss)");
        if(clss!=null) DEBUG.P("clss="+clss.getName());
	else DEBUG.P("clss="+clss);
        
	checkState(kt);
	//等价于Key<T> k = Context.<Key<T>>uncheckedCast(kt.get(clss));
	//因为kt.get(clss)返回的类型是Key<T>，刚好与等式左边的类型一样
	Key<T> k = uncheckedCast(kt.get(clss));
        
        DEBUG.P("k="+k);
        
	if (k == null) {
	    k = new Key<T>();
	    kt.put(clss, k);
	}
        
        DEBUG.P(0,this,"key(Class<T> clss)");
	return k;
    }

    public <T> T get(Class<T> clazz) {
        try {
        DEBUG.P(this,"get(Class<T> clazz)");
        if(clazz!=null) DEBUG.P("clazz="+clazz.getName());
	else DEBUG.P("clazz="+clazz);
        
	return get(key(clazz));
        
        } finally {
        DEBUG.P(0,this,"get(Class<T> clazz)");    
        }
    }

    public <T> void put(Class<T> clazz, T data) {
    DEBUG.P(this,"put(Class<T> clazz, T data)");
    if(data!=null)
		DEBUG.P("data="+data.getClass().getName());
	else DEBUG.P("data="+data);
	//DEBUG.P("context前="+toString());
	
	put(key(clazz), data);
	
	//DEBUG.P("context后="+toString());
	DEBUG.P(0,this,"put(Class<T> clazz, T data)");
    }
    public <T> void put(Class<T> clazz, Factory<T> fac) {
    DEBUG.P(this,"put(Class<T> clazz, Factory<T> fac)");
    if(fac!=null)
		DEBUG.P("fac="+fac.getClass().getName());
	else DEBUG.P("fac="+fac);
	//DEBUG.P("context前="+toString());
    
	put(key(clazz), fac);
	
	//DEBUG.P("context后="+toString());
	DEBUG.P(0,this,"put(Class<T> clazz, Factory<T> fac)");
    }

    /**
     * TODO: This method should be removed and Context should be made type safe.
     * This can be accomplished by using class literals as type tokens.
     */
    @SuppressWarnings("unchecked")
    private static <T> T uncheckedCast(Object o) {
        return (T)o;
    }

    public void dump() {
	for (Object value : ht.values())
	    System.err.println(value == null ? null : value.getClass());
    }

    public void clear() {
	ht = null;
	kt = null;
    }
    
    private static void checkState(Map<?,?> t) {
	if (t == null)
	    throw new IllegalStateException();
    }
}
